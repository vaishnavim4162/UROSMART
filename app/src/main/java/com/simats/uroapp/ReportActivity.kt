package com.simats.uroapp

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.simats.uroapp.tflite.ONNXDetector
import com.simats.uroapp.tflite.DetectionResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportActivity : AppCompatActivity() {

    private var analyzedBitmap1: Bitmap? = null
    private var analyzedBitmap2: Bitmap? = null
    private var allResults = mutableListOf<DetectionResult>()
    private var caseName: String = "Not Assigned"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val btnClose = findViewById<TextView>(R.id.btn_report_close)
        val tvCaseNo = findViewById<TextView>(R.id.tv_report_case_no)
        val ivAnalyzed = findViewById<ImageView>(R.id.iv_analyzed_image)
        val tvStatus = findViewById<TextView>(R.id.tv_report_status)
        val tvTitle = findViewById<TextView>(R.id.tv_report_success_title)

        caseName = intent.getStringExtra("CASE_NO") ?: "Not Assigned"
        if (caseName.isBlank()) caseName = "Not Assigned"
        tvCaseNo.text = caseName

        val uri1Str = intent.getStringExtra("IMAGE_URI1")
        val uri2Str = intent.getStringExtra("IMAGE_URI2")

        val detector = ONNXDetector(this)

        // Process Image 1
        uri1Str?.let {
            val uri = Uri.parse(it)
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                analyzedBitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val results = detector.detect(analyzedBitmap1!!)
                allResults.addAll(results)
                drawCleanLabels(analyzedBitmap1!!, results)
                ivAnalyzed.setImageBitmap(analyzedBitmap1)
            } catch (e: Exception) {
                ivAnalyzed.setImageURI(uri)
            }
        }

        // Process Image 2 (For PDF only)
        uri2Str?.let {
            val uri = Uri.parse(it)
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                analyzedBitmap2 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val results = detector.detect(analyzedBitmap2!!)
                allResults.addAll(results)
                drawCleanLabels(analyzedBitmap2!!, results)
            } catch (e: Exception) {
                // Silently fail for second image processing if it errors
            }
        }

        if (allResults.isNotEmpty()) {
            tvStatus.text = "Objects Detected"
            tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            tvTitle.text = "Report Successfully Generated!"
        } else {
            tvStatus.text = "No Detection"
            tvStatus.setTextColor(Color.GRAY)
            tvTitle.text = "No High-Confidence Medical Objects Detected"
        }
        updateResultsUI(allResults)

        val btnDownload = findViewById<android.widget.Button>(R.id.btn_report_download)
        val btnShare = findViewById<android.widget.Button>(R.id.btn_report_share)

        btnDownload.setOnClickListener { downloadReportAsPdf() }
        btnShare.setOnClickListener { shareReport() }
        btnClose.setOnClickListener { finish() }
    }

    private fun generateReportPdf(): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageWidth = 1080
        val pageHeight = 1920
        val dateStr = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date())

        // --- PAGE 1: ANALYSIS SUMMARY ---
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1 = page1.canvas

        // 1. Title
        paint.color = Color.BLACK
        paint.textSize = 60f
        paint.isFakeBoldText = true
        canvas1.drawText("Urine Microscopy Report", 60f, 100f, paint)

        // 2. Case Details
        paint.textSize = 40f
        paint.isFakeBoldText = false
        canvas1.drawText("Case: $caseName", 60f, 170f, paint)
        canvas1.drawText("Date: $dateStr", 60f, 220f, paint)

        // 3. Analysis Results
        paint.textSize = 45f
        paint.isFakeBoldText = true
        canvas1.drawText("Analysis Results:", 60f, 320f, paint)

        val classes = listOf("Yeast", "Triple Phosphate", "Calcium Oxalate", "Squamous Cells", "Uric Acid")
        var yPos = 380f
        for (cls in classes) {
            val isPresent = allResults.any { it.label == cls }
            paint.textSize = 38f
            paint.isFakeBoldText = true
            paint.color = if (isPresent) Color.parseColor("#F44336") else Color.parseColor("#4CAF50")
            val status = if (isPresent) "Present" else "Absent"
            canvas1.drawText("• $cls: $status", 80f, yPos, paint)
            yPos += 50f
        }

        // 4. Analyzed Images (Side by Side)
        val imgWidth = 460
        val imgHeight = 460
        val imgY = yPos + 40f
        
        analyzedBitmap1?.let {
            val src = Rect(0, 0, it.width, it.height)
            val dest = Rect(60, imgY.toInt(), 60 + imgWidth, (imgY + imgHeight).toInt())
            canvas1.drawBitmap(it, src, dest, null)
        }
        analyzedBitmap2?.let {
            val src = Rect(0, 0, it.width, it.height)
            val dest = Rect(pageWidth - 60 - imgWidth, imgY.toInt(), pageWidth - 60, (imgY + imgHeight).toInt())
            canvas1.drawBitmap(it, src, dest, null)
        }

        // 5. Summary Disclaimer Box
        val boxTop = imgY + imgHeight + 60f
        val boxX = 60f
        val boxWidth = pageWidth - 120
        
        paint.color = Color.parseColor("#757575")
        paint.textSize = 28f
        paint.isFakeBoldText = false
        val summaryText = "• These results may not be fully accurate. Confidence depends on image quality and sample preparation.\n" +
                         "• This is NOT a professional medical diagnosis. Results must be verified by a qualified physician.\n" +
                         "• Please consult a doctor to confirm these findings before taking any medical action."
        
        val textPaintForMeasure = TextPaint(paint)
        val measureLayout = StaticLayout.Builder.obtain(summaryText, 0, summaryText.length, textPaintForMeasure, boxWidth - 60)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(5f, 1f)
            .build()
            
        val textHeight = measureLayout.height
        val boxHeight = textHeight + 120f // text height + header + padding
        
        val boxPaint = Paint().apply {
            color = Color.parseColor("#FFEBEE")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#FF5252")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas1.drawRoundRect(boxX, boxTop, pageWidth - 60f, boxTop + boxHeight, 12f, 12f, boxPaint)
        canvas1.drawRoundRect(boxX, boxTop, pageWidth - 60f, boxTop + boxHeight, 12f, 12f, borderPaint)

        paint.color = Color.parseColor("#FF5252")
        paint.textSize = 32f
        paint.isFakeBoldText = true
        canvas1.drawText("⚠ Important Medical Disclaimer", 90f, boxTop + 50f, paint)

        paint.color = Color.parseColor("#757575")
        paint.textSize = 28f
        paint.isFakeBoldText = false
        drawWrappedText(canvas1, summaryText, 90f, boxTop + 90f, boxWidth - 60, paint)

        // 6. Footer
        paint.color = Color.GRAY
        paint.textSize = 24f
        canvas1.drawText("SIMATS UroSmart  •  Generated on $dateStr  •  See Page 2 for full disclaimer", pageWidth / 2f - 350f, pageHeight - 50f, paint)

        pdfDocument.finishPage(page1)

        // --- PAGE 2: FULL DISCLAIMER ---
        val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2 = page2.canvas

        paint.color = Color.parseColor("#FF5252")
        paint.textSize = 60f
        paint.isFakeBoldText = true
        canvas2.drawText("Medical Disclaimer", 60f, 100f, paint)

        paint.color = Color.BLACK
        paint.textSize = 30f
        paint.isFakeBoldText = false
        val fullDisclaimer = "1. For Informational Purposes Only\n" +
                "SIMATS UroSmart is a clinical decision-support aid intended to assist trained medical professionals in interpreting urinalysis microscopy images. It is NOT a substitute for professional medical advice, diagnosis, or treatment.\n\n" +
                "2. Results May Not Be Accurate\n" +
                "The automated detection system provides probabilistic results and may produce false positives or false negatives. Accuracy depends on image quality, sample preparation, and microscope calibration. All results must be verified by a qualified laboratory technician or physician before clinical use.\n\n" +
                "3. Consult a Healthcare Professional\n" +
                "Always seek the advice of your physician, pathologist, or other qualified health provider regarding any medical condition. Never disregard professional medical advice or delay in seeking it because of something displayed in this report.\n\n" +
                "4. No Doctor–Patient Relationship\n" +
                "Use of SIMATS UroSmart does not establish a doctor–patient relationship between the user and the developers, SIMATS, or any affiliated institution.\n\n" +
                "5. Not a Standalone Diagnostic Device\n" +
                "SIMATS UroSmart is intended for use by trained healthcare professionals in conjunction with standard clinical practice. It is not approved as a standalone diagnostic device and must always be used alongside conventional laboratory methods.\n\n" +
                "6. Emergency Situations\n" +
                "If you believe a patient is experiencing a medical emergency, call emergency services immediately. Do not rely on this app or this report in emergency situations.\n\n" +
                "7. Contact\n" +
                "For medical or regulatory queries: medical@simats.ac.in"
        
        drawWrappedText(canvas2, fullDisclaimer, 60f, 160f, pageWidth - 120, paint)

        // Footer Page 2
        paint.color = Color.GRAY
        paint.textSize = 24f
        canvas2.drawText("SIMATS UroSmart  •  Sri Muthukumaran Institute of Technology and Science  •  Case: $caseName", 60f, pageHeight - 50f, paint)

        pdfDocument.finishPage(page2)

        // Save file
        val file = File(getExternalFilesDir(null), "UroSmart_Report_${System.currentTimeMillis()}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            
            // Save metadata alongside the PDF for history
            saveMetadata(caseName, allResults, file)
            
            file
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }

    private fun saveMetadata(caseName: String, results: List<DetectionResult>, pdfFile: File) {
        try {
            val metadataFile = File(pdfFile.absolutePath.replace(".pdf", ".json"))
            val detectedLabels = results.map { it.label }.distinct()
            val resultSummary = if (detectedLabels.isEmpty()) "No Detection" else "Detected: ${detectedLabels.joinToString(", ")}"
            
            val json = """
                {
                  "caseName": "$caseName",
                  "resultSummary": "$resultSummary",
                  "timestamp": ${System.currentTimeMillis()}
                }
            """.trimIndent()
            metadataFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadReportAsPdf() {
        val file = generateReportPdf()
        if (file != null) {
            Toast.makeText(this, "Report saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            
            // Launch Label Verification for Federated Learning
            val intent = Intent(this, LabelVerificationActivity::class.java)
            val uri1Str = this.intent.getStringExtra("IMAGE_URI1")
            intent.putExtra("IMAGE_URI", uri1Str)
            
            // Pass detection results for UI automation
            val detectedLabels = allResults.map { it.label }.distinct()
            intent.putStringArrayListExtra("DETECTED_LABELS", ArrayList(detectedLabels))
            
            val confidenceBundle = Bundle()
            allResults.groupBy { it.label }.forEach { (label, results) ->
                val maxConf = results.maxByOrNull { it.confidence }?.confidence ?: 0f
                confidenceBundle.putFloat(label, maxConf)
            }
            intent.putExtra("CONFIDENCE_SCORES", confidenceBundle)
            
            startActivity(intent)

            // Navigate to Feedback screen
            startActivity(Intent(this, FeedbackActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Error generating report", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareReport() {
        val file = generateReportPdf()
        if (file != null) {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "UroSmart Microscopy Report")
                putExtra(Intent.EXTRA_TEXT, "UroSmart Clinical Report Generated for Case: $caseName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Report via"))
        } else {
            Toast.makeText(this, "Error generating report for sharing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, y: Float, width: Int, paint: Paint) {
        val textPaint = TextPaint(paint)
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(5f, 1f)
            .build()
        canvas.save()
        canvas.translate(x, y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawCleanLabels(bitmap: Bitmap, results: List<DetectionResult>) {
        val canvas = Canvas(bitmap)
        val w = bitmap.width
        val h = bitmap.height
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = w / 180f
            isAntiAlias = true
        }
        val bgPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = w / 32f
            isFakeBoldText = true
            isAntiAlias = true
        }
        for (res in results) {
            val colorStr = when (res.label) {
                "Triple Phosphate" -> "#2196F3"
                "Calcium Oxalate" -> "#F44336"
                "Yeast" -> "#FF9800"
                "Uric Acid" -> "#4CAF50"
                "Squamous Cells" -> "#9C27B0"
                else -> "#757575"
            }
            val color = Color.parseColor(colorStr)
            paint.color = color
            bgPaint.color = color
            val l = res.x * w
            val t = res.y * h
            val r = (res.x + res.w) * w
            val b = (res.y + res.h) * h
            canvas.drawRect(l, t, r, b, paint)
            val labelText = "${res.label} ${(res.confidence * 100).toInt()}%"
            val tw = textPaint.measureText(labelText)
            val th = textPaint.textSize
            val badge = RectF(l, t - th - 15f, l + tw + 20f, t)
            canvas.drawRoundRect(badge, 8f, 8f, bgPaint)
            canvas.drawText(labelText, l + 10f, t - 10f, textPaint)
        }
    }

    private fun updateResultsUI(results: List<DetectionResult>) {
        val classes = listOf("Yeast", "Triple Phosphate", "Calcium Oxalate", "Squamous Cells", "Uric Acid")
        for (cls in classes) {
            val tvId = when(cls) {
                "Yeast" -> R.id.tv_yeast_percent
                "Triple Phosphate" -> R.id.tv_triple_percent
                "Calcium Oxalate" -> R.id.tv_calcium_percent
                "Squamous Cells" -> R.id.tv_squamous_percent
                "Uric Acid" -> R.id.tv_uric_percent
                else -> 0
            }
            val tv = findViewById<TextView>(tvId)
            val switchId = when(cls) {
                "Yeast" -> R.id.switch_yeast_status
                "Triple Phosphate" -> R.id.switch_triple_status
                "Calcium Oxalate" -> R.id.switch_calcium_status
                "Squamous Cells" -> R.id.switch_squamous_status
                "Uric Acid" -> R.id.switch_uric_status
                else -> 0
            }
            val sw = findViewById<androidx.appcompat.widget.SwitchCompat>(switchId)
            val bestDetection = results.filter { it.label == cls }.maxByOrNull { it.confidence }
            if (bestDetection != null) {
                val p = (bestDetection.confidence * 100).toInt()
                tv.text = "Detected • $p%"
                val colorStr = when (cls) {
                    "Triple Phosphate" -> "#2196F3"
                    "Calcium Oxalate" -> "#F44336"
                    "Yeast" -> "#FF9800"
                    "Uric Acid" -> "#4CAF50"
                    "Squamous Cells" -> "#9C27B0"
                    else -> "#4CAF50"
                }
                tv.setTextColor(Color.parseColor(colorStr))
                sw.isChecked = true
            } else {
                tv.text = "Not Detected"
                tv.setTextColor(Color.parseColor("#757575"))
                sw.isChecked = false
            }
        }
    }
}
