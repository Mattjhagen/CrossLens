# Crash Reporting Integration Plan

This document outlines the plan for adding crash reporting to CrossLens. **No integration is performed yet** — this is preparatory documentation only.

## Overview

Crash reporting helps identify and fix bugs that occur in production. The plan is to integrate **Firebase Crashlytics** for:
- Automatic crash and error logging
- Breadcrumbs showing user actions before crashes
- Non-fatal exception tracking
- Performance monitoring (optional)

## Why Firebase Crashlytics

**Advantages:**
- ✅ Free for small-scale apps
- ✅ Deep Android integration
- ✅ Automatic crash symbolication for ProGuard/R8
- ✅ Real-time alerts
- ✅ User impact metrics (crash-free users %)
- ✅ Works offline (crashes uploaded when network available)
- ✅ Integrates with Google Play Console
- ✅ No PII collection by default (GDPR-friendly)

**Alternatives considered:**
- Sentry (paid after free tier, better multi-platform)
- Bugsnag (paid, good for enterprise)
- Rollbar (paid, strong filtering)

## Prerequisites (Must Complete First)

Before integrating Crashlytics, complete:

1. ✅ **Release signing setup** (this milestone)
   - Need SHA-256 certificate fingerprint for Firebase project setup
   - Location: `keytool -list -v -keystore keystore/crosslens-release.jks`

2. ⏳ **Firebase project creation** (next milestone)
   - Create project at https://console.firebase.google.com
   - Add Android app with package name: `com.crosslens.app`
   - Register SHA-256 fingerprint from release keystore
   - Download `google-services.json`

3. ⏳ **Privacy policy update** (before production)
   - Disclose crash reporting and what data is collected
   - Provide opt-out mechanism (if required by jurisdiction)

## Integration Steps (Future Milestone)

### Step 1: Firebase Project Setup

**Manual steps in Firebase Console:**

1. Go to https://console.firebase.google.com
2. Click "Add project" or select existing project
3. Enter project name: `CrossLens` (or choose your own)
4. Disable Google Analytics (or enable if you want it)
5. Click "Add app" → Android
6. Enter Android package name: `com.crosslens.app`
7. Enter SHA-256 fingerprint:
   ```bash
   keytool -list -v -keystore keystore/crosslens-release.jks -alias crosslens-release-key | grep SHA256
   ```
8. Download `google-services.json`
9. Place in `app/google-services.json` (already gitignored)

**Important:** Do NOT add debug keystore fingerprint to production Firebase project. Use separate Firebase project for debug builds if needed.

### Step 2: Add Dependencies

In `app/build.gradle.kts`:

```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services") version "4.4.0"
    id("com.google.firebase.crashlytics") version "2.9.9"
}

dependencies {
    // ... existing dependencies
    
    // Firebase BoM (Bill of Materials - manages versions)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Crashlytics (version managed by BoM)
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    
    // Optional: Analytics (for better crash context)
    // implementation("com.google.firebase:firebase-analytics-ktx")
}
```

In project-level `build.gradle.kts`:

```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}
```

### Step 3: Initialize Crashlytics

In `Application` class (create if doesn't exist):

```kotlin
package com.crosslens.app

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CrossLensApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            // Enable crash reporting
            setCrashlyticsCollectionEnabled(true)
            
            // Set custom keys for debugging
            setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            setCustomKey("version_name", BuildConfig.VERSION_NAME)
            setCustomKey("version_code", BuildConfig.VERSION_CODE)
        }
    }
}
```

Update `AndroidManifest.xml`:

```xml
<application
    android:name=".CrossLensApplication"
    ...>
```

### Step 4: Add Mapping File Upload for ProGuard

In `app/build.gradle.kts` release build type:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        
        // Upload ProGuard mapping files to Crashlytics
        // This enables symbolicated stack traces
        firebaseCrashlytics {
            mappingFileUploadEnabled = true
        }
    }
}
```

### Step 5: Add Crashlytics ProGuard Rules

In `app/proguard-rules.pro`:

```proguard
# Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Keep crash reporting annotations
-keepattributes *Annotation*
```

### Step 6: Test Crash Reporting

Add test crash button in debug builds:

```kotlin
// In settings or debug menu
if (BuildConfig.DEBUG) {
    Button(onClick = { throw RuntimeException("Test crash") }) {
        Text("Trigger Test Crash")
    }
}
```

Build and test:
```bash
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# Trigger crash, reopen app
# Check Firebase Console → Crashlytics (may take 5-10 min)
```

## Usage Patterns

### Log Non-Fatal Exceptions

```kotlin
try {
    // Risky operation
    storyRepository.syncStories()
} catch (e: Exception) {
    // Log to Crashlytics without crashing
    FirebaseCrashlytics.getInstance().recordException(e)
    
    // Still handle gracefully
    _uiState.value = HomeUiState.Error(e.message)
}
```

### Add Breadcrumbs

```kotlin
// Track user actions before crash
FirebaseCrashlytics.getInstance().log("User opened story: $storyId")
FirebaseCrashlytics.getInstance().log("User toggled For You: $enabled")
```

### Set User Identifier (Optional)

```kotlin
// Only if you have user accounts
// DO NOT use PII (email, real name)
FirebaseCrashlytics.getInstance().setUserId(userId)
```

### Custom Crash Keys

```kotlin
// Add context to crashes
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("story_count", storyList.size)
    setCustomKey("network_available", isNetworkAvailable)
    setCustomKey("entitlement", entitlement.activeTier.name)
}
```

## Privacy Considerations

### Data Collected by Default

Firebase Crashlytics collects:
- ✅ Stack traces (file names, line numbers, method names)
- ✅ Device model, OS version, screen size
- ✅ App version code and name
- ✅ Timestamp of crash
- ✅ Crash-free users percentage
- ✅ Custom keys and breadcrumbs you add

### Data NOT Collected by Default

- ❌ User email or name
- ❌ User IDs (unless explicitly set)
- ❌ Location data
- ❌ Analytics events (unless Firebase Analytics enabled)
- ❌ User preferences or saved data
- ❌ Story content or article text

### GDPR Compliance

**Crashlytics is privacy-friendly by default:**
- No PII collection without explicit opt-in
- Data stored in Google Cloud (EU/US)
- Can be disabled per-user if needed

**Recommended approach:**
1. Disclose crash reporting in privacy policy
2. Collect crashes by default (legitimate interest)
3. Provide opt-out in Settings (user choice)
4. Do NOT set user IDs with PII

**Opt-out implementation:**

```kotlin
// In Settings screen
var crashReportingEnabled by remember { mutableStateOf(true) }

Switch(
    checked = crashReportingEnabled,
    onCheckedChange = { enabled ->
        crashReportingEnabled = enabled
        FirebaseCrashlytics.getInstance()
            .setCrashlyticsCollectionEnabled(enabled)
    }
)
```

## Testing Strategy

### Debug Builds
- Test crashes locally before release
- Verify stack traces are readable
- Check custom keys appear in console

### Release Builds
- Test with ProGuard enabled
- Verify mapping file upload works
- Confirm symbolicated stack traces
- Test offline crash upload (crash while offline, then go online)

### CI/CD Integration
```bash
# Build release with Crashlytics
./gradlew :app:assembleRelease

# Mapping files automatically uploaded to Firebase
# Check: app/build/outputs/mapping/release/mapping.txt
```

## Monitoring and Alerts

### Firebase Console
- Dashboard: https://console.firebase.google.com/project/[project-id]/crashlytics
- Real-time crash alerts
- Crash-free users percentage
- Most impacted devices/OS versions

### Email Alerts
Configure in Firebase Console → Crashlytics → Settings:
- New issue detected
- Regressed issue (previously fixed)
- Velocity alerts (sudden spike)

### Integration with Issue Tracker
- Link crashes to GitHub issues
- Jira integration (if using)
- Slack notifications

## Rollout Plan

**Phase 1: Testing (v0.0.13-beta or later)**
- ✅ Add Crashlytics to debug builds only
- ✅ Test crash reporting locally
- ✅ Verify ProGuard mapping upload
- ✅ Document in privacy policy

**Phase 2: Beta Release**
- ✅ Enable Crashlytics in beta releases
- ✅ Monitor for 2-4 weeks
- ✅ Fix critical crashes before production
- ✅ Add opt-out toggle in Settings

**Phase 3: Production**
- ✅ Enable Crashlytics in production release
- ✅ Set up email alerts
- ✅ Monitor daily for first month
- ✅ Establish crash-free target (e.g., 99.5%)

## Cost Estimate

**Firebase Crashlytics Pricing:**
- Free tier: Unlimited crashes and events
- No paid tier needed for most apps
- Storage costs: Negligible (crash reports are small)

**Play Console Crash Reporting:**
- Google Play also provides crash reporting (free)
- Less detailed than Crashlytics
- Consider as backup or alternative

## Alternatives to Firebase

If Firebase is not suitable:

**Sentry:**
- Free: 5,000 events/month
- Paid: $26/month for 50,000 events
- Better for multi-platform (iOS, web, backend)
- Self-hosted option available

**Bugsnag:**
- Free: 7,500 events/month
- Paid: $59/month for 50,000 events
- Good dashboard and filtering

**Play Console Only:**
- Free, no setup required
- Less detailed stack traces
- No non-fatal exception tracking
- Good enough for small apps

## Security Considerations

### Secrets in Stack Traces

**Risk:** Accidentally logging sensitive data in exceptions

**Mitigation:**
```kotlin
// BAD: Logs password in stack trace
throw Exception("Login failed for user: $email with password: $password")

// GOOD: No sensitive data
throw Exception("Login failed: invalid credentials")

// GOOD: Log separately (not in exception message)
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("error_type", "login_failure")
    setCustomKey("has_email", email.isNotEmpty())
    // DO NOT log actual email or password
}
throw Exception("Authentication failed")
```

### ProGuard Mapping File Protection

**Risk:** Mapping file leaks allow reverse engineering

**Mitigation:**
- Mapping files stored in Firebase (access controlled)
- Not included in APK
- Only uploaded during build (not committed to Git)
- Restrict Firebase Console access

### API Key Security

**Risk:** `google-services.json` contains API keys

**Mitigation:**
- Already gitignored (✅)
- API keys are restricted to package name in Firebase Console
- Not sensitive (public in APK anyway)
- Use Firebase App Check for API abuse protection (future)

## Success Metrics

Track after integration:

- **Crash-free users:** Target 99%+ (within 7 days of release)
- **Most common crashes:** Fix top 3 each release
- **Time to fix:** Respond to critical crashes within 24 hours
- **Crash velocity:** Alert if crash rate spikes >10% in 1 hour

## Next Steps

1. ✅ Complete release signing setup (v0.0.12-beta — this milestone)
2. ⏳ Generate release keystore and record SHA-256 fingerprint
3. ⏳ Create Firebase project (v0.0.13-beta planned milestone)
4. ⏳ Download `google-services.json`
5. ⏳ Add Crashlytics dependencies and initialization
6. ⏳ Test crash reporting in debug builds
7. ⏳ Update privacy policy
8. ⏳ Enable in beta release and monitor

## References

- [Firebase Crashlytics Documentation](https://firebase.google.com/docs/crashlytics)
- [Crashlytics Android Setup](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [ProGuard with Crashlytics](https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android)
- [Crashlytics Privacy](https://firebase.google.com/support/privacy)
- [GDPR and Firebase](https://firebase.google.com/support/privacy/manage-iab-tcf-settings)

---

**Status:** 📋 Planning phase  
**Next Milestone:** v0.0.13-beta (Crash Reporting Integration)  
**Blocked by:** Release signing setup, Firebase project creation
