package com.fakhry.pomodojo.convention.project

import com.android.build.gradle.LibraryExtension
import com.fakhry.pomodojo.convention.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Project

// Configuring Android
internal fun Project.configureKotlinAndroid(
    extension: LibraryExtension,
) = extension.apply {

    // build a namespace from the module path to avoid collisions
    val moduleName = path.removePrefix(":").replace(":", ".")
    namespace = if (moduleName.isNotEmpty()) {
        "com.fakhry.pomodojo.$moduleName"
    } else {
        "com.fakhry.pomodojo"
    }

    compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
    defaultConfig {
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
