package com.example.tdm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdm.model.TherapeuticStatus
import com.example.tdm.viewmodel.AppTab
import com.example.tdm.viewmodel.TdmViewModel
import java.util.Locale

@Composable
fun SimulatorScreen(
  viewModel: TdmViewModel,
  modifier: Modifier = Modifier
) {
  val pkResult by viewModel.pkResult.collectAsState()
  val input by viewModel.input.collectAsState()
  val simDose by viewModel.simDose.collectAsState()
  val simInterval by viewModel.simInterval.collectAsState()
  val simResult by viewModel.simResult.collectAsState()
  val scrollState = rememberScrollState()

  if (pkResult == null) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Science,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(64.dp)
        )
        Text(
          text = "Pharmacokinetic Baseline Required",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Run a TDM calculation first to derive the patient's individual Ke and Vd for simulation.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
          onClick = { viewModel.setTab(AppTab.CALCULATOR) },
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Go to Calculator")
        }
      }
    }
    return
  }

  val pk = pkResult!!

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Simulator Header
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("simulator_header_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
      ),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "What-If Regimen Simulator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
          )
        }
        Text(
          text = "Test alternative dosing regimens using ${input.patient.patientName}'s established elimination parameters (Ke = ${String.format(Locale.US, "%.4f", pk.kePerHour)} h⁻¹, Vd = ${String.format(Locale.US, "%.1f", pk.vdLiters)} L):",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onTertiaryContainer
        )
      }
    }

    // Regimen Titration Controls
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
          text = "Proposed Regimen Controls",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        // Dose Selector Chips
        Column {
          Text(
            text = "Proposed Dose: ${simDose.toInt()} mg",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          val doseOptions = listOf(500.0, 750.0, 1000.0, 1250.0, 1500.0, 1750.0, 2000.0)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            doseOptions.take(4).forEach { d ->
              FilterChip(
                selected = simDose == d,
                onClick = { viewModel.updateSimulationRegimen(d, simInterval) },
                label = { Text("${d.toInt()}mg", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.weight(1f)
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            doseOptions.drop(4).forEach { d ->
              FilterChip(
                selected = simDose == d,
                onClick = { viewModel.updateSimulationRegimen(d, simInterval) },
                label = { Text("${d.toInt()}mg", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Interval Selector Chips
        Column {
          Text(
            text = "Proposed Dosing Interval: q${simInterval.toInt()}h",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))
          val intervalOptions = listOf(8.0, 12.0, 18.0, 24.0, 36.0, 48.0)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            intervalOptions.forEach { iv ->
              FilterChip(
                selected = simInterval == iv,
                onClick = { viewModel.updateSimulationRegimen(simDose, iv) },
                label = { Text("q${iv.toInt()}h", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // Side-by-Side Comparison Card
    simResult?.let { sim ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("simulation_comparison_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "Comparison: Current vs Simulated Regimen",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          // Table Header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Metric", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Current (${input.regimen.doseMg.toInt()}mg q${input.regimen.intervalHours.toInt()}h)", modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Simulated (${sim.doseMg.toInt()}mg q${sim.intervalHours.toInt()}h)", modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }

          // Row 1: Daily Dose
          val curDaily = input.regimen.doseMg * (24.0 / input.regimen.intervalHours)
          val simDaily = sim.doseMg * (24.0 / sim.intervalHours)
          ComparisonRow(
            label = "Total 24h Dose",
            current = "${curDaily.toInt()} mg",
            simulated = "${simDaily.toInt()} mg"
          )

          // Row 2: Projected AUC24
          ComparisonRow(
            label = "AUC₂₄ Target",
            current = "${String.format(Locale.US, "%.0f", pk.auc24MgHPerL)} mg·h/L",
            simulated = "${String.format(Locale.US, "%.0f", sim.auc24MgHPerL)} mg·h/L",
            simColor = when (sim.aucStatus) {
              TherapeuticStatus.THERAPEUTIC -> Color(0xFF2E7D32)
              TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFEF6C00)
              TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFC62828)
            }
          )

          // Row 3: Peak
          ComparisonRow(
            label = "Peak (C_max)",
            current = "${String.format(Locale.US, "%.1f", pk.cMaxMgL)} mg/L",
            simulated = "${String.format(Locale.US, "%.1f", sim.cMaxMgL)} mg/L"
          )

          // Row 4: Trough
          ComparisonRow(
            label = "Trough (C_min)",
            current = "${String.format(Locale.US, "%.1f", pk.cMinMgL)} mg/L",
            simulated = "${String.format(Locale.US, "%.1f", sim.cMinMgL)} mg/L"
          )

          // Status Badge for Simulated
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = when (sim.aucStatus) {
              TherapeuticStatus.THERAPEUTIC -> Color(0xFFE8F5E9)
              TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFFFF3E0)
              TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFFFEBEE)
            },
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (sim.aucStatus == TherapeuticStatus.THERAPEUTIC) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = when (sim.aucStatus) {
                  TherapeuticStatus.THERAPEUTIC -> Color(0xFF2E7D32)
                  TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFEF6C00)
                  TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFC62828)
                },
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Simulated Exposure Status: ${sim.aucStatus.label}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = when (sim.aucStatus) {
                  TherapeuticStatus.THERAPEUTIC -> Color(0xFF1B5E20)
                  TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFE65100)
                  TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFB71C1C)
                }
              )
            }
          }

          // Apply button
          Button(
            onClick = {
              viewModel.updateRegimen { it.copy(doseMg = sim.doseMg, intervalHours = sim.intervalHours) }
              viewModel.executeCalculation(switchToResults = true)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("apply_simulated_regimen_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Adopt This Regimen in Calculator")
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun ComparisonRow(
  label: String,
  current: String,
  simulated: String,
  simColor: Color? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(current, modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    Text(
      simulated,
      modifier = Modifier.weight(1.4f),
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = simColor ?: MaterialTheme.colorScheme.onSurface
    )
  }
}
