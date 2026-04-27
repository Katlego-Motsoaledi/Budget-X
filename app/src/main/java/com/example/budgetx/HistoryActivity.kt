package com.example.budgetx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetx.data.AppDatabase
import com.example.budgetx.databinding.ActivityHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ExpenseAdapter(emptyList())
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        loadExpenses()
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            // Fetching all data. In the next step, we can add date filtering
            val expenses = withContext(Dispatchers.IO) {
                // For a prototype, we fetch all. You can use DAO period query later.
                db.expenseDao().getExpensesByPeriod("1900-01-01", "2100-12-31")
            }
            adapter.updateData(expenses)
        }
    }
}