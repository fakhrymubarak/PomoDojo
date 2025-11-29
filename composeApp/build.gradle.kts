import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.googleGmsGoogleServices)
    alias(libs.plugins.googleFirebaseCrashlytics)
    id("com.fakhry.pomodojo.quality")
}

fun Project.envProps(fileName: String): Properties {
    val props = Properties()
    val envFile = rootProject.file(fileName)
    if (envFile.exists()) {
        envFile.inputStream().use { props.load(it) }
    }
    return props
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts("-framework", "AVFAudio")
        }

        iosTarget.compilations.getByName("main") {
            cinterops {
                @Suppress("unused")
                val pomodoroLiveActivityBridge by creating {
                    definitionFile =
                        project.file("src/nativeInterop/cinterop/PomodoroLiveActivityBridge.def")
                    packageName = "com.fakhry.pomodojo.liveactivity.bridge"
                    includeDirs(project.file("../iosApp/iosApp"))
                }
            }
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.ui.tooling)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
        }
        commonMain.dependencies {
            implementation(project(":core:database"))
            implementation(project(":core:datastore"))
            implementation(project(":shared:domain"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.collections.immutable)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ui.backhandler)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)

            // OrbitMVI
            implementation(libs.orbit.core)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.orbit.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.sqlite.bundled.jvm)
        }
    }
}

android {
    namespace = "com.fakhry.pomodojo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.fakhry.pomodojo"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            val envProps = project.envProps("config/dev.props")
            val dbName = envProps.getProperty("DB_NAME", "pomodojo_dev.db")
            buildConfigField("String", "LOCAL_DB_NAME", "\"$dbName\"")
        }
        create("prod") {
            dimension = "environment"
            val envProps = project.envProps("config/prod.props")
            val dbName = envProps.getProperty("DB_NAME", "pomodojo.db")
            buildConfigField("String", "LOCAL_DB_NAME", "\"$dbName\"")
        }
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.firebase.crashlytics)

    // Room KSP
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

private val environmentAttribute =
    Attribute.of("com.android.build.api.attributes.ProductFlavor:environment", String::class.java)
private val legacyEnvironmentAttribute = Attribute.of("environment", String::class.java)

configurations.configureEach {
    val nameLower = name.lowercase()
    when {
        nameLower.startsWith("androiddev") -> {
            attributes.attribute(environmentAttribute, "dev")
            attributes.attribute(legacyEnvironmentAttribute, "dev")
        }

        nameLower.startsWith("androidprod") -> {
            attributes.attribute(environmentAttribute, "prod")
            attributes.attribute(legacyEnvironmentAttribute, "prod")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.fakhry.pomodojo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "PomoDojo"
            packageVersion = "1.0.0"
            // Include jdk.unsupported so sun.misc.Unsafe is available in the jlinked runtime.
            modules("jdk.unsupported")
        }
    }
}

compose.resources {
    packageOfResClass = "com.fakhry.pomodojo.generated.resources"
}

room {
    schemaDirectory("$projectDir/schemas")
}
