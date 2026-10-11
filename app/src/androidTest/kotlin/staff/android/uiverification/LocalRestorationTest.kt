package staff.android.uiverification

import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.AnnotatedString
import org.junit.Rule
import org.junit.Test

@Composable
fun SaveableDraftProbe() {
    var raw by rememberSaveable { mutableStateOf("2") }
    OutlinedTextField(raw, { raw = it }, Modifier.testTag("local_draft"))
}

class LocalRestorationTest {
    @get:Rule val ui = createComposeRule()
    @Test fun emptyLocalSavedValueSurvivesReconstruction() {
        val restoration = StateRestorationTester(ui)
        restoration.setContent { ShopTheme { SaveableDraftProbe() } }
        ui.onNodeWithTag("local_draft").performTextReplacement("")
        restoration.emulateSavedInstanceStateRestore()
        ui.onNodeWithTag("local_draft").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.EditableText, AnnotatedString("")
        ))
    }
}
