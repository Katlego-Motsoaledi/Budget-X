package com.example.budgetx

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetx.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val user = binding.etRegUsername.text.toString().trim()
            val pass = binding.etRegPassword.text.toString().trim()
            val confirm = binding.etRegConfirmPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (pass != confirm) {
                // Requirement: Handle invalid inputs
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                finish() // Returns user to Login screen
            }
        }
    }
}