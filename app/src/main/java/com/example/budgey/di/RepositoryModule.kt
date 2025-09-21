package com.example.budgey.di

import com.example.budgey.data.repository.AuthRepository
import com.example.budgey.data.repository.AuthRepositoryImpl
import com.example.budgey.data.repository.ExpenseRepository
import com.example.budgey.data.repository.ExpenseRepositoryImpl
import com.example.budgey.data.repository.UserPreferencesRepository
import com.example.budgey.data.repository.UserPreferencesRepositoryImpl
import com.example.budgey.data.repository.UserRepository
import com.example.budgey.data.repository.UserRepositoryImpl
import com.example.budgey.data.repository.CategoryRepository
import com.example.budgey.data.repository.CategoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository
}
