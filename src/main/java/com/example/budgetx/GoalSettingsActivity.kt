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

            val minText = binding.etMinGoal.text.toString().trim()
            val maxText = binding.etMaxGoal.text.toString().trim()

            if (minText.isEmpty() || maxText.isEmpty()) {
                Toast.makeText(this, "Please fill in both goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val minVal = minText.toDoubleOrNull()
            val maxVal = maxText.toDoubleOrNull()

            if (minVal == null || maxVal == null) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minVal < 0 || maxVal < 0) {
                Toast.makeText(this, "Goals cannot be negative", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minVal > maxVal) {
                Toast.makeText(this, "Min goal cannot be greater than max goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.goalDao().setGoals(Goal(minGoal = minVal, maxGoal = maxVal))

                    Toast.makeText(this@GoalSettingsActivity, "Goals Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@GoalSettingsActivity, "Error saving goals", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}