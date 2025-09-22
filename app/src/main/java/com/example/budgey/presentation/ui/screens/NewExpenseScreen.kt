package com.example.budgey.presentation.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgey.R
import com.example.budgey.data.model.Category
import com.example.budgey.presentation.ui.state.NewExpenseUiState
import com.example.budgey.presentation.ui.state.UiMessage
import com.example.budgey.presentation.ui.state.UiMessageType
import com.example.budgey.presentation.viewmodel.NewExpenseViewModel
import com.example.budgey.ui.theme.*
import com.example.budgey.ui.theme.components.*
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Composable
fun NewExpenseScreen(
    modifier: Modifier = Modifier,
    viewModel: NewExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NewExpenseScreenContent(
        uiState = uiState,
        onAmountChange = viewModel::updateAmount,
        onCategorySelected = { categoryName ->
            // Convert string to category selection - this will be handled by the ViewModel
            viewModel.selectCategory(categoryName)
        },
        onDescriptionChange = viewModel::updateDescription,
        onStartDateSelected = { date ->
            // Convert LocalDate to Timestamp for ViewModel
            val timestamp = Timestamp(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            viewModel.updateBudgetStartDate(timestamp)
        },
        onEndDateSelected = { date ->
            // Convert LocalDate to Timestamp for ViewModel
            val timestamp = Timestamp(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            viewModel.updateBudgetEndDate(timestamp)
        },
        onSaveExpense = viewModel::saveExpense,
        onRetryLoadCategories = viewModel::loadCategories,
        onClearMessage = viewModel::clearMessage,
        modifier = modifier
    )
}

@Composable
private fun NewExpenseScreenContent(
    uiState: NewExpenseUiState,
    onAmountChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onSaveExpense: () -> Unit,
    onRetryLoadCategories: () -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }

    // Form validation states
    val isAmountValid = uiState.amount.isNotEmpty() &&
        uiState.amount.toDoubleOrNull()?.let { it > 0 } == true
    val isCategoryValid = uiState.selectedCategoryName.isNotEmpty()
    val areDatesValid = uiState.budgetStartDate != null && uiState.budgetEndDate != null
    val isFormValid = isAmountValid && isCategoryValid && areDatesValid

    // Handle success message with auto-clear
    LaunchedEffect(uiState.uiMessage) {
        if (uiState.uiMessage?.type == UiMessageType.SUCCESS) {
            kotlinx.coroutines.delay(3000) // Show success for 3 seconds
            onClearMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(BudgeySpacing.lg),
            verticalArrangement = Arrangement.spacedBy(BudgeySpacing.lg)
        ) {
            // Header
            ExpenseFormHeader()

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(BudgeySpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(BudgeySpacing.lg)
                ) {
                    // Amount Field
                    BudgeyTextField(
                        value = uiState.amount,
                        onValueChange = onAmountChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(amountFocusRequester),
                        label = "Amount (PKR)",
                        placeholder = "0.00",
                        leadingIcon = ImageVector.vectorResource(R.drawable.attach_money),
                        isError = uiState.amount.isNotEmpty() && !isAmountValid,
                        errorMessage = if (uiState.amount.isNotEmpty() && !isAmountValid) {
                            "Please enter a valid amount greater than 0"
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                descriptionFocusRequester.requestFocus()
                            }
                        ),
                        singleLine = true
                    )

                    // Category Dropdown with Shimmer/Error States
                    CategorySelectionSection(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategoryName,
                        isLoadingCategories = uiState.isLoadingCategories,
                        hasError = uiState.uiMessage?.type == UiMessageType.ERROR &&
                                   uiState.uiMessage.message.contains("categories"),
                        onCategorySelected = onCategorySelected,
                        onRetry = onRetryLoadCategories
                    )

                    // Description Field
                    BudgeyTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(descriptionFocusRequester),
                        label = "Description (Optional)",
                        placeholder = "Add a note about this expense",
                        leadingIcon = Icons.Default.Edit,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = false,
                        maxLines = 3
                    )

                    // Date Fields - Horizontal Layout
                    DateSelectionSection(
                        startDate = uiState.budgetStartDate?.toLocalDate(),
                        endDate = uiState.budgetEndDate?.toLocalDate(),
                        onStartDateSelected = onStartDateSelected,
                        onEndDateSelected = onEndDateSelected
                    )

                    Spacer(modifier = Modifier.height(BudgeySpacing.md))

                    // Save Button
                    BudgeyPrimaryButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onSaveExpense()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isFormValid,
                        loading = false, // Add loading state from ViewModel if needed
                        text = "Save Expense",
                        icon = Icons.Default.Add
                    )
                }
            }

            // Success/Error Messages
            AnimatedVisibility(
                visible = uiState.uiMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.uiMessage?.let { message ->
                    MessageCard(
                        message = message,
                        onDismiss = onClearMessage
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseFormHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Add New Expense",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Track your spending and manage your budget",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CategorySelectionSection(
    categories: List<Category>,
    selectedCategory: String,
    isLoadingCategories: Boolean,
    hasError: Boolean,
    onCategorySelected: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
    ) {
        when {
            isLoadingCategories -> {
                // Shimmer Effect for Loading Categories
                CategoryShimmerEffect()
            }
            hasError -> {
                // Error State with Retry
                CategoryErrorState(onRetry = onRetry)
            }
            else -> {
                // Normal Category Dropdown
                BudgeyDropdown(
                    options = categories.map { it.name },
                    selectedOption = selectedCategory.takeIf { it.isNotEmpty() },
                    onOptionSelected = onCategorySelected,
                    label = "Category",
                    placeholder = "Select a category",
                    leadingIcon = Icons.AutoMirrored.Outlined.List,
                    isError = selectedCategory.isEmpty(),
                    errorMessage = if (selectedCategory.isEmpty()) {
                        "Please select a category"
                    } else null,
                    searchable = true,
                    emptyMessage = "No categories found. Create one first!"
                )
            }
        }
    }
}

@Composable
private fun CategoryShimmerEffect() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    Column(verticalArrangement = Arrangement.spacedBy(BudgeySpacing.xs)) {
        Text(
            text = "Category",
            style = BudgeyTextStyles.inputLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = shimmerColors,
                        start = androidx.compose.ui.geometry.Offset(translateAnim.value - 200, 0f),
                        end = androidx.compose.ui.geometry.Offset(translateAnim.value, 0f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(BudgeySpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(BudgeySpacing.sm))
                Text(
                    text = "Loading categories...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CategoryErrorState(onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(BudgeySpacing.xs)) {
        Text(
            text = "Category",
            style = BudgeyTextStyles.inputLabel,
            color = MaterialTheme.colorScheme.error
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.error,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BudgeySpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Error fetching expense categories, please try again",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }

                BudgeySecondaryButton(
                    onClick = onRetry,
                    text = "Retry",
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DateSelectionSection(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)) {
        Text(
            text = "Budget Period",
            style = BudgeyTextStyles.inputLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
        ) {
            // Start Date
            BudgeyDatePicker(
                selectedDate = startDate,
                onDateSelected = onStartDateSelected,
                modifier = Modifier.weight(1f),
                label = "Start Date",
                placeholder = "Start",
                isError = startDate == null,
                errorMessage = if (startDate == null) "Required" else null
            )

            // End Date
            BudgeyDatePicker(
                selectedDate = endDate,
                onDateSelected = onEndDateSelected,
                modifier = Modifier.weight(1f),
                label = "End Date",
                placeholder = "End",
                isError = endDate == null || (startDate != null && endDate.isBefore(startDate)),
                errorMessage = when {
                    endDate == null -> "Required"
                    startDate != null && endDate.isBefore(startDate) -> "Must be after start date"
                    else -> null
                }
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: UiMessage,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (message.type) {
                UiMessageType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                UiMessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgeySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
        ) {
            Icon(
                imageVector = when (message.type) {
                    UiMessageType.SUCCESS -> Icons.Default.CheckCircle
                    UiMessageType.ERROR -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = when (message.type) {
                    UiMessageType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                    UiMessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                },
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = when (message.type) {
                    UiMessageType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                    UiMessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = when (message.type) {
                        UiMessageType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                        UiMessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Extension function to convert Timestamp to LocalDate
private fun Timestamp.toLocalDate(): LocalDate {
    return this.toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

// Preview Composables
@Preview(showBackground = true, name = "New Expense Screen - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "New Expense Screen - Dark")
@Composable
private fun NewExpenseScreenPreview() {
    BudgeyTheme {
        NewExpenseScreenContent(
            uiState = NewExpenseUiState(
                amount = "150.00",
                selectedCategoryName = "Food",
                description = "Lunch at restaurant",
                categories = listOf(
                    Category(name = "Food", userId = "user1", createdAt = Timestamp.now(), isActive = true),
                    Category(name = "Transport", userId = "user1", createdAt = Timestamp.now(), isActive = true)
                )
            ),
            onAmountChange = {},
            onCategorySelected = {},
            onDescriptionChange = {},
            onStartDateSelected = {},
            onEndDateSelected = {},
            onSaveExpense = {},
            onRetryLoadCategories = {},
            onClearMessage = {}
        )
    }
}

@Preview(showBackground = true, name = "New Expense Screen - Loading")
@Composable
private fun NewExpenseScreenLoadingPreview() {
    BudgeyTheme {
        NewExpenseScreenContent(
            uiState = NewExpenseUiState(
                isLoadingCategories = true,
                amount = "25.50"
            ),
            onAmountChange = {},
            onCategorySelected = {},
            onDescriptionChange = {},
            onStartDateSelected = {},
            onEndDateSelected = {},
            onSaveExpense = {},
            onRetryLoadCategories = {},
            onClearMessage = {}
        )
    }
}

@Preview(showBackground = true, name = "New Expense Screen - Error")
@Composable
private fun NewExpenseScreenErrorPreview() {
    BudgeyTheme {
        NewExpenseScreenContent(
            uiState = NewExpenseUiState(
                uiMessage = UiMessage(
                    type = UiMessageType.ERROR,
                    message = "Error fetching expense categories, please try again"
                )
            ),
            onAmountChange = {},
            onCategorySelected = {},
            onDescriptionChange = {},
            onStartDateSelected = {},
            onEndDateSelected = {},
            onSaveExpense = {},
            onRetryLoadCategories = {},
            onClearMessage = {}
        )
    }
}
