package com.crosslens.app.feature.comparison

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosslens.app.R
import com.crosslens.app.core.model.FrameObservation
import com.crosslens.app.core.ui.PaywallSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossLensScreen(
    storyId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CrossLensViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedArticleIndex.collectAsStateWithLifecycle()
    val hasAllAccess by viewModel.hasAllSourcesAccess.collectAsStateWithLifecycle()
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    var showPaywall by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crosslens_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is CrossLensUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CrossLensUiState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.story_not_found),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is CrossLensUiState.InsufficientSources -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Not enough sources to compare",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is CrossLensUiState.Error -> {
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
            is CrossLensUiState.Success -> {
                val maxFreeIndex = 1 // Free users can see first 2 sources
                val effectiveMaxIndex = if (hasAllAccess) state.articlesWithSources.lastIndex else maxFreeIndex

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Source selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousArticle() },
                            enabled = selectedIndex > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.previous_source))
                        }

                        Text(
                            text = "${selectedIndex + 1} / ${state.articlesWithSources.size}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(
                            onClick = {
                                if (selectedIndex < effectiveMaxIndex) {
                                    viewModel.nextArticle()
                                } else if (selectedIndex < state.articlesWithSources.lastIndex) {
                                    showPaywall = true
                                }
                            },
                            enabled = selectedIndex < state.articlesWithSources.lastIndex
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.next_source))
                        }
                    }

                    // Source pane with animation
                    val currentArticleWithSource = state.articlesWithSources.getOrNull(selectedIndex)
                    if (currentArticleWithSource != null) {
                        SourcePane(
                            articleWithSource = currentArticleWithSource,
                            reducedMotion = userPrefs.reducedMotion,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Frame observations
                    if (state.frameObservations.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    text = stringResource(R.string.framing_observations),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            items(state.frameObservations) { observation ->
                                FramingCard(observation)
                            }
                        }
                    }
                }
            }
        }

        if (showPaywall) {
            PaywallSheet(
                onDismiss = { showPaywall = false },
                onPreviewPlus = {
                    viewModel.enablePlusPreview()
                    showPaywall = false
                }
            )
        }
    }
}

@Composable
private fun SourcePane(
    articleWithSource: ArticleWithSource,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedRotation = remember { Animatable(0f) }

    LaunchedEffect(articleWithSource.article.id) {
        if (!reducedMotion) {
            animatedRotation.snapTo(-5f)
            animatedRotation.animateTo(
                0f,
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer {
                rotationY = if (reducedMotion) 0f else animatedRotation.value
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Source info
                if (articleWithSource.source != null) {
                    Text(
                        text = articleWithSource.source.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = articleWithSource.source.countryCodes.joinToString(", "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Divider()
            }

            item {
                Text(
                    text = stringResource(R.string.original_text),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = articleWithSource.article.originalHeadline,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = articleWithSource.article.originalExcerpt,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                Text(
                    text = "${stringResource(R.string.original_article)} (${stringResource(R.string.demo_link_notice)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FramingCard(observation: FrameObservation, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (observation.languageObservation != null) {
                Text(
                    text = observation.languageObservation,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (observation.emphasizedActors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Emphasized: ${observation.emphasizedActors.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

