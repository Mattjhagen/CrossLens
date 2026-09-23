# Instructions: Create GitHub Release v0.0.4-beta

## Prerequisites
✅ Code committed and pushed to main (commit 15ac1fb)  
⏳ Git tag `v0.0.4-beta` needs to be created and pushed  
✅ APK built at `app/build/outputs/apk/debug/app-debug.apk` (57MB)  
✅ Release notes prepared in `RELEASE_NOTES_v0.0.4.md`

## Step 0: Create and Push Git Tag

```bash
cd /Users/matt/Documents/R410-Shaggoth-AI-Command-Center/CrossLens/CrossLens
git tag -a v0.0.4-beta -m "v0.0.4-beta - Editorial Review Reset Control"
git push origin v0.0.4-beta
```

## Steps to Create Release on GitHub

### 1. Navigate to Releases
Go to: https://github.com/Mattjhagen/CrossLens/releases

### 2. Click "Draft a new release"

### 3. Configure Release Settings

**Choose a tag:**
- Select existing tag: `v0.0.4-beta`

**Release title:**
```
v0.0.4-beta - Editorial Review Reset Control
```

**Description:**
Copy the contents from `RELEASE_NOTES_v0.0.4.md` or use the following:

```markdown
## What's New

### Reset Control for Editorial Review Demo

Added tester-friendly reset functionality to restore the original candidate set without reinstalling the app.

**Features Added:**
- **Reset Button:** "Reset demo reviews" appears after review history section
- **Confirmation Dialog:** Clear explanation that reset only deletes local fictional demo review decisions
- **Candidate Restoration:** Restores original three mock candidates (cross-language, syndication, cluster) to pending state
- **Persistence:** After reset and app restart, candidates remain pending until reviewed
- **Third Mock Candidate:** Added "AI Regulation Framework Proposal" cluster to enable testing all three decision types

**What Reset Does:**
- ✅ Deletes all local demo editorial review decisions and notes
- ✅ Restores three original mock candidates to pending state
- ✅ Allows testers to repeat approve/reject/defer workflow
- ✅ Persists correctly across app restarts

**What Reset Does NOT Affect:**
- ✅ Reader-facing stories, settings, saved stories, or subscriptions
- ✅ Any non-editorial local data

## Test Results

✅ **All Tests Passing**
- 27 unit tests: 27 passed, 0 failed, 0 skipped
- Editorial Review tests: 13/13 passing (3 new reset tests)
- Lint: 0 errors, 0 warnings
- Build verification successful

## User-Facing Behavior

Testers can now:
1. Navigate to Settings → Access section → "Editorial Review"
2. Review three pending candidates (cross-language match, syndication, cluster proposal)
3. Make decisions on all candidates (approve, reject, defer with notes)
4. View complete review history and statistics
5. **Reset by clicking "Reset demo reviews" button**
6. **Confirm reset via dialog**
7. **Observe all three candidates restored to pending state**
8. **Restart app and verify candidates remain pending**
9. **Repeat workflow to test different decision combinations**

**Reset Confirmation Message:**
> This will delete all local fictional demo review decisions and notes, restoring the original candidate set.
> 
> This only affects demo editorial review data. Your settings, saved stories, and reading history will not be changed.

## Installation

Download `app-debug.apk` from this release and install on Android 10+ devices.

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

## Files Changed
- `app/src/main/res/values/strings.xml` - Added 5 reset string resources
- `app/src/main/java/com/crosslens/app/data/local/dao/EditorialReviewDao.kt` - Added deleteAllReviews query
- `app/src/main/java/com/crosslens/app/data/repository/EditorialReviewRepository.kt` - Added resetAllReviews method
- `app/src/main/java/com/crosslens/app/feature/editorial/EditorialReviewViewModel.kt` - Added third candidate & resetReviews
- `app/src/main/java/com/crosslens/app/feature/editorial/EditorialReviewScreen.kt` - Added reset button & dialog
- `app/src/test/java/com/crosslens/app/feature/editorial/EditorialReviewViewModelTest.kt` - Added 3 reset tests
- `docs/INGESTION_PROTOTYPE.md` - Documented reset control

## Changes from v0.0.3-beta

v0.0.3-beta introduced the editorial review workflow. v0.0.4-beta makes it tester-friendly:
- Reset control to repeat workflow without reinstall
- Third mock candidate for complete decision type coverage
- Improved test coverage with reset functionality tests

## Documentation

See `RELEASE_NOTES_v0.0.4.md` for complete details, test results, and limitations.

**Commit:** 15ac1fb
```

### 4. Upload APK Asset

Click "Attach binaries by dropping them here or selecting them"

**File to upload:**
`app/build/outputs/apk/debug/app-debug.apk`

**Rename to:** `CrossLens-v0.0.4-beta-debug.apk`

### 5. Set Release Options

- ☑️ **Set as a pre-release** (check this box - it's a beta)
- ☑️ **Set as the latest release** (check this box - it's the newest version)

### 6. Publish Release

Click "Publish release"

## Verification

After publishing:
1. Verify the release appears at https://github.com/Mattjhagen/CrossLens/releases
2. Verify the APK is downloadable
3. Verify the tag `v0.0.4-beta` is linked correctly
4. Verify "Pre-release" badge is visible

## Alternative: Using GitHub CLI

If you have `gh` CLI installed, you can create the release with:

```bash
cd /Users/matt/Documents/R410-Shaggoth-AI-Command-Center/CrossLens/CrossLens

# Create and push tag
git tag -a v0.0.4-beta -m "v0.0.4-beta - Editorial Review Reset Control"
git push origin v0.0.4-beta

# Create release
gh release create v0.0.4-beta \
  app/build/outputs/apk/debug/app-debug.apk#CrossLens-v0.0.4-beta-debug.apk \
  --title "v0.0.4-beta - Editorial Review Reset Control" \
  --notes-file RELEASE_NOTES_v0.0.4.md \
  --prerelease \
  --latest
```

## Done!

The release will be available for download and testing.

## Next Steps (Optional)

After release:
1. Test APK download and installation on Android 10+ device
2. Verify Settings → Editorial Review → Reset workflow
3. Test that reset properly restores all three candidates
4. Verify candidates persist as pending after app restart
5. Test all three decision types (approve, reject, defer)
6. Collect feedback on reset UX and clarity
7. Document any issues for next release
