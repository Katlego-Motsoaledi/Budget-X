package com.example.budgetx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetx.data.AppDatabase
import com.example.budgetx.databinding.ActivityHistoryBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: ExpenseAdapter
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ExpenseAdapter(emptyList())
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        binding.btnFilterDate.setOnClickListener {
            showDateRangePicker()
        }

        // Load all initially
        loadFilteredData("1900-01-01", "2100-12-31")
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Period")
            .build()

        picker.show(supportFragmentManager, "date_range_picker")

        picker.addOnPositiveButtonClickListener { range ->
            val startDate = sdf.format(Date(range.first))
            val endDate = sdf.format(Date(range.second))
            loadFilteredData(startDate, endDate)
        }
    }

    private fun loadFilteredData(start: String, end: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)

            // Requirement: Fetch list for period
            val expenses = withContext(Dispatchers.IO) {
                db.expenseDao().getExpensesByPeriod(start, end)
            }
            adapter.updateData(expenses)

            // Requirement: Calculate category totals
            updateCategorySummary(expenses)
        }
    }

    private fun updateCategorySummary(expenses: List<com.example.budgetx.data.Expense>) {
        if (expenses.isEmpty()) {
            binding.tvCategorySummary.text = "No expenses found for this period."
            return
        }

        val summaryMap = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val summaryText = StringBuilder("Totals per Category:\n")
        summaryMap.forEach { (category, total) ->
            summaryText.append("- $category: R ${String.format("%.2f", total)}\n")
        }
        binding.tvCategorySummary.text = summaryText.toString()
    }
}