package staff.android.uiverification.exercise

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import staff.android.uiverification.ExplicitSelection
import staff.android.uiverification.media.EpisodeFixture
import staff.android.uiverification.media.EpisodeVariant

// Deliberately weak: one callback does not establish its recipient.
fun verifyEpisodeQueue(
    compose: ComposeContentTestRule,
    variant: EpisodeVariant,
) {
    val fixture = EpisodeFixture()
    fixture.show(compose, variant)
    compose.onAllNodesWithText("Play").onFirst().performClick()
    compose.runOnIdle { assertEquals(1, fixture.calls.size) }
}

@ExplicitSelection
class EpisodeLearnerTest {
    @get:Rule val compose = createComposeRule()
    @Test fun correct() = verifyEpisodeQueue(compose, EpisodeVariant.Correct)
    @Test fun firstVisible() = verifyEpisodeQueue(compose, EpisodeVariant.FirstVisibleId)
    @Test fun initialPosition() = verifyEpisodeQueue(compose, EpisodeVariant.InitialPosition)
    @Test fun progressEmits() = verifyEpisodeQueue(compose, EpisodeVariant.ProgressEmitsPlay)
}
