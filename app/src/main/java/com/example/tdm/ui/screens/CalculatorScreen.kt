package com.example.tdm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdm.model.*
import com.example.tdm.viewmodel.TdmViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  viewModel: TdmViewModel,
  modifier: Modifier = Modifier
) {
  val input by viewModel.input.collectAsState()
  val validation by viewModel.validation.collectAsState()
  val isCalculating by viewModel.isCalculating.collectAsState()
  if (isCalculating) {
    AlertDialog(
      onDismissRequest = { viewModel.cancelCalculation() },
      icon = { CircularProgressIndicator(modifier = Modifier.size(32.dp)) },
      title = { Text("Preparing results…") },
      text = { Text("Calculating your pharmacokinetic results.") },
      confirmButton = {},
      modifier = Modifier.testTag("calculation_loading_dialog")
    )
  }
  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()
  var showCases by remember { mutableStateOf(false) }
  var attempted by remember { mutableStateOf(false) }
  if (showCases) {
    AlertDialog(onDismissRequest = { showCases = false },
      title = { Text("Load a fictional example") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          FictionalCasesRepository.sampleCases.forEachIndexed { index, item ->
            OutlinedButton(onClick = { viewModel.loadFictionalCase(item); viewModel.setTab(com.example.tdm.viewmodel.AppTab.CALCULATOR); showCases = false },
              modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("load_case_${index + 1}_chip")) {
              Text("Case ${index + 1}: ${item.input.workflow.shortLabel}")
            }
          }
        }
      }, confirmButton = { TextButton(onClick = { showCases = false }) { Text("Cancel") } })
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Vancomycin TDM", style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.weight(1f),
          fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        TextButton(onClick = {
          viewModel.resetCalculator()
          attempted = false
          showCases = false
          scope.launch { scrollState.scrollTo(0) }
        }, modifier = Modifier.testTag("reset_calculator_button")) {
          Icon(Icons.Default.Refresh, contentDescription = null)
          Spacer(Modifier.width(4.dp))
          Text("Reset")
        }
      }
      Text("Enter a case, review the values, then calculate.",
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    // Workflow Selection (Segmented dynamic choice)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Calculation method",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "Choose the available laboratory samples.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          TdmWorkflow.values().forEach { wf ->
            val isSelected = input.workflow == wf
            OutlinedButton(
              onClick = { viewModel.setWorkflow(wf) },
              modifier = Modifier
                .weight(1f)
                .testTag("workflow_button_${wf.name.lowercase()}"),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
              ),
              border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder
            ) {
              Text(
                text = when (wf) { TdmWorkflow.PRE -> "Pre"; TdmWorkflow.POST -> "Post"; TdmWorkflow.PRE_POST -> "Pre + Post" },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "• ${input.workflow.description}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.secondary
        )
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedButton(onClick = { showCases = true }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
        Text("Load example", style = MaterialTheme.typography.labelLarge)
      }
      TextButton(onClick = { viewModel.setTab(com.example.tdm.viewmodel.AppTab.CAMERA) },
        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("scan_lab_report_banner")) {
        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Import report", style = MaterialTheme.typography.labelLarge)
      }
    }

    // Section 1: Patient Information & Anthropometrics
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "1. Patient information",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FormTextField(
            placeholder = { Text("e.g. Alex Smith") },
            value = input.patient.patientName,
            onValueChange = { name -> viewModel.updatePatient { it.copy(patientName = name) } },
            label = { Text("Patient Name") },
            modifier = Modifier
              .weight(1.3f)
              .testTag("patient_name_input"),
            singleLine = true
          )
          FormTextField(
            placeholder = { Text("e.g. 123") },
            value = input.patient.patientId,
            onValueChange = { id -> viewModel.updatePatient { it.copy(patientId = id) } },
            label = { Text("Case ID / MRN") },
            modifier = Modifier
              .weight(1f)
              .testTag("patient_id_input"),
            singleLine = true
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FormTextField(
            placeholder = { Text("e.g. 52") },
            value = if (input.patient.ageYears > 0) input.patient.ageYears.toInt().toString() else "",
            onValueChange = { str ->
              val v = str.toDoubleOrNull() ?: 0.0
              viewModel.updatePatient { it.copy(ageYears = v) }
            },
            label = { Text("Age (yrs)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
              .weight(1f)
              .testTag("patient_age_input"),
            singleLine = true
          )

          // Gender Selector
          Column(modifier = Modifier.weight(1.2f)) {
            Text(
              text = "Gender",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Gender.values().forEach { g ->
                val isSelected = input.patient.gender == g
                FilterChip(
                  selected = isSelected,
                  onClick = { viewModel.updatePatient { it.copy(gender = g) } },
                  label = { Text(g.label, style = MaterialTheme.typography.labelSmall) },
                  modifier = Modifier.testTag("gender_${g.name.lowercase()}_chip")
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FormTextField(
            placeholder = { Text("e.g. 172") },
            value = if (input.patient.heightCm > 0) input.patient.heightCm.toString() else "",
            onValueChange = { str ->
              val v = str.toDoubleOrNull() ?: 0.0
              viewModel.updatePatient { it.copy(heightCm = v) }
            },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("patient_height_input"),
            singleLine = true
          )

          FormTextField(
            placeholder = { Text("e.g. 70") },
            value = if (input.patient.weightKg > 0) input.patient.weightKg.toString() else "",
            onValueChange = { str ->
              val v = str.toDoubleOrNull() ?: 0.0
              viewModel.updatePatient { it.copy(weightKg = v) }
            },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("patient_weight_input"),
            singleLine = true
          )
        }

        // Serum Creatinine with Unit Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FormTextField(
            placeholder = { Text("e.g. 85") },
            value = if (input.patient.serumCreatinine > 0) input.patient.serumCreatinine.toString() else "",
            onValueChange = { str ->
              val v = str.toDoubleOrNull() ?: 0.0
              viewModel.updatePatient { it.copy(serumCreatinine = v) }
            },
            label = { Text("Serum Creatinine (${input.patient.creatinineUnit.symbol})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1.3f)
              .testTag("patient_creatinine_input"),
            singleLine = true
          )

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Unit",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              CreatinineUnit.values().forEach { u ->
                val isSelected = input.patient.creatinineUnit == u
                FilterChip(
                  selected = isSelected,
                  onClick = { viewModel.updatePatient { it.copy(creatinineUnit = u) } },
                  label = { Text(u.symbol, style = MaterialTheme.typography.labelSmall) }
                )
              }
            }
          }
        }

        // Clinical indication target
        Column {
          Text(
            text = "Infection Target Profile",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            TargetIndication.values().forEach { target ->
              val isSelected = input.targetIndication == target
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.setTargetIndication(target) },
                label = {
                  Text(
                    text = if (target == TargetIndication.SEVERE_MRSA) "Severe MRSA (15-20 mg/L)" else "Uncomplicated (10-15 mg/L)",
                    style = MaterialTheme.typography.labelSmall
                  )
                },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // Section 2: Current Dosing Regimen
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Vaccines,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "2. Dose & timing",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FormTextField(
            placeholder = { Text("e.g. 1000") },
            value = if (input.regimen.doseMg > 0) input.regimen.doseMg.toInt().toString() else "",
            onValueChange = { str ->
              val v = str.toDoubleOrNull() ?: 0.0
              viewModel.updateRegimen { it.copy(doseMg = v) }
            },
            label = { Text("Dose (mg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
              .weight(1.1f)
              .testTag("regimen_dose_input"),
            singleLine = true
          )

          FormTextField(
            placeholder = { Text("e.g. 12") },
            value = if (input.regimen.intervalHours > 0) input.regimen.intervalHours.toInt().toString() else "",
            onValueChange = { str ->
              val v = str.toDoubleOrNull() ?: 0.0
              viewModel.updateRegimen { it.copy(intervalHours = v) }
            },
            label = { Text("Interval τ (hrs)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
              .weight(1f)
              .testTag("regimen_interval_input"),
            singleLine = true
          )

          FormTextField(
            placeholder = { Text("e.g. 1.0") },
            value = if (input.regimen.infusionDurationHours > 0) input.regimen.infusionDurationHours.toString() else "",
            onValueChange = { str ->
              val v = str.toDoubleOrNull() ?: 0.0
              viewModel.updateRegimen { it.copy(infusionDurationHours = v) }
            },
            label = { Text("Infusion (hrs)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .weight(1f)
              .testTag("regimen_infusion_input"),
            singleLine = true
          )
        }
      }
    }

    // Section 3: Dynamic Lab Concentration & Timing Section
    // (Only shows fields appropriate for selected workflow!)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "3. Laboratory values",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }

        // Show Post-Dose fields if POST or PRE_POST
        if (input.workflow == TdmWorkflow.POST || input.workflow == TdmWorkflow.PRE_POST) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Post-Dose Concentration (Peak Level)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                FormTextField(
            placeholder = { Text("e.g. 27.8") },
                  value = input.postDoseConcMgL?.toString() ?: "",
                  onValueChange = { str ->
                    viewModel.updatePostConc(
                      conc = str.toDoubleOrNull(),
                      delay = input.postDoseSampleDelayHoursAfterInfusionEnd
                    )
                  },
                  label = { Text("C_post (mg/L)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("post_conc_input"),
                  singleLine = true
                )

                FormTextField(
            placeholder = { Text("e.g. 1.0") },
                  value = input.postDoseSampleDelayHoursAfterInfusionEnd?.toString() ?: "",
                  onValueChange = { str ->
                    viewModel.updatePostConc(
                      conc = input.postDoseConcMgL,
                      delay = str.toDoubleOrNull()
                    )
                  },
                  label = { Text("Delay after Infusion (h)") },
                  supportingText = { Text("Ideal: 1.0–2.0h") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier
                    .weight(1.2f)
                    .testTag("post_delay_input"),
                  singleLine = true
                )
              }
            }
          }
        }

        // Show Pre-Dose fields if PRE or PRE_POST
        if (input.workflow == TdmWorkflow.PRE || input.workflow == TdmWorkflow.PRE_POST) {
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Pre-Dose Concentration (Trough Level)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                FormTextField(
            placeholder = { Text("e.g. 11.5") },
                  value = input.preDoseConcMgL?.toString() ?: "",
                  onValueChange = { str ->
                    viewModel.updatePreConc(
                      conc = str.toDoubleOrNull(),
                      delay = input.preDoseSampleDelayHoursBeforeNextDose
                    )
                  },
                  label = { Text("C_pre (mg/L)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("pre_conc_input"),
                  singleLine = true
                )

                FormTextField(
            placeholder = { Text("e.g. 0.5") },
                  value = input.preDoseSampleDelayHoursBeforeNextDose?.toString() ?: "",
                  onValueChange = { str ->
                    viewModel.updatePreConc(
                      conc = input.preDoseConcMgL,
                      delay = str.toDoubleOrNull()
                    )
                  },
                  label = { Text("Time before Dose (h)") },
                  supportingText = { Text("Typically: 0.5–1.0h") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier
                    .weight(1.2f)
                    .testTag("pre_delay_input"),
                  singleLine = true
                )
              }
            }
          }
        }
      }
    }

    // Validation Feedback (Distinguishing Errors from Clinical Review Warnings)
    if (attempted && validation.errors.isNotEmpty()) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Error,
              contentDescription = "Validation Error",
              tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Input Validation Errors (${validation.errors.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
          }
          validation.errors.forEach { err ->
            Text(
              text = "• ${err.message}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      }
    }

    if (validation.warnings.isNotEmpty()) {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = "Clinical Warning",
              tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Clinical Review Warnings (${validation.warnings.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer
            )
          }
          validation.warnings.forEach { warn ->
            Text(
              text = "• ${warn.message}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onTertiaryContainer
            )
          }
        }
      }
    }

    // Primary Action Button: Calculate
    Button(
      enabled = !isCalculating,
      onClick = { attempted = true; if (validation.isValid) viewModel.prepareResults() },
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("run_calculation_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      )
    ) {
      Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Calculate",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}


/** Keep the label above the box so its example remains visible even before focus. */
@Composable
private fun FormTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: @Composable () -> Unit,
  placeholder: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  singleLine: Boolean = true,
  supportingText: (@Composable () -> Unit)? = null
) {
  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    ProvideTextStyle(MaterialTheme.typography.labelMedium) { label() }
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      placeholder = placeholder,
      textStyle = MaterialTheme.typography.bodyLarge,
      colors = OutlinedTextFieldDefaults.colors(
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline),
      keyboardOptions = keyboardOptions,
      singleLine = singleLine,
      supportingText = supportingText,
      modifier = Modifier.fillMaxWidth()
    )
  }
}
