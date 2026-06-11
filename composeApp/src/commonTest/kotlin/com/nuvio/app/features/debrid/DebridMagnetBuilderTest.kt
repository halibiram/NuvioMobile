package com.nuvio.app.features.debrid

import com.nuvio.app.features.streams.StreamBehaviorHints
import com.nuvio.app.features.streams.StreamItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DebridMagnetBuilderTest {
    @Test
    fun `fromStream builds magnet link using infoHash when present`() {
        val stream = stream(infoHash = "1234567890123456789012345678901234567890")
        val magnet = DebridMagnetBuilder.fromStream(stream)
        assertEquals("magnet:?xt=urn:btih:1234567890123456789012345678901234567890&dn=Torrent.mkv", magnet)
    }

    @Test
    fun `fromStream extracts infoHash from torrent url when infoHash is null`() {
        val stream = stream(url = "torrent://1234567890123456789012345678901234567890/0", infoHash = null)
        val magnet = DebridMagnetBuilder.fromStream(stream)
        assertEquals("magnet:?xt=urn:btih:1234567890123456789012345678901234567890&dn=Torrent.mkv", magnet)
    }

    @Test
    fun `fromStream extracts infoHash from magnet url when infoHash is null`() {
        val stream = stream(url = "magnet:?xt=urn:btih:1234567890123456789012345678901234567890&dn=Test", infoHash = null)
        val magnet = DebridMagnetBuilder.fromStream(stream)
        assertEquals("magnet:?xt=urn:btih:1234567890123456789012345678901234567890&dn=Test", magnet)
    }

    @Test
    fun `fromStream returns null for non-torrent and non-magnet stream`() {
        val stream = stream(url = "https://example.com/video.mkv", infoHash = null)
        val magnet = DebridMagnetBuilder.fromStream(stream)
        assertNull(magnet)
    }

    private fun stream(
        name: String = "Torrent",
        url: String? = null,
        infoHash: String? = null,
    ): StreamItem =
        StreamItem(
            name = name,
            title = name,
            description = null,
            url = url,
            infoHash = infoHash,
            fileIdx = null,
            externalUrl = null,
            behaviorHints = StreamBehaviorHints(
                filename = "$name.mkv",
            ),
            addonName = "Addon",
            addonId = "addon:id",
        )
}
