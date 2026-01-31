package com.taotao.healthtracker.data

import android.content.Context
import com.taotao.healthtracker.data.entity.HealthRecord
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat { CSV, EXCEL }

object ExportManager {

    fun exportData(
        context: Context,
        records: List<HealthRecord>,
        format: ExportFormat
    ): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "HealthData_$timestamp.${if (format == ExportFormat.CSV) "csv" else "xlsx"}"
        val file = File(context.cacheDir, fileName)
        
        if (format == ExportFormat.CSV) {
            file.printWriter().use { out ->
                out.println("Date,SBP,DBP,HR,Weight")
                records.forEach { record ->
                    val sbp = record.sbp ?: ""
                    val dbp = record.dbp ?: ""
                    val hr = record.hr ?: ""
                    val w = record.weight ?: ""
                    out.println("${record.date},$sbp,$dbp,$hr,$w")
                }
            }
        } else {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Health Data")
            
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("Date")
            header.createCell(1).setCellValue("SBP")
            header.createCell(2).setCellValue("DBP")
            header.createCell(3).setCellValue("HR")
            header.createCell(4).setCellValue("Weight")
            
            records.forEachIndexed { index, record ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(record.date)
                if (record.sbp != null) row.createCell(1).setCellValue(record.sbp.toDouble())
                if (record.dbp != null) row.createCell(2).setCellValue(record.dbp.toDouble())
                if (record.hr != null) row.createCell(3).setCellValue(record.hr.toDouble())
                if (record.weight != null) row.createCell(4).setCellValue(record.weight.toDouble())
            }
            
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
        }
        
        return file
    }
}
