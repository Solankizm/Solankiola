#!/usr/bin/env bash
# Build the Solankizm debug APK. Run from anywhere.
set -euo pipefail

export JAVA_HOME=/usr/local/opt/openjdk@21
export ANDROID_HOME=/usr/local/share/android-commandlinetools
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

cd "$(dirname "$0")"
./gradlew :app:assembleDebug --no-daemon "$@"

echo
echo "APK -> app/build/outputs/apk/debug/app-debug.apk"
