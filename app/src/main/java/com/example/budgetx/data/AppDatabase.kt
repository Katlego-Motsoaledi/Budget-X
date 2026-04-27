package com.example.budgetx.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Version bumped to 2 because we added a new table
@Database(entities = [Expense::class, Goal::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao // Added for goals

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_x_database"
                )
                    .fallbackToDestructiveMigration() // This prevents crashes when changing database structure
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}