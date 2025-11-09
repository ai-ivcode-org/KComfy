package org.ivcode.gradle.s3mvn

import org.gradle.api.Project
import java.net.URI

open class S3MvnPluginExtension {
    var url: URI? = null
    var awsKey: String? = null
    var awsSecret: String? = null
}

internal fun S3MvnPluginExtension.getAwsSecretOrDefault(project: Project): String? =
    awsSecret
        ?: project.findProperty("AWS_SECRET_ACCESS_KEY") as String?
        ?: System.getenv("AWS_SECRET_ACCESS_KEY")

internal fun S3MvnPluginExtension.getAwsKeyOrDefault(project: Project): String? =
    awsKey
        ?: project.findProperty("AWS_ACCESS_KEY_ID") as String?
        ?: System.getenv("AWS_ACCESS_KEY_ID")