plugins {
    `kotlin-dsl`
}

group = "com.fakhry.pomodojo.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin) //if targetting Android
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin) //if you are using Compose Multiplatform
    implementation(project(":quality"))
}


gradlePlugin {
    plugins {
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
