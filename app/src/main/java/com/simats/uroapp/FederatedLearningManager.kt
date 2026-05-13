package com.simats.uroapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.simats.uroapp.network.ApiService
import com.simats.uroapp.network.BasicResponse
import com.simats.uroapp.network.FLModelInfoResponse
import com.simats.uroapp.network.GroundTruthLabels
import com.simats.uroapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FederatedLearningManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fl_prefs", Context.MODE_PRIVATE)
    private val apiService: ApiService = RetrofitClient.instance
    private val gson = Gson()

    companion object {
        private const val TAG = "FLManager"
        private const val MIN_TRAINING_SAMPLES = 5 // Reduced for demo/testing
        @Volatile
        private var instance: FederatedLearningManager? = null

        fun getInstance(context: Context): FederatedLearningManager {
            return instance ?: synchronized(this) {
                instance ?: FederatedLearningManager(context.applicationContext).also { instance = it }
            }
        }
    }

    var sampleCount: Int
        get() = prefs.getInt("sample_count", 0)
        private set(value) = prefs.edit().putInt("sample_count", value).apply()

    var modelVersion: String
        get() = prefs.getString("model_version", "1.0.0") ?: "1.0.0"
        private set(value) = prefs.edit().putString("model_version", value).apply()

    fun collectTrainingData(labels: GroundTruthLabels) {
        // In a real app, we would store image features or gradients
        // For this simulation, we just increment the sample count
        sampleCount++
        Log.d(TAG, "📥 Collected training sample. Total: $sampleCount")
        
        if (sampleCount >= MIN_TRAINING_SAMPLES) {
            startLocalTraining()
        }
    }

    fun startLocalTraining(onComplete: ((Boolean, String) -> Unit)? = null) {
        if (sampleCount < MIN_TRAINING_SAMPLES) {
            onComplete?.invoke(false, "Not enough samples (Need $MIN_TRAINING_SAMPLES)")
            return
        }

        Log.d(TAG, "🚀 Starting local training simulation...")
        
        // Simulate training delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val gradients = computeSimulatedGradients()
            val gradientsJson = gson.toJson(gradients)
            
            uploadGradients(gradientsJson) { success ->
                if (success) {
                    // Reset samples after successful upload (or move to archive)
                    sampleCount = 0
                    onComplete?.invoke(true, "Local training complete and gradients uploaded.")
                } else {
                    onComplete?.invoke(false, "Failed to upload gradients.")
                }
            }
        }, 3000)
    }

    private fun computeSimulatedGradients(): Map<String, List<Double>> {
        // Just dummy weight updates
        return mapOf(
            "yeast_layer" to listOf(0.01, -0.02, 0.05),
            "crystal_layer" to listOf(-0.01, 0.03, 0.02),
            "cell_layer" to listOf(0.04, -0.01, 0.01)
        )
    }

    private fun uploadGradients(gradientsJson: String, callback: (Boolean) -> Unit) {
        val clientId = "android_client_${System.currentTimeMillis()}"
        apiService.uploadGradients(clientId = clientId, gradients = gradientsJson).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Gradients uploaded successfully")
                    callback(true)
                } else {
                    Log.e(TAG, "❌ Failed to upload gradients: ${response.code()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                Log.e(TAG, "❌ Network error uploading gradients: ${t.message}")
                callback(false)
            }
        })
    }

    fun fetchGlobalModelInfo(callback: (FLModelInfoResponse?) -> Unit) {
        apiService.getGlobalModelInfo().enqueue(object : Callback<FLModelInfoResponse> {
            override fun onResponse(call: Call<FLModelInfoResponse>, response: Response<FLModelInfoResponse>) {
                if (response.isSuccessful) {
                    val info = response.body()
                    if (info != null) {
                        modelVersion = info.version
                    }
                    callback(info)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<FLModelInfoResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}
