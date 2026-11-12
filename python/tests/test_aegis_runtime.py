import json
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / "app" / "src" / "main" / "python"))
import aegis_runtime


class FakeAegis:
    def __init__(self):
        self.files = {"logs/system.log": "INFO boot\nERROR timeout\nERROR retry"}

    def read_text(self, path):
        if path not in self.files:
            raise FileNotFoundError(path)
        return self.files[path]

    def write_text(self, path, content):
        self.files[path] = content
        return path

    def list_files(self, path="."):
        prefix = "" if path == "." else path.rstrip("/") + "/"
        return sorted(name for name in self.files if name.startswith(prefix))


class AegisRuntimeTests(unittest.TestCase):
    def run_code(self, source, bridge=None):
        result = aegis_runtime.run_script(source, bridge or FakeAegis())
        return json.loads(result)

    def test_variables_and_print(self):
        result = self.run_code("signal = 'ACCESS GRANTED'\nprint(signal)")
        self.assertEqual(0, result["exit_code"])
        self.assertEqual("ACCESS GRANTED\n", result["stdout"])

    def test_for_if_and_error_count(self):
        source = (
            "log = aegis.read_text('logs/system.log').split('\\n')\n"
            "error_count = 0\n"
            "for line in log:\n"
            "    if 'ERROR' in line:\n"
            "        error_count += 1\n"
            "print(error_count)"
        )
        result = self.run_code(source)
        self.assertEqual(0, result["exit_code"])
        self.assertEqual("2\n", result["stdout"])

    def test_len_enumerate_and_source_indexing(self):
        source = (
            "records = ['RX-02', 'RX-14', 'RX-19']\n"
            "print(len(records))\n"
            "for index, record in enumerate(records):\n"
            "    if record == 'RX-14':\n"
            "        print(index, records[index])"
        )
        result = self.run_code(source)
        self.assertEqual(0, result["exit_code"])
        self.assertEqual("3\n1 RX-14\n", result["stdout"])

    def test_int_and_absolute_difference(self):
        source = (
            "aegis_time = int('12')\n"
            "archive_time = int('16')\n"
            "print(archive_time - aegis_time)\n"
            "print(abs(aegis_time - archive_time))"
        )
        result = self.run_code(source)
        self.assertEqual(0, result["exit_code"])
        self.assertEqual("4\n4\n", result["stdout"])

    def test_vfs_write_and_read_stays_in_bridge(self):
        bridge = FakeAegis()
        result = self.run_code(
            "aegis.write_text('reports/result.txt', 'verified')\n"
            "print(aegis.read_text('reports/result.txt'))",
            bridge
        )
        self.assertEqual(0, result["exit_code"])
        self.assertEqual("verified\n", result["stdout"])
        self.assertEqual("verified", bridge.files["reports/result.txt"])

    def test_import_and_reflection_are_rejected(self):
        for source in ("import os", "print((1).__class__)", "print(__builtins__)"):
            result = self.run_code(source)
            self.assertNotEqual(0, result["exit_code"])
            self.assertTrue(result["stderr"])

    def test_host_file_open_is_not_available(self):
        result = self.run_code("open('/tmp/aegis-test', 'w')")
        self.assertNotEqual(0, result["exit_code"])
        self.assertIn("not available", result["stderr"])

    def test_infinite_loop_hits_step_limit(self):
        result = self.run_code("while True:\n    pass")
        self.assertEqual(124, result["exit_code"])
        self.assertTrue(result["timed_out"])

    def test_source_and_output_limits(self):
        source_limit = self.run_code("x = '" + ("a" * (aegis_runtime.MAX_SOURCE_CHARS + 1)) + "'")
        output_limit = self.run_code("print('x' * 20000)")
        self.assertNotEqual(0, source_limit["exit_code"])
        self.assertNotEqual(0, output_limit["exit_code"])


if __name__ == "__main__":
    unittest.main()
