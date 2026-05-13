package com.simats.uroapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TermsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

        val btnDone = findViewById<TextView>(R.id.btn_terms_done)
        btnDone.setOnClickListener {
            finish()
        }
    }
}
