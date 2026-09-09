package com.example

import androidx.compose.ui.test.assertIsSelected
import androidx.test.espresso.Espresso.pressBack
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Starts the real activity and ViewModel, including Room, without cloud configuration. */
@RunWith(AndroidJUnit4::class)
class StandaloneStartupTest {
  @get:Rule val compose = createAndroidComposeRule<MainActivity>()

  @Test fun startsAndNavigatesWithoutCloudConfiguration() {
    compose.onNodeWithTag("tdm_app_root").assertIsDisplayed()
    compose.onNodeWithTag("nav_item_results").performClick()
    compose.onNodeWithTag("tdm_app_root").assertIsDisplayed()
    compose.onNodeWithTag("nav_item_calculator").performClick()
    compose.activityRule.scenario.recreate()
    compose.onNodeWithTag("tdm_app_root").assertIsDisplayed()
  }

  @Test fun backReturnsThroughVisitedScreensAfterRecreation() {
    compose.onNodeWithTag("nav_item_camera").performClick()
    compose.onNodeWithTag("nav_item_results").performClick()
    compose.activityRule.scenario.recreate()
    pressBack()
    compose.onNodeWithTag("nav_item_camera").assertIsSelected()
    pressBack()
    compose.onNodeWithTag("nav_item_calculator").assertIsSelected()
    compose.onNodeWithTag("tdm_app_root").assertIsDisplayed()
  }

  @Test fun backClosesDrawerBeforeLeavingScreen() {
    compose.onNodeWithTag("nav_item_camera").performClick()
    compose.onNodeWithTag("open_side_bar_button").performClick()
    pressBack()
    compose.onNodeWithTag("nav_item_camera").assertIsSelected()
    pressBack()
    compose.onNodeWithTag("nav_item_calculator").assertIsSelected()
  }
}
