package staff.android.uiverification

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Same-run capture sensitivity, not an approved regression baseline. */
class ComponentCaptureTest {
    @get:Rule val ui = createComposeRule()
    @Test fun stablePixelsRepeatAndVisibleDraftChangeIsDetected() {
        val state = mutableStateOf(EditorUi("camera", "3", 0, AcceptedQuantity()))
        ui.setContent {
            ShopTheme {
                Box(Modifier.width(320.dp).height(600.dp).testTag("snapshot")) {
                    QuantityScreen(state.value, {}, {}, {}, {})
                }
            }
        }
        ui.waitForIdle()
        val baseline = ui.onNodeWithTag("snapshot").captureToImage().asAndroidBitmap()
        val repeat = ui.onNodeWithTag("snapshot").captureToImage().asAndroidBitmap()
        ui.runOnIdle { state.value = state.value.copy(raw = "4") }
        ui.waitForIdle()
        val changed = ui.onNodeWithTag("snapshot").captureToImage().asAndroidBitmap()
        val output = requireNotNull(InstrumentationRegistry.getArguments()
            .getString("additionalTestOutputDir")) { "Run this capture through Gradle" }
        require(output.isNotBlank())
        val directory = File(output, "chapter10-images").apply {
            check(isDirectory || mkdirs())
        }
        listOf("baseline" to baseline, "repeat" to repeat, "changed" to changed)
            .forEach { (name, bitmap) ->
                File(directory, "$name.png").outputStream().use {
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            }
        assertTrue("Same quiescent component must repeat", baseline.sameAs(repeat))
        assertFalse("Changed visible draft must change pixels", baseline.sameAs(changed))
    }
}
