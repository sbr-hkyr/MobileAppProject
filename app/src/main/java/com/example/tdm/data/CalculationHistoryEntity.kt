package com.example.tdm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val patientName: String,
  val patientId: String,
  val workflow: String,
  val doseMg: Double,
  val intervalHours: Double,
  val infusionHours: Double,
  val crCl: Double,
  val ke: Double,
  val halfLife: Double,
  val vd: Double,
  val cMax: Double,
  val cMin: Double,
  val auc24: Double,
  val aucStatus: String,
  val recommendation: String,
  val labReportImageUri: String? = null
)
