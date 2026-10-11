package com.dfsek.terra.bukkit.bedrock;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import java.util.Locale;
import java.util.Set;

/** Optional, capability-based file integration; no Geyser dependency in the engine. */
public final class BedrockCommands {
    private BedrockCommands() {}
    public static boolean supportsBiomes(Plugin geyser) {
        if(geyser == null || !geyser.isEnabled()) return false;
        try {
            var loader = geyser.getClass().getClassLoader();
            Class.forName("org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBiomesEvent", false, loader);
            Class.forName("org.geysermc.geyser.registry.mappings.versions.biome.BiomeMappingsReader_v1", false, loader);
            Class.forName("org.geysermc.geyser.pack.CustomBiomeResourcePackManager", false, loader);
            return true;
        } catch(ClassNotFoundException | LinkageError unsupported) { return false; }
    }
    public static boolean handle(TerraBukkitPlugin plugin, CommandSender sender, String[] args) {
        if(args.length == 0 || !args[0].equalsIgnoreCase("bedrock")) return false;
        if(args.length != 2 || !Set.of("status", "export", "install").contains(args[1].toLowerCase(Locale.ROOT))) {
            sender.sendMessage("Uso: /terra2 bedrock <status|export|install>"); return true;
        }
        Plugin geyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
        if(args[1].equalsIgnoreCase("status")) {
            sender.sendMessage("Terra2 Bedrock: " + (geyser == null ? "GEYSER_ABSENT" : supportsBiomes(geyser)
                ? "CUSTOM_BIOME_API_PRESENT (registro e cliente ainda precisam ser validados)" : "UNSUPPORTED_CUSTOM_BIOMES")
                + "; exportação: plugins/Terra2/bedrock/terra2-biomes.json");
            sender.sendMessage("Cores precisam dos IDs personalizados nos chunks. VANILLA_COMPATIBLE ou outro plugin alterando biomas impede esta primeira integração.");
            return true;
        }
        boolean install = args[1].equalsIgnoreCase("install");
        if(install && !supportsBiomes(geyser)) {
            sender.sendMessage("Instalação recusada: o Geyser carregado não expõe suporte a mappings de biomas. Use export para preparar o arquivo; teste uma build compatível com PR #6658.");
            return true;
        }
        try {
            var document = BedrockMappings.document(plugin.platform().bedrockBiomeAppearances());
            var exported = plugin.getDataFolder().toPath().resolve("bedrock/terra2-biomes.json");
            BedrockMappings.write(exported, document);
            sender.sendMessage("Exportados " + document.getAsJsonObject("biomes").size() + " biomas: " + exported);
            if(install) {
                var target = geyser.getDataFolder().toPath().resolve("custom_mappings/terra2-biomes.json");
                BedrockMappings.write(target, document);
                sender.sendMessage("Mapping instalado: " + target + ". Reinicie o servidor e aceite o pack visual no Bedrock; não regenere mundos para aplicar cores.");
            }
        } catch(Exception error) {
            sender.sendMessage("Falha na integração Bedrock: " + error.getMessage());
            plugin.getLogger().warning("Terra2 Bedrock: " + error.getMessage());
        }
        return true;
    }
}
