package com.example.tdm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdm.model.PkResult
import java.util.Locale
import kotlin.math.exp
import kotlin.math.max

@Composable
fun PkCurveChart(
  pkResult: PkResult,
  intervalHours: Double,
  infusionHours: Double,
  modifier: Modifier = Modifier
) {
  var selectedTime by remember { mutableStateOf<Double?>(null) }

  val ke = pkResult.kePerHour
  val cMax = pkResult.cMaxMgL
  val cMin = pkResult.cMinMgL
  val postSample = pkResult.measuredPostSampleConc
  val preSample = pkResult.measuredPreSampleConc

  // Calculate maximum concentration on Y axis with headroom
  val yMax = max(45.0, (cMax * 1.25).coerceAtMost(100.0))
  val tau = intervalHours

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("pk_curve_chart_card"),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    tonalElevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Steady-State Concentration-Time Profile",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Single Dosing Interval (τ = ${tau.toInt()}h) with Target Zone",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Chart Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        LegendItem(color = Color(0xFF00897B), label = "PK Curve")
        LegendItem(color = Color(0x334CAF50), label = "Target Trough (10-20 mg/L)")
        LegendItem(color = Color(0xFFE53935), label = "Measured Sample")
      }

      Spacer(modifier = Modifier.height(12.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
      ) {
        val curveColor = MaterialTheme.colorScheme.primary
        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        val targetZoneColor = Color(0x2E4CAF50)
        val markerColor = Color(0xFFDE350B)

        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(tau) {
              detectTapGestures(
                onTap = { offset ->
                  val chartLeft = 80f
                  val chartRight = size.width - 20f
                  if (offset.x in chartLeft..chartRight) {
                    val ratio = (offset.x - chartLeft) / (chartRight - chartLeft)
                    selectedTime = (ratio * tau).coerceIn(0.0, tau)
                  }
                }
              )
            }
            .pointerInput(tau) {
              detectDragGestures { change, _ ->
                val chartLeft = 80f
                val chartRight = size.width - 20f
                val clampedX = change.position.x.coerceIn(chartLeft, chartRight)
                val ratio = (clampedX - chartLeft) / (chartRight - chartLeft)
                selectedTime = (ratio * tau).coerceIn(0.0, tau)
              }
            }
        ) {
          val paddingLeft = 80f
          val paddingBottom = 50f
          val paddingTop = 20f
          val paddingRight = 20f

          val plotWidth = size.width - paddingLeft - paddingRight
          val plotHeight = size.height - paddingTop - paddingBottom

          // Function to project (time, conc) to Canvas (x, y)
          fun toCanvasX(t: Double): Float = paddingLeft + (t / tau * plotWidth).toFloat()
          fun toCanvasY(c: Double): Float = paddingTop + ((yMax - c) / yMax * plotHeight).toFloat()

          // 1. Shaded Therapeutic Target Zone: 10 to 20 mg/L
          val yTargetTop = toCanvasY(20.0)
          val yTargetBottom = toCanvasY(10.0)
          drawRect(
            color = targetZoneColor,
            topLeft = Offset(paddingLeft, yTargetTop),
            size = androidx.compose.ui.geometry.Size(plotWidth, yTargetBottom - yTargetTop)
          )

          // 2. Grid lines & Y Axis labels
          val ySteps = listOf(0.0, 10.0, 20.0, 30.0, 40.0)
          val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            isAntiAlias = true
          }

          ySteps.forEach { conc ->
            if (conc <= yMax) {
              val y = toCanvasY(conc)
              drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
              )
              drawContext.canvas.nativeCanvas.drawText(
                "${conc.toInt()} mg/L",
                10f,
                y + 8f,
                textPaint
              )
            }
          }

          // 3. Time Grid lines (X Axis)
          val timeSteps = listOf(0.0, infusionHours, tau / 2, tau)
          timeSteps.distinct().forEach { t ->
            val x = toCanvasX(t)
            drawLine(
              color = gridColor,
              start = Offset(x, paddingTop),
              end = Offset(x, size.height - paddingBottom),
              strokeWidth = 1.5f,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
            drawContext.canvas.nativeCanvas.drawText(
              "${String.format(Locale.US, "%.1f", t)}h",
              x - 20f,
              size.height - 10f,
              textPaint
            )
          }

          // 4. Pharmacokinetic Curve Path
          // For t in [0, t_inf]: rising during infusion from cMin to cMax
          // For t in [t_inf, tau]: mono-exponential decay C(t) = cMax * exp(-ke * (t - t_inf))
          val curvePath = Path()
          val sampleSteps = 60
          for (i in 0..sampleSteps) {
            val t = (i.toDouble() / sampleSteps) * tau
            val conc = if (t <= infusionHours) {
              val fraction = t / infusionHours
              cMin + (cMax - cMin) * fraction
            } else {
              cMax * exp(-ke * (t - infusionHours))
            }
            val cx = toCanvasX(t)
            val cy = toCanvasY(conc)

            if (i == 0) curvePath.moveTo(cx, cy) else curvePath.lineTo(cx, cy)
          }

          drawPath(
            path = curvePath,
            color = curveColor,
            style = Stroke(width = 4.5f)
          )

          // 5. Highlight Cmax & Cmin dots
          val xPeak = toCanvasX(infusionHours)
          val yPeak = toCanvasY(cMax)
          drawCircle(color = curveColor, radius = 6f, center = Offset(xPeak, yPeak))

          val xTrough = toCanvasX(tau)
          val yTrough = toCanvasY(cMin)
          drawCircle(color = curveColor, radius = 6f, center = Offset(xTrough, yTrough))

          // 6. Draw Measured Samples if present
          if (postSample != null) {
            val tPost = (infusionHours + 1.0).coerceAtMost(tau)
            val xPost = toCanvasX(tPost)
            val yPost = toCanvasY(postSample)
            drawCircle(color = markerColor, radius = 7f, center = Offset(xPost, yPost))
          }

          if (preSample != null) {
            val tPre = (tau - 0.5).coerceAtLeast(0.0)
            val xPre = toCanvasX(tPre)
            val yPre = toCanvasY(preSample)
            drawCircle(color = markerColor, radius = 7f, center = Offset(xPre, yPre))
          }

          // 7. Interactive Scrubber cursor
          selectedTime?.let { curTime ->
            val curConc = if (curTime <= infusionHours) {
              cMin + (cMax - cMin) * (curTime / infusionHours)
            } else {
              cMax * exp(-ke * (curTime - infusionHours))
            }
            val curX = toCanvasX(curTime)
            val curY = toCanvasY(curConc)

            // Scrubber vertical line
            drawLine(
              color = Color(0xFF006875),
              start = Offset(curX, paddingTop),
              end = Offset(curX, size.height - paddingBottom),
              strokeWidth = 3f
            )

            // Intersection point
            drawCircle(color = Color(0xFF006875), radius = 8f, center = Offset(curX, curY))
            drawCircle(color = Color.White, radius = 4f, center = Offset(curX, curY))
          }
        }
      }

      // Interactive value display
      selectedTime?.let { t ->
        val conc = if (t <= infusionHours) {
          cMin + (cMax - cMin) * (t / infusionHours)
        } else {
          cMax * exp(-ke * (t - infusionHours))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Scrubber Time: ${String.format(Locale.US, "%.1f", t)}h",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "Estimated Conc: ${String.format(Locale.US, "%.1f", conc)} mg/L",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }
  }
}

@Composable
private fun LegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .background(color, RoundedCornerShape(2.dp))
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
