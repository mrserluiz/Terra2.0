import java.nio.file.*;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.bedrock.PositionVisualResolver;

/** Uses an official downloaded Geyser. No renderer/client acceptance is simulated. */
public final class OfficialGeyserPreviewProbe extends JavaPlugin {
    public static final class CaptureSession {
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
            world.save();
            getLogger().info("TERRA2_OFFICIAL_GEYSER_PREVIEW_OK definitions="+definitions+" profile="+sample.profile());
        } catch(Throwable error){getLogger().log(java.util.logging.Level.SEVERE,"TERRA2_OFFICIAL_GEYSER_PREVIEW_FAILED",error);}
        finally{Bukkit.shutdown();}
    }
}
