package com.example.budgetx

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
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

            // Current Month
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            val expenses = withContext(Dispatchers.IO) {
                db.expenseDao().getExpensesByPeriod(startDate, endDate)
            }

            val totalSpent = expenses.sumOf { it.amount }
            binding.tvTotalSpent.text = "R ${String.format("%.2f", totalSpent)}"

            val goals = withContext(Dispatchers.IO) {
                db.goalDao().getGoals()
            }

            if (goals != null && goals.maxGoal > 0) {
                val progress = ((totalSpent / goals.maxGoal) * 100).toInt()
                binding.pbBudget.progress = progress.coerceAtMost(100)
            }

            val categoryData = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { exp -> exp.amount }.toFloat() }

            binding.dashboardChart.setData(categoryData)
        }
    }
}

class SimpleBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var data: Map<String, Float> = emptyMap()

    private val barPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 28f
        isAntiAlias = true
    }

    fun setData(newData: Map<String, Float>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val barWidth = width / (data.size * 2f)
        val maxValue = data.values.maxOrNull() ?: 1f

        var x = barWidth

        data.forEach { (label, value) ->
            val barHeight = (value / maxValue) * (height * 0.7f)

            val left = x
            val top = height - barHeight
            val right = x + barWidth
            val bottom = height.toFloat()

            canvas.drawRect(left, top, right, bottom, barPaint)
            canvas.drawText(label, left, top - 10, textPaint)

            x += barWidth * 2
        }
    }
}
