package com.example.budgey.ui.theme.components

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.budgey.ui.theme.*

@Composable
fun BudgeyLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = BudgeySize.loadingMedium,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(BudgeyDuration.loading, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        val canvasSize = this.size.minDimension
        val strokeWidthPx = strokeWidth.toPx()

        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            ),
            size = androidx.compose.ui.geometry.Size(
                canvasSize - strokeWidthPx,
                canvasSize - strokeWidthPx
            ),
            topLeft = androidx.compose.ui.geometry.Offset(
                strokeWidthPx / 2,
                strokeWidthPx / 2
            )
        )
    }
}

@Composable
fun BudgeyPulsingLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = BudgeySize.loadingMedium,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val radius = (size.toPx() / 2) * scale
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius
        )
    }
}

@Composable
fun BudgeyDotLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    dotSize: Dp = 8.dp,
    spacing: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val animationDelay = 200
    val animationDuration = 600

    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, delayMillis = animationDelay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, delayMillis = animationDelay * 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(dot1Scale, dot2Scale, dot3Scale).forEach { scale ->
            Canvas(
                modifier = Modifier.size(dotSize)
            ) {
                val radius = (dotSize.toPx() / 2) * scale
                drawCircle(
                    color = color,
                    radius = radius
                )
            }
        }
    }
}

@Composable
fun BudgeySkeletonLoader(
    modifier: Modifier = Modifier,
    lines: Int = 3,
    animated: Boolean = true
) {
    val alpha = if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        animatedAlpha
    } else {
        0.6f // Static alpha value for non-animated state
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
    ) {
        repeat(lines) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(
                        when (index) {
                            lines - 1 -> 0.7f // Last line shorter
                            else -> 1f
                        }
                    )
                    .height(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                        shape = RoundedCornerShape(BudgeyRadius.xs)
                    )
            )
        }
    }
}

@Composable
fun BudgeyFullScreenLoader(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    showMessage: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BudgeySpacing.lg)
        ) {
            BudgeyLoadingIndicator(
                size = BudgeySize.loadingLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (showMessage) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BudgeyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 4.dp,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (animated) {
            tween(BudgeyDuration.slow, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "progress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth),
        color = color,
        trackColor = backgroundColor,
        strokeCap = StrokeCap.Round
    )
}

@Composable
fun BudgeyCircularProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = BudgeySize.loadingLarge,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 4.dp,
    animated: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (animated) {
            tween(BudgeyDuration.slow, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(
                color = backgroundColor,
                radius = size.toPx() / 2,
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Progress circle
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )

        // Progress text
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = BudgeyTextStyles.categoryLabel,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Preview Composables
@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyLoadingIndicatorPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Loading Indicators",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Small indicator
            BudgeyLoadingIndicator(
                size = BudgeySize.loadingSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Medium indicator
            BudgeyLoadingIndicator(
                size = BudgeySize.loadingMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            // Large indicator
            BudgeyLoadingIndicator(
                size = BudgeySize.loadingLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyPulsingLoadingIndicatorPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Pulsing Indicators",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            BudgeyPulsingLoadingIndicator(
                size = BudgeySize.loadingSmall,
                color = MaterialTheme.colorScheme.primary
            )

            BudgeyPulsingLoadingIndicator(
                size = BudgeySize.loadingMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            BudgeyPulsingLoadingIndicator(
                size = BudgeySize.loadingLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyDotLoadingIndicatorPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Dot Loading Indicators",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Small dots
            BudgeyDotLoadingIndicator(
                color = MaterialTheme.colorScheme.primary,
                dotSize = 6.dp,
                spacing = 3.dp
            )

            // Medium dots
            BudgeyDotLoadingIndicator(
                color = MaterialTheme.colorScheme.secondary,
                dotSize = 8.dp,
                spacing = 4.dp
            )

            // Large dots
            BudgeyDotLoadingIndicator(
                color = MaterialTheme.colorScheme.tertiary,
                dotSize = 10.dp,
                spacing = 6.dp
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeySkeletonLoaderPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Skeleton Loaders",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Animated skeleton with 3 lines
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                BudgeySkeletonLoader(
                    modifier = Modifier.padding(16.dp),
                    lines = 3,
                    animated = true
                )
            }

            // Static skeleton with 5 lines
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                BudgeySkeletonLoader(
                    modifier = Modifier.padding(16.dp),
                    lines = 5,
                    animated = false
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyProgressIndicatorPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Progress Indicators",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Linear progress indicators
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "25% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BudgeyProgressIndicator(
                    progress = 0.25f,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "50% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BudgeyProgressIndicator(
                    progress = 0.5f,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = "75% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BudgeyProgressIndicator(
                    progress = 0.75f,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyCircularProgressIndicatorPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Circular Progress",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BudgeyCircularProgressIndicator(
                    progress = 0.33f,
                    size = 48.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                BudgeyCircularProgressIndicator(
                    progress = 0.66f,
                    size = 48.dp,
                    color = MaterialTheme.colorScheme.secondary
                )

                BudgeyCircularProgressIndicator(
                    progress = 0.90f,
                    size = 48.dp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "All Loading Components - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "All Loading Components - Dark")
@Composable
private fun BudgeyAllLoadingIndicatorsPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Budgey Loading Components",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Spinning indicator
            BudgeyLoadingIndicator(
                size = BudgeySize.loadingMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Pulsing indicator
            BudgeyPulsingLoadingIndicator(
                size = BudgeySize.loadingMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            // Dot indicator
            BudgeyDotLoadingIndicator(
                color = MaterialTheme.colorScheme.tertiary
            )

            // Progress indicators
            BudgeyProgressIndicator(
                progress = 0.6f,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            BudgeyCircularProgressIndicator(
                progress = 0.75f,
                size = 56.dp
            )

            // Skeleton loader
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                BudgeySkeletonLoader(
                    modifier = Modifier.padding(12.dp),
                    lines = 3
                )
            }
        }
    }
}
