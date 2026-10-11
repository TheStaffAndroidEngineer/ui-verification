package staff.android.uiverification

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EditorPolicyTest {
    @Test fun validRawInputCannotOverrideANewerAcceptedRevision() {
        val stale = EditorUi("camera", "4", 0, AcceptedQuantity(3, 1))
        assertFalse(stale.canConfirm)
        val reviewed = stale.copy(raw = "3", baseRevision = 1)
        assertTrue(reviewed.canConfirm)
        assertFalse(reviewed.copy(phase = SavePhase.Saving).canConfirm)
    }
}
