package com.nuvio.app.features.details.components

import com.nuvio.app.features.player.PlayerPlaybackSnapshot

internal class HeroTrailerPlaybackObserver(
    private val onReady: () -> Unit,
    private val onEnded: () -> Unit,
    private val onError: () -> Unit,
) {
    private var ready = false
    private var finished = false

    fun onSnapshot(snapshot: PlayerPlaybackSnapshot) {
        if (finished) return
        if (snapshot.isEnded) {
            finished = true
            onEnded()
        } else if (!ready && !snapshot.isLoading &&
            (snapshot.isPlaying || snapshot.durationMs > 0L || snapshot.positionMs > 0L)
        ) {
            ready = true
            onReady()
        }
    }

    fun onError(message: String?) {
        if (finished || message.isNullOrBlank()) return
        finished = true
        onError()
    }
}
