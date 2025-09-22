package com.example.budgey.presentation.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgey.R
import com.example.budgey.presentation.ui.state.LoginUiState
import com.example.budgey.presentation.viewmodel.LoginViewModel
import com.example.budgey.ui.theme.*
import com.example.budgey.ui.theme.components.*

@Composable
fun LoginScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle successful login navigation
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToMain()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = { email ->
            // Clear email error when user starts typing
            if (uiState.emailError != null) {
                viewModel.clearErrors()
            }
        },
        onPasswordChange = { password ->
            // Clear password error when user starts typing
            if (uiState.passwordError != null) {
                viewModel.clearErrors()
            }
        },
        onLogin = { email, password ->
            viewModel.login(email, password)
        },
        modifier = modifier
    )
}

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf(uiState.email) }
    var password by remember { mutableStateOf(uiState.password) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    // Update local state when UI state changes
    LaunchedEffect(uiState.email) {
        email = uiState.email
    }
    LaunchedEffect(uiState.password) {
        password = uiState.password
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
                .padding(BudgeySpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo and Welcome Text
            WelcomeSection()

            Spacer(modifier = Modifier.height(BudgeySpacing.xxl))

            // Login Form
            LoginForm(
                email = email,
                password = password,
                isPasswordVisible = isPasswordVisible,
                emailError = uiState.emailError,
                passwordError = uiState.passwordError,
                isLoading = uiState.isLoading,
                onEmailChange = { newEmail ->
                    email = newEmail
                    onEmailChange(newEmail)
                },
                onPasswordChange = { newPassword ->
                    password = newPassword
                    onPasswordChange(newPassword)
                },
                onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                onLogin = { onLogin(email, password) },
                emailFocusRequester = emailFocusRequester,
                passwordFocusRequester = passwordFocusRequester,
                focusManager = focusManager,
                keyboardController = keyboardController
            )

            Spacer(modifier = Modifier.height(BudgeySpacing.lg))

            // Error Message
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(BudgeySpacing.md),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Loading Overlay
        if (uiState.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun WelcomeSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
    ) {
        // App Logo/Icon placeholder
        Card(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Budgey Logo",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            text = "Welcome to Budgey",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Track your expenses with ease",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoginForm(
    email: String,
    password: String,
    isPasswordVisible: Boolean,
    emailError: String?,
    passwordError: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onLogin: () -> Unit,
    emailFocusRequester: FocusRequester,
    passwordFocusRequester: FocusRequester,
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    val hidePassIcon = ImageVector.vectorResource(R.drawable.visibility_off)
    val showPassIcon = ImageVector.vectorResource(R.drawable.visibility_on)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgeySpacing.xl),
            verticalArrangement = Arrangement.spacedBy(BudgeySpacing.lg)
        ) {
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Email Field
            BudgeyTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester),
                label = "Email",
                placeholder = "Enter your email",
                leadingIcon = Icons.Default.Email,
                isError = emailError != null,
                errorMessage = emailError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        passwordFocusRequester.requestFocus()
                    }
                ),
                singleLine = true
            )

            // Password Field
            BudgeyTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                label = "Password",
                placeholder = "Enter your password",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (isPasswordVisible) hidePassIcon else showPassIcon,
                onTrailingIconClick = onPasswordVisibilityToggle,
                isError = passwordError != null,
                errorMessage = passwordError,
                enabled = !isLoading,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onLogin()
                        }
                    }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(BudgeySpacing.md))

            // Login Button
            BudgeyPrimaryButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onLogin()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                loading = isLoading,
                text = if (isLoading) "Signing In..." else "Sign In",
                icon = if (!isLoading) Icons.Default.ArrowForward else null
            )
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(BudgeySpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(BudgeySpacing.md)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Signing you in...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Preview Composables
@Preview(showBackground = true, name = "Login Screen - Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Login Screen - Dark")
@Composable
private fun LoginScreenPreview() {
    BudgeyTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLogin = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Login Screen - Loading")
@Composable
private fun LoginScreenLoadingPreview() {
    BudgeyTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                isLoading = true,
                email = "user@example.com",
                password = "password"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLogin = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Login Screen - Error")
@Composable
private fun LoginScreenErrorPreview() {
    BudgeyTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "invalid-email",
                password = "123",
                emailError = "Please enter a valid email address",
                passwordError = "Password must be at least 4 characters long",
                errorMessage = "Invalid email or password. Please try again."
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onLogin = { _, _ -> }
        )
    }
}
