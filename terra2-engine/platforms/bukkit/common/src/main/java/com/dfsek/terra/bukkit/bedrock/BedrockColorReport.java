package com.dfsek.terra.bukkit.bedrock;

import com.google.gson.JsonParser;
import org.bukkit.configuration.file.YamlConfiguration;
import org.terra2.api.visual.VisualProfile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import java.io.*;

/** Frozen diagnostics for the exact profiles compiled into a resource pack, not client render proof. */
public final class BedrockColorReport {
    private final Map<String,List<String>> details;
    private final String metadata,yaml;
    public BedrockColorReport(Collection<VisualProfile> originals,Collection<VisualProfile> calibrated,BedrockColorCalibration calibration,byte[] pack) throws Exception {
        var source=new HashMap<String,VisualProfile>();originals.forEach(p->source.put(p.id(),p));
        com.google.gson.JsonObject manifest=null;
        try(var zip=new ZipInputStream(new ByteArrayInputStream(pack),StandardCharsets.UTF_8)) {
            for(ZipEntry entry;(entry=zip.getNextEntry())!=null;)if(entry.getName().equals("manifest.json")){manifest=JsonParser.parseString(new String(zip.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();break;}
        }
        if(manifest==null)throw new IOException("Missing pack manifest");var header=manifest.getAsJsonObject("header");
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pack));
        metadata="Pack version="+header.get("version")+"; UUID="+header.get("uuid").getAsString()+"; SHA-256="+hash;
        var report=new YamlConfiguration();report.set("schema",2);report.set("color-pipeline","BASE_THEN_EFFECTIVE_HSV");report.set("pack.version",header.get("version").toString());report.set("pack.uuid",header.get("uuid").getAsString());report.set("pack.sha256",hash);report.set("client-rendering-confirmed",false);
        var rows=new ArrayList<Map<String,Object>>();var debug=new TreeMap<String,List<String>>();
        for(var profile:calibrated) {
            var original=source.get(profile.id());var lines=new ArrayList<String>();var colors=new TreeMap<String,Object>();
            for(String role:List.of("foliage","grass","water")) {
                Integer before=original.colors().get(role),after=profile.colors().get(role);if(before==null&&after==null)continue;
                var adjustment=calibration.adjustment(profile.id(),role);String from=calibration.origin(profile.id(),role);
                var values=new LinkedHashMap<String,Object>();values.put("original",hex(before));values.put("base",hex(adjustment.color()==null?before:adjustment.color()));values.put("final",hex(after));values.put("origin",from);values.put("saturation",adjustment.saturation());values.put("brightness",adjustment.brightness());values.put("hue-degrees",adjustment.hueDegrees());colors.put(role,values);
                lines.add(role+": original="+hex(before)+"; base="+hex(adjustment.color()==null?before:adjustment.color())+"; final="+hex(after)+"; "+from+"; S="+adjustment.saturation()+" V="+adjustment.brightness()+" H="+adjustment.hueDegrees());
            }
            rows.add(Map.of("id",profile.id(),"client-biome",BedrockClientBiomes.name(profile.id()),"colors",colors));debug.put(profile.id(),List.copyOf(lines));
        }
        report.set("profiles",rows);details=Map.copyOf(debug);yaml=report.saveToString();
    }
    private static String hex(Integer color){return color==null?"UNDEFINED":"#%06X".formatted(color);}
    public String metadata(){return metadata;}
    public List<String> details(String id){return details.getOrDefault(id,List.of("Perfil não encontrado no catálogo compilado: "+id));}
    public void write(Path path) throws IOException {
        if(Files.isSymbolicLink(path))throw new IOException("Report path cannot be a symlink");
        Path temp=Files.createTempFile(path.getParent(),"color-report-",".tmp");
        try{Files.writeString(temp,yaml);Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}finally{Files.deleteIfExists(temp);}
    }
}
