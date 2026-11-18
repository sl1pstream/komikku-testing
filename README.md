# integration/search-trackers

> **This README describes only this branch.** `integration/search-trackers` is a personal
> build branch of [sl1pstream/komikku-testing](https://github.com/sl1pstream/komikku-testing),
> a fork of [komikku-app/komikku](https://github.com/komikku-app/komikku).
> For the app itself, its features and its documentation, see the upstream project.

This branch is **not** a feature branch. It is a disposable, on-demand build target whose
only job is to combine the fork's `master` with in-flight tracker branches into a single
installable APK, so they can be tested together before any of them is merged upstream.

---

## Current state

| Item | Value |
| --- | --- |
| Latest code merge | `8ae2850` — *Merge branch 'master' into integration/search-trackers* |
| Local vs `origin` | **Identical** (in sync — pushing reports "Everything up-to-date") |
| Base | `master` at `290267d`, synced with upstream `komikku-app/komikku` |
| Search fixes | Present — the 4 search files are byte-identical to `master` |
| Trackers | Comick + MangaBaka both registered and compiling |
| Build | `assembleReleaseTest`; debug-signed APKs built from the `8ae2850` merge |

Commit hashes and the table above describe the state at the time of writing; regenerate the
branch and update this section if it drifts.

## What it contains

| Branch | Purpose |
| --- | --- |
| `master` | Fork's master, synced with upstream. **Includes the upstream global-search fixes** (stuck sources, leaked search threads, result-update races). |
| `feat/comick-tracker` | Comick tracker |
| `feat/mangabaka-tracker-clean` | MangaBaka tracker |
| `feat/tracker-ordering` | User-configurable tracker order (drag-to-reorder in Settings > Tracking) |
| `feat/syncyomi-v2-clean` | *Not included* — branch has been deleted |

Conceptually the branch is just:

```
master  (upstream, includes the global-search fixes)
  ├─ merge feat/comick-tracker
  └─ merge feat/mangabaka-tracker-clean   ← tracker conflicts resolved here
       = integration/search-trackers
```

Historically this branch was created from an earlier search-fix branch before `master` was
synced. Those commits have since been merged upstream and are part of `master`, so this
branch is now simply "`master` + both trackers".

## Why this branch exists

Three jobs, none of which the feature branches can do on their own:

1. **One combined artifact.** A single APK with all features present, so their interactions
   can actually be tested (for example: global search while trackers are logged in).
2. **A home for cross-branch conflicts.** `feat/comick-tracker` and
   `feat/mangabaka-tracker-clean` both register a tracker, so they both edit:
   - `app/src/main/java/eu/kanade/tachiyomi/data/track/TrackerManager.kt`
   - `app/src/main/java/eu/kanade/tachiyomi/ui/setting/track/TrackLoginActivity.kt`
   - `app/src/main/java/eu/kanade/presentation/more/settings/screen/SettingsTrackingScreen.kt`

   Deciding to keep **both** trackers registered is a property of the *combination*, not of
   either feature. The resolution therefore lives in this branch's merge commits, and the
   feature branches stay untouched.
3. **Compatibility proof.** If the merge is clean — or resolves to a compiling build — the
   features are known to coexist at the code level.

## What this branch is *not*

- It does **not** stay in sync with `master` or the feature branches. Git only changes it
  when you tell it to; syncing `master` does not propagate here automatically.
- It should **never** be merged back into a feature branch. Merging is one-way:
  features → integration.
- It is **not** built by CI. `.github/workflows/build_push.yml` only triggers on `master`,
  and Actions has produced no runs on this fork.
- It is disposable. Deleting and recreating it costs a few commands.

## Updating it

### Case A — a feature branch (or `master`) gained commits

```bash
git fetch origin
git checkout integration/search-trackers
git merge master                            # or origin/feat/comick-tracker, etc.
./gradlew assembleReleaseTest
```

This normally auto-merges, because the earlier conflict resolutions are already part of this
branch's history. If the tracker files conflict again, resolve them the same way: keep both
trackers.

### Case B — a branch was rewritten (force-push / rebase)

Rewriting a branch that is already merged here leaves the old commits in this branch's
ancestry. Rebuild the integration branch from scratch instead of merging:

```bash
git checkout -B integration/search-trackers master
git merge origin/feat/comick-tracker
git merge origin/feat/mangabaka-tracker-clean
# resolve TrackerManager.kt, TrackLoginActivity.kt, SettingsTrackingScreen.kt
```

### Case C — only a subset is wanted

Just don't merge the branch you don't need, or recreate the integration branch from the
branches you do want.

### Tip: remember conflict resolutions

```bash
git config --global rerere.enabled true
```

With `rerere` enabled, Git replays the previous tracker-file resolutions automatically the
next time the same conflicts appear.

## Building

The verification build for this branch is the `releaseTest` build type
(application id suffix `.rt`, unsigned output):

```bash
ANDROID_HOME=$HOME/Android/Sdk ./gradlew assembleReleaseTest
```

APKs land in `app/build/outputs/apk/releaseTest/`. To make them installable locally:

```bash
cd app/build/outputs/apk/releaseTest
for f in *-releaseTest-unsigned.apk; do
  out="${f/-unsigned/-signed}"
  $HOME/Android/Sdk/build-tools/<version>/apksigner sign \
    --ks ~/.android/debug.keystore \
    --ks-pass pass:android --key-pass pass:android \
    --ks-key-alias androiddebugkey --v4-signing-enabled false \
    --out "$out" "$f"
done
```

Because the debug keystore is used, uninstall any existing `app.komikku.rt` signed with a
different key before installing.

## Verifying the combination

```bash
# search files must be identical to master
git diff --stat master HEAD -- \
  app/src/main/java/eu/kanade/tachiyomi/ui/browse/feed/FeedScreenModel.kt \
  app/src/main/java/eu/kanade/tachiyomi/ui/browse/source/feed/SourceFeedScreenModel.kt \
  app/src/main/java/eu/kanade/tachiyomi/ui/browse/source/globalsearch/SearchScreenModel.kt \
  app/src/main/java/exh/recs/RecommendsScreenModel.kt

# everything that differs should be tracker-related (+ this README)
git diff --stat master HEAD
```

## Rules of thumb

- Merge branches **into** this branch; never the other way around.
- Keep this branch local or clearly marked if pushed — it is a build artifact, not a
  contribution.
- Regenerate it whenever a constituent branch changes, then rebuild and retest.
