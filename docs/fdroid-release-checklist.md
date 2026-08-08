# F-Droid release checklist

## Repository preparation

- [x] GitHub remains the sole primary repository; GitLab is a one-way mirror.
- [x] `LICENSE`, README, release notes, and Fastlane text are reviewed.
- [x] The release uses an unused versionCode (`3`) and versionName (`1.1.4`).
- [x] `git status` is understood and unrelated changes are preserved.
- [x] `git diff --check` passes.

## Local validation before CI

- [x] Use JDK 21 and run the complete release build.
- [x] Run `./gradlew test` (18 tests, 0 failures/errors).
- [x] Produce and inspect an unsigned release with `./gradlew assembleRelease`.
- [ ] Run the F-Droid scanner against a clean source checkout before CI.
- [ ] Run `fdroid lint` and `fdroid rewritemeta` in a temporary fdroiddata checkout; confirm rewriting is idempotent.
- [ ] Verify the clean source build path. Local scanner/build evidence comes before a CI or MR submission.

## Signing and reproducibility

- [x] No keystore, password, token, signed APK, or `*.db` file is staged.
- [x] Signing keys remain outside the repository.
- [x] `PHR_RELEASE_KEYSTORE_BASE64` and `PHR_RELEASE_KEYSTORE_PASSWORD` are protected GitHub secrets.
- [ ] The release keystore has an independent encrypted backup.
- [ ] Any upstream APK is signed only after the repository build has produced an unsigned artifact.
- [x] Reproducible builds are selected for this app.

## GitHub and GitLab mirror

- [x] Configure `GITLAB_USERNAME`, `GITLAB_TOKEN`, and `GITLAB_REPO` as GitHub secrets.
- [ ] Push the release commit to GitHub `main` and verify the mirror action.
- [ ] Compare GitHub and GitLab `main` hashes.
- [ ] Create annotated tag `v1.1.4+3` only after main matches.
- [ ] Push the tag once; tags are immutable and must never be moved, deleted, or reused.
- [ ] Compare tag object and peeled commit on GitHub and GitLab.

## F-Droid submission

- [x] Read the current official Gradle build template before writing metadata.
- [ ] Create a recipe only from a verified build, using a full 40-character immutable commit.
- [x] Add a real 512×512 app icon and 2–4 reviewed, privacy-safe screenshots; do not use placeholders.
- [ ] Use the official F-Droid App Inclusion MR template and state pipeline results truthfully.
- [ ] Report status precisely: prepared, submitted, CI passed, merged, indexed, or installable are different states.
