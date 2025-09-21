package com.example.budgey.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * User data class for Firestore integration
 * Represents user account information in the Budgey app
 */
data class User(
    @PropertyName("email")
    val email: String = "",

    @PropertyName("created_at")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @PropertyName("last_login")
    val lastLogin: Timestamp? = null,

    @PropertyName("display_name")
    val displayName: String? = null,

    @PropertyName("profile_image_url")
    val profileImageUrl: String? = null
) {
    // No-argument constructor required by Firestore
    constructor() : this("", null, null, null, null)
}

/**
 * Extension functions for User data class
 */
fun User.toMap(): Map<String, Any?> {
    return mapOf(
        "email" to email,
        "created_at" to createdAt,
        "last_login" to lastLogin,
        "display_name" to displayName,
        "profile_image_url" to profileImageUrl
    )
}

fun DocumentSnapshot.toUser(): User? {
    return try {
        User(
            email = getString("email") ?: "",
            createdAt = getTimestamp("created_at"),
            lastLogin = getTimestamp("last_login"),
            displayName = getString("display_name"),
            profileImageUrl = getString("profile_image_url")
        )
    } catch (e: Exception) {
        null
    }
}

fun User.withLastLogin(timestamp: Timestamp = Timestamp.now()): User {
    return this.copy(lastLogin = timestamp)
}

fun User.isNewUser(): Boolean {
    val now = Date()
    val createdDate = createdAt?.toDate()
    return createdDate?.let {
        (now.time - it.time) < 24 * 60 * 60 * 1000 // Less than 24 hours
    } ?: false
}
