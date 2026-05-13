package com.simats.uroapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        val btnDone = findViewById<TextView>(R.id.btn_privacy_done)
        btnDone.setOnClickListener {
            finish()
        }
    }
}
