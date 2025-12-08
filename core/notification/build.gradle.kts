plugins {
    alias(libs.plugins.pomodojoLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
            implementation(project(":domain:pomodoro"))
            implementation(project(":core:utils"))
            implementation(compose.runtime)
            implementation(compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
