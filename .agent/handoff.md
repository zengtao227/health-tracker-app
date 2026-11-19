# Handoff

## Completed
- Shipped v1.1.5 (versionCode 4) responding to MR !45196 review feedback: removed the `ACCESS_NETWORK_STATE` permission and localized "Zodiac Insight"/1Y/MAX into `L10n.kt`. **MR !45196 is MERGED** (linsui, squash commit `9b4ecf6f` on `fdroid/fdroiddata:master`, based on versionCode 4/commit `386bb853...`).
- Shipped v1.1.6 (versionCode 5): renamed the almanac fallback card's zh title from "运势黄历" to "星座洞察" to avoid visually clashing with the "黄历" toggle button (found via on-device screenshot). This landed on GitHub/GitLab *after* the MR had already merged, so this commit sits unreferenced on the fdroiddata fork branch `new-app-personal-health-records` (`15016983a7`) — not part of any MR, harmless, left alone.
- **User feedback (important, read before touching wording again):** "星座洞察"/"Zodiac Insight" was itself a bad name — the card only ever shows Chinese lunisolar/ganzhi data (`almanac.lunar`), never real Western zodiac content. `getHoroscopeInsight()` was dead code (never called from any screen); the Settings "Source & Algorithm" card falsely claimed the app computes a Horoscope. User decided: rename the card to "Almanac Brief"/"简明干支", remove the false Horoscope disclosure line, delete `getHoroscopeInsight()` — but explicitly **keep** the English date subtitle's "Leo Season" zodiac-season label (and `getZodiac()`), since a season name isn't a horoscope/fortune-telling claim.
- Shipped v1.1.7 (versionCode 6) implementing the above, fully tested locally (build/test/aapt2/apksigner) and confirmed on-device via emulator screenshots for every touched string, *before* pushing anything, per explicit user instruction not to push half-verified/half-decided content again.
- Verified via local `fdroid checkupdates --auto` (starting from the real merged master state) that F-Droid's own automated tooling will pick up tag `v1.1.7+6` and auto-append a versionCode-6 `Builds:` entry (skipping the orphaned versionCode 5) without any manual MR — evidence, not assumption.

## Current State
- GitHub main = GitLab main = `4e014f41e994b931c286ebf1bc81a79c9933a73d`.
- Tag `v1.1.7+6` published and identical (peeled commit) on GitHub and GitLab.
- GitHub Release `v1.1.7+6` public, asset `PersonalHealthRecords-1.1.7-6.apk`, signer SHA-256 `798d088a15e4361da84f6245d8dd67d49e6c50320018f725368c995123f39348` (same key as all prior releases), `ACCESS_NETWORK_STATE` confirmed absent on the actual downloaded CI-signed APK.
- fdroiddata: did **not** touch the fork branch this round — deliberately, since checkupdates evidence shows F-Droid's own bot will auto-discover this tag. The orphaned `15016983a7` (versionCode 5) commit on `new-app-personal-health-records` remains, unreferenced, harmless.

## Next Steps
- Nothing pending from us. Watch (next time this project comes up) whether F-Droid's build server has picked up versionCode 6 — check `https://gitlab.com/fdroid/fdroiddata/-/raw/master/metadata/com.taotao.healthtracker.yml` for `CurrentVersionCode: 6`.
- If it hasn't shown up after a reasonable wait and the user wants to accelerate, only then consider a fresh "App update" MR — start from current master (not the orphaned fork branch), append (don't edit in place).

## Key Decisions
- This app deliberately implements **only** the Chinese lunisolar almanac. No Western horoscope/fortune-telling feature exists or is planned — any future string/code implying otherwise is a bug, not a feature gap. A plain calendar "season" label (e.g. "Leo Season") is fine; anything implying a personality/fortune reading is not.
- `insightLanguage` (almanac card content language) and `language` (general app UI language) remain intentionally independent toggles.
- Process lesson from this session: content/wording decisions (unlike build/lint verification) require explicit user confirmation of the *exact* final text before implementation — get the diff shown and confirmed, then test, then push, in that order, every time. Don't cut a second release on your own judgment mid-task without checking in first.
