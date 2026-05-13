package com.simats.uroapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FederatedLearningActivity : AppCompatActivity() {

    private lateinit var flManager: FederatedLearningManager
    private lateinit var tvVersion: TextView
    private lateinit var tvSamples: TextView
    private lateinit var pbTraining: ProgressBar
    private lateinit var btnTrain: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_federated_learning)

        flManager = FederatedLearningManager.getInstance(this)

        tvVersion = findViewById(R.id.tv_fl_version)
        tvSamples = findViewById(R.id.tv_fl_samples)
        pbTraining = findViewById(R.id.pb_fl_training)
        btnTrain = findViewById(R.id.btn_start_training)
        val btnBack = findViewById<LinearLayout>(R.id.btn_back_fl)

        btnBack.setOnClickListener { finish() }

        updateUI()

        btnTrain.setOnClickListener {
            if (flManager.sampleCount < 5) {
                Toast.makeText(this, "Not enough samples. Collect more by verifying detections.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnTrain.isEnabled = false
            btnTrain.text = "Training..."
            
            flManager.startLocalTraining { success, message ->
                runOnUiThread {
                    btnTrain.isEnabled = true
                    btnTrain.text = "Start Local Training"
                    updateUI()
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Fetch latest version from server
        flManager.fetchGlobalModelInfo { info ->
            runOnUiThread {
                if (info != null) {
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        tvVersion.text = flManager.modelVersion
        tvSamples.text = "${flManager.sampleCount} / 5"
        pbTraining.progress = flManager.sampleCount
        
        if (flManager.sampleCount >= 5) {
            btnTrain.alpha = 1.0f
            btnTrain.isEnabled = true
        } else {
            btnTrain.alpha = 0.5f
            // btnTrain.isEnabled = false // Keep enabled to show toast
        }
    }
}
