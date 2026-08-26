package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ReportCard
import com.example.data.model.StudentGrade
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportCardPdfGenerator {

    fun generateAndShareReportCard(
        context: Context,
        reportCard: ReportCard,
        grades: List<StudentGrade>
    ): File? {
        val file = generatePdf(context, reportCard, grades) ?: return null
        sharePdf(context, file, reportCard.studentName)
        return file
    }

    fun generatePdf(
        context: Context,
        reportCard: ReportCard,
        grades: List<StudentGrade>
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width in points
            val pageHeight = 842 // A4 standard height in points

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Paints
            val textPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(30, 41, 59)
                textSize = 10f
            }

            val boldPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(15, 23, 42)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val headerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(30, 58, 138) // Deep Blue
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val subHeaderPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(71, 85, 105)
                textSize = 9.5f
            }

            val bgPaint = Paint().apply {
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(203, 213, 225)
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            // Outer Border
            bgPaint.color = Color.WHITE
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            bgPaint.color = Color.rgb(241, 245, 249)
            val margin = 20f
            val contentWidth = pageWidth - (margin * 2)

            // Header Banner
            val headerBg = RectF(margin, margin, margin + contentWidth, margin + 85f)
            bgPaint.color = Color.rgb(238, 242, 255)
            canvas.drawRoundRect(headerBg, 8f, 8f, bgPaint)

            linePaint.color = Color.rgb(199, 210, 254)
            canvas.drawRoundRect(headerBg, 8f, 8f, linePaint)

            // School Title
            val titleText = "ACADEMIATRACK SECONDARY COLLEGE"
            val titleWidth = headerPaint.measureText(titleText)
            canvas.drawText(titleText, (pageWidth - titleWidth) / 2, margin + 26f, headerPaint)

            val mottoText = "Excellence in Character, Leadership, and Academic Distinction"
            val mottoWidth = subHeaderPaint.measureText(mottoText)
            canvas.drawText(mottoText, (pageWidth - mottoWidth) / 2, margin + 42f, subHeaderPaint)

            // Terminal Report Header
            boldPaint.textSize = 13f
            boldPaint.color = Color.rgb(30, 58, 138)
            val termTitle = "OFFICIAL TERMINAL STUDENT REPORT SHEET"
            val termTitleWidth = boldPaint.measureText(termTitle)
            canvas.drawText(termTitle, (pageWidth - termTitleWidth) / 2, margin + 65f, boldPaint)

            // Session & Term badge
            val badgeText = "${reportCard.term.uppercase()} | ACADEMIC SESSION ${reportCard.session}"
            subHeaderPaint.textSize = 8.5f
            subHeaderPaint.color = Color.rgb(79, 70, 229)
            val badgeWidth = subHeaderPaint.measureText(badgeText)
            canvas.drawText(badgeText, (pageWidth - badgeWidth) / 2, margin + 78f, subHeaderPaint)

            // Student Information Box
            var yPos = margin + 95f
            val studentBox = RectF(margin, yPos, margin + contentWidth, yPos + 65f)
            bgPaint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(studentBox, 6f, 6f, bgPaint)
            linePaint.color = Color.rgb(226, 232, 240)
            canvas.drawRoundRect(studentBox, 6f, 6f, linePaint)

            boldPaint.textSize = 9.5f
            boldPaint.color = Color.rgb(15, 23, 42)
            textPaint.textSize = 9.5f
            textPaint.color = Color.rgb(51, 65, 85)

            val col1 = margin + 12f
            val col2 = margin + 180f
            val col3 = margin + 360f

            canvas.drawText("STUDENT NAME:", col1, yPos + 18f, boldPaint)
            canvas.drawText(reportCard.studentName, col1 + 80f, yPos + 18f, textPaint)

            canvas.drawText("ADMISSION NO:", col2, yPos + 18f, boldPaint)
            canvas.drawText(reportCard.admissionNo, col2 + 75f, yPos + 18f, textPaint)

            canvas.drawText("CLASS:", col3, yPos + 18f, boldPaint)
            canvas.drawText(reportCard.className, col3 + 42f, yPos + 18f, textPaint)

            canvas.drawText("CLASS POSITION:", col1, yPos + 38f, boldPaint)
            canvas.drawText(reportCard.classPosition, col1 + 80f, yPos + 38f, textPaint)

            canvas.drawText("ATTENDANCE:", col2, yPos + 38f, boldPaint)
            canvas.drawText("${reportCard.attendancePresent} / ${reportCard.attendanceTotal} Days", col2 + 75f, yPos + 38f, textPaint)

            canvas.drawText("OVERALL AVG:", col3, yPos + 38f, boldPaint)
            boldPaint.color = Color.rgb(5, 150, 105)
            canvas.drawText(String.format(Locale.US, "%.1f%%", reportCard.averageScore), col3 + 75f, yPos + 38f, boldPaint)
            boldPaint.color = Color.rgb(15, 23, 42)

            // Table Header
            yPos += 75f
            val colWidths = floatArrayOf(150f, 45f, 45f, 50f, 50f, 50f, 45f, 120f)
            val headers = arrayOf("SUBJECT", "1st CA (15)", "2nd CA (15)", "Midterm (20)", "Exam (50)", "Total (100)", "Grade", "Remarks")

            // Header Row Background
            bgPaint.color = Color.rgb(30, 58, 138)
            val tableHeaderRect = RectF(margin, yPos, margin + contentWidth, yPos + 22f)
            canvas.drawRect(tableHeaderRect, bgPaint)

            boldPaint.color = Color.WHITE
            boldPaint.textSize = 8.5f
            var currX = margin
            headers.forEachIndexed { index, header ->
                val w = colWidths[index]
                canvas.drawText(header, currX + 4f, yPos + 14f, boldPaint)
                currX += w
            }

            yPos += 22f

            // Subject Rows
            textPaint.textSize = 8.5f
            grades.forEachIndexed { i, grade ->
                val rowRect = RectF(margin, yPos, margin + contentWidth, yPos + 19f)
                bgPaint.color = if (i % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                canvas.drawRect(rowRect, bgPaint)

                linePaint.color = Color.rgb(241, 245, 249)
                canvas.drawLine(margin, yPos + 19f, margin + contentWidth, yPos + 19f, linePaint)

                var x = margin
                val vals = arrayOf(
                    grade.subjectName,
                    String.format(Locale.US, "%.1f", grade.test1Score),
                    String.format(Locale.US, "%.1f", grade.test2Score),
                    String.format(Locale.US, "%.1f", grade.midtermScore),
                    String.format(Locale.US, "%.1f", grade.examScore),
                    String.format(Locale.US, "%.1f", grade.totalScore),
                    grade.gradeLetter,
                    grade.remarks
                )

                vals.forEachIndexed { colIdx, text ->
                    val w = colWidths[colIdx]
                    if (colIdx == 6) {
                        // Grade letter color
                        boldPaint.textSize = 8.5f
                        boldPaint.color = when (text) {
                            "A1" -> Color.rgb(5, 150, 105)
                            "B2", "B3" -> Color.rgb(37, 99, 235)
                            "C4", "C5", "C6" -> Color.rgb(217, 119, 6)
                            else -> Color.rgb(225, 29, 72)
                        }
                        canvas.drawText(text, x + 12f, yPos + 13f, boldPaint)
                    } else {
                        textPaint.color = Color.rgb(51, 65, 85)
                        canvas.drawText(text, x + 4f, yPos + 13f, textPaint)
                    }
                    x += w
                }

                yPos += 19f
            }

            // Summary Bottom Row
            val summaryRect = RectF(margin, yPos, margin + contentWidth, yPos + 22f)
            bgPaint.color = Color.rgb(238, 242, 255)
            canvas.drawRect(summaryRect, bgPaint)
            boldPaint.color = Color.rgb(30, 58, 138)
            boldPaint.textSize = 8.5f
            canvas.drawText("GRAND TOTAL / AVERAGE:", margin + 8f, yPos + 15f, boldPaint)
            canvas.drawText(
                String.format(Locale.US, "%.1f / %.1f (%.2f%%)", reportCard.totalScore, reportCard.maxPossibleScore, reportCard.averageScore),
                margin + 170f,
                yPos + 15f,
                boldPaint
            )

            // Grading Scale Key Box
            yPos += 30f
            val keyRect = RectF(margin, yPos, margin + contentWidth, yPos + 24f)
            bgPaint.color = Color.rgb(241, 245, 249)
            canvas.drawRoundRect(keyRect, 4f, 4f, bgPaint)
            textPaint.textSize = 7.5f
            textPaint.color = Color.rgb(100, 116, 139)
            val keyText = "GRADING KEY: A1 (75-100% Distinction) | B2-B3 (65-74% Very Good) | C4-C6 (50-64% Credit) | D7-E8 (40-49% Pass) | F9 (0-39% Fail)"
            val keyW = textPaint.measureText(keyText)
            canvas.drawText(keyText, (pageWidth - keyW) / 2, yPos + 15f, textPaint)

            // Remarks Section
            yPos += 32f
            val remarksBox = RectF(margin, yPos, margin + contentWidth, yPos + 115f)
            bgPaint.color = Color.WHITE
            canvas.drawRoundRect(remarksBox, 6f, 6f, bgPaint)
            linePaint.color = Color.rgb(203, 213, 225)
            canvas.drawRoundRect(remarksBox, 6f, 6f, linePaint)

            boldPaint.textSize = 9f
            boldPaint.color = Color.rgb(30, 58, 138)
            textPaint.textSize = 8.5f
            textPaint.color = Color.rgb(51, 65, 85)

            // Class Teacher Remark
            canvas.drawText("CLASS TEACHER'S REMARK:", margin + 12f, yPos + 18f, boldPaint)
            canvas.drawText(reportCard.classTeacherRemark, margin + 12f, yPos + 32f, textPaint)
            canvas.drawLine(margin + 12f, yPos + 48f, margin + 350f, yPos + 48f, linePaint)
            textPaint.textSize = 7.5f
            canvas.drawText("Teacher's Signature & Date", margin + 12f, yPos + 58f, textPaint)

            // Principal Remark
            textPaint.textSize = 8.5f
            canvas.drawText("PRINCIPAL'S OVERALL REMARK & VERDICT:", margin + 12f, yPos + 76f, boldPaint)
            canvas.drawText(reportCard.principalRemark, margin + 12f, yPos + 90f, textPaint)
            canvas.drawLine(margin + 12f, yPos + 104f, margin + 350f, yPos + 104f, linePaint)
            textPaint.textSize = 7.5f
            canvas.drawText("Principal's Signature & Official School Stamp", margin + 12f, yPos + 112f, textPaint)

            // Stamp Box
            val stampBox = RectF(margin + contentWidth - 110f, yPos + 12f, margin + contentWidth - 12f, yPos + 100f)
            bgPaint.color = Color.rgb(238, 242, 255)
            canvas.drawRoundRect(stampBox, 8f, 8f, bgPaint)
            linePaint.color = Color.rgb(99, 102, 241)
            linePaint.strokeWidth = 1.5f
            canvas.drawRoundRect(stampBox, 8f, 8f, linePaint)

            boldPaint.color = Color.rgb(67, 56, 202)
            boldPaint.textSize = 8f
            canvas.drawText("OFFICIAL SEAL", stampBox.left + 15f, stampBox.top + 28f, boldPaint)
            subHeaderPaint.textSize = 7f
            canvas.drawText("APPROVED", stampBox.left + 22f, stampBox.top + 48f, subHeaderPaint)
            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
            canvas.drawText(dateStr, stampBox.left + 24f, stampBox.top + 68f, subHeaderPaint)

            // Footer
            val footerText = "Generated via AcademiaTrack Secondary School Portal • Verified Digital Copy"
            textPaint.textSize = 7.5f
            textPaint.color = Color.rgb(148, 163, 184)
            val footerW = textPaint.measureText(footerText)
            canvas.drawText(footerText, (pageWidth - footerW) / 2, pageHeight - 15f, textPaint)

            pdfDocument.finishPage(page)

            // Write to file
            val sanitizedName = reportCard.studentName.replace("\\s+".toRegex(), "_")
            val sanitizedTerm = reportCard.term.replace("\\s+".toRegex(), "_")
            val fileName = "ReportCard_${sanitizedName}_$sanitizedTerm.pdf"

            val outputDir = File(context.cacheDir, "report_cards").apply { mkdirs() }
            val outputFile = File(outputDir, fileName)

            val outputStream = FileOutputStream(outputFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun sharePdf(context: Context, file: File, studentName: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Terminal Report Card - $studentName")
                putExtra(Intent.EXTRA_TEXT, "Attached is the official Secondary School Terminal Report Card for $studentName.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Report Card PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF generated at: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
