#!/bin/bash
set -e

echo "🚀 Starting HealthTracker Manual Signing Workflow..."
SDK_BUILD_TOOLS="$HOME/Library/Android/sdk/build-tools/33.0.1"
ZIPALIGN="$SDK_BUILD_TOOLS/zipalign"
APKSIGNER="$SDK_BUILD_TOOLS/apksigner"
KEYSTORE="release.keystore"

# 1. Clean & Build Release APK (Let Gradle build it, we will resign it)
echo "🏗️  Building Raw APK..."
./gradlew clean assembleRelease

# Source APK path
ORIGIN_APK="app/build/outputs/apk/release/app-release.apk"
# Intermediate aligned APK
ALIGNED_APK="app/build/outputs/apk/release/app-release-aligned.apk"
# Final signed APK
FINAL_APK="release/HealthTracker.apk"

if [ ! -f "$ORIGIN_APK" ]; then
    echo "❌ APK build failed. file not found"
    exit 1
fi

# 2. Zipalign (Force alignment to new file)
echo "📏 Zipalign..."
rm -f "$ALIGNED_APK"
"$ZIPALIGN" -v -p 4 "$ORIGIN_APK" "$ALIGNED_APK" > /dev/null

# 3. Manual Signing (The Critical Fix)
echo "✍️  Manual Signing (Forcing V1 + V2)..."
"$APKSIGNER" sign --ks "$KEYSTORE" \
    --ks-pass pass:password \
    --ks-key-alias my-key-alias \
    --key-pass pass:password \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --out "$FINAL_APK" \
    "$ALIGNED_APK"

# 4. Verification
echo "🔍 Verifying Signature Schemes..."
"$APKSIGNER" verify --verbose "$FINAL_APK" | grep "Verified using"

# 5. Git & Release
TAG_NAME="v$(date +%Y%m%d%H%M)-fixed"
echo "🏷️  Releasing: $TAG_NAME"

git add .
git commit -m "fix: manual apksigner to enforce V1 signature" || true
git push origin main

gh release create "$TAG_NAME" "$FINAL_APK" --title "Fixed Release $TAG_NAME" --notes "Fixed 'File Incomplete' error by enforcing V1+V2 signatures manually."

echo "🎉 DONE! Please verify V1 scheme is true above."
