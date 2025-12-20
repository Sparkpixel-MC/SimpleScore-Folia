repositories {
    maven("https://repo.menthamc.org/repository/maven-public/")
}
dependencies {
    api(project(":core"))
    compileOnly("fun.bm.lophine:lophine-api:1.21.11-R0.1-SNAPSHOT")
    implementation(project(":bukkit:protocol"))
    implementation(project(":bukkit:worldguard"))

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