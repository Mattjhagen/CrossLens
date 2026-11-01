# Static verification evidence

Performed 2026-11-01. These checks do not compile Kotlin or prove runtime Android behavior.

- Android XML: all checked XML parsed successfully (including manifest, strings and backup/transfer rules).
- Version catalog TOML and GitHub Actions YAML parsed successfully.
- Python artifact-validator source parsed successfully.
- Nine historical migration SQL paths (1,2,3,4,5,6,8,9,10 → 11): SQLite table structure matched reconstructed schema 11 and a representative original article/excerpt was retained. Actual Room validation test is written but not run.
- Artifact validator: missing outputs rejected; synthetic APK/AAB ZIP entries accepted and SHA-256 ledger produced; corrupt ZIP rejected. Temporary synthetic fixtures removed; no real APK/AAB produced.
- Official Gradle 8.11.1 wrapper SHA-256 verified: 2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046. Distribution checksum pinned to f397b287023acdba1e9f6fc5ea72d22dd63669d59ed4a289a29b1a76eee151c6 from official reference.
- Current tracked text: no matches for private-key headers, AWS access-key IDs or GitHub-token patterns. Limited scan, not full history/advisory audit.
- Final source diff checked for whitespace errors; verification-log trailing blank lines corrected in a follow-up commit.
- No JDK/SDK/emulator/adb found in PATH or checked installation locations. JDK HTTP download probe failed DNS resolution.
- GitHub read-only access succeeded; write blocked by tool approval policy. CI unexecuted.
