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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.data.vfs.SharedPreferencesVfsRepository
import com.mazbayid.aegisnet.domain.editor.EditorDocument
import com.mazbayid.aegisnet.domain.editor.EditorDocumentService
import com.mazbayid.aegisnet.domain.vfs.VfsResult
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspace
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import com.mazbayid.aegisnet.ui.components.AegisPanel

private const val EDITOR_OPERATION_ID = "first_contact_01"

@Composable
fun EditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { SharedPreferencesVfsRepository(context) }
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
            "PERSONAL and MISSION workspaces are isolated. Mission source files may be read-only.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    workspace = VfsWorkspace.PERSONAL
                    fileSystem.switchWorkspace(workspace)
                    document = null
                    status = "Personal workspace selected."
                    statusIsError = false
                }
            ) { Text("PERSONAL") }
            Button(
                onClick = {
                    workspace = VfsWorkspace.MISSION
                    fileSystem.switchWorkspace(workspace)
                    document = null
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
            singleLine = true,
            label = { Text("Virtual file path") }
        )
        Spacer(Modifier.height(6.dp))
        Button(
            onClick = {
                when (val result = EditorDocumentService.open(fileSystem, path)) {
                    is VfsResult.Success -> {
                        document = result.value
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
                readOnly = current.access.name == "READ_ONLY",
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
                enabled = current.isDirty && current.access.name != "READ_ONLY"
            ) { Text("SAVE SOURCE") }
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
