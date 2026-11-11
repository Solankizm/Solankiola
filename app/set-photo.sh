#!/usr/bin/env bash
# Set your Solankizm display picture by pushing a photo from the Mac to the
# scooter over ADB. Works on the debug build via run-as (no root, no storage
# permission). The app shows it next time you open the dashboard.
#
#   ./set-photo.sh <scooter-ip>[:5555] /path/to/your-photo.jpg
set -euo pipefail

export ANDROID_HOME=/usr/local/share/android-commandlinetools
ADB="$ANDROID_HOME/platform-tools/adb"
PKG="com.solankizm.ola"

if [ $# -lt 2 ]; then
  echo "usage: ./set-photo.sh <scooter-ip>[:5555] <photo.jpg>"
  exit 1
fi

TARGET="$1"; [[ "$TARGET" == *:* ]] || TARGET="$TARGET:5555"
PHOTO="$2"
[ -f "$PHOTO" ] || { echo "No such file: $PHOTO"; exit 1; }

"$ADB" connect "$TARGET" >/dev/null
"$ADB" -s "$TARGET" push "$PHOTO" /data/local/tmp/solankizm_profile.jpg
"$ADB" -s "$TARGET" shell "run-as $PKG sh -c 'cp /data/local/tmp/solankizm_profile.jpg files/profile.jpg'"
"$ADB" -s "$TARGET" shell "rm -f /data/local/tmp/solankizm_profile.jpg"
echo "Done. Open Solankizm on the scooter — your photo is on the dashboard."
