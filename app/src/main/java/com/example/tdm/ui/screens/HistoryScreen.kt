package com.example.tdm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.tdm.data.CalculationHistoryEntity
import com.example.tdm.viewmodel.AppTab
import com.example.tdm.viewmodel.TdmViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  viewModel: TdmViewModel,
  modifier: Modifier = Modifier
) {
  val historyList by viewModel.historyList.collectAsState()
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Clear All History?") },
      text = { Text("This will permanently delete all saved patient TDM calculation logs from the local database.") },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.clearAllHistory()
            showClearConfirmDialog = false
          }
        ) {
          Text("Clear All", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Calculation History",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "${historyList.size} record${if (historyList.size == 1) "" else "s"} stored locally in Room DB",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (historyList.isNotEmpty()) {
        IconButton(
          onClick = { showClearConfirmDialog = true },
          modifier = Modifier.testTag("clear_all_history_button")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Clear All History",
            tint = MaterialTheme.colorScheme.error
          )
        }
      }
    }

    if (historyList.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 60.dp),
        contentAlignment = Alignment.TopCenter
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
          )
          Text(
            text = "No Saved Calculations Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "After running a TDM calculation, tap 'Save to History' on the results page to archive it here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = { viewModel.setTab(AppTab.CALCULATOR) },
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Start Calculation")
          }
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxSize()
          .testTag("history_list")
      ) {
        items(historyList, key = { it.id }) { item ->
          HistoryCard(
            item = item,
            onDelete = { viewModel.deleteHistoryItem(item.id) }
          )
        }
      }
    }
  }
}

@Composable
fun HistoryCard(
  item: CalculationHistoryEntity,
  onDelete: () -> Unit
) {
  val dateStr = remember(item.timestamp) {
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("history_card_${item.id}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = item.patientName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "ID: ${item.patientId} • $dateStr",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (item.labReportImageUri != null) {
            Surface(
              color = MaterialTheme.colorScheme.secondaryContainer,
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.padding(end = 6.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Attachment,
                  contentDescription = "Attached Lab Report",
                  tint = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Lab Slip",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
              }
            }
          }

          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = item.workflow,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete Record",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Regimen: ${item.doseMg.toInt()} mg q${item.intervalHours.toInt()}h",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "CrCl: ${String.format(Locale.US, "%.1f", item.crCl)} mL/min",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "AUC₂₄: ${String.format(Locale.US, "%.0f", item.auc24)} mg·h/L",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (item.aucStatus.contains("Therapeutic", ignoreCase = true) && !item.aucStatus.contains("Sub", ignoreCase = true) && !item.aucStatus.contains("Supra", ignoreCase = true)) {
              Color(0xFF2E7D32)
            } else {
              MaterialTheme.colorScheme.primary
            }
          )
          Text(
            text = "Trough: ${String.format(Locale.US, "%.1f", item.cMin)} mg/L",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
