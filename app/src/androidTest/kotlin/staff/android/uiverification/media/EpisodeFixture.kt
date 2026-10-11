package staff.android.uiverification.media

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.test.junit4.ComposeContentTestRule

class EpisodeFixture {
    val episodes = mutableStateOf(listOf(
        EpisodeUi("episode-a", "Field notes", false, 30),
        EpisodeUi("episode-b", "Field notes", false, 60),
    ))
    val calls = mutableListOf<String>()
    fun show(compose: ComposeContentTestRule, variant: EpisodeVariant) {
        compose.setContent {
            val inputMode = LocalInputModeManager.current
            SideEffect { inputMode.requestInputMode(InputMode.Keyboard) }
            MaterialTheme {
                EpisodeQueue(episodes.value, { calls += it }, variant)
            }
        }
    }
}
