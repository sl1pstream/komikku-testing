package eu.kanade.tachiyomi.data.track

// KMK -->
import eu.kanade.tachiyomi.data.track.model.TrackSearch

/** A tracker that can create an entry directly from a search query. */
interface CreatableTracker {
    suspend fun create(
        title: String,
        description: String,
        coverImage: ByteArray?,
        coverMimeType: String?,
    ): TrackSearch
}
// KMK <--
