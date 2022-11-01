import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // kotlin("jvm") version "1.5.10"
    kotlin("jvm") version "1.7.20"
    application
}

group = "net.finalatomicbuster"
version = "1.0-SNAPSHOT"

repositories {
    // jcenter()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.javalin:javalin:5.1.3")
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation(files("lib/fit.jar"))
    implementation("io.ktor:ktor-client-core:2.1.3")
    implementation("io.ktor:ktor-client-cio:2.1.3")
    implementation("io.ktor:ktor-client-logging:2.1.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("ServerKt")
}