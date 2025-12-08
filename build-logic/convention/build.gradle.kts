plugins {
    `kotlin-dsl`
}

group = "com.fakhry.pomodojo.buildlogic"

dependencies {
    implementation(project(":catalog"))
    implementation(project(":quality"))
    compileOnly(libs.android.gradlePlugin) //if targetting Android
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin) //if you are using Compose Multiplatform
}


gradlePlugin {
    plugins {
        register("pomodojoFeatures") {
            id = "com.fakhry.pomodojo.features"
            implementationClass = "com.fakhry.pomodojo.convention.plugins.PomodojoFeaturesPlugin"
        }
        register("pomodojoLibrary") {
            id = "com.fakhry.pomodojo.library"
            implementationClass = "com.fakhry.pomodojo.convention.plugins.PomodojoLibraryPlugin"
        }
        register("pomodojoDependencyGraph") {
            id = "com.fakhry.pomodojo.dependencygraph"
            implementationClass = "com.fakhry.pomodojo.convention.plugins.DependencyGraphPlugin"
        }
    }
}
