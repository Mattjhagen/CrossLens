package com.crosslens.app.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CrossLens editorial signature - a reusable masthead component.
 *
 * Implements the "crossing perspectives" concept through:
 * - Wordmark text with serif typography
 * - Thin crossing rule that intersects the text
 * - Subtle offset-pane geometry suggestion
 *
 * Theme-aware, scales for different contexts, and silent to TalkBack.
 */
@Composable
fun CrossLensSignature(
    modifier: Modifier = Modifier,
    size: SignatureSize = SignatureSize.Medium
) {
    Box(
        modifier = modifier
            .clearAndSetSemantics { }, // Silent to TalkBack - decorative branding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top perspective pane indicator
            PerspectivePane(
                width = size.paneWidth,
                height = size.paneHeight,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(size.spacing))

            // Wordmark with crossing rule
            Box(
                modifier = Modifier.height(size.wordmarkHeight),
                contentAlignment = Alignment.Center
            ) {
                // Diagonal crossing rule
                CrossingRule(
                    length = size.ruleLength,
                    strokeWidth = size.ruleStrokeWidth,
                    color = MaterialTheme.colorScheme.primary
                )

                // CrossLens wordmark
                Text(
                    text = "CrossLens",
                    style = when (size) {
                        SignatureSize.Small -> MaterialTheme.typography.titleLarge
                        SignatureSize.Medium -> MaterialTheme.typography.displaySmall
                        SignatureSize.Large -> MaterialTheme.typography.displayMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(size.spacing))

            // Bottom perspective pane indicator
            PerspectivePane(
                width = size.paneWidth,
                height = size.paneHeight,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            )
        }
    }
}

/**
 * Lightweight wordmark variant - text with thin crossing rule only.
 * For use in section headers where full signature would be too prominent.
 */
@Composable
fun CrossLensWordmark(
    modifier: Modifier = Modifier,
    size: SignatureSize = SignatureSize.Small
) {
    Box(
        modifier = modifier
            .clearAndSetSemantics { }
            .height(size.wordmarkHeight),
        contentAlignment = Alignment.Center
    ) {
        // Diagonal crossing rule
        CrossingRule(
            length = size.ruleLength,
            strokeWidth = size.ruleStrokeWidth,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        // CrossLens wordmark
        Text(
            text = "CrossLens",
            style = when (size) {
                SignatureSize.Small -> MaterialTheme.typography.titleMedium
                SignatureSize.Medium -> MaterialTheme.typography.titleLarge
                SignatureSize.Large -> MaterialTheme.typography.displaySmall
            },
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun CrossingRule(
    length: Dp,
    strokeWidth: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(length)
    ) {
        val strokePx = strokeWidth.toPx()
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PerspectivePane(
    width: Dp,
    height: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(width = width, height = height)
    ) {
        val cornerRadius = 8.dp.toPx()
        drawRoundRect(
            color = color,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
        )
    }
}

enum class SignatureSize(
    val wordmarkHeight: Dp,
    val ruleLength: Dp,
    val ruleStrokeWidth: Dp,
    val paneWidth: Dp,
    val paneHeight: Dp,
    val spacing: Dp
) {
    Small(
        wordmarkHeight = 32.dp,
        ruleLength = 80.dp,
        ruleStrokeWidth = 1.dp,
        paneWidth = 48.dp,
        paneHeight = 4.dp,
        spacing = 4.dp
    ),
    Medium(
        wordmarkHeight = 48.dp,
        ruleLength = 120.dp,
        ruleStrokeWidth = 1.5.dp,
        paneWidth = 72.dp,
        paneHeight = 6.dp,
        spacing = 8.dp
    ),
    Large(
        wordmarkHeight = 64.dp,
        ruleLength = 160.dp,
        ruleStrokeWidth = 2.dp,
        paneWidth = 96.dp,
        paneHeight = 8.dp,
        spacing = 12.dp
    )
}
