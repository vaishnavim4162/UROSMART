package com.simats.uroapp.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import java.util.Collections

/**
 * UroSmart Clinical AI Detection Engine (ONNX Runtime)
 * Optimized for YOLOv8 specific urine sediment components.
 */
class ONNXDetector(private val context: Context) {

    private val ortEnv = OrtEnvironment.getEnvironment()
    private var ortSession: OrtSession? = null
    
    // Model parameters (Matched to our specialized 5-class model)
    private val modelPath = "urosmart_model.onnx"
    private val labels = listOf("Yeast", "Triple Phosphate", "Calcium Oxalate", "Squamous Cells", "Uric Acid")
    
    private val inputSize = 320
    private val outputRows = 2100 // For imgsz=320
    private val numClasses = labels.size
    private val confidenceThreshold = 0.20f
    private val iouThreshold = 0.45f

    init {
        try {
            val modelBytes = context.assets.open(modelPath).readBytes()
            val options = OrtSession.SessionOptions()
            ortSession = ortEnv.createSession(modelBytes, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (ortSession == null) return emptyList()

        // 1. Preprocessing
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val floatBuffer = FloatBuffer.allocate(1 * 3 * inputSize * inputSize)
        
        // Convert Bitmap to FloatBuffer (CHW format, normalized to [0,1])
        val pixels = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        
        // R channel
        for (pixel in pixels) floatBuffer.put(Color.red(pixel) / 255.0f)
        // G channel
        for (pixel in pixels) floatBuffer.put(Color.green(pixel) / 255.0f)
        // B channel
        for (pixel in pixels) floatBuffer.put(Color.blue(pixel) / 255.0f)
        
        floatBuffer.rewind()
        
        val inputName = ortSession?.inputNames?.iterator()?.next() ?: "images"
        val inputTensor = OnnxTensor.createTensor(ortEnv, floatBuffer, longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong()))

        // 2. Inference
        val results = ortSession?.run(Collections.singletonMap(inputName, inputTensor))
        val output = results?.get(0)?.value as Array<Array<FloatArray>> // [1, 9, 2100]
        val data = output[0] // [9, 2100]

        // 3. Post-processing
        val detections = mutableListOf<DetectionResult>()
        
        for (i in 0 until outputRows) {
            var maxConf = 0f
            var maxId = -1
            
            // YOLOv8 output for 5 classes: [x, y, w, h, score0, score1, score2, score3, score4]
            for (c in 0 until numClasses) {
                val score = data[4 + c][i]
                if (score > maxConf) {
                    maxConf = score
                    maxId = c
                }
            }

            if (maxConf > confidenceThreshold) {
                val xCenter = data[0][i] / inputSize
                val yCenter = data[1][i] / inputSize
                val w = data[2][i] / inputSize
                val h = data[3][i] / inputSize
                
                detections.add(DetectionResult(
                    labels[maxId],
                    maxConf,
                    xCenter - w/2f,
                    yCenter - h/2f,
                    w,
                    h
                ))
            }
        }

        inputTensor.close()
        results?.close()

        return applyNMS(detections)
    }

    private fun applyNMS(detections: List<DetectionResult>): List<DetectionResult> {
        val sorted = detections.sortedByDescending { it.confidence }
        val selected = mutableListOf<DetectionResult>()
        val active = BooleanArray(sorted.size) { true }
        
        for (i in sorted.indices) {
            if (active[i]) {
                selected.add(sorted[i])
                for (j in i + 1 until sorted.size) {
                    if (active[j] && calculateIOU(sorted[i], sorted[j]) > iouThreshold) {
                        active[j] = false
                    }
                }
            }
        }
        return selected
    }

    private fun calculateIOU(a: DetectionResult, b: DetectionResult): Float {
        val x1 = maxOf(a.x, b.x)
        val y1 = maxOf(a.y, b.y)
        val x2 = minOf(a.x + a.w, b.x + b.w)
        val y2 = minOf(a.y + a.h, b.y + b.h)
        val intersection = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val union = (a.w * a.h) + (b.w * b.h) - intersection
        return if (union <= 0) 0f else intersection / union
    }
}
