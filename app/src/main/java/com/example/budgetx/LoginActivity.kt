package com.example.budgetx

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetx.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // This variable lets us access the UI elements without errors
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Requirement: Handle invalid inputs without crashing
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish() // This prevents the user from going back to Login when pressing the back button
            }
        }

        binding.tvRegisterLink.setOnClickListener {
            // This will take the user to the Register screen (Step 4)
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}