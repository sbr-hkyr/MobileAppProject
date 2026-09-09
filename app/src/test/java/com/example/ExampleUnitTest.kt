package com.example

import com.example.tdm.engine.TdmCalculationEngine
import com.example.tdm.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testPrePostCalculationEngine() {
    val input = FictionalCasesRepository.sampleCases[0].input
    val validation = TdmCalculationEngine.validate(input)
    assertTrue("Input should be valid", validation.isValid)

    val result = TdmCalculationEngine.calculate(input)
    assertNotNull(result)

    // Verify reasonable biological parameters
    assertTrue("Ke should be positive", result.kePerHour > 0.02 && result.kePerHour < 0.20)
    assertTrue("Half-life should be physiological", result.halfLifeHours > 3.0 && result.halfLifeHours < 25.0)
    assertTrue("Vd should be standard range", result.vdLiters > 25.0 && result.vdLiters < 100.0)
    assertTrue("AUC24 should be calculated", result.auc24MgHPerL > 200.0 && result.auc24MgHPerL < 1000.0)
    assertTrue("Cmax must exceed Cmin", result.cMaxMgL > result.cMinMgL)
    assertTrue("Detailed steps must have all 4 categories", result.detailedSteps.size >= 8)
  }

  @Test
  fun testPreOnlyWorkflow() {
    val input = FictionalCasesRepository.sampleCases[1].input
    val validation = TdmCalculationEngine.validate(input)
    assertTrue(validation.isValid)

    val result = TdmCalculationEngine.calculate(input)
    assertEquals(TdmWorkflow.PRE, result.workflow)
    assertTrue(result.cMinMgL > 0)
    assertTrue(result.cMaxMgL > result.cMinMgL)
  }

  @Test
  fun testPostOnlyWorkflow() {
    val input = FictionalCasesRepository.sampleCases[2].input
    val validation = TdmCalculationEngine.validate(input)
    assertTrue(validation.isValid)

    val result = TdmCalculationEngine.calculate(input)
    assertEquals(TdmWorkflow.POST, result.workflow)
    assertTrue(result.cMaxMgL > 0)
    assertTrue(result.cMinMgL > 0)
  }

  @Test
  fun testCrossfieldValidationCatchesInvertedConcentrations() {
    val invalidInput = TdmInput(
      workflow = TdmWorkflow.PRE_POST,
      postDoseConcMgL = 10.0,
      preDoseConcMgL = 25.0 // Trough higher than peak!
    )
    val validation = TdmCalculationEngine.validate(invalidInput)
    assertFalse("Validation must fail when peak <= trough", validation.isValid)
    assertTrue(validation.errors.any { it.field == "crossfieldConc" })
  }

  @Test
  fun testWhatIfSimulation() {
    val sim = TdmCalculationEngine.simulateRegimen(
      kePerHour = 0.055,
      vdLiters = 45.0,
      testDoseMg = 1000.0,
      testIntervalHours = 12.0
    )
    assertTrue(sim.cMaxMgL > sim.cMinMgL)
    assertTrue(sim.auc24MgHPerL > 0)
  }

  @Test
  fun testLabReportTextExtraction() {
    val sampleLabText = """
      HOSPITAL CANSELOR TUANKU MUHRIZ (UKM)
      PATIENT DEMOGRAPHICS
      Name: Encik Ahmad Razali
      MRN: HUKM-2026-881
      Dose: 1000 mg IV  Interval: q12h
      -----------------------------------------
      TEST PARAMETER         RESULT        REF
      Serum Creatinine       Creatinine: 85 umol/L
      Vancomycin Pre-dose    Trough: 12.4 mg/L
      Vancomycin Post-dose   Peak: 28.5 mg/L
    """.trimIndent()

    val extracted = com.example.tdm.camera.LabReportExtractor.parseClinicalText(sampleLabText)
    assertEquals("Encik Ahmad Razali", extracted.patientName)
    assertEquals("HUKM-2026-881", extracted.patientId)
    assertEquals(85.0, extracted.serumCreatinine ?: 0.0, 0.001)
    assertEquals(com.example.tdm.model.CreatinineUnit.UMOL_L, extracted.creatinineUnit)
    assertEquals(12.4, extracted.preDoseConcMgL ?: 0.0, 0.001)
    assertEquals(28.5, extracted.postDoseConcMgL ?: 0.0, 0.001)
    assertEquals(1000.0, extracted.doseMg ?: 0.0, 0.001)
    assertEquals(12.0, extracted.intervalHours ?: 0.0, 0.001)
  }

  @Test
  fun testThemeModeEnumValues() {
    val modes = ThemeMode.values()
    assertEquals(3, modes.size)
    assertTrue(modes.contains(ThemeMode.SYSTEM))
    assertTrue(modes.contains(ThemeMode.LIGHT))
    assertTrue(modes.contains(ThemeMode.DARK))
    assertEquals("System", ThemeMode.SYSTEM.label)
    assertEquals("Light", ThemeMode.LIGHT.label)
    assertEquals("Dark", ThemeMode.DARK.label)
  }
}
