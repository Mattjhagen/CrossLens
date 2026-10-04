package com.crosslens.app.feature.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosslens.app.core.model.ClusterConfidence
import com.crosslens.app.core.model.EventIntegrityMetadata
import com.crosslens.app.core.model.FindingSeverity
import com.crosslens.app.core.model.IntegrityCheckResult
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * DEBUG-ONLY Event Integrity Monitor Screen.
 *
 * Shows factual cluster-quality signals for recent events.
 * Excluded from release builds.
 *
 * DOES NOT infer ideology, bias, or truthfulness.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventIntegrityScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventIntegrityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Integrity Monitor [DEBUG]") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is EventIntegrityUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EventIntegrityUiState.Success -> {
                EventIntegrityContent(
                    clusters = state.clusters,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is EventIntegrityUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EventIntegrityContent(
    clusters: List<EventIntegrityDisplayData>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ DEBUG DIAGNOSTICS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Factual clustering signals. Does NOT infer ideology, bias, or truthfulness.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Summary stats
        item {
            IntegritySummaryCard(clusters)
        }

        // Cluster list
        items(clusters) { data ->
            ClusterIntegrityCard(data)
        }
    }
}

@Composable
private fun IntegritySummaryCard(clusters: List<EventIntegrityDisplayData>) {
    val passed = clusters.count { it.checkResult.passed }
    val failed = clusters.size - passed

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Clusters: ${clusters.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✅ Passed: $passed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "❌ Failed: $failed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ClusterIntegrityCard(data: EventIntegrityDisplayData) {
    val metadata = data.metadata
    val checkResult = data.checkResult

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with pass/fail indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (checkResult.passed) "✅ PASS" else "❌ FAIL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (checkResult.passed) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )

                ConfidenceBadge(metadata.confidence)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cluster ID (truncated)
            Text(
                text = "ID: ${metadata.clusterId.takeLast(12)}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Factual metrics
            MetricRow("Publishers", "${metadata.distinctPublisherCount}")
            MetricRow("Articles", "${metadata.articleCount}")
            MetricRow("Time Window", "${metadata.timeWindowHours}h")
            MetricRow("Avg Similarity", "${"%.1f".format(metadata.averageHeadlineSimilarity * 100)}%")
            MetricRow("Common Entities", "${metadata.commonNamedEntities.size}")

            if (metadata.commonNamedEntities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Entities: ${metadata.commonNamedEntities.take(3).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Match rationale
            Text(
                text = "Rationale:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = metadata.matchRationale,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Findings
            Text(
                text = "Findings:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            checkResult.findings.forEach { finding ->
                FindingChip(finding)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sources
            Text(
                text = "Sources: ${metadata.sourceIds.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            Text(
                text = "Clustered: ${formatter.format(metadata.clusteredAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConfidenceBadge(confidence: ClusterConfidence) {
    val (color, text) = when (confidence) {
        ClusterConfidence.HIGH -> MaterialTheme.colorScheme.primaryContainer to "HIGH"
        ClusterConfidence.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer to "MEDIUM"
        ClusterConfidence.LOW -> MaterialTheme.colorScheme.errorContainer to "LOW"
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FindingChip(finding: com.crosslens.app.core.model.IntegrityFinding) {
    val (icon, color) = when (finding.severity) {
        FindingSeverity.INFO -> "ℹ️" to MaterialTheme.colorScheme.surfaceVariant
        FindingSeverity.WARNING -> "⚠️" to MaterialTheme.colorScheme.tertiaryContainer
        FindingSeverity.ERROR -> "❌" to MaterialTheme.colorScheme.errorContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color, MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = finding.evidence,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * UI state for Event Integrity screen.
 */
sealed interface EventIntegrityUiState {
    data object Loading : EventIntegrityUiState
    data class Success(val clusters: List<EventIntegrityDisplayData>) : EventIntegrityUiState
    data class Error(val message: String) : EventIntegrityUiState
}

/**
 * Display data combining metadata and check result.
 */
data class EventIntegrityDisplayData(
    val metadata: EventIntegrityMetadata,
    val checkResult: IntegrityCheckResult
)
