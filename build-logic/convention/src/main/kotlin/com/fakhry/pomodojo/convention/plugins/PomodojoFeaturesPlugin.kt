package com.fakhry.pomodojo.convention.plugins

import com.fakhry.pomodojo.catalog.Core
import com.fakhry.pomodojo.catalog.androidxLifecycleRuntimeCompose
import com.fakhry.pomodojo.catalog.androidxLifecycleViewmodelCompose
import com.fakhry.pomodojo.catalog.composeCompiler
import com.fakhry.pomodojo.catalog.composeMultiplatform
import com.fakhry.pomodojo.catalog.kotlinTest
import com.fakhry.pomodojo.catalog.kotlinxCollectionsImmutable
import com.fakhry.pomodojo.catalog.kotlinxCoroutinesTest
import com.fakhry.pomodojo.catalog.libs
import com.fakhry.pomodojo.catalog.uiBackhandler
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Plugin that contains pure kotlin dependencies.
 * */
@Suppress("unused")
class PomodojoFeaturesPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        with(pluginManager) {
            apply(PomodojoLibraryPlugin::class.java)
            apply(libs.composeMultiplatform)
            apply(libs.composeCompiler)
        }

        val compose = extensions.getByType<ComposeExtension>().dependencies
        extensions.configure<KotlinMultiplatformExtension> {
            // common dependencies available in every features
            sourceSets.apply {
                androidMain.dependencies {

                }
                commonMain.dependencies {
                    implementation(project(Core.UTILS))
                    implementation(project(Core.DESIGN_SYSTEM))

                    // Compose
                    implementation(compose.runtime)
                    implementation(compose.foundation)
                    implementation(compose.material3)
                    implementation(compose.materialIconsExtended)
                    implementation(compose.ui)
                    implementation(compose.components.resources)
                    implementation(compose.components.uiToolingPreview)

                    implementation(libs.androidxLifecycleViewmodelCompose)
                    implementation(libs.androidxLifecycleRuntimeCompose)
                    implementation(libs.kotlinxCollectionsImmutable)
                    implementation(libs.uiBackhandler)
                }

                commonTest.dependencies {
                    implementation(libs.kotlinTest)
                    implementation(libs.kotlinxCoroutinesTest)
                }
            }
        }
    }
}
