package staff.android.uiverification

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.SdkSuppress
import org.junit.After
import org.junit.Rule
import org.junit.Test

@SdkSuppress(minSdkVersion = 34)
class AutomatedAccessibilityTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @After fun clearEspressoChecks() { AccessibilityChecks.disable() }

    @Test fun sharedValidatorChecksTheHybridEditorActions() {
        val validator = AccessibilityChecks.enable().setRunChecksFromRootView(true)
        ui.enableAccessibilityChecks(validator)
        val repository = ScriptedCartRepository()
        val driver = HybridDriver(ui, repository)
        driver.install()
        onView(withId(R.id.lab_controls)).perform(scrollTo(), click())
        onView(withId(R.id.renderer_toggle)).perform(scrollTo(), click())
        onView(withId(R.id.renderer_toggle)).perform(click())
        onView(withId(R.id.lab_controls)).perform(scrollTo(), click())
        driver.openCamera()
        ui.onNodeWithTag("quantity_input").performTextReplacement("")
        ui.onNodeWithTag("confirm").assertIsNotEnabled()
        ui.onNodeWithTag("quantity_input").performTextReplacement("3")
        ui.onNodeWithTag("confirm").performClick()
        driver.assertAcceptedCart()
    }
}
