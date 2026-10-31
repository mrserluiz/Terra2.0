import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.google.gson.*;

/** Isolated actual chunk/registry test. No client, renderer or other biome-writing plugin is simulated. */
public final class BiomeSaveProbe extends JavaPlugin {
    private final int[][] chunks={{20,20},{-21,-21},{0,0}};
    public void onEnable(){Bukkit.getScheduler().runTaskLater(this,this::check,40);}
    private void check(){
        JsonObject report=new JsonObject();
        try {
            var terra=(TerraBukkitPlugin)Bukkit.getPluginManager().getPlugin("Terra2");
            if(terra==null || !terra.isEnabled())throw new IllegalStateException("Terra2 is disabled");
            var world=new WorldCreator("terra2_aether_save_probe").seed(42).generator("Terra2:AETHER").createWorld();
            if(world==null)throw new IllegalStateException("World not created");
            var wrapper=(BukkitChunkGeneratorWrapper)world.getGenerator();
            if(wrapper.biomeCompatibility().vanilla)throw new IllegalStateException("Probe requires CUSTOM mode");
            report.addProperty("engine",terra.getDescription().getVersion());
            report.addProperty("pack",wrapper.getPack().getID()+"@"+wrapper.getPack().getVersion());
            report.addProperty("world",world.getUID().toString());
            for(var c:chunks)world.getChunkAt(c[0],c[1]);
            String before=snapshot(world,wrapper,report);
            world.save();
            // Generation adds transient chunk tickets; return to the tick loop before requesting unload.
            Bukkit.getScheduler().runTaskLater(this,()->finish(terra,world,wrapper,report,before,0),120);
            return;
        } catch(Throwable error) { fail(report,error); }
    }
    private void finish(TerraBukkitPlugin terra,World world,BukkitChunkGeneratorWrapper wrapper,JsonObject report,String before,int tries) {
        try {
            if(world.isChunkLoaded(20,20)) {
                world.unloadChunkRequest(20,20);
                if(tries<40){Bukkit.getScheduler().runTaskLater(this,()->finish(terra,world,wrapper,report,before,tries+1),5);return;}
                throw new IllegalStateException("Could not unload test chunk after transient tickets expired");
            }
            report.addProperty("unloaded",true);
            String unloaded=world.getBiome(320,world.getMinHeight(),320).getKey().toString();
            String expected=wrapper.biomeCompatibility().identity(wrapper.getPack().getBiomeProvider()
                .getBiome(320,world.getMinHeight(),320,world.getSeed())).registeredId();
            if(!unloaded.equals(expected))throw new IllegalStateException("Unloaded biome resolver mismatch: "+unloaded+" expected="+expected);
            report.addProperty("unloadedResolverChecked",true);
            world.getChunkAt(20,20);
            String after=snapshot(world,wrapper,report);
            if(!before.equals(after))throw new IllegalStateException("Actual saved biomes changed on chunk unload/reload");
            var checkpoint=getDataFolder().toPath().resolve("checkpoint.txt");Files.createDirectories(checkpoint.getParent());
            String signature=world.getUID()+"|"+before;
            boolean restarted=Files.exists(checkpoint);
            if(restarted && !Files.readString(checkpoint).equals(signature))throw new IllegalStateException("Actual saved biomes or registry colors changed across restart");
            Files.writeString(checkpoint,signature);
            report.addProperty("restartChecked",restarted);report.addProperty("sha256",before);
            report.addProperty("status","PASSED");report.addProperty("realClientTest",false);
            world.save();
            getLogger().info("TERRA2_ACTUAL_BIOME_SAVE_OK restart="+restarted+" unloadReload=true sha256="+before);
        } catch(Throwable error) { fail(report,error);return; }
        saveAndStop(report);
    }
    private void fail(JsonObject report,Throwable error) {
        report.addProperty("status","FAILED");report.addProperty("error",error.toString());
        getLogger().log(java.util.logging.Level.SEVERE,"TERRA2_ACTUAL_BIOME_SAVE_FAILED",error);
        saveAndStop(report);
    }
    private void saveAndStop(JsonObject report) {
            try{Files.createDirectories(getDataFolder().toPath());Files.writeString(getDataFolder().toPath().resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}
            catch(Exception error){getLogger().log(java.util.logging.Level.SEVERE,"Probe report write failed",error);}
            Bukkit.shutdown();
    }
    private String snapshot(World world,BukkitChunkGeneratorWrapper wrapper,JsonObject report)throws Exception {
        var digest=MessageDigest.getInstance("SHA-256");int count=0;Set<String> observed=new TreeSet<>();
        for(var c:chunks) {
          var snapshot=world.getChunkAt(c[0],c[1]).getChunkSnapshot(false,true,false);
          for(int y=world.getMinHeight();y<world.getMaxHeight();y+=4)for(int dz=0;dz<16;dz+=4)for(int dx=0;dx<16;dx+=4) {
            int x=(c[0]<<4)+dx,z=(c[1]<<4)+dz;
            String actual=snapshot.getBiome(dx,y,dz).getKey().toString();
            String expected=wrapper.biomeCompatibility().identity(wrapper.getPack().getBiomeProvider().getBiome(x,y,z,world.getSeed())).registeredId();
            if(!actual.equals(expected))throw new IllegalStateException("Actual chunk biome mismatch at "+x+","+y+","+z+": saved="+actual+" expected="+expected);
            digest.update((actual+"\n").getBytes(StandardCharsets.UTF_8));observed.add(actual);count++;
          }
        }
        report.addProperty("loadedChunkSnapshots",true);
        var terra=(TerraBukkitPlugin)Bukkit.getPluginManager().getPlugin("Terra2");
        var colors=new TreeMap<>(terra.platform().bedrockBiomeAppearances());
        JsonObject registry=new JsonObject();
        for(var e:colors.entrySet())if(e.getKey().startsWith("terra:aether/")) {
            var effects=e.getValue().getAsJsonObject("effects");
            if(!effects.has("grass_color") || !effects.has("foliage_color"))throw new IllegalStateException("Missing custom registry tint: "+e.getKey());
            registry.add(e.getKey(),e.getValue());digest.update((e.getKey()+"="+e.getValue()+"\n").getBytes(StandardCharsets.UTF_8));
        }
        if(registry.size()!=5)throw new IllegalStateException("Expected all five AETHER 0.5.1 registry profiles");
        report.addProperty("actualQuartCells",count);report.add("observedActualIds",new Gson().toJsonTree(observed));report.add("registryColors",registry);
        return HexFormat.of().formatHex(digest.digest());
    }
}
