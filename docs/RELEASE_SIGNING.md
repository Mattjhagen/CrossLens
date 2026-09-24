# Release Signing Guide

This document describes how to generate, protect, and use signing keys for CrossLens release builds.

## Overview

Android requires all APKs and AABs to be digitally signed. The signing key:
- **Identifies you as the publisher** to Google Play and users
- **Enables app updates:** You must use the same key to update an existing app installation
- **Cannot be changed:** Once published with a key, that app must use the same key forever
- **Must be backed up securely:** Losing your signing key means you cannot update your app

CrossLens uses a **user-owned signing key** stored locally and never committed to Git.

## Quick Reference

| File | Location | Committed to Git? | Purpose |
|------|----------|-------------------|---------|
| `keystore.properties.example` | Project root | ✅ Yes | Example configuration with placeholders |
| `keystore.properties` | Project root | ❌ No | Your actual signing credentials (ignored) |
| `crosslens-release.jks` | `keystore/` or secure location | ❌ No | Your actual signing key (ignored) |
| `keystore-backup/` | Encrypted backup location | ❌ No | Secure backup copies |

## One-Time Setup: Generate Your Signing Key

### Prerequisites
- JDK 21 installed (same JDK used for builds)
- Secure location to store the keystore file and backup

### Step 1: Create Keystore Directory

```bash
cd /Users/matt/CrossLens
mkdir -p keystore
```

### Step 2: Generate Release Keystore

⚠️ **IMPORTANT:** Choose a strong, unique password. You'll need this forever. Store it in a password manager immediately.

```bash
keytool -genkeypair \
  -keystore keystore/crosslens-release.jks \
  -alias crosslens-release-key \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD \
  -dname "CN=CrossLens,OU=Mobile,O=Your Organization,L=Your City,ST=Your State,C=US"
```

**Replace:**
- `YOUR_STORE_PASSWORD` — Strong password for the keystore file (e.g., 20+ random characters)
- `YOUR_KEY_PASSWORD` — Strong password for the key (can be same as store password)
- DN fields (CN, OU, O, L, ST, C) — Your organization information (or personal name)

**Example with strong passwords:**
```bash
keytool -genkeypair \
  -keystore keystore/crosslens-release.jks \
  -alias crosslens-release-key \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storepass "$(openssl rand -base64 32)" \
  -keypass "$(openssl rand -base64 32)" \
  -dname "CN=CrossLens,OU=Mobile,O=Matt Hagen,L=Seattle,ST=WA,C=US"
```

**Key details:**
- **Validity:** 10,000 days (~27 years) ensures the key won't expire during app lifetime
- **Key size:** 4096-bit RSA provides strong cryptographic security
- **Alias:** Identifies which key to use (one keystore can hold multiple keys)

### Step 3: Verify Key Creation

```bash
keytool -list -v -keystore keystore/crosslens-release.jks -alias crosslens-release-key
# Enter store password when prompted
```

Expected output should show:
- Alias name: `crosslens-release-key`
- Creation date
- Entry type: `PrivateKeyEntry`
- Certificate fingerprints (SHA-256, SHA-1)

**Record the SHA-256 fingerprint** — you'll need it for Firebase, Google Play, and other services.

### Step 4: Create Configuration File

```bash
cp keystore.properties.example keystore.properties
```

Edit `keystore.properties` and replace placeholders:

```properties
storeFile=keystore/crosslens-release.jks
storePassword=YOUR_ACTUAL_STORE_PASSWORD
keyAlias=crosslens-release-key
keyPassword=YOUR_ACTUAL_KEY_PASSWORD
```

✅ Verify `keystore.properties` is listed in `.gitignore` (already configured)

### Step 5: Verify Build Configuration

```bash
# This should fail with clear instructions (keystore not found)
./gradlew :app:assembleRelease

# After creating keystore.properties, this should succeed
./gradlew :app:assembleRelease
```

## Secure Backup Strategy

**Critical:** Losing your signing key means you cannot update your app. Back it up securely.

### Backup Locations (Choose Based on Your Security Model)

**Option 1: Encrypted Cloud Storage (Recommended)**
```bash
# Create encrypted backup directory
mkdir -p ~/Documents/crosslens-keystore-backup

# Copy keystore and properties
cp keystore/crosslens-release.jks ~/Documents/crosslens-keystore-backup/
cp keystore.properties ~/Documents/crosslens-keystore-backup/

# Encrypt with strong password (store password in password manager)
zip -e ~/Documents/crosslens-keystore-backup.zip \
  ~/Documents/crosslens-keystore-backup/*

# Upload crosslens-keystore-backup.zip to:
# - 1Password / LastPass secure notes
# - iCloud Drive (in encrypted folder)
# - Google Drive (personal, not shared)
# - Dropbox (in encrypted folder)

# Keep encrypted backup updated after any changes
```

**Option 2: Physical Media**
```bash
# Copy to encrypted USB drive or external SSD
cp keystore/crosslens-release.jks /Volumes/SecureBackup/
cp keystore.properties /Volumes/SecureBackup/

# Store physical media in:
# - Safe deposit box
# - Fireproof safe at home
# - Separate physical location from primary development machine
```

**Option 3: Password Manager**
- Store the `.jks` file as a secure attachment in 1Password/LastPass
- Store `keystore.properties` contents as secure note
- Enable 2FA on password manager account

### Backup Verification

Test restore process periodically:
```bash
# Extract backup
unzip ~/Documents/crosslens-keystore-backup.zip -d /tmp/keystore-restore-test

# Verify keystore is valid
keytool -list -v -keystore /tmp/keystore-restore-test/crosslens-release.jks

# Clean up
rm -rf /tmp/keystore-restore-test
```

## Using Environment Variables (CI/CD or Shared Machines)

Instead of `keystore.properties` file, use environment variables:

```bash
export CROSSLENS_STORE_FILE=/path/to/keystore/crosslens-release.jks
export CROSSLENS_STORE_PASSWORD=your_store_password
export CROSSLENS_KEY_ALIAS=crosslens-release-key
export CROSSLENS_KEY_PASSWORD=your_key_password
```

For CI/CD (GitHub Actions, GitLab CI):
- Store keystore file as base64-encoded secret
- Decode at build time: `echo $KEYSTORE_BASE64 | base64 -d > keystore.jks`
- Pass passwords as encrypted environment variables
- **Never log or print signing credentials**

## Building Release APK

### With Signing Credentials

```bash
# Build signed release APK
./gradlew :app:assembleRelease

# Output location
ls -lh app/build/outputs/apk/release/app-release.apk
```

### Verify Signed APK

```bash
# Extract signing certificate info
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk

# Verify SHA-256 fingerprint matches your keystore
keytool -list -v -keystore keystore/crosslens-release.jks -alias crosslens-release-key
```

**Match these fields:**
- Owner DN (CN, OU, O, etc.)
- Certificate fingerprints (SHA-256)
- Valid from/until dates

### Verify APK Content

```bash
# Check APK signature
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk

# Expected output:
# Verified using v1 scheme (JAR signing): true
# Verified using v2 scheme (APK Signature Scheme v2): true
# Verified using v3 scheme (APK Signature Scheme v3): true
# Number of signers: 1
```

### Without Signing Credentials

If `keystore.properties` is missing, the build will fail with:

```
Release signing configuration missing.
Create keystore.properties or set CROSSLENS_* environment variables.
See docs/RELEASE_SIGNING.md for setup instructions.
```

## Upgrade Compatibility

**Critical:** Apps can only be updated if signed with the same key.

### Rules
1. **Same package name:** Must always be `com.crosslens.app` (not `com.crosslens.app.debug`)
2. **Same signing key:** Must use the same keystore, alias, and password forever
3. **Increasing version codes:** Each release must have a higher `versionCode`

### Version Management

In `app/build.gradle.kts`:
```kotlin
defaultConfig {
    applicationId = "com.crosslens.app"  // Never change
    versionCode = 1      // Increment for each release: 1, 2, 3, ...
    versionName = "0.0.12-beta"  // Human-readable version
}
```

**Version code rules:**
- Must be an integer
- Must increase with each release (Play Store enforces this)
- Cannot skip numbers, but can jump (e.g., 1 → 10 is valid)
- Debug builds don't affect release version codes

### Testing Upgrade Compatibility

```bash
# Install current release
adb install app/build/outputs/apk/release/app-release.apk

# Build next version (increment versionCode first)
./gradlew :app:assembleRelease

# Upgrade install (should succeed without -r flag)
adb install app/build/outputs/apk/release/app-release.apk

# If signature mismatch, you'll see:
# INSTALL_FAILED_UPDATE_INCOMPATIBLE
```

## Key Rotation (Advanced)

Google Play supports key rotation via **Play App Signing**:
1. Upload old and new keys to Play Console
2. Play re-signs APKs with new key
3. Maintains upgrade compatibility

**When to rotate:**
- Key compromise suspected
- Moving from development key to production key
- Migrating to a different organization

**Process:**
1. Generate new keystore (same steps as initial setup)
2. Create opt-in request in Play Console
3. Upload both old and new certificates
4. Google handles signing with new key
5. Keep old key for 2+ years (Play requirement)

See: https://developer.android.com/studio/publish/app-signing#upgrade-key

## Key Compromise Response

If your signing key is compromised:

1. **Immediately:**
   - Remove compromised keystore from all systems
   - Revoke any CI/CD secrets containing the key
   - Change all passwords associated with the key

2. **For apps not yet published:**
   - Generate a new keystore
   - Update `keystore.properties`
   - Rebuild and sign with new key
   - No user impact (app not in production)

3. **For published apps:**
   - Contact Google Play support immediately
   - May require app takedown and republish under new package name
   - Users will need to uninstall and reinstall (cannot update)
   - Consider Play App Signing to prevent this scenario

## Security Best Practices

✅ **Do:**
- Use strong, unique passwords (20+ random characters)
- Store passwords in a password manager
- Back up keystore to at least 2 secure locations
- Test backup restore process periodically
- Use environment variables for CI/CD
- Restrict keystore file permissions: `chmod 600 keystore/*.jks`
- Enable 2FA on all accounts storing keystore backups

❌ **Don't:**
- Commit keystore or passwords to Git
- Share keystore via email or chat
- Reuse passwords from other services
- Store unencrypted keystore in cloud storage
- Use weak passwords like "password123"
- Grant others access to keystore without secure handoff
- Use debug keystore for release builds

## Troubleshooting

### Build fails: "keystore not found"
**Cause:** `keystore.properties` missing or `storeFile` path incorrect

**Fix:**
```bash
# Verify file exists
ls -la keystore.properties

# Check path is relative to project root
grep storeFile keystore.properties
```

### Build fails: "incorrect password"
**Cause:** Wrong password in `keystore.properties`

**Fix:**
```bash
# Verify password works with keytool
keytool -list -v -keystore keystore/crosslens-release.jks
# If this fails, password is wrong
```

### Install fails: "signature mismatch"
**Cause:** Device has app installed with different signature

**Fix:**
```bash
# Uninstall existing app first
adb uninstall com.crosslens.app

# Then install release
adb install app/build/outputs/apk/release/app-release.apk
```

### SHA-256 fingerprint doesn't match
**Cause:** Using wrong keystore file or alias

**Fix:**
```bash
# Verify alias in keystore
keytool -list -v -keystore keystore/crosslens-release.jks

# Check keystore.properties has correct alias
grep keyAlias keystore.properties
```

## References

- [Android App Signing Documentation](https://developer.android.com/studio/publish/app-signing)
- [keytool Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)
- [Google Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756)
- [APK Signature Scheme v3](https://source.android.com/security/apksigning/v3)

## Next Steps

After completing this setup:
1. ✅ Generate signing key and back it up securely
2. ✅ Create `keystore.properties` with actual credentials
3. ✅ Build and verify signed release APK
4. Record SHA-256 fingerprint for Firebase/Google Play setup
5. See `docs/CRASH_REPORTING_PLAN.md` for next integration steps
