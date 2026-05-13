package com.simats.uroapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DataPermissionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_permission)

        val btnAllow = findViewById<Button>(R.id.btn_allow)
        val btnDecline = findViewById<Button>(R.id.btn_decline)

        btnAllow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnDecline.setOnClickListener {
            finishAffinity() // Closes the app
        }
    }
}
