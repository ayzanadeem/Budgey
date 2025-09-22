package com.example.budgey.presentation.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgey.R
import com.example.budgey.data.model.Expense
import com.example.budgey.domain.usecase.CategoryBreakdown
import com.example.budgey.domain.usecase.ExpenseBreakdown
import com.example.budgey.domain.usecase.MonthlyBreakdown
import com.example.budgey.presentation.ui.state.ExpenseBreakdownUiState
import com.example.budgey.presentation.viewmodel.ExpenseBreakdownViewModel
import com.example.budgey.ui.theme.*
import com.example.budgey.ui.theme.components.*
import com.google.firebase.Timestamp
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBreakdownScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseBreakdownViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExpenseBreakdownScreenContent(
        uiState = uiState,
        onRetry = viewModel::retryLoadExpenseBreakdown,
        onRefresh = viewModel::refreshExpenseBreakdown,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseBreakdownScreenContent(
    uiState: ExpenseBreakdownUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State for expandable sections
    var expandedMonths by remember { mutableStateOf(setOf<String>()) }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    val lazyListState = rememberLazyListState()

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            ExpenseBreakdownHeader()

            when {
                uiState.isLoading -> {
                    // Shimmer loading effect
                    ExpenseBreakdownShimmer()
                }
                uiState.errorMessage != null -> {
                    // Error state
                    ExpenseBreakdownError(
                        errorMessage = uiState.errorMessage,
                        onRetry = onRetry
                    )
                }
                uiState.expenseBreakdown?.monthlyBreakdowns?.isNotEmpty() == true -> {
                    // Content with data
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = BudgeySpacing.lg,
                            vertical = BudgeySpacing.md
                        ),
                        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
                    ) {
                        items(
                            items = uiState.expenseBreakdown.monthlyBreakdowns,
                            key = { it.monthKey }
                        ) { monthBreakdown ->
                            MonthlyBreakdownItem(
                                monthBreakdown = monthBreakdown,
                                isExpanded = expandedMonths.contains(monthBreakdown.monthKey),
                                onToggleMonth = { monthKey ->
                                    expandedMonths = if (expandedMonths.contains(monthKey)) {
                                        expandedMonths - monthKey
                                    } else {
                                        expandedMonths + monthKey
                                    }
                                },
                                expandedCategories = expandedCategories,
                                onToggleCategory = { categoryKey ->
                                    expandedCategories = if (expandedCategories.contains(categoryKey)) {
                                        expandedCategories - categoryKey
                                    } else {
                                        expandedCategories + categoryKey
                                    }
                                }
                            )
                        }
                    }
                }
                else -> {
                    // Empty state
                    ExpenseBreakdownEmptyState()
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownHeader() {
    Column(
        modifier = Modifier.padding(BudgeySpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.trending_up),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "My Expenses",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Track and analyze your spending patterns",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ExpenseBreakdownShimmer() {
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(BudgeySpacing.lg),
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
    ) {
        items(3) { monthIndex ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(BudgeySpacing.lg)) {
                    // Month header shimmer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = shimmerColors,
                                    start = androidx.compose.ui.geometry.Offset(translateAnim.value - 200, 0f),
                                    end = androidx.compose.ui.geometry.Offset(translateAnim.value, 0f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(BudgeySpacing.md))

                    // Category items shimmer
                    repeat(3) { categoryIndex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = BudgeySpacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = shimmerColors,
                                            start = androidx.compose.ui.geometry.Offset(translateAnim.value - 200, 0f),
                                            end = androidx.compose.ui.geometry.Offset(translateAnim.value, 0f)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.width(BudgeySpacing.md))

                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(24.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = shimmerColors,
                                            start = androidx.compose.ui.geometry.Offset(translateAnim.value - 200, 0f),
                                            end = androidx.compose.ui.geometry.Offset(translateAnim.value, 0f)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownError(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BudgeySpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                    .padding(BudgeySpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Error loading expenses",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(BudgeySpacing.sm))

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
private fun ExpenseBreakdownEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(BudgeySpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.receipt),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(BudgeySpacing.lg))

        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Start adding expenses to see your breakdown here",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = BudgeySpacing.sm)
        )
    }
}

@Composable
private fun MonthlyBreakdownItem(
    monthBreakdown: MonthlyBreakdown,
    isExpanded: Boolean,
    onToggleMonth: (String) -> Unit,
    expandedCategories: Set<String>,
    onToggleCategory: (String) -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Month Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .clickable { onToggleMonth(monthBreakdown.monthKey) }
                    .padding(BudgeySpacing.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatMonthRange(monthBreakdown.monthKey),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${monthBreakdown.categories.size} categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
                    ) {
                        Text(
                            text = "PKR ${formatAmount(monthBreakdown.totalExpenses)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .rotate(rotationAngle)
                                .size(24.dp)
                        )
                    }
                }
            }

            // Categories Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier.padding(BudgeySpacing.lg)
                ) {
                    monthBreakdown.categories.forEach { categoryBreakdown ->
                        CategoryBreakdownItem(
                            categoryBreakdown = categoryBreakdown,
                            monthKey = monthBreakdown.monthKey,
                            isExpanded = expandedCategories.contains("${monthBreakdown.monthKey}_${categoryBreakdown.categoryName}"),
                            onToggleCategory = {
                                onToggleCategory("${monthBreakdown.monthKey}_${categoryBreakdown.categoryName}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownItem(
    categoryBreakdown: CategoryBreakdown,
    monthKey: String,
    isExpanded: Boolean,
    onToggleCategory: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "category_arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BudgeySpacing.xs)
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleCategory() }
                    .padding(BudgeySpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.sm)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.folder),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        Text(
                            text = categoryBreakdown.categoryName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${categoryBreakdown.expenses.size} expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.xs)
                ) {
                    Text(
                        text = "PKR ${formatAmount(categoryBreakdown.totalAmount)}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .size(20.dp)
                    )
                }
            }

            // Individual Expenses
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = BudgeySpacing.lg,
                        end = BudgeySpacing.md,
                        bottom = BudgeySpacing.md
                    )
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(BudgeySpacing.sm))

                    categoryBreakdown.expenses.forEach { expense ->
                        ExpenseItem(expense = expense)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = BudgeySpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (expense.description?.isNotBlank() == true) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatDate(expense.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "PKR ${formatAmount(expense.amount)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Helper functions
private fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    if (formatter is DecimalFormat) {
        formatter.maximumFractionDigits = 0
    }
    return formatter.format(amount)
}

private fun formatMonthRange(monthKey: String): String {
    // Format: "DD MMM YY - DD MMM YY"
    // This is a simplified version - adjust based on your monthKey format
    return try {
        val parts = monthKey.split("-")
        if (parts.size >= 2) {
            "${parts[0]} - ${parts[1]}"
        } else {
            monthKey
        }
    } catch (e: Exception) {
        monthKey
    }
}

private fun formatDate(timestamp: Timestamp?): String {
    return timestamp?.let {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(it.toDate())
    } ?: ""
}

// Preview Composables
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Expense Breakdown - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Expense Breakdown - Dark")
@Composable
private fun ExpenseBreakdownScreenPreview() {
    BudgeyTheme {
        ExpenseBreakdownScreenContent(
            uiState = ExpenseBreakdownUiState(
                expenseBreakdown = ExpenseBreakdown(
                    monthlyBreakdowns = listOf(
                        MonthlyBreakdown(
                            monthKey = "2024-01",
                            monthDisplayName = "January 2024",
                            categories = listOf(
                                CategoryBreakdown(
                                    categoryId = "food",
                                    categoryName = "Food",
                                    expenses = listOf(
                                        Expense(
                                            amount = 15000.0,
                                            description = "Grocery shopping",
                                            createdAt = Timestamp.now()
                                        ),
                                        Expense(
                                            amount = 10000.0,
                                            description = "Restaurant dinner",
                                            createdAt = Timestamp.now()
                                        )
                                    ),
                                    totalAmount = 25000.0,
                                    expenseCount = 2,
                                    averageAmount = 12500.0,
                                    percentage = 54.9
                                ),
                                CategoryBreakdown(
                                    categoryId = "transport",
                                    categoryName = "Transport",
                                    expenses = listOf(
                                        Expense(
                                            amount = 20650.0,
                                            description = "Fuel",
                                            createdAt = Timestamp.now()
                                        )
                                    ),
                                    totalAmount = 20650.0,
                                    expenseCount = 1,
                                    averageAmount = 20650.0,
                                    percentage = 45.1
                                )
                            ),
                            totalExpenses = 45650.0,
                            totalIncome = 0.0,
                            netAmount = -45650.0,
                            expenseCount = 3,
                            incomeCount = 0
                        )
                    ),
                    overallTotals = com.example.budgey.domain.usecase.OverallTotals(
                        totalExpenses = 45650.0,
                        totalIncome = 0.0,
                        netAmount = -45650.0,
                        monthCount = 1,
                        averageMonthlyExpenses = 45650.0,
                        averageMonthlyIncome = 0.0,
                        topExpenseCategory = "Food",
                        topIncomeCategory = null
                    )
                )
            ),
            onRetry = {},
            onRefresh = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Expense Breakdown - Loading")
@Composable
private fun ExpenseBreakdownLoadingPreview() {
    BudgeyTheme {
        ExpenseBreakdownScreenContent(
            uiState = ExpenseBreakdownUiState(isLoading = true),
            onRetry = {},
            onRefresh = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Expense Breakdown - Error")
@Composable
private fun ExpenseBreakdownErrorPreview() {
    BudgeyTheme {
        ExpenseBreakdownScreenContent(
            uiState = ExpenseBreakdownUiState(
                errorMessage = "Failed to load expense breakdown. Please check your connection and try again."
            ),
            onRetry = {},
            onRefresh = {}
        )
    }
}
