package com.simats.uroapp

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.simats.uroapp.network.GroundTruthLabels

class LabelVerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_label_verification)

        val ivPreview = findViewById<ImageView>(R.id.iv_verification_preview)
        val switchYeast = findViewById<SwitchCompat>(R.id.switch_yeast)
        val switchTriple = findViewById<SwitchCompat>(R.id.switch_triple)
        val switchCalcium = findViewById<SwitchCompat>(R.id.switch_calcium)
        val switchSquamous = findViewById<SwitchCompat>(R.id.switch_squamous)
        val switchUric = findViewById<SwitchCompat>(R.id.switch_uric)
        val btnSubmit = findViewById<Button>(R.id.btn_submit_verification)
        val btnSkip = findViewById<LinearLayout>(R.id.btn_back_verification)
        val tvSkipBottom = findViewById<TextView>(R.id.tv_skip_bottom)

        val tvStatusYeast = findViewById<TextView>(R.id.tv_status_yeast)
        val tvStatusTriple = findViewById<TextView>(R.id.tv_status_triple)
        val tvStatusCalcium = findViewById<TextView>(R.id.tv_status_calcium)
        val tvStatusSquamous = findViewById<TextView>(R.id.tv_status_squamous)
        val tvStatusUric = findViewById<TextView>(R.id.tv_status_uric)

        val imageUriStr = intent.getStringExtra("IMAGE_URI")
        if (imageUriStr != null) {
            ivPreview.setImageURI(Uri.parse(imageUriStr))
        }

        // Initialize switches based on detection results passed via intent
        val detectedLabels = intent.getStringArrayListExtra("DETECTED_LABELS") ?: arrayListOf()
        val confidenceScores = intent.getBundleExtra("CONFIDENCE_SCORES")

        val categories = listOf(
            Triple("Yeast", switchYeast, tvStatusYeast),
            Triple("Triple Phosphate", switchTriple, tvStatusTriple),
            Triple("Calcium Oxalate", switchCalcium, tvStatusCalcium),
            Triple("Squamous Cells", switchSquamous, tvStatusSquamous),
            Triple("Uric Acid", switchUric, tvStatusUric)
        )

        categories.forEach { (label, switch, tvStatus) ->
            if (detectedLabels.contains(label)) {
                switch.isChecked = true
                val conf = confidenceScores?.getFloat(label) ?: 0f
                val percent = (conf * 100).toInt()
                tvStatus.text = "System: Detected $percent%"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#2196F3"))
            } else {
                switch.isChecked = false
                tvStatus.text = "System: Not detected"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#757575"))
            }
        }

        btnSkip.setOnClickListener { finish() }
        tvSkipBottom.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val labels = GroundTruthLabels(
                yeast = switchYeast.isChecked,
                triplePhosphate = switchTriple.isChecked,
                calciumOxalate = switchCalcium.isChecked,
                squamousCells = switchSquamous.isChecked,
                uricAcid = switchUric.isChecked
            )

            FederatedLearningManager.getInstance(this).collectTrainingData(labels)
            
            Toast.makeText(this, "Thank you! Your feedback helps improve the model.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
