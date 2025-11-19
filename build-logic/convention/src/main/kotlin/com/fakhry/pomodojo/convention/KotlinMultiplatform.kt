package com.fakhry.pomodojo.convention

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension


// Configuring Kotlin Multiplatform
internal fun Project.configureKotlinMultiplatform(
    extension: KotlinMultiplatformExtension,
) = extension.apply {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    // common dependencies available in every module
    sourceSets.apply {
        commonMain {
            dependencies {
                implementation(
                    this@configureKotlinMultiplatform.dependencies.platform(
                        libs.findLibrary("koin-bom").get(),
                    ),
                )
                implementation(libs.findLibrary("koin-core").get())
                implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                implementation(libs.findLibrary("kotlinx-datetime").get())
                implementation(libs.findLibrary("kotlinx-serialization-json").get())
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    // applying the Cocoapods configuration we made
    (this as ExtensionAware).extensions.configure<CocoapodsExtension>(::configureKotlinCocoapods)
}
