# Personal Health Records

Personal Health Records is a local-first Android app for recording and reviewing personal health measurements. It supports blood pressure, heart rate, weight, blood glucose, and uric acid entries, with trend views and CSV/Excel import and export.

The app is available in Chinese and English. It also includes traditional calendar and zodiac references for personal interest; these references are not health guidance.

## Important notice

This app is a personal record-keeping tool, not a medical device. It does not provide medical diagnosis, treatment, or clinical advice. Do not use it as a substitute for a qualified healthcare professional.

## Privacy

Health records are stored locally on your device. No account is required. The Android manifest does not request the `INTERNET` permission, and the project has no analytics, advertising, or tracking components. Health data is excluded from Android cloud backup; Android's direct device-to-device transfer may migrate the app database and preferences to a new device.

You are responsible for protecting device access and for reviewing exported files before sharing them.

## Build from source

Prerequisites: JDK 21 and an Android SDK compatible with the project configuration.

```bash
./gradlew build
./gradlew test
```

To create an unsigned release APK for verification or F-Droid preparation:

```bash
./gradlew assembleRelease
```

The public repository intentionally contains no signing key. Upstream release APKs are signed after the source build with credentials supplied through protected GitHub secrets. For reproducible builds, F-Droid rebuilds the same source, verifies that the unsigned contents match the upstream APK, and then distributes the verified upstream signature.

## Contributing and support

Contributions are welcome. Please open an [issue](https://github.com/zengtao227/health-tracker-app/issues) before substantial changes, and submit pull requests against the GitHub repository.

## Source repositories

[GitHub](https://github.com/zengtao227/health-tracker-app) is the only primary development repository. The public [GitLab mirror](https://gitlab.com/zengtao227/health-tracker-app) is read-only for development and exists solely as a source mirror and F-Droid entry point.

## License

Copyright © 2026 Zeng Tao. Licensed under [GPL-3.0-or-later](LICENSE).
