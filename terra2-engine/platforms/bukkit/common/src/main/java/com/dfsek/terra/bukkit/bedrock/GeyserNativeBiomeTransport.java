package com.dfsek.terra.bukkit.bedrock;

import org.terra2.api.visual.VisualProfile;
import java.awt.Color;
import java.lang.reflect.Constructor;
import java.util.*;
import static com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call;

/** Experimental, session-local biome translation. No global registries or saved chunks are mutated. */
public final class GeyserNativeBiomeTransport {
    private static final Map<String,String> BEDROCK_NAMES=Map.ofEntries(
        Map.entry("badlands","mesa"),Map.entry("dark_forest","roofed_forest"),Map.entry("eroded_badlands","mesa_bryce"),
        Map.entry("ice_spikes","ice_plains_spikes"),Map.entry("mushroom_fields","mushroom_island"),Map.entry("nether_wastes","hell"),
        Map.entry("old_growth_birch_forest","birch_forest_mutated"),Map.entry("old_growth_pine_taiga","mega_taiga"),Map.entry("old_growth_spruce_taiga","redwood_taiga_mutated"),
        Map.entry("snowy_beach","cold_beach"),Map.entry("snowy_plains","ice_plains"),Map.entry("snowy_taiga","cold_taiga"),
        Map.entry("soul_sand_valley","soulsand_valley"),Map.entry("sparse_jungle","jungle_edge"),Map.entry("stony_shore","stone_beach"),
        Map.entry("swamp","swampland"),Map.entry("windswept_forest","extreme_hills_plus_trees"),Map.entry("windswept_gravelly_hills","extreme_hills_mutated"),
        Map.entry("windswept_hills","extreme_hills"),Map.entry("windswept_savanna","savanna_mutated"),Map.entry("wooded_badlands","mesa_plateau_stone"),
        Map.entry("end_barrens","the_end"),Map.entry("end_highlands","the_end"),Map.entry("end_midlands","the_end"),Map.entry("small_end_islands","the_end"),Map.entry("the_void","ocean"));
    private final GeyserPositionTransport transport;
    private final boolean modern;
    public record Plan(Object packet, Map<String,Integer> ids, Map<String,Object> definitions) {}
    public record Change(Object registry, Map<Integer,Object> original, Map<Integer,Object> installed) {}
    public GeyserNativeBiomeTransport(GeyserPositionTransport transport){
        this.transport=transport;boolean found;
        try{transport.type("org.geysermc.geyser.registry.java.JavaRegistryKey");found=true;}
        catch(ClassNotFoundException absent){found=false;}
        modern=found;
    }
    public Plan plan(Map<String,String> references,Map<String,VisualProfile> profiles) throws Exception {
        if(references.isEmpty() || references.size()>1024)throw new IllegalArgumentException("Native preview needs 1..1024 world biomes");
        Object holder=transport.type("org.geysermc.geyser.registry.Registries").getField("BIOMES").get(null);
        Object vanilla=call(holder,"get");
        Map<?,?> baseline=(Map<?,?>)call(vanilla,"getDefinitions");
        var definitions=new LinkedHashMap<String,Object>();var occupied=new HashSet<Integer>();
        for(var e:baseline.entrySet()) {definitions.put((String)e.getKey(),e.getValue());Object id=call(e.getValue(),"getId");if(id!=null)occupied.add((Integer)id);}
        var ids=new TreeMap<String,Integer>();int next=30000;
        for(var e:new TreeMap<>(references).entrySet()) {
            if(!e.getKey().startsWith("terra:") || !e.getValue().startsWith("minecraft:"))throw new IllegalArgumentException("Only Terra identities and vanilla references are allowed");
            VisualProfile profile=profiles.get(e.getKey());if(profile==null)continue;
            String reference=e.getValue().substring(10);String bedrock="minecraft:"+BEDROCK_NAMES.getOrDefault(reference,reference);
            Object base=definitions.get(bedrock);if(base==null)throw new IllegalArgumentException("Missing Bedrock vanilla reference: "+e.getValue());
            String name=BedrockClientBiomes.name(e.getKey());if(definitions.containsKey(name))throw new IllegalStateException("Custom biome name collision: "+name);
            while(occupied.contains(next))next++;if(next>32767)throw new IllegalStateException("Custom biome ID range exhausted");
            Object definition=cloneDefinition(base,next,profile.colors().get("water"));definitions.put(name,definition);ids.put(e.getKey(),next);occupied.add(next++);
        }
        if(ids.isEmpty())throw new IllegalStateException("No exported profiles match this world's biome identities");
        Object data=transport.type("org.cloudburstmc.protocol.bedrock.data.biome.BiomeDefinitions").getConstructor(Map.class).newInstance(definitions);
        Object packet=transport.type("org.cloudburstmc.protocol.bedrock.packet.BiomeDefinitionListPacket").getConstructor().newInstance();call(packet,"setBiomes",data);
        return new Plan(packet,Map.copyOf(ids),Collections.unmodifiableMap(definitions));
    }
    private Object cloneDefinition(Object base,int id,Integer water) throws Exception {
        Object[] args=new Object[14];args[0]=id;
        String[] fields={"Temperature","Downfall","RedSporeDensity","BlueSporeDensity","AshDensity","WhiteAshDensity","FoliageSnow","Depth","Scale"};
        for(int i=0;i<fields.length;i++)args[i+1]=call(base,"get"+fields[i]);
        Color original=(Color)call(base,"getMapWaterColor");
        args[10]=water==null?original:new Color((water>>16)&255,(water>>8)&255,water&255,original.getAlpha());
        args[11]=call(base,"isRain");args[12]=call(base,"getTags");args[13]=call(base,"getChunkGenData");
        for(Constructor<?> constructor:base.getClass().getConstructors())
            if(constructor.getParameterCount()==14 && constructor.getParameterTypes()[12]==List.class)return constructor.newInstance(args);
        throw new NoSuchMethodException("Supported Bedrock biome definition constructor missing");
    }
    /** Encode with every codec actually supported by this official Geyser; this is not a render test. */
    public int validateCodecs(Plan plan) throws Exception {
        Class<?> protocol=transport.type("org.geysermc.geyser.network.bedrock.GameProtocol");
        var versions=(Iterable<?>)protocol.getField("SUPPORTED_BEDROCK_PROTOCOLS").get(null);int count=0;
        for(Object version:versions) {
            Object codec=call(protocol,"getBedrockCodec",version);Object helper=call(codec,"createHelper");
            Object buffer=call(transport.type("io.netty.buffer.Unpooled"),"buffer");
            try {call(codec,"tryEncode",helper,buffer,plan.packet());
                if((Integer)call(buffer,"readableBytes")<=0)throw new IllegalStateException("Empty native biome packet");count++;
            }finally{call(buffer,"release");}
        }
        if(count==0)throw new IllegalStateException("No supported biome codecs");return count;
    }
    public Change install(Object session,Plan plan) throws Exception {
        Object registry=registry(session);
        var entries=new ArrayList<Object>(entries(registry));
        var original=new HashMap<Integer,Object>();var installed=new HashMap<Integer,Object>();
        for(int i=0;i<entries.size();i++) {
            Object entry=entries.get(i);String name=(String)call(call(entry,"key"),"asString");Integer target=plan.ids().get(name);if(target==null)continue;
            int networkId=(Integer)call(entry,"id");if(networkId!=i)throw new IllegalStateException("Unexpected Java registry entry order");
            Object replacement=replacement(entry,target);
            original.put(i,entry);installed.put(i,replacement);entries.set(i,replacement);
        }
        if(original.isEmpty())throw new IllegalStateException("No native Terra biome IDs in this connection. VANILLA_COMPATIBLE cannot use this prototype");
        // Definitions must precede chunks referring to their IDs. Late acceptance requires a real-client test.
        call(session,"sendUpstreamPacket",plan.packet());reset(registry,entries);
        return new Change(registry,Map.copyOf(original),Map.copyOf(installed));
    }
    private Object registry(Object session) throws Exception {
        Object cache=call(session,modern?"javaRegistries":"getRegistryCache");
        Object key=transport.type(modern?"org.geysermc.geyser.registry.java.JavaRegistries":
            "org.geysermc.geyser.session.cache.registry.JavaRegistries").getField("BIOME").get(null);
        return call(cache,"registry",key);
    }
    private List<?> entries(Object registry) throws Exception {
        if(!modern)return (List<?>)call(registry,"entries");
        var result=new ArrayList<Object>();
        call(registry,"forEachEntry",(java.util.function.Consumer<Object>)result::add);
        return result;
    }
    private Object replacement(Object original,int target) throws Exception {
        if(!modern)return transport.type("org.geysermc.geyser.session.cache.registry.RegistryEntryData")
            .getConstructors()[0].newInstance(call(original,"id"),call(original,"key"),target);
        Object key=transport.type("org.geysermc.geyser.registry.java.JavaRegistries").getField("BIOME").get(null);
        Object temporary=transport.type("org.geysermc.geyser.registry.java.SimpleJavaRegistry").getConstructors()[0].newInstance(key);
        call(temporary,"register",call(original,"id"),call(original,"key"),target);call(temporary,"freeze");
        return ((Optional<?>)call(temporary,"getById",call(original,"id"))).orElseThrow();
    }
    /** Rebuild only this connection's mutable biome registry, retaining untouched entry objects. */
    private void reset(Object registry,List<?> replacement) throws Exception {
        if(!modern){call(registry,"reset",replacement);return;}
        var before=entries(registry);
        try{write(registry,replacement);}
        catch(Exception | LinkageError failed){
            try{write(registry,before);}catch(Exception | LinkageError rollback){failed.addSuppressed(rollback);}
            throw failed;
        }
    }
    private void write(Object registry,List<?> entries) throws Exception {
        call(registry,"clear");for(Object entry:entries)call(registry,"register",entry);call(registry,"freeze");
    }
    public boolean isCurrent(Object session,Change change) throws Exception {return registry(session)==change.registry();}
    public boolean canReapply(Object session,Change change) throws Exception {
        if(isCurrent(session,change))return canReapply(change);
        var entries=entries(registry(session));
        for(var e:change.original().entrySet()) {
            if(e.getKey()>=entries.size())return false;
            Object current=entries.get(e.getKey());
            if(!Objects.equals(call(current,"key"),call(e.getValue(),"key")) ||
               !Objects.equals(call(current,"data"),call(e.getValue(),"data")))return false;
        }
        return true;
    }
    public int ownedMappings(Change change) throws Exception {
        var entries=entries(change.registry());int count=0;
        for(var e:change.installed().entrySet())if(e.getKey()<entries.size() && entries.get(e.getKey())==e.getValue())count++;
        return count;
    }
    public boolean canReapply(Change change) throws Exception {
        var entries=entries(change.registry());
        for(var e:change.installed().entrySet()) {
            if(e.getKey()>=entries.size())return false;Object current=entries.get(e.getKey());
            if(current==e.getValue())continue;Object original=change.original().get(e.getKey());
            if(!Objects.equals(call(current,"key"),call(original,"key")) || !Objects.equals(call(current,"data"),call(original,"data")))return false;
        }
        return true;
    }
    public int restore(Change change) throws Exception {
        var entries=new ArrayList<Object>(entries(change.registry()));int restored=0;
        for(var e:change.installed().entrySet()) {
            int i=e.getKey();if(i<entries.size() && entries.get(i)==e.getValue()){entries.set(i,change.original().get(i));restored++;}
        }
        if(restored>0)reset(change.registry(),entries);return restored;
    }
}
