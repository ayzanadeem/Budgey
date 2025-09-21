package com.example.budgey.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Category data class for Firestore integration
 * Represents expense categories in the Budgey app
 */
data class Category(
    @PropertyName("name")
    val name: String = "",

    @PropertyName("user_id")
    val userId: String = "",

    @PropertyName("created_at")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @PropertyName("is_active")
    val isActive: Boolean = true,

    @PropertyName("icon")
    val icon: String? = null,

    @PropertyName("color")
    val color: String? = null,

    @PropertyName("description")
    val description: String? = null
) {
    // No-argument constructor required by Firestore
    constructor() : this("", "", null, true, null, null, null)
}

/**
 * Extension functions for Category data class
 */
fun Category.toMap(): Map<String, Any?> {
    return mapOf(
        "name" to name,
        "user_id" to userId,
        "created_at" to createdAt,
        "is_active" to isActive,
        "icon" to icon,
        "color" to color,
        "description" to description
    )
}

fun DocumentSnapshot.toCategory(): Category? {
    return try {
        Category(
            name = getString("name") ?: "",
            userId = getString("user_id") ?: "",
            createdAt = getTimestamp("created_at"),
            isActive = getBoolean("is_active") ?: true,
            icon = getString("icon"),
            color = getString("color"),
            description = getString("description")
        )
    } catch (e: Exception) {
        null
    }
}

fun Category.deactivate(): Category {
    return this.copy(isActive = false)
}

fun Category.activate(): Category {
    return this.copy(isActive = true)
}

fun Category.updateName(newName: String): Category {
    return this.copy(name = newName)
}

fun Category.withIcon(iconName: String): Category {
    return this.copy(icon = iconName)
}

fun Category.withColor(colorHex: String): Category {
    return this.copy(color = colorHex)
}
