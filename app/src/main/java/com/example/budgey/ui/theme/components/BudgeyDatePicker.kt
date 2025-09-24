package com.example.budgey.ui.theme.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.budgey.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgeyDatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select date",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Create separate interaction source for the clickable area
    val clickableInteractionSource = remember { MutableInteractionSource() }
    val isFocused by clickableInteractionSource.collectIsFocusedAsState()

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    val displayText = selectedDate?.format(dateFormatter) ?: ""

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

        // Date Input Field - Wrap in clickable Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    enabled = enabled,
                    interactionSource = clickableInteractionSource,
                    indication = null
                ) {
                    if (enabled) {
                        showDatePicker = true
                    }
                }
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = BudgeyTextStyles.inputPlaceholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date",
                        tint = when {
                            isError -> MaterialTheme.colorScheme.error
                            isFocused -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(BudgeySize.iconMd)
                    )
                },
                trailingIcon = if (selectedDate != null && enabled) {
                    {
                        IconButton(
                            onClick = {
                                // Clear the date by setting it to null or current date
                                // You might want to pass null instead of LocalDate.now()
                                onDateSelected(LocalDate.now())
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear date",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(BudgeySize.iconSm)
                            )
                        }
                    }
                } else null,
                isError = isError,
                enabled = false, // Disable the TextField to prevent focus
                readOnly = true,
                textStyle = BudgeyTextStyles.inputText,
                singleLine = true,
                shape = RoundedCornerShape(BudgeyRadius.inputField),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = when {
                        isError -> MaterialTheme.colorScheme.error
                        isFocused -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    },
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    errorTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledLeadingIconColor = when {
                        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        isError -> MaterialTheme.colorScheme.error
                        isFocused -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
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

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                BudgeyTextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            onDateSelected(date)
                        }
                        showDatePicker = false
                    },
                    text = "OK"
                )
            },
            dismissButton = {
                BudgeyTextButton(
                    onClick = { showDatePicker = false },
                    text = "Cancel"
                )
            },
            shape = RoundedCornerShape(BudgeyRadius.lg),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun BudgeyDateRangePicker(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    startLabel: String = "Start Date",
    endLabel: String = "End Date",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
        ) {
            BudgeyDatePicker(
                selectedDate = startDate,
                onDateSelected = onStartDateSelected,
                label = startLabel,
                placeholder = "Select start",
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            BudgeyDatePicker(
                selectedDate = endDate,
                onDateSelected = onEndDateSelected,
                label = endLabel,
                placeholder = "Select end",
                enabled = enabled,
                isError = isError && endDate != null && startDate != null && endDate.isBefore(startDate),
                modifier = Modifier.weight(1f)
            )
        }

        // Global error message for date range
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = BudgeyTextStyles.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = BudgeySpacing.sm)
            )
        }
    }
}

// Preview Composables
@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyDatePickerPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic date picker
            BudgeyDatePicker(
                selectedDate = LocalDate.now(),
                onDateSelected = {},
                label = "Expense Date",
                placeholder = "Select date"
            )

            // Empty date picker
            BudgeyDatePicker(
                selectedDate = null,
                onDateSelected = {},
                label = "Budget Start Date",
                placeholder = "Select start date"
            )

            // Error state
            BudgeyDatePicker(
                selectedDate = null,
                onDateSelected = {},
                label = "Required Date",
                placeholder = "Select date",
                isError = true,
                errorMessage = "Please select a date"
            )

            // Disabled state
            BudgeyDatePicker(
                selectedDate = LocalDate.now().minusDays(1),
                onDateSelected = {},
                label = "Locked Date",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Theme")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Theme")
@Composable
private fun BudgeyDateRangePickerPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Date Range Picker",
                style = MaterialTheme.typography.headlineSmall
            )

            // Basic date range picker
            BudgeyDateRangePicker(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(30),
                onStartDateSelected = {},
                onEndDateSelected = {},
                startLabel = "Budget Start",
                endLabel = "Budget End"
            )

            // Error state
            BudgeyDateRangePicker(
                startDate = LocalDate.now(),
                endDate = LocalDate.now().minusDays(5),
                onStartDateSelected = {},
                onEndDateSelected = {},
                isError = true,
                errorMessage = "End date must be after start date",
                startLabel = "Period Start",
                endLabel = "Period End"
            )
        }
    }
}
