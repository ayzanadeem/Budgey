package com.example.budgey.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    /**
     * Logs out the current user and navigates to login screen
     */
    fun logout(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                onNavigateToLogin()
            } catch (_: Exception) {
                // Handle logout error if needed
                // For now, just navigate to login anyway
                onNavigateToLogin()
            }
        }
    }
}
