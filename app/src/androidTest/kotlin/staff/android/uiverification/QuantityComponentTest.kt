package staff.android.uiverification

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class QuantityComponentTest {
    @get:Rule val ui = createComposeRule()
    private val initial = EditorUi("camera", "3", 0, AcceptedQuantity())

    @Test fun validationMeaningAndCommandAreSeparateAssertions() {
        val state = mutableStateOf(initial)
        var confirms = 0
        ui.setContent {
            ShopTheme {
                QuantityScreen(state.value, { state.value = state.value.copy(raw = it) },
                    { confirms++ }, {}, {})
            }
        }
        ui.onNodeWithTag("quantity_input").performTextReplacement("")
        ui.onNodeWithTag("quantity_input").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.Error, "Enter a whole quantity from 0 to 5."
        ))
        ui.onNodeWithTag("confirm").assertIsNotEnabled()
        ui.runOnIdle { assertEquals(0, confirms) }
        ui.onNodeWithTag("quantity_input").performTextReplacement("3")
        ui.onNodeWithTag("confirm").assertIsEnabled().assertHasClickAction()
            .assertTextContains("Confirm").performClick()
        ui.runOnIdle {
            assertEquals(1, confirms)
            assertEquals(2, state.value.accepted!!.quantity)
        }
    }

    @Test fun anExistingActionMustAlsoFitInsideTheScrolledWindow() {
        var confirms = 0
        ui.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(320.dp, 360.dp))
                    then DeviceConfigurationOverride.FontScale(2f)
            ) {
                ShopTheme {
                    Box(Modifier.fillMaxSize().testTag("fixture_window")) {
                        QuantityScreen(initial, {}, { confirms++ }, {}, {})
                    }
                }
            }
        }
        ui.onNodeWithTag("confirm").assertExists().performScrollTo().assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
        val window = ui.onNodeWithTag("fixture_window").getBoundsInRoot()
        val button = ui.onNodeWithTag("confirm").getUnclippedBoundsInRoot()
        assertTrue(button.top >= window.top && button.bottom <= window.bottom)
        assertTrue(button.left >= window.left && button.right <= window.right)
        ui.onNodeWithTag("confirm").performClick()
        ui.runOnIdle { assertEquals(1, confirms) }
    }

    @Test fun keyboardTraversalReachesAndActivatesConfirm() {
        lateinit var inputMode: InputModeManager
        var confirms = 0
        ui.setContent {
            inputMode = LocalInputModeManager.current
            ShopTheme { QuantityScreen(initial, {}, { confirms++ }, {}, {}) }
        }
        ui.runOnIdle { inputMode.requestInputMode(InputMode.Keyboard) }
        ui.onNodeWithTag("quantity_input")
            .performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            .performKeyInput { pressKey(Key.Tab) }
        ui.onNodeWithTag("confirm").assertIsFocused()
            .performKeyInput { pressKey(Key.Enter) }
        ui.runOnIdle { assertEquals(1, confirms) }
    }
}
