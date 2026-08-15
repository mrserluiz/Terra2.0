package com.dfsek.terra.api.noise;

public interface DerivativeNoiseSampler extends NoiseSampler {
   static boolean isDifferentiable(NoiseSampler sampler) {
      return sampler instanceof DerivativeNoiseSampler dSampler && dSampler.isDifferentiable();
   }

   boolean isDifferentiable();

   double[] noised(long var1, double var3, double var5);

   double[] noised(long var1, double var3, double var5, double var7);
}
