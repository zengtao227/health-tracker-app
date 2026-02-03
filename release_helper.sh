#!/bin/bash
set -e

echo "🚀 Starting HealthTracker Release Workflow..."

# 1. Build Single Release APK
echo "🏗️  Building Android Release APK..."
./gradlew clean assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK build failed. file not found: $APK_PATH"
    exit 1
fi

echo "✅ APK generated at $APK_PATH"

# 2. Check Zipalign
ZIPALIGN_TOOL=~/Library/Android/sdk/build-tools/33.0.1/zipalign
if [ -f "$ZIPALIGN_TOOL" ]; then
    echo "🔍 Verifying zipalign..."
    "$ZIPALIGN_TOOL" -c -v 4 "$APK_PATH" > /dev/null
    if [ $? -eq 0 ]; then
        echo "✅ APK is verified aligned."
    else
        echo "❌ APK alignment check failed! gradle should handle this."
        exit 1
    fi
else
    echo "⚠️  zipalign too not found at expected path, skipping explicit check."
fi

# 3. Create Release Tag
RELEASE_TAG="v$(date +%Y%m%d%H%M)"
git add .
git commit -m "build: release $RELEASE_TAG" || true
git push origin main

# 4. Upload to GitHub Release
echo "⬆️  Uploading to GitHub Releases..."
gh release create "$RELEASE_TAG" "$APK_PATH" --title "Release $RELEASE_TAG" --notes "Automated release via Claude Code."

echo "🎉 Release Complete!"
