plugins {
    `kotlin-dsl`
}

group = "com.fakhry.pomodojo.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin) //if targetting Android
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin) //if you are using Compose Multiplatform
}


gradlePlugin {
    plugins {
        register("kotlinMultiplatform") {
            id = "com.fakhry.pomodojo.kotlinMultiplatform"
            implementationClass = "com.fakhry.pomodojo.convention.KotlinMultiplatformPlugin"
        }
    }
}
