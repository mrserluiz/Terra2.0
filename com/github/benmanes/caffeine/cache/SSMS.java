package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

class SSMS<K, V> extends SS<K, V> {
   static final LocalCacheFactory FACTORY = SSMS::new;
   long maximum;
   long weightedSize;
   long windowMaximum;
   long windowWeightedSize;
   long mainProtectedMaximum;
   long mainProtectedWeightedSize;
   double stepSize;
   long adjustment;
   int hitsInSample;
   int missesInSample;
   double previousSampleHitRate;
   final FrequencySketch<K> sketch = new FrequencySketch<>();
   final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque;
   final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque;
   final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque;

   SSMS(Caffeine<K, V> var1, @Nullable AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      if (var1.hasInitialCapacity()) {
         long var4 = Math.min(var1.getMaximum(), var1.getInitialCapacity());
         this.sketch.ensureCapacity(var4);
      }

      this.accessOrderWindowDeque = !var1.evicts() && !var1.expiresAfterAccess() ? null : new AccessOrderDeque<>();
      this.accessOrderProbationDeque = new AccessOrderDeque<>();
      this.accessOrderProtectedDeque = new AccessOrderDeque<>();
   }

   @Override
   protected final boolean evicts() {
      return true;
   }

   @Override
   protected final long maximum() {
      return this.maximum;
   }

   @Override
   protected final void setMaximum(long var1) {
      this.maximum = var1;
   }

   @Override
   protected final long weightedSize() {
      return this.weightedSize;
   }

   @Override
   protected final void setWeightedSize(long var1) {
      this.weightedSize = var1;
   }

   @Override
   protected final long windowMaximum() {
      return this.windowMaximum;
   }

   @Override
   protected final void setWindowMaximum(long var1) {
      this.windowMaximum = var1;
   }

   @Override
   protected final long windowWeightedSize() {
      return this.windowWeightedSize;
   }

   @Override
   protected final void setWindowWeightedSize(long var1) {
      this.windowWeightedSize = var1;
   }

   @Override
   protected final long mainProtectedMaximum() {
      return this.mainProtectedMaximum;
   }

   @Override
   protected final void setMainProtectedMaximum(long var1) {
      this.mainProtectedMaximum = var1;
   }

   @Override
   protected final long mainProtectedWeightedSize() {
      return this.mainProtectedWeightedSize;
   }

   @Override
   protected final void setMainProtectedWeightedSize(long var1) {
      this.mainProtectedWeightedSize = var1;
   }

   @Override
   protected final double stepSize() {
      return this.stepSize;
   }

   @Override
   protected final void setStepSize(double var1) {
      this.stepSize = var1;
   }

   @Override
   protected final long adjustment() {
      return this.adjustment;
   }

   @Override
   protected final void setAdjustment(long var1) {
      this.adjustment = var1;
   }

   @Override
   protected final int hitsInSample() {
      return this.hitsInSample;
   }

   @Override
   protected final void setHitsInSample(int var1) {
      this.hitsInSample = var1;
   }

   @Override
   protected final int missesInSample() {
      return this.missesInSample;
   }

   @Override
   protected final void setMissesInSample(int var1) {
      this.missesInSample = var1;
   }

   @Override
   protected final double previousSampleHitRate() {
      return this.previousSampleHitRate;
   }

   @Override
   protected final void setPreviousSampleHitRate(double var1) {
      this.previousSampleHitRate = var1;
   }

   @Override
   protected final FrequencySketch<K> frequencySketch() {
      return this.sketch;
   }

   @Override
   protected boolean fastpath() {
      return true;
   }

   @Override
   protected final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque() {
      return this.accessOrderWindowDeque;
   }

   @Override
   protected final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque() {
      return this.accessOrderProbationDeque;
   }

   @Override
   protected final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque() {
      return this.accessOrderProtectedDeque;
   }
}
