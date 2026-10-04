package com.crosslens.app.feature.sourcehealth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.crosslens.app.data.repository.RefreshResult
import com.crosslens.app.data.repository.SourceHealthStatus
import com.crosslens.app.data.repository.SourceHealthUiModel
import com.crosslens.app.data.repository.SourceRefreshOutcome
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Source Health & Coverage Status screen.
 * Shows technical health of RSS sources without editorial judgments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceHealthScreen(
    onNavigateBack: () -> Unit,
    viewModel: SourceHealthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Source Health") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is SourceHealthUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SourceHealthUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is SourceHealthUiState.Success -> {
                SourceHealthContent(
                    state = state,
                    onRefresh = viewModel::refreshAllSources,
                    onClearRefreshResult = viewModel::clearRefreshResult,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun SourceHealthContent(
    state: SourceHealthUiState.Success,
    onRefresh: () -> Unit,
    onClearRefreshResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary card
        item {
            SummaryCard(
                totalSources = state.totalSources,
                activeCount = state.activeCount,
                degradedCount = state.degradedCount,
                disabledCount = state.disabledCount,
                lastRefreshAt = state.lastRefreshAt
            )
        }

        // Manual refresh button
        item {
            RefreshButton(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh
            )
        }

        // Refresh result (if present)
        if (state.refreshResult != null) {
            item {
                RefreshResultCard(
                    result = state.refreshResult,
                    onDismiss = onClearRefreshResult
                )
            }
        }

        // About section
        item {
            AboutHealthMonitoring()
        }

        // Sources list header
        item {
            Text(
                text = "Source Status (${state.sources.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Sources list
        items(state.sources, key = { it.sourceId }) { source ->
            SourceHealthCard(source = source)
        }
    }
}

@Composable
private fun SummaryCard(
    totalSources: Int,
    activeCount: Int,
    degradedCount: Int,
    disabledCount: Int,
    lastRefreshAt: Instant?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Coverage Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusCount(
                    label = "Active",
                    count = activeCount,
                    color = MaterialTheme.colorScheme.primary
                )
                if (degradedCount > 0) {
                    StatusCount(
                        label = "Degraded",
                        count = degradedCount,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (disabledCount > 0) {
                    StatusCount(
                        label = "Disabled",
                        count = disabledCount,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (lastRefreshAt != null) {
                val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                    .withZone(ZoneId.systemDefault())
                Text(
                    text = "Last refresh: ${formatter.format(lastRefreshAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StatusCount(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun RefreshButton(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Button(
        onClick = onRefresh,
        enabled = !isRefreshing,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refreshing...")
        } else {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh All Sources")
        }
    }
}

@Composable
private fun RefreshResultCard(
    result: RefreshResult,
    onDismiss: () -> Unit
) {
    val backgroundColor = if (result.allSucceeded) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val textColor = if (result.allSucceeded) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (result.allSucceeded) "Refresh Complete" else "Refresh Completed with Errors",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "${result.successCount}/${result.totalSources} sources successful",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = textColor
                )
            }
        }
    }
}

@Composable
private fun AboutHealthMonitoring() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "About Health Monitoring",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Source health is determined by technical evidence only: fetch success, parse success, article freshness, and attribution quality. CrossLens never uses ideology, location, popularity, or editorial perspective as health factors.",
                style = MaterialTheme.typography.bodySmall
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            HealthStatusExplanation(
                status = "Active",
                description = "Source is fetching and parsing successfully",
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.primary
            )

            HealthStatusExplanation(
                status = "Degraded",
                description = "3+ consecutive failures but may recover",
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.tertiary
            )

            HealthStatusExplanation(
                status = "Disabled",
                description = "10+ consecutive failures; source disabled",
                icon = Icons.Default.Cancel,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun HealthStatusExplanation(
    status: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SourceHealthCard(source: SourceHealthUiModel) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.sourceName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = source.sourceId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                StatusBadge(status = source.status)
            }

            // Key metrics
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricChip(
                    label = "Success rate",
                    value = String.format("%.0f%%", source.last24hSuccessRate * 100)
                )
                MetricChip(
                    label = "24h articles",
                    value = source.last24hArticleCount.toString()
                )
                if (source.consecutiveFailures > 0) {
                    MetricChip(
                        label = "Failures",
                        value = source.consecutiveFailures.toString(),
                        isError = true
                    )
                }
            }

            // Last success/failure
            if (source.lastSuccessAt != null) {
                val timeSinceSuccess = formatTimeAgo(source.lastSuccessAt)
                Text(
                    text = "Last success: $timeSinceSuccess",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (source.lastFailureAt != null) {
                val timeSinceFailure = formatTimeAgo(source.lastFailureAt)
                Text(
                    text = "Last failure: $timeSinceFailure",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Expanded details
            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                if (source.lastErrorMessage != null) {
                    Text(
                        text = "Error:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = source.lastErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (source.lastErrorCategory != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Category: ${source.lastErrorCategory.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (source.disabledReason != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Disabled reason:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = source.disabledReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Expand indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: SourceHealthStatus) {
    val (icon, color, text) = when (status) {
        SourceHealthStatus.ACTIVE -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.primary,
            "Active"
        )
        SourceHealthStatus.DEGRADED -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.tertiary,
            "Degraded"
        )
        SourceHealthStatus.DISABLED -> Triple(
            Icons.Default.Cancel,
            MaterialTheme.colorScheme.error,
            "Disabled"
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    isError: Boolean = false
) {
    Surface(
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}

private fun formatTimeAgo(instant: Instant): String {
    val duration = Duration.between(instant, Instant.now())
    val hours = duration.toHours()
    val minutes = duration.toMinutes()

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        hours < 48 -> "1 day ago"
        else -> "${hours / 24} days ago"
    }
}
