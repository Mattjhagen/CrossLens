package com.crosslens.app.feature.articlenavigator

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.crosslens.app.core.ui.components.CrossLensSignature
import com.crosslens.app.core.ui.components.SignatureSize
import com.crosslens.app.data.ingestion.SourceMetadata
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

/**
 * Full-screen article navigator with Flipboard-inspired swipe gestures.
 *
 * Gestures:
 * - Swipe left: next article in same event cluster (horizontal navigation)
 * - Swipe right: previous article in same event cluster (horizontal navigation)
 * - Swipe up: next story in feed (vertical navigation)
 * - Swipe down: previous story in feed (vertical navigation)
 * - Tap top area: return to first article and refresh feed
 *
 * Horizontal navigation only available for confident event clusters with 2+ distinct publishers.
 * Shows boundary states when no previous/next item available.
 */
@Composable
fun ArticleNavigatorScreen(
    storyId: String,
    articleId: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticleNavigatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is ArticleNavigatorUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CrossLensSignature(size = SignatureSize.Medium)
                        CircularProgressIndicator()
                    }
                }
            }
            is ArticleNavigatorUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = onBackClick) {
                            Text("Back to Feed")
                        }
                    }
                }
            }
            is ArticleNavigatorUiState.Success -> {
                ArticleNavigatorContent(
                    article = state.article,
                    metadata = state.metadata,
                    navigationState = navigationState,
                    onSwipeLeft = { viewModel.navigateToNextInCluster() },
                    onSwipeRight = { viewModel.navigateToPreviousInCluster() },
                    onSwipeUp = { viewModel.navigateToNextInFeed() },
                    onSwipeDown = { viewModel.navigateToPreviousInFeed() },
                    onTopTap = { viewModel.returnToFirstAndRefresh() },
                    onOpenOriginal = { url -> openPublisherPage(context, url) }
                )
            }
        }
    }
}

@Composable
private fun ArticleNavigatorContent(
    article: Article,
    metadata: SourceMetadata?,
    navigationState: NavigationState?,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onTopTap: () -> Unit,
    onOpenOriginal: (String) -> Unit
) {
    // Track gesture direction for animation
    var lastGestureDirection by remember { mutableStateOf<GestureDirection?>(null) }
    var showBoundaryFeedback by remember { mutableStateOf<BoundaryType?>(null) }

    // Clear boundary feedback after delay
    LaunchedEffect(showBoundaryFeedback) {
        if (showBoundaryFeedback != null) {
            kotlinx.coroutines.delay(1000)
            showBoundaryFeedback = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(navigationState) {
                detectDragGestures(
                    onDragEnd = {
                        // Gesture completed - animation handled by AnimatedContent
                    }
                ) { change, dragAmount ->
                    change.consume()

                    val horizontalDrag = dragAmount.x
                    val verticalDrag = dragAmount.y

                    // Determine primary direction (horizontal vs vertical)
                    if (abs(horizontalDrag) > abs(verticalDrag)) {
                        // Horizontal swipe
                        if (abs(horizontalDrag) > 50) { // Threshold for swipe detection
                            if (horizontalDrag < 0 && navigationState?.hasNextInCluster == true) {
                                // Swipe left: next article in cluster
                                lastGestureDirection = GestureDirection.LEFT
                                onSwipeLeft()
                            } else if (horizontalDrag > 0 && navigationState?.hasPreviousInCluster == true) {
                                // Swipe right: previous article in cluster
                                lastGestureDirection = GestureDirection.RIGHT
                                onSwipeRight()
                            } else if (horizontalDrag < 0 && navigationState?.hasNextInCluster == false) {
                                // At boundary - show feedback
                                showBoundaryFeedback = BoundaryType.LAST_IN_CLUSTER
                            } else if (horizontalDrag > 0 && navigationState?.hasPreviousInCluster == false) {
                                // At boundary - show feedback
                                showBoundaryFeedback = BoundaryType.FIRST_IN_CLUSTER
                            }
                        }
                    } else {
                        // Vertical swipe
                        if (abs(verticalDrag) > 50) { // Threshold for swipe detection
                            if (verticalDrag < 0 && navigationState?.hasNextInFeed == true) {
                                // Swipe up: next story in feed
                                lastGestureDirection = GestureDirection.UP
                                onSwipeUp()
                            } else if (verticalDrag > 0 && navigationState?.hasPreviousInFeed == true) {
                                // Swipe down: previous story in feed
                                lastGestureDirection = GestureDirection.DOWN
                                onSwipeDown()
                            } else if (verticalDrag < 0 && navigationState?.hasNextInFeed == false) {
                                // At boundary - show feedback
                                showBoundaryFeedback = BoundaryType.LAST_IN_FEED
                            } else if (verticalDrag > 0 && navigationState?.hasPreviousInFeed == false) {
                                // At boundary - show feedback
                                showBoundaryFeedback = BoundaryType.FIRST_IN_FEED
                            }
                        }
                    }
                }
            }
    ) {
        // Top navigation area - tap to return to first and refresh
        Surface(
            onClick = onTopTap,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .semantics {
                    contentDescription = "CrossLens. Story ${navigationState?.currentFeedPosition?.plus(1) ?: 1} of ${navigationState?.totalFeedItems ?: 1}. Tap to return to first story and refresh feed."
                },
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CrossLensSignature(size = SignatureSize.Small)
                    if (navigationState != null) {
                        Text(
                            text = "${navigationState.currentFeedPosition + 1} of ${navigationState.totalFeedItems}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // Boundary feedback overlay
        if (showBoundaryFeedback != null) {
            BoundaryFeedbackOverlay(boundaryType = showBoundaryFeedback!!)
        }

        // Article content with animated transitions
        AnimatedContent(
            targetState = article.id,
            transitionSpec = {
                val direction = lastGestureDirection
                when (direction) {
                    GestureDirection.LEFT -> {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    }
                    GestureDirection.RIGHT -> {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                    GestureDirection.UP -> {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut()
                        )
                    }
                    GestureDirection.DOWN -> {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut()
                        )
                    }
                    null -> {
                        // No gesture - just fade
                        fadeIn(animationSpec = tween(300)).togetherWith(
                            fadeOut(animationSpec = tween(300))
                        )
                    }
                }.using(SizeTransform(clip = false))
            },
            label = "Article transition"
        ) { articleId ->
            // Article content - scrollable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .semantics {
                        contentDescription = "Article: ${article.originalHeadline}"
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Publisher and source info
            SourceInfoSection(
                metadata = metadata,
                article = article
            )

            // Article image if available
            article.imageUrl?.let { imageUrl ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Article image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                }
            }

            // Article headline
            Text(
                text = article.originalHeadline,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Language indicator
            Text(
                text = "Language: ${article.originalLanguage}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // Article content
            Text(
                text = article.originalContent,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Open original article button
            OutlinedButton(
                onClick = { onOpenOriginal(article.originalUrl) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Open original article on ${metadata?.publisherName ?: "publisher site"}"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Read on ${metadata?.publisherName ?: "Publisher Site"}")
            }

                // Navigation hints
                if (navigationState != null) {
                    NavigationHints(navigationState)
                }
            }
        }
    }
}

@Composable
private fun SourceInfoSection(
    metadata: SourceMetadata?,
    article: Article
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Publisher name
            metadata?.publisherName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Publisher context
            metadata?.let { meta ->
                val contextParts = listOf(
                    meta.country,
                    meta.primaryLanguage,
                    meta.editorialDescription
                ).filterNotNull()

                if (contextParts.isNotEmpty()) {
                    Text(
                        text = contextParts.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Published time
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            val publishedTime = article.publishedTime
                .atZone(ZoneId.systemDefault())
                .format(formatter)

            Text(
                text = "Published: $publishedTime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Attribution
            Text(
                text = article.attribution,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NavigationHints(navigationState: NavigationState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Navigation",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            // Horizontal navigation (within event cluster)
            if (navigationState.hasHorizontalNavigation) {
                Text(
                    text = "← Swipe left/right for other publishers (${navigationState.currentClusterPosition + 1}/${navigationState.totalClusterItems})",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "Single-source story (no horizontal navigation)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            // Vertical navigation (feed sequence)
            Text(
                text = "↕ Swipe up/down for next/previous story",
                style = MaterialTheme.typography.bodySmall
            )

            // Tap to refresh
            Text(
                text = "Tap top area to return to first story and refresh",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun BoundaryFeedbackOverlay(boundaryType: BoundaryType) {
    val alpha by animateFloatAsState(
        targetValue = 0.9f,
        animationSpec = tween(durationMillis = 200),
        label = "Boundary feedback alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (boundaryType) {
                        BoundaryType.FIRST_IN_CLUSTER -> "First publisher in this event"
                        BoundaryType.LAST_IN_CLUSTER -> "Last publisher in this event"
                        BoundaryType.FIRST_IN_FEED -> "First story in feed"
                        BoundaryType.LAST_IN_FEED -> "Last story in feed"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (boundaryType) {
                        BoundaryType.FIRST_IN_CLUSTER, BoundaryType.LAST_IN_CLUSTER ->
                            "Swipe vertically for other stories"
                        BoundaryType.FIRST_IN_FEED, BoundaryType.LAST_IN_FEED ->
                            if (boundaryType == BoundaryType.FIRST_IN_FEED)
                                "Tap top to refresh"
                            else
                                "End of feed"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun openPublisherPage(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        // Fallback to default browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

/**
 * Gesture direction for animation selection.
 */
private enum class GestureDirection {
    LEFT, RIGHT, UP, DOWN
}

/**
 * Boundary type for feedback overlay.
 */
private enum class BoundaryType {
    FIRST_IN_CLUSTER,
    LAST_IN_CLUSTER,
    FIRST_IN_FEED,
    LAST_IN_FEED
}
