package com.example.budgetx

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetx.data.AppDatabase
import com.example.budgetx.databinding.ActivityDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)

            // ===== CURRENT MONTH =====
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.time)

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.time)

            val expenses = withContext(Dispatchers.IO) {
                db.expenseDao().getExpensesByPeriod(startDate, endDate)
            }

            val totalSpent = expenses.sumOf { it.amount }
            binding.tvTotalSpent.text = "R ${"%.2f".format(totalSpent)}"

            // ===== GOALS =====
            val goals = withContext(Dispatchers.IO) {
                db.goalDao().getGoals()
            }

            if (goals != null && goals.maxGoal > 0) {

                val progress = ((totalSpent / goals.maxGoal) * 100)
                    .toInt()
                    .coerceAtMost(100)

                binding.pbBudget.progress = progress
            }

            // ===== STREAK CALCULATION =====
            val streak = calculateUnder5000Streak(db)

            // ===== GAMIFICATION ADDED HERE =====
            val level = getStreakLevel(streak)
            val badge = getBadge(streak)
            val fire = getFire(streak)

            binding.tvStreak.text = "$fire $streak • $level"
            binding.tvRating.text = badge

            // Optional: savings message becomes more motivating
            val savings = goals?.maxGoal?.minus(totalSpent) ?: 0.0
            binding.tvSavings.text = "Savings: R ${"%.2f".format(savings)}"

            // ===== CHART =====
            val categoryData = expenses.groupBy { it.category }
                .mapValues { entry ->
                    entry.value.sumOf { it.amount }.toFloat()
                }

            binding.dashboardChart.setData(categoryData)
        }
    }

    // ===== ORIGINAL STREAK LOGIC (UNCHANGED) =====
    private suspend fun calculateUnder5000Streak(db: AppDatabase): Int {
        var streak = 0
        val calendar = Calendar.getInstance()

        withContext(Dispatchers.IO) {
            for (i in 0 until 12) {
                val startCal = calendar.clone() as Calendar
                startCal.add(Calendar.MONTH, -i)
                startCal.set(Calendar.DAY_OF_MONTH, 1)

                val endCal = startCal.clone() as Calendar
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))

                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(startCal.time)

                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(endCal.time)

                val monthExpenses = db.expenseDao().getExpensesByPeriod(startDate, endDate)
                val total = monthExpenses.sumOf { it.amount }

                if (total <= 5000) {
                    streak++
                } else {
                    break
                }
            }
        }

        return streak
    }

    // ===== GAMIFICATION SYSTEM =====

    private fun getStreakLevel(streak: Int): String {
        return when {
            streak >= 12 -> "Financial Master"
            streak >= 6 -> "Money Warrior"
            streak >= 3 -> "Smart Saver"
            else -> "Beginner"
        }
    }

    private fun getBadge(streak: Int): String {
        return when {
            streak >= 12 -> "🏆 Platinum Saver Badge"
            streak >= 6 -> "🥇 Gold Saver Badge"
            streak >= 3 -> "🥈 Silver Saver Badge"
            else -> "🥉 Starter Badge"
        }
    }

    private fun getFire(streak: Int): String {
        return when {
            streak >= 12 -> "🔥🔥🔥🔥🔥"
            streak >= 6 -> "🔥🔥🔥🔥"
            streak >= 3 -> "🔥🔥🔥"
            streak >= 1 -> "🔥🔥"
            else -> "🔥"
        }
    }
}