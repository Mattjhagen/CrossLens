package com.crosslens.app.feature.sourcedetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crosslens.app.R
import com.crosslens.app.core.model.DigestPoint
import com.crosslens.app.core.model.SourceDigest
import com.crosslens.app.core.ui.components.CrossLensSignature
import com.crosslens.app.core.ui.components.SignatureSize
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceDetailScreen(
    articleId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SourceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPaywallNotice by remember { mutableStateOf(false) }

    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Source Article") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (val state = uiState) {
            is SourceDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
            is SourceDetailUiState.Error -> {
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
            is SourceDetailUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Source info
                    item {
                        SourceInfoCard(
                            sourceName = state.source.name,
                            sourceCountries = state.source.countryCodes,
                            attribution = state.article.attribution,
                            publishedTime = state.article.publishedTime,
                            isDemo = state.article.contentUseMetadata.isDemo
                        )
                    }

                    // Article content
                    item {
                        Card {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = state.article.originalHeadline,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Language: ${state.article.originalLanguage}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = state.article.originalContent,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                )
                            }
                        }
                    }

                    // Publisher page access
                    item {
                        OutlinedButton(
                            onClick = {
                                if (state.article.requiresSubscription) {
                                    showPaywallNotice = true
                                } else {
                                    openPublisherPage(context, state.article.originalUrl)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open publisher page")
                        }
                    }

                    // Demo AI digest
                    if (state.digest != null) {
                        item {
                            Divider()
                        }
                        item {
                            DigestSection(digest = state.digest)
                        }
                    }
                }

                // Paywall notice dialog
                if (showPaywallNotice) {
                    PaywallNoticeDialog(
                        sourceName = state.source.name,
                        onDismiss = { showPaywallNotice = false },
                        onContinue = {
                            showPaywallNotice = false
                            openPublisherPage(context, state.article.originalUrl)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceInfoCard(
    sourceName: String,
    sourceCountries: List<String>,
    attribution: String,
    publishedTime: java.time.Instant,
    isDemo: Boolean
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = sourceName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sourceCountries.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "By $attribution",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = publishedTime.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isDemo) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Demo Content",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DigestSection(digest: SourceDigest) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Demo AI Source Digest",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (digest.isDemo) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "DEMO",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = digest.summary,
                style = MaterialTheme.typography.bodyMedium
            )

            if (digest.agreements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Agreement across sources:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                digest.agreements.forEach { point ->
                    DigestPointItem(point)
                }
            }

            if (digest.differences.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Differences in framing:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                digest.differences.forEach { point ->
                    DigestPointItem(point)
                }
            }

            if (digest.missingEvidence.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Not clearly covered:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                digest.missingEvidence.forEach { missing ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text("• ", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = missing,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Generated: ${digest.methodVersion} • ${digest.sourcesCovered.size} sources analyzed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DigestPointItem(point: DigestPoint) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text("• ", style = MaterialTheme.typography.bodyMedium)
        Column {
            Text(
                text = point.observation,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Sources: ${point.supportingSourceIds.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaywallNoticeDialog(
    sourceName: String,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publisher Access") },
        text = {
            Text(
                "$sourceName may require a subscription or have limited access. " +
                "CrossLens does not bypass publisher access controls."
            )
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun openPublisherPage(context: Context, url: String) {
    try {
        // Use Custom Tabs for in-app browser experience
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        // Fallback to regular browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
