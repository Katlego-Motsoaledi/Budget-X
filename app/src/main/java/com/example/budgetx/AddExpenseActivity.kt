package com.example.budgetx

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
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null

    // Launcher for taking the photo
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.ivPhotoPreview.setImageURI(photoUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAttachPhoto.setOnClickListener {
            setupCamera()
        }

        binding.btnSaveExpense.setOnClickListener {
            saveExpenseToDatabase()
        }
    }

    private fun setupCamera() {
        val photoFile: File = File.createTempFile(
            "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}_",
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        currentPhotoPath = photoFile.absolutePath
        photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePhotoLauncher.launch(photoUri)
    }

    private fun saveExpenseToDatabase() {
        val amountStr = binding.etAmount.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val start = binding.etStartTime.text.toString().trim()
        val end = binding.etEndTime.text.toString().trim()
        val cat = binding.etCategory.text.toString().trim()

        if (amountStr.isEmpty() || date.isEmpty() || cat.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0

        val newExpense = Expense(
            amount = amount,
            date = date,
            startTime = start,
            endTime = end,
            description = binding.etDescription.text.toString(),
            category = cat,
            photoPath = currentPhotoPath // Saving the photo location to RoomDB
        )

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.expenseDao().insertExpense(newExpense)
            Toast.makeText(this@AddExpenseActivity, "Saved with Photo!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}