package com.nuvio.app.features.details.components

import com.nuvio.app.features.player.PlayerPlaybackSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeroTrailerPlaybackObserverTest {
    private val events = mutableListOf<String>()
    private val observer = newObserver()

    @Test
    fun `does not reveal an idle or buffering player`() {
        observer.onSnapshot(PlayerPlaybackSnapshot())
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false))
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = true, durationMs = 60_000L))

        assertTrue(events.isEmpty())
    }

    @Test
    fun `reports readiness once across polling and rebuffering`() {
        val playing = PlayerPlaybackSnapshot(isLoading = false, isPlaying = true, durationMs = 60_000L)
        observer.onSnapshot(playing)
        observer.onSnapshot(playing)
        observer.onSnapshot(playing.copy(isLoading = true))
        observer.onSnapshot(playing)

        assertEquals(listOf("ready"), events)
    }

    @Test
    fun `a paused prepared trailer is ready without resuming it`() {
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false, durationMs = 60_000L))

        assertEquals(listOf("ready"), events)
    }

    @Test
    fun `a playing trailer without known duration is ready`() {
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false, isPlaying = true))

        assertEquals(listOf("ready"), events)
    }

    @Test
    fun `reports completion once and ignores later snapshots`() {
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false, isPlaying = true))
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false, isEnded = true))
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false, isEnded = true))
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false, isPlaying = true))
        observer.onError("Late error")

        assertEquals(listOf("ready", "ended"), events)
    }

    @Test
    fun `an already ended trailer is not reported ready`() {
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false, isEnded = true, durationMs = 60_000L))

        assertEquals(listOf("ended"), events)
    }

    @Test
    fun `ignores cleared errors and reports a failure only once`() {
        observer.onError(null)
        observer.onError("")
        observer.onError(" ")
        assertTrue(events.isEmpty())

        observer.onError("Playback failed")
        observer.onError("Playback failed")
        observer.onSnapshot(PlayerPlaybackSnapshot(isLoading = false, isPlaying = true))
        observer.onSnapshot(PlayerPlaybackSnapshot(isEnded = true))

        assertEquals(listOf("error"), events)
    }

    @Test
    fun `a new source can become ready after the previous source ended`() {
        observer.onSnapshot(PlayerPlaybackSnapshot(isEnded = true))
        newObserver().onSnapshot(PlayerPlaybackSnapshot(isLoading = false, isPlaying = true))

        assertEquals(listOf("ended", "ready"), events)
    }

    private fun newObserver() = HeroTrailerPlaybackObserver(
        onReady = { events += "ready" },
        onEnded = { events += "ended" },
        onError = { events += "error" },
    )
}
