package com.example.tdm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DisclaimerScreen(
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Primary Warning Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("clinical_disclaimer_card"),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Medical Alert",
            tint = Color(0xFFC62828),
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Mandatory Clinical Disclaimer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB71C1C)
          )
        }
        Text(
          text = "TDM Insight is an academic software prototype developed for Mobile Application Development (CDE2313) at Albukhary International University.",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF212121)
        )
        Text(
          text = "It is intended strictly for educational and software development demonstration purposes. It must NOT be utilized as a clinically validated prescribing, diagnostic, or autonomous treatment-decision system in real clinical practice.\n\nAll demonstrated patient profiles are fictional. Qualified healthcare professionals and clinical pharmacokinetics specialists must consult approved local hospital formularies, laboratory therapeutic ranges, and current authoritative clinical guidelines before making any patient management decisions.",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF37474F)
        )
      }
    }

    // Clinical References & Authoritative Sources
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Authoritative Clinical Sources & Benchmarks",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        SourceItem(
          title = "Malaysian Pharmacy Information System (PhIS)",
          subtitle = "TDM Module Calculator Manual, Ministry of Health Malaysia (MOH). Standardizes Vancomycin Pre, Post, and Pre-Post multi-point pharmacokinetic workflows."
        )

        SourceItem(
          title = "Clinical Pharmacokinetics Pharmacy Handbook (2nd Ed.)",
          subtitle = "Pharmaceutical Services Programme, Ministry of Health Malaysia. Reference for Cockcroft-Gault CrCl equations, population Ke (0.00083×CrCl + 0.0044), and standard Vd ranges (0.7 L/kg)."
        )

        SourceItem(
          title = "myTDM Calculator (mytdmcalculator.com)",
          subtitle = "Interactive clinical therapeutic drug monitoring web benchmark for Vancomycin and Aminoglycosides."
        )

        SourceItem(
          title = "ASHP/IDSA/PIDS/SIDP 2020 Consensus Guidelines",
          subtitle = "Therapeutic monitoring of vancomycin for serious methicillin-resistant Staphylococcus aureus infections. Established consensus target AUC24/MIC ratio of 400–600 mg·h/L (assuming MIC 1.0 mg/L)."
        )
      }
    }

    // Course & Academic Context Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Academic Project Information",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "• Course: CDE2313 Mobile Application Development\n• Programme: Bachelor in Data Science\n• Institution: Albukhary International University (AIU)\n• Instructors: Ts. Mohd Zulkifli Mohd Zaki (Lead), Madam Siti Shafrah Shahawai (Co-Lead)\n• Architecture: MVVM + Clean Calculation Engine + Room Database + Jetpack Compose Material 3",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
private fun SourceItem(title: String, subtitle: String) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Text(
      text = "• $title",
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 12.dp)
    )
  }
}
