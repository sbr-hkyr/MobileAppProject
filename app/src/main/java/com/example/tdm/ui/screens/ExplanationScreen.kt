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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdm.model.CalculationStep
import com.example.tdm.model.StepCategory
import com.example.tdm.viewmodel.AppTab
import com.example.tdm.viewmodel.TdmViewModel

@Composable
fun ExplanationScreen(
  viewModel: TdmViewModel,
  modifier: Modifier = Modifier
) {
  val pkResult by viewModel.pkResult.collectAsState()
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
          imageVector = Icons.Default.MenuBook,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(64.dp)
        )
        Text(
          text = "No Calculation Recorded",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Run a TDM calculation to view the step-by-step pharmacokinetic derivation.",
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

  val steps = pkResult!!.detailedSteps

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Explanation Header Banner
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("explanation_header_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
      ),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoStories,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Calculation Explanation & Derivation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
        Text(
          text = "Auditable four-stage pharmacokinetic derivation following Malaysian PhIS & MOH clinical guidelines:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        // Visual Pipeline Progress Tracker
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          CategoryPill(label = "1. Inputs")
          Text("→", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
          CategoryPill(label = "2. Intermediates")
          Text("→", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
          CategoryPill(label = "3. PK Params")
          Text("→", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
          CategoryPill(label = "4. Final AUC")
        }
      }
    }

    // Group steps by Category
    StepCategory.values().forEach { cat ->
      val catSteps = steps.filter { it.category == cat }
      if (catSteps.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = cat.display,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          catSteps.forEach { step ->
            StepDetailCard(step = step)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun CategoryPill(label: String) {
  Surface(
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
    shape = RoundedCornerShape(8.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun StepDetailCard(step: CalculationStep) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("step_card_${step.stepNumber}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "Step ${step.stepNumber}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = step.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      // Formula Box
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Formula: ${step.formula}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Substituted: ${step.substitution}",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Calculated Result
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Result:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = step.result,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      if (step.notes.isNotEmpty()) {
        Text(
          text = "Clinical note: ${step.notes}",
          style = MaterialTheme.typography.bodySmall,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        )
      }
    }
  }
}
