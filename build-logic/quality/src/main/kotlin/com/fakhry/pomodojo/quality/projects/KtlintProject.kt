package com.fakhry.pomodojo.quality.projects

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import java.io.File

internal fun Project.configureKtlint() {
    configure<KtlintExtension> {
        android.set(true)
        filter {
            exclude { element ->
                element.file.absolutePath.contains("${File.separator}build${File.separator}")
            }
            exclude { element ->
                element.file.absolutePath.contains("${File.separator}build${File.separator}generated${File.separator}")
            }
        }
    }
}
