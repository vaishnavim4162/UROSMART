package com.simats.uroapp

import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvEmail = findViewById<TextView>(R.id.tv_profile_email)
        val btnBack = findViewById<LinearLayout>(R.id.btn_back_dashboard)
        val optionPrivacy = findViewById<LinearLayout>(R.id.option_privacy) // I need to add this ID to XML

        // Load saved email
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPref.getString("EMAIL", "No Email Found")
        val token = sharedPref.getString("TOKEN", "")
        tvEmail.text = savedEmail

        btnBack.setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.option_change_password).setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("EMAIL", savedEmail)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.option_privacy).setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_disclaimer).setOnClickListener {
            startActivity(Intent(this, DisclaimerActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.option_delete_account).setOnClickListener {
            showDeleteConfirmation(token)
        }
    }

    private fun showDeleteConfirmation(token: String?) {
        if (token.isNullOrEmpty()) {
            android.widget.Toast.makeText(this, "Session expired. Please login again.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount(token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount(token: String) {
        val authHeader = "Bearer $token"
        com.simats.uroapp.network.RetrofitClient.instance.deleteAccount(authHeader)
            .enqueue(object : retrofit2.Callback<com.simats.uroapp.network.BasicResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.simats.uroapp.network.BasicResponse>,
                    response: retrofit2.Response<com.simats.uroapp.network.BasicResponse>
                ) {
                    if (response.isSuccessful) {
                        android.widget.Toast.makeText(this@ProfileActivity, "Account deleted", android.widget.Toast.LENGTH_SHORT).show()
                        
                        // Clear preferences and logout
                        getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        android.widget.Toast.makeText(this@ProfileActivity, "Failed to delete account", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.simats.uroapp.network.BasicResponse>,
                    t: Throwable
                ) {
                    android.widget.Toast.makeText(this@ProfileActivity, "Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
    }
}
