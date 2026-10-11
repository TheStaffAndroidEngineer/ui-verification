package staff.android.uiverification

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationObservationTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private val repository = ScriptedCartRepository()
    private val driver = HybridDriver(ui, repository)

    @Before fun installFixture() {
        val broken = InstrumentationRegistry.getArguments()
            .getString("brokenReturn") == "true"
        driver.install(brokenReturn = broken)
    }

    @Test fun acceptedQuantityAloneMissesTheExtraEntry() {
        driver.confirmThree()
        driver.assertAcceptedCart()
        assertEquals(3, repository.records.value.getValue("camera").quantity)
    }

    @Test fun nextBackMustReachCatalog() {
        driver.confirmThree()
        driver.assertAcceptedCart()
        driver.back()
        assertEquals("Completed editor must be absent from history",
            R.id.catalog, driver.destination())
        onView(withId(R.id.open_cart)).check(matches(isDisplayed()))
        assertEquals(listOf(ConfirmCall("camera", 3, 0)), repository.calls)
    }
}
