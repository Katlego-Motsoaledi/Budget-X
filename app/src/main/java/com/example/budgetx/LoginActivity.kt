package com.example.budgetx

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetx.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {

            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {

                username.isEmpty() -> {
                    binding.etUsername.error = "Username is required"
                    binding.etUsername.requestFocus()
                }

                password.isEmpty() -> {
                    binding.etPassword.error = "Password is required"
                    binding.etPassword.requestFocus()
                }

                username.length < 3 -> {
                    binding.etUsername.error = "Username must be at least 3 characters"
                    binding.etUsername.requestFocus()
                }

                password.length < 4 -> {
                    binding.etPassword.error = "Password must be at least 4 characters"
                    binding.etPassword.requestFocus()
                }

                else -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}