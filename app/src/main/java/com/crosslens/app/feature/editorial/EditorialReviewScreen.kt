package com.crosslens.app.feature.editorial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosslens.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorialReviewScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorialReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCandidate by remember { mutableStateOf<ReviewCandidate?>(null) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    if (selectedCandidate != null) {
        ReviewDetailDialog(
            candidate = selectedCandidate!!,
            onDismiss = { selectedCandidate = null },
            onSubmitReview = { decision, note ->
                viewModel.submitReview(selectedCandidate!!, decision, note)
                selectedCandidate = null
            }
        )
    }

    if (showResetConfirmation) {
        ResetConfirmationDialog(
            onConfirm = {
                viewModel.resetReviews()
                showResetConfirmation = false
            },
            onDismiss = {
                showResetConfirmation = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editorial_review_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Stats card
            item {
                StatsCard(stats = uiState.stats)
            }

            // Demo notice
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = stringResource(R.string.editorial_review_demo_notice),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Pending candidates
            item {
                Text(
                    text = stringResource(
                        R.string.editorial_review_pending,
                        uiState.pendingCandidates.size
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.pendingCandidates) { candidate ->
                CandidateCard(
                    candidate = candidate,
                    onClick = { selectedCandidate = candidate }
                )
            }

            if (uiState.pendingCandidates.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.editorial_review_no_candidates),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Review history
            if (uiState.reviewHistory.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(
                            R.string.editorial_review_history,
                            uiState.reviewHistory.size
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(uiState.reviewHistory) { review ->
                    HistoryCard(review = review)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showResetConfirmation = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.editorial_review_reset))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(stats: com.crosslens.app.data.repository.ReviewStats) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                stringResource(R.string.editorial_review_stat_approved),
                stats.approved,
                MaterialTheme.colorScheme.primary
            )
            StatItem(
                stringResource(R.string.editorial_review_stat_rejected),
                stats.rejected,
                MaterialTheme.colorScheme.error
            )
            StatItem(
                stringResource(R.string.editorial_review_stat_deferred),
                stats.deferred,
                MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CandidateCard(
    candidate: ReviewCandidate,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = candidate.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (candidate.confidence != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text(candidate.confidence) },
                        enabled = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.editorial_review_sources_languages,
                    candidate.sourceIds.size,
                    candidate.languages.distinct().size
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = candidate.rationale,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = stringResource(
                    R.string.editorial_review_type_method,
                    candidate.type.name.replace("_", " "),
                    candidate.method
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun HistoryCard(review: com.crosslens.app.data.local.entity.EditorialReviewEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (review.decision) {
                "APPROVED" -> MaterialTheme.colorScheme.primaryContainer
                "REJECTED" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = review.candidateType.replace("_", " "),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = review.decision,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (review.decision) {
                        "APPROVED" -> MaterialTheme.colorScheme.primary
                        "REJECTED" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
            }

            if (review.note.isNotBlank()) {
                Text(
                    text = review.note,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewDetailDialog(
    candidate: ReviewCandidate,
    onDismiss: () -> Unit,
    onSubmitReview: (ReviewDecision, String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    var selectedDecision by remember { mutableStateOf<ReviewDecision?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editorial_review_dialog_title, candidate.title)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text(
                        text = stringResource(R.string.editorial_review_evidence),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = candidate.rationale,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                item {
                    Text(
                        text = stringResource(
                            R.string.editorial_review_sources,
                            candidate.sourceIds.joinToString(", ")
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = stringResource(
                            R.string.editorial_review_languages,
                            candidate.languages.joinToString(", ")
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (candidate.sharedEntities != null) {
                    item {
                        Text(
                            text = stringResource(
                                R.string.editorial_review_shared_entities,
                                candidate.sharedEntities.size
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.editorial_review_uncertainty),
                        style = MaterialTheme.typography.labelLarge
                    )
                    candidate.uncertaintyReasons.forEach { reason ->
                        Text(
                            text = "• $reason",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(stringResource(R.string.editorial_review_note_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.editorial_review_decision),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDecision == ReviewDecision.APPROVED,
                            onClick = { selectedDecision = ReviewDecision.APPROVED },
                            label = { Text(stringResource(R.string.editorial_review_approve)) }
                        )
                        FilterChip(
                            selected = selectedDecision == ReviewDecision.REJECTED,
                            onClick = { selectedDecision = ReviewDecision.REJECTED },
                            label = { Text(stringResource(R.string.editorial_review_reject)) }
                        )
                        FilterChip(
                            selected = selectedDecision == ReviewDecision.DEFERRED,
                            onClick = { selectedDecision = ReviewDecision.DEFERRED },
                            label = { Text(stringResource(R.string.editorial_review_defer)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDecision?.let { onSubmitReview(it, note) }
                },
                enabled = selectedDecision != null
            ) {
                Text(stringResource(R.string.editorial_review_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.editorial_review_cancel))
            }
        }
    )
}

@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editorial_review_reset_confirm_title)) },
        text = { Text(stringResource(R.string.editorial_review_reset_confirm_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.editorial_review_reset_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.editorial_review_reset_cancel))
            }
        }
    )
}
