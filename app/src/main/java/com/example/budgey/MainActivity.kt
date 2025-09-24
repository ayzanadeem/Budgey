package com.example.budgey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgey.navigation.BudgeyApp
import com.example.budgey.navigation.BudgeyDestination
import com.example.budgey.ui.theme.BudgeyTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Check if user is already logged in
            val isLoggedIn = firebaseAuth.currentUser != null
            val startDestination = if (isLoggedIn) {
                BudgeyDestination.NewEntry.route
            } else {
                BudgeyDestination.Login.route
            }

            BudgeyApp(startDestination = startDestination)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgeyAppPreview() {
    BudgeyTheme {
        BudgeyApp()
    }
}