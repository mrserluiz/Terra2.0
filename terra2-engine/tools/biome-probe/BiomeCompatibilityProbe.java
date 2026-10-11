import java.nio.file.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.compatibility.*;
import org.terra2.api.biome.*;

/** Disposable real-Paper validation; never included in the production JAR. */
public final class BiomeCompatibilityProbe extends JavaPlugin {
    public void onEnable(){Bukkit.getScheduler().runTaskLater(this,this::check,20L);}
    void require(boolean valid,String why){if(!valid)throw new IllegalStateException(why);}
    void check(){
        try {
            var terra=(TerraBukkitPlugin)Bukkit.getPluginManager().getPlugin("Terra2");
            require(terra!=null&&terra.isEnabled(),"Terra2 enabled");
            var api=Bukkit.getServicesManager().load(WorldBiomeService.class);
            require(api!=null&&api.apiVersion()==1,"Persisted identity service registered");
            require(api.at(Bukkit.getWorld("world"),0,64,0).isEmpty(),"Primary world untouched");
            var world=new WorldCreator("terra2_biome_compat").seed(42).generator("Terra2:HYDRAXIA").createWorld();
            var control=new WorldCreator("terra2_biome_control").seed(42).generator("Terra2:HYDRAXIA").createWorld();
            require(world!=null&&control!=null,"Both fixture worlds created");
            world.getChunkAt(0,0); control.getChunkAt(0,0);
            var identity=api.at(world,0,64,0).orElseThrow(()->new IllegalStateException("Missing identity at surface"));
            require(identity.registeredId().startsWith("terra:"),"Original registry identity retained");
            require(world.getBiome(0,64,0).getKey().toString().equals(identity.vanillaReference()),"Unmodified Bukkit consumer sees the vanilla equivalent");
            require(control.getBiome(0,64,0).getKey().toString().startsWith("terra:"),"Default custom mode unchanged");
            require(api.at(control,0,64,0).isEmpty(),"No metadata retrofit on default mode");
            for(int y=world.getMinHeight();y<world.getMaxHeight();y+=4) {
                var item=api.at(world,0,y,0).orElseThrow();
                require(world.getBiome(0,y,0).getKey().toString().equals(item.vanillaReference()),"Vertical vanilla reference at "+y);
            }
            // Negative coordinates and vertical bounds, independent of the current Bukkit biome.
            world.getChunkAt(-1,-2);
            require(api.at(world,-16,-64,-32).isPresent(),"Negative chunk coordinates preserved");
            require(api.at(world,0,world.getMaxHeight(),0).isEmpty(),"Height bounds");
            require(api.at(world,16*100000,64,16*100000).isEmpty(),"Unloaded lookup returns empty without generation");
            require(!world.isChunkLoaded(100000,100000),"Lookup did not load a chunk");
            int compared=0;
            for(int x=0;x<16;x+=3) for(int z=0;z<16;z+=3) for(int y=-60;y<200;y+=7) {
                require(world.getBlockAt(x,y,z).getBlockData().getAsString().equals(control.getBlockAt(x,y,z).getBlockData().getAsString()),"Terrain/decorations changed at "+x+","+y+","+z);
                compared++;
            }
            var chunk=world.getChunkAt(0,0); var pdc=chunk.getPersistentDataContainer();
            byte[] bytes=pdc.get(PersistedBiomeService.KEY,PersistentDataType.BYTE_ARRAY);
            require(bytes!=null,"Identity is in the same chunk persistent data");
            var decoded=BiomeIdentityChunk.decode(bytes);
            require(decoded.world.equals(world.getUID())&&decoded.seed==world.getSeed(),"World and seed recorded");
            String before=world.getBiome(0,64,0).getKey().toString();
            world.setBiome(0,64,0,org.bukkit.block.Biome.DESERT);
            require(api.at(world,0,64,0).orElseThrow().equals(identity),"Engine identity unaffected by external biome mutation");
            world.setBiome(0,64,0,Registry.BIOME.get(NamespacedKey.fromString(before)));
            byte[] corrupted=bytes.clone();corrupted[30]^=1;
            pdc.set(PersistedBiomeService.KEY,PersistentDataType.BYTE_ARRAY,corrupted);
            require(api.at(world,0,64,0).isEmpty(),"Corruption fails closed without reconstruction");
            pdc.set(PersistedBiomeService.KEY,PersistentDataType.BYTE_ARRAY,bytes);
            Path settings=terra.getDataFolder().toPath().resolve("terra2-settings.yml");String original=Files.readString(settings);
            try {
                Files.writeString(settings,original.replace("mode: VANILLA_COMPATIBLE","mode: CUSTOM"));
                boolean refused=false;try{terra.reloadGenerationSettings();}catch(Exception expected){refused=true;}
                require(refused&&api.at(world,0,64,0).isPresent(),"Mode reload rejected with active state preserved");
            }finally{Files.writeString(settings,original);}
            Path state=getDataFolder().toPath().resolve("identity.txt");Files.createDirectories(state.getParent());
            String value=identity.toString()+"\n"+HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
            if(Files.exists(state))require(Files.readString(state).equals(value),"Exact persisted identity survives restart");
            else Files.writeString(state,value);
            world.save();control.save();
            getLogger().info("TERRA2_BIOME_COMPAT_OK reference="+identity.vanillaReference()+" original="+identity.registeredId()+" comparedBlocks="+compared);
        }catch(Throwable failed){getLogger().log(java.util.logging.Level.SEVERE,"TERRA2_BIOME_COMPAT_FAILED",failed);}
        finally{Bukkit.shutdown();}
    }
}
