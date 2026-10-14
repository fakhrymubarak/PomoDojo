plugins {
    alias(libs.plugins.pomodojoFeatures)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:datastore"))
            implementation(project(":domain:pomodoro"))

            implementation(libs.androidx.datastore.preferences)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.firebase.config)
            implementation(libs.play.app.update.ktx)
        }
    }
}
