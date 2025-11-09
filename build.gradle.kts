import org.ivcode.gradle.s3mvn.utils.isSnapshot
import java.net.URI

plugins {
    kotlin("jvm") version "2.1.10"
    id("s3mvn")
}

group = "org.ivcode"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

s3mvn {
    val directory = if (isSnapshot(project.version.toString())) "snapshot" else "release"
    url = URI("s3://maven.ivcode.org/$directory/")
}

dependencies {
    // SLF4J Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.9")

    // Mustache
    implementation("com.github.spullara.mustache.java:compiler:0.9.14")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.11.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.1")


    // Kotlin reflection (needed when you want to read KClass annotations, properties, etc.)
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}