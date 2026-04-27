package com.example.budgetx

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetx.data.AppDatabase
import com.example.budgetx.data.Expense
import com.example.budgetx.databinding.ActivityAddExpenseBinding
import kotlinx.coroutines.launch

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveExpense.setOnClickListener {
            saveExpenseToDatabase()
        }
    }

    private fun saveExpenseToDatabase() {
        val amountStr = binding.etAmount.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val start = binding.etStartTime.text.toString().trim()
        val end = binding.etEndTime.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val cat = binding.etCategory.text.toString().trim()

        // Requirement: Handle invalid inputs made by user without crashing
        if (amountStr.isEmpty() || date.isEmpty() || start.isEmpty() || end.isEmpty() || cat.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Please enter a valid number for amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Creating the Expense object
        val newExpense = Expense(
            amount = amount,
            date = date,
            startTime = start,
            endTime = end,
            description = desc,
            category = cat,
            photoPath = null // Placeholder for optional photo
        )

        // Using Coroutines to save to RoomDB in the background
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.expenseDao().insertExpense(newExpense)

            Toast.makeText(this@AddExpenseActivity, "Expense Saved Successfully!", Toast.LENGTH_SHORT).show()
            finish() // Close the screen and go back
        }
    }
}