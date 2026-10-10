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

    public void onEnable() { Bukkit.getPluginManager().registerEvents(this, this); connect(); }
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
        var provided = registration();
        Plugin target = Bukkit.getPluginManager().getPlugin("AeternumSeasons");
        if(provided == null || target == null || !target.isEnabled()) {
            disconnect(); status("Inactive: requires Terra2 climate API and an enabled AeternumSeasons."); return;
        }
        try {
            Object consumer = target.getClass().getMethod("getSeasons").invoke(target);
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
            status("Connected: Terra2 climate API -> AeternumSeasons. Real biomes and configuration files remain untouched.");
        } catch(ReflectiveOperationException | RuntimeException incompatible) {
            disconnect();
            status("Inactive: AeternumSeasons must expose optional climate-provider API 1 (4.5.2-CLIMATE-API-BETA or compatible). "
                + incompatible.getClass().getSimpleName());
        }
    }
    private void disconnect() {
        if(seasons != null && unregister != null) {
            try { unregister.invoke(seasons, this); }
            catch(ReflectiveOperationException ignored) { /* Target may already be unloading. */ }
        }
        seasons = null; source = null; unregister = null;
    }
    @EventHandler public void pluginEnabled(PluginEnableEvent event) {
        if(event.getPlugin().getName().equals("Terra2") || event.getPlugin().getName().equals("AeternumSeasons")) connect();
    }
    @EventHandler public void pluginDisabled(PluginDisableEvent event) {
        if(event.getPlugin().getName().equals("Terra2") || event.getPlugin().getName().equals("AeternumSeasons")) disconnect();
    }
    @EventHandler public void serviceRegistered(ServiceRegisterEvent event) {
        if(event.getProvider().getService().getName().equals(SERVICE)) connect();
    }
    @EventHandler public void serviceUnregistered(ServiceUnregisterEvent event) {
        if(event.getProvider().getProvider() == source) disconnect();
    }
}
