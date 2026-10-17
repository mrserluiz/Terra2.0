package com.dfsek.terra.bukkit.bedrock;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import org.terra2.api.visual.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.PluginEnableEvent;
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
    private NativeWorldStore nativeWorlds;
    private final Set<UUID> nativePaused=new HashSet<>();
    private final Map<UUID,NativePreview> nativePlayers=new HashMap<>();
    private static final class NativePreview {
        final UUID world;final Object session;final int count;final GeyserNativeBiomeTransport.Plan plan;volatile boolean active=true,checking;
        volatile GeyserNativeBiomeTransport.Change change;volatile String result="PENDING_EVENT_LOOP";
        NativePreview(UUID world,Object session,GeyserNativeBiomeTransport.Plan plan){this.world=world;this.session=session;this.plan=plan;this.count=plan.ids().size();}
    }
    private final Map<Integer,Object> colors=new TreeMap<>();
    private final Map<String,VisualProfile> profiles=new TreeMap<>();
    private GeyserPositionTransport transport;
    private Object bus,owner;
    private BukkitTask timer;
    private volatile String status="DISABLED";
    private boolean packRegistered,defined;
    private boolean listening;
    private List<VisualProfile> catalog;
    private UUID packId;
    private BedrockColorReport colorReport;
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
        if(nativeWorlds==null)nativeWorlds=new NativeWorldStore(marker.getParent().resolve("native-worlds.json"));
        nativeWorlds.clear();Files.deleteIfExists(marker);close();status="DISABLED";
    }
    public void initialize() {
        try {
            if(!Files.exists(marker))return;
            if(Bukkit.getName().equalsIgnoreCase("Folia")){status="UNSUPPORTED_FOLIA_PREVIEW";return;}
            if(Files.isSymbolicLink(marker) || !Files.readString(marker).equals(MARKER))throw new IllegalStateException("Invalid preview marker");
            if(!listening){Bukkit.getPluginManager().registerEvents(this,plugin);listening=true;}
            if(nativeWorlds==null)nativeWorlds=new NativeWorldStore(marker.getParent().resolve("native-worlds.json"));
            Plugin geyser=Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
            if(geyser==null){status="GEYSER_ABSENT";return;}
            if(!geyser.isEnabled()){status="WAITING_GEYSER_ENABLE";return;}
            if(transport!=null)return;
            transport=new GeyserPositionTransport(geyser.getClass().getClassLoader());
            Object api=transport.api();bus=call(api,"eventBus");
            owner=call(transport.type("org.geysermc.geyser.api.event.EventRegistrar"),"of",plugin);
            subscribe("org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent",this::defineBlocks);
            subscribe("org.geysermc.geyser.api.event.lifecycle.GeyserDefineResourcePacksEvent",this::definePack);
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
        BedrockVisualPack.index(items); // Reject source collisions before display calibration.
        var calibration=BedrockColorCalibration.load(marker.getParent().resolve("color-calibration.yml"));
        calibration.validate(items);
        var originals=List.copyOf(items);
        items.replaceAll(calibration::apply);
        BedrockVisualPack.index(items);
        items.forEach(profile->profiles.put(profile.id(),profile));catalog=List.copyOf(items);
        BedrockVisualPack.write(pack,catalog);
        colorReport=new BedrockColorReport(originals,catalog,calibration,Files.readAllBytes(pack));
        colorReport.write(pack.getParent().resolve("color-report.yml"));
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
    public void enabled(PluginEnableEvent event) {
        if(event.getPlugin().getName().equals("Geyser-Spigot") && status.equals("WAITING_GEYSER_ENABLE"))initialize();
    }
    @EventHandler(priority=EventPriority.MONITOR)
    public void serverLoaded(ServerLoadEvent event) {
        if(status.equals("FAILED_DISABLED") || status.equals("GEYSER_ABSENT") || status.equals("DISABLED"))return;
        try {
            if(!defined || !packRegistered)throw new IllegalStateException("Geyser custom-content events missing; check gameplay.enable-custom-content and restart");
            int definitions=transport.validate(colors.values());status="READY_OPT_IN";
            plugin.getLogger().info("TERRA2_GEYSER_POSITION_READY colors="+colors.size()+" protocolDefinitions="+definitions+" pack="+packId);
            timer=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,20L,20L);
        } catch(Exception | LinkageError error){fail(error);}
    }
    public String status(){return status+"; grassColors="+colors.size()+"; packRegistered="+packRegistered+"; players="+players.size()+"; nativePlayers="+nativePlayers.size()+"; persistentWorlds="+(nativeWorlds==null?0:nativeWorlds.size());}
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
    public GeyserNativeBiomeTransport.Plan nativePlan(World world) throws Exception {
        if(!(world.getGenerator() instanceof com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper wrapper))throw new IllegalArgumentException("World has no Terra2 generator");
        if(wrapper.biomeCompatibility()==null || wrapper.biomeCompatibility().vanilla)throw new IllegalArgumentException("Native-biome prototype requires existing CUSTOM storage. Do not change a bound world's mode");
        var references=new TreeMap<String,String>();
        wrapper.getPack().getBiomeProvider().stream().forEach(biome->{
            var identity=wrapper.biomeCompatibility().identity(biome);references.put(identity.registeredId(),identity.vanillaReference());
        });
        return new GeyserNativeBiomeTransport(transport).plan(references,profiles);
    }
    public String startNative(Player player) throws Exception {return startNative(player,true);}
    private String startNative(Player player,boolean remember) throws Exception {
        if(!status.equals("READY_OPT_IN"))return "Prévia indisponível: "+status;
        plugin.assertGenerationAuthorized(player.getWorld().getName(),"PACKS");
        Object session=transport.connection(player.getUniqueId());if(session==null)return "Use um jogador conectado pelo Geyser local.";
        if(nativePlayers.size()>=64 && !nativePlayers.containsKey(player.getUniqueId()))return "Limite: 64 conexões na ponte nativa.";
        var plan=nativePlan(player.getWorld());stop(player,true);nativePaused.remove(player.getUniqueId());
        var state=new NativePreview(player.getWorld().getUID(),session,plan);nativePlayers.put(player.getUniqueId(),state);
        call(session,"executeInEventLoop",(Runnable)()->{
            if(!state.active)return;
            try {
                if((Boolean)call(session,"isClosed") || !(Boolean)call(session,"isSpawned"))throw new IllegalStateException("Geyser session closed");
                state.change=new GeyserNativeBiomeTransport(transport).install(session,plan);state.result="NATIVE_INSTALLED_EXPERIMENTAL";
                Bukkit.getScheduler().runTask(plugin,()->{
                    if(!state.active || !state.world.equals(player.getWorld().getUID()))return;
                    try {
                        var world=player.getWorld();plugin.assertGenerationAuthorized(world.getName(),"PACKS");
                        var wrapper=(com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper)world.getGenerator();
                        if(remember)rememberWorld(world);
                        state.result="NATIVE_PERSISTENT";plugin.visualRefresh().around(player);
                    }catch(Exception error){state.result="NATIVE_NOT_PERSISTED: "+error.getMessage();player.sendMessage(state.result);}
                });
            }catch(Exception | LinkageError error){state.result="NATIVE_FAILED: "+error.getMessage();
                Bukkit.getScheduler().runTask(plugin,()->{
                    player.sendMessage(state.result);
                    com.dfsek.terra.bukkit.util.GenerationReport.failure("Bedrock native biomes",player.getWorld().getName(),"CLIENT_BIOMES",error);
                });
            }
        });
        return "Ponte nativa solicitada. Após sucesso, este mundo será lembrado e aplicado aos jogadores Bedrock ao reconectar. A área próxima será reenviada sem regeneração. Use status; forget remove a ativação persistente deste mundo.";
    }
    private void tick() {
        if(!status.equals("READY_OPT_IN"))return;
        if(nativeWorlds!=null)for(Player player:Bukkit.getOnlinePlayers()) {
            if(nativePlayers.containsKey(player.getUniqueId()) || nativePaused.contains(player.getUniqueId()) || nativePlayers.size()>=64)continue;
            var world=player.getWorld();
            if(!(world.getGenerator() instanceof com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper wrapper) || wrapper.biomeCompatibility()==null || wrapper.biomeCompatibility().vanilla)continue;
            if(!nativeWorlds.matches(world.getUID(),world.getSeed(),wrapper.biomeCompatibility().fingerprint))continue;
            try {
                plugin.assertGenerationAuthorized(world.getName(),"PACKS");Object session=transport.connection(player.getUniqueId());
                if(session!=null && !(Boolean)call(session,"isClosed") && (Boolean)call(session,"isSpawned"))startNative(player,false);
            }catch(Exception refused){nativePaused.add(player.getUniqueId());}
        }
        for(UUID id:List.copyOf(nativePlayers.keySet())) {
            Player player=Bukkit.getPlayer(id);NativePreview state=nativePlayers.get(id);
            if(player==null || !player.isOnline()){stopNative(id);continue;}
            try {
                if(!state.world.equals(player.getWorld().getUID())){stopNative(id);continue;}
                plugin.assertGenerationAuthorized(player.getWorld().getName(),"PACKS");
                if(transport.connection(id)!=state.session){stopNative(id);continue;}
                if(state.change!=null && !state.checking && state.result.startsWith("NATIVE_PERSISTENT")) {
                    state.checking=true;call(state.session,"executeInEventLoop",(Runnable)()->{
                        try {
                            if(!state.active || (Boolean)call(state.session,"isClosed"))return;
                            var nativeTransport=new GeyserNativeBiomeTransport(transport);
                            if(nativeTransport.ownedMappings(state.change)!=state.change.installed().size()) {
                                if(!nativeTransport.canReapply(state.change)){state.result="NATIVE_MAPPING_CONFLICT";return;}
                                nativeTransport.restore(state.change);
                                state.change=nativeTransport.install(state.session,state.plan);state.result="NATIVE_PERSISTENT_REAPPLIED";
                                Bukkit.getScheduler().runTask(plugin,()->{if(state.active && state.world.equals(player.getWorld().getUID()))plugin.visualRefresh().around(player);});
                            }
                        }catch(Exception error){state.result="NATIVE_REAPPLY_FAILED: "+error.getMessage();}
                        finally{state.checking=false;}
                    });
                }
            }catch(Exception refused){stopNative(id);}
        }
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
    private void stopNative(UUID id) {
        NativePreview state=nativePlayers.remove(id);if(state==null)return;state.active=false;
        try {if((Boolean)call(state.session,"isClosed"))return;call(state.session,"executeInEventLoop",(Runnable)()->{
            try{if(state.change!=null)new GeyserNativeBiomeTransport(transport).restore(state.change);}
            catch(Exception error){plugin.getLogger().warning("Native biome mapping restore failed; reconnect: "+error);}
        });}catch(Exception error){plugin.getLogger().warning("Native biome session cleanup failed: "+error);}
    }
    public void pause(Player player){stop(player,true);nativePaused.add(player.getUniqueId());}
    public void rememberWorld(World world) throws Exception {
        plugin.assertGenerationAuthorized(world.getName(),"PACKS");nativePlan(world);
        var wrapper=(com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper)world.getGenerator();
        if(nativeWorlds==null)throw new IllegalStateException("Ponte não inicializada");
        nativeWorlds.enable(new NativeWorldStore.Binding(world.getUID(),world.getSeed(),world.getName(),wrapper.biomeCompatibility().fingerprint));
    }
    public boolean remembers(World world) {
        return nativeWorlds!=null && world.getGenerator() instanceof com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper wrapper
            && wrapper.biomeCompatibility()!=null && nativeWorlds.matches(world.getUID(),world.getSeed(),wrapper.biomeCompatibility().fingerprint);
    }
    public void forget(World world) throws Exception {
        if(nativeWorlds==null)throw new IllegalStateException("Ponte não inicializada");nativeWorlds.forget(world.getUID());
        for(UUID id:List.copyOf(nativePlayers.keySet()))if(nativePlayers.get(id).world.equals(world.getUID()))stopNative(id);
    }
    public void stop(Player player,boolean restore) {
        stopNative(player.getUniqueId());
        Preview state=players.remove(player.getUniqueId());if(state==null)return;state.active=false;state.epoch++;
        if(restore && player.getWorld().getUID().equals(state.world))for(Pos pos:state.overlay.keySet())restore(player,player.getWorld(),pos);
    }
    public List<String> colorDebug(Player player) {
        var lines=new ArrayList<String>();
        lines.add("Ponte: "+status()+"; "+playerStatus(player));
        if(colorReport==null){lines.add("Nenhum catálogo/pack compilado nesta ponte; use prepare e reinicie.");return lines;}
        lines.add(colorReport.metadata());
        var location=player.getLocation();var world=player.getWorld();
        lines.add("Mundo: "+world.getName()+"; Bukkit real: "+world.getBiome(location).getKey());
        var sample=PositionVisualResolver.at(world,location.getBlockX(),location.getBlockY(),location.getBlockZ());
        if(sample.isEmpty()){lines.add("Sem identidade Terra2 disponível nesta posição.");return lines;}
        var found=sample.get();lines.add("ID: "+found.profile()+"; leitura: "+found.source());
        lines.add("Client biome: "+BedrockClientBiomes.name(found.profile()));
        lines.addAll(colorReport.details(found.profile()));
        if(!world.getBiome(location).getKey().toString().equals(found.profile()))lines.add("ATENCAO: bioma Bukkit difere da identidade; o debug não comprova tradução efetiva deste chunk.");
        lines.add("Snapshot da compilação; editar YAML exige reinício. Não confirma renderização/aceitação no cliente.");return lines;
    }
    public String playerStatus(Player player) {
        NativePreview nativeState=nativePlayers.get(player.getUniqueId());
        if(nativeState!=null)return "Biomas nativos: "+nativeState.result+"; perfis="+nativeState.count+"; cliente/renderização não confirmados";
        Preview state=players.get(player.getUniqueId());return state==null?"Prévia desligada":"Blocos em prévia="+state.overlay.size()+"; updates submetidos="+state.submitted+" (não confirma renderização no cliente)";
    }
    @EventHandler public void quit(PlayerQuitEvent event){stop(event.getPlayer(),false);nativePaused.remove(event.getPlayer().getUniqueId());}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void teleport(PlayerTeleportEvent event){
        var player=event.getPlayer();
        if(event.getTo()==null || !player.getWorld().equals(event.getTo().getWorld())){stop(player,true);nativePaused.remove(player.getUniqueId());}
        else {var grass=players.remove(player.getUniqueId());if(grass!=null){grass.active=false;grass.epoch++;for(Pos pos:grass.overlay.keySet())restore(player,player.getWorld(),pos);}}
    }
    @EventHandler public void changedWorld(PlayerChangedWorldEvent event){stop(event.getPlayer(),false);nativePaused.remove(event.getPlayer().getUniqueId());}
    private void fail(Throwable error) {
        if(status.equals("FAILED_DISABLED"))return;status="FAILED_DISABLED";
        while(error instanceof java.lang.reflect.InvocationTargetException && error.getCause()!=null)error=error.getCause();
        final Throwable reason=error;
        Runnable cleanup=()->{
            for(UUID id:List.copyOf(nativePlayers.keySet()))stopNative(id);
            players.values().forEach(state->state.active=false);
            for(UUID id:List.copyOf(players.keySet())) {Player player=Bukkit.getPlayer(id);if(player!=null)stop(player,true);}
            plugin.getLogger().warning("Terra2 Bedrock preview disabled: "+reason);
            com.dfsek.terra.bukkit.util.GenerationReport.failure("Bedrock position preview","<client-preview>","RGB_PALETTE",reason);
        };
        if(Bukkit.isPrimaryThread())cleanup.run();else Bukkit.getScheduler().runTask(plugin,cleanup);
    }
    public void close() {
        for(UUID id:List.copyOf(nativePlayers.keySet()))stopNative(id);
        players.values().forEach(state->state.active=false);
        for(UUID id:List.copyOf(players.keySet())){Player player=Bukkit.getPlayer(id);if(player!=null)stop(player,true);}
        if(timer!=null)timer.cancel();
        if(bus!=null && owner!=null)try{call(bus,"unregisterAll",owner);}catch(Exception ignored){}
    }
}
