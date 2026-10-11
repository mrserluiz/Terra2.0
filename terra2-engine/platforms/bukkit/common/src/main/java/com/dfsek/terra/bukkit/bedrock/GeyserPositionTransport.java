package com.dfsek.terra.bukkit.bedrock;

import java.lang.reflect.*;
import java.util.*;

/** Optional adapter to official Geyser internals. No registry writes or global Java overrides. */
public final class GeyserPositionTransport {
    private final ClassLoader loader;
    private final Class<?> packetType, vectorType, flagType;
    public GeyserPositionTransport(ClassLoader loader) throws ReflectiveOperationException {
        this.loader=loader;
        packetType=type("org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket");
        vectorType=type("org.cloudburstmc.math.vector.Vector3i");
        flagType=type("org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket$Flag");
        var session=type("org.geysermc.geyser.session.GeyserSession");
        session.getMethod("getBlockMappings"); session.getMethod("executeInEventLoop",Runnable.class);
        session.getMethod("sendUpstreamPacket",type("org.cloudburstmc.protocol.bedrock.packet.BedrockPacket"));
        session.getMethod("isClosed"); session.getMethod("isSpawned");
    }
    public Class<?> type(String name) throws ClassNotFoundException { return Class.forName(name,false,loader); }
    public Object api() throws ReflectiveOperationException { return call(type("org.geysermc.geyser.api.GeyserApi"),"api"); }
    public Object connection(UUID player) throws ReflectiveOperationException { return call(api(),"connectionByUuid",player); }
    public Object packet(Object mappings,Object customState,int x,int y,int z) throws ReflectiveOperationException {
        var definitions=(Map<?,?>)call(mappings,"getCustomBlockStateDefinitions");
        Object definition=definitions.get(customState);
        if(definition==null) throw new IllegalStateException("Custom color is not registered for this Bedrock protocol");
        Object packet=packetType.getConstructor().newInstance();
        call(packet,"setBlockPosition",call(vectorType,"from",x,y,z));
        call(packet,"setDefinition",definition); call(packet,"setDataLayer",0);
        var flags=(Set<Object>)call(packet,"getFlags");
        flags.add(Enum.valueOf((Class)flagType,"NETWORK")); flags.add(Enum.valueOf((Class)flagType,"NEIGHBORS"));
        return packet;
    }
    /** Checks actual protocol registries and packet construction, without claiming a client connection. */
    public int validate(Collection<Object> states) throws ReflectiveOperationException {
        Object registry=type("org.geysermc.geyser.registry.BlockRegistries").getField("BLOCKS").get(null);
        var mappings=(Map<?,?>)call(registry,"get"); int count=0;
        for(Object mapping : mappings.values()) for(Object state : states) {
            Object update=packet(mapping,state,0,64,0), definition=call(update,"getDefinition");
            Object registered=call(mapping,"getDefinition",call(definition,"getRuntimeId"));
            if(registered!=definition) throw new IllegalStateException("Packet refers to an unregistered runtime ID");
            count++;
        }
        if(count==0) throw new IllegalStateException("No active custom block protocol definitions");
        return count;
    }
    public Object diagnosticPacket(Object state,int x,int y,int z) throws ReflectiveOperationException {
        Object registry=type("org.geysermc.geyser.registry.BlockRegistries").getField("BLOCKS").get(null);
        var mappings=(Map<?,?>)call(registry,"get");
        if(mappings.isEmpty())throw new IllegalStateException("No Bedrock protocol definitions");
        return packet(mappings.values().iterator().next(),state,x,y,z);
    }
    public void submit(Object session,List<Object> packets,java.util.function.BooleanSupplier current,
                       Runnable submitted,java.util.function.Consumer<Throwable> failed) throws ReflectiveOperationException {
        var snapshot=List.copyOf(packets);
        call(session,"executeInEventLoop",(Runnable)()->{
            if(!current.getAsBoolean())return;
            try {
                if((boolean)call(session,"isClosed") || !(boolean)call(session,"isSpawned"))return;
                for(Object packet:snapshot) {
                    if(!current.getAsBoolean())return;
                    call(session,"sendUpstreamPacket",packet);submitted.run();
                }
            } catch(Exception | LinkageError error){failed.accept(error);}
        });
    }
    public static Object call(Object target,String name,Object... args) throws ReflectiveOperationException {
        Class<?> type=target instanceof Class<?> c ? c : target.getClass();
        for(Method method:type.getMethods()) {
            if(!method.getName().equals(name) || method.getParameterCount()!=args.length)continue;
            Class<?>[] parameters=method.getParameterTypes(); boolean compatible=true;
            for(int i=0;i<args.length;i++) {
                if(args[i]==null){if(parameters[i].isPrimitive())compatible=false;continue;}
                Class<?> p=parameters[i];
                if(p==int.class)p=Integer.class;else if(p==boolean.class)p=Boolean.class;else if(p==float.class)p=Float.class;
                if(!p.isInstance(args[i]))compatible=false;
            }
            if(!compatible)continue;
            Object receiver=target instanceof Class<?> ? null : target;
            if(!method.canAccess(receiver) && !method.trySetAccessible())continue;
            return method.invoke(receiver,args);
        }
        throw new NoSuchMethodException(type.getName()+"."+name);
    }
}
