package com.fakhry.pomodojo.convention.plugins

import com.android.build.gradle.LibraryExtension
import com.fakhry.pomodojo.catalog.androidLibrary
import com.fakhry.pomodojo.catalog.koinBom
import com.fakhry.pomodojo.catalog.koinCompose
import com.fakhry.pomodojo.catalog.koinComposeViewmodel
import com.fakhry.pomodojo.catalog.koinComposeViewmodelNavigation
import com.fakhry.pomodojo.catalog.koinCore
import com.fakhry.pomodojo.catalog.kotlinCocoapods
import com.fakhry.pomodojo.catalog.kotlinMultiplatform
import com.fakhry.pomodojo.catalog.kotlinSerialization
import com.fakhry.pomodojo.catalog.kotlinTest
import com.fakhry.pomodojo.catalog.kotlinxCoroutinesCore
import com.fakhry.pomodojo.catalog.kotlinxCoroutinesTest
import com.fakhry.pomodojo.catalog.kotlinxDatetime
import com.fakhry.pomodojo.catalog.kotlinxSerializationJson
import com.fakhry.pomodojo.catalog.libs
import com.fakhry.pomodojo.convention.project.configureKotlinAndroid
import com.fakhry.pomodojo.convention.project.configureKotlinCocoapods
import org.gradle.api.Plugin
import org.gradle.api.Project
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
            apply(libs.kotlinMultiplatform)
            apply(libs.kotlinCocoapods)
            apply(libs.androidLibrary)
            apply(libs.kotlinSerialization)
        }

        extensions.configure<KotlinMultiplatformExtension> {

            androidTarget {
                // Publish all environment variants so flavored modules can resolve each other
                publishLibraryVariants("devDebug", "devRelease", "prodDebug", "prodRelease")
            }
            iosArm64()
            iosSimulatorArm64()
            jvm()

            // common dependencies available in every module
            sourceSets.apply {
                androidMain {

                }
                iosMain {

                }
                jvmMain {

                }
                commonMain {
                    dependencies {
                        implementation(dependencies.platform(libs.koinBom))
                        implementation(libs.koinCore)
                        implementation(libs.koinCompose)
                        implementation(libs.koinComposeViewmodel)
                        implementation(libs.koinComposeViewmodelNavigation)

                        implementation(libs.kotlinxCoroutinesCore)
                        implementation(libs.kotlinxDatetime)
                        implementation(libs.kotlinxSerializationJson)
                    }
                }

                commonTest.dependencies {
                    implementation(libs.kotlinTest)
                    implementation(libs.kotlinxCoroutinesTest)
                }
            }

            // applying the Cocoapods configuration we made
            (this as ExtensionAware).extensions.configure<CocoapodsExtension>(::configureKotlinCocoapods)
        }
        extensions.configure<LibraryExtension>(::configureKotlinAndroid)
    }
}
