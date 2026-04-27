package com.example.budgetx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetx.data.Expense
import com.example.budgetx.databinding.ItemExpenseBinding
import java.io.File
import android.net.Uri

class ExpenseAdapter(private var expenses: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.binding.apply {
            // Existing text logic
            tvItemCategory.text = expense.category
            tvItemDate.text = "${expense.date} | ${expense.startTime}"
            tvItemAmount.text = "R ${String.format("%.2f", expense.amount)}"

            // NEW PHOTO LOGIC STARTS HERE
            if (expense.photoPath != null) {
                // If a photo exists, make the image visible and load it
                ivItemPhoto.visibility = View.VISIBLE
                ivItemPhoto.setImageURI(Uri.fromFile(File(expense.photoPath)))
            } else {
                // If no photo was taken, hide the image completely so it doesn't leave a gap
                ivItemPhoto.visibility = View.GONE
            }
            // NEW PHOTO LOGIC ENDS HERE
        }
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}