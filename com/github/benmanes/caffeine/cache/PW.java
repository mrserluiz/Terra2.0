package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

class PW<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   protected static final VarHandle VALUE;
   volatile References.WeakValueReference<V> value;

   PW() {
   }

   PW(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      this(var1, (V)var3, var4, var5, var6);
   }

   PW(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      VALUE.set((PW)this, (References.WeakValueReference)(new References.WeakValueReference<>(var1, var2, var3)));
   }

   @Override
   public final Object getKeyReference() {
      References.WeakValueReference var1 = (References.WeakValueReference)VALUE.getAcquire((PW)this);
      return var1.getKeyReference();
   }

   @Override
   public final K getKey() {
      References.WeakValueReference var1 = (References.WeakValueReference)VALUE.getAcquire((PW)this);
      return (K)var1.getKeyReference();
   }

   @Override
   public final V getValue() {
      Reference var1 = (Reference)VALUE.getAcquire((PW)this);

      while (true) {
         Object var2 = var1.get();
         if (var2 != null) {
            return (V)var2;
         }

         VarHandle.loadLoadFence();
         Reference var3 = (Reference)VALUE.getAcquire((PW)this);
         if (var1 == var3) {
            return null;
         }

         var1 = var3;
      }
   }

   @Override
   public final void setValue(V var1, ReferenceQueue<V> var2) {
      Reference var3 = (Reference)VALUE.getAcquire((PW)this);
      VALUE.setRelease((PW)this, (References.WeakValueReference)(new References.WeakValueReference<>(this.getKeyReference(), var1, var2)));
      VarHandle.storeStoreFence();
      var3.clear();
   }

   @Override
   public final Object getValueReference() {
      return (Object)VALUE.getAcquire((PW)this);
   }

   @Override
   public final boolean containsValue(Object var1) {
      return this.getValue() == var1;
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new PW<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new PW<>(var1, (V)var2, var3, var4, var5);
   }

   @Override
   public boolean weakValues() {
      return true;
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
      References.WeakValueReference var1 = (References.WeakValueReference)VALUE.getOpaque((PW)this);
      var1.setKeyReference(RETIRED_STRONG_KEY);
      var1.clear();
   }

   @Override
   public final boolean isDead() {
      return this.getKeyReference() == DEAD_STRONG_KEY;
   }

   @Override
   public final void die() {
      References.WeakValueReference var1 = (References.WeakValueReference)VALUE.getOpaque((PW)this);
      var1.setKeyReference(DEAD_STRONG_KEY);
      var1.clear();
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         VALUE = var0.findVarHandle(PW.class, "value", References.WeakValueReference.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
