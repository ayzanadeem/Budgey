package com.example.budgey.ui.theme.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.budgey.ui.theme.BudgeyDuration
import com.example.budgey.ui.theme.BudgeyRadius
import com.example.budgey.ui.theme.BudgeySize
import com.example.budgey.ui.theme.BudgeySpacing
import com.example.budgey.ui.theme.BudgeyTextStyles
import com.example.budgey.ui.theme.BudgeyTheme

// Custom button variants for Budgey app
@Composable
fun BudgeyPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    text: String,
    icon: ImageVector? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(BudgeyDuration.button),
        label = "button_scale"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(BudgeyDuration.button),
        label = "button_color"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .size(
                width = 200.dp,
                height = BudgeySize.buttonLarge
            ),
        enabled = enabled && !loading,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(BudgeyRadius.button),
        contentPadding = PaddingValues(
            horizontal = BudgeySpacing.buttonPaddingHorizontal,
            vertical = BudgeySpacing.buttonPaddingVertical
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (loading) {
            BudgeyLoadingIndicator(
                size = BudgeySize.loadingSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(BudgeySize.iconSm)
                )
                Spacer(modifier = Modifier.size(BudgeySpacing.xs))
            }
            Text(
                text = text,
                style = BudgeyTextStyles.buttonMedium
            )
        }
    }
}

@Composable
fun BudgeySecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    text: String,
    icon: ImageVector? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(BudgeyDuration.button),
        label = "button_scale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .size(
                width = 200.dp,
                height = BudgeySize.buttonLarge
            ),
        enabled = enabled && !loading,
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled && !loading).copy(
            width = 1.5.dp
        ),
        shape = RoundedCornerShape(BudgeyRadius.button),
        contentPadding = PaddingValues(
            horizontal = BudgeySpacing.buttonPaddingHorizontal,
            vertical = BudgeySpacing.buttonPaddingVertical
        )
    ) {
        if (loading) {
            BudgeyLoadingIndicator(
                size = BudgeySize.loadingSmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(BudgeySize.iconSm)
                )
                Spacer(modifier = Modifier.size(BudgeySpacing.xs))
            }
            Text(
                text = text,
                style = BudgeyTextStyles.buttonMedium
            )
        }
    }
}

@Composable
fun BudgeyTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(BudgeyDuration.button),
        label = "button_scale"
    )

    TextButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(
            horizontal = BudgeySpacing.lg,
            vertical = BudgeySpacing.sm
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(BudgeySize.iconSm)
            )
            Spacer(modifier = Modifier.size(BudgeySpacing.xs))
        }
        Text(
            text = text,
            style = BudgeyTextStyles.buttonMedium
        )
    }
}

@Composable
fun BudgeyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(BudgeyDuration.button),
        label = "icon_button_scale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            modifier = Modifier.size(BudgeySize.iconMd)
        )
    }
}

// Preview Composables
@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyPrimaryButtonPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Normal state
            BudgeyPrimaryButton(
                onClick = {},
                text = "Save Expense"
            )

            // With icon
            BudgeyPrimaryButton(
                onClick = {},
                text = "Add Category",
                icon = Icons.Default.Add
            )

            // Loading state
            BudgeyPrimaryButton(
                onClick = {},
                text = "Saving...",
                loading = true
            )

            // Disabled state
            BudgeyPrimaryButton(
                onClick = {},
                text = "Disabled",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeySecondaryButtonPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Normal state
            BudgeySecondaryButton(
                onClick = {},
                text = "Cancel"
            )

            // With icon
            BudgeySecondaryButton(
                onClick = {},
                text = "Edit",
                icon = Icons.Default.Edit
            )

            // Loading state
            BudgeySecondaryButton(
                onClick = {},
                text = "Loading",
                loading = true
            )

            // Disabled state
            BudgeySecondaryButton(
                onClick = {},
                text = "Disabled",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyTextButtonPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Normal state
            BudgeyTextButton(
                onClick = {},
                text = "Skip"
            )

            // With icon
            BudgeyTextButton(
                onClick = {},
                text = "Learn More",
                icon = Icons.Default.Info
            )

            // Disabled state
            BudgeyTextButton(
                onClick = {},
                text = "Disabled",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyIconButtonPreview() {
    BudgeyTheme {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Normal state
            BudgeyIconButton(
                onClick = {},
                icon = Icons.Default.Favorite,
                contentDescription = "Favorite"
            )

            // Different icon
            BudgeyIconButton(
                onClick = {},
                icon = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )

            // Disabled state
            BudgeyIconButton(
                onClick = {},
                icon = Icons.Default.Lock,
                contentDescription = "Locked",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "All Button Variants - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "All Button Variants - Dark")
@Composable
private fun BudgeyAllButtonsPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Budgey Button Components",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            BudgeyPrimaryButton(
                onClick = {},
                text = "Primary Action",
                icon = Icons.Filled.Add
            )

            BudgeySecondaryButton(
                onClick = {},
                text = "Secondary Action",
                icon = Icons.Default.Edit
            )

            BudgeyTextButton(
                onClick = {},
                text = "Text Action",
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BudgeyIconButton(
                    onClick = {},
                    icon = Icons.Default.Add,
                    contentDescription = "Add"
                )

                BudgeyIconButton(
                    onClick = {},
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings"
                )

                BudgeyIconButton(
                    onClick = {},
                    icon = Icons.Default.MoreVert,
                    contentDescription = "More"
                )
            }
        }
    }
}
