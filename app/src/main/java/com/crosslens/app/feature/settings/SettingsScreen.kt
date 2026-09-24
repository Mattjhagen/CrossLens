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
import androidx.compose.ui.text.font.FontWeight
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
                        text = stringResource(R.string.settings_personalization),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    SettingItem(
                        title = stringResource(R.string.show_for_you),
                        subtitle = stringResource(R.string.show_for_you_summary)
                    ) {
                        Switch(
                            checked = userPrefs!!.showForYou,
                            onCheckedChange = { viewModel.updateShowForYou(it) }
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
                    Text(
                        text = "Local News",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    LocalLocationSection(
                        selectedLocationId = userPrefs!!.demoLocalLocation,
                        onLocationSelected = viewModel::updateDemoLocalLocation
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
    var showClearDialog by remember { mutableStateOf(false) }

    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Your reading preferences",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = stringResource(R.string.show_for_you_summary),
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
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.clear_preferences))
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(stringResource(R.string.clear_preferences_confirm_title))
            },
            text = {
                Text(stringResource(R.string.clear_preferences_confirm_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    }
                ) {
                    Text(stringResource(R.string.clear_preferences_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.clear_preferences_cancel))
                }
            }
        )
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

@Composable
private fun LocalLocationSection(
    selectedLocationId: String?,
    onLocationSelected: (String?) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Demo Local News",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Choose a demo location to see fictional local news stories. This is a preview feature - live local source availability will vary by location.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val selectedLocation = selectedLocationId?.let {
                com.crosslens.app.core.model.DemoLocalLocations.findById(it)
            }

            if (selectedLocation != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedLocation.cityName}, ${selectedLocation.regionName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Demo Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onLocationSelected(null) }) {
                            Text("×", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            var showLocationPicker by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showLocationPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedLocation == null) "Choose location" else "Change location")
            }

            if (showLocationPicker) {
                AlertDialog(
                    onDismissRequest = { showLocationPicker = false },
                    title = { Text("Choose Demo Location") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select a demo location to explore fictional local news:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            com.crosslens.app.core.model.DemoLocalLocations.ALL_DEMO_LOCATIONS.forEach { location ->
                                Card(
                                    onClick = {
                                        onLocationSelected(location.id)
                                        showLocationPicker = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = location.cityName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${location.regionName}, ${location.countryCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLocationPicker = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (selectedLocation != null) {
                Text(
                    text = "Local stories from this location will appear in Home when you filter by local news.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

