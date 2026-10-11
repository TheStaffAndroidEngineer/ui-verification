package staff.android.uiverification.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import staff.android.uiverification.R

@Composable
fun EpisodeQueue(
    episodes: List<EpisodeUi>,
    onPlayPause: (String) -> Unit,
    variant: EpisodeVariant = EpisodeVariant.Correct,
    modifier: Modifier = Modifier,
) {
    require(episodes.size == 2)
    require(episodes.map { it.id }.distinct().size == episodes.size)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        episodes.forEachIndexed { index, episode ->
            key(episode.id) {
                val initialIndex = remember { index }
                var previousProgress by remember {
                    mutableIntStateOf(episode.downloadPercent)
                }
                SideEffect {
                    if (variant == EpisodeVariant.ProgressEmitsPlay &&
                        previousProgress != episode.downloadPercent
                    ) onPlayPause(episode.id)
                    previousProgress = episode.downloadPercent
                }
                Column(Modifier.fillMaxWidth().padding(8.dp)
                    .testTag("episode-row:${episode.id}")) {
                    Text(episode.title)
                    val progressName = stringResource(R.string.media_progress)
                    LinearProgressIndicator(
                        progress = { episode.downloadPercent / 100f },
                        modifier = Modifier.fillMaxWidth()
                            .testTag("episode-progress:${episode.id}")
                            .semantics { contentDescription = progressName },
                    )
                    Button(
                        onClick = {
                            val id = when (variant) {
                                EpisodeVariant.FirstVisibleId -> episodes.first().id
                                EpisodeVariant.InitialPosition -> episodes[initialIndex].id
                                else -> episode.id
                            }
                            onPlayPause(id)
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                            .testTag("episode-play:${episode.id}"),
                    ) {
                        Text(stringResource(
                            if (episode.playing) R.string.media_pause
                            else R.string.media_play
                        ))
                    }
                }
            }
        }
    }
}
