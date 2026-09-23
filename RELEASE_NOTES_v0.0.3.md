# CrossLens v0.0.3 Beta Release

## Release Information

**Version:** v0.0.3-beta  
**Date:** 2026-09-22  
**Commit:** 8b5ac21  
**Build:** Debug APK

## What's New

### Editorial Review Workflow (Demo)

Added offline editorial review system demonstrating human review of ingestion candidates before publication.

**Features Implemented:**
- **Settings Entry Point:** Clear, accessible navigation from Settings to Editorial Review screen
- **Review Dashboard:** Statistics showing approved/rejected/deferred review counts
- **Pending Candidates:** Three mock candidates (cross-language match, syndication detection, cluster proposal)
- **Review Dialog:** Full evidence display with sources, languages, shared entities, and uncertainty reasons
- **Decision Workflow:** Approve/Reject/Defer with optional editorial notes
- **Audit Trail:** Complete review history showing all decisions with timestamps
- **Reset Control:** "Reset demo reviews" button restores original candidate set for re-testing all decision types
- **Accessibility:** All strings externalized to resources with proper content descriptions

**What This Demonstrates:**
- Editorial oversight before publishing clustered stories
- Evidence-based decision making with uncertainty disclosure
- Cross-language event matching requiring human verification
- Syndication detection requiring editorial confirmation
- Event clustering proposals requiring validation
- Audit trail for transparency and accountability
- Tester-friendly reset to repeat workflow without reinstalling

**Important Boundaries:**
- ⚠️ **Offline demo only** - Uses mock candidates from test fixtures, not live ingestion
- ⚠️ **No production integration** - Reviews stored locally, do not affect published stories
- ⚠️ **Fictional data** - All candidates are demo data for workflow demonstration
- See production prerequisites in updated `docs/INGESTION_PROTOTYPE.md`

### Technical Implementation

**New Components:**
- `EditorialReviewScreen.kt` - Compose UI with review workflow
- `EditorialReviewViewModel.kt` - Immutable state management with Flow
- `EditorialReviewRepository.kt` - Room persistence for review decisions
- `EditorialReviewViewModelTest.kt` - 9 comprehensive unit tests

**Files Changed:**
- `app/src/main/res/values/strings.xml` - Added 22 editorial review string resources
- `app/src/main/java/com/crosslens/app/feature/editorial/EditorialReviewScreen.kt` - All hard-coded strings replaced
- `app/src/main/java/com/crosslens/app/feature/settings/SettingsScreen.kt` - Added review entry point
- `app/src/test/java/com/crosslens/app/feature/editorial/EditorialReviewViewModelTest.kt` - New test suite
- `README.md` - Updated features and roadmap
- `docs/INGESTION_PROTOTYPE.md` - Documented workflow and production requirements

## Test Results

✅ **All Tests Passing**
```
./gradlew :app:testDebugUnitTest
BUILD SUCCESSFUL
Tests: 24 tests, 24 passed, 0 failed, 0 skipped
```

**New Tests (9):**
- ✅ Initial state has pending candidates and empty history
- ✅ Pending candidates are filtered by review history
- ✅ submitReview with APPROVED decision persists correctly
- ✅ submitReview with REJECTED decision persists correctly
- ✅ submitReview with DEFERRED decision persists correctly
- ✅ submitReview with empty note persists correctly
- ✅ submitReview preserves evidence and attribution
- ✅ Review history updates after submission
- ✅ Stats update correctly after multiple reviews

**Lint:**
```
./gradlew :app:lintDebug
BUILD SUCCESSFUL
Lint: 0 errors, 0 warnings
```

**Build:**
```
./gradlew :app:assembleDebug
BUILD SUCCESSFUL
APK: 57MB at app/build/outputs/apk/debug/app-debug.apk
```

## User-Facing Behavior

Users can now:
1. **Navigate** from Settings → Access section → "Editorial Review" card
2. **View** demo review dashboard with approval statistics
3. **Inspect** three pending candidates:
   - Cross-language event match (Geneva Climate Summit)
   - Suspected wire service syndication
   - Event cluster proposal (AI Regulation)
4. **Review** evidence including sources, languages, shared entities, and uncertainty reasons
5. **Decide** to approve, reject, or defer with optional editorial notes
6. **Track** review history showing all past decisions with audit trail
7. **Reset** all demo reviews to restore original candidates and re-test the workflow
8. **Understand** this is a fictional demo workflow through visible notices

**Demo Workflow Notice:**
> ⚠️ Demo Review Workflow
> 
> This is a fictional prototype for reviewing mock ingestion candidates. All data is demo-only and does not represent real news content.

## Known Limitations

### Scope Boundaries
- **Offline demo only:** No live ingestion pipeline integration
- **Mock candidates:** Uses hardcoded test data, not real article ingestion
- **Local persistence:** Reviews stored in Room, not production systems
- **No authentication:** Production requires reviewer roles and permissions
- **No queue management:** Production needs assignment and priority system

### Device Testing
- Manual accessibility testing incomplete (no device available)
- Physical device testing not performed (adb not available)
- TalkBack verification pending
- Performance profiling not completed

See complete limitations in `docs/INGESTION_PROTOTYPE.md` and `README.md`.

## Production Prerequisites

Before this workflow can handle live ingestion:
1. ✅ Connect to real-time ingestion pipeline
2. ✅ Implement authenticated reviewer identity system
3. ✅ Add review queue with assignment and priority
4. ✅ Integrate with story publication workflow
5. ✅ Add audit logging with reviewer attribution
6. ✅ Build registry admin UI for source approvals

See `docs/INGESTION_PROTOTYPE.md` for complete integration requirements.

## Installation

Download `app-debug.apk` from this release and install on Android 10+ devices.

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

## Verification

The implementation has been verified with:
1. ✅ Unit tests for ViewModel state transitions and evidence preservation (9/9 passing)
2. ✅ Full test suite including all previous tests (24/24 passing)
3. ✅ Lint clean (0 errors, 0 warnings)
4. ✅ Clean build successful after clean
5. ✅ String resources properly externalized
6. ✅ Accessibility content descriptions added
7. ⚠️ Device testing not performed (no emulator/device available)

## Documentation Updates

- **README.md:** Added editorial review to features, updated roadmap
- **INGESTION_PROTOTYPE.md:** Documented offline workflow, boundaries, and production prerequisites
- **String resources:** 22 new localized strings with accessibility labels

## Related Work

This milestone builds on:
- Source adapter contracts (`SourceAdapter.kt`)
- Entity extraction prototype (`EntityExtraction.kt`)
- Cross-language event matching (`CrossLanguageEventMatcher.kt`)
- Syndication detection (`SyndicationDetector.kt`)
- Editorial decision persistence (`EditorialReviewEntity`)

Next steps: Connect to live ingestion pipeline with authenticated reviewer system.
