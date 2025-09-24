package com.example.budgey.ui.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.budgey.ui.theme.*

@Composable
fun BudgeyPagination(
    currentPage: Int,
    hasNextPage: Boolean,
    hasPreviousPage: Boolean,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BudgeyRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgeySpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            BudgeySecondaryButton(
                onClick = onPreviousPage,
                modifier = Modifier.weight(1f),
                enabled = hasPreviousPage && !isLoading,
                text = "Previous",
                icon = Icons.Default.KeyboardArrowLeft
            )

            // Page indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = BudgeySpacing.md)
            ) {
                Text(
                    text = "Page",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currentPage.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Next button
            BudgeyPrimaryButton(
                onClick = onNextPage,
                modifier = Modifier.weight(1f),
                enabled = hasNextPage && !isLoading,
                text = "Next",
                icon = Icons.Default.KeyboardArrowRight
            )
        }

        // Loading indicator
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BudgeyPaginationPreview() {
    BudgeyTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // First page
            BudgeyPagination(
                currentPage = 1,
                hasNextPage = true,
                hasPreviousPage = false,
                onNextPage = {},
                onPreviousPage = {}
            )

            // Middle page
            BudgeyPagination(
                currentPage = 5,
                hasNextPage = true,
                hasPreviousPage = true,
                onNextPage = {},
                onPreviousPage = {}
            )

            // Last page
            BudgeyPagination(
                currentPage = 10,
                hasNextPage = false,
                hasPreviousPage = true,
                onNextPage = {},
                onPreviousPage = {}
            )

            // Loading state
            BudgeyPagination(
                currentPage = 3,
                hasNextPage = true,
                hasPreviousPage = true,
                onNextPage = {},
                onPreviousPage = {},
                isLoading = true
            )
        }
    }
}
