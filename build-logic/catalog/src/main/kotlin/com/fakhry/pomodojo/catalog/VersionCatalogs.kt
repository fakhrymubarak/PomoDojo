package com.fakhry.pomodojo.catalog

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

// ==============================================================================
// LIBRARIES
// ==============================================================================

// AndroidX
val VersionCatalog.androidxDatastore get() = findLibAlias("androidx-datastore")
val VersionCatalog.androidxDatastorePreferences get() = findLibAlias("androidx-datastore-preferences")
val VersionCatalog.androidxUiTooling get() = findLibAlias("androidx-ui-tooling")
val VersionCatalog.androidxCoreKtx get() = findLibAlias("androidx-core-ktx")
val VersionCatalog.androidxCoreSplashscreen get() = findLibAlias("androidx-core-splashscreen")
val VersionCatalog.androidxAppcompat get() = findLibAlias("androidx-appcompat")
val VersionCatalog.androidxActivityCompose get() = findLibAlias("androidx-activity-compose")
val VersionCatalog.androidxLifecycleViewmodelCompose get() = findLibAlias("androidx-lifecycle-viewmodelCompose")
val VersionCatalog.androidxLifecycleRuntimeCompose get() = findLibAlias("androidx-lifecycle-runtimeCompose")

// Room & SQLite
val VersionCatalog.androidxRoomCompiler get() = findLibAlias("androidx-room-compiler")
val VersionCatalog.androidxRoomRuntime get() = findLibAlias("androidx-room-runtime")
val VersionCatalog.androidxRoomPaging get() = findLibAlias("androidx-room-paging")
val VersionCatalog.androidxSqliteBundledJvm get() = findLibAlias("androidx-sqlite-bundled-jvm")
val VersionCatalog.sqliteBundled get() = findLibAlias("sqlite-bundled")

// Testing
val VersionCatalog.kotlinTest get() = findLibAlias("kotlin-test")
val VersionCatalog.kotlinTestJunit get() = findLibAlias("kotlin-testJunit")
val VersionCatalog.junit get() = findLibAlias("junit")
val VersionCatalog.androidxTestExtJunit get() = findLibAlias("androidx-testExt-junit")
val VersionCatalog.androidxEspressoCore get() = findLibAlias("androidx-espresso-core")
val VersionCatalog.androidxRunner get() = findLibAlias("androidx-runner")
val VersionCatalog.androidxCoreTest get() = findLibAlias("androidx-core") // mapped to 'core' in toml
val VersionCatalog.kotlinxCoroutinesTest get() = findLibAlias("kotlinx-coroutines-test")
val VersionCatalog.orbitTest get() = findLibAlias("orbit-test")

// Kotlin & Coroutines
val VersionCatalog.kotlinStdlib get() = findLibAlias("kotlin-stdlib")
val VersionCatalog.kotlinxCollectionsImmutable get() = findLibAlias("kotlinx-collections-immutable")
val VersionCatalog.kotlinxDatetime get() = findLibAlias("kotlinx-datetime")
val VersionCatalog.kotlinxCoroutinesCore get() = findLibAlias("kotlinx-coroutines-core")
val VersionCatalog.kotlinxCoroutinesSwing get() = findLibAlias("kotlinx-coroutinesSwing")
val VersionCatalog.kotlinxSerializationJson get() = findLibAlias("kotlinx-serialization-json")

// Koin
val VersionCatalog.koinBom get() = findLibAlias("koin-bom")
val VersionCatalog.koinCore get() = findLibAlias("koin-core")
val VersionCatalog.koinCompose get() = findLibAlias("koin-compose")
val VersionCatalog.koinComposeViewmodel get() = findLibAlias("koin-compose-viewmodel")
val VersionCatalog.koinComposeViewmodelNavigation get() = findLibAlias("koin-compose-viewmodel-navigation")

// Orbit MVI
val VersionCatalog.orbitCompose get() = findLibAlias("orbit-compose")
val VersionCatalog.orbitCore get() = findLibAlias("orbit-core")
val VersionCatalog.orbitViewmodel get() = findLibAlias("orbit-viewmodel")

// Compose Utils
val VersionCatalog.navigationCompose get() = findLibAlias("navigation-compose")
val VersionCatalog.uiBackhandler get() = findLibAlias("ui-backhandler")

// Firebase & Google
val VersionCatalog.firebaseCrashlytics get() = findLibAlias("firebase-crashlytics")

// Gradle Plugins (Libraries context)
val VersionCatalog.androidGradlePlugin get() = findLibAlias("android-gradlePlugin")
val VersionCatalog.kotlinGradlePlugin get() = findLibAlias("kotlin-gradlePlugin")
val VersionCatalog.composeGradlePlugin get() = findLibAlias("compose-gradlePlugin")
val VersionCatalog.detektGradlePlugin get() = findLibAlias("detekt-gradlePlugin")
val VersionCatalog.ktlintGradlePlugin get() = findLibAlias("ktlint-gradlePlugin")


// ==============================================================================
// PLUGINS
// ==============================================================================

val VersionCatalog.androidApplication get() = findPluginId("androidApplication")
val VersionCatalog.androidLibrary get() = findPluginId("androidLibrary")
val VersionCatalog.composeHotReload get() = findPluginId("composeHotReload")
val VersionCatalog.composeMultiplatform get() = findPluginId("composeMultiplatform")
val VersionCatalog.composeCompiler get() = findPluginId("composeCompiler")
val VersionCatalog.kotlinMultiplatform get() = findPluginId("kotlinMultiplatform")
val VersionCatalog.kotlinSerialization get() = findPluginId("kotlinSerialization")
val VersionCatalog.kotlinCocoapods get() = findPluginId("kotlinCocoapods")
val VersionCatalog.room get() = findPluginId("room")
val VersionCatalog.ksp get() = findPluginId("ksp")
val VersionCatalog.ktlint get() = findPluginId("ktlint")
val VersionCatalog.googleGmsGoogleServices get() = findPluginId("googleGmsGoogleServices")
val VersionCatalog.googleFirebaseCrashlytics get() = findPluginId("googleFirebaseCrashlytics")
val VersionCatalog.jacoco get() = findPluginId("jacoco")
val VersionCatalog.androidKotlinMultiplatformLibrary get() = findPluginId("androidKotlinMultiplatformLibrary")
val VersionCatalog.androidLint get() = findPluginId("androidLint")
val VersionCatalog.detekt get() = findPluginId("detekt")
val VersionCatalog.jetbrainsKotlinJvm get() = findPluginId("jetbrainsKotlinJvm")
val VersionCatalog.pomodojoLibrary get() = findPluginId("pomodojoLibrary")
val VersionCatalog.pomodojoFeatures get() = findPluginId("pomodojoFeatures")


val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

private fun VersionCatalog.findPluginId(string: String) = findPlugin(string).get().get().pluginId

private fun VersionCatalog.findLibAlias(alias: String): Provider<MinimalExternalModuleDependency?> =
    findLibrary(alias).get()
