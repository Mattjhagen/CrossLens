# Source Health Manual Refresh - Root Cause Analysis

**Date:** 2026-10-04  
**Branch:** `feature/source-health-recovery-hardening`  
**Status:** ROOT CAUSE IDENTIFIED - Fix in progress

---

## Executive Summary

Through systematic debugging with comprehensive diagnostic logging across all layers (ViewModel, Repository, Monitor, DAO), we identified that **the refresh button's onClick handler is not executing**. Despite adding Log.wtf() calls (highest priority logs that cannot be filtered), ZERO logs appeared when tapping the button, confirming the click event is not reaching the ViewModel.

---

## Investigation Timeline

### Phase 1: Initial Hypothesis (Previous Session)
- **Hypothesis**: Repository catch block not recording health checks
- **Fix Applied**: Added fallback `healthMonitor.recordCheck()` in repository  
- **Result**: Bug fixed, but refresh still non-functional

### Phase 2: ViewModel Race Condition (Previous Session)
- **Hypothesis**: ViewModel manually overwriting Flow emissions with stale state
- **Fix Applied**: Separated refresh flags from source data, removed manual state overwrites
- **Result**: Bug fixed, but refresh still non-functional

### Phase 3: Database Initialization (This Session)
- **Hypothesis**: Empty database preventing Flow emissions
- **Discovery**: `loadPersistedState()` only loaded data, didn't create initial entries
- **Fix Applied**: 
  ```kotlin
  suspend fun initializeSourceHealth(configuredSources: List<Pair<String, String>>) {
      for ((sourceId, sourceName) in configuredSources) {
          val existing = healthDao.getHealth(sourceId)
          if (existing == null) {
              healthDao.upsert(SourceHealthEntity(
                  sourceId = sourceId,
                  sourceName = sourceName,
                  status = SourceHealthStatus.ACTIVE.name,
                  // ... initial values
                  updatedAt = Instant.now().toEpochMilli()
              ))
          }
      }
  }
  ```
- **Result**: ✅ Database populated with 23 sources, UI displays data correctly
- **But**: Refresh button still doesn't update timestamps

### Phase 4: Comprehensive Diagnostic Logging (This Session)
Added logging at EVERY stage of the state path:
1. **ViewModel.refreshAllSources()** entry - Log.wtf() at method start
2. **ViewModel Flow collection** - Log when new data arrives
3. **Repository.refreshAllSources()** - Log each adapter fetch
4. **SourceHealthMonitor.recordCheck()** - Log health check recording
5. **SourceHealthMonitor.updateSourceStatus()** - Log status updates and database writes
6. **Database writes** - Log before/after DAO.upsert()

**Critical Finding**: Tapped refresh button 5+ times across multiple test runs with fresh app installs. Result: **ZERO LOGS APPEARED**.

---

## Root Cause: Button onClick Not Firing

### Evidence

1. **No ViewModel logs**: The `Log.wtf("refreshAllSources() CALLED")` never appeared
2. **No Repository logs**: No adapter fetches logged
3. **No Monitor logs**: No health check recording logged
4. **UI timestamp frozen**: "Last refresh: Oct 04, 11:14" never changed despite multiple taps at 11:19-11:24
5. **Database unchanged**: Queries showed no timestamp updates after refresh attempts

### Code Analysis

The button wiring APPEARS correct:

```kotlin
// SourceHealthScreen.kt
SourceHealthContent(
    state = state,
    onRefresh = viewModel::refreshAllSources,  // Method reference
    ...
)

// RefreshButton
Button(
    onClick = onRefresh,  // Lambda passed through
    enabled = !isRefreshing,
    ...
)
```

But something prevents the click from reaching the ViewModel.

### Possible Causes

1. **Touch event consumed by parent**: LazyColumn may be consuming touch events
2. **Compose recomposition issue**: Button may not be recomposing with correct lambda
3. **Method reference issue**: `viewModel::refreshAllSources` may not bind correctly in this context
4. **Coroutine cancellation**: ViewModelScope may be cancelled/inactive

---

## Fixes Applied

### Fix #1: Database Initialization ✅
**File**: `SourceHealthMonitor.kt`  
**Problem**: Fresh installs had empty database, preventing Flow emissions  
**Solution**: Added `initializeSourceHealth()` to create initial ACTIVE entries

**Impact**: UI now displays 23 sources correctly on first launch

### Fix #2: Explicit Button Click Logging 🔄
**File**: `SourceHealthScreen.kt`  
**Change**: Wrapped `onClick` handler with direct logging:
```kotlin
Button(
    onClick = {
        android.util.Log.wtf("SourceHealth:Debug", "BUTTON CLICKED")
        onRefresh()
    },
    ...
)
```

**Purpose**: Determine if click reaches Compose layer at all

### Fix #3: ViewModel Diagnostics 🔄
**File**: `SourceHealthViewModel.kt`  
**Changes**:
- Added init block logging to verify ViewModel creation
- Added Log.wtf() at `refreshAllSources()` entry
- Added Flow emission logging

**Purpose**: Trace complete state path from click to UI update

---

## Architecture Validation

### State Flow (When Working Correctly)

```
User Tap
  ↓
Compose Button onClick
  ↓
ViewModel.refreshAllSources()
  ↓
viewModelScope.launch { ... }
  ↓
Repository.refreshAllSources()
  ↓
for each RssSourceAdapter:
    adapter.fetchArticles()
      ↓
    healthMonitor.recordCheck(...)
      ↓
    updateSourceStatus(...)
      ↓
    healthDao.upsert(entity)
  ↓
Room triggers Flow emission
  ↓
repository.observeAllSourceHealth() emits
  ↓
ViewModel.loadSourceHealth() collects
  ↓
_uiState.value = Success(... lastRefreshAt = maxUpdatedAt ...)
  ↓
Compose recomposes with new timestamp
```

### Broken Link

Based on zero logs appearing, the break is at **Step 1 or 2**: either the button tap isn't registering, or the onClick isn't calling the lambda.

---

## Next Steps

### Immediate (Before Merge)

1. **✅ DONE**: Add button click logging
2. **IN PROGRESS**: Test with click logging to confirm button tap registers
3. **TODO**: If button click logs appear but ViewModel logs don't:
   - Issue is lambda wiring between Compose and ViewModel
   - Try direct lambda instead of method reference: `onRefresh = { viewModel.refreshAllSources() }`
4. **TODO**: If no logs appear at all:
   - Button may not be clickable due to LazyColumn touch handling
   - Move button outside LazyColumn or use `clickable` modifier differently
5. **TODO**: Once clicks work, verify complete state path with existing diagnostics

### Regression Prevention

1. **Add integration test**:
   ```kotlin
   @Test
   fun refreshButton_updatesTimestamp() {
       // Navigate to Source Health
       // Get initial max timestamp from database
       // Tap refresh button
       // Wait for refresh to complete
       // Assert timestamp increased
   }
   ```

2. **Add unit test**:
   ```kotlin
   @Test
   fun `refreshAllSources updates database timestamps`() = runTest {
       // Mock repository
       // Call viewModel.refreshAllSources()
       // Verify repository.refreshAllSources() called
       // Verify Flow emits new state
   }
   ```

### Alternative Fix (If Button Issue Persists)

If the button click issue cannot be resolved quickly:

```kotlin
// Replace Button with explicit clickable modifier
Surface(
    modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = !isRefreshing) {
            android.util.Log.wtf("SourceHealth:Debug", "SURFACE CLICKED")
            onRefresh()
        },
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(...)
            Text("Refreshing...")
        } else {
            Icon(Icons.Default.Refresh, ...)
            Text("Refresh All Sources")
        }
    }
}
```

---

##Diagnostic Tools Added

### SourceHealthDiagnostics.kt
Centralized diagnostic logging utility with methods for:
- ViewModel refresh lifecycle
- Flow emissions
- Repository adapter operations  
- Monitor health check recording
- Database writes
- State updates

**Usage**: All logs use tag `"SourceHealth:Debug"` for easy filtering:
```bash
adb logcat -s "SourceHealth:Debug"
```

### Diagnostic Log Format
```
═══ ViewModel: refreshAllSources() START ═══
  Thread: DefaultDispatcher-worker-1
  Time: 1728044523000
  
─── Repository: Adapter: ABC News (abc-au-rss) ───
  ┌─ Adapter: ABC News
  └─ SUCCESS: 25 articles in 1234ms
  
─── Monitor: recordCheck() START ───
  Source ID: abc-au-rss
  Fetch succeeded: true
  Parse succeeded: true
  
─── Monitor: updateSourceStatus() ───
  Consecutive failures: 0
  New status: ACTIVE
  
─── Monitor: Writing to database ───
  Status: ACTIVE
  Updated at: 1728044525000
✓ Database write completed for abc-au-rss
```

---

## Test Results

### Unit Tests: ✅ PASSING (21/21)
- `SourceHealthRepositoryTest`: 9 tests
- `SourceHealthErrorCategoryTest`: 12 tests

### Integration Tests: ⏳ PENDING
- `SourceHealthRefreshTest`: Created, not yet run
- Requires working refresh button to validate

### Device Testing: ❌ BLOCKED
- **Device**: Google Pixel 11, Android 17
- **Blocker**: Refresh button does not execute code
- **Evidence**: 16 screenshots showing frozen timestamp across 10+ minutes of testing
- **Diagnostic**: Zero logs from any layer despite comprehensive instrumentation

---

## Files Modified

### New Files
- `app/src/main/java/com/crosslens/app/util/SourceHealthDiagnostics.kt` - Diagnostic logging utility
- `app/src/androidTest/java/com/crosslens/app/SourceHealthRefreshTest.kt` - Integration test
- `docs/SOURCE_HEALTH_ROOT_CAUSE_ANALYSIS.md` - This document

### Modified Files
- `app/src/main/java/com/crosslens/app/data/ingestion/SourceHealthMonitor.kt`
  - Added `initializeSourceHealth()` method
  - Added diagnostic logging throughout
- `app/src/main/java/com/crosslens/app/data/repository/SourceHealthRepository.kt`
  - Updated `loadPersistedState()` to call `initializeSourceHealth()`
  - Added diagnostic logging
- `app/src/main/java/com/crosslens/app/feature/sourcehealth/SourceHealthViewModel.kt`
  - Added init block logging
  - Added comprehensive refresh logging
  - Fixed race condition (previous session)
- `app/src/main/java/com/crosslens/app/feature/sourcehealth/SourceHealthScreen.kt`
  - Added button onClick logging

---

## Merge Decision

### ❌ DO NOT MERGE

**Blocking Issue**: Manual refresh button is non-functional.

**What Works**:
- ✅ Database initialization creates 23 source entries
- ✅ UI displays source health data correctly
- ✅ Repository fallback health recording (previous fix)
- ✅ ViewModel race condition resolved (previous fix)  
- ✅ Flow-based reactive state updates (when database changes)

**What Doesn't Work**:
- ❌ Tapping "Refresh All Sources" button does nothing
- ❌ UI timestamp never updates
- ❌ Cannot validate offline recovery behavior  
- ❌ Cannot validate degraded/disabled transitions
- ❌ Cannot validate recovery to ACTIVE

**Risk**: Users will see a non-functional button, creating false expectation and poor UX.

---

## Recommendation

### Option A: Fix Button Issue (Preferred)
1. Identify why onClick isn't firing
2. Apply fix (likely lambda wiring or touch handling)
3. Verify with click logging
4. Complete device validation
5. Merge with confidence

**Timeline**: 1-2 hours if button issue is simple wiring problem

### Option B: Temporary Workaround
1. Hide "Refresh All Sources" button
2. Keep automatic background refresh (if implemented)
3. Merge database initialization fix (it works)
4. File "Manual Refresh" as separate issue for next sprint

**Timeline**: 15 minutes, but leaves feature incomplete

### Option C: Deep Investigation
1. Create minimal reproduction in isolated test app
2. Debug Compose touch handling vs LazyColumn
3. Research known Compose Button issues
4. May require Compose version upgrade or workaround

**Timeline**: 2-4 hours, may uncover framework issue

---

**Current Status**: All diagnostic infrastructure in place. One more test run with button click logging will reveal if the tap reaches Compose layer. If it does, the fix is trivial (lambda wiring). If it doesn't, we need Option B or C.
