# PomoDojo Compose Multiplatform

## Features

- Shared Kotlin Multiplatform codebase powering Android, Desktop, and iOS entry points with Compose
  UI.
- Pomodoro-focused workflows: dashboard, active session controls, and persistence via Room/SQLite.
- Orbit MVI view models with Koin dependency injection for predictable state and thin platform
  layers.
- Centralized preferences, theming, and localization resources in `commonMain` for consistent UX.

## Setup

One-time steps to get a fresh clone building. Firebase config (`google-services.json` for each
flavor) and the environment files (`config/dev.props` / `config/prod.props`) are committed, so the
only file you must provide locally is `local.properties`.

### Prerequisites

- **JDK 21** – the Gradle build pins a JDK 21 toolchain; both Java and Kotlin compile to target 21.
- **Android SDK API 36** – `compileSdk`/`targetSdk = 36`, `minSdk = 24`.
- **Xcode 15+** – only required to build/run the iOS target.
- **Graphviz** (optional) – only to render the dependency-graph PNG (`brew install graphviz`).

### Steps

1. Clone the repo and open it in Android Studio, which generates `local.properties` automatically.
   To create it by hand, point it at your Android SDK:
   ```
   sdk.dir=/Users/<you>/Library/Android/sdk
   ```
2. Enable the local git hooks so commits/pushes run the same checks as CI (see
   [Git Hooks](#git-hooks)):
   ```
   git config core.hooksPath .githooks
   ```
3. Verify the toolchain and build:
   ```
   ./gradlew :composeApp:assembleDevDebug
   ```

### Build flavors

The app declares an `environment` dimension with `dev` and `prod` flavors, producing variants such
as `devDebug` and `prodRelease`. The `dev` flavor uses the `.dev` application-id suffix and a
separate database (`pomodojo_dev.db`), so it installs alongside a prod build.

## How to Build

```
./gradlew :composeApp:assembleDebug   # Android debug APK + KSP/Room schema verification
./gradlew :composeApp:run             # Desktop JVM app (Compose for Desktop)
./gradlew jacocoJvmTestReport  # Generates coverage in composeApp/build/reports
./gradlew ktlintCheck                 # Repository-wide Kotlin style checks
./gradlew ktlintFormat                # Auto-formats Kotlin sources (apply before committing)
./gradlew generateProjectDependencyGraph  # Writes a DOT graph of inter-module dependencies to build/reports/dependency-graph
xed iosApp/iosApp.xcodeproj           # Open the Swift runner, then build via Xcode's iOSApp scheme
```

Requirements: JDK 21, Android SDK path defined in `local.properties`, Xcode 15+ for iOS builds.

### Dependency Graph

- Generate DOT: `./gradlew generateProjectDependencyGraph`
- Optional PNG (requires Graphviz `dot` brew install graphviz):
  `dot -Tpng build/reports/dependency-graph/project-dependencies.dot -o build/reports/dependency-graph/project-dependencies.png`

## How to Run

- Android: `./gradlew :composeApp:installDebug` with an emulator/device connected, then launch the
  PomoDojo app.
- iOS/iPadOS: `xed iosApp/iosApp.xcodeproj` and run the `iosApp` scheme in Xcode against your target
  simulator (iPhone or iPad) or a connected device.
- JVM Desktop:
  ` ./gradlew :composeApp:createDistributable && ./composeApp/build/compose/binaries/main/app/PomoDojo.app/Contents/MacOS/PomoDojo`
  to start the Compose for Desktop app locally.

### Compose hot reload (Desktop)

- Start the app with the hot-reload agent: `./gradlew :composeApp:hotRunJvm`
- In another terminal, stream changes into the running app:
  `./gradlew :composeApp:reload --continuous`
  (leave both processes running while editing)

## Git Hooks

Local hooks mirror CI by running `ktlintCheck`, `check`, Android/Desktop/iOS builds before every
push. Enable them once per clone:

```
git config core.hooksPath .githooks
```

## Project Structure

- `composeApp/src/commonMain` – Shared UI, domain, DI, and resource definitions.
- `composeApp/src/androidMain | iosMain | jvmMain` – Platform-specific implementations and
  launchers.
- `composeApp/schemas` – Room schema snapshots tracked for migration reviews.
- `composeApp/src/commonTest` & `jvmTest` – Multiplatform/JVM test suites.
- `iosApp/iosApp` – Swift entry point, assets, and configuration files for the iOS target.

## Firebase App Distribution

Use the Gradle plugin (no Firebase CLI needed):

```bash
export FIREBASE_SERVICE_ACCOUNT_FILE=/path/to/service-account.json
export FIREBASE_APP_ID=1:YOUR:APP:ID
export FIREBASE_GROUPS=testers
export FIREBASE_RELEASE_NOTES="Manual upload"
./gradlew :composeApp:appDistributionUploadDevDebug
```

Notes:

- Service account must belong to the Firebase project for the app ID and have the
  `Firebase App Distribution Admin` (or broader `Firebase Admin`) role.
- `FIREBASE_APP_ID` is required; it must be the Android app’s Firebase App ID (not the package
  name).
- At least one recipient is required: set `FIREBASE_GROUPS` (comma-separated) or use Gradle
  properties like `-PfirebaseGroups=qa`; `FIREBASE_RELEASE_NOTES` can be omitted.
- CI (`.github/workflows/rc-pipeline.yml`) writes the service account JSON, exports these env vars,
  and runs `:composeApp:appDistributionUploadDevDebug` after `assembleDevDebug`.

## License
License information has not been finalized. Until a LICENSE file is added, all rights are reserved
by the repository owner.
