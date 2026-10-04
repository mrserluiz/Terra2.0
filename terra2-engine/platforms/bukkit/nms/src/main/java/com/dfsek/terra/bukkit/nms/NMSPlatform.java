package com.dfsek.terra.bukkit.nms;

import com.dfsek.tectonic.api.TypeRegistry;
import com.dfsek.tectonic.api.exception.LoadException;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.attribute.AmbientAdditionsSettings;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.Biome.TemperatureModifier;
import net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Locale;

import com.dfsek.terra.addon.InternalAddon;
import com.dfsek.terra.api.addon.BaseAddon;
import com.dfsek.terra.api.event.events.platform.PlatformInitializationEvent;
import com.dfsek.terra.api.event.functional.FunctionalEventHandler;
import com.dfsek.terra.api.world.biome.PlatformBiome;
import com.dfsek.terra.bukkit.PlatformImpl;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.nms.config.BiomeAdditionsSoundTemplate;
import com.dfsek.terra.bukkit.nms.config.BiomeMoodSoundTemplate;
import com.dfsek.terra.bukkit.nms.config.BiomeParticleConfigTemplate;
import com.dfsek.terra.bukkit.nms.config.EntityTypeTemplate;
import com.dfsek.terra.bukkit.nms.config.MusicSoundTemplate;
import com.dfsek.terra.bukkit.nms.config.SoundEventTemplate;
import com.dfsek.terra.bukkit.nms.config.SpawnCostConfig;
import com.dfsek.terra.bukkit.nms.config.SpawnEntryConfig;
import com.dfsek.terra.bukkit.nms.config.SpawnSettingsTemplate;
import com.dfsek.terra.bukkit.nms.config.SpawnTypeConfig;
import com.dfsek.terra.bukkit.nms.config.VillagerTypeTemplate;


public class NMSPlatform extends PlatformImpl {
    private NativePackRuntime nativeRuntime;
    public NativePackRuntime nativeRuntime() { return nativeRuntime; }
    @Override public org.terra2.pack.TemplateMigration.Backend templateMigration() { return new NativePaperTemplateMigration(); }
    @Override public void initializeLootManager(org.terra2.adapter.vanilla.paper.WorldLootManager manager) {
        NativePackRuntime.registries().lookupOrThrow(net.minecraft.core.registries.Registries.LOOT_TABLE).listElements()
            .forEach(holder -> NativeEntityLootBridge.wrap(holder.value(), holder.key().identifier().toString(), manager));
    }
    @Override public void prepareNativePacks(String world, java.util.List<org.terra2.pack.TerraPack> packs) throws java.io.IOException { nativeRuntime.prepare(world, packs); }
    @Override public java.util.Map<String, String> nativeLootTables(String world) { return nativeRuntime.lootTables(world); }

    @Override
    public boolean usesNativeBiomeProvider() { return true; }
    @Override public org.terra2.pack.NativePackBackend nativePackBackend() {
        return new org.terra2.pack.NativePackBackend() {
        @Override public boolean executableOverlay(org.terra2.pack.ResourceBundle source, org.terra2.pack.TemplateMigration.Report migration,
            org.terra2.pack.PackCompiler.Profile profile) { return NativePackRuntime.executable(source, migration, profile); }
        @Override public org.terra2.pack.NativePackBackend.Validation validate(org.terra2.pack.ResourceBundle source) {
            var server = net.minecraft.server.MinecraftServer.getServer();
            var registries = new java.util.LinkedHashMap<net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>>, net.minecraft.core.HolderLookup.RegistryLookup<?>>();
            server.registryAccess().listRegistries().forEach(lookup -> registries.put(lookup.key(), lookup));
            server.reloadableRegistries().lookup().listRegistries().forEach(lookup -> registries.put(lookup.key(), lookup));
            try {
                var graph = new NativeResourceGraph(source, net.minecraft.core.HolderLookup.Provider.create(registries.values().stream()));
                var report = graph.report();
                return new org.terra2.pack.NativePackBackend.Validation("paper-26.2-native-graph-1", report.fingerprint(), report.decoded(), report.errors());
            } catch(RuntimeException | LinkageError error) {
                return new org.terra2.pack.NativePackBackend.Validation("paper-26.2-native-graph-1", source.fingerprint(), java.util.Map.of(),
                    java.util.List.of("Native graph initialization: " + error.getMessage()));
            }
        }
        };
    }

    public NMSPlatform(TerraBukkitPlugin plugin) {
        super(plugin);
        nativeRuntime = new NativePackRuntime(plugin);

        Bukkit.getPluginManager().registerEvents(new NMSInjectListener(plugin), plugin);
    }

    @Override
    public void register(TypeRegistry registry) {
        super.register(registry);
        registry.registerLoader(PlatformBiome.class, (type, o, loader, depthTracker) -> parseBiome((String) o, depthTracker))
            .registerLoader(Identifier.class, (type, o, loader, depthTracker) -> {
                Identifier identifier = Identifier.tryParse((String) o);
                if(identifier == null)
                    throw new LoadException("Invalid identifier: " + o, depthTracker);
                return identifier;
            })
            .registerLoader(Precipitation.class, (type, o, loader, depthTracker) -> Precipitation.valueOf(((String) o).toUpperCase(
                Locale.ROOT)))
            .registerLoader(GrassColorModifier.class,
                (type, o, loader, depthTracker) -> GrassColorModifier.valueOf(((String) o).toUpperCase(
                    Locale.ROOT)))
            .registerLoader(TemperatureModifier.class,
                (type, o, loader, depthTracker) -> TemperatureModifier.valueOf(((String) o).toUpperCase(
                    Locale.ROOT)))
            .registerLoader(MobCategory.class, (type, o, loader, depthTracker) -> MobCategory.valueOf((String) o))
            .registerLoader(AmbientParticle.class, BiomeParticleConfigTemplate::new)
            .registerLoader(SoundEvent.class, SoundEventTemplate::new)
            .registerLoader(AmbientMoodSettings.class, BiomeMoodSoundTemplate::new)
            .registerLoader(AmbientAdditionsSettings.class, BiomeAdditionsSoundTemplate::new)
            .registerLoader(Music.class, MusicSoundTemplate::new)
            .registerLoader(EntityType.class, EntityTypeTemplate::new)
            .registerLoader(SpawnCostConfig.class, SpawnCostConfig::new)
            .registerLoader(SpawnEntryConfig.class, SpawnEntryConfig::new)
            .registerLoader(SpawnTypeConfig.class, SpawnTypeConfig::new)
            .registerLoader(MobSpawnSettings.class, SpawnSettingsTemplate::new)
            .registerLoader(VillagerType.class, VillagerTypeTemplate::new);
    }

    @Override
    protected InternalAddon load() {
        InternalAddon internalAddon = super.load();

        this.getEventManager().getHandler(FunctionalEventHandler.class)
            .register(internalAddon, PlatformInitializationEvent.class)
            .priority(1)
            .then(event -> AwfulBukkitHacks.registerBiomes(this.getRawConfigRegistry()))
            .global();

        return internalAddon;
    }

    @Override
    protected Iterable<BaseAddon> platformAddon() {
        return List.of(new NMSAddon(this));
    }
}
