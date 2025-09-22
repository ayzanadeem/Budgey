package com.example.budgey.presentation.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgey.presentation.ui.state.AddCategoryUiState
import com.example.budgey.presentation.viewmodel.AddCategoryViewModel
import com.example.budgey.ui.theme.*
import com.example.budgey.ui.theme.components.*
import kotlinx.coroutines.delay

/**
 * Add Category Screen - Allows users to create new expense categories
 * Features comprehensive validation, loading states, and success/error feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    modifier: Modifier = Modifier,
    viewModel: AddCategoryViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddCategoryScreenContent(
        uiState = uiState,
        onCategoryNameChange = viewModel::updateCategoryName,
        onSaveCategory = viewModel::saveCategory,
        onClearError = viewModel::clearError,
        onClearSavedState = viewModel::clearSavedState,
        onResetForm = viewModel::resetForm,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategoryScreenContent(
    uiState: AddCategoryUiState,
    onCategoryNameChange: (String) -> Unit,
    onSaveCategory: () -> Unit,
    onClearError: () -> Unit,
    onClearSavedState: () -> Unit,
    onResetForm: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Auto-hide success message after 3 seconds
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            delay(3000)
            onClearSavedState()
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
                .verticalScroll(scrollState)
                .padding(BudgeySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BudgeySpacing.lg)
        ) {
            // Header Section
            AddCategoryHeader(onNavigateBack = onNavigateBack)

            // Form Section
            AddCategoryForm(
                uiState = uiState,
                onCategoryNameChange = onCategoryNameChange,
                onSaveCategory = onSaveCategory,
                onClearError = onClearError,
                focusManager = focusManager
            )

            // Success/Error Messages
            MessageSection(
                uiState = uiState,
                onClearError = onClearError
            )

            Spacer(modifier = Modifier.height(BudgeySpacing.xl))
        }
    }
}

@Composable
private fun AddCategoryHeader(
    onNavigateBack: (() -> Unit)?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
    ) {
        // Back button row
        if (onNavigateBack != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.semantics {
                        contentDescription = "Navigate back"
                        role = Role.Button
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Header content
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Add New Category",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Create a new category to organize your expenses",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddCategoryForm(
    uiState: AddCategoryUiState,
    onCategoryNameChange: (String) -> Unit,
    onSaveCategory: () -> Unit,
    onClearError: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgeySpacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgeySpacing.lg)
        ) {
            // Category Name Input
            CategoryNameInput(
                categoryName = uiState.categoryName,
                onCategoryNameChange = onCategoryNameChange,
                hasError = uiState.errorMessage != null,
                isEnabled = !uiState.isSaving
            )

            // Validation Error Message (inline with input)
            AnimatedVisibility(
                visible = uiState.errorMessage != null && !uiState.isSaved,
                enter = slideInVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                InputErrorMessage(
                    errorMessage = uiState.errorMessage ?: "",
                    onDismiss = onClearError
                )
            }

            Spacer(modifier = Modifier.height(BudgeySpacing.sm))

            // Save Button
            SaveCategoryButton(
                isLoading = uiState.isSaving,
                isEnabled = canSave(uiState),
                onSaveCategory = {
                    focusManager.clearFocus()
                    onSaveCategory()
                }
            )
        }
    }
}

@Composable
private fun CategoryNameInput(
    categoryName: String,
    onCategoryNameChange: (String) -> Unit,
    hasError: Boolean,
    isEnabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.xs)
    ) {
        Text(
            text = "Category Name",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        BudgeyTextField(
            value = categoryName,
            onValueChange = onCategoryNameChange,
            placeholder = "Enter category name (e.g., Food, Transport)",
            leadingIcon = Icons.Outlined.Info,
            enabled = isEnabled,
            isError = hasError,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Category name input field"
                    if (hasError) {
                        error("Category name has validation errors")
                    }
                }
        )

        // Character count helper
        Text(
            text = "${categoryName.length}/20",
            style = MaterialTheme.typography.bodySmall,
            color = when {
                categoryName.length > 20 -> MaterialTheme.colorScheme.error
                categoryName.length > 15 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun InputErrorMessage(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Card(
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
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgeySpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(24.dp)
                    .semantics {
                        contentDescription = "Dismiss error message"
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SaveCategoryButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onSaveCategory: () -> Unit
) {
    BudgeyPrimaryButton(
        onClick = onSaveCategory,
        text = if (isLoading) "Saving..." else "Save Category",
        icon = if (isLoading) null else Icons.Default.Add,
        enabled = isEnabled && !isLoading,
        loading = isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (isLoading) {
                    "Saving category, please wait"
                } else if (isEnabled) {
                    "Save new category"
                } else {
                    "Save button disabled, check category name"
                }
                role = Role.Button
            }
    )
}

@Composable
private fun MessageSection(
    uiState: AddCategoryUiState,
    onClearError: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
    ) {
        // Success Message
        AnimatedVisibility(
            visible = uiState.isSaved,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300)) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        ) {
            SuccessMessage()
        }

        // Global Error Message (for save operation errors)
        AnimatedVisibility(
            visible = uiState.errorMessage != null && uiState.errorMessage.contains("saving", ignoreCase = true),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            GlobalErrorMessage(
                errorMessage = uiState.errorMessage ?: "",
                onDismiss = onClearError
            )
        }
    }
}

@Composable
private fun SuccessMessage() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF4CAF50).copy(alpha = 0.5f)
                )
            )
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.semantics {
            contentDescription = "Category saved successfully"
            role = Role.Image
        }
    ) {
        Row(
            modifier = Modifier.padding(BudgeySpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "New category is saved",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun GlobalErrorMessage(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Card(
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
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.semantics {
            contentDescription = "Error saving category: $errorMessage"
            role = Role.Image
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgeySpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(BudgeySpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "There was an error in saving: $errorMessage",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    contentDescription = "Dismiss error message"
                    role = Role.Button
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Helper Functions
private fun canSave(uiState: AddCategoryUiState): Boolean {
    return !uiState.isSaving &&
            uiState.categoryName.trim().isNotEmpty() &&
            uiState.categoryName.trim().length >= 2 &&
            uiState.categoryName.trim().length <= 50 &&
            uiState.errorMessage == null
}

// Preview Composables
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Add Category - Empty")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Add Category - Empty Dark")
@Composable
private fun AddCategoryScreenEmptyPreview() {
    BudgeyTheme {
        AddCategoryScreenContent(
            uiState = AddCategoryUiState(),
            onCategoryNameChange = {},
            onSaveCategory = {},
            onClearError = {},
            onClearSavedState = {},
            onResetForm = {},
            onNavigateBack = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Add Category - With Input")
@Composable
private fun AddCategoryScreenWithInputPreview() {
    BudgeyTheme {
        AddCategoryScreenContent(
            uiState = AddCategoryUiState(
                categoryName = "Food & Dining"
            ),
            onCategoryNameChange = {},
            onSaveCategory = {},
            onClearError = {},
            onClearSavedState = {},
            onResetForm = {},
            onNavigateBack = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Add Category - Loading")
@Composable
private fun AddCategoryScreenLoadingPreview() {
    BudgeyTheme {
        AddCategoryScreenContent(
            uiState = AddCategoryUiState(
                categoryName = "Transportation",
                isSaving = true
            ),
            onCategoryNameChange = {},
            onSaveCategory = {},
            onClearError = {},
            onClearSavedState = {},
            onResetForm = {},
            onNavigateBack = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Add Category - Success")
@Composable
private fun AddCategoryScreenSuccessPreview() {
    BudgeyTheme {
        AddCategoryScreenContent(
            uiState = AddCategoryUiState(
                categoryName = "",
                isSaved = true
            ),
            onCategoryNameChange = {},
            onSaveCategory = {},
            onClearError = {},
            onClearSavedState = {},
            onResetForm = {},
            onNavigateBack = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Add Category - Error")
@Composable
private fun AddCategoryScreenErrorPreview() {
    BudgeyTheme {
        AddCategoryScreenContent(
            uiState = AddCategoryUiState(
                categoryName = "Food",
                errorMessage = "A category with this name already exists"
            ),
            onCategoryNameChange = {},
            onSaveCategory = {},
            onClearError = {},
            onClearSavedState = {},
            onResetForm = {},
            onNavigateBack = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Add Category - Save Error")
@Composable
private fun AddCategoryScreenSaveErrorPreview() {
    BudgeyTheme {
        AddCategoryScreenContent(
            uiState = AddCategoryUiState(
                categoryName = "Entertainment",
                errorMessage = "There was an error in saving: Network connection failed"
            ),
            onCategoryNameChange = {},
            onSaveCategory = {},
            onClearError = {},
            onClearSavedState = {},
            onResetForm = {},
            onNavigateBack = {}
        )
    }
}
