package eu.kanade.tachiyomi.data.track.local

// KMK -->
import android.app.Application
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.track.BaseTracker
import eu.kanade.tachiyomi.data.track.CreatableTracker
import eu.kanade.tachiyomi.data.track.DeletableTracker
import eu.kanade.tachiyomi.data.track.model.TrackMangaMetadata
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.IOException
import java.util.concurrent.TimeUnit
import tachiyomi.domain.track.model.Track as DomainTrack

class LocalManga(id: Long) : BaseTracker(id, "Local"), CreatableTracker, DeletableTracker {

    companion object {
        const val READING = 1L
        const val COMPLETED = 2L
        const val ON_HOLD = 3L
        const val DROPPED = 4L
        const val PLAN_TO_READ = 5L
        private val SCORES = (listOf("-") + (1..10).map { it.toString() }).toImmutableList()
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val serverUrl get() = trackPreferences.localTrackerUrl().get().trimEnd('/')
    private val localClient by lazy {
        client.newBuilder()
            .dns(Dns.SYSTEM)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    override val supportsReadingDates = false
    override fun getLogo(): Int = R.drawable.brand_local
    override fun getStatusList(): List<Long> = listOf(READING, COMPLETED, ON_HOLD, DROPPED, PLAN_TO_READ)
    override fun getStatus(status: Long): StringResource? = when (status) {
        READING -> MR.strings.reading
        COMPLETED -> MR.strings.completed
        ON_HOLD -> MR.strings.on_hold
        DROPPED -> MR.strings.dropped
        PLAN_TO_READ -> MR.strings.plan_to_read
        else -> null
    }
    override fun getReadingStatus() = READING
    override fun getRereadingStatus() = -1L
    override fun getCompletionStatus() = COMPLETED
    override fun getScoreList(): ImmutableList<String> = SCORES
    override fun indexToScore(index: Int): Double = index.toDouble()
    override fun displayScore(track: DomainTrack): String =
        if (track.score <= 0) "-" else track.score.toInt().toString()

    override val isLoggedIn: Boolean get() = serverUrl.isNotBlank()
    override val isLoggedInFlow: Flow<Boolean> by lazy {
        trackPreferences.localTrackerUrl().changes().map { it.isNotBlank() }
    }

    override suspend fun login(username: String, password: String) {
        saveServerUrl(username)
    }

    override fun logout() {
        super.logout()
        trackPreferences.localTrackerUrl().set("")
    }

    override fun hasNotStartedReading(status: Long): Boolean = status == PLAN_TO_READ

    fun saveServerUrl(value: String) {
        trackPreferences.localTrackerUrl().set(normalizeServerUrl(value))
    }

    fun normalizedServerUrl(value: String = serverUrl): String = normalizeServerUrl(value)

    private fun normalizeServerUrl(value: String): String {
        val trimmed = value.trim().trimEnd('/')
        if (trimmed.isEmpty()) return ""
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
        val parsed = withScheme.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Enter a valid server address")
        if (parsed.encodedPath != "/" || parsed.query != null || parsed.fragment != null) {
            throw IllegalArgumentException("Enter a server address without a path")
        }
        return parsed.newBuilder().encodedPath("/").build().toString().trimEnd('/')
    }

    suspend fun testConnection(url: String = serverUrl) = withIOContext {
        val normalized = normalizeServerUrl(url)
        if (normalized.isBlank()) throw IOException("Enter a server address")
        request(normalized, "/api/statuses").use { response ->
            if (!response.isSuccessful) throw IOException("Server returned HTTP ${response.code}")
        }
    }

    override suspend fun search(query: String): List<TrackSearch> = withIOContext {
        val searchUrl = "$serverUrl/api/search".toHttpUrl().newBuilder().addQueryParameter("q", query).build()
        try {
            localClient.newCall(Request.Builder().url(searchUrl).build()).execute().use { response ->
                response.requireSuccess()
                JSONArray(response.body.string()).toTrackSearches()
            }
        } catch (e: IOException) {
            throw IOException(
                Injekt.get<Application>().stringResource(KMR.strings.local_tracker_search_connection_error),
                e,
            )
        }
    }

    override suspend fun create(
        title: String,
        description: String,
        coverImage: ByteArray?,
        coverMimeType: String?,
    ): TrackSearch = withIOContext {
        val payload = JSONObject().apply {
            put("title", title.trim())
            put("description", description.trim())
            put("status", "plan_to_read")
            put("chapter", 0)
            put("score", JSONObject.NULL)
        }
        val item = request(serverUrl, "/api/manga", "POST", payload).use { response ->
            response.requireSuccess()
            JSONObject(response.body.string()).toTrackSearch()
        }
        if (coverImage != null) {
            val body = coverImage.toRequestBody((coverMimeType ?: "image/jpeg").toMediaType())
            localClient.newCall(
                Request.Builder()
                    .url("${normalizeServerUrl(serverUrl)}/api/manga/${item.remote_id}/cover")
                    .post(body)
                    .build(),
            ).execute().use { it.requireSuccess() }
        }
        item
    }

    override suspend fun delete(track: DomainTrack) = withIOContext {
        request(serverUrl, "/api/manga/${track.remoteId}", "DELETE").use { it.requireSuccess() }
    }

    override suspend fun bind(track: Track, hasReadChapters: Boolean): Track = withIOContext {
        fetch(track.remote_id).applyTo(track, serverUrl)
    }

    override suspend fun update(track: Track, didReadChapter: Boolean): Track = withIOContext {
        val payload = JSONObject().apply {
            put("status", track.status.toServerStatus())
            put("chapter", track.last_chapter_read.toInt())
            put("score", if (track.score <= 0) JSONObject.NULL else track.score.toInt())
        }
        request(serverUrl, "/api/manga/${track.remote_id}", "PUT", payload).use { response ->
            response.requireSuccess()
            JSONObject(response.body.string()).toLocalEntry().applyTo(track, serverUrl)
        }
    }

    override suspend fun refresh(track: Track): Track = withIOContext {
        fetch(track.remote_id).applyTo(track, serverUrl)
    }

    override suspend fun getMangaMetadata(track: DomainTrack): TrackMangaMetadata = withIOContext {
        val manga = fetch(track.remoteId)
        TrackMangaMetadata(
            remoteId = manga.id,
            title = manga.title,
            thumbnailUrl = "$serverUrl/api/cover?id=${manga.id}",
            description = manga.description,
        )
    }

    private suspend fun fetch(id: Long): LocalEntry = withIOContext {
        request(serverUrl, "/api/manga/$id").use { response ->
            response.requireSuccess()
            JSONObject(response.body.string()).toLocalEntry()
        }
    }

    private fun request(base: String, path: String, method: String = "GET", json: JSONObject? = null) =
        localClient.newCall(
            Request.Builder()
                .url("${normalizeServerUrl(base)}$path")
                .method(
                    method,
                    when {
                        method == "GET" -> null
                        json != null -> json.toString().toRequestBody(JSON_MEDIA_TYPE)
                        else -> "".toRequestBody(JSON_MEDIA_TYPE)
                    },
                )
                .build(),
        ).execute()

    private fun okhttp3.Response.requireSuccess() {
        if (!isSuccessful) throw IOException("Local tracker returned HTTP $code")
    }

    private fun JSONArray.toTrackSearches(): List<TrackSearch> = List(length()) { getJSONObject(it).toTrackSearch() }

    private fun JSONObject.toTrackSearch() = TrackSearch.create(id).apply {
        remote_id = getLong("id")
        title = optString("title")
        summary = optString("description")
        cover_url = "$serverUrl/api/cover?id=$remote_id"
        tracking_url = "$serverUrl/?id=$remote_id"
        status = optString("status").toAppStatus()
        last_chapter_read = optDouble("chapter", 0.0).takeIf { !it.isNaN() } ?: 0.0
        score = optDouble("score", 0.0).takeIf { !it.isNaN() } ?: 0.0
    }

    private data class LocalEntry(
        val id: Long,
        val title: String,
        val description: String,
        val status: Long,
        val chapter: Double,
        val score: Double,
    ) {
        fun applyTo(track: Track, baseUrl: String): Track = track.apply {
            remote_id = this@LocalEntry.id
            title = this@LocalEntry.title
            status = this@LocalEntry.status
            last_chapter_read = chapter
            total_chapters = 0
            score = this@LocalEntry.score
            tracking_url = "$baseUrl/?id=${this@LocalEntry.id}"
        }
    }

    private fun JSONObject.toLocalEntry() = LocalEntry(
        id = getLong("id"),
        title = optString("title"),
        description = optString("description"),
        status = optString("status").toAppStatus(),
        chapter = optDouble("chapter", 0.0).takeIf { !it.isNaN() } ?: 0.0,
        score = optDouble("score", 0.0).takeIf { !it.isNaN() } ?: 0.0,
    )

    private fun Long.toServerStatus() = when (this) {
        READING -> "reading"
        COMPLETED -> "completed"
        ON_HOLD -> "on_hold"
        DROPPED -> "dropped"
        else -> "plan_to_read"
    }

    private fun String.toAppStatus() = when (this) {
        "reading" -> READING
        "completed" -> COMPLETED
        "on_hold" -> ON_HOLD
        "dropped" -> DROPPED
        else -> PLAN_TO_READ
    }
}
// KMK <--
