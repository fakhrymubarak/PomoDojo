plugins {
    alias(libs.plugins.pomodojoLibrary)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:utils")) // Dispatcher Provider
            implementation(project(":core:datastore"))
            implementation(project(":core:database"))
            implementation(project(":core:notification"))

            implementation(project(":data:pomodoro"))
            implementation(project(":data:preferences"))
            implementation(project(":data:history"))

            implementation(project(":domain:preferences"))
            implementation(project(":domain:focus"))
            implementation(project(":domain:history"))

            implementation(project(":feature:dashboard"))
            implementation(project(":feature:focus"))
            implementation(project(":feature:preferences"))

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(project.dependencies.platform(libs.koin.bom))
        }

        androidMain.dependencies {
            implementation(project(":core:datastore"))
            implementation(project(":core:database"))
            implementation(project(":core:notification"))
        }
    }
}
