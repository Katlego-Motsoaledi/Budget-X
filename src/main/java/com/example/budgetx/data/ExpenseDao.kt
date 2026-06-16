package com.example.budgetx.data

import androidx.room.*

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expense_table ORDER BY id DESC")
    suspend fun getAllExpenses(): List<Expense>

    @Query("""
        SELECT * FROM expense_table 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY id DESC
    """)
    suspend fun getExpensesByPeriod(
        startDate: String,
        endDate: String
    ): List<Expense>

    @Delete
    suspend fun deleteExpense(expense: Expense)
}