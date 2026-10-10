plugins {
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper") version Versions.Bukkit.runPaper
}

dependencies {
    // Required for :platforms:bukkit:runDevBundleServer task
    paperweight.paperDevBundle(Versions.Bukkit.paperDevBundle)

    shaded(project(":platforms:bukkit:common"))
    // Paper 26.1+ ships unobfuscated: package the runtime variant, never reobf.
    shaded(project(path = ":platforms:bukkit:nms", configuration = "runtimeElements"))
    shaded("xyz.jpenilla", "reflection-remapper", Versions.Bukkit.reflectionRemapper)
}

tasks {
    shadowJar {
        archiveFileName.set("Terra2-bukkit-7.0.24-BETA.jar")
        relocate("io.papermc.lib", "com.dfsek.terra.lib.paperlib")
        // Native Minecraft codecs use JsonOps with the server's Gson JsonElement identity.
        // Relocating these call sites produces incompatible JSON objects in the shaded runtime.
        relocate("com.google.common", "com.dfsek.terra.lib.google.common")
        relocate("org.apache.logging.slf4j", "com.dfsek.terra.lib.slf4j-over-log4j")
        exclude("org/slf4j/**")
        exclude("org/checkerframework/**")
        exclude("org/jetbrains/annotations/**")
        exclude("org/intellij/**")
        exclude("com/google/errorprone/**")
        exclude("com/google/j2objc/**")
        exclude("javax/**")
    }

    runServer {
        minecraftVersion(Versions.Bukkit.minecraft)
        dependsOn(shadowJar)
        pluginJars(shadowJar.get().archiveFile)

        downloadPlugins {
            modrinth("viaversion", "5.5.0")
            modrinth("viabackwards", "5.5.0")
        }
    }
}


addonDir(project.file("./run/plugins/Terra/addons"), tasks.named("runServer").get())
