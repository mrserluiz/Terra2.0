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
    private org.terra2.adapter.vanilla.paper.DatapackRuntime datapacks;
    private org.terra2.pack.TerraPackStore terraPacks;
    private final java.util.concurrent.atomic.AtomicBoolean converting = new java.util.concurrent.atomic.AtomicBoolean();
    private final Map<String, java.util.List<org.bukkit.generator.BlockPopulator>> extraPopulators = new HashMap<>();
    private final Map<String, String> compositionVersions = new HashMap<>();
    private org.terra2.core.GenerationManager<com.dfsek.terra.api.block.state.BlockState, com.dfsek.terra.api.world.biome.Biome> core;
    private volatile YamlConfiguration generationSettings;
    private String primaryWorldName;
    private org.terra2.adapter.vanilla.paper.WorldLootManager lootManager;
    public YamlConfiguration generationSettings() { return generationSettings; }
    public org.terra2.pack.TerraPackStore terraPackStore() { return terraPacks; }
    public PlatformImpl platform() { return platform; }
    public org.terra2.adapter.vanilla.paper.WorldLootManager lootManager() { return lootManager; }
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
        try {
            for(String resource : java.util.List.of("datapacks/terra2-flat-demo/pack.mcmeta",
                    "datapacks/terra2-flat-demo/data/terra2_demo/dimension/flat.json")) {
                if(!new File(getDataFolder(), resource).exists()) saveResource(resource, false);
            }
            terraPacks = new org.terra2.pack.TerraPackStore(getDataFolder().toPath());
            for(String resource : java.util.List.of("conversion/input/flat-demo/pack.mcmeta",
                    "conversion/input/flat-demo/data/terra2_demo/dimension/flat.json",
                    "conversion/input/scatter-demo/pack.mcmeta",
                    "conversion/input/scatter-demo/data/terra2_demo/worldgen/configured_feature/debris.json",
                    "conversion/input/scatter-demo/data/terra2_demo/worldgen/placed_feature/debris.json"))
                if(!new File(getDataFolder(), resource).exists()) saveResource(resource, false);
            datapacks = new org.terra2.adapter.vanilla.paper.DatapackRuntime(getDataFolder().toPath().resolve("datapacks"),
                java.util.Set.of(primaryWorldName, primaryWorldName + "_nether", primaryWorldName + "_the_end"));
        } catch(java.io.IOException error) {
            logger.error("Cannot initialize private datapack directory", error);
            Bukkit.getPluginManager().disablePlugin(this); return;
        }
        if(!doVersionCheck()) {
            return;
        }

        platform = NMSInitializer.init(this);
        if(platform != null && platform.nativePackBackend() != null) terraPacks.nativeBackend(platform.nativePackBackend());
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
            public void bindWorld(org.bukkit.event.world.WorldInitEvent event) {
                if(event.getWorld().getGenerator() instanceof org.terra2.adapter.vanilla.paper.FlatChunkGenerator flat) {
                    try {
                        assertGenerationAuthorized(event.getWorld().getName(), generationSettings.isString("worlds." + event.getWorld().getName() + ".datapack") ? "DATAPACK" : "PACKS");
                        flat.bind(event.getWorld());
                        logger.info("Terra2 datapack core bound {} to {} using {}", event.getWorld().getName(),
                            event.getWorld().getKey(), flat.selection());
                    } catch(Exception error) {
                        com.dfsek.terra.bukkit.util.GenerationReport.failure("datapack-world-init", event.getWorld().getName(), flat.selection(), error);
                        throw new IllegalStateException("Datapack world initialization refused", error);
                    }
                }
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
            lootManager = new org.terra2.adapter.vanilla.paper.WorldLootManager(this,
                org.terra2.core.LootOriginKeyStore.load(getDataFolder().toPath().resolve("loot/origin.key")));
            Bukkit.getPluginManager().registerEvents(lootManager, this);
            if(generationSettings.getBoolean("generation.enabled")) {
                for(String world : generationSettings.getConfigurationSection("worlds").getKeys(false)) {
                    if(generationSettings.isString("worlds." + world + ".datapack")) continue;
                    try {
                        assertGenerationAuthorized(world, "PACKS");
                        platform.prepareNativePacks(world, composition(generationSettings, world).packs());
                    } catch(Exception refused) {
                        com.dfsek.terra.bukkit.util.GenerationReport.failure("native-startup-preflight", world,
                            com.dfsek.terra.bukkit.util.GenerationSettings.packSelection(generationSettings, world), refused);
                    }
                }
            }
            Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
                public void bindLoot(org.bukkit.event.world.WorldInitEvent event) {
                    if(event.getWorld().getGenerator() instanceof BukkitChunkGeneratorWrapper)
                        lootManager.bind(event.getWorld(), platform.nativeLootTables(event.getWorld().getName()));
                }
            }, this);
        } catch(Exception error) {
            logger.error("Cannot initialize persistent loot manager", error);
            Bukkit.getPluginManager().disablePlugin(this); return;
        }

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
        if(compositionVersions.containsKey(world.getName())) {
            try {
                new org.terra2.adapter.vanilla.DatapackManifest(world.getName(), world.getKey().toString(), world.getSeed(),
                    core.binding(world.getName()).plan().identity(), generatorPacks.get(world.getName()), "terra-base", world.getMinHeight(), world.getMaxHeight())
                    .verifyOrCreate(world.getWorldFolder().toPath().resolve("terra2-generation.json"));
            } catch(Exception error) {
                com.dfsek.terra.bukkit.util.GenerationReport.failure("terrapack-manifest", world.getName(), generatorPacks.get(world.getName()), error);
                throw new IllegalStateException("TerraPack composition manifest refused", error);
            }
        }
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
        if(id.equals("DATAPACK")) {
            if(generatorMap.containsKey(worldName)) throw new IllegalArgumentException("World already has a Terra generator: " + worldName);
            try { return datapacks.prepare(worldName, generationSettings); }
            catch(java.io.IOException error) { throw new IllegalArgumentException("Cannot read datapack for " + worldName, error); }
        }
        if(platform == null) throw new IllegalStateException("Terra2 engine is not initialized");
        Composition composition;
        try { composition = composition(generationSettings, worldName); }
        catch(java.io.IOException error) { throw new IllegalArgumentException("Cannot load TerraPack composition", error); }
        if(composition.legacy() == null) {
            if(generatorMap.containsKey(worldName)) throw new IllegalArgumentException("World already has a Terra generator: " + worldName);
            return datapacks.preparePacks(worldName, composition.selection(), composition.packs());
        }
        if(datapacks.hasWorld(worldName)) throw new IllegalArgumentException("World already has a vanilla/TerraPack generator: " + worldName);
        ConfigPack pack = composition.legacy();
        String selected = composition.selection();
        try { platform.prepareNativePacks(worldName, composition.packs()); }
        catch(java.io.IOException error) { throw new IllegalArgumentException("Cannot restore native pack resources", error); }
        var delegate = generatorMap.computeIfAbsent(worldName, name -> {
            var legacy = pack.getGeneratorProvider().newInstance(pack);
            var plan = org.terra2.adapter.terra.TerraPlanCompiler.compile(pack, legacy);
            if(!composition.packs().isEmpty()) {
                plan = org.terra2.pack.PlanComposition.compose(selected, composition.version(), plan,
                    org.terra2.pack.TerraPackStore.features(composition.packs()), block -> {
                        var data = Bukkit.createBlockData(block);
                        if(!data.getMaterial().isSolid()) throw new IllegalArgumentException("Simple-block feature requires solid block: " + block);
                        return com.dfsek.terra.bukkit.world.block.data.BukkitBlockState.newInstance(data);
                    }, com.dfsek.terra.api.block.state.BlockState::isAir);
                compositionVersions.put(worldName, composition.version());
                extraPopulators.put(worldName, java.util.List.of(new org.terra2.adapter.vanilla.paper.CoreDecorationPopulator<>(
                    core, name, com.dfsek.terra.bukkit.world.block.data.BukkitBlockState::newInstance,
                    state -> (org.bukkit.block.data.BlockData) state.getHandle())));
            }
            core.authorize(new org.terra2.core.WorldTarget(name, null), plan);
            var generator = new org.terra2.adapter.terra.CoreTerraGenerator(core, name, legacy);
            generatorPacks.put(worldName, selected);
            return generator;
        });
        if(!selected.equals(generatorPacks.get(worldName)) || (compositionVersions.containsKey(worldName)
                && !composition.version().equals(compositionVersions.get(worldName))))
            throw new IllegalArgumentException("Cannot replace active TerraPack composition: " + worldName);
        var wrapper = new BukkitChunkGeneratorWrapper(delegate, pack, platform.getWorldHandle().air(), platform.usesNativeBiomeProvider());
        wrapper.setExtraPopulators(extraPopulators.getOrDefault(worldName, java.util.List.of()));
        return wrapper;
        } catch(RuntimeException | LinkageError e) {
            com.dfsek.terra.bukkit.util.GenerationReport.failure("generator-request", worldName, id, e);
            throw e;
        }
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command,
                             String label, String[] args) {
        if(command.getName().equalsIgnoreCase("terra2") && args.length == 2 && args[0].equalsIgnoreCase("loot") && args[1].equalsIgnoreCase("status")) {
            if(!sender.hasPermission("terra2.settings.reload")) { sender.sendMessage("Sem permissão"); return true; }
            sender.sendMessage(lootManager == null ? "Loot indisponível" : lootManager.status()); return true;
        }
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
        if(args.length >= 1 && args[0].equalsIgnoreCase("convert")) {
            if(args.length < 3 || args.length > 4) { sender.sendMessage("Uso: /terra2 convert <ID> <fonte.zip;outra.zip> [namespace:dimension]"); return true; }
            String outputId = args[1], inputs = args[2], dimension = args.length == 4 ? args[3] : null;
            try {
                org.terra2.pack.ResourceBundle.fileName(outputId);
                if(platform.getConfigRegistry().getByID(outputId).isPresent()) throw new IllegalArgumentException("ID já pertence a um Community Pack: " + outputId);
            } catch(RuntimeException error) { sender.sendMessage("Conversão recusada: " + error.getMessage()); return true; }
            if(!converting.compareAndSet(false, true)) { sender.sendMessage("Terra2: uma conversão já está em andamento."); return true; }
            sender.sendMessage("Terra2: conversão iniciada em segundo plano. Nenhum mundo será criado ou alterado.");
            asyncScheduler.runNow(this, task -> {
                String response;
                try {
                    var templateMigration = platform.templateMigration();
                    var result = terraPacks.convert(outputId, java.util.Arrays.stream(inputs.split(";", -1)).map(String::trim).toList(), dimension, templateMigration,
                        org.terra2.pack.PackCompiler.Profile.GENERATION_AND_LOOT);
                    response = "TerraPack " + result.id() + ": " + result.status() + "; pendências: " + result.blockers()
                        + "; arquivo: " + result.output() + "; relatório: " + result.report();
                    logger.info(response);
                } catch(Exception error) { response = "Conversão recusada: " + error.getMessage(); logger.warn(response); }
                finally { converting.set(false); }
                String message = response;
                globalRegionScheduler.execute(this, () -> sender.sendMessage(message));
            });
            return true;
        }
        if(args.length >= 1 && args[0].equalsIgnoreCase("packs")) {
            try {
                if(args.length == 2 && args[1].equalsIgnoreCase("list")) sender.sendMessage("TerraPacks locais: " + terraPacks.list());
                else if(args.length == 3 && args[1].equalsIgnoreCase("inspect")) {
                    var info = terraPacks.inspect(args[2]);
                    sender.sendMessage("TerraPack " + args[2] + ": " + info.get("status").getAsString());
                    var compiled = info.getAsJsonObject("compiled");
                    sender.sendMessage("Recursos: " + compiled.get("resourceKinds"));
                    if(info.has("nativeResourceValidation")) {
                        var nativeReport = info.getAsJsonObject("nativeResourceValidation");
                        sender.sendMessage("Grafo nativo decodificado: " + nativeReport.get("decoded")
                            + "; erros: " + nativeReport.getAsJsonArray("errors").size());
                        sender.sendMessage("Este diagnóstico não autoriza a execução de um pack BLOCKED.");
                    }
                    if(info.has("structureMigration")) {
                        var migration = info.getAsJsonObject("structureMigration");
                        sender.sendMessage("Estruturas NBT lidas: " + migration.getAsJsonArray("pieces").size()
                            + "; referências: " + migration.getAsJsonArray("references").size()
                            + "; erros de migração: " + migration.getAsJsonArray("errors").size());
                        sender.sendMessage("Etapas ainda sem execução: " + migration.get("pendingBackends"));
                    }
                    var blockers = compiled.getAsJsonArray("blockers");
                    sender.sendMessage("Pendências (" + blockers.size() + "): " + java.util.stream.StreamSupport.stream(blockers.spliterator(), false).limit(8).toList());
                } else sender.sendMessage("Uso: /terra2 packs list | /terra2 packs inspect <ID>");
            } catch(Exception error) { sender.sendMessage("TerraPack recusado: " + error.getMessage()); }
            return true;
        }
        if(args.length >= 1 && args[0].equalsIgnoreCase("datapack")) {
            try {
                if(args.length == 2 && args[1].equalsIgnoreCase("list")) {
                    sender.sendMessage("Terra2 datapacks: " + datapacks.list());
                } else if((args.length == 3 || args.length == 4) && args[1].equalsIgnoreCase("inspect")) {
                    var reader = datapacks.read(args[2]); var inspection = reader.inspect();
                    sender.sendMessage("Dimensões encontradas: " + inspection.dimensions());
                    sender.sendMessage("Recursos não suportados (" + inspection.unsupported().size() + "): "
                        + inspection.unsupported().stream().sorted().limit(10).toList());
                    if(args.length == 4) {
                        var definition = reader.flat(args[3]);
                        sender.sendMessage("Conversão flat validada: " + definition.dimension() + "; SHA-256: " + definition.fingerprint());
                    } else sender.sendMessage("Para validar a conversão: /terra2 datapack inspect " + args[2] + " <namespace:dimension>");
                } else sender.sendMessage("Uso: /terra2 datapack list | /terra2 datapack inspect <arquivo> [namespace:dimension]");
            } catch(Exception error) {
                sender.sendMessage("Datapack recusado: " + error.getMessage());
                logger.warn("Datapack inspection refused: {}", error.getMessage());
            }
            return true;
        }
        if(args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Uso: /terra2 reload | /terra2 datapack list | /terra2 datapack inspect <arquivo> [dimensão]");
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

    private record Composition(String selection, ConfigPack legacy, java.util.List<org.terra2.pack.TerraPack> packs, String version) {}
    private Composition composition(YamlConfiguration settings, String world) throws java.io.IOException {
        var ids = com.dfsek.terra.bukkit.util.GenerationSettings.packIds(settings, world);
        if(ids.isEmpty()) throw new IllegalArgumentException("No packs selected for " + world);
        ConfigPack legacy = null; var converted = new java.util.ArrayList<org.terra2.pack.TerraPack>();
        for(int index = 0; index < ids.size(); index++) {
            String selected = ids.get(index);
            var original = platform.getConfigRegistry().getByID(selected);
            if(original.isPresent()) {
                if(terraPacks.contains(selected)) throw new IllegalArgumentException("Ambiguous legacy/TerraPack ID: " + selected);
                if(index != 0 || legacy != null) throw new IllegalArgumentException("Only the first pack can be a legacy terrain base: " + selected);
                legacy = original.get();
            } else converted.add(terraPacks.load(selected));
        }
        org.terra2.pack.TerraPackStore.validateComposition(converted, legacy != null);
        if(legacy == null && (converted.isEmpty() || converted.getFirst().terrain() == null))
            throw new IllegalArgumentException("The first TerraPack must provide terrain");
        String version = (legacy == null ? "" : legacy.getVersion() + "+")
            + String.join("+", converted.stream().map(org.terra2.pack.TerraPack::fingerprint).toList());
        return new Composition(String.join(";", ids), legacy, java.util.List.copyOf(converted), version);
    }
    public synchronized void reloadGenerationSettings() throws Exception {
        var next = com.dfsek.terra.bukkit.util.GenerationSettings.load(new File(getDataFolder(), "terra2-settings.yml"));
        var worlds = next.getConfigurationSection("worlds");
        for(String world : worlds.getKeys(false)) {
            if(next.isString("worlds." + world + ".datapack")) {
                if(generatorMap.containsKey(world)) throw new IllegalArgumentException("Cannot replace active Terra composition: " + world);
                continue;
            }
            var selected = composition(next, world);
            if(generatorMap.containsKey(world) && (!selected.selection().equals(generatorPacks.get(world)) ||
                (compositionVersions.containsKey(world) && !selected.version().equals(compositionVersions.get(world)))))
                throw new IllegalArgumentException("Cannot replace active TerraPack composition: " + world);
            if(datapacks.hasWorld(world)) datapacks.validatePackReload(world, selected.selection(), selected.packs());
        }
        datapacks.validateReload(next);
        var applyLoot = lootManager == null ? null : lootManager.prepareReload(next);
        generationSettings = next;
        if(applyLoot != null) applyLoot.run();
    }
    public void assertLegacyGenerationAuthorized(String worldName, String baseId) {
        var ids = com.dfsek.terra.bukkit.util.GenerationSettings.packIds(generationSettings, worldName);
        if(ids.isEmpty() || !platform.getConfigRegistry().getByID(ids.getFirst()).orElseThrow().getID().equals(baseId))
            throw new IllegalArgumentException("Unauthorized terrain base: " + worldName);
        assertGenerationAuthorized(worldName, "PACKS");
    }

    public void assertGenerationAuthorized(String worldName, String id) {
        YamlConfiguration settings = generationSettings;
        if(settings == null || !settings.getBoolean("generation.enabled", false)) {
            throw new IllegalStateException("Terra2 generation is disabled; refusing generator request for " + worldName);
        }
        String authorizedPack = settings.isString("worlds." + worldName + ".datapack") ? "DATAPACK"
            : com.dfsek.terra.bukkit.util.GenerationSettings.packSelection(settings, worldName);
        boolean configuredPacks = !settings.isString("worlds." + worldName + ".datapack") && !authorizedPack.isEmpty();
        if(!(id.equals("PACKS") && configuredPacks) && !id.equals(authorizedPack)) {
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
