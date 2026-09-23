# Instructions: Create GitHub Release v0.0.3-beta

## Prerequisites
✅ Code committed and pushed to main (commit 8b5ac21)  
⏳ Git tag `v0.0.3-beta` needs to be created and pushed  
✅ APK built at `app/build/outputs/apk/debug/app-debug.apk` (57MB)  
✅ Release notes prepared in `RELEASE_NOTES_v0.0.3.md`

## Step 0: Create and Push Git Tag

```bash
cd /Users/matt/Documents/R410-Shaggoth-AI-Command-Center/CrossLens/CrossLens
git tag -a v0.0.3-beta -m "v0.0.3-beta - Editorial Review Workflow"
git push origin v0.0.3-beta
```

## Steps to Create Release on GitHub

### 1. Navigate to Releases
Go to: https://github.com/Mattjhagen/CrossLens/releases

### 2. Click "Draft a new release"

### 3. Configure Release Settings

**Choose a tag:**
- Select existing tag: `v0.0.3-beta`

**Release title:**
```
v0.0.3-beta - Editorial Review Workflow (Demo)
```

**Description:**
Copy the contents from `RELEASE_NOTES_v0.0.3.md` or use the following:

```markdown
## What's New

### Editorial Review Workflow (Demo)

Added offline editorial review system demonstrating human review of ingestion candidates before publication.

**Features Implemented:**
- **Settings Entry Point:** Clear, accessible navigation from Settings to Editorial Review screen
- **Review Dashboard:** Statistics showing approved/rejected/deferred review counts
- **Pending Candidates:** Mock cross-language matches and syndication detection candidates
- **Review Dialog:** Full evidence display with sources, languages, shared entities, and uncertainty reasons
- **Decision Workflow:** Approve/Reject/Defer with optional editorial notes
- **Audit Trail:** Complete review history showing all decisions with timestamps
- **Accessibility:** All strings externalized to resources with proper content descriptions

**Important Boundaries:**
- ⚠️ **Offline demo only** - Uses mock candidates from test fixtures, not live ingestion
- ⚠️ **No production integration** - Reviews stored locally, do not affect published stories
- ⚠️ **Fictional data** - All candidates are demo data for workflow demonstration

## Test Results

✅ **All Tests Passing**
- 24 unit tests: 24 passed, 0 failed, 0 skipped
- New Editorial Review ViewModel tests (9 tests)
- Lint: 0 errors, 0 warnings
- Build verification successful

## User-Facing Behavior

Users can now:
1. Navigate from Settings → Access section → "Editorial Review" card
2. View demo review dashboard with approval statistics
3. Inspect pending candidates showing cross-language matches and syndication detection
4. Review evidence including sources, languages, shared entities, and uncertainty reasons
5. Decide to approve, reject, or defer with optional editorial notes
6. Track review history showing all past decisions with audit trail

**Demo Workflow Notice:**
> ⚠️ This is a fictional prototype for reviewing mock ingestion candidates. All data is demo-only.

## Installation

Download `app-debug.apk` from this release and install on Android 10+ devices.

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

## Files Changed
- `app/src/main/res/values/strings.xml` - Added 22 editorial review string resources
- `app/src/main/java/com/crosslens/app/feature/editorial/EditorialReviewScreen.kt` - String resource integration
- `app/src/main/java/com/crosslens/app/feature/settings/SettingsScreen.kt` - Added review entry point
- `app/src/test/java/com/crosslens/app/feature/editorial/EditorialReviewViewModelTest.kt` - New test suite (9 tests)
- `README.md` - Updated features and roadmap
- `docs/INGESTION_PROTOTYPE.md` - Documented workflow and production requirements

## Documentation

See `RELEASE_NOTES_v0.0.3.md` for complete details, limitations, and production prerequisites.

**Commit:** 8b5ac21
```

### 4. Upload APK Asset

Click "Attach binaries by dropping them here or selecting them"

**File to upload:**
`app/build/outputs/apk/debug/app-debug.apk`

**Rename to:** `CrossLens-v0.0.3-beta-debug.apk`

### 5. Set Release Options

- ☑️ **Set as a pre-release** (check this box - it's a beta)
- ☑️ **Set as the latest release** (check this box - it's the newest version)

### 6. Publish Release

Click "Publish release"

## Verification

After publishing:
1. Verify the release appears at https://github.com/Mattjhagen/CrossLens/releases
2. Verify the APK is downloadable
3. Verify the tag `v0.0.3-beta` is linked correctly
4. Verify "Pre-release" badge is visible

## Alternative: Using GitHub CLI

If you have `gh` CLI installed, you can create the release with:

```bash
cd /Users/matt/Documents/R410-Shaggoth-AI-Command-Center/CrossLens/CrossLens

# Create and push tag
git tag -a v0.0.3-beta -m "v0.0.3-beta - Editorial Review Workflow"
git push origin v0.0.3-beta

# Create release
gh release create v0.0.3-beta \
  app/build/outputs/apk/debug/app-debug.apk#CrossLens-v0.0.3-beta-debug.apk \
  --title "v0.0.3-beta - Editorial Review Workflow (Demo)" \
  --notes-file RELEASE_NOTES_v0.0.3.md \
  --prerelease \
  --latest
```

## Done!

The release will be available for download and testing.

## Next Steps (Optional)

After release:
1. Test APK download and installation on Android 10+ device
2. Verify Settings → Editorial Review navigation works
3. Test review workflow with mock candidates
4. Collect feedback on UI/UX and workflow clarity
5. Document any issues for next release
