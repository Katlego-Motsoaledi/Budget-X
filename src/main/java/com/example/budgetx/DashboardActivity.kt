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

            val expenses = withContext(Dispatchers.IO) {
                db.expenseDao().getAllExpenses()
            }

            val totalSpent = expenses.sumOf { it.amount }

            binding.tvTotalSpent.text = "Spent: R %.2f".format(totalSpent)

            val goal = withContext(Dispatchers.IO) {
                db.goalDao().getGoals()
            }

            val budgetGoal = goal?.maxGoal ?: 0.0
            val minGoal = goal?.minGoal ?: 0.0

            if (budgetGoal <= 0) {
                showEmptyState()
                return@launch
            }

            val savings = budgetGoal - totalSpent

            binding.tvSavings.text = "Savings: R %.2f".format(savings)

            binding.pbBudget.progress =
                ((totalSpent / budgetGoal) * 100).toInt().coerceIn(0, 100)

            binding.tvWarning.text = when {
                totalSpent >= budgetGoal -> "⚠ Budget exceeded!"
                totalSpent >= budgetGoal * 0.8 -> "⚠ Close to limit"
                totalSpent >= minGoal -> "👍 Above minimum goal"
                else -> "🔥 Great control"
            }

            val days = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

            binding.tvDailyLimit.text =
                "Daily Limit: R %.2f".format(budgetGoal / days)

            val streak = calculateStreak(expenses, budgetGoal)

            binding.tvStreak.text = "🔥 $streak"

            binding.tvRating.text = when {
                streak >= 12 -> "Platinum"
                streak >= 6 -> "Gold"
                streak >= 3 -> "Silver"
                else -> "Bronze"
            }

            runCatching {
                val chartData = expenses
                    .groupBy { it.category }
                    .mapValues { it.value.sumOf { e -> e.amount }.toFloat() }

                binding.dashboardChart.setData(chartData)
            }
        }
    }

    private fun calculateStreak(
        expenses: List<com.example.budgetx.data.Expense>,
        budget: Double
    ): Int {

        val calendar = Calendar.getInstance()
        var streak = 0

        for (i in 0 until 12) {

            val month = calendar.get(Calendar.MONTH) - i
            val year = calendar.get(Calendar.YEAR)

            val monthlyTotal = expenses.filter {

                val parts = it.date.split("-")
                if (parts.size < 3) return@filter false

                val y = parts[0].toIntOrNull() ?: return@filter false
                val m = parts[1].toIntOrNull() ?: return@filter false

                y == year && m == month + 1
            }.sumOf { it.amount }

            if (monthlyTotal <= budget) streak++
            else break
        }

        return streak
    }

    private fun resetStreak() {
        binding.tvStreak.text = "🔥 0"
        binding.tvRating.text = "Bronze"
        binding.tvWarning.text = "Streak reset"
    }

    private fun showEmptyState() {
        binding.tvTotalSpent.text = "Spent: R 0.00"
        binding.tvSavings.text = "Savings: R 0.00"
        binding.tvWarning.text = "No budget set"
        binding.tvDailyLimit.text = "Daily Limit: R 0.00"
        binding.pbBudget.progress = 0
    }
}