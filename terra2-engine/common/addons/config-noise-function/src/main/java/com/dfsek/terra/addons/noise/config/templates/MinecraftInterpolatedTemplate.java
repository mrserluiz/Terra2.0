package com.dfsek.terra.addons.noise.config.templates;

import com.dfsek.seismic.type.sampler.Sampler;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.exception.ValidationException;
import java.util.LinkedHashMap;
import java.util.Map;

/** World-aligned cell interpolation BEFORE squeeze, including negative coordinates. */
public final class MinecraftInterpolatedTemplate extends SamplerTemplate<Sampler> {
    @Value("sampler") private Sampler sampler;
    @Value("cell-width") private int width;
    @Value("cell-height") private int height;
    @Value("squeeze") @Default private boolean squeeze = false;
    @Override public boolean validate() throws ValidationException {
        super.validate();
        if(getDimensions() != 3 || width < 1 || width > 64 || height < 1 || height > 64)
            throw new ValidationException("Minecraft interpolation requires dimensions: 3 and cells in [1,64]");
        return true;
    }
    @Override public Sampler get() { return new Interpolated(sampler, width, height, squeeze); }
    public static final class Interpolated implements Sampler {
        private record Point(long seed, int x, int y, int z) {}
        private final Sampler source;
        private final int width, height;
        private final boolean squeeze;
        // Worker-local, bounded cache; no chunk/world handles retained.
        private final ThreadLocal<Map<Point, Double>> cache = ThreadLocal.withInitial(() -> new LinkedHashMap<>() {
            @Override protected boolean removeEldestEntry(Map.Entry<Point, Double> e) { return size() > 4096; }
        });
        public Interpolated(Sampler source, int width, int height, boolean squeeze) {
            if(width < 1 || height < 1) throw new IllegalArgumentException("Cell dimensions must be positive");
            this.source = source; this.width = width; this.height = height; this.squeeze = squeeze;
        }
        private double corner(long seed, int x, int y, int z) {
            return cache.get().computeIfAbsent(new Point(seed,x,y,z), p -> source.getSample(seed,x,y,z));
        }
        private static double lerp(double t, double a, double b) { return a + t * (b - a); }
        @Override public double getSample(long seed, double x, double z) { throw new UnsupportedOperationException("3D density only"); }
        @Override public double getSample(long seed, double x, double y, double z) {
            int bx = (int)Math.floor(x / width) * width, by = (int)Math.floor(y / height) * height,
                bz = (int)Math.floor(z / width) * width;
            double fx = (x-bx)/width, fy = (y-by)/height, fz = (z-bz)/width;
            double a = lerp(fy, corner(seed,bx,by,bz), corner(seed,bx,by+height,bz));
            double b = lerp(fy, corner(seed,bx+width,by,bz), corner(seed,bx+width,by+height,bz));
            double c = lerp(fy, corner(seed,bx,by,bz+width), corner(seed,bx,by+height,bz+width));
            double d = lerp(fy, corner(seed,bx+width,by,bz+width), corner(seed,bx+width,by+height,bz+width));
            double value = lerp(fz, lerp(fx,a,b), lerp(fx,c,d));
            if(!squeeze) return value;
            value = Math.max(-1,Math.min(1,value));
            return value/2 - value*value*value/24;
        }
    }
}
