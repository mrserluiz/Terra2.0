repositories {

}

dependencies {
    shadedApi(project(":common:implementation:terra2-core"))
    shadedApi(project(":common:implementation:vanilla-adapter"))
    shadedApi("com.google.code.gson", "gson", Versions.Libraries.Internal.gson)
    shadedApi(project(":common:implementation:base"))

    compileOnly("io.papermc.paper", "paper-api", Versions.Bukkit.paper)
    testImplementation("io.papermc.paper", "paper-api", Versions.Bukkit.paper)

    compileOnly("org.mvplugins.multiverse.core", "multiverse-core", Versions.Bukkit.multiverse)

    shadedApi("io.papermc", "paperlib", Versions.Bukkit.paperLib)

    shadedApi("com.google.guava", "guava", Versions.Libraries.Internal.guava)

    shadedApi("org.incendo", "cloud-paper", Versions.Bukkit.cloud)
}

// Ship the same reviewable demo used in the documentation; never auto-authorize it.
tasks.named<ProcessResources>("processResources") {
    from(rootProject.file("examples/datapacks")) { into("datapacks") }
}
