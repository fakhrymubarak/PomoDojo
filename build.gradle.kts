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
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.googleGmsGoogleServices) apply false
    alias(libs.plugins.googleFirebaseCrashlytics) apply false
    id("com.fakhry.pomodojo.dependencygraph")
}

tasks.register("ktlintCheck")

tasks.register("ktlintFormat")

val rootJacocoCoverage = tasks.register("jacocoJvmTestCoverageVerification")
val rootJacocoReport = tasks.register("jacocoJvmTestReport")

gradle.projectsEvaluated {
    val coverageTasks =
        subprojects.mapNotNull { it.tasks.findByName("jacocoJvmTestCoverageVerification") }
    val reportTasks = subprojects.mapNotNull { it.tasks.findByName("jacocoJvmTestReport") }
    val ktlintCheckTasks =
        subprojects.flatMap { it.tasks.matching { task -> task.name == "ktlintCheck" } }
    val ktlintFormatTasks =
        subprojects.flatMap { it.tasks.matching { task -> task.name == "ktlintFormat" } }

    rootJacocoCoverage.configure {
        dependsOn(coverageTasks)
    }
    rootJacocoReport.configure {
        dependsOn(reportTasks)
    }
    tasks.named("ktlintCheck").configure {
        dependsOn(ktlintCheckTasks)
    }
    tasks.named("ktlintFormat").configure {
        dependsOn(ktlintFormatTasks)
    }
}
