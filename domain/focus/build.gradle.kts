plugins {
    id("com.fakhry.pomodojo.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
