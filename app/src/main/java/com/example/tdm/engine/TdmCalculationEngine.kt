package com.example.tdm.engine

import com.example.tdm.model.*
import java.util.Locale
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt

object TdmCalculationEngine {

  /**
   * Validates the input form state thoroughly, returning fatal errors that block
   * calculation and non-fatal clinical warnings that prompt review.
   */
  fun validate(input: TdmInput): ValidationResult {
    val errors = mutableListOf<ValidationError>()
    val warnings = mutableListOf<ValidationWarning>()

    val p = input.patient
    val r = input.regimen

    // Patient demographics
    if (p.ageYears !in 1.0..120.0) {
      errors.add(ValidationError("age", "Age must be between 1 and 120 years."))
    }
    if (p.heightCm !in 50.0..250.0) {
      errors.add(ValidationError("height", "Height must be between 50 and 250 cm."))
    }
    if (p.weightKg !in 10.0..350.0) {
      errors.add(ValidationError("weight", "Weight must be between 10 and 350 kg."))
    }

    val crNormalized = if (p.creatinineUnit == CreatinineUnit.MG_DL) {
      p.serumCreatinine * 88.4
    } else {
      p.serumCreatinine
    }
    if (crNormalized <= 5.0 || crNormalized > 2000.0) {
      errors.add(ValidationError("creatinine", "Serum creatinine value is outside biological range."))
    } else if (crNormalized > 300.0) {
      warnings.add(ValidationWarning("creatinine", "High serum creatinine (${String.format(Locale.US, "%.1f", crNormalized)} µmol/L) indicates severe renal impairment. Close monitoring advised."))
    }

    // Regimen validation
    if (r.doseMg !in 100.0..4000.0) {
      errors.add(ValidationError("dose", "Dose must be between 100 and 4000 mg."))
    }
    if (r.intervalHours !in 6.0..72.0) {
      errors.add(ValidationError("interval", "Dosing interval must be between 6 and 72 hours."))
    }
    if (r.infusionDurationHours <= 0.0 || r.infusionDurationHours > 6.0) {
      errors.add(ValidationError("infusionDuration", "Infusion duration must be between 0.5 and 6 hours."))
    }
    if (r.infusionDurationHours >= r.intervalHours) {
      errors.add(ValidationError("infusionDuration", "Infusion duration (${r.infusionDurationHours}h) cannot equal or exceed interval (${r.intervalHours}h)."))
    }

    // Infusion rate safety check (standard guideline: <= 1000 mg/hr to prevent Red Man Syndrome)
    val infusionRateMgPerHr = if (r.infusionDurationHours > 0) r.doseMg / r.infusionDurationHours else 0.0
    if (infusionRateMgPerHr > 1000.0) {
      warnings.add(ValidationWarning("infusionRate", "Infusion rate (${String.format(Locale.US, "%.0f", infusionRateMgPerHr)} mg/hr) exceeds recommended 1000 mg/hr. Risk of infusion reaction (Red Man Syndrome)."))
    }

    if (input.workflow != TdmWorkflow.POST && input.preDoseSampleDelayHoursBeforeNextDose == null) {
      errors.add(ValidationError("preDelay", "Enter the pre-dose sampling time."))
    }
    if (input.workflow != TdmWorkflow.PRE && input.postDoseSampleDelayHoursAfterInfusionEnd == null) {
      errors.add(ValidationError("postDelay", "Enter the post-dose sampling time."))
    }

    // Workflow-specific lab concentration and timing validation
    when (input.workflow) {
      TdmWorkflow.PRE -> {
        val pre = input.preDoseConcMgL
        val preDelay = input.preDoseSampleDelayHoursBeforeNextDose ?: 0.0
        if (pre == null || pre <= 0.0) {
          errors.add(ValidationError("preConc", "Pre-dose concentration is required and must be > 0 mg/L."))
        } else if (pre > 100.0) {
          warnings.add(ValidationWarning("preConc", "Unusually high pre-dose concentration (${pre} mg/L). Confirm assay accuracy."))
        }
        if (preDelay < 0.0 || preDelay >= (r.intervalHours - r.infusionDurationHours)) {
          errors.add(ValidationError("preDelay", "Pre-dose sampling time must be within 0 to ${r.intervalHours - r.infusionDurationHours}h before next dose."))
        }
      }

      TdmWorkflow.POST -> {
        val post = input.postDoseConcMgL
        val postDelay = input.postDoseSampleDelayHoursAfterInfusionEnd ?: 1.0
        if (post == null || post <= 0.0) {
          errors.add(ValidationError("postConc", "Post-dose concentration is required and must be > 0 mg/L."))
        } else if (post > 120.0) {
          warnings.add(ValidationWarning("postConc", "Extreme peak concentration (${post} mg/L). Verify assay dilution and timing."))
        }
        if (postDelay < 0.5) {
          warnings.add(ValidationWarning("postDelay", "Post-dose sample taken < 1h after infusion end may still be in the distribution phase (alpha phase). 1-2 hours is recommended."))
        }
        if (postDelay >= (r.intervalHours - r.infusionDurationHours)) {
          errors.add(ValidationError("postDelay", "Post-dose sampling delay cannot exceed elimination time window."))
        }
      }

      TdmWorkflow.PRE_POST -> {
        val pre = input.preDoseConcMgL
        val post = input.postDoseConcMgL
        val preDelay = input.preDoseSampleDelayHoursBeforeNextDose ?: 0.5
        val postDelay = input.postDoseSampleDelayHoursAfterInfusionEnd ?: 1.0

        if (pre == null || pre <= 0.0) {
          errors.add(ValidationError("preConc", "Pre-dose concentration is required for Pre+Post workflow."))
        }
        if (post == null || post <= 0.0) {
          errors.add(ValidationError("postConc", "Post-dose concentration is required for Pre+Post workflow."))
        }

        if (pre != null && post != null && pre > 0.0 && post > 0.0) {
          if (post <= pre) {
            errors.add(ValidationError("crossfieldConc", "Peak concentration ($post mg/L) must be greater than trough concentration ($pre mg/L)."))
          }

          val deltaT = (r.intervalHours - r.infusionDurationHours - postDelay - preDelay)
          if (deltaT <= 0.5) {
            errors.add(ValidationError("samplingTimes", "Elapsed time between post-sample and pre-sample (Δt = ${String.format(Locale.US, "%.2f", deltaT)}h) is too short for reliable Ke estimation."))
          }
        }
      }
    }

    return ValidationResult(
      isValid = errors.isEmpty(),
      errors = errors,
      warnings = warnings
    )
  }

  /**
   * Main calculation engine method. Takes validated inputs and produces a comprehensive
   * PkResult model with intermediate calculations, pharmacokinetic parameters, and explainable steps.
   */
  fun calculate(input: TdmInput): PkResult {
    val p = input.patient
    val r = input.regimen
    val steps = mutableListOf<CalculationStep>()
    var stepCounter = 1

    // Step 1: Normalize Serum Creatinine to µmol/L
    val scrUmolL = if (p.creatinineUnit == CreatinineUnit.MG_DL) {
      p.serumCreatinine * 88.4
    } else {
      p.serumCreatinine
    }
    steps.add(
      CalculationStep(
        stepNumber = stepCounter++,
        category = StepCategory.INPUT_BASELINE,
        title = "Serum Creatinine Standardization",
        formula = "Standard Unit = µmol/L (Conversion factor: 1 mg/dL = 88.4 µmol/L)",
        substitution = "${p.serumCreatinine} ${p.creatinineUnit.symbol}",
        result = "${String.format(Locale.US, "%.1f", scrUmolL)} µmol/L",
        notes = "Clinical equations in Malaysia MOH / PhIS reference use µmol/L."
      )
    )

    // Step 2: Ideal Body Weight (IBW) & Adjusted Body Weight (AdjBW)
    // Devine formula:
    // Male: 50.0 + 0.9 * (Height - 152.0)
    // Female: 45.5 + 0.9 * (Height - 152.0)
    val heightOffset = max(0.0, p.heightCm - 152.0)
    val ibw = if (p.gender == Gender.MALE) {
      50.0 + 0.9 * heightOffset
    } else {
      45.5 + 0.9 * heightOffset
    }

    val isObese = p.weightKg > 1.2 * ibw
    val adjBw = if (isObese) {
      ibw + 0.4 * (p.weightKg - ibw)
    } else null

    // Dosing weight:
    // If underweight (actual < IBW), use actual weight.
    // If obese (>120% IBW), use AdjBW.
    // Otherwise use actual weight or IBW.
    val dosingWeight = when {
      p.weightKg < ibw -> p.weightKg
      isObese -> adjBw ?: ibw
      else -> p.weightKg
    }

    val ibwFormula = if (p.gender == Gender.MALE) {
      "IBW = 50 + 0.9 × (Height - 152 cm)"
    } else {
      "IBW = 45.5 + 0.9 × (Height - 152 cm)"
    }

    steps.add(
      CalculationStep(
        stepNumber = stepCounter++,
        category = StepCategory.INPUT_BASELINE,
        title = "Ideal Body Weight & Dosing Weight Determination",
        formula = "$ibwFormula | If Weight > 120% IBW, AdjBW = IBW + 0.4 × (ABW - IBW)",
        substitution = "Height: ${p.heightCm} cm, Weight: ${p.weightKg} kg (IBW = ${String.format(Locale.US, "%.1f", ibw)} kg)",
        result = "IBW: ${String.format(Locale.US, "%.1f", ibw)} kg | Dosing Weight: ${String.format(Locale.US, "%.1f", dosingWeight)} kg ${if (isObese) "(Adjusted for Obesity)" else ""}",
        notes = if (isObese) "Patient is obese (>120% IBW). Adjusted Body Weight (AdjBW) is applied." else "Standard dosing weight applied."
      )
    )

    // Step 3: Creatinine Clearance (Cockcroft-Gault)
    // CrCl (mL/min) = [(140 - Age) * Weight] / [0.814 * Scr] * (0.85 if female)
    val weightForCrCl = if (isObese) (adjBw ?: ibw) else p.weightKg
    val genderMultiplier = if (p.gender == Gender.FEMALE) 0.85 else 1.0
    val crCl = ((140.0 - p.ageYears) * weightForCrCl / (0.814 * scrUmolL)) * genderMultiplier

    steps.add(
      CalculationStep(
        stepNumber = stepCounter++,
        category = StepCategory.INPUT_BASELINE,
        title = "Creatinine Clearance (Cockcroft-Gault)",
        formula = "CrCl = [(140 - Age) × Weight (kg)] / [0.814 × SrCr (µmol/L)] × (0.85 if Female)",
        substitution = "[(140 - ${p.ageYears.toInt()}) × ${String.format(Locale.US, "%.1f", weightForCrCl)}] / [0.814 × ${String.format(Locale.US, "%.1f", scrUmolL)}] × $genderMultiplier",
        result = "${String.format(Locale.US, "%.1f", crCl)} mL/min",
        notes = "Determines patient renal function baseline and population elimination rate constant."
      )
    )

    // Step 4: Population Estimates (Prior / Reference)
    val kePop = 0.00083 * crCl + 0.0044
    val vdPop = 0.7 * dosingWeight

    // Calculate specific PK based on Workflow
    val ke: Double
    val vd: Double
    val cMax: Double
    val cMin: Double

    when (input.workflow) {
      TdmWorkflow.PRE_POST -> {
        val cPost = input.postDoseConcMgL ?: 25.0
        val cPre = input.preDoseConcMgL ?: 10.0
        val tPost = input.postDoseSampleDelayHoursAfterInfusionEnd ?: 1.0
        val tPre = input.preDoseSampleDelayHoursBeforeNextDose ?: 0.5

        // Elimination interval between the two points:
        // t1 = r.infusionDurationHours + tPost
        // t2 = r.intervalHours - tPre
        // deltaT = t2 - t1
        val deltaT = max(0.1, r.intervalHours - r.infusionDurationHours - tPost - tPre)

        // Individual Ke
        ke = ln(cPost / cPre) / deltaT

        // Extrapolated Peak at end of infusion (t_inf)
        cMax = cPost * exp(ke * tPost)

        // Extrapolated Trough at end of interval (tau)
        cMin = cPre * exp(-ke * tPre)

        // Individual Vd via Sawchuk-Zaske steady state equation:
        // Vd = [Dose * (1 - e^(-ke * t_inf))] / [t_inf * ke * (Cmax - Cmin * e^(-ke * t_inf))]
        val numerator = r.doseMg * (1.0 - exp(-ke * r.infusionDurationHours))
        val denominator = r.infusionDurationHours * ke * (cMax - (cMin * exp(-ke * r.infusionDurationHours)))
        vd = if (denominator > 0) numerator / denominator else vdPop

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.INTERMEDIATE,
            title = "Two-Point Elimination Time Window (Δt)",
            formula = "Δt = τ - t_inf - t_post - t_pre",
            substitution = "${r.intervalHours}h - ${r.infusionDurationHours}h - ${tPost}h - ${tPre}h",
            result = "${String.format(Locale.US, "%.2f", deltaT)} hours",
            notes = "Exact elimination duration elapsed between post-dose sample and pre-dose sample."
          )
        )

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.PHARMACOKINETICS,
            title = "Individual Elimination Rate Constant (Ke)",
            formula = "Ke = ln(C_post / C_pre) / Δt",
            substitution = "ln(${String.format(Locale.US, "%.1f", cPost)} / ${String.format(Locale.US, "%.1f", cPre)}) / ${String.format(Locale.US, "%.2f", deltaT)}",
            result = "${String.format(Locale.US, "%.4f", ke)} h⁻¹",
            notes = "Patient-specific first-order elimination rate constant derived from Sawchuk-Zaske method."
          )
        )

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.PHARMACOKINETICS,
            title = "True Peak & Trough Extrapolation",
            formula = "C_max = C_post × e^(Ke × t_post)  |  C_min = C_pre × e^(-Ke × t_pre)",
            substitution = "C_max = ${cPost} × e^(${String.format(Locale.US, "%.4f", ke)} × $tPost) | C_min = ${cPre} × e^(-${String.format(Locale.US, "%.4f", ke)} × $tPre)",
            result = "C_max: ${String.format(Locale.US, "%.1f", cMax)} mg/L  |  C_min: ${String.format(Locale.US, "%.1f", cMin)} mg/L",
            notes = "True end-of-infusion peak and end-of-dosing-interval trough concentrations."
          )
        )

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.PHARMACOKINETICS,
            title = "Individual Volume of Distribution (Vd)",
            formula = "Vd = [Dose × (1 - e^(-Ke × t_inf))] / [t_inf × Ke × (C_max - C_min × e^(-Ke × t_inf))]",
            substitution = "[${r.doseMg.toInt()} × (1 - e^(-${String.format(Locale.US, "%.4f", ke)} × ${r.infusionDurationHours}))] / [${r.infusionDurationHours} × ${String.format(Locale.US, "%.4f", ke)} × (${String.format(Locale.US, "%.1f", cMax)} - ${String.format(Locale.US, "%.1f", cMin)} × e^(-${String.format(Locale.US, "%.4f", ke)} × ${r.infusionDurationHours}))]",
            result = "${String.format(Locale.US, "%.1f", vd)} L (${String.format(Locale.US, "%.2f", vd / dosingWeight)} L/kg)",
            notes = "Standard population reference for Vancomycin Vd is 0.5 to 1.0 L/kg."
          )
        )
      }

      TdmWorkflow.PRE -> {
        val cPre = input.preDoseConcMgL ?: 12.0
        val tPre = input.preDoseSampleDelayHoursBeforeNextDose ?: 0.5

        // In Pre-only workflow, Ke is estimated from renal function:
        ke = kePop
        cMin = cPre * exp(-ke * tPre)

        // Project Cmax at steady state:
        // Cmin = Cmax * e^(-ke * (tau - t_inf))  =>  Cmax = Cmin / e^(-ke * (tau - t_inf))
        val elimTime = max(0.1, r.intervalHours - r.infusionDurationHours)
        cMax = cMin / exp(-ke * elimTime)

        // Calculate individual Vd from steady state
        val numerator = r.doseMg * (1.0 - exp(-ke * r.infusionDurationHours))
        val denominator = r.infusionDurationHours * ke * (cMax - (cMin * exp(-ke * r.infusionDurationHours)))
        vd = if (denominator > 0) numerator / denominator else vdPop

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.INTERMEDIATE,
            title = "Population Ke Estimation from Renal Function",
            formula = "Ke = 0.00083 × CrCl + 0.0044",
            substitution = "0.00083 × ${String.format(Locale.US, "%.1f", crCl)} + 0.0044",
            result = "${String.format(Locale.US, "%.4f", ke)} h⁻¹",
            notes = "Pre-dose only workflow estimates Ke from patient's Cockcroft-Gault creatinine clearance."
          )
        )

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.PHARMACOKINETICS,
            title = "Steady-State Trough and Back-Extrapolated Peak",
            formula = "C_min = C_pre × e^(-Ke × t_pre)  |  C_max = C_min / e^(-Ke × (τ - t_inf))",
            substitution = "C_min = ${cPre} × e^(-${String.format(Locale.US, "%.4f", ke)} × $tPre)  |  C_max = ${String.format(Locale.US, "%.1f", cMin)} / e^(-${String.format(Locale.US, "%.4f", ke)} × ${elimTime})",
            result = "C_min: ${String.format(Locale.US, "%.1f", cMin)} mg/L  |  C_max: ${String.format(Locale.US, "%.1f", cMax)} mg/L",
            notes = "True end-of-interval trough and projected end-of-infusion peak."
          )
        )

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.PHARMACOKINETICS,
            title = "Volume of Distribution (Vd)",
            formula = "Vd = [Dose × (1 - e^(-Ke × t_inf))] / [t_inf × Ke × (C_max - C_min × e^(-Ke × t_inf))]",
            substitution = "Calculated using estimated C_max and C_min at steady-state",
            result = "${String.format(Locale.US, "%.1f", vd)} L (${String.format(Locale.US, "%.2f", vd / dosingWeight)} L/kg)",
            notes = "Patient volume of distribution consistent with observed trough."
          )
        )
      }

      TdmWorkflow.POST -> {
        val cPost = input.postDoseConcMgL ?: 28.0
        val tPost = input.postDoseSampleDelayHoursAfterInfusionEnd ?: 1.0

        // In Post-only workflow, Ke is estimated from renal function:
        ke = kePop
        cMax = cPost * exp(ke * tPost)

        val elimTime = max(0.1, r.intervalHours - r.infusionDurationHours)
        cMin = cMax * exp(-ke * elimTime)

        val numerator = r.doseMg * (1.0 - exp(-ke * r.infusionDurationHours))
        val denominator = r.infusionDurationHours * ke * (cMax - (cMin * exp(-ke * r.infusionDurationHours)))
        vd = if (denominator > 0) numerator / denominator else vdPop

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.INTERMEDIATE,
            title = "Population Ke Estimation from Renal Function",
            formula = "Ke = 0.00083 × CrCl + 0.0044",
            substitution = "0.00083 × ${String.format(Locale.US, "%.1f", crCl)} + 0.0044",
            result = "${String.format(Locale.US, "%.4f", ke)} h⁻¹",
            notes = "Post-dose only workflow estimates Ke from patient's Cockcroft-Gault creatinine clearance."
          )
        )

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.PHARMACOKINETICS,
            title = "True Peak Extrapolation and Projected Trough",
            formula = "C_max = C_post × e^(Ke × t_post)  |  C_min = C_max × e^(-Ke × (τ - t_inf))",
            substitution = "C_max = ${cPost} × e^(${String.format(Locale.US, "%.4f", ke)} × $tPost) | C_min = ${String.format(Locale.US, "%.1f", cMax)} × e^(-${String.format(Locale.US, "%.4f", ke)} × ${elimTime})",
            result = "C_max: ${String.format(Locale.US, "%.1f", cMax)} mg/L  |  C_min: ${String.format(Locale.US, "%.1f", cMin)} mg/L",
            notes = "True end-of-infusion peak and projected end-of-interval trough."
          )
        )

        steps.add(
          CalculationStep(
            stepNumber = stepCounter++,
            category = StepCategory.PHARMACOKINETICS,
            title = "Volume of Distribution (Vd)",
            formula = "Vd = [Dose × (1 - e^(-Ke × t_inf))] / [t_inf × Ke × (C_max - C_min × e^(-Ke × t_inf))]",
            substitution = "Calculated using extrapolated peak and projected trough",
            result = "${String.format(Locale.US, "%.1f", vd)} L (${String.format(Locale.US, "%.2f", vd / dosingWeight)} L/kg)",
            notes = "Patient volume of distribution consistent with observed peak."
          )
        )
      }
    }

    // Step: Half-Life
    val halfLife = if (ke > 0) ln(2.0) / ke else 0.0
    steps.add(
      CalculationStep(
        stepNumber = stepCounter++,
        category = StepCategory.PHARMACOKINETICS,
        title = "Elimination Half-Life (t₁/₂)",
        formula = "t₁/₂ = ln(2) / Ke = 0.693 / Ke",
        substitution = "0.69315 / ${String.format(Locale.US, "%.4f", ke)}",
        result = "${String.format(Locale.US, "%.1f", halfLife)} hours",
        notes = "Time required for serum vancomycin concentration to decrease by 50%."
      )
    )

    // Step: Clearance
    val clLPerHr = ke * vd
    val clMlPerMin = clLPerHr * 1000.0 / 60.0
    steps.add(
      CalculationStep(
        stepNumber = stepCounter++,
        category = StepCategory.PHARMACOKINETICS,
        title = "Drug Clearance (CL)",
        formula = "CL = Ke × Vd  |  CL (mL/min) = CL (L/h) × 1000 / 60",
        substitution = "${String.format(Locale.US, "%.4f", ke)} h⁻¹ × ${String.format(Locale.US, "%.1f", vd)} L",
        result = "${String.format(Locale.US, "%.2f", clLPerHr)} L/h (${String.format(Locale.US, "%.1f", clMlPerMin)} mL/min)",
        notes = "Volume of blood cleared of vancomycin per unit time."
      )
    )

    // Step: AUC24 Calculation
    // AUC_tau = Dose / CL
    // AUC_24 = AUC_tau * (24 / tau) = Daily Dose / CL
    val aucTau = if (clLPerHr > 0) r.doseMg / clLPerHr else 0.0
    val dosesPerDay = 24.0 / r.intervalHours
    val dailyDoseMg = r.doseMg * dosesPerDay
    val auc24 = aucTau * dosesPerDay
    val auc24OverMic = if (input.micMgL > 0) auc24 / input.micMgL else auc24

    steps.add(
      CalculationStep(
        stepNumber = stepCounter++,
        category = StepCategory.FINAL_OUTCOMES,
        title = "24-Hour Area Under the Curve (AUC₂₄)",
        formula = "AUC_τ = Dose / CL  |  AUC₂₄ = AUC_τ × (24 / τ) = Total Daily Dose / CL",
        substitution = "(${r.doseMg.toInt()} mg / ${String.format(Locale.US, "%.2f", clLPerHr)} L/h) × (24 / ${r.intervalHours.toInt()}) = ${dailyDoseMg.toInt()} mg / ${String.format(Locale.US, "%.2f", clLPerHr)} L/h",
        result = "${String.format(Locale.US, "%.1f", auc24)} mg·h/L",
        notes = "Primary pharmacodynamic index predictive of vancomycin clinical efficacy and nephrotoxicity risk."
      )
    )

    steps.add(
      CalculationStep(
        stepNumber = stepCounter++,
        category = StepCategory.FINAL_OUTCOMES,
        title = "AUC₂₄ / MIC Ratio",
        formula = "AUC₂₄ / MIC (MIC assumed = ${input.micMgL} mg/L)",
        substitution = "${String.format(Locale.US, "%.1f", auc24)} / ${input.micMgL}",
        result = "${String.format(Locale.US, "%.1f", auc24OverMic)}",
        notes = "Consensus guidelines target: 400 to 600 mg·h/L for MRSA infections."
      )
    )

    // Therapeutic Status assessment
    val aucStatus = when {
      auc24 < 400.0 -> TherapeuticStatus.SUBTHERAPEUTIC
      auc24 in 400.0..600.0 -> TherapeuticStatus.THERAPEUTIC
      else -> TherapeuticStatus.SUPRATHERAPEUTIC
    }

    val (minTargetTrough, maxTargetTrough) = if (input.targetIndication == TargetIndication.SEVERE_MRSA) {
      Pair(15.0, 20.0)
    } else {
      Pair(10.0, 15.0)
    }

    val troughStatus = when {
      cMin < minTargetTrough -> TherapeuticStatus.SUBTHERAPEUTIC
      cMin in minTargetTrough..maxTargetTrough -> TherapeuticStatus.THERAPEUTIC
      else -> TherapeuticStatus.SUPRATHERAPEUTIC
    }

    val recommendation = generateRecommendation(
      aucStatus = aucStatus,
      troughStatus = troughStatus,
      auc24 = auc24,
      cMin = cMin,
      cMax = cMax,
      interval = r.intervalHours,
      dose = r.doseMg,
      targetIndication = input.targetIndication
    )

    return PkResult(
      workflow = input.workflow,
      ibwKg = ibw,
      isObese = isObese,
      adjBwKg = adjBw,
      dosingWeightKg = dosingWeight,
      crClMlPerMin = crCl,
      serumCreatinineUmolL = scrUmolL,
      kePerHour = ke,
      halfLifeHours = halfLife,
      vdLiters = vd,
      vdPerKg = vd / dosingWeight,
      clearanceLPerHr = clLPerHr,
      clearanceMlPerMin = clMlPerMin,
      cMaxMgL = cMax,
      cMinMgL = cMin,
      measuredPostSampleConc = input.postDoseConcMgL,
      measuredPreSampleConc = input.preDoseConcMgL,
      aucTauMgHPerL = aucTau,
      auc24MgHPerL = auc24,
      micMgL = input.micMgL,
      auc24OverMic = auc24OverMic,
      aucStatus = aucStatus,
      troughStatus = troughStatus,
      clinicalRecommendation = recommendation,
      detailedSteps = steps
    )
  }

  /**
   * Generates clear, professional clinical recommendations based on guideline targets.
   */
  private fun generateRecommendation(
    aucStatus: TherapeuticStatus,
    troughStatus: TherapeuticStatus,
    auc24: Double,
    cMin: Double,
    cMax: Double,
    interval: Double,
    dose: Double,
    targetIndication: TargetIndication
  ): String {
    return when {
      aucStatus == TherapeuticStatus.THERAPEUTIC && troughStatus == TherapeuticStatus.THERAPEUTIC -> {
        "Optimal therapeutic exposure achieved. Current AUC₂₄ (${String.format(Locale.US, "%.0f", auc24)} mg·h/L) and trough (${String.format(Locale.US, "%.1f", cMin)} mg/L) are both within the target therapeutic window. Maintain current regimen (${dose.toInt()} mg q${interval.toInt()}h) with routine therapeutic monitoring."
      }

      aucStatus == TherapeuticStatus.SUBTHERAPEUTIC -> {
        val suggestedDose = ((450.0 / auc24) * dose / 250.0).roundToInt() * 250
        "Sub-therapeutic exposure detected (AUC₂₄ < 400 mg·h/L). High risk of clinical failure or antimicrobial resistance. Consider increasing total daily dose (e.g. titrate to ~${suggestedDose} mg q${interval.toInt()}h or shortening the dosing interval), and repeat TDM monitoring at steady-state."
      }

      aucStatus == TherapeuticStatus.SUPRATHERAPEUTIC -> {
        val suggestedDose = ((500.0 / auc24) * dose / 250.0).roundToInt() * 250
        "Supra-therapeutic exposure detected (AUC₂₄ > 600 mg·h/L). Elevated risk of vancomycin-induced nephrotoxicity (acute kidney injury). Strongly recommend dose reduction (e.g. titrate to ~${suggestedDose} mg q${interval.toInt()}h) or extending the dosing interval. Re-check serum creatinine and trough levels."
      }

      cMin > 20.0 -> {
        "Elevated trough level (${String.format(Locale.US, "%.1f", cMin)} mg/L > 20 mg/L) presents heightened nephrotoxicity risk. Even if AUC is acceptable, consider extending dosing interval or withholding the next dose until trough drops below target."
      }

      else -> {
        "Target trough for ${targetIndication.label} is ${targetIndication.targetTroughRange}. Current trough is ${String.format(Locale.US, "%.1f", cMin)} mg/L with AUC₂₄ ${String.format(Locale.US, "%.0f", auc24)} mg·h/L. Review clinical response, renal function trends, and infection severity before adjusting regimen."
      }
    }
  }

  /**
   * Simulates steady-state outcomes for any test regimen given known patient Ke and Vd.
   */
  fun simulateRegimen(
    kePerHour: Double,
    vdLiters: Double,
    testDoseMg: Double,
    testIntervalHours: Double,
    infusionDurationHours: Double = 1.0,
    micMgL: Double = 1.0
  ): SimulationResult {
    val clLPerHr = kePerHour * vdLiters
    val numerator = testDoseMg * (1.0 - exp(-kePerHour * infusionDurationHours))
    val denominator = infusionDurationHours * kePerHour * vdLiters * (1.0 - exp(-kePerHour * testIntervalHours))
    val cMax = if (denominator > 0) numerator / denominator else 0.0

    val elimTime = max(0.1, testIntervalHours - infusionDurationHours)
    val cMin = cMax * exp(-kePerHour * elimTime)

    val dailyDoseMg = testDoseMg * (24.0 / testIntervalHours)
    val auc24 = if (clLPerHr > 0) dailyDoseMg / clLPerHr else 0.0

    val aucStatus = when {
      auc24 < 400.0 -> TherapeuticStatus.SUBTHERAPEUTIC
      auc24 in 400.0..600.0 -> TherapeuticStatus.THERAPEUTIC
      else -> TherapeuticStatus.SUPRATHERAPEUTIC
    }

    return SimulationResult(
      doseMg = testDoseMg,
      intervalHours = testIntervalHours,
      cMaxMgL = cMax,
      cMinMgL = cMin,
      auc24MgHPerL = auc24,
      aucStatus = aucStatus
    )
  }
}

data class SimulationResult(
  val doseMg: Double,
  val intervalHours: Double,
  val cMaxMgL: Double,
  val cMinMgL: Double,
  val auc24MgHPerL: Double,
  val aucStatus: TherapeuticStatus
)
