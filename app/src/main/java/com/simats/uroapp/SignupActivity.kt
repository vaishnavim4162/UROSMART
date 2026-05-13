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
import com.simats.uroapp.network.RetrofitClient
import com.simats.uroapp.network.SignupRequest
import com.simats.uroapp.network.SignupResponse

class SignupActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etPhone = findViewById<EditText>(R.id.et_phone)
        val etSignupEmail = findViewById<EditText>(R.id.et_signup_email)
        val etSignupPassword = findViewById<EditText>(R.id.et_signup_password)
        val ivPasswordToggle = findViewById<ImageView>(R.id.iv_signup_password_toggle)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val ivConfirmPasswordToggle = findViewById<ImageView>(R.id.iv_confirm_password_toggle)
        val btnSignup = findViewById<Button>(R.id.btn_signup)
        val tvLoginLink = findViewById<TextView>(R.id.tv_login_link)

        ivPasswordToggle.setOnClickListener {
            if (isPasswordVisible) {
                etSignupPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isPasswordVisible = false
            } else {
                etSignupPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isPasswordVisible = true
            }
            etSignupPassword.setSelection(etSignupPassword.text.length)
        }

        ivConfirmPasswordToggle.setOnClickListener {
            if (isConfirmPasswordVisible) {
                etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isConfirmPasswordVisible = false
            } else {
                etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isConfirmPasswordVisible = true
            }
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        btnSignup.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val email = etSignupEmail.text.toString().trim()
            val password = etSignupPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(this, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                android.widget.Toast.makeText(this, "Passwords do not match", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = SignupRequest(phone, email, password)
            RetrofitClient.instance.signup(request).enqueue(object : retrofit2.Callback<SignupResponse> {
                override fun onResponse(call: retrofit2.Call<SignupResponse>, response: retrofit2.Response<SignupResponse>) {
                    if (response.isSuccessful) {
                        val signupResponse = response.body()
                        val email = signupResponse?.user?.email ?: ""
                        val token = signupResponse?.access_token ?: ""

                        // Save user data
                        val sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("EMAIL", email)
                            putString("TOKEN", token)
                            apply()
                        }

                        android.widget.Toast.makeText(this@SignupActivity, "Registration successful", android.widget.Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignupActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            com.google.gson.Gson().fromJson(errorBody, SignupResponse::class.java).error ?: "Error: ${response.code()}"
                        } catch (e: Exception) {
                            errorBody ?: "Registration failed (${response.code()})"
                        }
                        android.widget.Toast.makeText(this@SignupActivity, errorMessage, android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<SignupResponse>, t: Throwable) {
                    android.widget.Toast.makeText(this@SignupActivity, "Network error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
