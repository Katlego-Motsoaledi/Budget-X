package com.example.budgetx.data

import androidx.room.*

@Dao
interface ExpenseDao {

    // Requirement: Add an expense entry
    @Insert
    suspend fun insertExpense(expense: Expense)

    // Requirement: View a list of all expenses during a selectable period
    @Query("SELECT * FROM expense_table WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByPeriod(startDate: String, endDate: String): List<Expense>

    // Requirement: View the total amount spent on each category
    @Query("SELECT SUM(amount) FROM expense_table WHERE category = :categoryName AND date BETWEEN :startDate AND :endDate")
    fun getTotalAmountForCategory(categoryName: String, startDate: String, endDate: String): Double

    // Bonus: A command to delete an entry if the user makes a mistake
    @Delete
    suspend fun deleteExpense(expense: Expense)
}