plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.Slimefun:Slimefun4:RC-37")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    archiveFileName.set("InsurancePlugin-${version}.jar")
    minimize()
}