package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;

class FS<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   protected static final VarHandle KEY;
   protected static final VarHandle VALUE;
   volatile References.WeakKeyReference<K> key;
   volatile V value;

   FS() {
   }

   FS(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      this(new References.WeakKeyReference<>(var1, var2), (V)var3, var4, var5, var6);
   }

   FS(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      KEY.set((FS)this, (Object)var1);
      VALUE.set((FS)this, (Object)var2);
   }

   @Override
   public final K getKey() {
      return (K)(Reference)KEY.get((FS)this).get();
   }

   @Override
   public final Object getKeyReference() {
      return (Object)KEY.getOpaque((FS)this);
   }

   @Override
   public final V getValue() {
      return (V)(Object)VALUE.getAcquire((FS)this);
   }

   @Override
   public final void setValue(V var1, ReferenceQueue<V> var2) {
      VALUE.setRelease((FS)this, (Object)var1);
   }

   @Override
   public final Object getValueReference() {
      return (Object)VALUE.getAcquire((FS)this);
   }

   @Override
   public final boolean containsValue(Object var1) {
      return Objects.equals(var1, this.getValue());
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FS<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FS<>(var1, (V)var2, var3, var4, var5);
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
      this.key.clear();
      KEY.set((FS)this, (NodeFactory.RetiredWeakKey)RETIRED_WEAK_KEY);
   }

   @Override
   public final boolean isDead() {
      return this.getKeyReference() == DEAD_WEAK_KEY;
   }

   @Override
   public final void die() {
      this.key.clear();
      VALUE.set((FS)this, (Void)null);
      KEY.set((FS)this, (NodeFactory.DeadWeakKey)DEAD_WEAK_KEY);
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         KEY = var0.findVarHandle(FS.class, "key", References.WeakKeyReference.class);
         VALUE = var0.findVarHandle(FS.class, "value", Object.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
