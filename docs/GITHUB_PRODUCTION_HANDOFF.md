# GitHub handoff

The requested branch is `feature/android-production-readiness`. Source/doc changes are in the shared workspace. Original `.git` is read-only under this environment's filesystem policy; commits and branch live in copied metadata at `/tmp/crosslens-production-git` with this workspace as work tree. Original checkout HEAD remains main. Unrelated untracked crosslens-codex.log and production-prompt.md were untouched.

Local gh CLI authentication is invalid. The GitHub connector can read the repository and retrieved the official wrapper, but its create-branch request was rejected: `MCP tool call requires approval, but approval policy is never`. No remote writes, PR or CI execution occurred. No alternate mutation path was used to bypass the rejection.

Remote main was inspected read-only: `1f80ecb6202f13901ec75ecaceda477c5388d2a2`, one commit beyond local base `b186fae9f835b7db300c090c828b49df57523e62`. That commit adds the owner's Production_Readiness.md only. It was not overwritten or removed. Integrate current main before opening the PR.

## Portable branch

`artifacts/crosslens-android-production-readiness.bundle` contains the incremental feature commits with the original main commit as prerequisite. `artifacts/crosslens-android-production-readiness.patch` is a second review/import option; `artifacts/SHA256SUMS` records integrity. These are Git handoff artifacts, NOT APKs or AABs, and are intentionally untracked.

On an authenticated, writable checkout that contains the base history:

```sh
# Use the actual absolute location of the delivered bundle.
git fetch /ABSOLUTE/PATH/crosslens-android-production-readiness.bundle refs/heads/feature/android-production-readiness:refs/heads/feature/android-production-readiness
git switch feature/android-production-readiness
git fetch origin main
git merge origin/main
# Run the complete build/test/lint/device gates before requesting review.
git push -u origin feature/android-production-readiness
gh pr create --draft --base main --head feature/android-production-readiness --title 'Harden Android persistence, news ingestion and release verification' --body-file docs/PRODUCTION_PR_DESCRIPTION.md
```

If local changes prevent switching, preserve them and use a separate clean clone. Do not force a branch update or overwrite unrelated files. Do not apply both bundle and patch. The bundle is preferred because it preserves logical commits. Merge only after review; publication remains an owner action.
