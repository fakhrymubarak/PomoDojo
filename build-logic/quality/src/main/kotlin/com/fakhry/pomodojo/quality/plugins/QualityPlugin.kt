package com.fakhry.pomodojo.quality.plugins

import com.fakhry.pomodojo.quality.libs
import com.fakhry.pomodojo.quality.projects.configureDetekt
import com.fakhry.pomodojo.quality.projects.configureKtlint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog

class QualityPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        with(pluginManager) {
            apply(libs.findPluginId("ktlint"))
            apply(libs.findPluginId("detekt"))
        }
        pluginManager.apply(JacocoMultiplatformPlugin::class.java)

        configureKtlint()
        configureDetekt()

        tasks.matching { it.name == "check" }.configureEach {
            dependsOn("ktlintCheck")
            dependsOn("detekt")
        }
    }


    private fun VersionCatalog.findPluginId(alias: String): String {
        return findPlugin(alias).get().get().pluginId
    }
}
