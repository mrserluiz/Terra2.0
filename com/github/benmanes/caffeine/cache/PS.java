package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;

class PS<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   protected static final VarHandle KEY;
   protected static final VarHandle VALUE;
   volatile K key;
   volatile V value;

   PS() {
   }

   PS(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      this(var1, (V)var3, var4, var5, var6);
   }

   PS(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      KEY.set((PS)this, (Object)var1);
      VALUE.set((PS)this, (Object)var2);
   }

   @Override
   public final K getKey() {
      return (K)(Object)KEY.getOpaque((PS)this);
   }

   @Override
   public final Object getKeyReference() {
      return (Object)KEY.getOpaque((PS)this);
   }

   @Override
   public final V getValue() {
      return (V)(Object)VALUE.getAcquire((PS)this);
   }

   @Override
   public final void setValue(V var1, ReferenceQueue<V> var2) {
      VALUE.setRelease((PS)this, (Object)var1);
   }

   @Override
   public final Object getValueReference() {
      return (Object)VALUE.getAcquire((PS)this);
   }

   @Override
   public final boolean containsValue(Object var1) {
      return Objects.equals(var1, this.getValue());
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new PS<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new PS<>(var1, (V)var2, var3, var4, var5);
   }

   @Override
   public final boolean isAlive() {
      Object var1 = this.getKeyReference();
      return var1 != RETIRED_STRONG_KEY && var1 != DEAD_STRONG_KEY;
   }

   @Override
   public final boolean isRetired() {
      return this.getKeyReference() == RETIRED_STRONG_KEY;
   }

   @Override
   public final void retire() {
      KEY.set((PS)this, (NodeFactory.RetiredStrongKey)RETIRED_STRONG_KEY);
   }

   @Override
   public final boolean isDead() {
      return this.getKeyReference() == DEAD_STRONG_KEY;
   }

   @Override
   public final void die() {
      VALUE.set((PS)this, (Void)null);
      KEY.set((PS)this, (NodeFactory.DeadStrongKey)DEAD_STRONG_KEY);
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         KEY = var0.findVarHandle(PS.class, "key", Object.class);
         VALUE = var0.findVarHandle(PS.class, "value", Object.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
