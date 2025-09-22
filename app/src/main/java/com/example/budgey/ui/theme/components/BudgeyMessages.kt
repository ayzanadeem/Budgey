package com.example.budgey.ui.theme.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.budgey.ui.theme.*

enum class MessageType {
    SUCCESS, ERROR, WARNING, INFO
}

@Composable
fun BudgeyMessage(
    message: String,
    type: MessageType,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    val (backgroundColor, contentColor, borderColor, icon) = when (type) {
        MessageType.SUCCESS -> Quadruple(
            lightExtendedColors.successContainer,
            lightExtendedColors.onSuccessContainer,
            lightExtendedColors.success,
            Icons.Default.CheckCircle
        )
        MessageType.ERROR -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error,
            Icons.Default.Warning
        )
        MessageType.WARNING -> Quadruple(
            lightExtendedColors.warningContainer,
            lightExtendedColors.onWarningContainer,
            lightExtendedColors.warning,
            Icons.Default.Warning
        )
        MessageType.INFO -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline,
            Icons.Default.Info
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(BudgeyDuration.medium)
        ) + fadeIn(animationSpec = tween(BudgeyDuration.medium)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(BudgeyDuration.medium)
        ) + fadeOut(animationSpec = tween(BudgeyDuration.medium))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = borderColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(BudgeyRadius.md)
                ),
            shape = RoundedCornerShape(BudgeyRadius.md),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = BudgeyElevation.sm
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BudgeySpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(BudgeySize.iconMd)
                )

                Spacer(modifier = Modifier.width(BudgeySpacing.sm))

                Text(
                    text = message,
                    style = when (type) {
                        MessageType.SUCCESS -> BudgeyTextStyles.successMessage
                        MessageType.ERROR -> BudgeyTextStyles.errorMessage
                        MessageType.WARNING -> BudgeyTextStyles.warningMessage
                        MessageType.INFO -> MaterialTheme.typography.bodyMedium
                    },
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )

                if (action != null) {
                    Spacer(modifier = Modifier.width(BudgeySpacing.sm))
                    action()
                }

                if (onDismiss != null) {
                    Spacer(modifier = Modifier.width(BudgeySpacing.xs))
                    BudgeyIconButton(
                        onClick = onDismiss,
                        icon = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun BudgeySuccessMessage(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    BudgeyMessage(
        message = message,
        type = MessageType.SUCCESS,
        modifier = modifier,
        visible = visible,
        onDismiss = onDismiss,
        action = action
    )
}

@Composable
fun BudgeyErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null
) {
    BudgeyMessage(
        message = message,
        type = MessageType.ERROR,
        modifier = modifier,
        visible = visible,
        onDismiss = onDismiss,
        action = if (onRetry != null) {
            {
                BudgeyTextButton(
                    onClick = onRetry,
                    text = "Retry"
                )
            }
        } else null
    )
}

@Composable
fun BudgeyWarningMessage(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    BudgeyMessage(
        message = message,
        type = MessageType.WARNING,
        modifier = modifier,
        visible = visible,
        onDismiss = onDismiss,
        action = action
    )
}

@Composable
fun BudgeyInfoMessage(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onDismiss: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    BudgeyMessage(
        message = message,
        type = MessageType.INFO,
        modifier = modifier,
        visible = visible,
        onDismiss = onDismiss,
        action = action
    )
}

@Composable
fun BudgeySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BudgeySpacing.md),
                shape = RoundedCornerShape(BudgeyRadius.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = BudgeyElevation.lg
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(BudgeySpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.weight(1f)
                    )

                    snackbarData.visuals.actionLabel?.let { actionLabel ->
                        Spacer(modifier = Modifier.width(BudgeySpacing.sm))
                        BudgeyTextButton(
                            onClick = { snackbarData.performAction() },
                            text = actionLabel
                        )
                    }
                }
            }
        }
    )
}

// Helper data class for quadruple values
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
