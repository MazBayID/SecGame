package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.data.vfs.SharedPreferencesVfsRepository
import com.mazbayid.aegisnet.domain.terminal.TerminalDispatcher
import com.mazbayid.aegisnet.domain.terminal.TerminalOutputKind
import com.mazbayid.aegisnet.domain.vfs.VfsWorkspace
import com.mazbayid.aegisnet.domain.vfs.VirtualFileSystem
import com.mazbayid.aegisnet.ui.components.AegisPanel

private const val ACTIVE_OPERATION_ID = "first_contact_01"

private data class TerminalLine(
    val command: String?,
    val output: String,
    val isError: Boolean
)

@Composable
fun TerminalScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { SharedPreferencesVfsRepository(context) }
    val restoreResult = remember(repository) {
        runCatching {
            VirtualFileSystem.fromSnapshots(
                operationId = ACTIVE_OPERATION_ID,
                personal = repository.loadPersonal(),
                mission = repository.loadMission(ACTIVE_OPERATION_ID)
            )
        }
    }

    if (restoreResult.isFailure) {
        AegisPanel(title = "TERMINAL / SAVE RECOVERY") {
            Text(
                "Saved workspace could not be read. The original save has been preserved; no files were overwritten.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(12.dp))
            TerminalActionButton("RETURN TO APP HUB", onBack)
        }
        return
    }

    val fileSystem = remember(restoreResult) { restoreResult.getOrThrow() }
    val dispatcher = remember(fileSystem) { TerminalDispatcher(fileSystem) }
    val history = remember { mutableStateListOf<TerminalLine>() }
    var input by remember { mutableStateOf("") }
    var saveError by remember { mutableStateOf(false) }
    val historyScroll = rememberScrollState()

    LaunchedEffect(history.size) {
        historyScroll.animateScrollTo(historyScroll.maxValue)
    }

    fun submitCommand() {
        val command = input.trim()
        if (command.isEmpty()) return

        val output = dispatcher.execute(command)
        if (output.kind == TerminalOutputKind.CLEAR) {
            history.clear()
        } else {
            history += TerminalLine(
                command = command,
                output = output.text,
                isError = output.kind == TerminalOutputKind.ERROR
            )
            if (history.size > 100) history.removeAt(0)
        }

        val personalSaved = repository.savePersonal(fileSystem.snapshot(VfsWorkspace.PERSONAL))
        val missionSaved = repository.saveMission(
            ACTIVE_OPERATION_ID,
            fileSystem.snapshot(VfsWorkspace.MISSION)
        )
        saveError = !personalSaved || !missionSaved
        input = ""
    }

    AegisPanel(
        title = "TERMINAL / LOCAL WORKSPACE",
        trailing = {
            Text(
                "SIMULATED VFS",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 8.sp
            )
        }
    ) {
        Text(
            "This is an in-game command environment. It does not run Android shell commands.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .verticalScroll(historyScroll),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "AEGIS-NET VIRTUAL CONSOLE  /  OFFLINE",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
            if (history.isEmpty()) {
                Text(
                    "Type 'help' to list available commands.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
            history.forEach { line ->
                line.command?.let {
                    Text(
                        "operator@aegis:$ $it",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
                if (line.output.isNotEmpty()) {
                    Text(
                        line.output,
                        color = if (line.isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (saveError) {
            Spacer(Modifier.height(6.dp))
            Text(
                "SAVE ERROR — command ran in memory, but workspace persistence was not confirmed.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 9.sp
            )
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("In-game command") },
            placeholder = { Text("help") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submitCommand() })
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { submitCommand() }, modifier = Modifier.weight(1f)) {
                Text("EXECUTE")
            }
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("BACK")
            }
        }
    }
}

@Composable
private fun TerminalActionButton(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        onClick = onClick
    ) {
        Text(
            label,
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp
        )
    }
}
