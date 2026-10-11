package staff.android.uiverification

import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class ControlledCompletionTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private val repository = ScriptedCartRepository().apply { holdWrites = true }
    private val driver = HybridDriver(ui, repository)
    @Before fun installFixture() { driver.install() }

    @Test fun virtualUiTimeDoesNotCompleteAnExternalWrite() {
        driver.confirmThree()
        ui.waitUntil(8_000) { repository.calls.size == 1 }
        ui.onNodeWithTag("confirm").assertIsNotEnabled()
        ui.onNodeWithTag("quantity_input").assertIsNotEnabled()
        ui.onNodeWithText(ui.activity.getString(R.string.saving)).assertExists()
        assertEquals(R.id.quantity, driver.destination())
        assertEquals(2, repository.records.value.getValue("camera").quantity)

        ui.mainClock.advanceTimeBy(5_000)
        ui.onNodeWithTag("confirm").performClick()
        assertEquals(listOf(ConfirmCall("camera", 3, 0)), repository.calls)
        assertEquals(2, repository.records.value.getValue("camera").quantity)
        assertEquals(R.id.quantity, driver.destination())

        repository.succeed()
        driver.assertAcceptedCart()
        driver.back()
        assertEquals(R.id.catalog, driver.destination())
        assertEquals(1, repository.calls.size)
    }

    @Test fun failedWriteRetainsDraftAndRequiresAnotherConfirm() {
        driver.confirmThree()
        ui.waitUntil(8_000) { repository.calls.size == 1 }
        repository.fail(IOException("Controlled disk failure"))
        ui.onNodeWithTag("problem").assertTextContains(
            ui.activity.getString(R.string.save_failed)
        )
        ui.onNodeWithTag("quantity_input").assertTextContains("3")
        ui.onNodeWithTag("confirm").assertIsEnabled()
        assertEquals(2, repository.records.value.getValue("camera").quantity)
        assertEquals(R.id.quantity, driver.destination())
        assertEquals(1, repository.calls.size)

        ui.onNodeWithTag("confirm").performClick()
        ui.waitUntil(8_000) { repository.calls.size == 2 }
        repository.succeed()
        driver.assertAcceptedCart()
        assertEquals(List(2) { ConfirmCall("camera", 3, 0) }, repository.calls)
    }

    @Test fun recreateAnEmptyDraftWithoutAcceptingIt() {
        driver.openCamera()
        ui.onNodeWithTag("quantity_input").performTextReplacement("")
        ui.activityRule.scenario.recreate()
        driver.at(R.id.quantity)
        ui.onNodeWithTag("quantity_input").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.EditableText, AnnotatedString("")
        ))
        ui.onNodeWithTag("confirm").assertIsNotEnabled()
        assertEquals(2, repository.records.value.getValue("camera").quantity)
        assertEquals(emptyList<ConfirmCall>(), repository.calls)
    }

    @Test fun completionAfterRecreationReturnsOnce() {
        driver.confirmThree()
        ui.waitUntil(8_000) { repository.calls.size == 1 }
        ui.activityRule.scenario.recreate()
        driver.at(R.id.quantity)
        ui.onNodeWithTag("quantity_input").assertTextContains("3")
        repository.succeed()
        driver.assertAcceptedCart()
        driver.back()
        assertEquals(R.id.catalog, driver.destination())
        assertEquals(1, repository.calls.size)
    }
}
