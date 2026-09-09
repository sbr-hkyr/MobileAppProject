package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.tdm.viewmodel.TdmViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmptyStartupTest {
  @Test fun newSessionHasNoPatientOrCalculatedResult() {
    val vm = TdmViewModel(ApplicationProvider.getApplicationContext<Application>())
    val input = vm.input.value
    assertEquals("", input.patient.patientName)
    assertEquals("", input.patient.patientId)
    assertEquals(0.0, input.patient.ageYears, 0.0)
    assertEquals(0.0, input.regimen.doseMg, 0.0)
    assertNull(input.preDoseConcMgL)
    assertNull(input.postDoseConcMgL)
    assertNull(input.preDoseSampleDelayHoursBeforeNextDose)
    assertNull(input.postDoseSampleDelayHoursAfterInfusionEnd)
    assertNull(vm.pkResult.value)
  }
}
