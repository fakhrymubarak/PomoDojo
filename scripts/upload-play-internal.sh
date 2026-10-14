#!/usr/bin/env bash
# Build the prod release AAB and upload it to the Google Play internal testing track,
# then bump versionCode and commit — Play rejects a versionCode it has already seen.
#
# Requires (both gitignored):
#   play-publish-account.json  Play Console service account, at the repo root
#   local.properties           signing_store_file / signing_store_password /
#                              signing_key_alias / signing_key_password
#
# Usage: scripts/upload-play-internal.sh
set -euo pipefail

cd "$(dirname "$0")/.."
GRADLE_FILE=composeApp/build.gradle.kts

if [[ ! -f play-publish-account.json ]]; then
  echo "❌ play-publish-account.json not found at the repo root."
  exit 1
fi
# Without these, release falls back to debug signing and Play rejects the bundle.
if ! grep -q '^signing_store_file=' local.properties 2>/dev/null && [[ -z "${signing_store_file:-}" ]]; then
  echo "❌ Release signing not configured (signing_* in local.properties)."
  exit 1
fi

VERSION_CODE=$(sed -nE 's/^ *versionCode = ([0-9]+)$/\1/p' "$GRADLE_FILE")
echo "Uploading com.fakhry.pomodojo versionCode $VERSION_CODE to internal…"
./gradlew :composeApp:publishProdReleaseBundle

NEW_CODE=$((VERSION_CODE + 1))
sed -E -i '' "s/^( *versionCode = )$VERSION_CODE$/\1$NEW_CODE/" "$GRADLE_FILE"
git commit -m "chore: bump versionCode to $NEW_CODE after Play internal upload" -- "$GRADLE_FILE"
echo "✅ Uploaded versionCode $VERSION_CODE; bumped to $NEW_CODE."
