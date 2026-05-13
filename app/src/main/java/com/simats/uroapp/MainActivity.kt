package com.simats.uroapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import android.widget.LinearLayout

import androidx.drawerlayout.widget.DrawerLayout
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val btnMenu = findViewById<ImageView>(R.id.btn_menu)
        val btnLogout = findViewById<LinearLayout>(R.id.btn_logout)
        val navView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_privacy -> {
                    val intent = Intent(this, PrivacyActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_terms -> {
                    val intent = Intent(this, TermsActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        findViewById<LinearLayout>(R.id.btn_disclaimer_dashboard).setOnClickListener {
            startActivity(Intent(this, DisclaimerActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.card_upload).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.card_reports).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}