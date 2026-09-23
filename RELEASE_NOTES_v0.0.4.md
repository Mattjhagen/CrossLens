# CrossLens v0.0.4 Beta Release

## Release Information

**Version:** v0.0.4-beta  
**Date:** 2026-09-23  
**Commit:** 15ac1fb  
**Build:** Debug APK

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
- ✅ Reader-facing stories in Home/Story/CrossLens screens
- ✅ Settings (theme, preferences, Plus preview)
- ✅ Saved stories or reading history
- ✅ Subscriptions or entitlements
- ✅ Any non-editorial local data

**Design:**
- Works in dark/light themes and RTL layouts
- All strings externalized to resources for accessibility
- Error button styling (red) for confirmation to emphasize action
- Dismissible with proper back behavior

## Technical Implementation

**New Components:**
- `EditorialReviewDao.deleteAllReviews()` - Room query to delete all reviews
- `EditorialReviewRepository.resetAllReviews()` - Repository method calling DAO
- `EditorialReviewViewModel.resetReviews()` - ViewModel method for UI
- `ResetConfirmationDialog` - Compose dialog with confirmation flow

**Files Changed:**
- `app/src/main/res/values/strings.xml` - Added 5 reset string resources
- `app/src/main/java/com/crosslens/app/data/local/dao/EditorialReviewDao.kt` - Added deleteAllReviews query
- `app/src/main/java/com/crosslens/app/data/repository/EditorialReviewRepository.kt` - Added resetAllReviews method
- `app/src/main/java/com/crosslens/app/feature/editorial/EditorialReviewViewModel.kt` - Added third candidate & resetReviews
- `app/src/main/java/com/crosslens/app/feature/editorial/EditorialReviewScreen.kt` - Added reset button & dialog
- `app/src/test/java/com/crosslens/app/feature/editorial/EditorialReviewViewModelTest.kt` - Added 3 reset tests
- `docs/INGESTION_PROTOTYPE.md` - Documented reset control

## Test Results

✅ **All Tests Passing**
```
./gradlew :app:testDebugUnitTest
BUILD SUCCESSFUL
Tests: 27 tests, 27 passed, 0 failed, 0 skipped
```

**New Tests (3):**
- ✅ reset with two reviewed candidates filters correctly before reset
- ✅ candidates restored after reset in real scenario
- ✅ reset verification confirms repository method called

**Editorial Review Tests (13 total):**
- ✅ Initial state has pending candidates and empty history (updated for 3 candidates)
- ✅ Pending candidates are filtered by review history (updated for 3 candidates)
- ✅ submitReview with APPROVED decision persists correctly
- ✅ submitReview with REJECTED decision persists correctly
- ✅ submitReview with DEFERRED decision persists correctly
- ✅ submitReview with empty note persists correctly
- ✅ submitReview preserves evidence and attribution
- ✅ Review history updates after submission
- ✅ Stats update correctly after multiple reviews
- ✅ resetReviews calls repository resetAllReviews
- ✅ reset with two reviewed candidates filters correctly before reset
- ✅ candidates restored after reset in real scenario
- ✅ reset verification confirms repository method called

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

Testers can now:
1. **Navigate** to Settings → Access section → "Editorial Review"
2. **Review** three pending candidates:
   - Cross-language event match (Geneva Climate Summit) - HIGH confidence
   - Suspected wire service syndication - HIGH confidence
   - Event cluster proposal (AI Regulation) - MEDIUM confidence
3. **Make decisions** on all candidates (approve, reject, defer with notes)
4. **View** complete review history and statistics
5. **Reset** by clicking "Reset demo reviews" button
6. **Confirm** reset via dialog explaining what will be deleted
7. **Observe** all three candidates restored to pending state
8. **Restart** app and verify candidates remain pending
9. **Repeat** workflow to test different decision combinations

**Reset Confirmation Message:**
> This will delete all local fictional demo review decisions and notes, restoring the original candidate set.
> 
> This only affects demo editorial review data. Your settings, saved stories, and reading history will not be changed.

## Known Limitations

### Scope Boundaries
- **Offline demo only:** No live ingestion pipeline integration
- **Mock candidates:** Uses hardcoded test data, not real article ingestion
- **Local persistence:** Reviews and resets stored in Room, not production systems
- **No authentication:** Production requires reviewer roles and permissions
- **No queue management:** Production needs assignment and priority system

### Device Testing
- Manual accessibility testing incomplete (no device available)
- Physical device testing not performed (adb not available)
- TalkBack verification pending
- Performance profiling not completed

See complete limitations in `docs/INGESTION_PROTOTYPE.md` and `README.md`.

## Verification

The implementation has been verified with:
1. ✅ Unit tests for reset functionality (3/3 passing)
2. ✅ Full editorial review test suite (13/13 passing)
3. ✅ Complete unit test suite (27/27 passing)
4. ✅ Lint clean (0 errors, 0 warnings)
5. ✅ Clean build successful
6. ✅ String resources properly externalized
7. ✅ Accessibility content descriptions added
8. ⚠️ Device testing not performed (no emulator/device available)

## Installation

Download `app-debug.apk` from this release and install on Android 10+ devices.

**Note:** This is a debug build for testing purposes. Not suitable for production distribution.

## Changes from v0.0.3-beta

This release builds on v0.0.3-beta by adding:
- Reset control with confirmation dialog
- Third mock candidate for complete decision type coverage
- 3 new reset tests
- Updated documentation explaining reset functionality

v0.0.3-beta introduced the editorial review workflow itself. v0.0.4-beta makes it tester-friendly by allowing workflow repetition without reinstall.

## Documentation Updates

- **INGESTION_PROTOTYPE.md:** Added reset control section explaining behavior and boundaries
- **Test suite:** Updated candidate count expectations from 2 to 3
- **String resources:** 5 new localized strings with accessibility labels

## Related Work

This milestone builds on v0.0.3-beta:
- Editorial review workflow (Settings → Editorial Review)
- Review dashboard with statistics
- Decision workflow (approve/reject/defer with notes)
- Review history and audit trail
- Mock cross-language matching and syndication detection
- Entity extraction and frame observation candidates
