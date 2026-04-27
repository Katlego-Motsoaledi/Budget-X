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

        // Add a way to get to the Goal Settings screen
        binding.tvTotalSpent.setOnClickListener {
            startActivity(Intent(this, GoalSettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboardData() // Refresh data every time you return to this screen
    }

    private fun updateDashboardData() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)

            // 1. Get current goals
            val goals = withContext(Dispatchers.IO) { db.goalDao().getGoals() }

            // 2. Get total spent
            val expenses = withContext(Dispatchers.IO) {
                db.expenseDao().getExpensesByPeriod("1900-01-01", "2100-12-31")
            }
            val total = expenses.sumOf { it.amount }

            // 3. Update UI
            binding.tvTotalSpent.text = "R ${String.format("%.2f", total)}"

            if (goals != null && goals.maxGoal > 0) {
                val progress = (total / goals.maxGoal * 100).toInt()
                binding.pbBudget.progress = if (progress > 100) 100 else progress

                // Change color to Red if overspending
                if (total > goals.maxGoal) {
                    binding.tvTotalSpent.setTextColor(android.graphics.Color.RED)
                } else {
                    binding.tvTotalSpent.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                }
            }
        }
    }
}