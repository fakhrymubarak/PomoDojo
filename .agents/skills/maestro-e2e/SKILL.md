---
name: maestro-e2e
description: Runs the Maestro end-to-end flows in .maestro/ on an Android emulator and/or iOS simulator. Use when asked to e2e-test the app, run Maestro, or add a Maestro flow or test tag.
---

The flows in `.maestro/` drive a real **debug** build through a whole Session. They tap Compose `testTag`s, which Android exposes as `resource-id` (via `testTagsAsResourceId` in `MainActivity`) and iOS exposes as accessibility identifiers. The Session runs on 1-minute Segments, and the `focus_option_1` chip exists only in debug builds (`isDebugBuild` in `core:utils`), so a release or prod build fails on its first tap.

## Run

Run both platforms unless the request names one. Each flow takes about 5 minutes, so run `maestro test` with a 10-minute timeout or in the background. Write output to a temp dir with `--test-output-dir`, which also holds the screenshot of a failing step.

### Android

1. Check for a device with `adb devices`. If none is listed, boot one with `emulator -list-avds`, then `emulator -avd <name> -no-snapshot-save &`. Wait until `adb shell getprop sys.boot_completed` prints `1`.
2. Build and install the dev debug APK:
   `rtk ./gradlew :composeApp:assembleDevDebug && adb install -r composeApp/build/outputs/apk/dev/debug/composeApp-dev-debug.apk`
3. Run `maestro test -e APP_ID=com.fakhry.pomodojo.dev --test-output-dir <tmp>/android .maestro/`

### iOS

1. Boot an iPhone simulator with `xcrun simctl boot "iPhone 17"`, then `open -a Simulator`. Take its UDID from `xcrun simctl list devices booted`.
2. Build the Debug app and install it:
   `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,id=<udid>' -derivedDataPath <tmp>/dd CODE_SIGNING_ALLOWED=NO build`
   `xcrun simctl install <udid> <tmp>/dd/Build/Products/Debug-iphonesimulator/PomoDojo.app`
3. Run `maestro --device <udid> test -e APP_ID=com.fakhry.pomodojo.PomoDojo --test-output-dir <tmp>/ios .maestro/`

When an emulator and a simulator are both running, `--device` decides where Maestro runs. Pass it on every run.

**Done** when every flow on every requested platform has a result. For each platform, report pass or fail. For a failure, give the failing command, the tag it was waiting for, and the screenshot path.

## Adding tags and flows

- Tag with `Modifier.testTag("<screen>_<element>")`, e.g. `session_skip_button`. Option chips get `<group>_<value>` automatically from `PreferenceOptionsCompose(testTag = …)`, e.g. `focus_option_25`. A button that changes meaning with state gets one tag per state (`session_pause_button` / `session_resume_button`), so flows assert state through the tag alone.
- Every `DialogCard` tags its buttons `dialog_confirm` / `dialog_dismiss`. Tell dialogs apart by the tag on the card (`session_gate`, `session_confirm`).
- Select elements by `id:`, never by visible text. The same flow then runs on both platforms and in every language.
- Each flow starts with `launchApp: {clearState: true, permissions: {all: allow}}`. That resets preferences to their defaults and keeps the notification permission prompt from blocking the Start button.
- The repeat-count wheel (`WheelNumbers`) has nothing tappable. Flows keep the default 4 cycles instead of swiping it.
- Wait out a running Segment with `extendedWaitUntil` and `timeout: 90000`. A 1-minute Segment plus the Gate needs more than Maestro's default wait.
