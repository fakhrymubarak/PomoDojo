plugins {
    `kotlin-dsl`
}

group = "com.fakhry.pomodojo.buildlogic"
dependencies {
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("quality") {
            id = "com.fakhry.pomodojo.quality"
            implementationClass = "com.fakhry.pomodojo.quality.plugins.QualityPlugin"
        }
    }
}
