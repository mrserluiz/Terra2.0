package com.dfsek.terra.bukkit.bedrock;

import org.bukkit.configuration.file.YamlConfiguration;
import org.terra2.api.visual.VisualProfile;
import java.awt.Color;
import java.nio.file.*;
import java.util.*;

/** Bedrock-only display adjustments. Source pack profiles and Java biomes remain untouched. */
public final class BedrockColorCalibration {
    public record Adjustment(double saturation,double brightness,double hueDegrees) {
        public Adjustment {
            if(!Double.isFinite(saturation)||saturation<0||saturation>2 || !Double.isFinite(brightness)||brightness<0||brightness>2 || !Double.isFinite(hueDegrees)||Math.abs(hueDegrees)>30)
                throw new IllegalArgumentException("Calibration: saturation/brightness must be 0..2; hue-degrees -30..30");
        }
        public int apply(int rgb) {
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
                roles:
                  foliage: {saturation: 1.0, brightness: 1.0, hue-degrees: 0.0}
                  grass: {saturation: 1.0, brightness: 1.0, hue-degrees: 0.0}
                  water: {saturation: 1.0, brightness: 1.0, hue-degrees: 0.0}
                biomes: {}
                """,StandardOpenOption.CREATE_NEW);
        if(Files.isSymbolicLink(path)||Files.size(path)>65536)throw new IllegalArgumentException("Invalid calibration file");
        var yaml=new YamlConfiguration();yaml.load(path.toFile());var roles=readRoles(yaml.getConfigurationSection("roles"));
        var overrides=new HashMap<String,Map<String,Adjustment>>();var section=yaml.getConfigurationSection("biomes");
        if(section!=null)for(String id:section.getKeys(false))overrides.put(id,readRoles(section.getConfigurationSection(id)));
        return new BedrockColorCalibration(roles,overrides);
    }
    private static Map<String,Adjustment> readRoles(org.bukkit.configuration.ConfigurationSection section) {
        var result=new HashMap<String,Adjustment>();if(section==null)return result;
        for(String role:section.getKeys(false)) {
            if(!Set.of("foliage","grass","water").contains(role))throw new IllegalArgumentException("Unknown calibration role: "+role);
            var value=section.getConfigurationSection(role);if(value==null)throw new IllegalArgumentException("Invalid calibration role: "+role);
            for(String key:value.getKeys(false))if(!Set.of("saturation","brightness","hue-degrees").contains(key)||!value.isDouble(key)&&!value.isInt(key)&&!value.isLong(key))throw new IllegalArgumentException("Invalid calibration number: "+role+"."+key);
            result.put(role,new Adjustment(value.getDouble("saturation",1),value.getDouble("brightness",1),value.getDouble("hue-degrees",0)));
        }
        return result;
    }
    public VisualProfile apply(VisualProfile source) {
        var colors=new TreeMap<>(source.colors());
        colors.replaceAll((role,rgb)->biomes.getOrDefault(source.id(),Map.of()).getOrDefault(role,roles.getOrDefault(role,IDENTITY)).apply(rgb));
        return new VisualProfile(source.id(),colors);
    }
}
