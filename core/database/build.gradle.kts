import java.util.Properties

plugins {
    alias(libs.plugins.pomodojoLibrary)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
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
    androidTarget {
        publishLibraryVariants("devDebug", "devRelease", "prodDebug", "prodRelease")
    }
    sourceSets {
        androidMain
        commonMain.dependencies {
            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
}

dependencies {
    // Room KSP
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.fakhry.pomodojo.core.database"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
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
}
