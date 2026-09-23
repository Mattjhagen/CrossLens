package com.crosslens.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.crosslens.app.core.model.AccessTier
import com.crosslens.app.core.model.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onEditorialReviewClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val entitlement by viewModel.entitlement.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (userPrefs == null || entitlement == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.settings_display),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    SettingItem(
                        title = stringResource(R.string.theme),
                        subtitle = when (userPrefs!!.theme) {
                            Theme.SYSTEM -> stringResource(R.string.theme_system)
                            Theme.LIGHT -> stringResource(R.string.theme_light)
                            Theme.DARK -> stringResource(R.string.theme_dark)
                        }
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        TextButton(onClick = { expanded = true }) {
                            Text(when (userPrefs!!.theme) {
                                Theme.SYSTEM -> stringResource(R.string.theme_system)
                                Theme.LIGHT -> stringResource(R.string.theme_light)
                                Theme.DARK -> stringResource(R.string.theme_dark)
                            })
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_system)) },
                                onClick = {
                                    viewModel.updateTheme(Theme.SYSTEM)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_light)) },
                                onClick = {
                                    viewModel.updateTheme(Theme.LIGHT)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_dark)) },
                                onClick = {
                                    viewModel.updateTheme(Theme.DARK)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                item {
                    SettingItem(
                        title = stringResource(R.string.reduced_motion),
                        subtitle = "Disable animations"
                    ) {
                        Switch(
                            checked = userPrefs!!.reducedMotion,
                            onCheckedChange = { viewModel.updateReducedMotion(it) }
                        )
                    }
                }

                item {
                    Divider()
                }

                item {
                    Text(
                        text = stringResource(R.string.settings_access),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.access_tier),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (entitlement!!.activeTier) {
                                    AccessTier.FREE -> stringResource(R.string.access_free)
                                    AccessTier.PLUS_DEMO -> stringResource(R.string.access_plus_demo)
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (entitlement!!.activeTier == AccessTier.FREE) {
                                Button(
                                    onClick = { viewModel.previewPlus() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.preview_plus))
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.resetToFree() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.reset_to_free))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.paywall_demo_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Divider()
                }

                item {
                    Text(
                        text = "Personal Preferences",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    PersonalPreferencesSection(
                        personalPreferences = viewModel.personalRelevancePreferences.collectAsStateWithLifecycle().value,
                        onRemovePreference = viewModel::removePersonalPreference,
                        onClearAll = viewModel::clearAllPersonalPreferences
                    )
                }

                item {
                    Divider()
                }

                item {
                    Card(
                        onClick = onEditorialReviewClick,
                        modifier = Modifier.fillMaxWidth()
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
                                    text = stringResource(R.string.editorial_review_entry),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.editorial_review_demo_notice)
                                        .take(80) + "…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    control: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        control()
    }
}

@Composable
private fun PersonalPreferencesSection(
    personalPreferences: List<com.crosslens.app.core.model.PersonalRelevancePreference>,
    onRemovePreference: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Your reading preferences",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "These choices will shape future recommendations when that feature is introduced. They don't affect which sources or evidence you see in story comparisons.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (personalPreferences.isEmpty()) {
                Text(
                    text = "No preferences saved yet. Use \"Show more like this\" or \"Show less like this\" when reading source articles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Group by preference type
                val morePrefs = personalPreferences.filter {
                    it.preferenceType == com.crosslens.app.core.model.PreferenceType.MORE
                }
                val lessPrefs = personalPreferences.filter {
                    it.preferenceType == com.crosslens.app.core.model.PreferenceType.LESS
                }

                if (morePrefs.isNotEmpty()) {
                    Text(
                        text = "Show more:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    morePrefs.forEach { pref ->
                        PreferenceChip(
                            preference = pref,
                            onRemove = { onRemovePreference(pref.id) }
                        )
                    }
                }

                if (lessPrefs.isNotEmpty()) {
                    Text(
                        text = "Show less:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    lessPrefs.forEach { pref ->
                        PreferenceChip(
                            preference = pref,
                            onRemove = { onRemovePreference(pref.id) }
                        )
                    }
                }

                HorizontalDivider()

                OutlinedButton(
                    onClick = onClearAll,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset all preferences")
                }
            }
        }
    }
}

@Composable
private fun PreferenceChip(
    preference: com.crosslens.app.core.model.PersonalRelevancePreference,
    onRemove: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDimensionValue(preference.dimensionValue, preference.dimensionType),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = when (preference.dimensionType) {
                        com.crosslens.app.core.model.DimensionType.TOPIC -> "Topic"
                        com.crosslens.app.core.model.DimensionType.REGION -> "Region"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Text("×", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

private fun formatDimensionValue(value: String, type: com.crosslens.app.core.model.DimensionType): String {
    return when (type) {
        com.crosslens.app.core.model.DimensionType.TOPIC -> {
            // Format topic IDs to readable names
            value.replace("_", " ").split(" ").joinToString(" ") {
                it.replaceFirstChar { char -> char.uppercase() }
            }
        }
        com.crosslens.app.core.model.DimensionType.REGION -> {
            // Format region IDs to readable names
            when (value) {
                "europe" -> "Europe"
                "north_america" -> "North America"
                "middle_east" -> "Middle East"
                "asia" -> "Asia"
                "africa" -> "Africa"
                "south_america" -> "South America"
                "oceania" -> "Oceania"
                else -> value.replace("_", " ").split(" ").joinToString(" ") {
                    it.replaceFirstChar { char -> char.uppercase() }
                }
            }
        }
    }
}

