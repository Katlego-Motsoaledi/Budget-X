package com.example.budgetx.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_table")
data class Goal(
    @PrimaryKey val id: Int = 1, // We only ever need one row for the current goals
    val minGoal: Double,
    val maxGoal: Double
)