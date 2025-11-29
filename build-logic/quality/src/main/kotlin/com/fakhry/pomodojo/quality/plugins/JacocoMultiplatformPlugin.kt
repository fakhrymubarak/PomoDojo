package com.fakhry.pomodojo.quality.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.math.BigDecimal
import javax.inject.Inject

abstract class JacocoMultiplatformExtension @Inject constructor(
    objects: ObjectFactory,
) {
    val classExclusions = objects
        .listProperty(String::class.java)
        .convention(listOf("**/generated/**"))
    val lineCoverageThreshold =
        objects.property(BigDecimal::class.java).convention(BigDecimal.ZERO)
}

class JacocoMultiplatformPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("jacoco")

        val jacocoExtension =
            extensions.create("jacocoMultiplatform", JacocoMultiplatformExtension::class.java)
        val kotlinExt = extensions.getByType<KotlinMultiplatformExtension>()

        tasks.withType<Test>().configureEach {
            extensions.configure(JacocoTaskExtension::class) {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }

        afterEvaluate {
            registerJacocoTasks(kotlinExt, jacocoExtension)
        }
    }

    private fun Project.registerJacocoTasks(
        kotlinExt: KotlinMultiplatformExtension,
        extension: JacocoMultiplatformExtension,
    ) {
        val jacocoJvmExec = layout.buildDirectory.file("jacoco/jvmTest.exec")
        val jvmMainCompilation =
            kotlinExt.targets.getByName("jvm").compilations.getByName("main")
        val jvmSourceDirs =
            jvmMainCompilation.allKotlinSourceSets.flatMap { it.kotlin.sourceDirectories.files }

        tasks.register<JacocoReport>("jacocoJvmTestReport") {
            dependsOn("jvmTest")

            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(false)
                html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jvmTestResult"))
            }

            sourceDirectories.setFrom(files(jvmSourceDirs))
            classDirectories.setFrom(
                files(
                    jvmMainCompilation.output.classesDirs.files.map { dir ->
                        fileTree(dir) {
                            exclude(extension.classExclusions.get())
                        }
                    },
                ),
            )
            executionData.setFrom(jacocoJvmExec)
        }

        tasks.register<JacocoCoverageVerification>("jacocoJvmTestCoverageVerification") {
            dependsOn("jvmTest")

            sourceDirectories.setFrom(files(jvmSourceDirs))
            classDirectories.setFrom(
                files(
                    jvmMainCompilation.output.classesDirs.files.map { dir ->
                        fileTree(dir) {
                            exclude(extension.classExclusions.get())
                        }
                    },
                ),
            )
            executionData.setFrom(jacocoJvmExec)
            violationRules {
                rule {
                    element = "BUNDLE"
                    limit {
                        counter = "LINE"
                        value = "COVEREDRATIO"
                        minimum = extension.lineCoverageThreshold.get()
                    }
                }
            }
        }

        tasks.named("check").configure {
            dependsOn("jacocoJvmTestCoverageVerification")
        }

        tasks.named("jacocoJvmTestCoverageVerification").configure {
            finalizedBy("jacocoJvmTestReport")
        }
    }
}
