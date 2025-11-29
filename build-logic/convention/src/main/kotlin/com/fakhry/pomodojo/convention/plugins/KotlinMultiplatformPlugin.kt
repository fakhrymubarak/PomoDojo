package com.fakhry.pomodojo.convention.plugins

import com.android.build.gradle.LibraryExtension
import com.fakhry.pomodojo.convention.libs
import com.fakhry.pomodojo.convention.project.configureKotlinAndroid
import com.fakhry.pomodojo.convention.project.configureKotlinCocoapods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension

/**
 * Plugin that contains pure kotlin dependencies.
 * */
class KotlinMultiplatformPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        with(pluginManager) {
            apply(libs.findPluginId("kotlinMultiplatform"))
            apply(libs.findPluginId("kotlinCocoapods"))
            apply(libs.findPluginId("androidLibrary"))
            apply(libs.findPluginId("kotlinSerialization"))
        }

        extensions.configure<KotlinMultiplatformExtension> {

            androidTarget()
            iosArm64()
            iosSimulatorArm64()
            jvm()

            // common dependencies available in every module
            sourceSets.apply {
                commonMain {
                    dependencies {
                        implementation(dependencies.platform(libs.findLibrary("koin-bom").get()))
                        implementation(libs.findLibrary("koin-core").get())
                        implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                        implementation(libs.findLibrary("kotlinx-datetime").get())
                        implementation(libs.findLibrary("kotlinx-serialization-json").get())
                    }
                }

                commonTest.dependencies {
                    implementation(libs.findLibrary("kotlin-test").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                }
            }

            // applying the Cocoapods configuration we made
            (this as ExtensionAware).extensions.configure<CocoapodsExtension>(::configureKotlinCocoapods)
        }
        extensions.configure<LibraryExtension>(::configureKotlinAndroid)
    }

    private fun VersionCatalog.findPluginId(string: String): String {
        return findPlugin(string).get().get().pluginId
    }

}
