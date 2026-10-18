package com.dfsek.terra.addons.noise;
import com.dfsek.seismic.type.sampler.Sampler;
import com.dfsek.terra.addons.noise.config.templates.MinecraftInterpolatedTemplate.Interpolated;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MinecraftInterpolationTest {
    private Sampler source() { return new Sampler() {
        public double getSample(long seed,double x,double z) { throw new UnsupportedOperationException(); }
        public double getSample(long seed,double x,double y,double z) { return seed*0.01+x*y*0.003+z*0.1; }
    }; }
    @Test void negativeCoordinatesSeedsAndSubdividedCellsKeepTheSameField() {
        var source=source(); var interpolated=new Interpolated(source,8,4,false);
        for(long seed:new long[]{0,42,-1}) for(int x=-17;x<=17;x++) for(int z=-17;z<=17;z++) for(int y=7;y<17;y++) {
            assertEquals(source.getSample(seed,x,y,z),interpolated.getSample(seed,x,y,z),1e-12);
        }
    }
    @Test void squeezeRunsAfterInterpolationAndPreservesSolidAirBoundary() {
        var raw=new Interpolated(source(),8,4,false); var squeezed=new Interpolated(source(),8,4,true);
        for(int x=-17;x<=17;x++) for(int y=-5;y<=5;y++) {
            double v=Math.clamp(raw.getSample(42,x,y,-7),-1,1);
            assertEquals(v/2-v*v*v/24,squeezed.getSample(42,x,y,-7),1e-12);
            assertEquals(Math.signum(v),Math.signum(squeezed.getSample(42,x,y,-7)));
        }
    }
}
