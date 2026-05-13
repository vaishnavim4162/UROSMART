package com.simats.uroapp

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.uroapp.network.LoginRequest
import com.simats.uroapp.network.LoginResponse
import com.simats.uroapp.network.RetrofitClient

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etPassword = findViewById<EditText>(R.id.et_password)
        val ivPasswordToggle = findViewById<ImageView>(R.id.iv_password_toggle)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        ivPasswordToggle.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
                isPasswordVisible = false
            } else {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                // You might want a different icon for "visibility off" but using same for now
                ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
                isPasswordVisible = true
            }
            etPassword.setSelection(etPassword.text.length)
        }

        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.et_email).text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(this, "Please enter email and password", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)
            RetrofitClient.instance.login(request).enqueue(object : retrofit2.Callback<LoginResponse> {
                override fun onResponse(call: retrofit2.Call<LoginResponse>, response: retrofit2.Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val email = loginResponse?.user?.email ?: ""
                        val token = loginResponse?.access_token ?: ""
                        
                        // Save user data
                        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("EMAIL", email)
                            putString("TOKEN", token)
                            apply()
                        }

                        android.widget.Toast.makeText(this@LoginActivity, "Login successful", android.widget.Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            com.google.gson.Gson().fromJson(errorBody, LoginResponse::class.java).error ?: "Error: ${response.code()}"
                        } catch (e: Exception) {
                            errorBody ?: "Login failed (${response.code()})"
                        }
                        android.widget.Toast.makeText(this@LoginActivity, errorMessage, android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                    android.widget.Toast.makeText(this@LoginActivity, "Network error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
        }

        val tvSignup = findViewById<TextView>(R.id.tv_signup)
        tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
