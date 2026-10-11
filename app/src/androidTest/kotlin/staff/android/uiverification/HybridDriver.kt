package staff.android.uiverification

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule

class HybridDriver(
    val ui: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
    val repository: ScriptedCartRepository,
) {
    fun install(brokenReturn: Boolean = false) {
        ui.activityRule.scenario.onActivity { activity ->
            (activity.application as ShopApplication).apply {
                this.repository = this@HybridDriver.repository
                composeEditor = true
                this.brokenReturn = brokenReturn
            }
        }
    }

    fun destination(): Int {
        var id = 0
        ui.activityRule.scenario.onActivity {
            id = it.navigation.currentDestination!!.id
        }
        return id
    }

    fun at(id: Int) {
        ui.waitUntil(8_000) { destination() == id }
        ui.waitForIdle()
    }

    fun openCamera() {
        onView(withId(R.id.open_cart)).perform(click())
        at(R.id.cart)
        onView(withId(R.id.open_camera)).perform(click())
        at(R.id.quantity)
        ui.onNodeWithTag("quantity_input").assertExists()
    }

    fun confirmThree() {
        openCamera()
        ui.onNodeWithTag("quantity_input").performTextReplacement("3")
        ui.onNodeWithTag("confirm").performClick()
    }

    fun assertAcceptedCart() {
        at(R.id.cart)
        onView(withId(R.id.open_camera)).check(matches(isDisplayed()))
        onView(withId(R.id.open_camera)).check(matches(withText(
            ui.activity.getString(R.string.cart_line, "Camera", 3)
        )))
    }

    fun back() {
        ui.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        ui.waitForIdle()
    }
}
