package com.dfsek.terra.addons.noise.config.templates;

import com.dfsek.seismic.type.sampler.Sampler;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.exception.ValidationException;
import com.dfsek.terra.api.Platform;

/** Explicit parameters follow Minecraft's old_blended_noise codec. */
public final class OldBlendedNoiseTemplate extends SamplerTemplate<Sampler> {
    private final Platform platform;
    @Value("xz-scale") private double xzScale;
    @Value("y-scale") private double yScale;
    @Value("xz-factor") private double xzFactor;
    @Value("y-factor") private double yFactor;
    @Value("smear-scale-multiplier") private double smear;
    public OldBlendedNoiseTemplate(Platform platform) { this.platform = platform; }
    @Override public boolean validate() throws ValidationException {
        super.validate();
        if(getDimensions() != 3) throw new ValidationException("old_blended_noise requires dimensions: 3");
        for(double v : new double[]{xzScale, yScale, xzFactor, yFactor, smear})
            if(!Double.isFinite(v) || v < 0.001 || v > 1000)
                throw new ValidationException("old_blended_noise parameters must be finite in [0.001, 1000]");
        return true;
    }
    @Override public Sampler get() { return platform.oldBlendedNoise(xzScale, yScale, xzFactor, yFactor, smear); }
}
