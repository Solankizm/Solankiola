#!/usr/bin/env bash
# Install the built APK onto the scooter head unit over ADB.
# PREREQ: the scooter is on YOUR phone hotspot and ADB is authorised
#         (use S1Mods once, or `adb connect <scooter-ip>:5555`).
set -euo pipefail

export ANDROID_HOME=/usr/local/share/android-commandlinetools
ADB="$ANDROID_HOME/platform-tools/adb"
APK="$(dirname "$0")/app/build/outputs/apk/debug/app-debug.apk"

if [ $# -lt 1 ]; then
  echo "usage: ./install.sh <scooter-ip>[:5555]"
  echo "       (find the IP in your phone's Hotspot > Connected Devices)"
  exit 1
fi

TARGET="$1"; [[ "$TARGET" == *:* ]] || TARGET="$TARGET:5555"

"$ADB" connect "$TARGET"
"$ADB" -s "$TARGET" install -r "$APK"
echo "Installed. Launch 'Solankizm' from the scooter app drawer (Balls nav drawer)."
