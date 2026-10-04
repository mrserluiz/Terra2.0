package org.terra2.adapter.vanilla.paper;

import com.google.gson.Gson;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.*;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.terra2.core.*;

/** Fresh loot is the only mutation trigger. Drop/spawn/pickup never reinterpret an existing item. */
public final class WorldLootManager implements Listener {
    private record Rule(String name, List<String> lore, NamespacedKey model, Set<Material> items) {}
    private record Binding(WorldTarget target, boolean decorate, Map<String, String> owners,
                           Map<String, WorldLootPolicy> policies, Map<String, Rule> rules) {}
    private static final Gson JSON = new Gson();
    private final TerraBukkitPlugin plugin;
    private final LootOrigin origin;
    private final NamespacedKey marker;
    private volatile Map<String, Binding> worlds = Map.of();
    private final Set<String> nativeTables = ConcurrentHashMap.newKeySet();
    private final AtomicLong issued = new AtomicLong();
    public WorldLootManager(TerraBukkitPlugin plugin, LootOrigin origin) {
        this.plugin = plugin; this.origin = origin; marker = new NamespacedKey(plugin, "loot_origin");
    }
    public void declareNativeTables(Collection<String> tables) { nativeTables.addAll(tables); }
    public synchronized void bind(World world, Map<String, String> nativeOwners) {
        plugin.assertGenerationAuthorized(world.getName(), "PACKS");
        var binding = binding(world, nativeOwners, plugin.generationSettings());
        var previous = worlds.get(world.getName());
        if(previous != null && !previous.target.equals(binding.target)) throw new IllegalStateException("Cannot rebind loot dimension: " + world.getName());
        var next = new HashMap<>(worlds); next.put(world.getName(), binding); worlds = Map.copyOf(next);
    }
    public synchronized Runnable prepareReload(ConfigurationSection settings) {
        Map<String, Binding> next = new HashMap<>();
        if(settings.getBoolean("generation.enabled", false)) worlds.forEach((name, previous) -> {
            var world = Bukkit.getWorld(name);
            if(world == null || settings.getConfigurationSection("worlds." + name) == null) return;
            Map<String, String> nativeOwners = new HashMap<>();
            previous.owners.forEach((table, pack) -> { if(nativeTables.contains(table)) nativeOwners.put(table, pack); });
            var binding = binding(world, nativeOwners, settings);
            if(!previous.target.equals(binding.target)) throw new IllegalStateException("Loot dimension changed: " + name);
            next.put(name, binding);
        });
        var prepared = Map.copyOf(next); return () -> worlds = prepared;
    }
    private Binding binding(World world, Map<String, String> nativeOwners, ConfigurationSection settings) {
        var target = new WorldTarget(world.getName(), world.getKey().toString());
        var config = settings.getConfigurationSection("worlds." + world.getName() + ".loot");
        boolean decorate = config != null && config.getBoolean("enabled", false);
        Map<String, Rule> rules = new HashMap<>();
        if(config != null && config.getConfigurationSection("tables") != null) {
            config.getConfigurationSection("tables").getValues(false).forEach((table, raw) -> {
                org.terra2.pack.ResourceBundle.identifier(table);
                if(!(raw instanceof ConfigurationSection section)) throw new IllegalArgumentException("Loot rule must be a section: " + table);
                String text = section.getString("name");
                var lore = section.getStringList("lore");
                if((text != null && text.length() > 1024) || lore.size() > 64 || lore.stream().anyMatch(line -> line.length() > 1024))
                    throw new IllegalArgumentException("Loot presentation exceeds limits");
                String rawModel = section.getString("item-model");
                var model = rawModel == null ? null : NamespacedKey.fromString(rawModel);
                if(rawModel != null && model == null) throw new IllegalArgumentException("Invalid item-model: " + rawModel);
                Set<Material> items = new HashSet<>();
                for(String item : section.getStringList("items")) {
                    Material material = Material.matchMaterial(item);
                    if(material == null || !material.isItem()) throw new IllegalArgumentException("Invalid loot item: " + item);
                    items.add(material);
                }
                rules.put(table, new Rule(text, List.copyOf(lore), model, Set.copyOf(items)));
            });
        }
        Map<String, String> owners = new HashMap<>(nativeOwners);
        for(String table : rules.keySet()) if(!table.startsWith("terra2:") && Bukkit.getLootTable(NamespacedKey.fromString(table)) != null)
            owners.putIfAbsent(table, "vanilla");
        Map<String, Set<String>> grouped = new HashMap<>();
        owners.forEach((table, pack) -> grouped.computeIfAbsent(pack, ignored -> new HashSet<>()).add(table));
        Map<String, WorldLootPolicy> policies = new HashMap<>();
        grouped.forEach((pack, tables) -> {
            var policy = new WorldLootPolicy(Set.of(), origin); policy.authorize(target, pack, tables); policies.put(pack, policy);
        });
        return new Binding(target, decorate, Map.copyOf(owners), Map.copyOf(policies), Map.copyOf(rules));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void generate(LootGenerateEvent event) {
        String table = event.getLootTable().getKey().toString();
        var world = event.getWorld(); var binding = worlds.get(world.getName());
        boolean nativeTable = nativeTables.contains(table);
        if(binding == null || !binding.target.dimensionKey().equals(world.getKey().toString()) || !binding.owners.containsKey(table)) {
            if(nativeTable) event.setCancelled(true);
            return;
        }
        try { plugin.assertGenerationAuthorized(world.getName(), "PACKS"); }
        catch(RuntimeException denied) { if(nativeTable) event.setCancelled(true); return; }
        if(!nativeTable && !binding.decorate) return;
        String pack = binding.owners.get(table);
        Rule rule = binding.rules.get(table);
        if(rule == null && nativeTable) rule = binding.rules.get(originalTable(table));
        var output = new ArrayList<ItemStack>();
        for(ItemStack input : event.getLoot()) {
            if(input == null || input.getType().isAir()) continue;
            var meta = input.getItemMeta();
            if(meta.getPersistentDataContainer().has(marker)) { output.add(input); continue; }
            var receipt = binding.policies.get(pack).mint(WorldLootPolicy.Trigger.LOOT_TABLE, binding.target, pack, table, false).orElseThrow();
            var item = input.clone(); meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(marker, PersistentDataType.STRING, JSON.toJson(receipt));
            if(binding.decorate && rule != null && (rule.items.isEmpty() || rule.items.contains(item.getType()))) {
                if(rule.name != null) meta.displayName(Component.text(format(rule.name, binding.target)));
                if(!rule.lore.isEmpty()) meta.lore(rule.lore.stream().map(line -> Component.text(format(line, binding.target))).toList());
                if(rule.model != null) meta.setItemModel(rule.model);
            }
            item.setItemMeta(meta); issued.incrementAndGet(); output.add(item);
        }
        event.setLoot(output);
    }
    private static String format(String text, WorldTarget target) {
        return text.replace("{world}", target.worldName()).replace("{dimension}", target.dimensionKey());
    }
    private static String originalTable(String alias) {
        String[] parts = alias.substring("terra2:".length()).split("/", 3);
        return parts.length == 3 ? parts[1] + ":" + parts[2] : alias;
    }
    public boolean authentic(ItemStack item) {
        if(item == null || !item.hasItemMeta()) return false;
        String value = item.getItemMeta().getPersistentDataContainer().get(marker, PersistentDataType.STRING);
        try { return value != null && value.length() <= 16384 && origin.authentic(JSON.fromJson(value, LootOrigin.Receipt.class)); }
        catch(RuntimeException invalid) { return false; }
    }
    public String status() { return "Loot: " + worlds.size() + " mundos vinculados; " + issued.get() + " origens emitidas nesta execução"; }
}
