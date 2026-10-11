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
        if(args.length != 2 || !Set.of("status", "export", "install", "pack", "prepare", "preview", "stop", "disable").contains(args[1].toLowerCase(Locale.ROOT))) {
            sender.sendMessage("Uso: /terra2 bedrock <status|export|pack|prepare|preview|stop|disable>"); return true;
        }
        Plugin geyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
        var bridge=plugin.bedrockPreview();
        if(Set.of("prepare","preview","stop","disable").contains(args[1].toLowerCase(Locale.ROOT))) {
            try {
                if(bridge==null)throw new IllegalStateException("Preview module not initialized");
                if(args[1].equalsIgnoreCase("prepare")) {
                    bridge.prepare();sender.sendMessage("Teste Bedrock autorizado para o próximo início. Reinicie com o Geyser oficial; o Terra2 registrará e enviará seu pack automaticamente.");
                } else if(args[1].equalsIgnoreCase("disable")) {
                    bridge.disable();sender.sendMessage("Prévia desativada e autorização removida. Reinicie para retirar os registros e o pack desta sessão do Geyser.");
                } else if(sender instanceof org.bukkit.entity.Player player) {
                    if(args[1].equalsIgnoreCase("stop")){bridge.stop(player,true);sender.sendMessage("Prévia encerrada; aparência real restaurada.");}
                    else sender.sendMessage(bridge.start(player));
                } else sender.sendMessage("Execute preview/stop em jogo pelo jogador Bedrock.");
            } catch(Exception error){sender.sendMessage("Prévia recusada: "+error.getMessage());}
            return true;
        }
        if(args[1].equalsIgnoreCase("pack")) {
            if(bridge!=null && bridge.status().startsWith("READY_OPT_IN")) {
                sender.sendMessage("A paleta registrada nesta sessão está congelada em plugins/Terra2/bedrock/terra2-palette.mcpack. Reinicie para atualizar cores/providers.");
                return true;
            }
            try {
                var profiles = new java.util.ArrayList<>(BedrockVisualPack.fromBiomes(plugin.platform().bedrockBiomeAppearances()));
                for(var registration : Bukkit.getServicesManager().getRegistrations(org.terra2.api.visual.VisualPaletteProvider.class)) {
                    var provider = registration.getProvider();
                    if(provider.apiVersion() != 1) throw new IllegalArgumentException("Unsupported visual provider API");
                    profiles.addAll(provider.profiles());
                }
                var target = plugin.getDataFolder().toPath().resolve("bedrock/terra2-palette.mcpack");
                BedrockVisualPack.write(target, profiles);
                var index = BedrockVisualPack.index(profiles);
                sender.sendMessage("Paleta RGB gerada: " + target + "; cores exatas: " + index.get("exact_color_count")
                    + "; perfis: " + index.getAsJsonObject("profiles").size());
                sender.sendMessage("Atlas preparatório. Aplicação por posição ainda BLOCKED; copiar o pack não ativa cores de biomas. Nenhum mapping/world foi alterado.");
            } catch(Exception error) {
                sender.sendMessage("Falha ao preparar paleta: " + error.getMessage());
                plugin.getLogger().warning("Terra2 palette: " + error.getMessage());
            }
            return true;
        }
        if(args[1].equalsIgnoreCase("install")) {
            sender.sendMessage("Instalação experimental retirada. Mantenha o Geyser oficial; use pack para preparar a paleta genérica. A aplicação por posição ainda não está disponível.");
            return true;
        }
        if(args[1].equalsIgnoreCase("status")) {
            sender.sendMessage("Terra2 Bedrock: " + (geyser == null ? "GEYSER_ABSENT" : supportsBiomes(geyser)
                ? "CUSTOM_BIOME_API_PRESENT (registro e cliente ainda precisam ser validados)" : "UNSUPPORTED_CUSTOM_BIOMES")
                + "; exportação: plugins/Terra2/bedrock/terra2-biomes.json");
            sender.sendMessage("Prévia por posição: "+(bridge==null?"DISABLED":bridge.status()));
            if(sender instanceof org.bukkit.entity.Player player && bridge!=null)sender.sendMessage(bridge.playerStatus(player));
            if(supportsBiomes(geyser)) {
                try {
                    var registryType = Class.forName("org.geysermc.geyser.registry.Registries", false, geyser.getClass().getClassLoader());
                    Object registry = registryType.getField("CUSTOM_BIOMES").get(null);
                    var mappings = (java.util.Map<?, ?>) registry.getClass().getMethod("get").invoke(registry);
                    long count = mappings.keySet().stream().filter(id -> id.toString().startsWith("terra:")).count();
                    sender.sendMessage("Mappings Terra registrados no Geyser: " + count + "; pack gerado: "
                        + java.nio.file.Files.isRegularFile(geyser.getDataFolder().toPath().resolve("cache/custom_biomes.mcpack")));
                } catch(ReflectiveOperationException | LinkageError error) {
                    sender.sendMessage("Não foi possível consultar o registro desta versão do Geyser; confirme no console após reiniciar.");
                }
            }
            if(sender instanceof org.bukkit.entity.Player player) {
                var world = player.getWorld(); var location = player.getLocation();
                sender.sendMessage("Mundo: " + world.getName() + "; bioma Bukkit no local: " + world.getBiome(location).getKey());
                if(world.getGenerator() instanceof com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper wrapper)
                    sender.sendMessage("Pack: " + wrapper.getPack().getID() + "; projeção vanilla: "
                        + (wrapper.biomeCompatibility() != null && wrapper.biomeCompatibility().vanilla));
            }
            return true;
        }
        try {
            var document = BedrockMappings.document(plugin.platform().bedrockBiomeAppearances());
            var exported = plugin.getDataFolder().toPath().resolve("bedrock/terra2-biomes.json");
            BedrockMappings.write(exported, document);
            sender.sendMessage("Exportados " + document.getAsJsonObject("biomes").size() + " biomas: " + exported);
        } catch(Exception error) {
            sender.sendMessage("Falha na integração Bedrock: " + error.getMessage());
            plugin.getLogger().warning("Terra2 Bedrock: " + error.getMessage());
        }
        return true;
    }
}
