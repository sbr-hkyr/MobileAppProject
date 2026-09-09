package com.example.tdm.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tdm.model.PkResult
import com.example.tdm.model.TherapeuticStatus
import com.example.tdm.ui.components.PkCurveChart
import com.example.tdm.viewmodel.AppTab
import com.example.tdm.viewmodel.TdmViewModel
import java.io.File
import java.util.Locale

@Composable
fun ResultsScreen(
  viewModel: TdmViewModel,
  modifier: Modifier = Modifier
) {
  val pkResult by viewModel.pkResult.collectAsState()
  val input by viewModel.input.collectAsState()
  val attachedImage by viewModel.attachedReportImagePath.collectAsState()
  val capturedBitmap by viewModel.capturedBitmap.collectAsState()
  val context = LocalContext.current
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
          imageVector = Icons.Default.Calculate,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(64.dp)
        )
        Text(
          text = "No Active Calculation",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Please enter patient parameters and run the TDM calculation first.",
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

  val res = pkResult!!

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Top Hero Card: Primary Target Status
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("therapeutic_status_card"),
      colors = CardDefaults.cardColors(
        containerColor = if (isDark) {
          when (res.aucStatus) {
            TherapeuticStatus.THERAPEUTIC -> Color(0xFF132F1A)
            TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFF38230D)
            TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFF381515)
          }
        } else {
          when (res.aucStatus) {
            TherapeuticStatus.THERAPEUTIC -> Color(0xFFE8F5E9)
            TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFFFF3E0)
            TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFFFEBEE)
          }
        }
      ),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "AUC₂₄ Exposure Target",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${String.format(Locale.US, "%.0f", res.auc24MgHPerL)} mg·h/L",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = if (isDark) {
                when (res.aucStatus) {
                  TherapeuticStatus.THERAPEUTIC -> Color(0xFF81C784)
                  TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFFFB74D)
                  TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFEF5350)
                }
              } else {
                when (res.aucStatus) {
                  TherapeuticStatus.THERAPEUTIC -> Color(0xFF1B5E20)
                  TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFE65100)
                  TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFB71C1C)
                }
              }
            )
          }

          Surface(
            color = when (res.aucStatus) {
              TherapeuticStatus.THERAPEUTIC -> Color(0xFF2E7D32)
              TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFEF6C00)
              TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFC62828)
            },
            shape = RoundedCornerShape(20.dp)
          ) {
            Text(
              text = res.aucStatus.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Guideline Target: 400–600 mg·h/L",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "AUC₂₄/MIC: ${String.format(Locale.US, "%.1f", res.auc24OverMic)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    // Key Parameters 2-column Grid
    Text(
      text = "Pharmacokinetic Parameters",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      PkStatCard(
        title = "Elimination Rate (Ke)",
        value = "${String.format(Locale.US, "%.4f", res.kePerHour)} h⁻¹",
        subtitle = "t₁/₂ = ${String.format(Locale.US, "%.1f", res.halfLifeHours)} hours",
        modifier = Modifier.weight(1f)
      )
      PkStatCard(
        title = "Volume of Distrib. (Vd)",
        value = "${String.format(Locale.US, "%.1f", res.vdLiters)} L",
        subtitle = "${String.format(Locale.US, "%.2f", res.vdPerKg)} L/kg (Ref: 0.5–1.0)",
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      PkStatCard(
        title = "Vancomycin Clearance",
        value = "${String.format(Locale.US, "%.2f", res.clearanceLPerHr)} L/h",
        subtitle = "${String.format(Locale.US, "%.1f", res.clearanceMlPerMin)} mL/min",
        modifier = Modifier.weight(1f)
      )
      PkStatCard(
        title = "Creatinine Clearance",
        value = "${String.format(Locale.US, "%.1f", res.crClMlPerMin)} mL/min",
        subtitle = "Cockcroft-Gault (IBW: ${String.format(Locale.US, "%.1f", res.ibwKg)}kg)",
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      PkStatCard(
        title = "True Peak (C_max)",
        value = "${String.format(Locale.US, "%.1f", res.cMaxMgL)} mg/L",
        subtitle = "At end of ${input.regimen.infusionDurationHours}h infusion",
        modifier = Modifier.weight(1f)
      )
      PkStatCard(
        title = "True Trough (C_min)",
        value = "${String.format(Locale.US, "%.1f", res.cMinMgL)} mg/L",
        subtitle = "Target: ${input.targetIndication.targetTroughRange}",
        statusColor = when (res.troughStatus) {
          TherapeuticStatus.THERAPEUTIC -> Color(0xFF2E7D32)
          TherapeuticStatus.SUBTHERAPEUTIC -> Color(0xFFEF6C00)
          TherapeuticStatus.SUPRATHERAPEUTIC -> Color(0xFFC62828)
        },
        modifier = Modifier.weight(1f)
      )
    }

    // Interactive Pharmacokinetic Curve Graph
    PkCurveChart(
      pkResult = res,
      intervalHours = input.regimen.intervalHours,
      infusionHours = input.regimen.infusionDurationHours
    )

    // Clinical Interpretation & Recommendation Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("clinical_recommendation_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      ),
      shape = RoundedCornerShape(14.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Clinical Assessment & Regimen Recommendation",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = res.clinicalRecommendation,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Attached Lab Report Image (if captured via Camera or loaded)
    if (attachedImage != null || capturedBitmap != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("attached_lab_report_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Attached Laboratory Report (Scanned)",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          if (capturedBitmap != null) {
            Image(
              bitmap = capturedBitmap!!.asImageBitmap(),
              contentDescription = "Scanned Lab Slip",
              contentScale = ContentScale.Fit,
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
            )
          } else if (attachedImage != null) {
            AsyncImage(
              model = File(attachedImage!!),
              contentDescription = "Scanned Lab Slip",
              contentScale = ContentScale.Fit,
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
            )
          }

          Text(
            text = "Values verified and confirmed from this report were utilized in this calculation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Action Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { viewModel.setTab(AppTab.EXPLANATION) },
        modifier = Modifier
          .weight(1f)
          .testTag("view_explanation_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Explain Steps", style = MaterialTheme.typography.labelLarge)
      }

      FilledTonalButton(
        onClick = { viewModel.setTab(AppTab.SIMULATOR) },
        modifier = Modifier
          .weight(1f)
          .testTag("open_simulation_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(imageVector = Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("What-If Sim", style = MaterialTheme.typography.labelLarge)
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      OutlinedButton(
        onClick = { viewModel.saveCurrentToHistory() },
        modifier = Modifier
          .weight(1f)
          .testTag("save_history_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save to History", style = MaterialTheme.typography.labelMedium)
      }

      OutlinedButton(
        onClick = {
          val text = viewModel.formatShareSummary()
          val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
          }
          val shareIntent = Intent.createChooser(sendIntent, "Share TDM Report")
          context.startActivity(shareIntent)
        },
        modifier = Modifier
          .weight(1f)
          .testTag("share_summary_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Export / Share", style = MaterialTheme.typography.labelMedium)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
fun PkStatCard(
  title: String,
  value: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  statusColor: Color? = null
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = statusColor ?: MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
      )
    }
  }
}
