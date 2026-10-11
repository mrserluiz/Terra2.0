package com.dfsek.terra.bukkit.bedrock;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import org.terra2.api.visual.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import static com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call;

/** Explicitly enabled, bounded grass-only client preview. Never replaces saved blocks or Java mappings. */
public final class BedrockPreviewBridge implements Listener {
    private static final String MARKER="terra2-position-preview-v1";
    private final TerraBukkitPlugin plugin;
    private final Map<UUID,Preview> players=new HashMap<>();
    private final Map<Integer,Object> colors=new TreeMap<>();
    private final Map<String,VisualProfile> profiles=new TreeMap<>();
    private GeyserPositionTransport transport;
    private Object bus,owner;
    private BukkitTask timer;
    private volatile String status="DISABLED";
    private boolean packRegistered,defined;
    private List<VisualProfile> catalog;
    private UUID packId;
    private final Path pack,marker;
    private record Pos(int x,int y,int z) {}
    private static final class Preview {
        final UUID world; final Object session; final Map<Pos,Integer> overlay=new HashMap<>();
        volatile boolean active=true; volatile long epoch; volatile long submitted;
        Preview(UUID world,Object session){this.world=world;this.session=session;}
    }
    public BedrockPreviewBridge(TerraBukkitPlugin plugin) {
        this.plugin=plugin;pack=plugin.getDataFolder().toPath().resolve("bedrock/terra2-palette.mcpack");
        marker=plugin.getDataFolder().toPath().resolve("bedrock/preview.enabled");
    }
    public void prepare() throws Exception {
        Files.createDirectories(marker.getParent());
        if(Files.isSymbolicLink(marker) || (Files.exists(marker) && !Files.readString(marker).equals(MARKER)))
            throw new java.io.IOException("Unknown preview marker; refusing replacement");
        Files.writeString(marker,MARKER);
    }
    public void disable() throws Exception {
        if(Files.isSymbolicLink(marker) || (Files.exists(marker) && !Files.readString(marker).equals(MARKER)))
            throw new java.io.IOException("Unknown preview marker; refusing deletion");
        Files.deleteIfExists(marker);close();status="DISABLED";
    }
    public void initialize() {
        try {
            if(!Files.exists(marker))return;
            if(Bukkit.getName().equalsIgnoreCase("Folia")){status="UNSUPPORTED_FOLIA_PREVIEW";return;}
            if(Files.isSymbolicLink(marker) || !Files.readString(marker).equals(MARKER))throw new IllegalStateException("Invalid preview marker");
            Plugin geyser=Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
            if(geyser==null || !geyser.isEnabled()){status="GEYSER_ABSENT";return;}
            transport=new GeyserPositionTransport(geyser.getClass().getClassLoader());
            Object api=transport.api();bus=call(api,"eventBus");
            owner=call(transport.type("org.geysermc.geyser.api.event.EventRegistrar"),"of",plugin);
            subscribe("org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent",this::defineBlocks);
            subscribe("org.geysermc.geyser.api.event.lifecycle.GeyserDefineResourcePacksEvent",this::definePack);
            Bukkit.getPluginManager().registerEvents(this,plugin);
            status="WAITING_GEYSER_STARTUP";
        } catch(Exception | LinkageError failure){fail(failure);}
    }
    private void subscribe(String event,Consumer<Object> handler) throws Exception {
        Consumer<Object> safe=value->{try{handler.accept(value);}catch(Exception | LinkageError error){fail(error);}};
        call(bus,"subscribe",owner,transport.type(event),safe);
    }
    private void catalog() throws Exception {
        if(catalog!=null)return;
        var items=new ArrayList<>(BedrockVisualPack.fromBiomes(plugin.platform().bedrockBiomeAppearances()));
        for(var registration:Bukkit.getServicesManager().getRegistrations(VisualPaletteProvider.class)) {
            var provider=registration.getProvider();
            if(provider.apiVersion()!=1)throw new IllegalArgumentException("Unsupported visual provider API");
            items.addAll(provider.profiles());
        }
        BedrockVisualPack.index(items); // Reject collisions/limits before writing or registering anything.
        items.forEach(profile->profiles.put(profile.id(),profile));catalog=List.copyOf(items);
        BedrockVisualPack.write(pack,catalog);
        try(var zip=new java.util.zip.ZipFile(pack.toFile())) {
            var manifest=com.google.gson.JsonParser.parseString(new String(zip.getInputStream(zip.getEntry("manifest.json")).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            packId=UUID.fromString(manifest.getAsJsonObject("header").get("uuid").getAsString());
        }
    }
    private void defineBlocks(Object event) {
        try {
            if(defined)throw new IllegalStateException("Geyser reload is not supported during a preview; restart the server");
            catalog();var requested=new TreeSet<Integer>();
            profiles.values().forEach(profile->{Integer grass=profile.colors().get("grass");if(grass!=null)requested.add(grass);});
            if(requested.isEmpty() || requested.size()>128)throw new IllegalStateException("Grass preview needs 1..128 distinct colors");
            for(int rgb:requested) {
                Object materialBuilder=call(transport.type("org.geysermc.geyser.api.block.custom.component.MaterialInstance"),"builder");
                call(materialBuilder,"texture","terra2_rgb_%06x".formatted(rgb));
                call(materialBuilder,"renderMethod","opaque");call(materialBuilder,"faceDimming",true);call(materialBuilder,"ambientOcclusion",true);
                Object componentsBuilder=call(transport.type("org.geysermc.geyser.api.block.custom.component.CustomBlockComponents"),"builder");
                call(componentsBuilder,"unitCube",true);call(componentsBuilder,"materialInstance","*",call(materialBuilder,"build"));
                call(componentsBuilder,"placeAir",true);call(componentsBuilder,"destructibleByMining",0.6f);
                Object builder=call(transport.type("org.geysermc.geyser.api.block.custom.CustomBlockData"),"builder");
                call(builder,"name","terra2_preview_rgb_%06x".formatted(rgb));call(builder,"includedInCreativeInventory",false);
                call(builder,"components",call(componentsBuilder,"build"));Object block=call(builder,"build");
                call(event,"register",block);colors.put(rgb,call(block,"defaultBlockState"));
            }
            // No registerOverride() or registerItemOverride(): ordinary chunks stay vanilla.
            defined=true;
        } catch(Exception | LinkageError error){fail(error);}
    }
    private void definePack(Object event) {
        try {
            catalog();
            for(Object existing:(List<?>)call(event,"resourcePacks"))if(packId.equals(call(existing,"uuid"))){packRegistered=true;return;}
            Object codec=call(transport.type("org.geysermc.geyser.api.pack.PackCodec"),"path",pack);
            Object resource=call(transport.type("org.geysermc.geyser.api.pack.ResourcePack"),"create",codec);
            Object options=java.lang.reflect.Array.newInstance(transport.type("org.geysermc.geyser.api.pack.option.ResourcePackOption"),0);
            call(event,"register",resource,options);packRegistered=true;
        } catch(Exception | LinkageError error){fail(error);}
    }
    @EventHandler(priority=EventPriority.MONITOR)
    public void serverLoaded(ServerLoadEvent event) {
        if(status.equals("FAILED_DISABLED"))return;
        try {
            if(!defined || !packRegistered)throw new IllegalStateException("Geyser custom-content events missing; check gameplay.enable-custom-content and restart");
            int definitions=transport.validate(colors.values());status="READY_OPT_IN";
            plugin.getLogger().info("TERRA2_GEYSER_POSITION_READY colors="+colors.size()+" protocolDefinitions="+definitions+" pack="+packId);
            timer=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,20L,20L);
        } catch(Exception | LinkageError error){fail(error);}
    }
    public String status(){return status+"; grassColors="+colors.size()+"; packRegistered="+packRegistered+"; players="+players.size();}
    public int validateProtocol() throws Exception {return transport.validate(colors.values());}
    public Object diagnosticPacket(int x,int y,int z) throws Exception {return transport.diagnosticPacket(colors.values().iterator().next(),x,y,z);}
    public Optional<Integer> grassAt(World world,int x,int y,int z) {
        return PositionVisualResolver.at(world,x,y,z).map(sample->profiles.get(sample.profile()))
            .map(profile->profile.colors().get("grass"));
    }
    public String start(Player player) throws Exception {
        if(!status.equals("READY_OPT_IN"))return "Prévia indisponível: "+status+". Use prepare e reinicie; mantenha o Geyser oficial.";
        plugin.assertGenerationAuthorized(player.getWorld().getName(),"PACKS");
        Object session=transport.connection(player.getUniqueId());
        if(session==null)return "Este teste exige um jogador conectado pelo Geyser local.";
        if(players.size()>=2 && !players.containsKey(player.getUniqueId()))return "Limite do protótipo: 2 jogadores em prévia.";
        stop(player,true);players.put(player.getUniqueId(),new Preview(player.getWorld().getUID(),session));
        return "Prévia de grama ativada: raio 4, até 64 blocos próximos. Aceite o pack e reconecte antes do teste. Água/folhas/céu não são alterados.";
    }
    private void tick() {
        if(!status.equals("READY_OPT_IN"))return;
        for(UUID id:List.copyOf(players.keySet())) {
            Player player=Bukkit.getPlayer(id);Preview state=players.get(id);
            if(player==null || !player.isOnline()){state.active=false;players.remove(id);continue;}
            if(!player.getWorld().getUID().equals(state.world)){stop(player,false);continue;}
            if(!player.hasPermission("terra2.settings.reload")){stop(player,true);continue;}
            try {
                plugin.assertGenerationAuthorized(player.getWorld().getName(),"PACKS");
                var world=player.getWorld();var location=player.getLocation();var wanted=new LinkedHashMap<Pos,Integer>();
                search:for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)for(int dy=-3;dy<=3;dy++) {
                    int x=location.getBlockX()+dx,y=location.getBlockY()+dy,z=location.getBlockZ()+dz;
                    if(y<world.getMinHeight() || y>=world.getMaxHeight() || !world.isChunkLoaded(x>>4,z>>4))continue;
                    if(world.getBlockAt(x,y,z).getType()!=Material.GRASS_BLOCK)continue;
                    var rgb=grassAt(world,x,y,z);if(rgb.isPresent() && colors.containsKey(rgb.get()))wanted.put(new Pos(x,y,z),rgb.get());
                    if(wanted.size()>=64)break search;
                }
                for(Pos pos:state.overlay.keySet())if(!wanted.containsKey(pos))restore(player,world,pos);
                state.overlay.clear();state.overlay.putAll(wanted);long epoch=++state.epoch;
                Object mappings=call(state.session,"getBlockMappings");var packets=new ArrayList<Object>();
                for(var entry:wanted.entrySet()) {
                    var pos=entry.getKey();packets.add(transport.packet(mappings,colors.get(entry.getValue()),pos.x,pos.y,pos.z));
                }
                if(!packets.isEmpty())transport.submit(state.session,packets,()->state.active && state.epoch==epoch,
                    ()->state.submitted++,error->{
                        state.active=false;Bukkit.getScheduler().runTask(plugin,()->{stop(player,true);fail(error);});
                });
            } catch(Exception | LinkageError error){stop(player,true);fail(error);return;}
        }
    }
    private void restore(Player player,World world,Pos pos) {
        if(world.isChunkLoaded(pos.x>>4,pos.z>>4))player.sendBlockChange(new Location(world,pos.x,pos.y,pos.z),world.getBlockAt(pos.x,pos.y,pos.z).getBlockData());
    }
    public void stop(Player player,boolean restore) {
        Preview state=players.remove(player.getUniqueId());if(state==null)return;state.active=false;state.epoch++;
        if(restore && player.getWorld().getUID().equals(state.world))for(Pos pos:state.overlay.keySet())restore(player,player.getWorld(),pos);
    }
    public String playerStatus(Player player) {
        Preview state=players.get(player.getUniqueId());return state==null?"Prévia desligada":"Blocos em prévia="+state.overlay.size()+"; updates submetidos="+state.submitted+" (não confirma renderização no cliente)";
    }
    @EventHandler public void quit(PlayerQuitEvent event){stop(event.getPlayer(),false);}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void teleport(PlayerTeleportEvent event){stop(event.getPlayer(),true);}
    @EventHandler public void changedWorld(PlayerChangedWorldEvent event){stop(event.getPlayer(),false);}
    private void fail(Throwable error) {
        if(status.equals("FAILED_DISABLED"))return;status="FAILED_DISABLED";
        while(error instanceof java.lang.reflect.InvocationTargetException && error.getCause()!=null)error=error.getCause();
        final Throwable reason=error;
        Runnable cleanup=()->{
            players.values().forEach(state->state.active=false);
            for(UUID id:List.copyOf(players.keySet())) {Player player=Bukkit.getPlayer(id);if(player!=null)stop(player,true);}
            plugin.getLogger().warning("Terra2 Bedrock preview disabled: "+reason);
            com.dfsek.terra.bukkit.util.GenerationReport.failure("Bedrock position preview","<client-preview>","RGB_PALETTE",reason);
        };
        if(Bukkit.isPrimaryThread())cleanup.run();else Bukkit.getScheduler().runTask(plugin,cleanup);
    }
    public void close() {
        players.values().forEach(state->state.active=false);
        for(UUID id:List.copyOf(players.keySet())){Player player=Bukkit.getPlayer(id);if(player!=null)stop(player,true);}
        if(timer!=null)timer.cancel();
        if(bus!=null && owner!=null)try{call(bus,"unregisterAll",owner);}catch(Exception ignored){}
    }
}
