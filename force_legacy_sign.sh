#!/bin/bash
set -e

echo "🚀 Starting Legacy Signing Workflow..."

# Paths
SDK_BUILD_TOOLS="$HOME/Library/Android/sdk/build-tools/33.0.1"
ZIPALIGN="$SDK_BUILD_TOOLS/zipalign"
APKSIGNER="$SDK_BUILD_TOOLS/apksigner"
KEYSTORE="release.keystore"

# 1. Build Standard Release (Unsigned or Debug-signed)
echo "🏗️  Building Raw APK (minSdk 26)..."
./gradlew clean assembleRelease

ORIGIN_APK="app/build/outputs/apk/release/app-release.apk"
V1_SIGNED_APK="app/build/outputs/apk/release/app-v1-signed.apk"
FINAL_APK="release/HealthTracker.apk"

if [ ! -f "$ORIGIN_APK" ]; then
    echo "❌ APK build failed."
    exit 1
fi

# 2. Force V1 Sign using jarsigner (The Old Way)
# Note: Zipalign MUST happen AFTER jarsigner
echo "✍️  Signing with jarsigner (V1)..."
cp "$ORIGIN_APK" "$V1_SIGNED_APK"
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
    -keystore "$KEYSTORE" \
    -storepass password \
    -keypass password \
    "$V1_SIGNED_APK" my-key-alias > /dev/null

# 3. Zipalign (Corrects the offsets after jarsigner)
echo "📏 Zipaligning..."
rm -f "$FINAL_APK"
"$ZIPALIGN" -v -p 4 "$V1_SIGNED_APK" "$FINAL_APK" > /dev/null

# 4. Append V2 Signature (Optional but recommended, using apksigner)
# Important: verify we don't STRIP V1
echo "✍️  Appending V2 Signature..."
"$APKSIGNER" sign --ks "$KEYSTORE" \
    --ks-pass pass:password \
    --ks-key-alias my-key-alias \
    --key-pass pass:password \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    "$FINAL_APK"

# 5. Verification
echo "🔍 Verify Signatures:"
"$APKSIGNER" verify --verbose "$FINAL_APK" | grep "Verified using"

# 6. Release
TAG_NAME="v$(date +%Y%m%d%H%M)-legacy"
git add .
git commit -m "build: force legacy jarsigner flow" || true
git push origin main

gh release create "$TAG_NAME" "$FINAL_APK" --title "Legacy Signed $TAG_NAME" --notes "Signed using jarsigner+zipalign+apksigner for max compatibility."

echo "🎉 DONE! Verify if V1 is true now."
