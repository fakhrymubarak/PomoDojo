package com.fakhry.pomodojo.quality.plugins

import com.fakhry.pomodojo.catalog.detekt
import com.fakhry.pomodojo.catalog.ktlint
import com.fakhry.pomodojo.catalog.libs
import com.fakhry.pomodojo.quality.projects.configureDetekt
import com.fakhry.pomodojo.quality.projects.configureKtlint
import org.gradle.api.Plugin
import org.gradle.api.Project

class QualityPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        with(pluginManager) {
            apply(libs.ktlint)
            apply(libs.detekt)
        }
        pluginManager.apply(JacocoMultiplatformPlugin::class.java)

        configureKtlint()
        configureDetekt()

        tasks.matching { it.name == "check" }.configureEach {
            dependsOn("ktlintCheck")
            dependsOn("detekt")
        }
    }
}
