package com.example.budgetx

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetx.data.AppDatabase
import com.example.budgetx.data.Goal
import com.example.budgetx.databinding.ActivityGoalSettingsBinding
import kotlinx.coroutines.launch

class GoalSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveGoals.setOnClickListener {
            // Get inputs and handle invalid entries to prevent crashes
            val minText = binding.etMinGoal.text.toString().trim()
            val maxText = binding.etMaxGoal.text.toString().trim()

            if (minText.isEmpty() || maxText.isEmpty()) {
                Toast.makeText(this, "Please fill in both goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val minVal = minText.toDoubleOrNull() ?: 0.0
            val maxVal = maxText.toDoubleOrNull() ?: 0.0

            // Save to database using a coroutine
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                db.goalDao().setGoals(Goal(minGoal = minVal, maxGoal = maxVal))

                Toast.makeText(this@GoalSettingsActivity, "Goals Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}