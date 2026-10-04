/*
 * This file is part of Terra.
 *
 * Terra is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Terra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Terra.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dfsek.terra.bukkit;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.dfsek.terra.api.command.CommandSender;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.event.events.platform.CommandRegistrationEvent;
import com.dfsek.terra.api.event.events.platform.PlatformInitializationEvent;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.dfsek.terra.bukkit.listeners.CommonListener;
import com.dfsek.terra.bukkit.util.PaperUtil;
import com.dfsek.terra.bukkit.util.VersionUtil;
import com.dfsek.terra.bukkit.world.BukkitAdapter;


public class TerraBukkitPlugin extends JavaPlugin {
    private static final Logger logger = LoggerFactory.getLogger(TerraBukkitPlugin.class);
    private final Map<String, com.dfsek.terra.api.world.chunk.generation.ChunkGenerator> generatorMap = new HashMap<>();
    private final Map<String, String> generatorPacks = new HashMap<>();
    private com.dfsek.terra.bukkit.util.ServerStallMonitor stallMonitor;
    private com.dfsek.terra.bukkit.util.ConsoleCapture consoleCapture;
    private io.papermc.paper.threadedregions.scheduler.ScheduledTask heartbeatTask;
    private PlatformImpl platform;
    private org.terra2.core.GenerationManager<com.dfsek.terra.api.block.state.BlockState, com.dfsek.terra.api.world.biome.Biome> core;
    private volatile YamlConfiguration generationSettings;
    private String primaryWorldName;
    private AsyncScheduler asyncScheduler = this.getServer().getAsyncScheduler();

    private GlobalRegionScheduler globalRegionScheduler = this.getServer().getGlobalRegionScheduler();

    @Override
    public void onEnable() {
        com.dfsek.terra.bukkit.util.GenerationReport.initialize(getDataFolder().toPath(),
            "Plugin: " + getDescription().getVersion() + "; Server: " + Bukkit.getVersion()
                + "; Java: " + System.getProperty("java.version"));
        File settingsFile = new File(getDataFolder(), "terra2-settings.yml");
        if(!settingsFile.exists()) saveResource("terra2-settings.yml", false);
        try {
            generationSettings = com.dfsek.terra.bukkit.util.GenerationSettings.load(settingsFile);
        } catch(java.io.IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            logger.error("Invalid Terra2 settings; disabling plugin", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        java.util.Properties serverProperties = new java.util.Properties();
        try(var input = java.nio.file.Files.newInputStream(java.nio.file.Path.of("server.properties"))) {
            serverProperties.load(input);
            primaryWorldName = serverProperties.getProperty("level-name", "world");
        } catch(java.io.IOException e) {
            logger.error("Cannot identify the primary world; disabling Terra2.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        core = new org.terra2.core.GenerationManager<>(java.util.Set.of(primaryWorldName, primaryWorldName + "_nether", primaryWorldName + "_the_end"));
        if(!doVersionCheck()) {
            return;
        }

        platform = NMSInitializer.init(this);
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
            public void bindWorld(org.bukkit.event.world.WorldInitEvent event) {
                if(event.getWorld().getGenerator() instanceof BukkitChunkGeneratorWrapper)
                    bindCoreDimension(event.getWorld());
            }
        }, this);
        if(platform == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        platform.getEventManager().callEvent(new PlatformInitializationEvent());

        try {
            PaperCommandManager<CommandSender> commandManager = getCommandSenderPaperCommandManager();

            platform.getEventManager().callEvent(new CommandRegistrationEvent(commandManager));

        } catch(Exception e) { // This should never happen.
            logger.error("""
                         TERRA HAS BEEN DISABLED
                         
                         Errors occurred while registering commands.
                         Please report this to Terra.
                         """.strip(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new CommonListener(platform), this); // Register master event listener
        PaperUtil.checkPaper(this);
        stallMonitor = new com.dfsek.terra.bukkit.util.ServerStallMonitor();
        heartbeatTask = globalRegionScheduler.runAtFixedRate(this, task -> stallMonitor.heartbeat(), 1, 20);
        consoleCapture = new com.dfsek.terra.bukkit.util.ConsoleCapture(getDataFolder().toPath().resolve("reports"),
            java.nio.file.Path.of("logs", "latest.log"), "Plugin: " + getDescription().getVersion() + "; Server: " + Bukkit.getVersion());
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
            public void playerCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
                captureMultiverseCommand(event.getMessage(), event.getPlayer().getName());
            }
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
            public void serverCommand(org.bukkit.event.server.ServerCommandEvent event) {
                captureMultiverseCommand(event.getCommand(), event.getSender().getName());
            }
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
            public void remoteCommand(org.bukkit.event.server.RemoteServerCommandEvent event) {
                captureMultiverseCommand(event.getCommand(), event.getSender().getName());
            }
        }, this);
        logger.info("Terra2 diagnostics enabled; reports directory: {}", getDataFolder().toPath().resolve("reports"));
    }

    @Override
    public void onDisable() {
        if(heartbeatTask != null) heartbeatTask.cancel();
        if(stallMonitor != null) stallMonitor.close();
        if(consoleCapture != null) consoleCapture.close();
    }

    @NotNull
    private PaperCommandManager<CommandSender> getCommandSenderPaperCommandManager() throws Exception {
        PaperCommandManager<CommandSender> commandManager = PaperCommandManager.builder(SenderMapper.create(
                BukkitAdapter::adapt,
                BukkitAdapter::adapt
            ))
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildOnEnable(this);

        commandManager.brigadierManager().setNativeNumberSuggestions(false);

        return commandManager;
    }

    public PlatformImpl getPlatform() {
        return platform;
    }

    @SuppressWarnings({ "deprecation", "AccessOfSystemProperties" })
    private boolean doVersionCheck() {
        logger.info("Running on Minecraft version {} with server implementation {}.", VersionUtil.getMinecraftVersionInfo(),
            Bukkit.getServer().getName());

        if(!VersionUtil.getSpigotVersionInfo().isSpigot())
            logger.error("YOU ARE RUNNING A CRAFTBUKKIT OR BUKKIT SERVER. PLEASE UPGRADE TO PAPER.");

        if(!VersionUtil.getSpigotVersionInfo().isPaper())
            logger.error("YOU ARE RUNNING A SPIGOT SERVER. PLEASE UPGRADE TO PAPER.");

        if(VersionUtil.getSpigotVersionInfo().isMohist()) {
            if(System.getProperty("IKnowMohistCausesLotsOfIssuesButIWillUseItAnyways") == null) {
                Runnable runnable = () -> { // scary big block of text
                    logger.error("""
                                 .----------------------------------------------------------------------------------.
                                 |                                                                                  |
                                 |                                ⚠ !! Warning !! ⚠                                 |
                                 |                                                                                  |
                                 |                         You are currently using Mohist.                          |
                                 |                                                                                  |
                                 |                                Do not use Mohist.                                |
                                 |                                                                                  |
                                 |   The concept of combining the rigid Bukkit platform, which assumes a 100%       |
                                 |   Vanilla server, with the flexible Forge platform, which allows changing        |
                                 |   core components of the game, simply does not work. These platforms are         |
                                 |   incompatible at a conceptual level, the only way to combine them would         |
                                 |   be to make incompatible changes to both. As a result, Mohist's Bukkit          |
                                 |   API implementation is not compliant. This will cause many plugins to           |
                                 |   break. Rather than fix their platform, Mohist has chosen to distribute         |
                                 |   unofficial builds of plugins they deem to be "fixed". These builds are not     |
                                 |   "fixed", they are simply hacked together to work with Mohist's half-baked      |
                                 |   Bukkit implementation. To distribute these as "fixed" versions implies that:   |
                                 |       - These builds are endorsed by the original developers. (They are not)     |
                                 |       - The issue is on the plugin's end, not Mohist's. (It is not. The issue    |
                                 |       is that Mohist chooses to not create a compliant Bukkit implementation)    |
                                 |   Please, do not use Mohist. It causes issues with most plugins, and rather      |
                                 |   than fixing their platform, Mohist has chosen to distribute unofficial         |
                                 |   hacked-together builds of plugins, calling them "fixed". If you want           |
                                 |   to use a server API with Forge mods, look at the Sponge project, an            |
                                 |   API that is designed to be implementation-agnostic, with first-party           |
                                 |   support for the Forge mod loader. You are bound to encounter issues if         |
                                 |   you use Terra with Mohist. We will provide NO SUPPORT for servers running      |
                                 |   Mohist. If you wish to proceed anyways, you can add the JVM System Property    |
                                 |   "IKnowMohistCausesLotsOfIssuesButIWillUseItAnyways" to enable the plugin. No   |
                                 |   support will be provided for servers running Mohist.                           |
                                 |                                                                                  |
                                 |                   Because of this **TERRA HAS BEEN DISABLED**.                   |
                                 |                    Do not come ask us why it is not working.                     |
                                 |                                                                                  |
                                 |----------------------------------------------------------------------------------|
                                 """.strip());
                };
                runnable.run();
                asyncScheduler.runDelayed(this, task -> runnable.run(), 200L, TimeUnit.SECONDS);
                // Bukkit.shutdown(); // we're not *that* evil
                Bukkit.getPluginManager().disablePlugin(this);
                return false;
            } else {
                logger.warn("""
                            You are using Mohist, so we will not give you any support for issues that may arise.
                            Since you enabled the "IKnowMohistCausesLotsOfIssuesButIWillUseItAnyways" flag, we won't disable Terra. But be warned.
                            
                            > I felt a great disturbance in the JVM, as if millions of plugins suddenly cried out in stack traces and were suddenly silenced.
                            > I fear something terrible has happened.
                            > - Astrash
                            """.strip());
            }
        }
        return true;
    }

    public void bindCoreDimension(org.bukkit.World world) {
        core.bindDimension(world.getName(), world.getKey().toString());
        logger.info("Terra2 core bound {} to {} using {}", world.getName(), world.getKey(),
            core.binding(world.getName()).plan().identity());
    }

    @Override
    public @Nullable
    synchronized ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        if(id == null || id.trim().isEmpty()) { return null; }
        try {
        assertGenerationAuthorized(worldName, id);
        if(platform == null) throw new IllegalStateException("Terra2 engine is not initialized");
        return new BukkitChunkGeneratorWrapper(generatorMap.computeIfAbsent(worldName, name -> {
            ConfigPack pack = platform.getConfigRegistry().getByID(id).orElseThrow(
                () -> new IllegalArgumentException("No such config pack \"" + id + "\""));
            var legacy = pack.getGeneratorProvider().newInstance(pack);
            var plan = org.terra2.adapter.terra.TerraPlanCompiler.compile(pack, legacy);
            core.authorize(new org.terra2.core.WorldTarget(name, null), plan);
            var generator = new org.terra2.adapter.terra.CoreTerraGenerator(core, name, legacy);
            generatorPacks.put(worldName, id);
            return generator;
        }), platform.getRawConfigRegistry().getByID(id).orElseThrow(), platform.getWorldHandle().air(), platform.usesNativeBiomeProvider());
        } catch(RuntimeException | LinkageError e) {
            com.dfsek.terra.bukkit.util.GenerationReport.failure("generator-request", worldName, id, e);
            throw e;
        }
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command,
                             String label, String[] args) {
        if(command.getName().equalsIgnoreCase("terra2reportlog")) {
            if(!sender.hasPermission("terra2.diagnostics.capture")) {
                sender.sendMessage("Sem permissão: terra2.diagnostics.capture"); return true;
            }
            String action = args.length == 0 ? "start" : args[0].toLowerCase(java.util.Locale.ROOT);
            if(args.length > 1 || !java.util.Set.of("start", "stop", "status").contains(action)) {
                sender.sendMessage("Uso: /terra2reportlog [start|stop|status]"); return true;
            }
            if(consoleCapture == null) { sender.sendMessage("Captura indisponível: Terra2 ainda não inicializado."); return true; }
            try {
                switch(action) {
                    case "start" -> sender.sendMessage("Terra2: captura iniciada: " + consoleCapture.start("/terra2reportlog", sender.getName()));
                    case "stop" -> sender.sendMessage("Terra2: captura encerrada: " + consoleCapture.stop("Comando manual"));
                    case "status" -> sender.sendMessage("Terra2: " + consoleCapture.status());
                }
            } catch(java.io.IOException error) {
                sender.sendMessage("Terra2: falha ao salvar captura. Consulte o console."); logger.error("Could not start/stop console capture", error);
            }
            return true;
        }
        if(!command.getName().equalsIgnoreCase("terra2")) return false;
        if(!sender.hasPermission("terra2.settings.reload")) {
            sender.sendMessage("Sem permissão: terra2.settings.reload");
            return true;
        }
        if(args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Uso: /terra2 reload");
            return true;
        }
        try {
            reloadGenerationSettings();
            sender.sendMessage("Terra2: autorizações recarregadas. Mundos já carregados mantêm seus geradores.");
        } catch(Exception e) {
            sender.sendMessage("Terra2: configuração recusada; autorizações anteriores mantidas. " + e.getMessage());
            logger.error("Could not reload Terra2 generation settings", e);
        }
        return true;
    }

    private void captureMultiverseCommand(String command, String sender) {
        String label = command.stripLeading().split("\\s+", 2)[0].replaceFirst("^/", "").toLowerCase(java.util.Locale.ROOT);
        int namespace = label.lastIndexOf(':');
        if(namespace >= 0) label = label.substring(namespace + 1);
        if(!label.equals("mv") && !label.equals("multiverse")) return;
        if(consoleCapture == null) return;
        try { consoleCapture.start(command, sender); }
        catch(java.io.IOException error) { logger.error("Could not capture Multiverse command", error); }
    }

    public synchronized void reloadGenerationSettings() throws Exception {
        var next = com.dfsek.terra.bukkit.util.GenerationSettings.load(new File(getDataFolder(), "terra2-settings.yml"));
        var worlds = next.getConfigurationSection("worlds");
        for(String world : worlds.getKeys(false)) {
            String pack = next.getString("worlds." + world + ".pack");
            if(platform.getConfigRegistry().getByID(pack).isEmpty())
                throw new IllegalArgumentException("Pack não carregado: " + pack);
        }
        for(String world : generatorMap.keySet()) {
            String oldPack = generatorPacks.get(world);
            String newPack = next.getString("worlds." + world + ".pack");
            if(newPack != null && !java.util.Objects.equals(oldPack, newPack))
                throw new IllegalArgumentException("Não é possível trocar o pack do gerador existente: " + world);
        }
        generationSettings = next;
    }

    public void assertGenerationAuthorized(String worldName, String id) {
        YamlConfiguration settings = generationSettings;
        if(settings == null || !settings.getBoolean("generation.enabled", false)) {
            throw new IllegalStateException("Terra2 generation is disabled; refusing generator request for " + worldName);
        }
        String authorizedPack = settings.getString("worlds." + worldName + ".pack");
        if(!id.equals(authorizedPack)) {
            throw new IllegalArgumentException("World/pack pair is not authorized: " + worldName + "/" + id);
        }
        if(settings.getStringList("generation.protected-worlds").stream()
                .anyMatch(name -> name.equalsIgnoreCase(worldName))) {
            throw new IllegalArgumentException("Terra2 refuses generation in protected world " + worldName);
        }
        if(primaryWorldName == null || worldName.equalsIgnoreCase(primaryWorldName)
                || worldName.equalsIgnoreCase(primaryWorldName + "_nether")
                || worldName.equalsIgnoreCase(primaryWorldName + "_the_end")) {
            throw new IllegalArgumentException("Terra2 refuses generation in the primary world or its vanilla dimensions");
        }
        org.bukkit.World loaded = Bukkit.getWorld(worldName);
        if(loaded != null && java.util.Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end")
                .contains(loaded.getKey().toString())) {
            throw new IllegalArgumentException("Terra2 refuses generation in vanilla dimension " + loaded.getKey());
        }
    }

    public AsyncScheduler getAsyncScheduler() {
        return asyncScheduler;
    }

    public GlobalRegionScheduler getGlobalRegionScheduler() {
        return globalRegionScheduler;
    }
}
