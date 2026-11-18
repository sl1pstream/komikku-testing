# integration/search-trackers

> **This README describes only this branch.** `integration/search-trackers` is a personal
> build branch of [sl1pstream/komikku-testing](https://github.com/sl1pstream/komikku-testing),
> a fork of [komikku-app/komikku](https://github.com/komikku-app/komikku).
> For the app itself, its features and its documentation, see the upstream project.

This branch is **not** a feature branch. It is a disposable, on-demand build target whose
only job is to combine several in-flight feature branches into a single installable APK,
so they can be tested together before any of them is merged upstream.

---

## What it contains

| Branch | Purpose |
| --- | --- |
| `fix/global-search` | Upstream global-search fixes (stuck sources, leaked search threads, result-update race conditions) |
| `feat/comick-tracker` | Comick tracker |
| `feat/mangabaka-tracker-clean` | MangaBaka tracker |
| `feat/syncyomi-v2-clean` | *Not included* (deliberately excluded for now) |

Everything sits on top of the fork's `master`:

```
master
  └─ fix/global-search                     (upstream search fixes)
       ├─ merge feat/comick-tracker
       └─ merge feat/mangabaka-tracker-clean   ← tracker conflicts resolved here
            = integration/search-trackers
```

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

- It does **not** stay in sync with the feature branches. Git only changes it when you tell it to.
- It should **never** be merged back into a feature branch. Merging is one-way:
  features → integration.
- It is **not** built by CI. `.github/workflows/build_push.yml` only triggers on `master`.
- It is disposable. Deleting and recreating it costs a few commands.

## Updating it

### Case A — a feature branch gained commits (normal evolution)

```bash
git checkout integration/search-trackers
git merge origin/feat/comick-tracker        # or whichever branch changed
./gradlew assembleReleaseTest
```

This normally auto-merges, because the earlier conflict resolutions are already part of this
branch's history. If the same files conflict again, resolve them the same way: keep both
trackers.

### Case B — a feature branch was rewritten (force-push / rebase)

Rewriting a branch that is already merged here leaves the old commits in this branch's
ancestry. Rebuild the integration branch from scratch instead of merging:

```bash
git checkout -B integration/search-trackers fix/global-search
git merge origin/feat/comick-tracker
git merge origin/feat/mangabaka-tracker-clean
# resolve TrackManager.kt, TrackLoginActivity.kt, SettingsTrackingScreen.kt
```

### Case C — only a subset is wanted

Just don't merge the branch you don't need, or recreate the integration branch from the
feature branches you do want.

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

APKs land in `app/build/outputs/apk/releaseTest/`. To make one installable locally:

```bash
$ANDROID_HOME/build-tools/<version>/apksigner sign \
  --ks ~/.android/debug.keystore \
  --ks-pass pass:android --key-pass pass:android \
  --ks-key-alias androiddebugkey \
  --out app-universal-releaseTest-signed.apk \
  app-universal-releaseTest-unsigned.apk
```

Because the debug keystore is used, uninstall any existing `app.komikku.rt` signed with a
different key before installing.

## Rules of thumb

- Merge feature branches **into** this branch; never the other way around.
- Keep this branch local or clearly marked if pushed — it is a build artifact, not a
  contribution.
- Regenerate it whenever a constituent branch changes, then rebuild and retest.
