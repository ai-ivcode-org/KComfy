package org.ivcode.gradle.s3mvn

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

/**
 * Plugin to publish artifacts to GitHub Maven Repository
 */
class S3MvnPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("maven-publish")
        project.extensions.create("s3mvn", S3MvnPluginExtension::class.java)

        project.afterEvaluate {
            val extension = project.extensions.findByType(S3MvnPluginExtension::class.java)
                ?: return@afterEvaluate

            val url = extension.url ?: throw IllegalArgumentException("url is required")
            val awsKey = extension.getAwsKeyOrDefault(project)
            val awsSecret = extension.getAwsSecretOrDefault(project)

            project.extensions.configure(PublishingExtension::class.java) {
                publications {
                    create("s3", MavenPublication::class.java) {
                        groupId = project.group.toString()
                        artifactId = project.name
                        version = project.version.toString()

                        val componentName = if (project.plugins.hasPlugin("java")) "java" else "kotlin"
                        from(project.components.getByName(componentName))
                    }
                }

                repositories {
                    maven {
                        this.name = "s3"
                        this.url = url

                        if(awsKey != null && awsSecret != null) {
                            credentials (AwsCredentials::class.java) {
                                accessKey = awsKey
                                secretKey = awsSecret
                            }
                        }
                    }
                }
            }
        }
    }
}