import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.dfsek.terra.bukkit.config.PreLoadCompatibilityOptions;
import java.nio.file.*;
import java.util.*;
public final class AetherPackProbe extends JavaPlugin {
 public void onEnable(){Bukkit.getScheduler().runTaskLater(this,this::check,60L);}
 void check(){try {
  var world=new WorldCreator("terra2_aether_test").seed(42).generator("Terra2:AETHER").createWorld();
  if(world==null||!(world.getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper)||!wrapper.getPack().getID().equals("AETHER"))throw new IllegalStateException("AETHER generator missing");
  var pack=wrapper.getPack(); var policy=pack.getContext().get(PreLoadCompatibilityOptions.class);
  if(policy.vanillaFeatures()||policy.vanillaStructures()||policy.isBeard()||wrapper.shouldGenerateStructures()||wrapper.shouldGenerateCaves())throw new IllegalStateException("Vanilla generation not isolated");
  long count=pack.getBiomeProvider().stream().map(b->b.getID()).distinct().count();if(count!=4)throw new IllegalStateException("Expected four biomes: "+count);
  var terra=(com.dfsek.terra.bukkit.TerraBukkitPlugin)Bukkit.getPluginManager().getPlugin("Terra2");
  var nativeNoise=terra.platform().oldBlendedNoise(0.25,0.25,80,160,8);
  var properties=new com.dfsek.terra.bukkit.world.BukkitWorldProperties(world);
  int parity=0;
  for(int x:new int[]{-129,-9,-1,0,7,15,127})for(int z:new int[]{-129,1,125})for(int y=0;y<=128;y++){
   double density=reference(nativeNoise,42,x,y,z);
   boolean solid=!wrapper.getHandle().getBlock(properties,x,y,z,pack.getBiomeProvider()).isAir();
   if(solid!=(density>0))throw new IllegalStateException("Density mismatch at "+x+","+y+","+z+" expected="+density+" solid="+solid);
   parity++;
  }
  var observed=new TreeSet<String>();
  for(int x=-4096;x<=4096;x+=64)for(int z=-4096;z<=4096;z+=64)observed.add(pack.getBiomeProvider().getBiome(x,64,z,42).getID());
  if(observed.size()!=4)throw new IllegalStateException("Four biomes not distributed: "+observed);
  var state=getDataFolder().toPath().resolve("checkpoint.txt");Files.createDirectories(state.getParent());
  var digest=java.security.MessageDigest.getInstance("SHA-256");
  int solids=0,voids=0,starts=0;var trees=new TreeMap<String,Integer>();
  Object level=world.getClass().getMethod("getHandle").invoke(world);
  var getChunk=level.getClass().getMethod("getChunk",int.class,int.class);
  for(int x=-512;x<=512;x+=128)for(int z=-512;z<=512;z+=128){
   var c=world.getChunkAt(x>>4,z>>4);boolean land=false;
   Object nativeChunk=getChunk.invoke(level,x>>4,z>>4);
   starts+=((Map<?,?>)nativeChunk.getClass().getMethod("getAllStarts").invoke(nativeChunk)).size();
   for(int y=0;y<=160;y++){
    var b=c.getBlock(x&15,y,z&15);String data=b.getBlockData().getAsString();digest.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    if(y<8&&!b.getType().isAir())throw new IllegalStateException("Unexpected blocks below island floor at "+x+","+y+","+z);
    if(y>=8&&y<=128&&!b.getType().isAir())land=true;
   }
   String biome=pack.getBiomeProvider().getBiome(x,64,z,42).getID();digest.update(biome.getBytes(java.nio.charset.StandardCharsets.UTF_8));
   int logs=0;for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)for(int y=8;y<150;y++)if(c.getBlock(dx,y,dz).getType().name().endsWith("_LOG"))logs++;
   trees.merge(biome,logs,Integer::sum);
   if(land)solids++;else voids++;
  }
  if(trees.values().stream().mapToInt(Integer::intValue).sum()==0)throw new IllegalStateException("Terra vegetation stages did not run");
  if(solids==0||voids==0||starts!=0)throw new IllegalStateException("Expected island/void without native structures: "+solids+"/"+voids+" starts="+starts);
  String checkpoint=world.getUID()+"|"+HexFormat.of().formatHex(digest.digest());
  if(Files.exists(state)&&!Files.readString(state).equals(checkpoint))throw new IllegalStateException("Saved terrain/biomes changed after restart");
  if(!Bukkit.getWorld("world").getBiome(0,64,0).getKey().getNamespace().equals("minecraft"))throw new IllegalStateException("Primary world was modified");
  world.save();Files.writeString(state,checkpoint);
  getLogger().info("AETHER_PACK_OK biomes="+observed+" landColumns="+solids+" voidColumns="+voids+" parityBlocks="+parity+" vanillaStarts="+starts+" logsByBiome="+trees+" saveRestart=true visualTest=false");
 }catch(Throwable failure){getLogger().log(java.util.logging.Level.SEVERE,"AETHER_PACK_FAILED",failure);}finally{Bukkit.shutdown();}}
 static double reference(com.dfsek.seismic.type.sampler.Sampler noise,long seed,int x,int y,int z){
  int bx=Math.floorDiv(x,8)*8,by=Math.floorDiv(y,4)*4,bz=Math.floorDiv(z,8)*8;
  double value=0;
  for(int ix=0;ix<=1;ix++)for(int iy=0;iy<=1;iy++)for(int iz=0;iz<=1;iz++){
   int cy=by+iy*4;
   double lower=Math.max(0,Math.min(1,(cy-8)/32.0)),upper=Math.max(0,Math.min(1,(128-cy)/72.0));
   double corner=-0.15+lower*(-0.1+upper*(0.07+noise.getSample(seed,bx+ix*8,cy,bz+iz*8)));
   value+=corner*(ix==0?1-(x-bx)/8.0:(x-bx)/8.0)*(iy==0?1-(y-by)/4.0:(y-by)/4.0)*(iz==0?1-(z-bz)/8.0:(z-bz)/8.0);
  }
  return value;
 }
}
