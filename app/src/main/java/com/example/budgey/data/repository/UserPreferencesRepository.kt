package com.example.budgey.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface UserPreferencesRepository {
    suspend fun saveBudgetStartDate(date: Date)
    fun getBudgetStartDate(): Flow<Date?>
    suspend fun saveBudgetEndDate(date: Date)
    fun getBudgetEndDate(): Flow<Date?>
    suspend fun clearBudgetDates()
}

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : UserPreferencesRepository {

    companion object {
        private const val KEY_BUDGET_START_DATE = "budget_start_date"
        private const val KEY_BUDGET_END_DATE = "budget_end_date"
    }

    override suspend fun saveBudgetStartDate(date: Date) {
        sharedPreferences.edit()
            .putLong(KEY_BUDGET_START_DATE, date.time)
            .apply()
    }

    override fun getBudgetStartDate(): Flow<Date?> = flow {
        val timestamp = sharedPreferences.getLong(KEY_BUDGET_START_DATE, -1L)
        emit(if (timestamp != -1L) Date(timestamp) else null)
    }

    override suspend fun saveBudgetEndDate(date: Date) {
        sharedPreferences.edit()
            .putLong(KEY_BUDGET_END_DATE, date.time)
            .apply()
    }

    override fun getBudgetEndDate(): Flow<Date?> = flow {
        val timestamp = sharedPreferences.getLong(KEY_BUDGET_END_DATE, -1L)
        emit(if (timestamp != -1L) Date(timestamp) else null)
    }

    override suspend fun clearBudgetDates() {
        sharedPreferences.edit()
            .remove(KEY_BUDGET_START_DATE)
            .remove(KEY_BUDGET_END_DATE)
            .apply()
    }
}
