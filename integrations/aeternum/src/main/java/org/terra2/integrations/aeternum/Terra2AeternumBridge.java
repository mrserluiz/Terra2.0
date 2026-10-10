package org.terra2.integrations.aeternum;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.server.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/** Separate adapter: no linked Terra2/Aeternum classes, no config or biome writes. */
public final class Terra2AeternumBridge extends JavaPlugin implements Listener {
    private static final String SERVICE = "org.terra2.api.climate.WorldClimateService";
    private Object seasons;
    private Object source;
    private Method unregister;
    private String lastStatus;
    private boolean paused;

    public void onEnable() { saveDefaultConfig(); Bukkit.getPluginManager().registerEvents(this, this); connect(); }
    public void onDisable() { disconnect(); }

    private void status(String message) {
        if(!message.equals(lastStatus)) { getLogger().info(message); lastStatus = message; }
    }
    private RegisteredServiceProvider<?> registration() {
        for(Class<?> type : Bukkit.getServicesManager().getKnownServices()) {
            if(!type.getName().equals(SERVICE)) continue;
            var candidate = Bukkit.getServicesManager().getRegistration(type);
            if(candidate != null && candidate.getPlugin().getName().equals("Terra2") && candidate.getPlugin().isEnabled()) return candidate;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private void connect() {
        if(paused) { disconnect(); status("Paused: climate metadata is disconnected."); return; }
        var provided = registration();
        Plugin target = Bukkit.getPluginManager().getPlugin(targetName());
        if(provided == null || target == null || !target.isEnabled()) {
            disconnect(); status("Inactive: requires Terra2 climate API and an enabled AeternumSeasons."); return;
        }
        try {
            Object consumer = ClimateReceiverAdapter.find(target, getConfig().getString("receiver.accessor", "getSeasons"));
            if(seasons == consumer && source == provided.getProvider()) return;
            disconnect();
            Method register = consumer.getClass().getMethod("registerWorldClimateProvider", Plugin.class, Function.class);
            Method remove = consumer.getClass().getMethod("unregisterWorldClimateProvider", Plugin.class);
            Object provider = provided.getProvider();
            if(!Integer.valueOf(1).equals(provider.getClass().getMethod("apiVersion").invoke(provider)))
                throw new IllegalArgumentException("Unsupported Terra2 climate API version");
            Method read = provider.getClass().getMethod("describe", World.class);
            Function<World, Map<String, String>> metadata = world -> {
                if(world == null || !provided.getPlugin().isEnabled()) return Map.of();
                try {
                    Map<String, String> data = (Map<String, String>) read.invoke(provider, world);
                    if(data == null || data.isEmpty()) return Map.of();
                    if(!"1".equals(data.get("schema")) || !"Terra2".equals(data.get("source"))
                        || !world.getName().equals(data.get("world")) || !world.getUID().toString().equals(data.get("world-uuid")))
                        throw new IllegalArgumentException("Climate metadata does not match the requested world");
                    return data;
                } catch(ReflectiveOperationException error) { throw new IllegalStateException("Climate source unavailable", error); }
            };
            register.invoke(consumer, this, metadata);
            seasons = consumer; source = provider; unregister = remove;
            status("Connected: Terra2 climate API -> " + target.getName() + " " + target.getPluginMeta().getVersion() + ". Real biomes and configuration files remain untouched.");
        } catch(ReflectiveOperationException | RuntimeException incompatible) {
            disconnect();
            status("UNSUPPORTED: " + target.getName() + " " + target.getPluginMeta().getVersion()
                + " has no usable climate receiver contract. Terra2 metadata remains available through Bukkit ServicesManager; target was not modified. "
                + incompatible.getClass().getSimpleName());
        }
    }
    private String targetName() { return getConfig().getString("receiver.plugin", "AeternumSeasons"); }

    private void disconnect() {
        if(seasons != null && unregister != null) {
            try { unregister.invoke(seasons, this); }
            catch(ReflectiveOperationException ignored) { /* Target may already be unloading. */ }
        }
        seasons = null; source = null; unregister = null;
    }
    @Override public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(!sender.hasPermission("terra2.climate.bridge.manage")) { sender.sendMessage("Permission: terra2.climate.bridge.manage"); return true; }
        if(args.length != 1) return false;
        switch(args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "disconnect" -> { paused = true; disconnect(); status("Paused: climate metadata is disconnected."); }
            case "connect" -> { paused = false; connect(); }
            case "status" -> { }
            default -> { return false; }
        }
        sender.sendMessage("Terra2 climate bridge: " + (paused ? "PAUSED" : source != null ? "CONNECTED" : "INACTIVE"));
        sender.sendMessage(lastStatus == null ? "No receiver detected." : lastStatus);
        return true;
    }
    @Override public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if(!sender.hasPermission("terra2.climate.bridge.manage") || args.length != 1) return java.util.List.of();
        return java.util.List.of("connect", "disconnect", "status").stream()
            .filter(value -> value.startsWith(args[0].toLowerCase(java.util.Locale.ROOT))).toList();
    }
    @EventHandler public void pluginEnabled(PluginEnableEvent event) {
        if(event.getPlugin().getName().equals("Terra2") || event.getPlugin().getName().equals(targetName())) connect();
    }
    @EventHandler public void pluginDisabled(PluginDisableEvent event) {
        if(event.getPlugin().getName().equals("Terra2") || event.getPlugin().getName().equals(targetName())) disconnect();
    }
    @EventHandler public void serviceRegistered(ServiceRegisterEvent event) {
        if(event.getProvider().getService().getName().equals(SERVICE)) connect();
    }
    @EventHandler public void serviceUnregistered(ServiceUnregisterEvent event) {
        if(event.getProvider().getProvider() == source) disconnect();
    }
}
