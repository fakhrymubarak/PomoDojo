package com.fakhry.pomodojo.convention.plugins

import com.fakhry.pomodojo.quality.plugins.QualityPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class PomodojoLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(KotlinMultiplatformPlugin::class.java)
        pluginManager.apply(QualityPlugin::class.java)
    }
}
