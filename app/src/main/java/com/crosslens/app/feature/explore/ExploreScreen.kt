package com.crosslens.app.feature.explore

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosslens.app.R
import com.crosslens.app.core.model.Story

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    onStoryClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val stories by viewModel.filteredStories.collectAsStateWithLifecycle()
    val selectedRegions by viewModel.selectedRegions.collectAsStateWithLifecycle()
    val selectedCountries by viewModel.selectedCountries.collectAsStateWithLifecycle()
    val selectedTopics by viewModel.selectedTopics.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.explore_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    if (selectedRegions.isNotEmpty() || selectedCountries.isNotEmpty() || selectedTopics.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text(stringResource(R.string.clear_filters))
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.filter_region),
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedRegions.contains("europe"),
                            onClick = { viewModel.toggleRegion("europe") },
                            label = { Text("Europe") }
                        )
                        FilterChip(
                            selected = selectedRegions.contains("north_america"),
                            onClick = { viewModel.toggleRegion("north_america") },
                            label = { Text("North America") }
                        )
                        FilterChip(
                            selected = selectedRegions.contains("middle_east"),
                            onClick = { viewModel.toggleRegion("middle_east") },
                            label = { Text("Middle East") }
                        )
                        FilterChip(
                            selected = selectedRegions.contains("asia"),
                            onClick = { viewModel.toggleRegion("asia") },
                            label = { Text("Asia") }
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.filter_topic),
                        style = MaterialTheme.typography.titleSmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedTopics.contains("environment"),
                            onClick = { viewModel.toggleTopic("environment") },
                            label = { Text("Environment") }
                        )
                        FilterChip(
                            selected = selectedTopics.contains("politics"),
                            onClick = { viewModel.toggleTopic("politics") },
                            label = { Text("Politics") }
                        )
                        FilterChip(
                            selected = selectedTopics.contains("technology"),
                            onClick = { viewModel.toggleTopic("technology") },
                            label = { Text("Technology") }
                        )
                        FilterChip(
                            selected = selectedTopics.contains("economics"),
                            onClick = { viewModel.toggleTopic("economics") },
                            label = { Text("Economics") }
                        )
                    }
                }
            }

            item {
                Divider()
            }

            if (stories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.no_results),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.adjust_filters),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(stories) { story ->
                    StoryCard(story = story, onClick = { onStoryClick(story.id) })
                }
            }
        }
    }
}

@Composable
private fun StoryCard(
    story: Story,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = story.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = story.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
        }
    }
}

