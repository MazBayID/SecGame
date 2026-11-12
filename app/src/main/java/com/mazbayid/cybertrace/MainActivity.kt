package com.mazbayid.cybertrace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.cybertrace.domain.learning.LearningLesson
import com.mazbayid.cybertrace.domain.learning.PythonCurriculum

private val Background = Color(0xFF070B12)
private val Panel = Color(0xFF0D1420)
private val Border = Color(0xFF1B3544)
private val Neon = Color(0xFF55F2C2)
private val Muted = Color(0xFF7E91A5)
private val MainText = Color(0xFFE2EDF5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.rgb(7, 11, 18)
        window.navigationBarColor = android.graphics.Color.rgb(7, 11, 18)

        setContent {
            MaterialTheme {
                CyberTraceApp()
            }
        }
    }
}

@Composable
private fun CyberTraceApp() {
    var showingFirstLesson by remember { mutableStateOf(false) }

    if (showingFirstLesson) {
        FirstLessonScreen(onBack = { showingFirstLesson = false })
    } else {
        CyberTraceStartScreen(onStartLesson = { showingFirstLesson = true })
    }
}

@Composable
private fun CyberTraceStartScreen(onStartLesson: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "CYBER//TRACE",
                        color = MainText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "NEXUS CITY  /  2089",
                        color = Muted,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                }
                StatusIndicator()
            }

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "INCOMING TRANSMISSION",
                color = Neon,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "The network\nis not what it seems.",
                color = MainText,
                fontSize = 32.sp,
                lineHeight = 39.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "A signal has appeared in the noise. Someone is trying to reach you.",
                color = Muted,
                fontSize = 15.sp,
                lineHeight = 23.sp
            )

            Spacer(modifier = Modifier.height(28.dp))
            TransmissionPanel()

            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = onStartLesson,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Neon,
                    contentColor = Background
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "OPEN FIRST TRANSMISSION",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "PHASE 01  /  SYSTEM INITIALIZATION",
                color = Muted,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FOUNDATION BUILD  •  0.2.0",
                color = Neon,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun FirstLessonScreen(onBack: () -> Unit) {
    val lesson = PythonCurriculum.firstLesson
    var currentStepIndex by remember { mutableStateOf(0) }
    var practiceSource by remember(lesson.id) { mutableStateOf(lesson.starterCode) }
    val currentStep = lesson.steps[currentStepIndex]
    val isLastStep = currentStepIndex == lesson.steps.lastIndex

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CYBER//TRACE  /  TRAINING",
                    color = Neon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                TextButton(onClick = onBack) {
                    Text("EXIT", color = Muted)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "CHAPTER ${lesson.chapterId}  •  LESSON ${lesson.id}",
                color = Neon,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lesson.title,
                color = MainText,
                fontSize = 27.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))
            StoryPanel(lesson.storyIntroduction)

            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEARNING STEPS",
                    color = Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${currentStepIndex + 1} / ${lesson.steps.size}",
                    color = Neon,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                lesson.steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (index <= currentStepIndex) Neon else Border,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            LessonStepPanel(
                number = currentStepIndex + 1,
                heading = currentStep.heading,
                explanation = currentStep.explanation,
                exampleCode = currentStep.exampleCode,
                expectedOutput = currentStep.expectedOutput
            )

            if (isLastStep) {
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "GUIDED PRACTICE",
                    color = Neon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lesson.practicePrompt,
                    color = MainText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                PythonWorkspacePreview(
                    source = practiceSource,
                    onSourceChange = { practiceSource = it }
                )

                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Border, RoundedCornerShape(8.dp))
                        .background(Panel, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "WORKSPACE STATUS",
                        color = Muted,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Python runtime integration is in progress.",
                        color = MainText,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "This is a lesson preview. Code execution will be enabled only after the isolated interpreter passes its safety checks.",
                        color = Muted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = { currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0) },
                    enabled = currentStepIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("PREVIOUS", color = if (currentStepIndex > 0) MainText else Muted)
                }
                Button(
                    onClick = {
                        if (!isLastStep) currentStepIndex += 1
                    },
                    enabled = !isLastStep,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Neon,
                        contentColor = Background,
                        disabledContainerColor = Border,
                        disabledContentColor = Muted
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isLastStep) "LESSON PREVIEW" else "NEXT STEP",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PythonWorkspacePreview(
    source: String,
    onSourceChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .background(Panel, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "PYTHON EDITOR  /  LOCAL DRAFT",
            color = Neon,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = source,
            onValueChange = onSourceChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .border(1.dp, Border, RoundedCornerShape(6.dp))
                .background(Color(0xFF05090F), RoundedCornerShape(6.dp))
                .padding(12.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = MainText,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (source.isEmpty()) {
                        Text(
                            text = "Write Python code here...",
                            color = Muted,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = Border,
                disabledContentColor = Muted
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "RUN  /  RUNTIME NOT CONNECTED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "OUTPUT",
            color = Muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "No execution yet. Your draft is editable, but it is not being run.",
            color = Muted,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun LessonStepPanel(
    number: Int,
    heading: String,
    explanation: String,
    exampleCode: String?,
    expectedOutput: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .background(Panel, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "STEP ${number.toString().padStart(2, '0')}",
            color = Neon,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = heading,
            color = MainText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = explanation,
            color = Muted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        if (exampleCode != null) {
            Spacer(modifier = Modifier.height(10.dp))
            CodePanel(exampleCode)
        }
        if (expectedOutput != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OUTPUT  >  $expectedOutput",
                color = Neon,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun StoryPanel(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .background(Panel, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "ARIA  /  INCOMING MESSAGE",
            color = Neon,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MainText,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun CodePanel(code: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(6.dp))
            .background(Color(0xFF05090F), RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ">>>",
            color = Neon,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = code,
            color = MainText,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StatusIndicator() {
    Row(
        modifier = Modifier
            .border(1.dp, Border, RoundedCornerShape(6.dp))
            .background(Panel, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(6.dp)
                .background(Neon, RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = "LOCAL SYSTEM",
            color = Neon,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun TransmissionPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .background(Panel, RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "UNKNOWN SOURCE",
            color = Muted,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "> connection established",
            color = Neon,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "> encrypted packet detected",
            color = MainText,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "> awaiting decryption module...",
            color = Muted,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
