package com.example.tdm.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.tdm.camera.ExtractedLabData
import com.example.tdm.model.CreatinineUnit
import com.example.tdm.viewmodel.AppTab
import com.example.tdm.viewmodel.TdmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
  viewModel: TdmViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val capturedBitmap by viewModel.capturedBitmap.collectAsState()
  val extractedData by viewModel.extractedLabData.collectAsState()
  val isProcessingOcr by viewModel.isProcessingOcr.collectAsState()
  val ocrError by viewModel.ocrError.collectAsState()

  val scrollState = rememberScrollState()

  // State for editable confirmation form
  var confirmedName by remember { mutableStateOf("") }
  var confirmedId by remember { mutableStateOf("") }
  var confirmedCr by remember { mutableStateOf("") }
  var confirmedCrUnit by remember { mutableStateOf(CreatinineUnit.UMOL_L) }
  var confirmedTrough by remember { mutableStateOf("") }
  var confirmedPeak by remember { mutableStateOf("") }
  var confirmedDose by remember { mutableStateOf("") }
  var confirmedInterval by remember { mutableStateOf("") }
  var attachImageToCalculation by remember { mutableStateOf(true) }

  // Sync state when OCR extracts data
  LaunchedEffect(extractedData) {
    extractedData?.let { data ->
      confirmedName = data.patientName ?: ""
      confirmedId = data.patientId ?: ""
      confirmedCr = data.serumCreatinine?.toString() ?: ""
      confirmedCrUnit = data.creatinineUnit
      confirmedTrough = data.preDoseConcMgL?.toString() ?: ""
      confirmedPeak = data.postDoseConcMgL?.toString() ?: ""
      confirmedDose = data.doseMg?.toInt()?.toString() ?: ""
      confirmedInterval = data.intervalHours?.toInt()?.toString() ?: ""
    }
  }

  // Camera preview launcher
  val takePicturePreviewLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    bitmap?.let { viewModel.onImageCaptured(it) }
  }

  // Camera permission launcher
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      takePicturePreviewLauncher.launch(null)
    }
  }

  // Photo Picker launcher
  val pickMediaLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    uri?.let { selectedUri ->
      try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, selectedUri))
        } else {
          @Suppress("DEPRECATION")
          MediaStore.Images.Media.getBitmap(context.contentResolver, selectedUri)
        }
        viewModel.onImageCaptured(bitmap)
      } catch (e: Exception) {
        // Fallback
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Banner
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("camera_header_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
      ),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Laboratory Report Camera Scanner",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
        Text(
          text = "Capture fictional laboratory reports, extract pharmacokinetic levels via OCR, review, and confirm values before calculating.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        // Flow Stepper Indicator
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          StepBadge(step = "1", label = "Camera", active = capturedBitmap == null)
          Text("→", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
          StepBadge(step = "2", label = "Capture", active = isProcessingOcr)
          Text("→", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
          StepBadge(step = "3", label = "Review", active = capturedBitmap != null && !isProcessingOcr)
          Text("→", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
          StepBadge(step = "4", label = "Confirm", active = extractedData != null)
        }
      }
    }

    // Capture Controls Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Select Capture Method",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        // Action Buttons: Camera & Gallery
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              val permission = Manifest.permission.CAMERA
              if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                takePicturePreviewLauncher.launch(null)
              } else {
                cameraPermissionLauncher.launch(permission)
              }
            },
            modifier = Modifier
              .weight(1f)
              .testTag("launch_camera_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Snap Camera")
          }

          OutlinedButton(
            onClick = {
              pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier
              .weight(1f)
              .testTag("pick_gallery_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pick Image")
          }
        }

        // Simulated Lab Slips (Optimal for cloud streaming emulator testing)
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Text(
          text = "Or Test with Simulated Hospital Lab Slips (1-Tap):",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          OutlinedButton(
            onClick = { viewModel.loadSimulatedLabReport(0) },
            modifier = Modifier
              .weight(1f)
              .testTag("sim_report_1_button"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("UKM (Pre+Post)", style = MaterialTheme.typography.labelSmall, maxLines = 1)
          }

          OutlinedButton(
            onClick = { viewModel.loadSimulatedLabReport(1) },
            modifier = Modifier
              .weight(1f)
              .testTag("sim_report_2_button"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("HRPZ (Pre)", style = MaterialTheme.typography.labelSmall, maxLines = 1)
          }

          OutlinedButton(
            onClick = { viewModel.loadSimulatedLabReport(2) },
            modifier = Modifier
              .weight(1f)
              .testTag("sim_report_3_button"),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("HKL (Post)", style = MaterialTheme.typography.labelSmall, maxLines = 1)
          }
        }
      }
    }

    // Stage 2: Captured Image Preview & OCR Scanning Progress
    capturedBitmap?.let { bmp ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("captured_image_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Captured Lab Slip Preview",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            TextButton(
              onClick = { viewModel.clearCapturedImage() },
              modifier = Modifier.testTag("clear_captured_image_button")
            ) {
              Text("Retake", color = MaterialTheme.colorScheme.error)
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(220.dp)
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
              .background(Color.Black),
            contentAlignment = Alignment.Center
          ) {
            Image(
              bitmap = bmp.asImageBitmap(),
              contentDescription = "Captured Lab Report",
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize()
            )

            if (isProcessingOcr) {
              Surface(
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxSize()
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center,
                  modifier = Modifier.padding(16.dp)
                ) {
                  CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = "Running On-Device OCR Analysis...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Extracting Serum Creatinine, Pre-dose and Post-dose concentrations",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }

          if (ocrError != null) {
            Surface(
              color = MaterialTheme.colorScheme.errorContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = ocrError!!,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }
      }

      // Stage 3: Review and Confirm Extracted Values Form
      // "Users must always review and confirm extracted values before using them."
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("confirm_extracted_values_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FactCheck,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Review & Confirm Extracted Values",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Text(
            text = "Verify OCR extracted values against the original laboratory sheet before committing:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // Patient Name & ID
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = confirmedName,
              onValueChange = { confirmedName = it },
              label = { Text("Patient Name") },
              modifier = Modifier
                .weight(1.3f)
                .testTag("confirm_name_input"),
              singleLine = true
            )
            OutlinedTextField(
              value = confirmedId,
              onValueChange = { confirmedId = it },
              label = { Text("MRN / ID") },
              modifier = Modifier
                .weight(1f)
                .testTag("confirm_id_input"),
              singleLine = true
            )
          }

          // Serum Creatinine with Unit Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = confirmedCr,
              onValueChange = { confirmedCr = it },
              label = { Text("Serum Creatinine (${confirmedCrUnit.symbol})") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier
                .weight(1.3f)
                .testTag("confirm_cr_input"),
              singleLine = true
            )

            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              CreatinineUnit.values().forEach { u ->
                FilterChip(
                  selected = confirmedCrUnit == u,
                  onClick = { confirmedCrUnit = u },
                  label = { Text(u.symbol, style = MaterialTheme.typography.labelSmall) }
                )
              }
            }
          }

          // Vancomycin Concentrations
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = confirmedTrough,
              onValueChange = { confirmedTrough = it },
              label = { Text("Pre-dose / Trough (mg/L)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier
                .weight(1f)
                .testTag("confirm_trough_input"),
              singleLine = true
            )
            OutlinedTextField(
              value = confirmedPeak,
              onValueChange = { confirmedPeak = it },
              label = { Text("Post-dose / Peak (mg/L)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier
                .weight(1f)
                .testTag("confirm_peak_input"),
              singleLine = true
            )
          }

          // Dose & Interval
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = confirmedDose,
              onValueChange = { confirmedDose = it },
              label = { Text("Dose (mg)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier
                .weight(1f)
                .testTag("confirm_dose_input"),
              singleLine = true
            )
            OutlinedTextField(
              value = confirmedInterval,
              onValueChange = { confirmedInterval = it },
              label = { Text("Interval τ (h)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier
                .weight(1f)
                .testTag("confirm_interval_input"),
              singleLine = true
            )
          }

          // Checkbox to attach report image to calculation record
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Checkbox(
              checked = attachImageToCalculation,
              onCheckedChange = { attachImageToCalculation = it },
              modifier = Modifier.testTag("attach_image_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Attach scanned lab slip to calculation record in History",
              style = MaterialTheme.typography.bodySmall
            )
          }

          // Confirm and Apply Action Button
          Button(
            onClick = {
              val confirmedData = ExtractedLabData(
                patientName = confirmedName.ifBlank { null },
                patientId = confirmedId.ifBlank { null },
                serumCreatinine = confirmedCr.toDoubleOrNull(),
                creatinineUnit = confirmedCrUnit,
                preDoseConcMgL = confirmedTrough.toDoubleOrNull(),
                postDoseConcMgL = confirmedPeak.toDoubleOrNull(),
                doseMg = confirmedDose.toDoubleOrNull(),
                intervalHours = confirmedInterval.toDoubleOrNull()
              )
              viewModel.confirmExtractedValues(
                confirmedData = confirmedData,
                attachImage = attachImageToCalculation
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("confirm_and_apply_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            )
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Confirm Values & Use in Calculation",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun StepBadge(step: String, label: String, active: Boolean) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Surface(
      color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.size(24.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = step,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontSize = 10.sp,
      fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
      color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
