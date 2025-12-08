package com.fakhry.pomodojo.convention.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

abstract class GenerateProjectDependencyGraph : DefaultTask() {
    @get:Input
    abstract val edges: ListProperty<String>

    @get:Input
    abstract val nodes: ListProperty<String>

    @get:OutputFile
    abstract val dotFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val parsedEdges = edges.get().mapNotNull { raw ->
            raw.split("||", limit = 2).takeIf { it.size == 2 }?.let { it[0] to it[1] }
        }
        val sortedNodes = nodes.get().sorted()
        val output = dotFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(
            buildString {
                appendLine("digraph G {")
                sortedNodes.forEach { node -> appendLine("""  "$node";""") }
                parsedEdges.sortedWith(compareBy({ it.first }, { it.second }))
                    .forEach { (from, to) ->
                        // Exclude dependency injection dependencies graphs
                        if (from != ":app:di") {
                            appendLine("""  "$from" -> "$to";""")
                        }
                    }
                appendLine("}")
            },
        )
        logger.lifecycle("Wrote DOT graph to ${output.absolutePath}")
    }
}

@Suppress("unused")
class DependencyGraphPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) return

        target.tasks.register<GenerateProjectDependencyGraph>("generateProjectDependencyGraph") {
            group = "reporting"
            description = "Generates a simple DOT graph of inter-project dependencies."

            val edgeSet = linkedSetOf<Pair<String, String>>()
            val nodeSet = linkedSetOf<String>()

            target.rootProject.allprojects.forEach { project ->
                project.configurations.forEach { configuration ->
                    configuration.dependencies.withType(ProjectDependency::class.java)
                        .forEach { dependency ->
                            val from = project.path
                            val to = dependency.copy().path
                            if (from == to) return@forEach // Safety: plugin wiring can add reflexive configs
                            edgeSet += from to to
                            nodeSet += from
                            nodeSet += to
                        }
                }
            }

            edges.set(edgeSet.map { "${it.first}||${it.second}" })
            nodes.set(nodeSet.toList())
            dotFile.set(target.layout.buildDirectory.file("reports/dependency-graph/project-dependencies.dot"))
        }
    }
}
