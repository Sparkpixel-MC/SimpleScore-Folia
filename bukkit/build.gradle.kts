dependencies {
    api(project(":core"))
    implementation(project(":bukkit:protocol"))
    implementation(project(":bukkit:worldguard"))

    compileOnly("dev.folia:folia-api:1.21.6-R0.1-SNAPSHOT")
    compileOnly(libs.papi)
    compileOnly(libs.adventureMiniMessage)
    compileOnly(libs.adventureSerializer)

    implementation(libs.bStatsBukkit)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}