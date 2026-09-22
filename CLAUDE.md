# CrossLens repository instructions

Read `README.md` and `docs/ANDROID_BUILD_GUIDE.md` before implementation. The README defines product intent and proposed domain contracts; the build guide defines the initial milestone and its acceptance criteria.

## Scope

Build a native Android skeleton using Kotlin, Jetpack Compose, Material 3, MVVM, Coroutines/Flow, Hilt, Retrofit/OkHttp, and Room. Use a single app module, minimum API 29, and a mock-data-first repository architecture. Add DataStore for preferences. Keep the app usable offline after installation.

## Implementation rules

- Inspect the repository before editing and preserve unrelated work.
- Finish the build phases in order. Use small working increments; avoid speculative framework layers.
- Resolve a compatible stable toolchain from official documentation, pin versions, and record the chosen JDK/SDK/build commands. Do not guess incompatible version combinations or use dynamic versions.
- Expose immutable UI state from ViewModels. Use lifecycle-aware Flow collection, injected dispatchers where needed, and no blocking disk/network work on the main thread.
- Keep domain models separate from Room entities and network DTOs. Make mock/live selection a dependency-injection concern, not a screen concern.
- Seed deterministic, explicitly fictional sample reporting into Room idempotently. Preserve settings across restarts. Mock mode must make no network requests.
- Keep original content and translations separate. Label demo translations and demo Lens Gap values visibly. Do not invent a production scoring formula or label disputed claims as verified.
- Use string resources, accessible controls, dark/light themes, scalable text, and RTL-compatible layouts. Do not claim UI localization merely because sample articles have translations.
- Include a working Gradle wrapper, meaningful tests, and an updated README. Never claim a command passed unless it ran successfully; report environmental blockers precisely.
- Do not commit secrets, SDK paths, generated build outputs, or signing keys. Do not add a license without the owner's choice.

## Completion

Use the build guide's acceptance checklist and verification commands. Summarize the implemented behavior, test results, remaining limitations, and APK location. Do not extend the milestone into live services or deployment.
