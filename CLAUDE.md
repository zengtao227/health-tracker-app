# Project release facts

- Application ID: `com.taotao.healthtracker`
- Store name: Personal Health Records
- Release target: versionName `1.1.5`, versionCode `4`
- License: GPL-3.0-or-later, Copyright © 2026 Zeng Tao
- Primary repository: https://github.com/zengtao227/health-tracker-app
- Public mirror: https://gitlab.com/zengtao227/health-tracker-app

## Release conventions

- GitHub is the sole source of truth. GitLab is a one-way public mirror; do not develop or push independent changes there.
- Build with JDK 21 through `./gradlew`; run `./gradlew build` and `./gradlew test` before a release.
- Public source builds must remain unsigned. The tag release workflow signs only after the source build, using protected secrets; keep signing keys, passwords, and signed artifacts outside Git.
- Create a tag only after the exact release commit is on GitHub main and the mirror has been verified. Published tags and versionCodes are immutable and must never be reused.
- Health records are sensitive local data. Never commit, upload, or inspect `*.db` files as part of release work.
