package com.simats.uroapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.File
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvReports: RecyclerView
    private lateinit var tvReportCount: TextView
    private lateinit var tvNoReports: TextView
    private lateinit var etSearch: EditText
    private var allReports = mutableListOf<ReportRecord>()
    private var filteredReports = mutableListOf<ReportRecord>()
    private lateinit var adapter: ReportAdapter
    
    private var selectedDate: Calendar? = null

    data class ReportRecord(val file: File, val caseName: String, val resultSummary: String, val timestamp: Long)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvReports = findViewById(R.id.rv_reports)
        tvReportCount = findViewById(R.id.tv_report_count)
        tvNoReports = findViewById(R.id.tv_no_reports)
        etSearch = findViewById(R.id.et_search_reports)

        findViewById<TextView>(R.id.btn_back_dashboard).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_clear_all).setOnClickListener { clearAllReports() }
        findViewById<ImageView>(R.id.iv_calendar_picker).setOnClickListener { showCalendarDialog() }

        setupRecyclerView()
        setupSearch()
        loadReports()
    }

    private fun setupRecyclerView() {
        adapter = ReportAdapter(filteredReports, { record -> viewPdf(record.file) }, 
                                          { record -> sharePdf(record.file, record.caseName) },
                                          { record -> deleteReport(record) })
        rvReports.layoutManager = LinearLayoutManager(this)
        rvReports.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadReports() {
        allReports.clear()
        val dir = getExternalFilesDir(null)
        val files = dir?.listFiles { file -> file.extension == "pdf" }
        
        files?.forEach { file ->
            val metadataFile = File(file.absolutePath.replace(".pdf", ".json"))
            var caseName = "Unknown Case"
            var resultSummary = "No Summary"
            var timestamp = file.lastModified()

            if (metadataFile.exists()) {
                try {
                    val json = JSONObject(metadataFile.readText())
                    caseName = json.optString("caseName", "Unknown Case")
                    resultSummary = json.optString("resultSummary", "No Summary")
                    timestamp = json.optLong("timestamp", timestamp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            allReports.add(ReportRecord(file, caseName, resultSummary, timestamp))
        }

        allReports.sortByDescending { it.timestamp }
        applyFilters()
    }

    private fun applyFilters() {
        val query = etSearch.text.toString().lowercase()
        filteredReports.clear()
        
        for (report in allReports) {
            val matchesSearch = report.caseName.lowercase().contains(query)
            val matchesDate = selectedDate == null || isSameDay(report.timestamp, selectedDate!!.timeInMillis)
            
            if (matchesSearch && matchesDate) {
                filteredReports.add(report)
            }
        }
        
        updateUI()
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun updateUI() {
        adapter.notifyDataSetChanged()
        tvReportCount.text = "Medical Reports (${filteredReports.size})"
        if (filteredReports.isEmpty()) {
            tvNoReports.visibility = View.VISIBLE
            rvReports.visibility = View.GONE
        } else {
            tvNoReports.visibility = View.GONE
            rvReports.visibility = View.VISIBLE
        }
    }

    private fun showCalendarDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_calendar, null)
        val dialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
            .setView(dialogView)
            .create()

        val calendarView = dialogView.findViewById<CalendarView>(R.id.calendar_view)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btn_calendar_cancel)
        val btnDone = dialogView.findViewById<TextView>(R.id.btn_calendar_done)
        val btnShowAll = dialogView.findViewById<Button>(R.id.btn_show_all_reports)

        var tempDate = Calendar.getInstance()
        selectedDate?.let { tempDate.timeInMillis = it.timeInMillis }
        calendarView.date = tempDate.timeInMillis

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            tempDate.set(year, month, dayOfMonth)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        
        btnDone.setOnClickListener {
            selectedDate = Calendar.getInstance().apply { timeInMillis = tempDate.timeInMillis }
            applyFilters()
            dialog.dismiss()
        }

        btnShowAll.setOnClickListener {
            selectedDate = null
            applyFilters()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun viewPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open Report"))
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePdf(file: File, caseName: String) {
        try {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "UroSmart Report: $caseName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Download/Share Report"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteReport(record: ReportRecord) {
        val metadataFile = File(record.file.absolutePath.replace(".pdf", ".json"))
        record.file.delete()
        if (metadataFile.exists()) metadataFile.delete()
        allReports.remove(record)
        applyFilters()
        Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllReports() {
        val dir = getExternalFilesDir(null)
        dir?.listFiles()?.forEach { it.delete() }
        allReports.clear()
        applyFilters()
        Toast.makeText(this, "All reports cleared", Toast.LENGTH_SHORT).show()
    }
}

class ReportAdapter(private val reports: List<HistoryActivity.ReportRecord>, 
                          private val onView: (HistoryActivity.ReportRecord) -> Unit,
                          private val onDownload: (HistoryActivity.ReportRecord) -> Unit,
                          private val onDelete: (HistoryActivity.ReportRecord) -> Unit) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCaseName: TextView = view.findViewById(R.id.tv_item_case_name)
        val tvResult: TextView = view.findViewById(R.id.tv_item_result)
        val btnView: ImageView = view.findViewById(R.id.btn_item_view)
        val btnDownload: ImageView = view.findViewById(R.id.btn_item_download)
        val btnDelete: ImageView = view.findViewById(R.id.btn_item_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        holder.tvCaseName.text = report.caseName
        holder.tvResult.text = report.resultSummary
        
        holder.btnView.setOnClickListener { onView(report) }
        holder.btnDownload.setOnClickListener { onDownload(report) }
        holder.btnDelete.setOnClickListener { onDelete(report) }
    }

    override fun getItemCount() = reports.size
}
