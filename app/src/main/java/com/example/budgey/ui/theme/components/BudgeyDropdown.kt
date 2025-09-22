package com.example.budgey.ui.theme.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.budgey.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgeyDropdown(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select option",
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    searchable: Boolean = false,
    emptyMessage: String = "No options available",
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val filteredOptions = remember(options, searchQuery) {
        if (searchQuery.isEmpty()) {
            options
        } else {
            options.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused || expanded) 1.02f else 1f,
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
                    isFocused || expanded -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(bottom = BudgeySpacing.xs)
            )
        }

        // Dropdown Field
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (enabled) {
                    expanded = !expanded
                    if (!expanded) {
                        searchQuery = ""
                        keyboardController?.hide()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedOption ?: "",
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = enabled)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = BudgeyTextStyles.inputPlaceholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = if (leadingIcon != null) {
                    {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = when {
                                isError -> MaterialTheme.colorScheme.error
                                isFocused || expanded -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(BudgeySize.iconMd)
                        )
                    }
                } else null,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                isError = isError,
                enabled = enabled,
                readOnly = true,
                textStyle = BudgeyTextStyles.inputText,
                singleLine = true,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(BudgeyRadius.inputField),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    errorTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            // Dropdown Menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchQuery = ""
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .exposedDropdownSize()
                    .heightIn(max = 200.dp)
            ) {
                // Search field for searchable dropdown
                if (searchable) {
                    BudgeyTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search options...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        trailingIcon = if (searchQuery.isNotEmpty()) Icons.Default.Clear else null,
                        onTrailingIconClick = { searchQuery = "" }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }

                // Options list
                if (filteredOptions.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(BudgeySpacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn {
                        items(filteredOptions) { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        style = BudgeyTextStyles.dropdownItem,
                                        color = if (option == selectedOption) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                },
                                onClick = {
                                    onOptionSelected(option)
                                    expanded = false
                                    searchQuery = ""
                                    keyboardController?.hide()
                                },
                                leadingIcon = if (option == selectedOption) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(BudgeySize.iconSm)
                                        )
                                    }
                                } else null,
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    leadingIconColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = BudgeySpacing.md,
                                    vertical = BudgeySpacing.sm
                                )
                            )
                        }
                    }
                }
            }
        }

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
fun BudgeyMultiSelectDropdown(
    options: List<String>,
    selectedOptions: List<String>,
    onOptionToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select options",
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    searchable: Boolean = true,
    maxSelections: Int? = null,
    emptyMessage: String = "No options available",
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val filteredOptions = remember(options, searchQuery) {
        if (searchQuery.isEmpty()) {
            options
        } else {
            options.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    val displayText = when {
        selectedOptions.isEmpty() -> ""
        selectedOptions.size == 1 -> selectedOptions.first()
        else -> "${selectedOptions.size} items selected"
    }

    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = BudgeyTextStyles.inputLabel,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFocused || expanded -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(bottom = BudgeySpacing.xs)
            )
        }

        // Multi-select field
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (enabled) {
                    expanded = !expanded
                    if (!expanded) {
                        searchQuery = ""
                        keyboardController?.hide()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = enabled),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = BudgeyTextStyles.inputPlaceholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = if (leadingIcon != null) {
                    {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = when {
                                isError -> MaterialTheme.colorScheme.error
                                isFocused || expanded -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(BudgeySize.iconMd)
                        )
                    }
                } else null,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedOptions.isNotEmpty()) {
                            BudgeyIconButton(
                                onClick = { selectedOptions.forEach { onOptionToggled(it) } },
                                icon = Icons.Default.Clear,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                isError = isError,
                enabled = enabled,
                readOnly = true,
                textStyle = BudgeyTextStyles.inputText,
                singleLine = true,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(BudgeyRadius.inputField)
            )

            // Multi-select menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchQuery = ""
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .exposedDropdownSize()
                    .heightIn(max = 250.dp)
            ) {
                // Search field
                if (searchable) {
                    BudgeyTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search options...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        trailingIcon = if (searchQuery.isNotEmpty()) Icons.Default.Clear else null,
                        onTrailingIconClick = { searchQuery = "" }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }

                // Options with checkboxes
                if (filteredOptions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(BudgeySpacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn {
                        items(filteredOptions) { option ->
                            val isSelected = selectedOptions.contains(option)
                            val canSelect = maxSelections == null ||
                                selectedOptions.size < maxSelections ||
                                isSelected

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        style = BudgeyTextStyles.dropdownItem,
                                        color = if (canSelect) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        }
                                    )
                                },
                                onClick = {
                                    if (canSelect) {
                                        onOptionToggled(option)
                                    }
                                },
                                leadingIcon = {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (canSelect) {
                                                onOptionToggled(option)
                                            }
                                        },
                                        enabled = canSelect,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                },
                                enabled = canSelect,
                                contentPadding = PaddingValues(
                                    horizontal = BudgeySpacing.md,
                                    vertical = BudgeySpacing.xs
                                )
                            )
                        }
                    }
                }
            }
        }

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

// Preview Composables
@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyDropdownPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val categories = listOf("Food", "Transportation", "Entertainment", "Shopping", "Bills", "Health")

            // Basic dropdown
            BudgeyDropdown(
                options = categories,
                selectedOption = "Food",
                onOptionSelected = {},
                label = "Category",
                placeholder = "Select category"
            )

            // Searchable dropdown
            BudgeyDropdown(
                options = categories,
                selectedOption = null,
                onOptionSelected = {},
                label = "Category",
                placeholder = "Search categories...",
                leadingIcon = Icons.AutoMirrored.Outlined.List,
                searchable = true
            )

            // Error state
            BudgeyDropdown(
                options = categories,
                selectedOption = null,
                onOptionSelected = {},
                label = "Required Category",
                placeholder = "Select category",
                isError = true,
                errorMessage = "Please select a category"
            )

            // Empty options
            BudgeyDropdown(
                options = emptyList(),
                selectedOption = null,
                onOptionSelected = {},
                label = "Empty Dropdown",
                placeholder = "No options",
                emptyMessage = "No categories found"
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyMultiSelectDropdownPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val tags = listOf("Important", "Recurring", "Tax Deductible", "Business", "Personal", "Urgent")

            // Multi-select dropdown
            BudgeyMultiSelectDropdown(
                options = tags,
                selectedOptions = listOf("Important", "Recurring"),
                onOptionToggled = {},
                label = "Tags",
                placeholder = "Select tags",
                leadingIcon = Icons.Default.Home
            )

            // With max selections
            BudgeyMultiSelectDropdown(
                options = tags,
                selectedOptions = listOf("Business"),
                onOptionToggled = {},
                label = "Priority Tags (Max 2)",
                placeholder = "Select up to 2 tags",
                maxSelections = 2
            )
        }
    }
}
