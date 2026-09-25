package com.crosslens.app.feature.eventcomparison

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crosslens.app.core.model.Article
import com.crosslens.app.data.ingestion.SourceMetadata
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Event comparison screen showing multiple source articles side-by-side.
 * Displays original headlines, excerpts, source attribution, and metadata
 * for reader comparison without claiming truth or bias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventComparisonScreen(
    storyId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventComparisonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Comparison") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is EventComparisonUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is EventComparisonUiState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Event not found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Back")
                        }
                    }
                }
            }
            is EventComparisonUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Back")
                        }
                    }
                }
            }
            is EventComparisonUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Event header with gap notices
                    item {
                        EventHeader(
                            title = state.story.title,
                            sourceCount = state.articles.size,
                            articles = state.articles
                        )
                    }

                    // Article cards with "why this appears" explanations
                    items(state.articles) { articleWithMetadata ->
                        ArticleComparisonCard(
                            article = articleWithMetadata.article,
                            metadata = articleWithMetadata.metadata,
                            totalPublishers = state.articles.size,
                            allPublishers = state.articles.mapNotNull { it.metadata?.publisherName }
                        )
                    }

                    // Explanation footer
                    item {
                        ComparisonExplanation(
                            articleCount = state.articles.size
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventHeader(
    title: String,
    sourceCount: Int,
    articles: List<ArticleWithMetadata>,
    modifier: Modifier = Modifier
) {
    // Compute coverage metadata for gap notices
    val languages = articles.mapNotNull { it.metadata?.primaryLanguage }.distinct()
    val countries = articles.mapNotNull { it.metadata?.country }.distinct()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Coverage summary
        Text(
            text = "$sourceCount source${if (sourceCount > 1) "s" else ""} reporting this event",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics {
                contentDescription = "$sourceCount sources reporting this event"
            }
        )

        // Coverage gap notices
        if (sourceCount == 1) {
            CoverageGapNotice(
                text = "Single publisher - coverage may be incomplete"
            )
        }

        if (languages.size == 1) {
            CoverageGapNotice(
                text = if (sourceCount == 1) {
                    "One language represented"
                } else {
                    "$sourceCount publishers; one language represented"
                }
            )
        }

        if (sourceCount > 1 && countries.size == 1) {
            CoverageGapNotice(
                text = "All sources from ${countries.first()}"
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun CoverageGapNotice(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "⚠",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun ArticleComparisonCard(
    article: Article,
    metadata: SourceMetadata?,
    totalPublishers: Int,
    allPublishers: List<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val publisherName = metadata?.publisherName ?: article.attribution

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Publisher header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = publisherName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (metadata != null) {
                        Text(
                            text = "${metadata.country} • ${metadata.primaryLanguage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = formatPublicationTime(article.publishedTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "Why this appears" explanation
            if (totalPublishers > 1) {
                val otherPublishers = allPublishers.filter { it != publisherName }.take(2)
                val explanation = if (otherPublishers.isNotEmpty()) {
                    "Different publisher from ${otherPublishers.joinToString(", ")}"
                } else {
                    "One of $totalPublishers publishers reporting this event"
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Article image if available
            if (article.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(article.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = article.originalHeadline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Original headline
            Text(
                text = article.originalHeadline,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Excerpt
            Text(
                text = article.originalExcerpt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Editorial description if available
            if (metadata?.editorialDescription != null && metadata.descriptionProvenance != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = metadata.editorialDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Source: ${metadata.descriptionProvenance}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Open original button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.originalUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Original Article")
            }
        }
    }
}

@Composable
private fun ComparisonExplanation(
    articleCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "About This Comparison",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "These $articleCount articles were grouped because they report on the same specific event. " +
                        "CrossLens preserves each publisher's original headline, wording, and timing for you to compare. " +
                        "We do not claim any article is more accurate, truthful, or biased than another.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Source metadata (country, language, editorial descriptions) is provided for context only, " +
                        "with documented provenance shown where available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Format publication time as relative time (e.g., "2 hours ago") for recent articles,
 * or absolute time for older articles.
 */
private fun formatPublicationTime(publishedTime: Instant): String {
    val now = Instant.now()
    val hoursAgo = ChronoUnit.HOURS.between(publishedTime, now)

    return when {
        hoursAgo < 1 -> {
            val minutesAgo = ChronoUnit.MINUTES.between(publishedTime, now)
            if (minutesAgo < 1) "Just now" else "${minutesAgo}m ago"
        }
        hoursAgo < 24 -> "${hoursAgo}h ago"
        hoursAgo < 48 -> "Yesterday"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
                .withZone(ZoneId.systemDefault())
            formatter.format(publishedTime)
        }
    }
}
