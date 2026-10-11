package staff.android.uiverification.solution

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import staff.android.uiverification.ExplicitSelection
import staff.android.uiverification.media.EpisodeFixture
import staff.android.uiverification.media.EpisodeVariant

fun verifyEpisodeQueue(
    compose: ComposeContentTestRule,
    variant: EpisodeVariant,
) {
    val fixture = EpisodeFixture()
    fixture.show(compose, variant)
    compose.onAllNodesWithText("Field notes").assertCountEquals(2)
    compose.runOnIdle {
        assertEquals("initial rendering", emptyList<String>(), fixture.calls)
    }
    val playB = compose.onNodeWithTag("episode-play:episode-b")
    playB.assertTextContains("Play").assertHasClickAction()
        .assertIsEnabled().performClick()
    compose.runOnIdle {
        assertEquals("before reorder", listOf("episode-b"), fixture.calls)
        fixture.calls.clear()
        fixture.episodes.value = fixture.episodes.value.reversed()
    }
    compose.runOnIdle {
        assertEquals("reorder is not an action", emptyList<String>(), fixture.calls)
    }
    playB.performClick()
    compose.runOnIdle {
        assertEquals("after reorder", listOf("episode-b"), fixture.calls)
        fixture.calls.clear()
    }
    playB.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
    playB.assertIsFocused()
    compose.runOnIdle {
        fixture.episodes.value = fixture.episodes.value.map {
            if (it.id == "episode-b") it.copy(downloadPercent = 61) else it
        }
    }
    playB.assertIsFocused()
    compose.runOnIdle {
        assertEquals("progress is not an action", emptyList<String>(), fixture.calls)
    }
    playB.performKeyInput { pressKey(Key.Enter) }
    compose.runOnIdle {
        assertEquals("keyboard recipient", listOf("episode-b"), fixture.calls)
    }
}

abstract class EpisodeReferenceContract {
    @get:Rule val compose = createComposeRule()
    protected abstract val variant: EpisodeVariant
    @Test fun preservesIdentityThroughReorderAndProgress() =
        verifyEpisodeQueue(compose, variant)
}

class EpisodeReferenceTest : EpisodeReferenceContract() {
    override val variant = EpisodeVariant.Correct
}

@ExplicitSelection
class EpisodeFirstVisibleMutantTest : EpisodeReferenceContract() {
    override val variant = EpisodeVariant.FirstVisibleId
}

@ExplicitSelection
class EpisodeInitialPositionMutantTest : EpisodeReferenceContract() {
    override val variant = EpisodeVariant.InitialPosition
}

@ExplicitSelection
class EpisodeProgressMutantTest : EpisodeReferenceContract() {
    override val variant = EpisodeVariant.ProgressEmitsPlay
}
