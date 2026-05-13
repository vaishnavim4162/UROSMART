package com.simats.uroapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FeedbackActivity : AppCompatActivity() {

    private val ratings = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val btnSkipTop = findViewById<TextView>(R.id.btn_feedback_skip_top)
        val btnSkipBottom = findViewById<TextView>(R.id.btn_feedback_skip_bottom)
        val btnSubmit = findViewById<Button>(R.id.btn_feedback_submit)

        btnSkipTop.setOnClickListener { navigateToDashboard() }
        btnSkipBottom.setOnClickListener { navigateToDashboard() }
        
        btnSubmit.setOnClickListener {
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
            navigateToDashboard()
        }

        setupRating(1, listOf(R.id.iv_q1_s1, R.id.iv_q1_s2, R.id.iv_q1_s3, R.id.iv_q1_s4, R.id.iv_q1_s5))
        setupRating(2, listOf(R.id.iv_q2_s1, R.id.iv_q2_s2, R.id.iv_q2_s3, R.id.iv_q2_s4, R.id.iv_q2_s5))
        setupRating(3, listOf(R.id.iv_q3_s1, R.id.iv_q3_s2, R.id.iv_q3_s3, R.id.iv_q3_s4, R.id.iv_q3_s5))
        setupRating(4, listOf(R.id.iv_q4_s1, R.id.iv_q4_s2, R.id.iv_q4_s3, R.id.iv_q4_s4, R.id.iv_q4_s5))
    }

    private fun setupRating(questionId: Int, starIds: List<Int>) {
        val stars = starIds.map { findViewById<ImageView>(it) }
        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                updateStars(stars, index + 1)
                ratings[questionId] = index + 1
            }
        }
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setImageResource(R.drawable.ic_star_filled)
            } else {
                imageView.setImageResource(R.drawable.ic_star_outline)
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
