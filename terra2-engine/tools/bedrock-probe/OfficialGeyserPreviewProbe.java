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
            var colorReport=new org.bukkit.configuration.file.YamlConfiguration();colorReport.load(terra.getDataFolder().toPath().resolve("bedrock/color-report.yml").toFile());
            boolean exactFound=false;
            for(var row:colorReport.getMapList("profiles"))if("terra:hydraxia/hydraxia/autumnal_woodlands".equals(row.get("id"))) {
                var color=(java.util.Map<?,?>)((java.util.Map<?,?>)row.get("colors")).get("foliage");
                require("#FF6A19".equals(color.get("final"))&&"BIOME_COLOR".equals(color.get("origin")),"Exact YAML biome color reaches compiled report despite global HSV");exactFound=true;
            }
            require(exactFound,"Exact biome color report present");
            var resource=terra.getDataFolder().toPath().resolve("bedrock/terra2-palette.mcpack");
            require(java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(resource))).equals(colorReport.getString("pack.sha256")),"Frozen report SHA matches registered pack bytes");
            try(var zip=new java.util.zip.ZipFile(resource.toFile())) {
                String file="biomes/"+com.dfsek.terra.bukkit.bedrock.BedrockClientBiomes.name("terra:hydraxia/hydraxia/autumnal_woodlands").substring(7)+".client_biome.json";
                var document=com.google.gson.JsonParser.parseString(new String(zip.getInputStream(zip.getEntry(file)).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                require("#FF6A19".equals(document.getAsJsonObject("minecraft:client_biome").getAsJsonObject("components").getAsJsonObject("minecraft:foliage_appearance").get("color").getAsString()),"Exact YAML color compiled into actual registered client biome");
            }
            getLogger().info("TERRA2_EXACT_COLOR_OK report=true pack=true restartPhase="+Files.exists(getDataFolder().toPath().resolve("identity.sha256"))+" clientVisualTest=false");
            var transport=new com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport(Bukkit.getPluginManager().getPlugin("Geyser-Spigot").getClass().getClassLoader());
            Object update=terra.bedrockPreview().diagnosticPacket(-16,64,32);
            Object position=com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(update,"getBlockPosition");
            require((int)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(position,"getX")==-16,"Packet retains requested position");
            var captured=new CaptureSession();var count=new java.util.concurrent.atomic.AtomicInteger();
            transport.submit(captured,java.util.List.of(update),()->true,count::incrementAndGet,error->{throw new AssertionError(error);});
            require(captured.packets.size()==1 && captured.packets.get(0)==update && count.get()==1,"Real protocol packet reaches isolated upstream capture");
            transport.submit(captured,java.util.List.of(update),()->false,count::incrementAndGet,error->{throw new AssertionError(error);});
            require(captured.packets.size()==1,"Stale preview batch is not emitted");
            Object blockRegistry=transport.type("org.geysermc.geyser.registry.BlockRegistries").getField("CUSTOM_BLOCK_STATE_OVERRIDES").get(null);
            require(((java.util.Map<?,?>)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(blockRegistry,"get")).isEmpty(),"No global Java block override installed");
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
            require(nativeTransport.ownedMappings(change)==1,"Native mapping ownership tracked");
            require(captured.packets.get(captured.packets.size()-1)==plan.packet(),"Native definition packet submitted before remapping chunks");
            var changed=(java.util.List<?>)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(registry,"entries");
            require(changed.get(0)==vanillaEntry && changed.get(1)!=terraEntry,"Only native Terra entry changed, vanilla entry preserved");
            require(nativeTransport.restore(change)==1,"Session mapping restored");
            require(((java.util.List<?>)com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(registry,"entries")).get(1)==terraEntry,"Original Terra entry restored exactly");
            var secondChange=nativeTransport.install(captured,plan);
            Object foreign=entryType.getConstructors()[0].newInstance(1,com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(keyType,"key",profile.id()),17);
            com.dfsek.terra.bukkit.bedrock.GeyserPositionTransport.call(registry,"reset",java.util.List.of(vanillaEntry,foreign));
            require(!nativeTransport.canReapply(secondChange),"Automatic reapply refuses another integration's changed mapping");
            require(nativeTransport.restore(secondChange)==0,"Restore preserves a later mapping owned by another integration");
            try{terra.bedrockPreview().nativePlan(world);throw new AssertionError("Projected world must refuse native-only prototype");}catch(IllegalArgumentException expected){}
            var nativeWorld=new WorldCreator("terra2_native_biome_test").seed(42).generator("Terra2:HYDRAXIA").createWorld();require(nativeWorld!=null,"Native test world created");
            var complete=terra.bedrockPreview().nativePlan(nativeWorld);require(nativeTransport.validateCodecs(complete)>0,"Complete HYDRAXIA biome packet encodes");var nativeWrapper=(com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper)nativeWorld.getGenerator();
            long expectedProfiles=nativeWrapper.getPack().getBiomeProvider().stream().map(biome->nativeWrapper.biomeCompatibility().identity(biome).registeredId()).distinct().count();
            require(expectedProfiles>0 && complete.ids().size()==expectedProfiles,"Every world biome resolves to a real Bedrock definition");nativeWorld.save();
            Path nativeIdentity=getDataFolder().toPath().resolve("native-edits.sha256");
            nativeWorld.getChunkAt(0,0);
            if(Files.exists(nativeIdentity)) {
                byte[] saved=nativeWorld.getChunkAt(0,0).getPersistentDataContainer().get(com.dfsek.terra.bukkit.compatibility.BiomeEditOverrides.KEY,org.bukkit.persistence.PersistentDataType.BYTE_ARRAY);
                require(saved!=null,"Edited identities survive server restart");
                String savedHash=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(saved));
                require(Files.readString(nativeIdentity).equals(savedHash),"Edit payload remains identical after save/restart");
                var historical=terra.biomeService().at(nativeWorld,0,64,0).orElseThrow();
                require(nativeWorld.getBiome(0,64,0).getKey().toString().equals(historical.registeredId()),"Actual edited biome survives restart alongside its identity");
                require(terra.bedrockPreview().remembers(nativeWorld),"Native-world activation survives restart");
            }
            String target=nativeWrapper.getPack().getBiomeProvider().stream().findFirst().orElseThrow().getID();
            var editor=new com.dfsek.terra.bukkit.util.SetBiomeCommand(terra);
            var blockBefore=nativeWorld.getBlockAt(0,64,0).getBlockData().getAsString();
            require(editor.apply(nativeWorld,target,0,64,0,3,67,3)==1,"A single actual Paper biome cell edited");
            var edited=terra.biomeService().at(nativeWorld,0,64,0).orElseThrow();require(edited.terraId().equals(target),"Own reader resolves saved edits in CUSTOM worlds");
            require(nativeWorld.getBiome(0,64,0).getKey().toString().equals(edited.registeredId()),"Actual Paper biome and own identity agree");
            require(PositionVisualResolver.at(nativeWorld,0,64,0).orElseThrow().source().equals("PERSISTED_EDIT"),"Bedrock visual resolver follows edited identity");
            require(nativeWorld.getBlockAt(0,64,0).getBlockData().getAsString().equals(blockBefore),"Biome edits do not regenerate blocks");
            try{editor.apply(Bukkit.getWorld("world"),target,0,64,0,3,67,3);throw new AssertionError("Primary world edit must be refused");}catch(IllegalArgumentException expected){}
            try{editor.apply(nativeWorld,target,0,64,0,1023,255,1023);throw new AssertionError("Oversized edit must be refused");}catch(IllegalArgumentException expected){}
            byte[] editedData=nativeWorld.getChunkAt(0,0).getPersistentDataContainer().get(com.dfsek.terra.bukkit.compatibility.BiomeEditOverrides.KEY,org.bukkit.persistence.PersistentDataType.BYTE_ARRAY);
            Files.writeString(nativeIdentity,java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(editedData)));
            terra.bedrockPreview().rememberWorld(nativeWorld);require(terra.bedrockPreview().remembers(nativeWorld),"Rendering world explicitly remembered");
            require(!terra.bedrockPreview().remembers(Bukkit.getWorld("world")),"Rendering main world remains unauthorized");
            String projectedTarget=((com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper)world.getGenerator()).getPack().getBiomeProvider().stream().findFirst().orElseThrow().getID();
            require(editor.apply(world,projectedTarget,4,64,4,7,67,7)==1,"Projected worlds support identity-preserving biome editing");
            var projectedEdit=terra.biomeService().at(world,4,64,4).orElseThrow();
            require(world.getBiome(4,64,4).getKey().toString().equals(projectedEdit.vanillaReference()),"Projected world writes valid vanilla reference");
            require(PositionVisualResolver.at(world,4,64,4).orElseThrow().source().equals("PERSISTED_EDIT"),"Projected edit resolves original Terra identity");
            require(java.util.Arrays.equals(identity,world.getChunkAt(0,0).getPersistentDataContainer().get(com.dfsek.terra.bukkit.compatibility.PersistedBiomeService.KEY,org.bukkit.persistence.PersistentDataType.BYTE_ARRAY)),"Edits do not rewrite generation identity palette");
            nativeWorld.save();nativeWorld.refreshChunk(0,0);
            require(java.util.Arrays.equals(editedData,nativeWorld.getChunkAt(0,0).getPersistentDataContainer().get(com.dfsek.terra.bukkit.compatibility.BiomeEditOverrides.KEY,org.bukkit.persistence.PersistentDataType.BYTE_ARRAY)),"Refresh retains saved identity");
            getLogger().info("TERRA2_EDIT_PERSISTENCE_OK primaryProtected=true nativeActivationStored=true realWorldEditSelectionTest=false realBedrockReconnectTest=false");
            getLogger().info("TERRA2_NATIVE_BIOME_TRANSPORT_OK waterRGB=9B42FF liquid=VANILLA sessionCapture=true clientVisualTest=false");
            world.save();
            getLogger().info("TERRA2_OFFICIAL_GEYSER_PREVIEW_OK definitions="+definitions+" profile="+sample.profile());
        } catch(Throwable error){getLogger().log(java.util.logging.Level.SEVERE,"TERRA2_OFFICIAL_GEYSER_PREVIEW_FAILED",error);}
        finally{Bukkit.shutdown();}
    }
}
