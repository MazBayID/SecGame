package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.data.execution.ChaquopyScriptExecutionEngine
import com.mazbayid.aegisnet.data.execution.SharedPreferencesRunReceiptRepository
import com.mazbayid.aegisnet.data.vfs.SharedPreferencesVfsRepository
import com.mazbayid.aegisnet.domain.execution.RunCoordinator
import com.mazbayid.aegisnet.domain.execution.RunCoordinatorResult
import com.mazbayid.aegisnet.domain.editor.EditorDocument
import com.mazbayid.aegisnet.domain.editor.EditorDocumentService
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspace
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mazbayid.aegisnet.ui.components.AegisPanel

private const val EDITOR_OPERATION_ID = "first_contact_01"

@Composable
fun EditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { SharedPreferencesVfsRepository(context) }
    val runReceiptRepository = remember(context) { SharedPreferencesRunReceiptRepository(context) }
    val scope = rememberCoroutineScope()
    val restoreResult = remember(repository) {
        runCatching {
            VirtualFileSystem.fromSnapshots(
                operationId = EDITOR_OPERATION_ID,
                personal = repository.loadPersonal(),
                mission = repository.loadMission(EDITOR_OPERATION_ID)
            )
        }
    }

    if (restoreResult.isFailure) {
        AegisPanel(title = "EDITOR / SAVE RECOVERY") {
            Text(
                "Saved workspace could not be read. The original save was preserved.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(10.dp))
            Button(onClick = onBack) { Text("BACK") }
        }
        return
    }

    val fileSystem = remember(restoreResult) { restoreResult.getOrThrow() }
    var workspace by remember { mutableStateOf(VfsWorkspace.PERSONAL) }
    var path by remember { mutableStateOf("scripts/analysis.py") }
    var document by remember { mutableStateOf<EditorDocument?>(null) }
    var status by remember { mutableStateOf("Open a writable script from the virtual workspace.") }
    var statusIsError by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var runOutput by remember { mutableStateOf("") }
    var lastRunHash by remember { mutableStateOf<String?>(null) }

    fun persistWorkspace(): Boolean = when (workspace) {
        VfsWorkspace.PERSONAL -> repository.savePersonal(fileSystem.snapshot(workspace))
        VfsWorkspace.MISSION -> repository.saveMission(
            EDITOR_OPERATION_ID,
            fileSystem.snapshot(workspace)
        )
    }

    AegisPanel(
        title = "EDITOR / SOURCE WORKSPACE",
        trailing = {
            Text(
                if (document?.isDirty == true) "UNSAVED" else "LOCAL",
                color = if (document?.isDirty == true) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.primary,
                fontSize = 8.sp
            )
        }
    ) {
        Text(
            "PERSONAL and MISSION workspaces are isolated. Mission source files may be read-only. RUN uses the bounded in-process Python runtime.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !isRunning,
                onClick = {
                    workspace = VfsWorkspace.PERSONAL
                    fileSystem.switchWorkspace(workspace)
                    document = null
                    runOutput = ""
                    status = "Personal workspace selected."
                    statusIsError = false
                }
            ) { Text("PERSONAL") }
            Button(
                enabled = !isRunning,
                onClick = {
                    workspace = VfsWorkspace.MISSION
                    fileSystem.switchWorkspace(workspace)
                    document = null
                    runOutput = ""
                    status = "Mission workspace selected."
                    statusIsError = false
                }
            ) { Text("MISSION") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = path,
            onValueChange = { path = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning,
            singleLine = true,
            label = { Text("Virtual file path") }
        )
        Spacer(Modifier.height(6.dp))
        Button(
            enabled = !isRunning,
            onClick = {
                runOutput = ""
                when (val result = EditorDocumentService.open(fileSystem, path)) {
                    is VfsResult.Success -> {
                        document = result.value
                        runOutput = ""
                        lastRunHash = null
                        status = "Opened ${result.value.path} · ${result.value.access.name}"
                        statusIsError = false
                    }
                    is VfsResult.Failure -> {
                        status = "${result.code}: ${result.message}"
                        statusIsError = true
                    }
                }
            }
        ) { Text("OPEN FILE") }

        Spacer(Modifier.height(10.dp))
        document?.let { current ->
            Text(
                "${current.path}  /  ${current.origin.name}",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = current.buffer,
                onValueChange = { updated ->
                    document = EditorDocumentService.update(current, updated)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 9,
                maxLines = 16,
                readOnly = current.access.name == "READ_ONLY" || isRunning,
                label = { Text("Source buffer") }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "BUFFER SHA-256  ${EditorDocumentService.sha256(current.buffer)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        when (val result = EditorDocumentService.save(fileSystem, current)) {
                            is VfsResult.Success -> {
                                document = result.value
                                if (persistWorkspace()) {
                                    status = "Saved exact buffer to ${result.value.path}."
                                    statusIsError = false
                                } else {
                                    status = "SAVE ERROR: local persistence was not confirmed."
                                    statusIsError = true
                                }
                            }
                            is VfsResult.Failure -> {
                                status = "${result.code}: ${result.message}"
                                statusIsError = true
                            }
                        }
                    },
                    enabled = current.isDirty && current.access.name != "READ_ONLY" && !isRunning,
                    modifier = Modifier.weight(1f)
                ) { Text("SAVE SOURCE") }

                Button(
                    onClick = {
                        isRunning = true
                        runOutput = "Starting Python 3.13 teaching runtime..."
                        scope.launch {
                            try {
                                val result = withContext(Dispatchers.Default) {
                                    RunCoordinator(
                                        operationId = EDITOR_OPERATION_ID,
                                        fileSystem = fileSystem,
                                        engine = ChaquopyScriptExecutionEngine(context)
                                    ).run(current)
                                }
                                when (result) {
                                    is RunCoordinatorResult.Executed -> {
                                        val attempt = result.attempt
                                        lastRunHash = attempt.snapshot.sourceHash
                                        val persisted = withContext(Dispatchers.IO) {
                                            persistWorkspace() && runReceiptRepository.save(attempt.receipt)
                                        }
                                        document = when (
                                            val reopened = EditorDocumentService.open(fileSystem, current.path)
                                        ) {
                                            is VfsResult.Success -> reopened.value
                                            is VfsResult.Failure -> current.copy(
                                                buffer = attempt.snapshot.source,
                                                savedContent = attempt.snapshot.source
                                            )
                                        }
                                        runOutput = buildString {
                                            append("EXIT CODE: ${attempt.execution.exitCode}\n")
                                            append("SOURCE SHA-256: ${attempt.receipt.sourceHash}\n\n")
                                            if (attempt.execution.stdout.isNotEmpty()) {
                                                append(attempt.execution.stdout)
                                            }
                                            if (attempt.execution.stderr.isNotEmpty()) {
                                                append("\n").append(attempt.execution.stderr)
                                            }
                                            if (attempt.execution.stdout.isEmpty() && attempt.execution.stderr.isEmpty()) {
                                                append("(no output)")
                                            }
                                            if (!persisted) {
                                                append("\n\nWARNING: workspace or RUN receipt was not saved.")
                                            }
                                        }
                                        status = when {
                                            !persisted -> "RUN finished, but local persistence failed."
                                            attempt.receipt.successful -> "RUN succeeded. Exact source snapshot recorded."
                                            else -> "RUN failed. Review the output before retrying."
                                        }
                                        statusIsError = !persisted || !attempt.receipt.successful
                                    }
                                    is RunCoordinatorResult.Rejected -> {
                                        runOutput = "${result.code}: ${result.message}"
                                        status = "RUN was rejected before execution."
                                        statusIsError = true
                                    }
                                }
                            } catch (error: Exception) {
                                runOutput = error.message ?: "Unexpected RUN failure."
                                status = "RUN failed before a receipt could be saved."
                                statusIsError = true
                            } finally {
                                isRunning = false
                            }
                        }
                    },
                    enabled = current.access.name != "READ_ONLY" && !isRunning,
                    modifier = Modifier.weight(1f)
                ) { Text(if (isRunning) "RUNNING..." else "RUN PYTHON") }
            }
            if (lastRunHash != null) {
                Text(
                    "LAST RUN SNAPSHOT  ${lastRunHash}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp
                )
            }
            if (runOutput.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    runOutput,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            status,
            color = if (statusIsError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(10.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("RETURN TO APP HUB")
        }
    }
}
