import java.nio.file.*;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.bedrock.PositionVisualResolver;

/** Uses an official downloaded Geyser. No renderer/client acceptance is simulated. */
public final class OfficialGeyserPreviewProbe extends JavaPlugin {
    public static final class CaptureSession {
        public Object registryCache;
        public Object getRegistryCache(){return registryCache;}
        public final java.util.List<Object> packets=new java.util.ArrayList<>();
        public boolean isClosed(){return false;}
        public boolean isSpawned(){return true;}
        public void executeInEventLoop(Runnable task){task.run();}
        public void sendUpstreamPacket(Object packet){packets.add(packet);}
    }
    public void onEnable(){Bukkit.getScheduler().runTaskLater(this,this::check,100L);}
    void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    void check(){
        try {
            var terra=(TerraBukkitPlugin)Bukkit.getPluginManager().getPlugin("Terra2");
            require(terra!=null&&terra.isEnabled(),"Terra2 enabled with official Geyser");
            require(terra.bedrockPreview().status().startsWith("READY_OPT_IN"),terra.bedrockPreview().status());
            int definitions=terra.bedrockPreview().validateProtocol();require(definitions>0,"Real packet runtime IDs validated");
            var transport=new com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport(Bukkit.getPluginManager().getPlugin("Geyser-Spigot").getClass().getClassLoader());
            Object update=terra.bedrockPreview().diagnosticPacket(-16,64,32);
            Object position=com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(update,"getBlockPosition");
            require((int)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(position,"getX")==-16,"Packet retains requested position");
            var captured=new CaptureSession();var count=new java.util.concurrent.atomic.AtomicInteger();
            transport.submit(captured,java.util.List.of(update),()->true,count::incrementAndGet,error->{throw new AssertionError(error);});
            require(captured.packets.size()==1 && captured.packets.get(0)==update && count.get()==1,"Real protocol packet reaches isolated upstream capture");
            transport.submit(captured,java.util.List.of(update),()->false,count::incrementAndGet,error->{throw new AssertionError(error);});
            require(captured.packets.size()==1,"Stale preview batch is not emitted");
            Object registry=transport.type("org.geysermc.geyser.registry.BlockRegistries").getField("CUSTOM_BLOCK_STATE_OVERRIDES").get(null);
            require(((java.util.Map<?,?>)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(registry,"get")).isEmpty(),"No global Java block override installed");
            var world=new WorldCreator("terra2_bedrock_test").seed(42).generator("Terra2:HYDRAXIA").createWorld();
            require(world!=null,"Authorized world created");world.getChunkAt(0,0);
            var sample=PositionVisualResolver.at(world,0,64,0).orElseThrow();
            require(sample.source().equals("PERSISTED_IDENTITY"),"Visual position uses saved identity, not Bukkit mask");
            require(sample.profile().startsWith("terra:"),"Original visual profile retained");
            require(PositionVisualResolver.at(Bukkit.getWorld("world"),0,64,0).isEmpty(),"Primary world has no Terra preview profile");
            require(PositionVisualResolver.at(world,1600000,64,1600000).isEmpty(),"No unloaded-chunk lookup/generation");
            byte[] identity=world.getChunkAt(0,0).getPersistentDataContainer().get(com.dfsek.terra.bukkit.compatibility.PersistedBiomeService.KEY,org.bukkit.persistence.PersistentDataType.BYTE_ARRAY);
            String checksum=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(identity));
            Path state=getDataFolder().toPath().resolve("identity.sha256");Files.createDirectories(state.getParent());
            if(Files.exists(state))require(Files.readString(state).equals(checksum),"Identity survives preview registration/restart");else Files.writeString(state,checksum);
            var nativeTransport=new com.dfsek.terra.bukkit.bedrock.GeyserNativeBiomeTransport(transport);
            var profile=new org.terra2.api.visual.VisualProfile(sample.profile(),java.util.Map.of("water",0x9B42FF,"grass",0xFFD1EF,"foliage",0xFF99CC));
            Object biomeHolder=transport.type("org.geysermc.geyser.registry.Registries").getField("BIOMES").get(null);
            Object baseline=com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(biomeHolder,"get");
            var baselineDefinitions=new java.util.HashMap<>((java.util.Map<?,?>)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(baseline,"getDefinitions"));
            var plan=nativeTransport.plan(java.util.Map.of(profile.id(),"minecraft:plains"),java.util.Map.of(profile.id(),profile));
            require(nativeTransport.validateCodecs(plan)>0,"Native biome packet encodes with actual supported official Geyser codecs");
            require(plan.ids().size()==1 && plan.ids().get(profile.id())>=30000,"Custom native biome IDs are allocated separately");
            Object nativeDefinition=plan.definitions().get(com.dfsek.terra.bukkit.bedrock.BedrockClientBiomes.name(profile.id()));
            var water=(java.awt.Color)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(nativeDefinition,"getMapWaterColor");
            require((water.getRGB()&0xFFFFFF)==0x9B42FF,"Native biome retains exact water RGB");
            Object base=baselineDefinitions.get("minecraft:plains");
            require(water.getAlpha()==((java.awt.Color)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(base,"getMapWaterColor")).getAlpha(),"Water opacity inherited from vanilla");
            require(baselineDefinitions.equals(com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(baseline,"getDefinitions")),"Global Geyser biome definitions untouched");
            var cache=transport.type("org.geysermc.geyser.session.cache.RegistryCache").getConstructors()[0].newInstance(new Object[]{null});
            Object key=transport.type("org.geysermc.geyser.session.cache.registry.JavaRegistries").getField("BIOME").get(null);
            Object registry=com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(cache,"registry",key);
            var entryType=transport.type("org.geysermc.geyser.session.cache.registry.RegistryEntryData");
            var keyType=transport.type("org.geysermc.geyser.util.MinecraftKey");
            Object vanillaEntry=entryType.getConstructors()[0].newInstance(0,com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(keyType,"key","minecraft:plains"),1);
            Object terraEntry=entryType.getConstructors()[0].newInstance(1,com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(keyType,"key",profile.id()),-1);
            com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(registry,"reset",java.util.List.of(vanillaEntry,terraEntry));captured.registryCache=cache;
            var change=nativeTransport.install(captured,plan);
            require(captured.packets.get(captured.packets.size()-1)==plan.packet(),"Native definition packet submitted before remapping chunks");
            var changed=(java.util.List<?>)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(registry,"entries");
            require(changed.get(0)==vanillaEntry && changed.get(1)!=terraEntry,"Only native Terra entry changed, vanilla entry preserved");
            require(nativeTransport.restore(change)==1,"Session mapping restored");
            require(((java.util.List<?>)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(registry,"entries")).get(1)==terraEntry,"Original Terra entry restored exactly");
            var secondChange=nativeTransport.install(captured,plan);
            Object foreign=entryType.getConstructors()[0].newInstance(1,com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(keyType,"key",profile.id()),17);
            com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(registry,"reset",java.util.List.of(vanillaEntry,foreign));
            require(nativeTransport.restore(secondChange)==0,"Restore preserves a later mapping owned by another integration");
            try{terra.bedrockPreview().nativePlan(world);throw new AssertionError("Projected world must refuse native-only prototype");}catch(IllegalArgumentException expected){}
            var nativeWorld=new WorldCreator("terra2_native_biome_test").seed(42).generator("Terra2:HYDRAXIA").createWorld();require(nativeWorld!=null,"Native test world created");
            var complete=terra.bedrockPreview().nativePlan(nativeWorld);require(nativeTransport.validateCodecs(complete)>0,"Complete HYDRAXIA biome packet encodes");require(complete.ids().size()>100,"All HYDRAXIA reference names resolve to real Bedrock definitions");nativeWorld.save();
            getLogger().info("TERRA2_NATIVE_BIOME_TRANSPORT_OK waterRGB=9B42FF liquid=VANILLA sessionCapture=true clientVisualTest=false");
            world.save();
            getLogger().info("TERRA2_OFFICIAL_GEYSER_PREVIEW_OK definitions="+definitions+" profile="+sample.profile());
        } catch(Throwable error){getLogger().log(java.util.logging.Level.SEVERE,"TERRA2_OFFICIAL_GEYSER_PREVIEW_FAILED",error);}
        finally{Bukkit.shutdown();}
    }
}
