"""Restricted, deterministic Python teaching runtime for AEGIS-Net.

This is an in-process gameplay boundary, not a security sandbox. The script
language deliberately excludes imports, reflection, host file access and
arbitrary attribute access. All file operations go through the supplied VFS
bridge object.
"""
import ast
import builtins
import io
import json
import sys

MAX_SOURCE_CHARS = 16_384
MAX_AST_NODES = 5_000
MAX_COLLECTION_ITEMS = 10_000
MAX_EXECUTION_STEPS = 30_000
MAX_OUTPUT_CHARS = 16_000
VFS_METHODS = {"read_text", "write_text", "list_files"}
SAFE_STRING_METHODS = {"split", "strip", "lower", "upper", "count", "startswith", "endswith", "find", "index", "replace"}

class ExecutionLimitExceeded(Exception):
    pass

class OutputLimitExceeded(Exception):
    pass

class LimitedOutput:
    def __init__(self, limit):
        self.limit = limit
        self.parts = []
        self.length = 0

    def write(self, value):
        value = str(value)
        if self.length + len(value) > self.limit:
            raise OutputLimitExceeded("Terminal output limit exceeded.")
        self.parts.append(value)
        self.length += len(value)
        return len(value)

    def flush(self):
        return None

    def getvalue(self):
        return "".join(self.parts)


def _bounded_range(*args):
    value = builtins.range(*args)
    try:
        length = len(value)
    except OverflowError as error:
        raise ValueError("range is too large for this operation.") from error
    if length > MAX_COLLECTION_ITEMS:
        raise ValueError("range exceeds the allowed item limit.")
    return value


def _bounded_list(iterable=()):
    result = []
    for item in iterable:
        if len(result) >= MAX_COLLECTION_ITEMS:
            raise ValueError("list exceeds the allowed item limit.")
        result.append(item)
    return result


def _validate(tree):
    nodes = list(ast.walk(tree))
    if len(nodes) > MAX_AST_NODES:
        raise ValueError("Script is too complex.")

    forbidden = (
        ast.Import, ast.ImportFrom, ast.Global, ast.Nonlocal,
        ast.FunctionDef, ast.AsyncFunctionDef, ast.ClassDef, ast.Lambda,
        ast.With, ast.AsyncWith, ast.Try, ast.Raise, ast.Delete,
        ast.Await, ast.Yield, ast.YieldFrom,
    )
    for node in nodes:
        if isinstance(node, forbidden):
            raise ValueError("This Python feature is not available in the training runtime.")
        if isinstance(node, ast.Name) and node.id.startswith("_"):
            raise ValueError("Names beginning with '_' are not available.")
        if isinstance(node, ast.Attribute):
            allowed_vfs = (
                isinstance(node.value, ast.Name)
                and node.value.id == "aegis"
                and node.attr in VFS_METHODS
            )
            allowed_string_method = node.attr in SAFE_STRING_METHODS
            allowed = allowed_vfs or allowed_string_method
            if not allowed:
                raise ValueError("Only the documented aegis VFS methods are available.")
        if isinstance(node, ast.Call):
            if isinstance(node.func, ast.Name):
                if node.func.id not in SAFE_BUILTINS:
                    raise ValueError("This function is not available in the training runtime.")
            elif isinstance(node.func, ast.Attribute):
                allowed_vfs = (
                    isinstance(node.func.value, ast.Name)
                    and node.func.value.id == "aegis"
                    and node.func.attr in VFS_METHODS
                )
                allowed_string_method = node.func.attr in SAFE_STRING_METHODS
                if not (allowed_vfs or allowed_string_method):
                    raise ValueError("Only the documented aegis VFS methods may be called.")
            else:
                raise ValueError("Dynamic function calls are not available.")
        if isinstance(node, ast.Constant):
            if isinstance(node.value, int) and abs(node.value) > 1_000_000:
                raise ValueError("Integer literal exceeds the allowed limit.")
            if isinstance(node.value, str) and len(node.value) > MAX_SOURCE_CHARS:
                raise ValueError("Text literal exceeds the allowed limit.")
        if isinstance(node, ast.BinOp) and isinstance(
            node.op, (ast.Pow, ast.MatMult, ast.LShift, ast.RShift)
        ):
            raise ValueError("This operator is not available in the training runtime.")


def _safe_print(*args, **kwargs):
    if "file" in kwargs:
        raise ValueError("Redirecting print output is not available.")
    builtins.print(*args, file=_ACTIVE_OUTPUT[0], **kwargs)


def _safe_builtins():
    return {
        "print": _safe_print,
        "len": len,
        "enumerate": enumerate,
        "int": int,
        "abs": abs,
        "range": _bounded_range,
        "str": str,
        "float": float,
        "bool": bool,
        "list": _bounded_list,
        "tuple": tuple,
        "dict": dict,
        "set": set,
        "min": min,
        "max": max,
        "sum": sum,
        "sorted": sorted,
        "zip": zip,
        "any": any,
        "all": all,
        "round": round,
    }


SAFE_BUILTINS = set(_safe_builtins())
_ACTIVE_OUTPUT = [None]


def run_script(source, aegis):
    """Run one source snapshot and return a JSON result for Kotlin."""
    if not isinstance(source, str):
        return json.dumps({
            "exit_code": 1, "stdout": "", "stderr": "TypeError: source must be text.",
            "timed_out": False
        })
    if len(source) > MAX_SOURCE_CHARS:
        return json.dumps({
            "exit_code": 1, "stdout": "", "stderr": "Source size limit exceeded.",
            "timed_out": False
        })

    output = LimitedOutput(MAX_OUTPUT_CHARS)
    _ACTIVE_OUTPUT[0] = output
    timed_out = False
    previous_trace = sys.gettrace()
    steps = [0]

    try:
        tree = ast.parse(source, filename="<aegis-script>", mode="exec")
        _validate(tree)

        def trace(frame, event, arg):
            if event == "line":
                steps[0] += 1
                if steps[0] > MAX_EXECUTION_STEPS:
                    raise ExecutionLimitExceeded("Execution step limit exceeded.")
            return trace

        namespace = {
            "__builtins__": _safe_builtins(),
            "aegis": aegis,
        }
        sys.settrace(trace)
        try:
            exec(compile(tree, "<aegis-script>", "exec"), namespace, namespace)
        finally:
            sys.settrace(previous_trace)

        return json.dumps({
            "exit_code": 0, "stdout": output.getvalue(), "stderr": "",
            "timed_out": False
        })
    except SyntaxError as error:
        detail = "SyntaxError: {}".format(error.msg)
        if error.lineno is not None:
            detail += " (line {})".format(error.lineno)
        return json.dumps({
            "exit_code": 1, "stdout": output.getvalue(), "stderr": detail,
            "timed_out": False
        })
    except ExecutionLimitExceeded as error:
        timed_out = True
        return json.dumps({
            "exit_code": 124, "stdout": output.getvalue(), "stderr": str(error),
            "timed_out": True
        })
    except Exception as error:
        return json.dumps({
            "exit_code": 1, "stdout": output.getvalue(),
            "stderr": "{}: {}".format(type(error).__name__, error),
            "timed_out": timed_out
        })
    finally:
        sys.settrace(previous_trace)
        _ACTIVE_OUTPUT[0] = None
