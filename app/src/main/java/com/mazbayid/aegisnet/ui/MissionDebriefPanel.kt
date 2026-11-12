package com.mazbayid.aegisnet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazbayid.aegisnet.domain.mission.MissionCriterionResult
import com.mazbayid.aegisnet.domain.mission.MissionEvaluation

private enum class DebriefDimension(val label: String) {
    INTELLIGENCE("INTELLIGENCE"),
    EVIDENCE("EVIDENCE"),
    TECHNICAL("TECHNICAL"),
    JUDGMENT("JUDGMENT")
}

/**
 * Groups actual evaluator criteria into a mission debrief. Only dimensions
 * represented by active criteria are shown; no fictional scores are invented.
 */
@Composable
fun MissionDebriefPanel(evaluation: MissionEvaluation) {
    val grouped = evaluation.criteria.groupBy(::dimensionFor)
    val passedCount = evaluation.criteria.count { it.passed }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (evaluation.accepted) "OPERATION DEBRIEF / ACCEPTED" else "SUBMISSION REVIEW / REVISION REQUIRED",
            color = if (evaluation.accepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp
        )
        Text(
            "CRITERIA: $passedCount / ${evaluation.criteria.size} PASSED",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        DebriefDimension.entries.forEach { dimension ->
            val criteria = grouped[dimension].orEmpty()
            if (criteria.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    dimension.label,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                criteria.forEach { criterion ->
                    DebriefCriterionRow(criterion)
                }
            }
        }
    }
}

@Composable
private fun DebriefCriterionRow(criterion: MissionCriterionResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (criterion.passed) "PASS" else "FAIL",
            color = if (criterion.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            criterion.message,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}

private fun dimensionFor(criterion: MissionCriterionResult): DebriefDimension =
    when {
        criterion.objectiveId == "report_unresolved_cause" -> DebriefDimension.JUDGMENT
        criterion.code.contains("evidence") -> DebriefDimension.EVIDENCE
        criterion.code.contains("run") ||
            criterion.code.contains("source") ||
            criterion.code.contains("output") -> DebriefDimension.TECHNICAL
        criterion.code.contains("approach") ||
            criterion.code.contains("outcome") -> DebriefDimension.INTELLIGENCE
        else -> DebriefDimension.JUDGMENT
    }
