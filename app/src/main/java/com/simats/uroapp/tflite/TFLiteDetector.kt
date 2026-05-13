package com.simats.uroapp.tflite

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * UroSmart Advanced AI Detection Engine
 * Uses YOLOv8/v11 TFLite Model for medical-grade identification.
 */
class TFLiteDetector(context: Context) {

    private var interpreter: Interpreter? = null
    private val modelPath = "urosmart_model.tflite"
    private val labels = listOf("Yeast", "Triple Phosphate", "Calcium Oxalate", "Squamous Cells", "Uric Acid")
    
    // Model parameters
    private val inputSize = 640
    private val outputRows = 8400 
    private var numClasses = labels.size 
    private val confidenceThreshold = 0.20f
    private val iouThreshold = 0.5f

    init {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(true)
            }
            interpreter = Interpreter(model, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (interpreter == null) return emptyList()

        // 1. Preprocessing
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // 2. Inference
        // YOLOv8 TFLite output is [1, 4 + numClasses, 8400]
        val outputBuffer = ByteBuffer.allocateDirect(1 * (4 + numClasses) * outputRows * 4)
        outputBuffer.order(ByteOrder.nativeOrder())
        
        interpreter?.run(tensorImage.buffer, outputBuffer)
        outputBuffer.rewind()

        // 3. Post-processing
        val results = mutableListOf<DetectionResult>()
        val output = FloatArray((4 + numClasses) * outputRows)
        outputBuffer.asFloatBuffer().get(output)

        for (i in 0 until outputRows) {
            var maxConf = 0f
            var maxId = -1
            
            // YOLOv8 stores scores for all classes
            for (c in 0 until numClasses) {
                val score = output[(4 + c) * outputRows + i]
                if (score > maxConf) {
                    maxConf = score
                    maxId = c
                }
            }

            if (maxConf > confidenceThreshold) {
                // YOLOv8 output: [x_center, y_center, width, height]
                val xCenter = output[0 * outputRows + i] / inputSize
                val yCenter = output[1 * outputRows + i] / inputSize
                val w = output[2 * outputRows + i] / inputSize
                val h = output[3 * outputRows + i] / inputSize
                
                results.add(DetectionResult(
                    labels[maxId],
                    maxConf,
                    xCenter - w/2f,
                    yCenter - h/2f,
                    w,
                    h
                ))
            }
        }

        // Apply Basic NMS
        return applyNMS(results)
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
        return intersection / union
    }
}

data class DetectionResult(
    val label: String,
    val confidence: Float,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float
)

