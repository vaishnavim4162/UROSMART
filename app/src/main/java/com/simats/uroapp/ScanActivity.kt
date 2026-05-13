package com.simats.uroapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ScanActivity : AppCompatActivity() {
    private var imageUri1: Uri? = null
    private var imageUri2: Uri? = null

    private val pickImage1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri1 = it
            showPreview(1, it)
        }
    }

    private val pickImage2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri2 = it
            showPreview(2, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        val btnBack = findViewById<LinearLayout>(R.id.btn_back_dashboard_scan)
        val btnCancel = findViewById<Button>(R.id.btn_cancel_scan)
        val btnSubmit = findViewById<Button>(R.id.btn_submit_scan)
        val btnUpload1 = findViewById<LinearLayout>(R.id.btn_upload_image1)
        val btnUpload2 = findViewById<LinearLayout>(R.id.btn_upload_image2)
        val etCaseNo = findViewById<EditText>(R.id.et_case_no)

        val btnDelete1 = findViewById<ImageView>(R.id.btn_delete1)
        val btnDelete2 = findViewById<ImageView>(R.id.btn_delete2)
        val btnEdit1 = findViewById<ImageView>(R.id.btn_edit1)
        val btnEdit2 = findViewById<ImageView>(R.id.btn_edit2)

        btnBack.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val caseNo = etCaseNo.text.toString()
            if (imageUri1 == null || imageUri2 == null) {
                Toast.makeText(this, "Please select both images", Toast.LENGTH_SHORT).show()
            } else {
                // Pre-validate images
                if (!isValidMicroscopeImage(imageUri1!!)) {
                    showErrorDialog("1st image is not right")
                    return@setOnClickListener
                }
                if (!isValidMicroscopeImage(imageUri2!!)) {
                    showErrorDialog("2nd image is not right")
                    return@setOnClickListener
                }

                // Federated Learning & AI Model Processing
                val progressDialog = android.app.ProgressDialog(this)
                progressDialog.setMessage("Step 1/2: Updating model with latest federated insights...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Step 1: Simulated Local Training on new scan
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    progressDialog.setMessage("Step 2/2: Analyzing images using improved AI model...")
                    
                    // Step 2: Simulated Analysis
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        Toast.makeText(this, "Collaborative Learning Complete", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this, ReportActivity::class.java)
                        intent.putExtra("CASE_NO", caseNo)
                        intent.putExtra("IMAGE_URI1", imageUri1.toString())
                        intent.putExtra("IMAGE_URI2", imageUri2.toString())
                        startActivity(intent)
                        finish()
                    }, 3000)
                }, 2500)
            }
        }

        btnUpload1.setOnClickListener { pickImage1.launch("image/*") }
        btnUpload2.setOnClickListener { pickImage2.launch("image/*") }
        btnEdit1.setOnClickListener { pickImage1.launch("image/*") }
        btnEdit2.setOnClickListener { pickImage2.launch("image/*") }
        btnDelete1.setOnClickListener { removeImage(1) }
        btnDelete2.setOnClickListener { removeImage(2) }
    }

    private fun showPreview(index: Int, uri: Uri) {
        val preview = if (index == 1) findViewById<ImageView>(R.id.iv_preview1) else findViewById<ImageView>(R.id.iv_preview2)
        val uploadBtn = if (index == 1) findViewById<LinearLayout>(R.id.btn_upload_image1) else findViewById<LinearLayout>(R.id.btn_upload_image2)
        val editBtn = if (index == 1) findViewById<ImageView>(R.id.btn_edit1) else findViewById<ImageView>(R.id.btn_edit2)
        val deleteBtn = if (index == 1) findViewById<ImageView>(R.id.btn_delete1) else findViewById<ImageView>(R.id.btn_delete2)

        preview.setImageURI(uri)
        preview.visibility = View.VISIBLE
        editBtn.visibility = View.VISIBLE
        deleteBtn.visibility = View.VISIBLE
        uploadBtn.visibility = View.GONE
    }

    private fun removeImage(index: Int) {
        val preview = if (index == 1) findViewById<ImageView>(R.id.iv_preview1) else findViewById<ImageView>(R.id.iv_preview2)
        val uploadBtn = if (index == 1) findViewById<LinearLayout>(R.id.btn_upload_image1) else findViewById<LinearLayout>(R.id.btn_upload_image2)
        val editBtn = if (index == 1) findViewById<ImageView>(R.id.btn_edit1) else findViewById<ImageView>(R.id.btn_edit2)
        val deleteBtn = if (index == 1) findViewById<ImageView>(R.id.btn_delete1) else findViewById<ImageView>(R.id.btn_delete2)

        if (index == 1) imageUri1 = null else imageUri2 = null
        preview.visibility = View.GONE
        editBtn.visibility = View.GONE
        deleteBtn.visibility = View.GONE
        uploadBtn.visibility = View.VISIBLE
    }

    private fun isValidMicroscopeImage(uri: Uri): Boolean {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            return ratio in 0.7..1.4
        } catch (e: Exception) {
            return false
        }
    }

    private fun showErrorDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Submission")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
