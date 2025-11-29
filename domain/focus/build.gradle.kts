plugins {
    id("com.fakhry.pomodojo.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:domain"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
