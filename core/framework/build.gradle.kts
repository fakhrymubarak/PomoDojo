plugins {
    id("com.fakhry.pomodojo.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.compilations.getByName("main") {
            cinterops {
                val pomodoroLiveActivityBridge by creating {
                    definitionFile =
                        project.file("src/nativeInterop/cinterop/PomodoroLiveActivityBridge.def")
                    packageName = "com.fakhry.pomodojo.liveactivity.bridge"
                    includeDirs(project.file("../../iosApp/iosApp"))
                }
            }
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:utils"))
            implementation(project(":shared:domain"))
            implementation(project(":domain:focus"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.runtime)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(project(":core:datastore"))
            implementation(project(":core:database"))
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.fakhry.pomodojo.core.framework"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") { dimension = "environment" }
        create("prod") { dimension = "environment" }
    }
}
