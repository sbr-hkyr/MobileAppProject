package com.example.tdm.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tdm.camera.ExtractedLabData
import com.example.tdm.camera.LabReportExtractor
import com.example.tdm.data.CalculationHistoryDao
import com.example.tdm.data.CalculationHistoryEntity
import com.example.tdm.data.TdmDatabase
import com.example.tdm.engine.SimulationResult
import com.example.tdm.engine.TdmCalculationEngine
import com.example.tdm.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

enum class AppTab(val title: String, val iconName: String) {
  CALCULATOR("Calculator", "calculate"),
  RESULTS("PK Results", "insights"),
  CAMERA("Lab Camera", "photo_camera"),
  EXPLANATION("Explanation", "menu_book"),
  SIMULATOR("Simulation", "science"),
  HISTORY("History", "history"),
  DISCLAIMER("Disclaimer", "policy")
}

class TdmViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("tdm_app_prefs", Context.MODE_PRIVATE)

  private val _themeMode = MutableStateFlow(
    when (prefs.getString("theme_mode", "SYSTEM")) {
      "LIGHT" -> ThemeMode.LIGHT
      "DARK" -> ThemeMode.DARK
      else -> ThemeMode.SYSTEM
    }
  )
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  fun setThemeMode(mode: ThemeMode) {
    _themeMode.value = mode
    prefs.edit().putString("theme_mode", mode.name).apply()
    viewModelScope.launch {
      _userMessage.emit("Theme switched to ${mode.label} mode")
    }
  }

  fun toggleTheme() {
    val next = when (_themeMode.value) {
      ThemeMode.LIGHT -> ThemeMode.DARK
      ThemeMode.DARK -> ThemeMode.LIGHT
      ThemeMode.SYSTEM -> ThemeMode.DARK
    }
    setThemeMode(next)
  }

  private val dao: CalculationHistoryDao = TdmDatabase.getDatabase(application).calculationHistoryDao()

  private val _activeTab = MutableStateFlow(AppTab.CALCULATOR)
  val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

  private val _input = MutableStateFlow(TdmInput(
    patient = Patient(patientName = "", patientId = "", ageYears = 0.0,
      heightCm = 0.0, weightKg = 0.0, serumCreatinine = 0.0),
    regimen = DosingRegimen(doseMg = 0.0, intervalHours = 0.0, infusionDurationHours = 0.0),
    preDoseConcMgL = null, preDoseSampleDelayHoursBeforeNextDose = null,
    postDoseConcMgL = null, postDoseSampleDelayHoursAfterInfusionEnd = null
  ))
  val input: StateFlow<TdmInput> = _input.asStateFlow()

  private val _validation = MutableStateFlow(TdmCalculationEngine.validate(_input.value))
  val validation: StateFlow<ValidationResult> = _validation.asStateFlow()

  private val _pkResult = MutableStateFlow<PkResult?>(null)
  val pkResult: StateFlow<PkResult?> = _pkResult.asStateFlow()

  // Attached Lab Report Image path for current calculation
  private val _attachedReportImagePath = MutableStateFlow<String?>(null)
  val attachedReportImagePath: StateFlow<String?> = _attachedReportImagePath.asStateFlow()

  // Camera & OCR state
  private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
  val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

  private val _extractedLabData = MutableStateFlow<ExtractedLabData?>(null)
  val extractedLabData: StateFlow<ExtractedLabData?> = _extractedLabData.asStateFlow()

  private val _isProcessingOcr = MutableStateFlow(false)
  val isProcessingOcr: StateFlow<Boolean> = _isProcessingOcr.asStateFlow()

  private val _ocrError = MutableStateFlow<String?>(null)
  val ocrError: StateFlow<String?> = _ocrError.asStateFlow()

  // Simulation state
  private val _simDose = MutableStateFlow(1000.0)
  val simDose: StateFlow<Double> = _simDose.asStateFlow()

  private val _simInterval = MutableStateFlow(12.0)
  val simInterval: StateFlow<Double> = _simInterval.asStateFlow()

  private val _simResult = MutableStateFlow<SimulationResult?>(null)
  val simResult: StateFlow<SimulationResult?> = _simResult.asStateFlow()

  private val _userMessage = MutableSharedFlow<String>()
  val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

  val historyList: StateFlow<List<CalculationHistoryEntity>> = dao.getAllHistory()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


  private val navigationHistory = mutableListOf<AppTab>()
  private val _canGoBack = MutableStateFlow(false)
  val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

  fun setTab(tab: AppTab) {
    if (tab == _activeTab.value) return
    navigationHistory.add(_activeTab.value)
    _activeTab.value = tab
    _canGoBack.value = true
  }

  fun goBack() {
    if (navigationHistory.isEmpty()) return
    _activeTab.value = navigationHistory.removeAt(navigationHistory.lastIndex)
    _canGoBack.value = navigationHistory.isNotEmpty()
  }

  fun setWorkflow(workflow: TdmWorkflow) {
    _input.update { it.copy(workflow = workflow) }
    validateCurrent()
  }

  fun updatePatient(updater: (Patient) -> Patient) {
    _input.update { it.copy(patient = updater(it.patient)) }
    validateCurrent()
  }

  fun updateRegimen(updater: (DosingRegimen) -> DosingRegimen) {
    _input.update { it.copy(regimen = updater(it.regimen)) }
    validateCurrent()
  }

  fun updatePreConc(conc: Double?, delay: Double?) {
    _input.update { it.copy(preDoseConcMgL = conc, preDoseSampleDelayHoursBeforeNextDose = delay) }
    validateCurrent()
  }

  fun updatePostConc(conc: Double?, delay: Double?) {
    _input.update { it.copy(postDoseConcMgL = conc, postDoseSampleDelayHoursAfterInfusionEnd = delay) }
    validateCurrent()
  }

  fun setTargetIndication(target: TargetIndication) {
    _input.update { it.copy(targetIndication = target) }
    validateCurrent()
  }

  fun setMic(mic: Double) {
    _input.update { it.copy(micMgL = mic) }
    validateCurrent()
  }

  fun loadFictionalCase(fictionalCase: FictionalCase) {
    _input.value = fictionalCase.input
    _attachedReportImagePath.value = null
    validateCurrent()
    executeCalculation(switchToResults = true)
    viewModelScope.launch {
      _userMessage.emit("Loaded: ${fictionalCase.title}")
    }
  }

  private fun validateCurrent(): ValidationResult {
    val res = TdmCalculationEngine.validate(_input.value)
    _validation.value = res
    return res
  }

  private val _isCalculating = MutableStateFlow(false)
  val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()
  private var calculationJob: Job? = null

  fun prepareResults() {
    if (_isCalculating.value || !validateCurrent().isValid) return
    _isCalculating.value = true
    calculationJob = viewModelScope.launch {
      try {
        // Short visual transition; no network request is involved.
        delay(650)
        executeCalculation(switchToResults = true)
      } finally {
        _isCalculating.value = false
      }
    }
  }

  fun cancelCalculation() {
    calculationJob?.cancel()
    _isCalculating.value = false
  }

  fun executeCalculation(switchToResults: Boolean = true) {
    val valResult = validateCurrent()
    if (!valResult.isValid) {
      viewModelScope.launch {
        _userMessage.emit("Cannot calculate: Please resolve input errors.")
      }
      return
    }

    val result = TdmCalculationEngine.calculate(_input.value)
    _pkResult.value = result

    // Initialize simulation parameters from calculated patient values
    _simDose.value = _input.value.regimen.doseMg
    _simInterval.value = _input.value.regimen.intervalHours
    recalculateSimulation(result)

    if (switchToResults) {
      setTab(AppTab.RESULTS)
    }
  }

  // --- CAMERA & OCR WORKFLOW (Capture -> Review -> Confirm -> Use in Calculation) ---

  fun onImageCaptured(bitmap: Bitmap) {
    _capturedBitmap.value = bitmap
    _isProcessingOcr.value = true
    _ocrError.value = null

    // Run ML Kit Text Recognition on the captured bitmap
    LabReportExtractor.processBitmapWithOcr(
      bitmap = bitmap,
      onSuccess = { extracted ->
        _extractedLabData.value = extracted
        _isProcessingOcr.value = false
        viewModelScope.launch {
          _userMessage.emit("Lab report scanned. Please review and confirm extracted values.")
        }
      },
      onFailure = { ex ->
        _isProcessingOcr.value = false
        _ocrError.value = "OCR scanning failed: ${ex.localizedMessage ?: "Unknown error"}. You can enter values manually."
      }
    )
  }

  fun loadSimulatedLabReport(index: Int) {
    val bitmap = when (index) {
      0 -> LabReportExtractor.generateSimulatedLabReportBitmap(
        hospitalName = "HOSPITAL CANSELOR TUANKU MUHRIZ (UKM)",
        patientName = "Encik Ahmad Razali",
        mrn = "HUKM-2026-881",
        serumCr = "85",
        troughConc = "12.4",
        peakConc = "28.5",
        dose = "1000",
        interval = "12"
      )
      1 -> LabReportExtractor.generateSimulatedLabReportBitmap(
        hospitalName = "HOSPITAL RAJA PEREMPUAN ZAINAB II (HRPZ)",
        patientName = "Puan Fatimah Daud",
        mrn = "HRPZ-2026-319",
        serumCr = "165",
        troughConc = "16.8",
        peakConc = null,
        dose = "750",
        interval = "24"
      )
      else -> LabReportExtractor.generateSimulatedLabReportBitmap(
        hospitalName = "HOSPITAL KUALA LUMPUR (HKL) - ICU",
        patientName = "Chong Wei Lun",
        mrn = "HKL-2026-904",
        serumCr = "52",
        troughConc = null,
        peakConc = "33.2",
        dose = "1500",
        interval = "12"
      )
    }
    onImageCaptured(bitmap)
  }

  fun confirmExtractedValues(
    confirmedData: ExtractedLabData,
    attachImage: Boolean
  ) {
    // 1. Save captured image to local app storage if requested
    var savedImagePath: String? = null
    if (attachImage && _capturedBitmap.value != null) {
      try {
        val file = File(getApplication<Application>().filesDir, "lab_report_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
          _capturedBitmap.value?.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        savedImagePath = file.absolutePath
        _attachedReportImagePath.value = savedImagePath
      } catch (e: Exception) {
        // Continue if saving image file fails
      }
    }

    // 2. Determine and adapt workflow dynamically based on available lab concentrations
    val targetWorkflow = when {
      confirmedData.preDoseConcMgL != null && confirmedData.postDoseConcMgL != null -> TdmWorkflow.PRE_POST
      confirmedData.preDoseConcMgL != null -> TdmWorkflow.PRE
      confirmedData.postDoseConcMgL != null -> TdmWorkflow.POST
      else -> _input.value.workflow
    }

    // 3. Update Patient demographics if extracted
    _input.update { current ->
      val updatedPatient = current.patient.copy(
        patientName = confirmedData.patientName ?: current.patient.patientName,
        patientId = confirmedData.patientId ?: current.patient.patientId,
        serumCreatinine = confirmedData.serumCreatinine ?: current.patient.serumCreatinine,
        creatinineUnit = confirmedData.creatinineUnit
      )

      val updatedRegimen = current.regimen.copy(
        doseMg = confirmedData.doseMg ?: current.regimen.doseMg,
        intervalHours = confirmedData.intervalHours ?: current.regimen.intervalHours
      )

      current.copy(
        workflow = targetWorkflow,
        patient = updatedPatient,
        regimen = updatedRegimen,
        preDoseConcMgL = confirmedData.preDoseConcMgL ?: current.preDoseConcMgL,
        postDoseConcMgL = confirmedData.postDoseConcMgL ?: current.postDoseConcMgL
      )
    }

    // 4. Validate & calculate
    validateCurrent()
    executeCalculation(switchToResults = true)

    viewModelScope.launch {
      _userMessage.emit("Confirmed values successfully imported from lab report!")
    }
  }

  fun clearCapturedImage() {
    _capturedBitmap.value = null
    _extractedLabData.value = null
    _ocrError.value = null
    _isProcessingOcr.value = false
  }

  // --- SIMULATION ---

  fun updateSimulationRegimen(dose: Double, interval: Double) {
    _simDose.value = dose
    _simInterval.value = interval
    val currentPk = _pkResult.value ?: return
    recalculateSimulation(currentPk)
  }

  private fun recalculateSimulation(pk: PkResult) {
    val sim = TdmCalculationEngine.simulateRegimen(
      kePerHour = pk.kePerHour,
      vdLiters = pk.vdLiters,
      testDoseMg = _simDose.value,
      testIntervalHours = _simInterval.value,
      infusionDurationHours = _input.value.regimen.infusionDurationHours,
      micMgL = _input.value.micMgL
    )
    _simResult.value = sim
  }

  // --- HISTORY & SHARING ---

  fun saveCurrentToHistory() {
    val res = _pkResult.value ?: return
    val inp = _input.value
    viewModelScope.launch {
      val entity = CalculationHistoryEntity(
        patientName = inp.patient.patientName,
        patientId = inp.patient.patientId,
        workflow = inp.workflow.title,
        doseMg = inp.regimen.doseMg,
        intervalHours = inp.regimen.intervalHours,
        infusionHours = inp.regimen.infusionDurationHours,
        crCl = res.crClMlPerMin,
        ke = res.kePerHour,
        halfLife = res.halfLifeHours,
        vd = res.vdLiters,
        cMax = res.cMaxMgL,
        cMin = res.cMinMgL,
        auc24 = res.auc24MgHPerL,
        aucStatus = res.aucStatus.label,
        recommendation = res.clinicalRecommendation,
        labReportImageUri = _attachedReportImagePath.value
      )
      dao.insert(entity)
      _userMessage.emit("Calculation and attached report saved to local history!")
    }
  }

  fun deleteHistoryItem(id: Long) {
    viewModelScope.launch {
      dao.deleteById(id)
      _userMessage.emit("History entry removed.")
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      dao.deleteAll()
      _userMessage.emit("History cleared.")
    }
  }

  fun formatShareSummary(): String {
    val res = _pkResult.value ?: return "No TDM result available."
    val inp = _input.value
    return buildString {
      appendLine("=== TDM INSIGHT CALCULATION REPORT ===")
      appendLine("Patient: ${inp.patient.patientName} (ID: ${inp.patient.patientId})")
      appendLine("Age: ${inp.patient.ageYears.toInt()}y | Gender: ${inp.patient.gender.label} | Weight: ${inp.patient.weightKg}kg | Height: ${inp.patient.heightCm}cm")
      appendLine("SrCr: ${inp.patient.serumCreatinine} ${inp.patient.creatinineUnit.symbol} | CrCl: ${String.format(Locale.US, "%.1f", res.crClMlPerMin)} mL/min")
      if (_attachedReportImagePath.value != null) {
        appendLine("Attached Lab Report: ${_attachedReportImagePath.value}")
      }
      appendLine("----------------------------------------")
      appendLine("Workflow: ${inp.workflow.title}")
      appendLine("Regimen: ${inp.regimen.doseMg.toInt()} mg IV over ${inp.regimen.infusionDurationHours}h every ${inp.regimen.intervalHours.toInt()}h")
      appendLine("----------------------------------------")
      appendLine("PHARMACOKINETIC PARAMETERS:")
      appendLine("• Ke: ${String.format(Locale.US, "%.4f", res.kePerHour)} h⁻¹")
      appendLine("• Half-life (t1/2): ${String.format(Locale.US, "%.1f", res.halfLifeHours)} h")
      appendLine("• Vd: ${String.format(Locale.US, "%.1f", res.vdLiters)} L (${String.format(Locale.US, "%.2f", res.vdPerKg)} L/kg)")
      appendLine("• Clearance (CL): ${String.format(Locale.US, "%.2f", res.clearanceLPerHr)} L/h (${String.format(Locale.US, "%.1f", res.clearanceMlPerMin)} mL/min)")
      appendLine("• True Peak (Cmax): ${String.format(Locale.US, "%.1f", res.cMaxMgL)} mg/L")
      appendLine("• True Trough (Cmin): ${String.format(Locale.US, "%.1f", res.cMinMgL)} mg/L [Status: ${res.troughStatus.label}]")
      appendLine("• AUC24: ${String.format(Locale.US, "%.1f", res.auc24MgHPerL)} mg·h/L [Status: ${res.aucStatus.label}]")
      appendLine("• AUC24/MIC: ${String.format(Locale.US, "%.1f", res.auc24OverMic)}")
      appendLine("----------------------------------------")
      appendLine("RECOMMENDATION:")
      appendLine(res.clinicalRecommendation)
      appendLine("----------------------------------------")
      appendLine("DISCLAIMER: For academic & software demonstration purposes only. TDM Insight prototype.")
    }
  }
}
