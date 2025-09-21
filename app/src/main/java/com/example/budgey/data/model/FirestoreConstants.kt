package com.example.budgey.data.model

/**
 * Firestore collection references and constants
 * Centralized place for all Firestore collection names and field names
 */
object FirestoreCollections {
    const val USERS = "users"
    const val CATEGORIES = "categories"
    const val EXPENSES = "expenses"

    // Subcollections
    const val USER_CATEGORIES = "user_categories"
    const val USER_EXPENSES = "user_expenses"
}

object FirestoreFields {
    // Common fields
    const val CREATED_AT = "created_at"
    const val USER_ID = "user_id"

    // User fields
    const val EMAIL = "email"
    const val LAST_LOGIN = "last_login"
    const val DISPLAY_NAME = "display_name"
    const val PROFILE_IMAGE_URL = "profile_image_url"

    // Category fields
    const val NAME = "name"
    const val IS_ACTIVE = "is_active"
    const val ICON = "icon"
    const val COLOR = "color"
    const val DESCRIPTION = "description"

    // Expense fields
    const val CATEGORY_ID = "category_id"
    const val CATEGORY_NAME = "category_name"
    const val AMOUNT = "amount"
    const val BUDGET_START_DATE = "budget_start_date"
    const val BUDGET_END_DATE = "budget_end_date"
    const val BUDGET_MONTH_KEY = "budget_month_key"
    const val TYPE = "type"
    const val CURRENCY = "currency"
    const val PAYMENT_METHOD = "payment_method"
    const val RECEIPT_URL = "receipt_url"
    const val TAGS = "tags"
}
