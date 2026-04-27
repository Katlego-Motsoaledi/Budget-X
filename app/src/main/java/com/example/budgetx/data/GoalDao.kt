package com.example.budgetx.data

import androidx.room.*

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGoals(goal: Goal)

    @Query("SELECT * FROM goal_table WHERE id = 1")
    suspend fun getGoals(): Goal?
}