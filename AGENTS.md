# Repository Guidelines

## Project Structure & Module Organization

The multiplatform source of truth lives in `composeApp/src`: shared UI, domain, and DI code in
`commonMain/kotlin`, with platform forks under `androidMain`, `iosMain`, and `jvmMain`. Desktop
launcher code stays in `jvmMain`; Android entrypoints and resources stay in `androidMain`. Shared
assets reside in `commonMain/composeResources`. Tests mirror the layout via `commonTest` for
platform-agnostic logic and `jvmTest` for JVM-only behavior. Room schema snapshots live in
`composeApp/schemas`, and the native Swift launcher remains in `iosApp/iosApp`.

## Build, Test, and Development Commands

```
./gradlew :composeApp:assembleDebug        # Android debug build + KSP/Room schema validation
./gradlew :composeApp:run                  # Desktop JVM app for rapid UI iteration
./gradlew :composeApp:jvmTest              # Executes JVM + common tests (kotlin.test/orbit)
./gradlew :composeApp:jacocoJvmTestReport  # Generates coverage in composeApp/build/reports
./gradlew ktlintCheck                      # Kotlin style verification across all source sets
./gradlew ktlintFormat                     # Auto-formats Kotlin files; run before large refactors
xed iosApp/iosApp.xcodeproj                # Opens the iOS runner; build via the iOSApp scheme
```

Ensure `local.properties` resolves the Android SDK and run Gradle with a Java 17-compatible JDK.

## Coding Style & Naming Conventions

Follow idiomatic Kotlin with 4-space indentation, trailing commas in multiline parameter lists, and
prefer `val` over `var`. Compose screens and components use PascalCase suffixed with `Screen`,
`Section`, or `Header`. Platform-specific extensions follow the `StringExt.android.kt` pattern: keep
expect declarations in `commonMain` and actuals per source set. Keep DI wiring inside
`com.fakhry.pomodojo.di`, rely on Orbit containers for stateful view models, and house Room
entities/DAOs in `focus/data/db`.

## Testing Guidelines

Add pure business logic tests to `composeApp/src/commonTest` using `kotlin.test`,
`kotlinx.coroutines.test`, and `orbit-test`. JVM-only logic (Koin modules, view-model orchestration)
belongs in `composeApp/src/jvmTest`, typically with fakes similar to `DashboardViewModelTest`. Match
the `*Test.kt` suffix, keep one scenario per `@Test`, wire coroutines with `StandardTestDispatcher`,
and always run `./gradlew :composeApp:jvmTest`, keeping Jacoco trends flat or improving.

## Commit & Pull Request Guidelines

Commits follow the `type: imperative summary` style seen in history (
`feat: add start countDown logic`, `refactor: optimize recomposition…`). Scope each commit narrowly,
include schema or resource updates, and mention modules touched. PRs should explain motivation,
summarize the approach, link tracking issues, and attach screenshots or screen recordings for
UI-facing changes on Android, Desktop, and (when feasible) iOS. Close by listing verification
commands and highlighting follow-up TODOs.

## Configuration & Security Tips

Never commit secrets: keep API keys or signing configs in untracked `local.properties` or
environment variables. If you change Room entities, regenerate schemas (
`./gradlew :composeApp:kspDebugKotlin`) so `composeApp/schemas` stays in sync for migration review.
Prefer `gradle.properties` for shared but non-sensitive compiler flags.
