package com.simats.uroapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DisclaimerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimer)

        val btnDone = findViewById<TextView>(R.id.btn_disclaimer_done)
        btnDone.setOnClickListener {
            finish()
        }
    }
}
