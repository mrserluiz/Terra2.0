package com.dfsek.terra.bukkit.util;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.dfsek.terra.bukkit.compatibility.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.persistence.PersistentDataType;
import org.terra2.api.biome.BiomeIdentity;
import java.util.*;
import static com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call;

/** WorldEdit selection adapter; the edit itself is synchronous, bounded and staged before mutation. */
public final class SetBiomeCommand {
    private final TerraBukkitPlugin plugin;
    public SetBiomeCommand(TerraBukkitPlugin plugin){this.plugin=plugin;}
    public List<String> complete(CommandSender sender,String prefix) {
        if(!(sender instanceof Player player) || !(player.getWorld().getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper))return List.of();
        return wrapper.getPack().getBiomeProvider().stream().map(b->b.getID()).distinct().filter(id->id.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))).sorted().limit(200).toList();
    }
    public boolean handle(CommandSender sender,String[] args) {
        if(args.length==0 || !args[0].equalsIgnoreCase("setbiome"))return false;
        if(args.length!=2 || !(sender instanceof Player player)){DiagnosticMessages.send(sender,"Uso em jogo: /terra2 setbiome <ID_DO_BIOMA> usando uma seleção cuboide do WorldEdit.");return true;}
        try {
            var world=player.getWorld();plugin.assertGenerationAuthorized(world.getName(),"PACKS");
            var we=Bukkit.getPluginManager().getPlugin("WorldEdit");if(we==null)we=Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
            if(we==null || !we.isEnabled())throw new IllegalArgumentException("WorldEdit/FAWE não está disponível");
            var loader=we.getClass().getClassLoader();
            // Player also implements Entity: select the exact overload, not the first compatible one.
            var adapter=Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter",false,loader);
            Object adapted=adapter.getMethod("adapt",Player.class).invoke(null,player);
            Object engine=call(Class.forName("com.sk89q.worldedit.WorldEdit",false,loader),"getInstance");
            Object session=call(call(engine,"getSessionManager"),"get",adapted);
            Object adaptedWorld=adapter.getMethod("adapt",World.class).invoke(null,world);
            Object region=call(session,"getSelection",adaptedWorld);
            var bounds=selectionBounds(region,loader);
            int changed=apply(world,args[1],bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX(),bounds.maxY(),bounds.maxZ());
            DiagnosticMessages.send(sender,"Bioma aplicado e identidade persistida: "+args[1]+"; células 4x4x4: "+changed+". Bordas são ajustadas à grade de biomas. Atualização visual enfileirada; terreno/estruturas não são regenerados.");
        }catch(Exception error){Throwable cause=error;while(cause instanceof java.lang.reflect.InvocationTargetException && cause.getCause()!=null)cause=cause.getCause();DiagnosticMessages.send(sender,"Setbiome recusado/falhou: "+cause.getMessage());}
        return true;
    }
    public record Bounds(int minX,int minY,int minZ,int maxX,int maxY,int maxZ) {}
    public static Bounds selectionBounds(Object region,ClassLoader loader) throws ReflectiveOperationException {
        if(!Class.forName("com.sk89q.worldedit.regions.CuboidRegion",false,loader).isInstance(region))throw new IllegalArgumentException("Use uma seleção cuboide: //sel cuboid");
        Object min=call(region,"getMinimumPoint"),max=call(region,"getMaximumPoint");
        return new Bounds(coordinate(min,"X"),coordinate(min,"Y"),coordinate(min,"Z"),coordinate(max,"X"),coordinate(max,"Y"),coordinate(max,"Z"));
    }
    private static int coordinate(Object vector,String axis) throws ReflectiveOperationException {return (Integer)call(vector,"getBlock"+axis);}
    private record Cell(int x,int y,int z,org.bukkit.block.Biome old) {}
    private record ChunkKey(int x,int z) {}
    public int apply(World world,String id,int minX,int minY,int minZ,int maxX,int maxY,int maxZ) throws Exception {
        if(!Bukkit.isPrimaryThread() || Bukkit.getName().equalsIgnoreCase("Folia"))throw new IllegalArgumentException("Esta edição exige Paper e a thread do mundo");
        plugin.assertGenerationAuthorized(world.getName(),"PACKS");
        if(!(world.getGenerator() instanceof BukkitChunkGeneratorWrapper wrapper) || wrapper.biomeCompatibility()==null)throw new IllegalArgumentException("Mundo não está vinculado ao Terra2");
        var biome=wrapper.getPack().getBiomeProvider().stream().filter(b->b.getID().equalsIgnoreCase(id)||wrapper.biomeCompatibility().identity(b).registeredId().equalsIgnoreCase(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Bioma desconhecido neste pack: "+id));
        BiomeIdentity identity=wrapper.biomeCompatibility().identity(biome);
        var target=wrapper.biomeCompatibility().vanilla?wrapper.biomeCompatibility().reference(biome):Registry.BIOME.get(NamespacedKey.fromString(identity.registeredId()));
        if(target==null)throw new IllegalArgumentException("Bioma não registrado no Paper: "+identity.registeredId());
        if(minX>maxX || minY>maxY || minZ>maxZ || minX< -30000000 || maxX>30000000 || minZ< -30000000 || maxZ>30000000)throw new IllegalArgumentException("Seleção inválida");
        int x0=minX&~3,x1=maxX&~3,z0=minZ&~3,z1=maxZ&~3,y0=Math.max(minY,world.getMinHeight())&~3,y1=Math.min(maxY,world.getMaxHeight()-1)&~3;
        long count=((long)x1-x0+4)/4*((long)z1-z0+4)/4*Math.max(0,((long)y1-y0+4)/4);
        if(count<1 || count>4096)throw new IllegalArgumentException("Limite: 4096 células de bioma (262144 blocos); reduza a seleção");
        var cells=new ArrayList<Cell>();var edits=new LinkedHashMap<ChunkKey,Map<Integer,BiomeIdentity>>();
        for(int x=x0;x<=x1;x+=4)for(int z=z0;z<=z1;z+=4) {
            if(!world.isChunkLoaded(x>>4,z>>4))throw new IllegalArgumentException("Carregue antes todos os chunks da seleção; nenhum chunk será gerado pela edição");
            var key=new ChunkKey(x>>4,z>>4);var indices=edits.computeIfAbsent(key,k->new HashMap<>());
            if(edits.size()>64)throw new IllegalArgumentException("Limite: 64 chunks por edição");
            for(int y=y0;y<=y1;y+=4){cells.add(new Cell(x,y,z,world.getBiome(x,y,z)));indices.put(((y-world.getMinHeight())>>2)*16+((z&15)>>2)*4+((x&15)>>2),identity);}
        }
        var oldPayloads=new HashMap<ChunkKey,byte[]>();var payloads=new LinkedHashMap<ChunkKey,byte[]>();
        for(var e:edits.entrySet()) {
            var chunk=world.getChunkAt(e.getKey().x(),e.getKey().z());byte[] previous=chunk.getPersistentDataContainer().get(BiomeEditOverrides.KEY,PersistentDataType.BYTE_ARRAY);oldPayloads.put(e.getKey(),previous);
            payloads.put(e.getKey(),BiomeEditOverrides.edit(previous,world.getUID(),world.getSeed(),chunk.getX(),chunk.getZ(),world.getMinHeight(),world.getMaxHeight()-world.getMinHeight(),wrapper.getPack().getID(),wrapper.biomeCompatibility().fingerprint,e.getValue(),wrapper.biomeCompatibility().identities()).encode());
        }
        int written=0;
        try {
            for(var cell:cells){world.setBiome(cell.x(),cell.y(),cell.z(),target);written++;}
            for(var e:payloads.entrySet())world.getChunkAt(e.getKey().x(),e.getKey().z()).getPersistentDataContainer().set(BiomeEditOverrides.KEY,PersistentDataType.BYTE_ARRAY,e.getValue());
        }catch(Exception failure) {
            try {
                for(int i=0;i<written;i++){var cell=cells.get(i);world.setBiome(cell.x(),cell.y(),cell.z(),cell.old());}
                for(var e:oldPayloads.entrySet()){var pdc=world.getChunkAt(e.getKey().x(),e.getKey().z()).getPersistentDataContainer();if(e.getValue()==null)pdc.remove(BiomeEditOverrides.KEY);else pdc.set(BiomeEditOverrides.KEY,PersistentDataType.BYTE_ARRAY,e.getValue());}
            }catch(Exception rollback){failure.addSuppressed(rollback);}
            GenerationReport.failure("Setbiome",world.getName(),wrapper.getPack().getID(),failure);throw failure;
        }
        for(var key:edits.keySet())plugin.visualRefresh().request(world,key.x(),key.z());
        return written;
    }
}
