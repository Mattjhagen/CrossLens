# Free Premium Verification

Candidate: `codex/free-premium-remove-demo` (temporary `PLUS_FREE` entitlement).

## Automated checks

- Debug APK packaging completed: `:app:assembleDebug` (2026-11-01).
- `:app:testDebugUnitTest` passed with JDK 17 and Android platform 36.
- `:app:lintDebug` ran and failed on an existing `UnusedContentLambdaTargetStateParameter` error in `ArticleNavigatorScreen.kt:363`; it is outside this change and has not been suppressed.
- `:app:assembleRelease` and `:app:compileDebugAndroidTestKotlin` passed; the release APK is unsigned.
- Android instrumentation tests require a connected device or emulator; none was available in this environment.
- Device navigation, accessibility, and screenshots have not been verified.

## Artifact

`CrossLens-free-premium-debug.apk` is the debug build. Billing code remains present but premium features are temporarily available without purchase. This is not a release build.
