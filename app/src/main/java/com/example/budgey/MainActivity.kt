package com.example.budgey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgey.navigation.BudgeyApp
import com.example.budgey.ui.theme.BudgeyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgeyApp()
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

//todo:
// fix focus automatic capture when screen open
// add pagination to expenses list
// change budget month key logic + change how header shows month