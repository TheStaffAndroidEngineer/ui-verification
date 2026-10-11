package staff.android.uiverification

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

data class ConfirmCall(val productId: String, val quantity: Int, val revision: Long)

/** Test-only store. Each held command needs one explicit success or failure. */
class ScriptedCartRepository : CartRepository {
    override val records = MutableStateFlow(
        Products.all.associate { it.id to AcceptedQuantity() }
    )
    var holdWrites = false
    @Volatile var calls: List<ConfirmCall> = emptyList()
        private set
    @Volatile private var pending: CompletableDeferred<Unit>? = null

    override suspend fun confirm(id: String, quantity: Int, revision: Long) {
        require(Products.find(id) != null && quantity in 0..5)
        check(pending == null) { "A second command started before the first ended" }
        val gate = if (holdWrites) CompletableDeferred<Unit>() else null
        pending = gate
        calls = calls + ConfirmCall(id, quantity, revision)
        try {
            gate?.await()
            val accepted = records.value.getValue(id)
            if (accepted.revision != revision) throw RevisionMismatch()
            if (accepted.quantity != quantity) records.value = records.value +
                (id to AcceptedQuantity(quantity, accepted.revision + 1))
        } finally {
            pending = null
        }
    }

    fun succeed() { checkNotNull(pending).complete(Unit) }
    fun fail(error: IOException) { checkNotNull(pending).completeExceptionally(error) }

    override suspend fun reset() {
        check(pending == null)
        records.value = Products.all.associate { it.id to AcceptedQuantity() }
        calls = emptyList()
    }
}
