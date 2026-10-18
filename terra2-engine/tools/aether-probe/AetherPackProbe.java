import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import java.nio.file.*;
public final class AetherPackProbe extends JavaPlugin {
 public void onEnable(){Bukkit.getScheduler().runTaskLater(this,this::check,60L);}
 void check(){try {
  var world=new WorldCreator("terra2_aether_test").seed(42).generator("Terra2:AETHER").createWorld();
  if(world==null||!(world.getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper)||!wrapper.getPack().getID().equals("AETHER"))throw new IllegalStateException("AETHER generator missing");
  long count=wrapper.getPack().getBiomeProvider().stream().map(b->b.getID()).distinct().count();if(count!=4)throw new IllegalStateException("Expected four biomes: "+count);
  var state=getDataFolder().toPath().resolve("checkpoint.txt");Files.createDirectories(state.getParent());
  var block=world.getBlockAt(0,96,0).getBlockData().getAsString();
  if(Files.exists(state)&&!Files.readString(state).equals(world.getUID()+"|"+block))throw new IllegalStateException("Restart checkpoint changed");
  int solids=0,voids=0;
  for(int x=-512;x<=512;x+=128)for(int z=-512;z<=512;z+=128){var c=world.getChunkAt(x>>4,z>>4);boolean land=false;for(int y=64;y<=128;y++)if(!c.getBlock(x&15,y,z&15).getType().isAir()){land=true;break;}if(land)solids++;else voids++;}
  if(solids==0||voids==0)throw new IllegalStateException("Expected island and void columns: "+solids+"/"+voids);
  world.save();Files.writeString(state,world.getUID()+"|"+block);
  getLogger().info("AETHER_PACK_OK biomes="+count+" landColumns="+solids+" voidColumns="+voids+" saveRestart=true visualTest=false");
 }catch(Throwable failure){getLogger().log(java.util.logging.Level.SEVERE,"AETHER_PACK_FAILED",failure);}finally{Bukkit.shutdown();}}
}
