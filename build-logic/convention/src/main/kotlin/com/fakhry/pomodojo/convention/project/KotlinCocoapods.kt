package com.fakhry.pomodojo.convention.project

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension

// Configuring Cocoapods for iOS
internal fun Project.configureKotlinCocoapods(
    extension: CocoapodsExtension,
) = extension.apply {
    val moduleName = this@configureKotlinCocoapods.path.split(":").drop(1).joinToString("-")
    summary = "Shared module"
    homepage = "https://github.com/fakhry/pomodojo"
    version = "1.0"
    ios.deploymentTarget = "14.1"
    name = moduleName
    framework {
        isStatic = true
        baseName = moduleName
    }
}
