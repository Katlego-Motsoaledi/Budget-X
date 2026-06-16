package com.example.budgetx

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.budgetx.data.AppDatabase
import com.example.budgetx.data.Expense
import com.example.budgetx.databinding.ActivityAddExpenseBinding
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding

    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->

            if (success && photoUri != null) {

                binding.ivPhotoPreview.visibility = View.VISIBLE

                try {
                    binding.ivPhotoPreview.setImageURI(photoUri)
                } catch (e: Exception) {
                    Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Photo not captured", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()
        setupTimePickers()

        binding.btnAttachPhoto.setOnClickListener {
            setupCamera()
        }

        binding.btnSaveExpense.setOnClickListener {
            saveExpenseToDatabase()
        }
    }

    private fun setupDatePicker() {

        binding.etDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->

                    val selectedDate =
                        String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

                    binding.etDate.setText(selectedDate)

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePickers() {

        binding.etStartTime.setOnClickListener {

            val calendar = Calendar.getInstance()

            TimePickerDialog(
                this,
                { _, hour, minute ->

                    binding.etStartTime.setText(
                        String.format("%02d:%02d", hour, minute)
                    )

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        binding.etEndTime.setOnClickListener {

            val calendar = Calendar.getInstance()

            TimePickerDialog(
                this,
                { _, hour, minute ->

                    binding.etEndTime.setText(
                        String.format("%02d:%02d", hour, minute)
                    )

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupCamera() {

        try {

            val photoFile = File.createTempFile(
                "JPEG_${System.currentTimeMillis()}_",
                ".jpg",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            currentPhotoPath = photoFile.absolutePath

            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )

            takePhotoLauncher.launch(photoUri!!)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Camera failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveExpenseToDatabase() {

        val amountStr = binding.etAmount.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val start = binding.etStartTime.text.toString().trim()
        val end = binding.etEndTime.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (amountStr.isEmpty() ||
            date.isEmpty() ||
            category.isEmpty()
        ) {

            Toast.makeText(
                this,
                "Please fill all required fields",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val amount = amountStr.toDoubleOrNull()

        if (amount == null || amount <= 0) {

            Toast.makeText(
                this,
                "Enter a valid amount",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val expense = Expense(
            amount = amount,
            date = date,
            startTime = start,
            endTime = end,
            description = description,
            category = category,
            photoPath = currentPhotoPath
        )

        lifecycleScope.launch {

            val db = AppDatabase.getDatabase(applicationContext)

            db.expenseDao().insertExpense(expense)

            Toast.makeText(
                this@AddExpenseActivity,
                "Expense saved successfully",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}