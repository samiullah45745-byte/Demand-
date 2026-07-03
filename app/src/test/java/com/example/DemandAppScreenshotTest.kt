package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class DemandAppScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun demandItemCard_screenshot() {
    composeTestRule.setContent { 
        MyApplicationTheme { 
            DemandItemCard(
                item = com.example.data.DemandItem(id = 1, name = "ٹماٹر (Tomato)", unit = "کلو", quantity = "15"),
                onQuantityChanged = {},
                onDelete = {}
            ) 
        } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/demand_item_card.png")
  }
}
