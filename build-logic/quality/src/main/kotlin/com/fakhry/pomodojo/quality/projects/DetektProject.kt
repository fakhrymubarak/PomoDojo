package com.fakhry.pomodojo.quality.projects

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

internal fun Project.configureDetekt() {
    configure<DetektExtension> {
        buildUponDefaultConfig = true
        parallel = true

        val configFile =
            rootProject.layout.projectDirectory.file("config/detekt/detekt.yml").asFile
        if (configFile.exists()) {
            config.setFrom(files(configFile))
        }

        val baselineFile =
            rootProject.layout.projectDirectory.file("config/detekt/baseline.xml").asFile
        if (baselineFile.exists()) {
            baseline = file(baselineFile)
        }
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        exclude("**/build/**")
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
        }
    }
}
