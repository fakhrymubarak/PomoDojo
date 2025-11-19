import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.googleGmsGoogleServices) apply false
    alias(libs.plugins.googleFirebaseCrashlytics) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<KtlintExtension> {
        android.set(true)
        filter {
            exclude { element ->
                element.file.absolutePath.contains("${File.separator}build${File.separator}")
            }
        }
    }

    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(tasks.named("ktlintCheck"))
    }
}

tasks.register("ktlintCheck") {
    dependsOn(subprojects.map { "${it.path}:ktlintCheck" })
}

tasks.register("ktlintFormat") {
    dependsOn(subprojects.map { "${it.path}:ktlintFormat" })
}

val rootJacocoCoverage = tasks.register("jacocoJvmTestCoverageVerification")
val rootJacocoReport = tasks.register("jacocoJvmTestReport")

gradle.projectsEvaluated {
    val coverageTasks =
        subprojects.mapNotNull { it.tasks.findByName("jacocoJvmTestCoverageVerification") }
    val reportTasks = subprojects.mapNotNull { it.tasks.findByName("jacocoJvmTestReport") }

    rootJacocoCoverage.configure {
        dependsOn(coverageTasks)
    }
    rootJacocoReport.configure {
        dependsOn(reportTasks)
    }
}
