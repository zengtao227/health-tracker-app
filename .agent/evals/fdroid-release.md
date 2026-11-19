# Task Eval: F-Droid release

## Goal
- Publish Personal Health Records 1.1.4 (versionCode 3) from a public GitLab mirror through the official F-Droid inclusion workflow, with reproducible upstream signing and truthful status reporting.

## Acceptance Criteria
- [x] Planning/review uses Sol high; routine implementation defaults to Terra medium and complex debugging uses Terra high.
- [x] The Android source builds with JDK 21, Gradle 8.5, and the currently configured Android toolchain.
- [x] Release signing is external to Gradle and no keystore, password, token, signed APK, or database is tracked.
- [x] GPL-3.0-or-later license, README, release workflow, mirror workflow, and Fastlane text/icon exist.
- [x] Two to four reviewed, synthetic-data phone screenshots exist in Fastlane metadata.
- [x] Final tests, lint, unsigned APK metadata, and signing state pass after all code changes.
- [x] The GitLab source mirror is public at the user-confirmed namespace and matches GitHub main/tag source commits.
- [x] GitHub Actions secrets for release signing and GitLab mirroring are configured; the active values were not printed or committed.
- [x] A clean immutable source export has zero F-Droid scanner errors and builds in the current F-Droid buildserver environment.
- [x] The upstream tag and signed GitHub release APK are published exactly once.
- [x] The fdroiddata recipe passes lint, idempotent rewritemeta, scanner, local build, and reproducibility verification.
- [x] An official App Inclusion MR is submitted from a public, unprotected branch using the complete current template.
- [ ] Publication status is reported as installable only after F-Droid merges, builds, indexes, and serves the app.

## Verification
- Command: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew --no-daemon clean test assembleRelease :app:lint --console=plain`
- Expected: build succeeds; all debug/release tests pass; lint has no fatal/error; release APK is unsigned and has app ID `com.taotao.healthtracker`, versionName `1.1.4`, versionCode `3`.
- Command: `fdroid lint com.taotao.healthtracker && fdroid rewritemeta com.taotao.healthtracker && fdroid rewritemeta com.taotao.healthtracker`
- Expected: lint passes and the second rewrite produces no diff.
- Command: `fdroid scanner --exit-code com.taotao.healthtracker:3`
- Expected: zero scanner errors after recipe removals.
- Command: current F-Droid buildserver image running `fdroid build --verbose --test --on-server --no-tarball com.taotao.healthtracker:3`
- Expected: APK is produced at the recipe-declared output path.
- Command: `git ls-remote` against GitHub and GitLab main/tag refs.
- Expected: source commit hashes match; annotated tag objects peel to the same commit.

## Manual Checks
- [x] Fastlane icon is a real 512×512 PNG and was visually reviewed.
- [x] Every phone screenshot contains only synthetic data and has no keyboard, notification, crash, or private information.
- [x] Store description states local storage, no app Internet access, cloud-backup exclusion, direct device transfer behavior, and the medical disclaimer.
- [x] Release keystore has a user-confirmed independent encrypted backup.
- [ ] F-Droid MR checklist claims only checks that have actually passed.

## Result
- Status: PARTIAL
- Evidence:
  - JDK 21 `clean test assembleRelease :app:lint` passed after CSV import hardening: 18 tests, 0 failures/errors, 0 lint Fatal/Error.
  - fdroidserver 2.4.3 clean scanner of immutable commit `19ba5f0d5f2b0c434219439e4214447cd47f848c` passed with 0 errors/problems; all six recipe removals and the standard wrapper removals were applied.
  - Recipe lint passes and two consecutive `rewritemeta` runs are idempotent (SHA-256 `3e1fc893b0a012e59a1bd2577a1872c48e6db3c36e27ee9a970dfb6f5fd239b2`).
  - Official buildserver-trixie image `sha256:9bae53bb4ddbf8fa5bb7385bf2e62e7c6318f99ab0d25b2a551ad38abb528068` built versionCode 3 successfully from the immutable commit with OpenJDK 21.0.11 and Gradle 8.5: 44 tasks, `BUILD SUCCESSFUL in 10m 25s`, fdroid exit 0, 1 build succeeded.
  - Password-protected PKCS#12 backup at the owner's private backup folder (outside this repo) is mode 0600, byte-identical to the source (SHA-256 `1459faf0b4815adb5afe259a2ad9c25a7acb60eb1b2ca1e2c3a8330944581fe4`), and contains the expected alias/certificate.
  - GitHub Release `v1.1.4+3` is public, not draft/prerelease. Asset `PersonalHealthRecords-1.1.4-3.apk` is 14,225,414 bytes, SHA-256 `1bee919870f5f88c99d9a42c976db3333865ed3b8c615c43b3b79653610b2f51`, and has the expected signer.
  - Full post-release F-Droid reproducibility verification passed: build exit 0, binary comparison successful, allowed signer matched. Evidence directory `/private/tmp/phr-fdroid-repro-N1unnr`.
  - Local APK signing verified certificate SHA-256 `798d088a15e4361da84f6245d8dd67d49e6c50320018f725368c995123f39348`.
  - GitHub release and GitLab mirror secrets are configured. The first GitLab token appeared in an automation snapshot, so it was immediately rotated; only the unexposed replacement is active and the clipboard was cleared.
  - The personal GitLab project `zengtao227/health-tracker-app` is public and configured as the mirror target.
  - Mirror workflow run `31257653012` passed; GitHub and GitLab `main` both resolve to `19ba5f0d5f2b0c434219439e4214447cd47f848c`.
  - Current stable local F-Droid environment is fdroidserver 2.4.3 with fdroiddata `c23004a98740e157f21ff2db95ad5ebe861d4588`.
- Remaining risks:
  - GitLab represents the release tag as lightweight while GitHub uses an annotated tag; both resolve to the same immutable source commit and the recipe pins the full commit.
  - Official App Inclusion MR opened 2026-08-08: https://gitlab.com/fdroid/fdroiddata/-/merge_requests/45196, from `zengtao227/fdroiddata:new-app-personal-health-records` (sha `3406535fb197265337c26811e33cd959681fde5e`) into `fdroid/fdroiddata:master`. Verified independently via GitLab API before creation: fork pipeline `2743654518` on the same sha was 9/9 job success, fork is public, source branch unprotected, remote raw metadata byte-identical to local. Title/description/draft-state verified in-browser before clicking Create. The MR-description `/label ~"New App"` quick action was recognized (stripped from saved description) but the label did not end up applied, and this account has no maintainer access on the upstream project to add it manually — left for maintainer, non-blocking.
  - MR pipeline `2743693540` (fork project 85097881, sha `3406535f...`) reached terminal state 2026-08-08: 9/9 jobs success (check apk, check source code, schema validation, tools check scripts, fdroid rewritemeta, fdroid lint, git redirect, checkupdates, fdroid build). Raw `fdroid build` job log independently confirmed `BUILD SUCCESSFUL` and a successful reproducibility comparison. Independently re-verified via GitLab API (not just trusting the subagent's report): MR `state: opened`, `merge_status: mergeable`, still not merged.
  - Credential cleanup done and independently re-verified: `fdroiddata-submit` project token revoked (subagent screenshot evidence: active tokens 1→0, moved to Inactive/Revoked); one-time token file and askpass helper both confirmed absent via `ls`; macOS clipboard confirmed empty (`pbpaste | wc -c` = 0). Browser virtual clipboard clear was attempted (`navigator.clipboard.writeText('')`) but not independently confirmable — treat as best-effort only.
  - MR merge and F-Droid build/index are still outstanding and outside this task's control (maintainer review, then F-Droid's own build server).

## Update 2026-08-09: v1.1.5 (versionCode 4) — responded to reviewer feedback
- Reviewer `chivalry` (@diligentowldroid) asked to remove `ACCESS_NETWORK_STATE` and localize "Zodiac Insight"/"1Y"/"MAX". Root-caused both: an unused `NetworkType.CONNECTED` constraint on the local-only `AlmanacWorker` job pulled in the permission; those three strings bypassed the app's existing `L10n.kt` dictionary. Deliberately did NOT touch the `insLang=="zh"` branch's Chinese almanac content strings — confirmed via code reading that `insightLanguage` is a separate, intentional toggle from the general `language` field ("Culture decoupled Insight Card"), so rerouting those through `appLang` would have introduced a real content/label mismatch bug.
- Local verification before pushing: JDK21 `clean test assembleRelease :app:lint` (18 tests PASS, lint clean), `aapt2 dump permissions` confirmed `ACCESS_NETWORK_STATE` gone from the built APK, `apksigner verify` confirmed still unsigned.
- Shipped: commit `386bb853cef9e6409d88f5cbc7293191833ffc2b` on GitHub/GitLab main (hash-verified equal both sides), tag `v1.1.5+4` (annotated, peeled commit verified equal both sides), GitHub Release published and downloaded independently — signer cert SHA-256 `798d088a15e4361da84f6245d8dd67d49e6c50320018f725368c995123f39348` matches `AllowedAPKSigningKeys`, and `aapt2`/`apksigner` re-run directly on the downloaded CI-built artifact (not just the local build) confirm versionCode 4/versionName 1.1.5 and the permission's absence.
- Bonus fix: `.github/workflows/release.yml` had `changelogs/3.txt` hardcoded as the release notes source; fixed to derive from actual versionCode, and corrected the already-published v1.1.5+4 release notes via `gh release edit`.
- fdroiddata fork branch updated in place (single Builds entry, pre-merge so no historical second entry): pushed `09ef2ba2e5`, then a second commit `9407469a95` after the fork's real `fdroid rewritemeta` CI job showed it wanted `AllowedAPKSigningKeys` back on one line (opposite of what my local fdroidserver-master venv produced despite matching the claimed git revision — treated real CI's demonstrated output as authoritative rather than my local tool). Local precheck before both pushes: schema validation via `check-jsonschema` (CI's exact command), `rewritemeta` idempotency (2x, empty diff), `fdroid lint` (with real fdroiddata `config/categories.yml` in place — plain `fdroid lint` alone false-positives on Categories per lessons-learned #9), `checkupdates --auto` simulation (zero diff vs. manual edit — confirms the new tag pattern resolves correctly), scanner.scan_source() real destructive run (count=0), and a JDK21-only Gradle build from the exact recipe subdir post-scan (BUILD SUCCESSFUL).
- Real fork pipeline `2745184114` for `9407469a95`: 9/9 jobs green, checked individually. `fdroid build` job's raw log: `Successfully built com.taotao.healthtracker:4 from 386bb853...`, `compared built binary to supplied reference binary successfully`, reference signer matches.
- Posted an update comment on MR !45196 addressing both reviewer points by name, with the new version/commit/pipeline link. Did not touch MR title/description/labels.
- Not done / explicitly not claimed: MR is still unmerged (`waiting-on-response` label untouched by us, left for maintainer); F-Droid's own production build/index has not run yet; "installable from F-Droid" is not yet true.
