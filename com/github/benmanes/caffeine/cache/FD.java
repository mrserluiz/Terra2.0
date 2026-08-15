package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

class FD<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   protected static final VarHandle VALUE;
   volatile References.SoftValueReference<V> value;

   FD() {
   }

   FD(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      this(new References.WeakKeyReference<>(var1, var2), (V)var3, var4, var5, var6);
   }

   FD(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      VALUE.set((FD)this, (References.SoftValueReference)(new References.SoftValueReference<>(var1, var2, var3)));
   }

   @Override
   public final Object getKeyReference() {
      References.SoftValueReference var1 = (References.SoftValueReference)VALUE.getAcquire((FD)this);
      return var1.getKeyReference();
   }

   @Override
   public final K getKey() {
      References.SoftValueReference var1 = (References.SoftValueReference)VALUE.getAcquire((FD)this);
      Reference var2 = (Reference)var1.getKeyReference();
      return (K)var2.get();
   }

   @Override
   public final V getValue() {
      Reference var1 = (Reference)VALUE.getAcquire((FD)this);

      while (true) {
         Object var2 = var1.get();
         if (var2 != null) {
            return (V)var2;
         }

         VarHandle.loadLoadFence();
         Reference var3 = (Reference)VALUE.getAcquire((FD)this);
         if (var1 == var3) {
            return null;
         }

         var1 = var3;
      }
   }

   @Override
   public final void setValue(V var1, ReferenceQueue<V> var2) {
      Reference var3 = (Reference)VALUE.getAcquire((FD)this);
      VALUE.setRelease((FD)this, (References.SoftValueReference)(new References.SoftValueReference<>(this.getKeyReference(), var1, var2)));
      VarHandle.storeStoreFence();
      var3.clear();
   }

   @Override
   public final Object getValueReference() {
      return (Object)VALUE.getAcquire((FD)this);
   }

   @Override
   public final boolean containsValue(Object var1) {
      return this.getValue() == var1;
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FD<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FD<>(var1, (V)var2, var3, var4, var5);
   }

   @Override
   public Object newLookupKey(Object var1) {
      return new References.LookupKeyReference<>(var1);
   }

   @Override
   public Object newReferenceKey(K var1, ReferenceQueue<K> var2) {
      return new References.WeakKeyReference<>(var1, var2);
   }

   @Override
   public boolean softValues() {
      return true;
   }

   @Override
   public final boolean isAlive() {
      Object var1 = this.getKeyReference();
      return var1 != RETIRED_WEAK_KEY && var1 != DEAD_WEAK_KEY;
   }

   @Override
   public final boolean isRetired() {
      return this.getKeyReference() == RETIRED_WEAK_KEY;
   }

   @Override
   public final void retire() {
      References.SoftValueReference var1 = (References.SoftValueReference)VALUE.getOpaque((FD)this);
      Reference var2 = (Reference)var1.getKeyReference();
      var2.clear();
      var1.setKeyReference(RETIRED_WEAK_KEY);
      var1.clear();
   }

   @Override
   public final boolean isDead() {
      return this.getKeyReference() == DEAD_WEAK_KEY;
   }

   @Override
   public final void die() {
      References.SoftValueReference var1 = (References.SoftValueReference)VALUE.getOpaque((FD)this);
      Reference var2 = (Reference)var1.getKeyReference();
      var2.clear();
      var1.setKeyReference(DEAD_WEAK_KEY);
      var1.clear();
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         VALUE = var0.findVarHandle(FD.class, "value", References.SoftValueReference.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
