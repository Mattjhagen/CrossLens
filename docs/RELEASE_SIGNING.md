# Release signing and artifact verification

This guide describes the current candidate. **No production key was created, replaced or used in this session.** Unsigned artifacts are allowed for verification; they cannot be submitted as signed Play uploads.

CrossLens release application ID stays `com.crosslens.app`. Debug uses `com.crosslens.app.debug`. Preserve the owner's existing signing lineage. With Play App Signing, the app signing key and upload key have different roles; recovery/rotation depends on enrollment and Google's supported procedures. See [official signing documentation](https://developer.android.com/studio/publish/app-signing).

## Credentials

Use the existing owner-managed keystore. Do not regenerate a key because a file is missing. `keystore.properties`, JKS/keystore files and service configuration are ignored; keep them outside Git. Root-relative storeFile paths resolve from the repository root.

Supported environment variables: `CROSSLENS_STORE_FILE`, `CROSSLENS_STORE_PASSWORD`, `CROSSLENS_KEY_ALIAS`, `CROSSLENS_KEY_PASSWORD`. Build property precedence is Gradle properties → environment → ignored keystore.properties. Use a secure local/CI secret store; avoid passing passwords on command lines, printing them or writing them into logs. `keystore.properties.example` shows public field names only.

When all signing values exist, release builds use the owner's key. With none, release APK/AAB are unsigned. An unsigned AAB may still be named app-release.aab; the filename does not prove a signature. No debug-signing fallback is configured for release.

## Build and verify

```sh
./gradlew :app:assembleRelease :app:bundleRelease :app:lintRelease
python3 scripts/verify_release_artifacts.py
apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

Unsigned APK normally has `app-release-unsigned.apk`; verify the actual output path. Compare the public signing-certificate fingerprint against the owner's known upload certificate. Preserve mapping.txt, artifact SHA-256, versionCode, commit and test reports together. Validate AAB with official bundletool and test Play-generated split APKs through internal testing. Self-signed certificate warnings do not replace signature verification; do not ignore an unsigned-archive warning.

Default builds need no Firebase credentials. To opt into a configured Firebase build, supply a matching ignored google-services.json for each package being built and `-Pcrosslens.firebaseEnabled=true`. Runtime crash collection remains disabled. Mapping upload additionally requires `-Pcrosslens.uploadCrashlyticsMapping=true` and must be verified in that configured environment; no upload ran here.

Configure public in-app pages using `-Pcrosslens.privacyPolicyUrl=https://OWNER/POLICY` and `-Pcrosslens.contactUrl=https://OWNER/CONTACT`, after hosting real owner-reviewed pages. Placeholders above are documentation only.

VersionCode must exceed all previously uploaded codes; skipping unused numbers is permitted. Candidate code 16 exceeds the checked-out code 15, but Play Console's highest uploaded code is unknown. Never uninstall a real user's existing app to work around a signing mismatch: resolve signing lineage and test an update with `adb install -r` on a controlled test device, preserving its data.

All commands in this session stopped before compilation because no JDK/SDK was installed. No APK/AAB or certificate verification exists yet. See [actual results](verification/2026-11-01/RESULTS.md) and [release checklist](PLAY_STORE_RELEASE_CHECKLIST.md).
