plugins {
    alias(libs.plugins.pomodojoLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:utils"))
            implementation(project(":feature:focus"))
            implementation(project(":core:database"))
            implementation(project(":core:framework"))
            implementation(project(":core:datastore"))
            implementation(project(":core:designsystem"))
            implementation(project(":feature:common"))
            implementation(project(":feature:preferences"))
            implementation(project(":domain:focus"))
            implementation(project(":domain:preferences"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.orbit.core)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.orbit.test)
        }
    }
}

android {
    namespace = "com.fakhry.pomodojo.feature.dashboard"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    flavorDimensions += "environment"
    productFlavors {
        create("dev") { dimension = "environment" }
        create("prod") { dimension = "environment" }
    }
}
