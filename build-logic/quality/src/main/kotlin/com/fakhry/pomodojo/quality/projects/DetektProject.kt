package com.fakhry.pomodojo.quality.projects

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

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

    // Detekt does not auto-discover Kotlin Multiplatform source sets because KMP
    // projects don't use the conventional src/main/kotlin layout. Wire every KMP
    // source directory into the detekt task explicitly so it actually analyses code.
    afterEvaluate {
        val kmpExtension = extensions.findByType<KotlinMultiplatformExtension>()
        if (kmpExtension != null) {
            val kmpSourceDirs = kmpExtension.sourceSets
                .flatMap { it.kotlin.srcDirs }
                .filter { it.exists() }
            tasks.withType<Detekt>().configureEach {
                setSource(kmpSourceDirs)
            }
            tasks.withType<DetektCreateBaselineTask>().configureEach {
                setSource(kmpSourceDirs)
            }
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
