package com.example.tdm.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.tdm.model.CreatinineUnit
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.regex.Pattern

data class ExtractedLabData(
  val rawText: String = "",
  val patientName: String? = null,
  val patientId: String? = null,
  val serumCreatinine: Double? = null,
  val creatinineUnit: CreatinineUnit = CreatinineUnit.UMOL_L,
  val preDoseConcMgL: Double? = null,
  val postDoseConcMgL: Double? = null,
  val doseMg: Double? = null,
  val intervalHours: Double? = null
)

object LabReportExtractor {

  fun processBitmapWithOcr(
    bitmap: Bitmap,
    onSuccess: (ExtractedLabData) -> Unit,
    onFailure: (Exception) -> Unit
  ) {
    try {
      val image = InputImage.fromBitmap(bitmap, 0)
      val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

      recognizer.process(image)
        .addOnSuccessListener { visionText ->
          val extracted = parseClinicalText(visionText.text)
          onSuccess(extracted)
        }
        .addOnFailureListener { e ->
          // Fallback parsing if ML Kit encounters model download delay in offline/test environment
          val extracted = parseClinicalText("")
          if (extracted.patientName != null || extracted.serumCreatinine != null) {
            onSuccess(extracted)
          } else {
            onFailure(e)
          }
        }
    } catch (e: Exception) {
      onFailure(e)
    }
  }

  fun parseClinicalText(text: String): ExtractedLabData {
    var patientName: String? = null
    var patientId: String? = null
    var serumCreatinine: Double? = null
    var creatinineUnit: CreatinineUnit = CreatinineUnit.UMOL_L
    var preDoseConcMgL: Double? = null
    var postDoseConcMgL: Double? = null
    var doseMg: Double? = null
    var intervalHours: Double? = null

    val lines = text.lines()

    // 1. Parse Patient Name & ID
    for (line in lines) {
      val nameMatcher = Pattern.compile("(?i)(?:patient\\s*name|name|pt)[:\\s]+([A-Za-z\\s]{3,30})").matcher(line)
      if (nameMatcher.find() && patientName == null) {
        val candidate = nameMatcher.group(1)?.trim()
        if (candidate != null && !candidate.equals("ID", ignoreCase = true)) {
          patientName = candidate
        }
      }

      val idMatcher = Pattern.compile("(?i)(?:mrn|id|rn|patient\\s*id)[:\\s]+([A-Z0-9\\-]+)").matcher(line)
      if (idMatcher.find() && patientId == null) {
        patientId = idMatcher.group(1)?.trim()
      }
    }

    // 2. Parse Serum Creatinine
    val crPattern = Pattern.compile("(?i)(?:creatinine|s\\.cr|serum\\s+cr|cr)[:\\s]+([0-9]+(?:\\.[0-9]+)?)\\s*(umol/l|µmol/l|mg/dl)?")
    val crMatcher = crPattern.matcher(text)
    if (crMatcher.find()) {
      serumCreatinine = crMatcher.group(1)?.toDoubleOrNull()
      val unitStr = crMatcher.group(2)?.lowercase()
      if (unitStr != null && unitStr.contains("mg")) {
        creatinineUnit = CreatinineUnit.MG_DL
      } else {
        creatinineUnit = CreatinineUnit.UMOL_L
      }
    }

    // 3. Parse Vancomycin Pre-dose / Trough
    val troughPattern = Pattern.compile("(?i)(?:trough|pre[- ]?dose|pre|cmin)[:\\s]+([0-9]+(?:\\.[0-9]+)?)\\s*(?:mg/l|mcg/ml|ug/ml)?")
    val troughMatcher = troughPattern.matcher(text)
    if (troughMatcher.find()) {
      preDoseConcMgL = troughMatcher.group(1)?.toDoubleOrNull()
    }

    // 4. Parse Vancomycin Post-dose / Peak
    val peakPattern = Pattern.compile("(?i)(?:peak|post[- ]?dose|post|cmax)[:\\s]+([0-9]+(?:\\.[0-9]+)?)\\s*(?:mg/l|mcg/ml|ug/ml)?")
    val peakMatcher = peakPattern.matcher(text)
    if (peakMatcher.find()) {
      postDoseConcMgL = peakMatcher.group(1)?.toDoubleOrNull()
    }

    // 5. Parse Dose & Interval
    val dosePattern = Pattern.compile("(?i)(?:dose|regimen)[:\\s]+([0-9]{3,4})\\s*mg")
    val doseMatcher = dosePattern.matcher(text)
    if (doseMatcher.find()) {
      doseMg = doseMatcher.group(1)?.toDoubleOrNull()
    }

    val intervalPattern = Pattern.compile("(?i)(?:q|interval[:\\s]+|every\\s+)([0-9]{1,2})\\s*h")
    val intervalMatcher = intervalPattern.matcher(text)
    if (intervalMatcher.find()) {
      intervalHours = intervalMatcher.group(1)?.toDoubleOrNull()
    }

    return ExtractedLabData(
      rawText = text,
      patientName = patientName,
      patientId = patientId,
      serumCreatinine = serumCreatinine,
      creatinineUnit = creatinineUnit,
      preDoseConcMgL = preDoseConcMgL,
      postDoseConcMgL = postDoseConcMgL,
      doseMg = doseMg,
      intervalHours = intervalHours
    )
  }

  // Generates a crisp, realistic simulated hospital lab slip bitmap for testing/demos
  fun generateSimulatedLabReportBitmap(
    hospitalName: String,
    patientName: String,
    mrn: String,
    serumCr: String,
    troughConc: String?,
    peakConc: String?,
    dose: String,
    interval: String
  ): Bitmap {
    val width = 720
    val height = 960
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background paper
    canvas.drawColor(Color.WHITE)

    val paint = Paint().apply {
      isAntiAlias = true
      color = Color.DKGRAY
    }

    // Border
    val borderPaint = Paint().apply {
      color = Color.LTGRAY
      style = Paint.Style.STROKE
      strokeWidth = 4f
    }
    canvas.drawRect(20f, 20f, width - 20f, height - 20f, borderPaint)

    // Header Banner
    val headerPaint = Paint().apply {
      color = Color.rgb(0, 104, 117) // Teal Primary
      style = Paint.Style.FILL
    }
    canvas.drawRect(20f, 20f, width - 20f, 130f, headerPaint)

    val titlePaint = Paint().apply {
      color = Color.WHITE
      textSize = 28f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }
    canvas.drawText(hospitalName, 40f, 65f, titlePaint)

    val subTitlePaint = Paint().apply {
      color = Color.rgb(200, 240, 245)
      textSize = 20f
      isAntiAlias = true
    }
    canvas.drawText("CLINICAL BIOCHEMISTRY & TDM LABORATORY REPORT", 40f, 100f, subTitlePaint)

    // Patient Demographics Box
    paint.textSize = 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.color = Color.BLACK
    canvas.drawText("PATIENT DEMOGRAPHICS", 40f, 180f, paint)

    paint.typeface = Typeface.DEFAULT
    paint.textSize = 22f
    paint.color = Color.DKGRAY
    canvas.drawText("Name: $patientName", 40f, 220f, paint)
    canvas.drawText("MRN: $mrn", 40f, 255f, paint)
    canvas.drawText("Dose: $dose mg IV", 40f, 290f, paint)
    canvas.drawText("Interval: q${interval}h", 380f, 290f, paint)

    val linePaint = Paint().apply {
      color = Color.LTGRAY
      strokeWidth = 2f
    }
    canvas.drawLine(40f, 320f, width - 40f, 320f, linePaint)

    // Table Header
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.color = Color.BLACK
    canvas.drawText("TEST PARAMETER", 40f, 360f, paint)
    canvas.drawText("RESULT", 360f, 360f, paint)
    canvas.drawText("REF. RANGE", 520f, 360f, paint)
    canvas.drawLine(40f, 375f, width - 40f, 375f, linePaint)

    // Row 1: Serum Creatinine
    var curY = 420f
    paint.typeface = Typeface.DEFAULT
    paint.color = Color.DKGRAY
    canvas.drawText("Serum Creatinine", 40f, curY, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Creatinine: $serumCr", 360f, curY, paint)
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("60 - 110 umol/L", 520f, curY, paint)

    // Row 2: Vancomycin Trough
    if (troughConc != null) {
      curY += 60f
      canvas.drawText("Vancomycin Pre-dose", 40f, curY, paint)
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("Trough: $troughConc mg/L", 360f, curY, paint)
      paint.typeface = Typeface.DEFAULT
      canvas.drawText("10.0 - 20.0 mg/L", 520f, curY, paint)
    }

    // Row 3: Vancomycin Peak
    if (peakConc != null) {
      curY += 60f
      canvas.drawText("Vancomycin Post-dose", 40f, curY, paint)
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("Peak: $peakConc mg/L", 360f, curY, paint)
      paint.typeface = Typeface.DEFAULT
      canvas.drawText("25.0 - 40.0 mg/L", 520f, curY, paint)
    }

    // Footer
    curY += 120f
    canvas.drawLine(40f, curY, width - 40f, curY, linePaint)
    curY += 40f
    val stampPaint = Paint().apply {
      color = Color.rgb(0, 100, 0)
      textSize = 18f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }
    canvas.drawText("VERIFIED BY PHARMACOKINETICS LAB SPECIALIST", 40f, curY, stampPaint)
    curY += 30f
    val smallPaint = Paint().apply {
      color = Color.GRAY
      textSize = 16f
      isAntiAlias = true
    }
    canvas.drawText("Fictional Laboratory Record - For Academic Evaluation Only", 40f, curY, smallPaint)

    return bitmap
  }
}
