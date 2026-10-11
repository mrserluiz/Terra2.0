package com.dfsek.terra.bukkit.compatibility;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.world.biome.Biome;
import com.dfsek.terra.bukkit.world.BukkitBiomeInfo;
import com.dfsek.terra.bukkit.world.BukkitPlatformBiome;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.terra2.api.biome.BiomeIdentity;

/** Immutable per-world projection. Does not mutate pack biomes or global registries. */
public final class BiomeCompatibility {
    public final boolean vanilla;
    public final String fingerprint;
    private final Map<Biome, BiomeIdentity> identities;
    private final Map<Biome, org.bukkit.block.Biome> references;
    private final String settingsToken;
    private BiomeCompatibility(boolean vanilla, String fingerprint, String settingsToken,
            Map<Biome,BiomeIdentity> identities, Map<Biome,org.bukkit.block.Biome> references) {
        this.vanilla=vanilla;this.fingerprint=fingerprint;this.settingsToken=settingsToken;
        this.identities=Map.copyOf(identities);this.references=Map.copyOf(references);
    }
    public static String settingsToken(YamlConfiguration settings, String world) {
        var section=settings.getConfigurationSection("worlds."+world+".biomes");
        if(section==null) return "CUSTOM:{}";
        String mode=section.getString("mode", "CUSTOM");
        var mappings=section.getConfigurationSection("references");
        return mode+":"+(mappings==null ? "{}" : new TreeMap<>(mappings.getValues(false)).toString());
    }
    public void validateReload(YamlConfiguration settings, String world) {
        if(!settingsToken.equals(settingsToken(settings,world))) throw new IllegalArgumentException("Biome projection is immutable for a bound world: "+world);
    }
    public static BiomeCompatibility prepare(YamlConfiguration settings, String world, ConfigPack pack) {
        String token=settingsToken(settings,world);
        boolean vanilla=settings.getString("worlds."+world+".biomes.mode","CUSTOM").equals("VANILLA_COMPATIBLE");
        Map<Biome,BiomeIdentity> identities=new HashMap<>(); Map<Biome,org.bukkit.block.Biome> refs=new HashMap<>();
        var overrides=settings.getConfigurationSection("worlds."+world+".biomes.references");
        Map<String,Object> mappings=overrides==null ? Map.of() : overrides.getValues(false);
        Set<String> used=new HashSet<>();
        for(Biome biome:pack.getBiomeProvider().stream().toList()) {
            var platform=(BukkitPlatformBiome)biome.getPlatformBiome();
            String registered=platform.getContext().get(BukkitBiomeInfo.class).biomeKey().toString();
            String id=biome.getID();
            if(mappings.containsKey(id) && mappings.containsKey(registered)) throw new IllegalArgumentException("Duplicate biome reference for "+id);
            String key=mappings.containsKey(registered)?registered:id;
            String reference=mappings.containsKey(key)?(String)mappings.get(key):platform.getHandle().getKey().toString();
            if(mappings.containsKey(key))used.add(key);
            NamespacedKey ref=NamespacedKey.fromString(reference);
            var nativeBiome=ref==null?null:Registry.BIOME.get(ref);
            if(ref==null || !ref.getNamespace().equals("minecraft") || nativeBiome==null)
                throw new IllegalArgumentException("Unknown vanilla reference for "+id+": "+reference);
            identities.put(biome,new BiomeIdentity(id,registered,ref.toString()));refs.put(biome,nativeBiome);
        }
        if(!used.containsAll(mappings.keySet()))throw new IllegalArgumentException("Unknown biome reference IDs: "+mappings.keySet().stream().filter(k->!used.contains(k)).toList());
        String content=pack.getID()+"@"+pack.getVersion()+":"+token+":"+identities.values().stream()
            .map(i->i.terraId()+"="+i.registeredId()+"="+i.vanillaReference()).sorted().toList();
        try {
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8)));
            return new BiomeCompatibility(vanilla,hash,token,identities,refs);
        }catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
    public BiomeIdentity identity(Biome biome) { return Objects.requireNonNull(identities.get(biome), "Unregistered Terra biome"); }
    public org.bukkit.block.Biome reference(Biome biome) { return Objects.requireNonNull(references.get(biome), "Unregistered reference"); }
    public void bind(Path folder, UUID uuid, long seed, String name) throws java.io.IOException {
        BiomeWorldManifest.bind(folder, uuid, seed, vanilla, fingerprint, name);
    }
    public void bind(World world) throws java.io.IOException {
        bind(world.getWorldPath(), world.getUID(), world.getSeed(), world.getName());
    }
}
