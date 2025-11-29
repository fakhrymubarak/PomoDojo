plugins {
    id("com.fakhry.pomodojo.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
        }
    }
}
