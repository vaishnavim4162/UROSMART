package com.simats.uroapp

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.simats.uroapp.network.RetrofitClient

class ChangePasswordActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val etNewPassword = findViewById<EditText>(R.id.et_new_password)
        val ivNewPasswordToggle = findViewById<ImageView>(R.id.iv_new_password_toggle)
        val etConfirmNewPassword = findViewById<EditText>(R.id.et_confirm_new_password)
        val ivConfirmNewPasswordToggle = findViewById<ImageView>(R.id.iv_confirm_new_password_toggle)
        val btnConfirmReset = findViewById<Button>(R.id.btn_confirm_reset)

        ivNewPasswordToggle.setOnClickListener {
            if (isPasswordVisible) {
                etNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isPasswordVisible = false
            } else {
                etNewPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isPasswordVisible = true
            }
            etNewPassword.setSelection(etNewPassword.text.length)
        }

        ivConfirmNewPasswordToggle.setOnClickListener {
            if (isConfirmPasswordVisible) {
                etConfirmNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isConfirmPasswordVisible = false
            } else {
                etConfirmNewPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isConfirmPasswordVisible = true
            }
            etConfirmNewPassword.setSelection(etConfirmNewPassword.text.length)
        }

        val email = intent.getStringExtra("EMAIL") ?: ""

        btnConfirmReset.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmNewPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                android.widget.Toast.makeText(this, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                android.widget.Toast.makeText(this, "Passwords do not match", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = com.simats.uroapp.network.ChangePasswordRequest(email, newPassword)
            RetrofitClient.instance.resetPassword(request).enqueue(object : retrofit2.Callback<com.simats.uroapp.network.BasicResponse> {
                override fun onResponse(call: retrofit2.Call<com.simats.uroapp.network.BasicResponse>, response: retrofit2.Response<com.simats.uroapp.network.BasicResponse>) {
                    if (response.isSuccessful) {
                        android.widget.Toast.makeText(this@ChangePasswordActivity, "Password reset successful", android.widget.Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        android.widget.Toast.makeText(this@ChangePasswordActivity, "Reset failed", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.simats.uroapp.network.BasicResponse>, t: Throwable) {
                    android.widget.Toast.makeText(this@ChangePasswordActivity, "Network error", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
