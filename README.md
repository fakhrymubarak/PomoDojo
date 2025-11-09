# PomoDojo Compose Multiplatform

## Features
- Shared Kotlin Multiplatform codebase powering Android, Desktop, and iOS entry points with Compose UI.
- Pomodoro-focused workflows: dashboard, active session controls, and persistence via Room/SQLite.
- Orbit MVI view models with Koin dependency injection for predictable state and thin platform layers.
- Centralized preferences, theming, and localization resources in `commonMain` for consistent UX.

## How to Build
```
./gradlew :composeApp:assembleDebug   # Android debug APK + KSP/Room schema verification
./gradlew :composeApp:run             # Desktop JVM app (Compose for Desktop)
./gradlew :composeApp:jvmTest         # Shared + JVM unit tests with kotlin.test/orbit
./gradlew ktlintCheck                 # Repository-wide Kotlin style checks
./gradlew ktlintFormat                # Auto-formats Kotlin sources (apply before committing)
xed iosApp/iosApp.xcodeproj           # Open the Swift runner, then build via Xcode's iOSApp scheme
```
Requirements: JDK 17+, Android SDK path defined in `local.properties`, Xcode 15+ for iOS builds.

## Project Structure
- `composeApp/src/commonMain` – Shared UI, domain, DI, and resource definitions.
- `composeApp/src/androidMain | iosMain | jvmMain` – Platform-specific implementations and launchers.
- `composeApp/schemas` – Room schema snapshots tracked for migration reviews.
- `composeApp/src/commonTest` & `jvmTest` – Multiplatform/JVM test suites.
- `iosApp/iosApp` – Swift entry point, assets, and configuration files for the iOS target.

## License
License information has not been finalized. Until a LICENSE file is added, all rights are reserved by the repository owner.
