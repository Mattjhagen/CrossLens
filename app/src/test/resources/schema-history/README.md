# Reconstructed historical schemas

These SQL fixtures are reconstructed from the Kotlin entities in Git history, NOT Room-generated JSON exports or databases recovered from actual installed APKs. SQLite affinity follows the existing Room converters (lists to JSON TEXT, Instant to INTEGER, enums to TEXT).

- Version 1: `fb82be6`
- Version 2: `e1c5032`
- Version 3: `c6004c0`
- Version 4: `b67c334`
- Version 5: `7275c32`
- Version 6: `99ae13d`
- Version 8: `38a9d3b`
- Version 9: `f156c13`
- Version 10: `66040de`
- Version 11: `dd928ef`

The history jumps from 6 to 8 in commit 38a9d3b. No version-7 schema is available. Do not guess it: obtain any actually distributed version-7 APK/database before supporting that path. Direct non-destructive migrations to 11 cover the evidenced versions. Robolectric tests must open each fixture using actual Room schema validation and preserve representative rows. These tests have not run in the dependency-restricted/JDK-less environment. Future Room schema JSON exports must be checked into app/schemas.
