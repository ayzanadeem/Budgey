package com.example.budgey.ui.theme.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.budgey.R
import com.example.budgey.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgeyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(BudgeyDuration.medium),
        label = "border_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = tween(BudgeyDuration.medium),
        label = "scale"
    )

    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = BudgeyTextStyles.inputLabel,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFocused -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(bottom = BudgeySpacing.xs)
            )
        }

        // Text Field
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .focusRequester(focusRequester)
                .border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(BudgeyRadius.inputField)
                ),
            placeholder = if (placeholder != null) {
                {
                    Text(
                        text = placeholder,
                        style = BudgeyTextStyles.inputPlaceholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = when {
                            isError -> MaterialTheme.colorScheme.error
                            isFocused -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(BudgeySize.iconMd)
                    )
                }
            } else null,
            trailingIcon = if (trailingIcon != null) {
                {
                    IconButton(
                        onClick = { onTrailingIconClick?.invoke() }
                    ) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = when {
                                isError -> MaterialTheme.colorScheme.error
                                isFocused -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(BudgeySize.iconMd)
                        )
                    }
                }
            } else null,
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = BudgeyTextStyles.inputText,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(BudgeyRadius.inputField),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                errorTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = BudgeyTextStyles.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    start = BudgeySpacing.sm,
                    top = BudgeySpacing.xs
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgeyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = tween(BudgeyDuration.medium),
        label = "scale"
    )

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            label = if (label != null) {
                {
                    Text(
                        text = label,
                        style = BudgeyTextStyles.inputLabel
                    )
                }
            } else null,
            placeholder = if (placeholder != null) {
                {
                    Text(
                        text = placeholder,
                        style = BudgeyTextStyles.inputPlaceholder
                    )
                }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(BudgeySize.iconMd)
                    )
                }
            } else null,
            trailingIcon = if (trailingIcon != null) {
                {
                    IconButton(
                        onClick = { onTrailingIconClick?.invoke() }
                    ) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(BudgeySize.iconMd)
                        )
                    }
                }
            } else null,
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = BudgeyTextStyles.inputText,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(BudgeyRadius.inputField),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                errorLabelColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                errorTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = BudgeyTextStyles.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    start = BudgeySpacing.sm,
                    top = BudgeySpacing.xs
                )
            )
        }
    }
}

@Composable
fun BudgeySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    enabled: Boolean = true,
    onClearClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        animationSpec = tween(BudgeyDuration.medium),
        label = "background_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BudgeySize.inputFieldCompact)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(BudgeyRadius.inputField)
            )
            .border(
                width = if (isFocused) 1.5.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(BudgeyRadius.inputField)
            )
            .padding(horizontal = BudgeySpacing.md),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(BudgeySize.iconMd)
            )

            Spacer(modifier = Modifier.width(BudgeySpacing.sm))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                textStyle = BudgeyTextStyles.inputText.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = BudgeyTextStyles.inputPlaceholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )

            if (value.isNotEmpty() && onClearClick != null) {
                BudgeyIconButton(
                    onClick = onClearClick,
                    icon = Icons.Default.Clear,
                    contentDescription = "Clear search"
                )
            }
        }
    }
}

// Preview Composables
@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyTextFieldPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic text field
            BudgeyTextField(
                value = "Sample input text",
                onValueChange = {},
                label = "Email Address",
                placeholder = "Enter your email"
            )

            // Text field with leading icon
            BudgeyTextField(
                value = "",
                onValueChange = {},
                label = "Amount",
                placeholder = "0.00",
                leadingIcon = ImageVector.vectorResource(R.drawable.attach_money)
            )

            // Error state
            BudgeyTextField(
                value = "invalid@",
                onValueChange = {},
                label = "Email",
                placeholder = "Enter email",
                isError = true,
                errorMessage = "Please enter a valid email address"
            )

            // Disabled state
            BudgeyTextField(
                value = "Disabled field",
                onValueChange = {},
                label = "Disabled Field",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyOutlinedTextFieldPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic outlined text field
            BudgeyOutlinedTextField(
                value = "Sample text",
                onValueChange = {},
                label = "Category Name",
                placeholder = "Enter category name"
            )

            // With icons
            BudgeyOutlinedTextField(
                value = "",
                onValueChange = {},
                label = "Description",
                placeholder = "Enter description",
                leadingIcon = Icons.Default.Edit,
                trailingIcon = Icons.Default.Clear
            )

            // Error state
            BudgeyOutlinedTextField(
                value = "Invalid input",
                onValueChange = {},
                label = "Category",
                isError = true,
                errorMessage = "Category name already exists"
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeySearchFieldPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Empty search field
            BudgeySearchField(
                value = "",
                onValueChange = {},
                placeholder = "Search expenses..."
            )

            // Search field with text
            BudgeySearchField(
                value = "Food",
                onValueChange = {},
                placeholder = "Search categories...",
                onClearClick = {}
            )

            // Disabled search field
            BudgeySearchField(
                value = "Disabled search",
                onValueChange = {},
                enabled = false
            )
        }
    }
}
