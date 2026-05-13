package com.simats.uroapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val btnContinue = findViewById<Button>(R.id.btn_continue)
        val tvBackToLogin = findViewById<TextView>(R.id.tv_back_to_login)

        tvBackToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnContinue.setOnClickListener {
            val email = findViewById<android.widget.EditText>(R.id.et_forgot_email).text.toString().trim()
            if (email.isEmpty()) {
                android.widget.Toast.makeText(this, "Please enter your email", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("EMAIL", email)
            startActivity(intent)
            finish()
        }
    }
}
