plugins {
    alias(libs.plugins.pomodojoLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.compilations.getByName("main") {
            cinterops {
                @Suppress("unused")
                val pomodoroLiveActivityBridge by creating {
                    definitionFile =
                        project.file("src/nativeInterop/cinterop/PomodoroLiveActivityBridge.def")
                    packageName = "com.fakhry.pomodojo.liveactivity.bridge"
                    includeDirs(project.file("../../iosApp/iosApp"))
                }
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:utils"))
            implementation(project(":domain:focus"))
            implementation(project(":domain:preferences"))
            implementation(project(":data:pomodoro"))
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
