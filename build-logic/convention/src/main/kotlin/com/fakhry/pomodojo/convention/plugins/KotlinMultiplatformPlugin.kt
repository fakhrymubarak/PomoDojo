package com.fakhry.pomodojo.convention.plugins

import com.android.build.gradle.LibraryExtension
import com.fakhry.pomodojo.convention.libs
import com.fakhry.pomodojo.convention.project.configureKotlinAndroid
import com.fakhry.pomodojo.convention.project.configureKotlinMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        with(pluginManager) {
            apply(libs.findPluginId("kotlinMultiplatform"))
            apply(libs.findPluginId("kotlinCocoapods"))
            apply(libs.findPluginId("androidLibrary"))
            apply(libs.findPluginId("kotlinSerialization"))
        }

        extensions.configure<KotlinMultiplatformExtension>(::configureKotlinMultiplatform)
        extensions.configure<LibraryExtension>(::configureKotlinAndroid)
    }

    private fun VersionCatalog.findPluginId(string: String): String {
        return findPlugin(string).get().get().pluginId
    }

}
