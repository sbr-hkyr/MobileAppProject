package com.example

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.tdm.model.FictionalCasesRepository
import com.example.tdm.viewmodel.AppTab
import com.example.tdm.viewmodel.TdmViewModel
import java.time.Duration
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalculationLoadingTest {
  private fun ready(): TdmViewModel = TdmViewModel(ApplicationProvider.getApplicationContext<Application>()).apply {
    loadFictionalCase(FictionalCasesRepository.sampleCases[0])
    setTab(AppTab.CALCULATOR)
  }
  @Test fun preparesThenShowsResults() {
    val vm = ready()
    vm.prepareResults()
    vm.prepareResults()
    assertTrue(vm.isCalculating.value)
    assertEquals(AppTab.CALCULATOR, vm.activeTab.value)
    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
    assertFalse(vm.isCalculating.value)
    assertEquals(AppTab.RESULTS, vm.activeTab.value)
  }
  @Test fun cancelledPreparationDoesNotNavigate() {
    val vm = ready()
    vm.prepareResults()
    vm.cancelCalculation()
    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
    assertFalse(vm.isCalculating.value)
    assertEquals(AppTab.CALCULATOR, vm.activeTab.value)
  }
}
