package com.dfsek.terra.bukkit.bedrock;

import org.bukkit.configuration.file.YamlConfiguration;
import org.terra2.api.visual.VisualProfile;
import java.awt.Color;
import java.nio.file.*;
import java.util.*;

/** Bedrock-only display adjustments. Source pack profiles and Java biomes remain untouched. */
public final class BedrockColorCalibration {
    public record Adjustment(double saturation,double brightness,double hueDegrees,Integer color) {
        public Adjustment(double saturation,double brightness,double hueDegrees){this(saturation,brightness,hueDegrees,null);}
        public Adjustment {
            if(color!=null && (color<0 || color>0xFFFFFF))throw new IllegalArgumentException("Invalid RGB color");
            if(!Double.isFinite(saturation)||saturation<0||saturation>2 || !Double.isFinite(brightness)||brightness<0||brightness>2 || !Double.isFinite(hueDegrees)||Math.abs(hueDegrees)>30)
                throw new IllegalArgumentException("Calibration: saturation/brightness must be 0..2; hue-degrees -30..30");
        }
        public int apply(int rgb) {
            if(color!=null)rgb=color;
            if(saturation==1 && brightness==1 && hueDegrees==0)return rgb;
            float[] hsv=Color.RGBtoHSB((rgb>>16)&255,(rgb>>8)&255,rgb&255,null);
            float hue=(float)((hsv[0]+hueDegrees/360.0+1)%1);
            return Color.HSBtoRGB(hue,(float)Math.min(1,hsv[1]*saturation),(float)Math.min(1,hsv[2]*brightness))&0xFFFFFF;
        }
    }
    private static final Adjustment IDENTITY=new Adjustment(1,1,0);
    private final Map<String,Adjustment> roles;
    private final Map<String,Map<String,Adjustment>> biomes;
    public BedrockColorCalibration(Map<String,Adjustment> roles,Map<String,Map<String,Adjustment>> biomes) {
        this.roles=Map.copyOf(roles);
        var copy=new HashMap<String,Map<String,Adjustment>>();biomes.forEach((id,r)->copy.put(id,Map.copyOf(r)));this.biomes=Map.copyOf(copy);
    }
    public static BedrockColorCalibration load(Path path) throws Exception {
        Files.createDirectories(path.getParent());
        if(!Files.exists(path))Files.writeString(path,"""
                # Bedrock display only. Java and original pack colors remain unchanged.
                # Start neutral; calibrate against Java screenshots at the same position/time.
                # Supported roles: foliage, grass, water. Per-biome overrides use exact registered IDs.
                # Optional color: "#FF6A19" sets the base RGB; effective HSV adjustments are applied afterwards.
                roles:
                  foliage: {saturation: 1.0, brightness: 1.0, hue-degrees: 0.0}
                  grass: {saturation: 1.0, brightness: 1.0, hue-degrees: 0.0}
                  water: {saturation: 1.0, brightness: 1.0, hue-degrees: 0.0}
                biomes: {}
                """,StandardOpenOption.CREATE_NEW);
        if(Files.isSymbolicLink(path)||Files.size(path)>65536)throw new IllegalArgumentException("Invalid calibration file");
        var yaml=new YamlConfiguration();yaml.options().pathSeparator('|');yaml.load(path.toFile());
        for(String key:yaml.getKeys(false))if(!Set.of("roles","biomes").contains(key))throw new IllegalArgumentException("Unknown calibration root key (check indentation): "+key);
        for(String key:Set.of("roles","biomes"))if(yaml.contains(key)&&!yaml.isConfigurationSection(key))throw new IllegalArgumentException("Expected calibration section: "+key);
        var roles=readRoles(yaml.getConfigurationSection("roles"),Map.of());
        var overrides=new HashMap<String,Map<String,Adjustment>>();var section=yaml.getConfigurationSection("biomes");
        if(section!=null)for(String id:section.getKeys(false)) {
            if(section.getConfigurationSection(id)==null)throw new IllegalArgumentException("Expected biome section: "+id);
            overrides.put(id,readRoles(section.getConfigurationSection(id),roles));
        }
        return new BedrockColorCalibration(roles,overrides);
    }
    public void validate(Collection<VisualProfile> catalog) {
        var ids=new HashSet<String>();catalog.forEach(p->ids.add(p.id()));
        for(String id:biomes.keySet())if(!ids.contains(id))throw new IllegalArgumentException("Unknown calibration biome ID: "+id+"; use the exact registered ID from /terra2 biome");
    }
    public Adjustment adjustment(String id,String role) {return biomes.getOrDefault(id,Map.of()).getOrDefault(role,roles.getOrDefault(role,IDENTITY));}
    public String origin(String id,String role) {
        String scope=biomes.getOrDefault(id,Map.of()).containsKey(role)?"BIOME":roles.containsKey(role)?"GLOBAL":"ORIGINAL";
        return scope+(adjustment(id,role).color()!=null?"_BASE_COLOR_HSV":"_HSV");
    }
    private static Map<String,Adjustment> readRoles(org.bukkit.configuration.ConfigurationSection section,Map<String,Adjustment> defaults) {
        var result=new HashMap<String,Adjustment>();if(section==null)return result;
        for(String role:section.getKeys(false)) {
            if(!Set.of("foliage","grass","water").contains(role))throw new IllegalArgumentException("Unknown calibration role: "+role);
            var value=section.getConfigurationSection(role);if(value==null)throw new IllegalArgumentException("Invalid calibration role: "+role);
            for(String key:value.getKeys(false)) {
                if(key.equals("color"))continue;
                if(!Set.of("saturation","brightness","hue-degrees").contains(key)||!(value.get(key) instanceof Number))throw new IllegalArgumentException("Invalid calibration number: "+role+"."+key);
            }
            var fallback=defaults.getOrDefault(role,IDENTITY);
            Integer color=fallback.color();
            if(value.getKeys(false).contains("color")) {
                Object raw=value.get("color");
                if(!(raw instanceof String hex)||!hex.matches("#[0-9a-fA-F]{6}"))throw new IllegalArgumentException("Invalid color for "+role+": use quoted '#RRGGBB'");
                color=Integer.parseInt(((String)raw).substring(1),16);
            }
            result.put(role,new Adjustment(value.getDouble("saturation",fallback.saturation()),value.getDouble("brightness",fallback.brightness()),value.getDouble("hue-degrees",fallback.hueDegrees()),color));
        }
        return result;
    }
    public VisualProfile apply(VisualProfile source) {
        var colors=new TreeMap<>(source.colors());
        colors.replaceAll((role,rgb)->adjustment(source.id(),role).apply(rgb));
        for(String role:Set.of("foliage","grass","water")) {
            var effective=adjustment(source.id(),role);
            if(effective.color()!=null && !colors.containsKey(role))colors.put(role,effective.apply(0));
        }
        return new VisualProfile(source.id(),colors);
    }
}
