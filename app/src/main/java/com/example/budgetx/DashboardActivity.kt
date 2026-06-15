package com.example.budgetx

import android.content.Intent
import android.graphics.Color
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

        binding.btnSetGoal.setOnClickListener {
            startActivity(Intent(this, GoalSettingsActivity::class.java))
        }

        // NEW FEATURE BUTTON
        binding.btnResetStreak.setOnClickListener {
            resetStreak()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(applicationContext)

            // ===== MONTH RANGE =====
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

            // ===== GOAL =====
            val goal = withContext(Dispatchers.IO) {
                db.goalDao().getGoals()
            }

            val budgetGoal = goal?.maxGoal ?: 0.0

            if (budgetGoal <= 0) {
                showEmptyState()
                return@launch
            }

            // ===== PROGRESS =====
            val progress = ((totalSpent / budgetGoal) * 100)
                .toInt()
                .coerceAtMost(100)

            binding.pbBudget.progress = progress

            // ===== STREAK =====
            val streak = calculateStreak(db, budgetGoal)

            binding.tvStreak.text = "${getFire(streak)} $streak • ${getLevel(streak)}"
            binding.tvRating.text = getBadge(streak)

            val savings = budgetGoal - totalSpent
            binding.tvSavings.text = "Savings: R ${"%.2f".format(savings)}"

            // ===== WARNING SYSTEM =====
            val usage = (totalSpent / budgetGoal) * 100

            binding.tvWarning.text = when {
                usage >= 100 -> "⚠ Budget exceeded!"
                usage >= 80 -> "⚠ Close to limit"
                usage >= 50 -> "👍 On track"
                else -> "🔥 Great control"
            }

            binding.tvTotalSpent.setTextColor(
                when {
                    usage >= 100 -> Color.parseColor("#EF4444")
                    usage >= 80 -> Color.parseColor("#F59E0B")
                    else -> Color.parseColor("#22C55E")
                }
            )

            // ===== DAILY LIMIT =====
            val days = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val daily = budgetGoal / days

            binding.tvDailyLimit.text = "Daily Limit: R ${"%.2f".format(daily)}"

            // ===== CHART =====
            val chartData = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { e -> e.amount }.toFloat() }

            binding.dashboardChart.setData(chartData)
        }
    }

    // ================= RESET FEATURE =================
    private fun resetStreak() {
        binding.tvStreak.text = "🔥 Streak reset"
        binding.tvRating.text = "🥉 Starter Badge"
        binding.tvWarning.text = "Start rebuilding your streak 💪"
    }

    // ================= STREAK =================
    private suspend fun calculateStreak(db: AppDatabase, budgetGoal: Double): Int {

        var streak = 0
        val calendar = Calendar.getInstance()

        withContext(Dispatchers.IO) {

            for (i in 0 until 12) {

                val start = calendar.clone() as Calendar
                start.add(Calendar.MONTH, -i)
                start.set(Calendar.DAY_OF_MONTH, 1)

                val end = start.clone() as Calendar
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))

                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(start.time)

                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(end.time)

                val month = db.expenseDao().getExpensesByPeriod(startDate, endDate)
                val total = month.sumOf { it.amount }

                if (total <= budgetGoal) streak++
                else break
            }
        }

        return streak
    }

    // ================= GAMIFICATION =================
    private fun getLevel(streak: Int): String {
        return when {
            streak >= 12 -> "Master"
            streak >= 6 -> "Pro Saver"
            streak >= 3 -> "Smart Saver"
            else -> "Beginner"
        }
    }

    private fun getBadge(streak: Int): String {
        return when {
            streak >= 12 -> "🏆 Platinum"
            streak >= 6 -> "🥇 Gold"
            streak >= 3 -> "🥈 Silver"
            else -> "🥉 Bronze"
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

    private fun showEmptyState() {
        binding.tvStreak.text = "Set a budget goal to start 🔥"
        binding.tvRating.text = "-"
        binding.tvSavings.text = "Savings: R 0.00"
        binding.tvWarning.text = "No budget set"
        binding.tvDailyLimit.text = "Daily Limit: R 0.00"
        binding.pbBudget.progress = 0
    }
}