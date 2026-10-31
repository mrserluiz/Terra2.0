package com.dfsek.terra.bukkit.nms;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NativeColorTest {
    @Test void rgb24RoundTripsThrough263EnvironmentVectors(){
        for(int channel=0;channel<256;channel++) {
            for(int color:new int[]{channel<<16,channel<<8,channel,channel*0x010101,0x9B42FF,0x35E8D3})
                assertEquals(color,NativeColor.rgb24(NativeColor.rgb(color)));
        }
    }
}
