package com.dfsek.terra.bukkit.util;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Read-only discovery commands. All expensive work has a per-player concurrency guard. */
public final class DiscoveryCommands {
    private final TerraBukkitPlugin plugin;
    private final Set<UUID> busy = ConcurrentHashMap.newKeySet();
    public DiscoveryCommands(TerraBukkitPlugin plugin) { this.plugin = plugin; }
    private void message(CommandSender sender, String key, Object... values) {
        sender.sendMessage(CommandMessages.text(plugin.generationSettings().getString("language", "pt_BR"), key, values));
    }
    public List<String> complete(CommandSender sender, String[] args) {
        if(args.length == 1) return filter(List.of("biome", "structures", "locate"), args[0]);
        if(!args[0].equalsIgnoreCase("locate")) return List.of();
        if(args.length == 2) return filter(List.of("biome", "structure"), args[1]);
        if(args.length != 3 || !(sender instanceof Player player)) return List.of();
        if(args[1].equalsIgnoreCase("structure")) return filter(StructureIndex.entries(player.getWorld().getUID()).stream()
            .map(StructureIndex.Placement::id).distinct().toList(), args[2]);
        if(args[1].equalsIgnoreCase("biome") && player.getWorld().getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper)
            return filter(wrapper.getPack().getBiomeProvider().stream().map(biome -> biome.getID()).distinct().toList(), args[2]);
        return List.of();
    }
    private List<String> filter(Collection<String> values, String prefix) {
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))
            .distinct().sorted().limit(200).toList();
    }
    public boolean handle(CommandSender sender, String[] args) {
        if(args.length == 0 || !Set.of("biome", "structures", "locate").contains(args[0].toLowerCase(Locale.ROOT))) return false;
        if(!(sender instanceof Player player)) { message(sender, "discover-player"); return true; }
        var world = player.getWorld(); var location = player.getLocation();
        if(args[0].equalsIgnoreCase("biome") && args.length == 1) {
            String id = world.getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper
                ? wrapper.getPack().getBiomeProvider().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), world.getSeed()).getID()
                : world.getBiome(location).getKey().toString();
            message(sender, "discover-biome", world.getName(), id, world.getBiome(location).getKey()); return true;
        }
        boolean near = args[0].equalsIgnoreCase("structures");
        if((near && args.length > 2) || (!near && (!args[0].equalsIgnoreCase("locate") || args.length < 3 || args.length > 4))) {
            message(sender, "discover-usage"); return true;
        }
        int radius;
        try { radius = Integer.parseInt(near ? (args.length == 2 ? args[1] : "256") : (args.length == 4 ? args[3] : "4096"));
            if(radius < 32 || radius > 8192) throw new NumberFormatException();
        } catch(NumberFormatException error) { message(sender, "discover-radius"); return true; }
        boolean biome = !near && args[1].equalsIgnoreCase("biome");
        if(!near && !biome && !args[1].equalsIgnoreCase("structure")) { message(sender, "discover-usage"); return true; }
        var uuid = world.getUID(); long seed = world.getSeed();
        var provider = world.getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper ? wrapper.getPack().getBiomeProvider() : null;
        if(biome && provider == null) { message(sender, "discover-provider"); return true; }
        if(!biome && !StructureIndex.ready(uuid)) { message(sender, "discover-index"); return true; }
        if(biome && provider.stream().noneMatch(value -> value.getID().equalsIgnoreCase(args[2]))) {
            message(sender, "discover-id", args[2]); return true;
        }
        if(!busy.add(player.getUniqueId())) { message(sender, "discover-busy"); return true; }
        message(sender, "discover-search");
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        plugin.getAsyncScheduler().runNow(plugin, task -> {
            String key; Object[] values;
            try {
                if(biome) {
                    var result = BiomeSearch.find(x, z, radius, args[2], (bx, bz) -> provider.getBiome(bx, y, bz, seed).getID());
                    key = result.samples() > 0 && !result.limited() ? "discover-found-biome" : "discover-notfound-biome";
                    values = key.equals("discover-found-biome") ? new Object[]{args[2], result.x(), y, result.z()} : new Object[]{Math.abs(result.samples())};
                } else {
                    var found = StructureIndex.entries(uuid).stream().filter(value -> near || value.id().equalsIgnoreCase(args[2]))
                        .filter(value -> distance(x, z, value) <= (long) radius * radius)
                        .sorted(Comparator.comparingLong(value -> distance(x, z, value))).limit(near ? 10 : 1).toList();
                    key = "discover-structures";
                    values = new Object[]{found.isEmpty() ? "[]" : found.stream().map(value -> value.id() + " [" + value.x() + ", " + value.y() + ", " + value.z() + "] (" + value.kind() + ")").toList(), StructureIndex.full(uuid)};
                }
            } catch(Exception error) { key = "discover-error"; values = new Object[]{error.getMessage()}; }
            finally { busy.remove(player.getUniqueId()); }
            String responseKey = key; Object[] responseValues = values;
            player.getScheduler().run(plugin, ignored -> {
                if(player.getWorld().getUID().equals(uuid)) message(player, responseKey, responseValues);
            }, null);
        });
        return true;
    }
    private long distance(int x, int z, StructureIndex.Placement value) {
        long dx = (long) x - value.x(), dz = (long) z - value.z(); return dx * dx + dz * dz;
    }
}
