package com.nuvio.app.features.details.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.nuvio.app.features.player.IosPlayerPresentation
import com.nuvio.app.features.player.LocalIosPlayerPresentation
import com.nuvio.app.features.player.PlatformPlayerSurface
import com.nuvio.app.features.player.PlayerEngineController
import com.nuvio.app.features.player.PlayerPlaybackSnapshot
import com.nuvio.app.features.player.PlayerResizeMode

@Composable
actual fun HeroTrailerPlayerSurface(
    sourceUrl: String,
    sourceAudioUrl: String?,
    playWhenReady: Boolean,
    muted: Boolean,
    modifier: Modifier,
    onReady: () -> Unit,
    onEnded: () -> Unit,
    onError: () -> Unit,
    onControllerReady: (PlayerEngineController) -> Unit,
    onSnapshot: (PlayerPlaybackSnapshot) -> Unit,
    fullscreen: Boolean,
) {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val latestOnReady = rememberUpdatedState(onReady)
    val latestOnEnded = rememberUpdatedState(onEnded)
    val latestOnError = rememberUpdatedState(onError)

    key(sourceUrl, sourceAudioUrl) {
        val observer = remember {
            HeroTrailerPlaybackObserver(
                onReady = { latestOnReady.value() },
                onEnded = { latestOnEnded.value() },
                onError = { latestOnError.value() },
            )
        }
        CompositionLocalProvider(
            LocalIosPlayerPresentation provides IosPlayerPresentation(
                embedded = true,
                fullscreen = fullscreen,
                muted = muted,
            ),
        ) {
            PlatformPlayerSurface(
                sourceUrl = sourceUrl,
                sourceAudioUrl = sourceAudioUrl,
                useYoutubeChunkedPlayback = true,
                modifier = modifier,
                playWhenReady = playWhenReady && lifecycleState.isAtLeast(Lifecycle.State.RESUMED),
                resizeMode = PlayerResizeMode.Zoom,
                useNativeController = false,
                onControllerReady = onControllerReady,
                onSnapshot = {
                    onSnapshot(it)
                    observer.onSnapshot(it)
                },
                onError = observer::onError,
            )
        }
    }
}
