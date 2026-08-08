# GitHub → GitLab → F-Droid release runbook

## Status vocabulary

Keep these states distinct in all release updates:

1. **Repository prepared**: release files and local checks are ready.
2. **GitHub release published**: an upstream GitHub release has been created.
3. **GitLab mirrored**: GitHub main/tag hashes match the public GitLab mirror.
4. **F-Droid MR submitted**: a metadata merge request exists; this is not publication.
5. **F-Droid CI passed**: the official pipeline completed successfully.
6. **Merged / indexed / installable**: only after F-Droid merges, builds, indexes, and serves the app can it be described as available in F-Droid.

This repository is currently only being prepared. No F-Droid recipe, merge request, CI result, or indexed release is implied by this document.

## One-way repository model

GitHub is the only primary repository: https://github.com/zengtao227/health-tracker-app

GitLab is a public, one-way mirror and F-Droid source endpoint: https://gitlab.com/zengtao227/health-tracker-app

Never develop directly in the mirror, reverse-sync it, or force-push either release ref. Investigate and back up any divergence before acting.

## Prepare the release

1. Confirm `git status` and preserve unrelated work.
2. Confirm the intended version is `1.1.4` with versionCode `3`; never reuse a versionCode.
3. Run the local scanner and reproducible-source build checks before any CI submission. A successful CI run is not a substitute for local validation.
4. Run `./gradlew build` and `./gradlew test` using JDK 21.
5. Build the unsigned release with `./gradlew assembleRelease`. Keep keystores, passwords, and any signed upstream artifact outside the repository. The tag release workflow signs only after this source build.
6. Review `git diff --check` and commit the exact release material to GitHub `main`.

## Configure and verify the mirror

The GitHub Actions workflow expects these repository secrets:

- `GITLAB_USERNAME`
- `GITLAB_TOKEN` with minimum required GitLab write access
- `GITLAB_REPO` in `namespace/project` form

The workflow has separate ordinary pushes for `main` and tags, uses complete history, and never uses force push. Do not print or copy tokens into logs.

After a main push, verify both hashes:

```bash
git ls-remote https://github.com/zengtao227/health-tracker-app.git refs/heads/main
git ls-remote https://gitlab.com/zengtao227/health-tracker-app.git refs/heads/main
```

They must match before tagging.

## Tag and mirror

Create an annotated, immutable release tag only after all release files are committed and `main` is verified in GitLab. Use the project convention `v1.1.4+3`.

```bash
git tag -a v1.1.4+3 -m 'Release 1.1.4 (3)'
git push origin v1.1.4+3
```

Do not move, delete, or reuse this tag. Verify both the tag object and peeled source commit after the mirror workflow completes:

```bash
git ls-remote https://github.com/zengtao227/health-tracker-app.git 'refs/tags/v1.1.4+3*'
git ls-remote https://gitlab.com/zengtao227/health-tracker-app.git 'refs/tags/v1.1.4+3*'
```

## F-Droid submission prerequisites

Before creating a recipe, fetch and read the current official Gradle template from fdroiddata. Build metadata must reference an immutable, full 40-character source commit, not a branch or movable tag. Run local `fdroid lint`, `fdroid rewritemeta`, scanner, and a clean source build before opening an F-Droid merge request.

The repository includes an independently reviewed 512×512 icon and three privacy-safe phone screenshots containing only synthetic data. Re-review every image before each submission and never replace these with placeholders. A recipe is deliberately outside this preparation change and must be based on the verified build.

## Reproducibility and signing

Reproducible builds are enabled for this app. The release workflow expects `PHR_RELEASE_KEYSTORE_BASE64` and `PHR_RELEASE_KEYSTORE_PASSWORD` as protected GitHub secrets. It builds an unsigned APK from the tagged source, aligns it with Android Build Tools 34.0.0, signs it, verifies the certificate, and publishes `PersonalHealthRecords-<versionName>-<versionCode>.apk`.

Never commit a keystore, token, password, signed APK, or personal health database. F-Droid must rebuild the same source and verify that its unsigned contents match the upstream APK before distributing the upstream signature. Back up the release keystore separately; losing it permanently breaks the signing lineage.
