package com.dfsek.terra.bukkit.bedrock;
import org.junit.jupiter.api.Test;
import org.terra2.api.visual.VisualProfile;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class BedrockColorCalibrationTest {
    @Test void neutralPreservesEveryChannel() {
        var adjustment=new BedrockColorCalibration.Adjustment(1,1,0);
        for(int color:new int[]{0,0xFFFFFF,0x9B42FF,0xAD6C35})assertEquals(color,adjustment.apply(color));
    }
    @Test void biomeOverrideDoesNotChangeJavaOrOtherRoles() {
        var source=new VisualProfile("terra:test",Map.of("foliage",0xAA8866,"water",0x9B42FF));
        var calibration=new BedrockColorCalibration(Map.of("foliage",new BedrockColorCalibration.Adjustment(1.15,1,0)),Map.of("terra:test",Map.of("foliage",new BedrockColorCalibration.Adjustment(1.25,1,0))));
        var target=calibration.apply(source);
        assertEquals(0xAA8866,source.colors().get("foliage"));assertEquals(source.colors().get("water"),target.colors().get("water"));
        assertEquals(new BedrockColorCalibration.Adjustment(1.25,1,0).apply(0xAA8866),target.colors().get("foliage"));assertNotEquals(source.colors().get("foliage"),target.colors().get("foliage"));
    }
    @Test void refusesInvalidAdjustments() {
        assertThrows(IllegalArgumentException.class,()->new BedrockColorCalibration.Adjustment(Double.NaN,1,0));
        assertThrows(IllegalArgumentException.class,()->new BedrockColorCalibration.Adjustment(1,3,0));
    }
}
