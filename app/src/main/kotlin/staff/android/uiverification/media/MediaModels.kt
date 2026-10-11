package staff.android.uiverification.media

data class EpisodeUi(
    val id: String,
    val title: String,
    val playing: Boolean,
    val downloadPercent: Int,
) {
    init {
        require(id.isNotBlank() && title.isNotBlank())
        require(downloadPercent in 0..100)
    }
}

enum class EpisodeVariant {
    Correct, FirstVisibleId, InitialPosition, ProgressEmitsPlay
}
