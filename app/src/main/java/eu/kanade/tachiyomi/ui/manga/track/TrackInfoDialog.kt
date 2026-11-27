package eu.kanade.tachiyomi.ui.manga.track

import android.app.Application
import android.content.Context
import android.graphics.Bitmap // KMK --> // KMK <--
import android.net.Uri // KMK --> // KMK <--
import androidx.activity.compose.rememberLauncherForActivityResult // KMK --> // KMK <--
import androidx.activity.result.contract.ActivityResultContracts // KMK --> // KMK <--
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background // KMK --> // KMK <--
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // KMK --> // KMK <--
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize // KMK --> // KMK <--
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // KMK --> // KMK <--
import androidx.compose.foundation.layout.offset // KMK --> // KMK <--
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size // KMK --> // KMK <--
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState // KMK --> // KMK <--
import androidx.compose.foundation.shape.RoundedCornerShape // KMK --> // KMK <--
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll // KMK --> // KMK <--
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack // KMK --> // KMK <--
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.RestartAlt // KMK --> // KMK <--
import androidx.compose.material3.Button // KMK --> // KMK <--
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // KMK --> // KMK <--
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField // KMK --> // KMK <--
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar // KMK --> // KMK <--
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect // KMK --> // KMK <--
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // KMK --> // KMK <--
import androidx.compose.ui.draw.drawBehind // KMK --> // KMK <--
import androidx.compose.ui.geometry.Offset // KMK --> // KMK <--
import androidx.compose.ui.layout.ContentScale // KMK --> // KMK <--
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.asDrawable // KMK --> // KMK <--
import coil3.compose.AsyncImage // KMK --> // KMK <--
import coil3.imageLoader // KMK --> // KMK <--
import coil3.request.ImageRequest // KMK --> // KMK <--
import coil3.request.allowHardware // KMK --> // KMK <--
import coil3.size.Size // KMK --> // KMK <--
import dev.icerock.moko.resources.StringResource
import eu.kanade.domain.track.interactor.RefreshTracks
import eu.kanade.domain.track.model.toDbTrack
import eu.kanade.domain.track.service.TrackPreferences
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.track.TrackChapterSelector
import eu.kanade.presentation.track.TrackDateSelector
import eu.kanade.presentation.track.TrackInfoDialogHome
import eu.kanade.presentation.track.TrackScoreSelector
import eu.kanade.presentation.track.TrackStatusSelector
import eu.kanade.presentation.track.TrackerSearch
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.track.CreatableTracker // KMK --> // KMK <--
import eu.kanade.tachiyomi.data.track.DeletableTracker
import eu.kanade.tachiyomi.data.track.EnhancedTracker
import eu.kanade.tachiyomi.data.track.Tracker
import eu.kanade.tachiyomi.data.track.TrackerManager
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.source.online.MetadataSource
import eu.kanade.tachiyomi.source.online.all.MergedSource
import eu.kanade.tachiyomi.util.lang.convertEpochMillisZone
import eu.kanade.tachiyomi.util.lang.toLocalDate
import eu.kanade.tachiyomi.util.system.copyToClipboard
import eu.kanade.tachiyomi.util.system.getBitmapOrNull // KMK --> // KMK <--
import eu.kanade.tachiyomi.util.system.openInBrowser
import eu.kanade.tachiyomi.util.system.toast
import exh.metadata.metadata.base.TrackerIdMetadata
import exh.source.MERGED_SOURCE_ID
import exh.source.getMainSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CancellationException // KMK --> // KMK <--
import kotlinx.coroutines.flow.MutableStateFlow // KMK --> // KMK <--
import kotlinx.coroutines.flow.StateFlow // KMK --> // KMK <--
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.util.QuerySanitizer.sanitize
import tachiyomi.core.common.util.lang.launchNonCancellable
import tachiyomi.core.common.util.lang.withIOContext
import tachiyomi.core.common.util.lang.withUIContext
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.manga.interactor.GetFlatMetadataById
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.interactor.GetMergedReferencesById
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.domain.track.interactor.DeleteTrack
import tachiyomi.domain.track.interactor.GetTracks
import tachiyomi.domain.track.model.Track
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR // KMK --> // KMK <--
import tachiyomi.presentation.core.components.LabeledCheckbox
import tachiyomi.presentation.core.components.material.AlertDialogContent
import tachiyomi.presentation.core.components.material.Scaffold // KMK --> // KMK <--
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import java.io.ByteArrayOutputStream // KMK --> // KMK <--
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import androidx.compose.ui.geometry.Size as DrawSize // KMK --> // KMK <--

data class TrackInfoDialogHomeScreen(
    private val mangaId: Long,
    private val mangaTitle: String,
    private val sourceId: Long,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val screenModel = rememberScreenModel { Model(mangaId, sourceId) }

        val dateFormat = remember { UiPreferences.dateFormat(Injekt.get<UiPreferences>().dateFormat().get()) }
        val state by screenModel.state.collectAsState()

        // SY -->
        Column(modifier = Modifier.animateContentSize()) {
            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .windowInsetsPadding(WindowInsets.systemBars),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Text(
                        stringResource(MR.strings.loading),
                        fontSize = 14.sp,
                    )
                }
            }
            // SY <--
            else {
                TrackInfoDialogHome(
                    trackItems = state.trackItems,
                    dateFormat = dateFormat,
                    onStatusClick = {
                        navigator.push(
                            TrackStatusSelectorScreen(
                                track = it.track!!,
                                serviceId = it.tracker.id,
                            ),
                        )
                    },
                    onChapterClick = {
                        navigator.push(
                            TrackChapterSelectorScreen(
                                track = it.track!!,
                                serviceId = it.tracker.id,
                            ),
                        )
                    },
                    onScoreClick = {
                        navigator.push(
                            TrackScoreSelectorScreen(
                                track = it.track!!,
                                serviceId = it.tracker.id,
                            ),
                        )
                    },
                    onStartDateEdit = {
                        navigator.push(
                            TrackDateSelectorScreen(
                                track = it.track!!,
                                serviceId = it.tracker.id,
                                start = true,
                            ),
                        )
                    },
                    onEndDateEdit = {
                        navigator.push(
                            TrackDateSelectorScreen(
                                track = it.track!!,
                                serviceId = it.tracker.id,
                                start = false,
                            ),
                        )
                    },
                    onNewSearch = {
                        if (it.tracker is EnhancedTracker) {
                            screenModel.registerEnhancedTracking(it)
                        } else {
                            // SY -->
                            screenModel.newSearch(navigator, it, mangaTitle)
                            // SY <--
                        }
                    },
                    onOpenInBrowser = { openTrackerInBrowser(context, it) },
                    onRemoved = {
                        navigator.push(
                            TrackerRemoveScreen(
                                mangaId = mangaId,
                                track = it.track!!,
                                serviceId = it.tracker.id,
                            ),
                        )
                    },
                    onCopyLink = { context.copyTrackerLink(it) },
                    onTogglePrivate = screenModel::togglePrivate,
                )
            }
        }
    }

    /**
     * Opens registered tracker url in browser
     */
    private fun openTrackerInBrowser(context: Context, trackItem: TrackItem) {
        val url = trackItem.track?.remoteUrl ?: return
        if (url.isNotBlank()) {
            context.openInBrowser(url)
        }
    }

    private fun Context.copyTrackerLink(trackItem: TrackItem) {
        val url = trackItem.track?.remoteUrl ?: return
        if (url.isNotBlank()) {
            copyToClipboard(url, url)
        }
    }

    private class Model(
        private val mangaId: Long,
        private val sourceId: Long,
        private val getTracks: GetTracks = Injekt.get(),
        // SY -->
        private val trackerManager: TrackerManager = Injekt.get(),
        private val trackPreferences: TrackPreferences = Injekt.get(),
        // SY <--
        // KMK -->
        private val sourceManager: SourceManager = Injekt.get(),
        // KMK <--
    ) : StateScreenModel<Model.State>(State()) {
        // KMK -->
        private val getFlatMetadataById: GetFlatMetadataById by injectLazy()
        private val getMangaById: GetManga by injectLazy()
        private val getMergedReferencesById: GetMergedReferencesById by injectLazy()
        // KMK <--

        init {
            screenModelScope.launch {
                refreshTrackers()
            }

            screenModelScope.launch {
                getTracks.subscribe(mangaId)
                    .catch { logcat(LogPriority.ERROR, it) }
                    .distinctUntilChanged()
                    .map { it.mapToTrackItem() }
                    .collectLatest { trackItems -> mutableState.update { it.copy(trackItems = trackItems) } }
            }
        }

        // KMK -->
        private suspend fun getMangaForTracking(item: TrackItem): Manga? {
            if (sourceId != MERGED_SOURCE_ID) {
                return getMangaById.await(mangaId)
            }
            item.tracker as EnhancedTracker
            val references = getMergedReferencesById.await(mangaId)
            return references.distinctBy { it.mangaSourceId }.firstNotNullOfOrNull { ref ->
                sourceManager.get(ref.mangaSourceId)
                    ?.takeIf(item.tracker::accept)
                    ?.let { ref.mangaId?.let { mangaId -> getMangaById.await(mangaId) } }
            }
        }
        // KMK <--

        fun registerEnhancedTracking(item: TrackItem) {
            item.tracker as EnhancedTracker
            screenModelScope.launchNonCancellable {
                val manga = getMangaForTracking(item) ?: return@launchNonCancellable
                try {
                    val matchResult = item.tracker.match(manga) ?: throw Exception()
                    item.tracker.register(matchResult, mangaId)
                } catch (_: Exception) {
                    withUIContext { Injekt.get<Application>().toast(MR.strings.error_no_match) }
                }
            }
        }

        // SY -->
        fun newSearch(navigator: Navigator, item: TrackItem, mangaTitle: String) {
            screenModelScope.launchNonCancellable {
                if (trackPreferences.resolveUsingSourceMetadata().get()) {
                    // Check if the tracker id is contained in the metadata
                    val result = getTrackerIdFromMetadata(item.tracker.id)
                    if (result != null) {
                        mutableState.update { it.copy(isLoading = true) }

                        // Try to register tracking by id
                        val success = registerTrackingById(item.tracker.id, result)

                        mutableState.update { it.copy(isLoading = false) }

                        if (success) {
                            // Return on success
                            return@launchNonCancellable
                        }
                    }
                }

                // Open search screen
                navigator.push(
                    TrackerSearchScreen(
                        mangaId = mangaId,
                        initialQuery = item.track?.title ?: mangaTitle,
                        currentUrl = item.track?.remoteUrl,
                        serviceId = item.tracker.id,
                    ),
                )
            }
        }

        suspend fun getTrackerIdFromMetadata(trackerId: Long): String? {
            try {
                val metadataSource = sourceManager.get(sourceId)
                    ?.getMainSource<MetadataSource<*, *>>() ?: return null

                return getFlatMetadataById.await(mangaId)?.run {
                    // Use 'raise' to dynamically obtain the specific metadata type and then attempt to cast
                    raise(metadataSource.metaClass) as? TrackerIdMetadata
                }?.let { metadata ->
                    when (trackerId) {
                        trackerManager.aniList.id -> metadata.anilistId
                        trackerManager.kitsu.id -> metadata.kitsuId
                        trackerManager.myAnimeList.id -> metadata.myAnimeListId
                        trackerManager.mangaUpdates.id -> metadata.mangaUpdatesId
                        else -> null
                    }
                }
            } catch (e: Throwable) {
                logcat(LogPriority.ERROR, e) { "Failed to get tracker ID from metadata" }
                return null
            }
        }

        suspend fun registerTrackingById(trackerId: Long, remoteId: String): Boolean {
            trackerManager.get(trackerId)?.let { tracker ->
                try {
                    tracker.searchById(remoteId)?.let { track ->
                        tracker.register(track, mangaId)
                        return true
                    }
                } catch (e: Throwable) {
                    logcat(LogPriority.ERROR, e) { "Failed to register tracking by id" }
                }
            }
            return false
        }
        // SY <--

        private suspend fun refreshTrackers() {
            val refreshTracks = Injekt.get<RefreshTracks>()
            val context = Injekt.get<Application>()

            refreshTracks.await(mangaId)
                .filter { it.first != null }
                .forEach { (track, e) ->
                    logcat(LogPriority.ERROR, e) {
                        "Failed to refresh track data mangaId=$mangaId for service ${track!!.id}"
                    }
                    withUIContext {
                        context.toast(
                            context.stringResource(
                                MR.strings.track_error,
                                track!!.name,
                                e.message ?: "",
                            ),
                        )
                    }
                }
        }

        fun togglePrivate(item: TrackItem) {
            screenModelScope.launchNonCancellable {
                item.tracker.setRemotePrivate(item.track!!.toDbTrack(), !item.track.private)
            }
        }

        private suspend fun List<Track>.mapToTrackItem(): List<TrackItem> {
            val loggedInTrackers = trackerManager.loggedInTrackers()
            val source = sourceManager.getOrStub(sourceId)
            // KMK -->
            return (loggedInTrackers + trackerManager.local.takeIf { it.isLoggedIn })
                .filterNotNull()
                .distinctBy { it.id }
                // KMK <--
                // Map to TrackItem
                .map { service -> TrackItem(find { it.trackerId == service.id }, service) }
                // Show only if the service supports this manga's source
                // KMK -->
                .let { trackers ->
                    val sources = if (source is MergedSource) {
                        sourceManager.getMergedSources(mangaId)
                    } else {
                        listOf(source)
                    }
                    trackers.filter { (it.tracker as? EnhancedTracker)?.accept(sources) ?: true }
                }
            // KMK <--
        }

        @Immutable
        data class State(
            val trackItems: List<TrackItem> = emptyList(),
            // SY -->
            val isLoading: Boolean = false,
            // SY <--
        )
    }
}

private data class TrackStatusSelectorScreen(
    private val track: Track,
    private val serviceId: Long,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            Model(
                track = track,
                tracker = Injekt.get<TrackerManager>().get(serviceId)!!,
            )
        }
        val state by screenModel.state.collectAsState()
        TrackStatusSelector(
            selection = state.selection,
            onSelectionChange = screenModel::setSelection,
            selections = remember { screenModel.getSelections() },
            onConfirm = {
                screenModel.setStatus()
                navigator.pop()
            },
            onDismissRequest = navigator::pop,
        )
    }

    private class Model(
        private val track: Track,
        private val tracker: Tracker,
    ) : StateScreenModel<Model.State>(State(track.status)) {

        fun getSelections(): Map<Long, StringResource?> {
            return tracker.getStatusList().associateWith { tracker.getStatus(it) }
        }

        fun setSelection(selection: Long) {
            mutableState.update { it.copy(selection = selection) }
        }

        fun setStatus() {
            screenModelScope.launchNonCancellable {
                tracker.setRemoteStatus(track.toDbTrack(), state.value.selection)
            }
        }

        @Immutable
        data class State(
            val selection: Long,
        )
    }
}

private data class TrackChapterSelectorScreen(
    private val track: Track,
    private val serviceId: Long,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            Model(
                track = track,
                tracker = Injekt.get<TrackerManager>().get(serviceId)!!,
            )
        }
        val state by screenModel.state.collectAsState()

        TrackChapterSelector(
            selection = state.selection,
            onSelectionChange = screenModel::setSelection,
            range = remember { screenModel.getRange() },
            onConfirm = {
                screenModel.setChapter()
                navigator.pop()
            },
            onDismissRequest = navigator::pop,
        )
    }

    private class Model(
        private val track: Track,
        private val tracker: Tracker,
    ) : StateScreenModel<Model.State>(State(track.lastChapterRead.toInt())) {

        fun getRange(): Iterable<Int> {
            val endRange = if (track.totalChapters > 0) {
                track.totalChapters
            } else {
                10000
            }
            return 0..endRange.toInt()
        }

        fun setSelection(selection: Int) {
            mutableState.update { it.copy(selection = selection) }
        }

        fun setChapter() {
            screenModelScope.launchNonCancellable {
                tracker.setRemoteLastChapterRead(track.toDbTrack(), state.value.selection)
            }
        }

        @Immutable
        data class State(
            val selection: Int,
        )
    }
}

private data class TrackScoreSelectorScreen(
    private val track: Track,
    private val serviceId: Long,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            Model(
                track = track,
                tracker = Injekt.get<TrackerManager>().get(serviceId)!!,
            )
        }
        val state by screenModel.state.collectAsState()

        TrackScoreSelector(
            selection = state.selection,
            onSelectionChange = screenModel::setSelection,
            selections = remember { screenModel.getSelections() },
            onConfirm = {
                screenModel.setScore()
                navigator.pop()
            },
            onDismissRequest = navigator::pop,
        )
    }

    private class Model(
        private val track: Track,
        private val tracker: Tracker,
    ) : StateScreenModel<Model.State>(State(tracker.displayScore(track))) {

        fun getSelections(): ImmutableList<String> {
            return tracker.getScoreList()
        }

        fun setSelection(selection: String) {
            mutableState.update { it.copy(selection = selection) }
        }

        fun setScore() {
            screenModelScope.launchNonCancellable {
                tracker.setRemoteScore(track.toDbTrack(), state.value.selection)
            }
        }

        @Immutable
        data class State(
            val selection: String,
        )
    }
}

private data class TrackDateSelectorScreen(
    private val track: Track,
    private val serviceId: Long,
    private val start: Boolean,
) : Screen() {

    @Transient
    private val selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val targetDate = Instant.ofEpochMilli(utcTimeMillis).toLocalDate(ZoneOffset.UTC)

            // Disallow future dates
            if (targetDate > LocalDate.now(ZoneOffset.UTC)) return false

            return when {
                // Disallow setting start date after finish date
                start && track.finishDate > 0 -> {
                    val finishDate = Instant.ofEpochMilli(track.finishDate).toLocalDate(ZoneOffset.UTC)
                    targetDate <= finishDate
                }
                // Disallow setting finish date before start date
                !start && track.startDate > 0 -> {
                    val startDate = Instant.ofEpochMilli(track.startDate).toLocalDate(ZoneOffset.UTC)
                    startDate <= targetDate
                }
                else -> {
                    true
                }
            }
        }

        override fun isSelectableYear(year: Int): Boolean {
            // Disallow future years
            if (year > LocalDate.now(ZoneOffset.UTC).year) return false

            return when {
                // Disallow setting start year after finish year
                start && track.finishDate > 0 -> {
                    val finishDate = Instant.ofEpochMilli(track.finishDate).toLocalDate(ZoneOffset.UTC)
                    year <= finishDate.year
                }
                // Disallow setting finish year before start year
                !start && track.startDate > 0 -> {
                    val startDate = Instant.ofEpochMilli(track.startDate).toLocalDate(ZoneOffset.UTC)
                    startDate.year <= year
                }
                else -> {
                    true
                }
            }
        }
    }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            Model(
                track = track,
                tracker = Injekt.get<TrackerManager>().get(serviceId)!!,
                start = start,
            )
        }

        val canRemove = if (start) {
            track.startDate > 0
        } else {
            track.finishDate > 0
        }
        TrackDateSelector(
            title = if (start) {
                stringResource(MR.strings.track_started_reading_date)
            } else {
                stringResource(MR.strings.track_finished_reading_date)
            },
            initialSelectedDateMillis = screenModel.initialSelection,
            selectableDates = selectableDates,
            onConfirm = {
                screenModel.setDate(it)
                navigator.pop()
            },
            onRemove = { screenModel.confirmRemoveDate(navigator) }.takeIf { canRemove },
            onDismissRequest = navigator::pop,
        )
    }

    private class Model(
        private val track: Track,
        private val tracker: Tracker,
        private val start: Boolean,
    ) : ScreenModel {

        // In UTC
        val initialSelection: Long
            get() {
                val millis = (if (start) track.startDate else track.finishDate)
                    .takeIf { it != 0L }
                    ?: Instant.now().toEpochMilli()
                return millis.convertEpochMillisZone(ZoneOffset.systemDefault(), ZoneOffset.UTC)
            }

        // In UTC
        fun setDate(millis: Long) {
            // Convert to local time
            val localMillis = millis.convertEpochMillisZone(ZoneOffset.UTC, ZoneOffset.systemDefault())
            screenModelScope.launchNonCancellable {
                if (start) {
                    tracker.setRemoteStartDate(track.toDbTrack(), localMillis)
                } else {
                    tracker.setRemoteFinishDate(track.toDbTrack(), localMillis)
                }
            }
        }

        fun confirmRemoveDate(navigator: Navigator) {
            navigator.push(TrackDateRemoverScreen(track, tracker.id, start))
        }
    }
}

private data class TrackDateRemoverScreen(
    private val track: Track,
    private val serviceId: Long,
    private val start: Boolean,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            Model(
                track = track,
                tracker = Injekt.get<TrackerManager>().get(serviceId)!!,
                start = start,
            )
        }
        AlertDialogContent(
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
            },
            title = {
                Text(
                    text = stringResource(MR.strings.track_remove_date_conf_title),
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                val serviceName = screenModel.getServiceName()
                Text(
                    text = if (start) {
                        stringResource(MR.strings.track_remove_start_date_conf_text, serviceName)
                    } else {
                        stringResource(MR.strings.track_remove_finish_date_conf_text, serviceName)
                    },
                )
            },
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small, Alignment.End),
                ) {
                    TextButton(onClick = navigator::pop) {
                        Text(text = stringResource(MR.strings.action_cancel))
                    }
                    FilledTonalButton(
                        onClick = {
                            screenModel.removeDate()
                            navigator.popUntil { it is TrackInfoDialogHomeScreen }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Text(text = stringResource(MR.strings.action_remove))
                    }
                }
            },
        )
    }

    private class Model(
        private val track: Track,
        private val tracker: Tracker,
        private val start: Boolean,
    ) : ScreenModel {

        fun getServiceName() = tracker.name

        fun removeDate() {
            screenModelScope.launchNonCancellable {
                if (start) {
                    tracker.setRemoteStartDate(track.toDbTrack(), 0)
                } else {
                    tracker.setRemoteFinishDate(track.toDbTrack(), 0)
                }
            }
        }
    }
}

data class TrackerSearchScreen(
    private val mangaId: Long,
    private val initialQuery: String,
    private val currentUrl: String?,
    private val serviceId: Long,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            Model(
                mangaId = mangaId,
                currentUrl = currentUrl,
                initialQuery = initialQuery,
                tracker = Injekt.get<TrackerManager>().get(serviceId)!!,
            )
        }

        val state by screenModel.state.collectAsState()

        // KMK -->
        val resultKey = "local-create:$serviceId:$mangaId"
        val createResult by TrackerCreateResultStore.result.collectAsState()
        LaunchedEffect(createResult) {
            val result = createResult
            if (result != null && result.key == resultKey) {
                screenModel.trackingSearch(result.query, result.item)
                TrackerCreateResultStore.consume(resultKey)
            }
        }
        // KMK <--

        val textFieldState = rememberTextFieldState(initialQuery)
        TrackerSearch(
            state = textFieldState,
            onDispatchQuery = { screenModel.trackingSearch(textFieldState.text.toString()) },
            queryResult = state.queryResult,
            selected = state.selected,
            onSelectedChange = screenModel::updateSelection,
            onConfirmSelection = f@{ private: Boolean ->
                val selected = state.selected ?: return@f
                selected.private = private
                screenModel.registerTracking(selected)
                navigator.pop()
            },
            onDismissRequest = navigator::pop,
            supportsPrivateTracking = screenModel.supportsPrivateTracking,
            // KMK -->
            createLabel = if (screenModel.canCreate && textFieldState.text.isNotBlank()) {
                stringResource(KMR.strings.local_tracker_create_action)
            } else {
                null
            },
            onCreate = if (screenModel.canCreate) {
                {
                    val query = textFieldState.text.toString()
                    navigator.push(TrackerCreateScreen(mangaId, serviceId, query, resultKey))
                }
            } else {
                null
            },
            // KMK <--
        )
    }

    private class Model(
        private val mangaId: Long,
        private val currentUrl: String? = null,
        initialQuery: String,
        private val tracker: Tracker,
    ) : StateScreenModel<Model.State>(State()) {

        val supportsPrivateTracking = tracker.supportsPrivateTracking
        // KMK -->
        val creatableTracker = tracker as? CreatableTracker
        val canCreate get() = creatableTracker != null
        // KMK <--

        init {
            // Run search on first launch
            if (initialQuery.isNotBlank()) {
                trackingSearch(initialQuery)
            }
        }

        // KMK -->
        fun trackingSearch(query: String, createdItem: TrackSearch? = null) {
            // KMK <--
            screenModelScope.launch {
                // To show loading state
                mutableState.update { it.copy(queryResult = null, selected = null) }

                val result = withIOContext {
                    try {
                        // KMK -->
                        val results = tracker.search(query.sanitize()).toMutableList().apply {
                            createdItem?.let { created ->
                                if (none { it.remote_id == created.remote_id }) add(created)
                            }
                        }
                        // KMK <--
                        Result.success(results)
                    } catch (e: Throwable) {
                        Result.failure(e)
                    }
                }
                mutableState.update { oldState ->
                    oldState.copy(
                        queryResult = result,
                        selected = result.getOrNull()?.find { it.tracking_url == currentUrl },
                    )
                }
            }
        }

        fun registerTracking(item: TrackSearch) {
            screenModelScope.launchNonCancellable { tracker.register(item, mangaId) }
        }

        fun updateSelection(selected: TrackSearch) {
            mutableState.update { it.copy(selected = selected) }
        }

        @Immutable
        data class State(
            val queryResult: Result<List<TrackSearch>>? = null,
            val selected: TrackSearch? = null,
        )
    }
}

// KMK -->
/**
 * Result handed back from [TrackerCreateScreen] to [TrackerSearchScreen].
 *
 * Kept out of the Screen itself: Voyager screens are Serializable and saved with the
 * navigator's state, so a function property would fail to be written to the saved-state Bundle.
 */
private data class TrackerCreateResult(
    val key: String,
    val query: String,
    val item: TrackSearch?,
)

private object TrackerCreateResultStore {
    private val _result = MutableStateFlow<TrackerCreateResult?>(null)
    val result: StateFlow<TrackerCreateResult?> = _result

    fun publish(result: TrackerCreateResult) {
        _result.value = result
    }

    fun consume(key: String) {
        if (_result.value?.key == key) {
            _result.value = null
        }
    }
}

/** Cuts the field outline where the reset button overlaps its top-right corner. */
@Composable
private fun FieldCornerCutout(modifier: Modifier = Modifier) {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier.drawBehind {
            val stroke = 2.dp.toPx()
            drawRect(
                color = background,
                size = DrawSize(size.width, stroke),
            )
            drawRect(
                color = background,
                topLeft = Offset(size.width - stroke, 0f),
                size = DrawSize(stroke, size.height),
            )
        },
    )
}

/** Reset icon that scales into view at the top-right corner of a text field. */
@Composable
private fun ResetFieldButton(
    visible: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0f),
        exit = scaleOut(targetScale = 0f),
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .offset(x = 4.dp, y = (-4).dp)
                .size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.RestartAlt,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private data class TrackerCreateScreen(
    private val mangaId: Long,
    private val serviceId: Long,
    private val initialTitle: String,
    private val resultKey: String,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val tracker = remember { Injekt.get<TrackerManager>().get(serviceId)!! }
        val screenModel = rememberScreenModel { Model(mangaId, tracker) }
        val state by screenModel.state.collectAsState()
        var title by remember { mutableStateOf(initialTitle) }
        var description by remember { mutableStateOf("") }
        var descriptionEdited by remember { mutableStateOf(false) }
        var coverUri by remember { mutableStateOf<Uri?>(null) }
        var useMangaCover by remember { mutableStateOf(true) }
        var isCreating by remember { mutableStateOf(false) }
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                coverUri = it
                useMangaCover = false
            }
        }
        LaunchedEffect(state.manga) {
            if (!descriptionEdited) description = state.manga?.description.orEmpty()
        }
        val previewCover = coverUri ?: if (useMangaCover) state.manga else null

        Scaffold(
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = navigator::pop) {
                            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                        }
                    },
                    title = { Text(text = stringResource(KMR.strings.local_tracker_create_action)) },
                )
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.padding.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
            ) {
                // KMK -->
                Box {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(KMR.strings.local_tracker_create_title_label)) },
                        singleLine = true,
                    )
                    if (title != initialTitle) {
                        FieldCornerCutout(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(36.dp),
                        )
                    }
                    ResetFieldButton(
                        visible = title != initialTitle,
                        contentDescription = stringResource(KMR.strings.local_tracker_create_reset_title),
                        onClick = { title = initialTitle },
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
                Box {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            descriptionEdited = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(KMR.strings.local_tracker_create_description_label)) },
                        minLines = 4,
                    )
                    if (descriptionEdited || description != state.manga?.description.orEmpty()) {
                        FieldCornerCutout(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(36.dp),
                        )
                    }
                    ResetFieldButton(
                        visible = descriptionEdited || description != state.manga?.description.orEmpty(),
                        contentDescription = stringResource(
                            KMR.strings.local_tracker_create_reset_description,
                        ),
                        onClick = {
                            description = state.manga?.description.orEmpty()
                            descriptionEdited = false
                        },
                        modifier = Modifier.align(Alignment.TopEnd),
                    )
                }
                // KMK <--
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)) {
                    Text(text = stringResource(KMR.strings.local_tracker_create_cover_label))
                    if (previewCover != null) {
                        AsyncImage(
                            model = previewCover,
                            contentDescription = stringResource(KMR.strings.local_tracker_create_cover_label),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = stringResource(KMR.strings.local_tracker_create_select_cover))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                    ) {
                        TextButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = stringResource(KMR.strings.local_tracker_create_change_cover))
                        }
                        TextButton(
                            onClick = {
                                coverUri = null
                                useMangaCover = false
                            },
                            enabled = previewCover != null,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = stringResource(MR.strings.action_remove))
                        }
                    }
                    // KMK -->
                    TextButton(
                        onClick = {
                            coverUri = null
                            useMangaCover = true
                        },
                        enabled = coverUri != null || !useMangaCover,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(KMR.strings.local_tracker_create_reset_cover),
                            modifier = Modifier.padding(start = MaterialTheme.padding.small),
                        )
                    }
                    // KMK <--
                }
                Button(
                    onClick = {
                        isCreating = true
                        screenModel.create(
                            title,
                            description,
                            coverUri,
                            useMangaCover,
                            context,
                        ) { createdItem ->
                            isCreating = false
                            // KMK -->
                            if (createdItem != null) {
                                TrackerCreateResultStore.publish(
                                    TrackerCreateResult(resultKey, initialTitle, createdItem),
                                )
                                navigator.pop()
                            }
                            // KMK <--
                        }
                    },
                    enabled = title.isNotBlank() && !isCreating && !state.isLoadingManga,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isCreating) {
                        CircularProgressIndicator()
                    } else {
                        Text(text = stringResource(KMR.strings.local_tracker_create_action))
                    }
                }
            }
        }
    }

    private class Model(
        private val mangaId: Long,
        private val tracker: Tracker,
        private val getManga: GetManga = Injekt.get(),
        private val application: Application = Injekt.get(),
    ) : StateScreenModel<Model.State>(State()) {

        init {
            screenModelScope.launch {
                val manga = withIOContext { getManga.await(mangaId) }
                mutableState.update { it.copy(manga = manga, isLoadingManga = false) }
            }
        }

        fun create(
            title: String,
            description: String,
            coverUri: Uri?,
            useMangaCover: Boolean,
            context: Context,
            // KMK -->
            onFinished: (TrackSearch?) -> Unit,
            // KMK <--
        ) {
            val creator = tracker as? CreatableTracker ?: return
            screenModelScope.launch {
                try {
                    val selectedCover = coverUri?.let { uri ->
                        withIOContext {
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: throw IllegalStateException("Could not read selected cover image")
                        }
                    }
                    val mangaCover = if (coverUri == null && useMangaCover) {
                        mutableState.value.manga?.let { loadMangaCover(it) }
                    } else {
                        null
                    }
                    val coverImage = selectedCover ?: mangaCover
                    val coverMimeType = when {
                        coverUri != null -> context.contentResolver.getType(coverUri)
                        mangaCover != null -> "image/jpeg"
                        else -> null
                    }
                    val item = withIOContext { creator.create(title, description, coverImage, coverMimeType) }
                    // KMK -->
                    withUIContext { onFinished(item) }
                    // KMK <--
                } catch (e: Exception) {
                    withUIContext {
                        Injekt.get<Application>().toast(e.message)
                        // KMK -->
                        onFinished(null)
                        // KMK <--
                    }
                }
            }
        }

        private suspend fun loadMangaCover(manga: Manga): ByteArray? = try {
            withIOContext {
                val request = ImageRequest.Builder(application)
                    .data(manga)
                    .size(Size.ORIGINAL)
                    .allowHardware(false)
                    .build()
                val bitmap = application.imageLoader.execute(request).image
                    ?.asDrawable(application.resources)
                    ?.getBitmapOrNull()
                    ?: return@withIOContext null
                ByteArrayOutputStream().use { output ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) output.toByteArray() else null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

        @Immutable
        data class State(
            val manga: Manga? = null,
            val isLoadingManga: Boolean = true,
        )
    }
}
// KMK <--

private data class TrackerRemoveScreen(
    private val mangaId: Long,
    private val track: Track,
    private val serviceId: Long,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            Model(
                mangaId = mangaId,
                track = track,
                tracker = Injekt.get<TrackerManager>().get(serviceId)!!,
            )
        }
        val serviceName = screenModel.getName()
        var removeRemoteTrack by remember { mutableStateOf(false) }
        AlertDialogContent(
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
            },
            title = {
                Text(
                    text = stringResource(MR.strings.track_delete_title, serviceName),
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    Text(
                        text = stringResource(MR.strings.track_delete_text, serviceName),
                    )

                    if (screenModel.isDeletable()) {
                        LabeledCheckbox(
                            label = stringResource(MR.strings.track_delete_remote_text, serviceName),
                            checked = removeRemoteTrack,
                            onCheckedChange = { removeRemoteTrack = it },
                        )
                    }
                }
            },
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        MaterialTheme.padding.small,
                        Alignment.End,
                    ),
                ) {
                    TextButton(onClick = navigator::pop) {
                        Text(text = stringResource(MR.strings.action_cancel))
                    }
                    FilledTonalButton(
                        onClick = {
                            screenModel.unregisterTracking(serviceId)
                            if (removeRemoteTrack) screenModel.deleteMangaFromService()
                            navigator.pop()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Text(text = stringResource(MR.strings.action_ok))
                    }
                }
            },
        )
    }

    private class Model(
        private val mangaId: Long,
        private val track: Track,
        private val tracker: Tracker,
        private val deleteTrack: DeleteTrack = Injekt.get(),
    ) : ScreenModel {

        fun getName() = tracker.name

        fun isDeletable() = tracker is DeletableTracker

        fun deleteMangaFromService() {
            screenModelScope.launchNonCancellable {
                try {
                    (tracker as DeletableTracker).delete(track)
                } catch (e: Exception) {
                    logcat(LogPriority.ERROR, e) { "Failed to delete entry from service" }
                }
            }
        }

        fun unregisterTracking(serviceId: Long) {
            screenModelScope.launchNonCancellable { deleteTrack.await(mangaId, serviceId) }
        }
    }
}
