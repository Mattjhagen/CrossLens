# CrossLens repository instructions

Read `README.md`, `docs/ANDROID_BUILD_GUIDE.md`, `docs/DESIGN_DIRECTION.md`, and `docs/CLAUDE_BUILD_RUNBOOK.md` before implementation. The README defines product intent and proposed domain contracts; the build guide defines the initial milestone and its acceptance criteria.

## Scope

Build a native Android skeleton using Kotlin, Jetpack Compose, Material 3, MVVM, Coroutines/Flow, Hilt, Retrofit/OkHttp, and Room. Use a single app module, minimum API 29, and a mock-data-first repository architecture. Add DataStore for preferences. Keep the app usable offline after installation.

## Design priority

A gorgeous, distinctive editorial experience is a first-milestone requirement. Follow `docs/DESIGN_DIRECTION.md`: magazine composition, expressive type, bundled imagery, original CrossLens perspective motifs, and subtle flip-inspired motion. The owner's Flipboard reference is inspiration for qualities, not permission to reproduce its identity or exact UI. Customize Material 3; do not deliver generic placeholder screens. Implement local saved stories, continue reading, a finite mock edition, and reduced motion. Complete the visual acceptance gate as well as functional checks.

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

## Quality gates and audits

Execute the runbook in order and maintain `docs/BUILD_STATUS.md` and `docs/QUALITY_REPORT.md` during implementation. Perform explicit architecture, editorial/data integrity, design, accessibility, security/privacy, dependency, and performance/offline review passes. Record evidence, fix blocking findings, and rerun affected checks. A missing device or tool is a blocked check, not a pass; continue independent work but keep milestone readiness incomplete until mandatory gates pass.

## Completion

Use the build guide's acceptance checklist and verification commands. Summarize the implemented behavior, test results, remaining limitations, and APK location. Do not extend the milestone into live services or deployment.
