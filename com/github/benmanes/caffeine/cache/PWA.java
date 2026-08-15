package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.ReferenceQueue;

class PWA<K, V> extends PW<K, V> {
   protected static final VarHandle ACCESS_TIME;
   volatile long accessTime;
   Node<K, V> previousInAccessOrder;
   Node<K, V> nextInAccessOrder;

   PWA() {
   }

   PWA(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super((K)var1, var2, (V)var3, var4, var5, var6);
      ACCESS_TIME.set((PWA)this, (long)var6);
   }

   PWA(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, (V)var2, var3, var4, var5);
      ACCESS_TIME.set((PWA)this, (long)var5);
   }

   @Override
   public Node<K, V> getPreviousInVariableOrder() {
      return this.previousInAccessOrder;
   }

   @Override
   public void setPreviousInVariableOrder(Node<K, V> var1) {
      this.previousInAccessOrder = var1;
   }

   @Override
   public Node<K, V> getNextInVariableOrder() {
      return this.nextInAccessOrder;
   }

   @Override
   public void setNextInVariableOrder(Node<K, V> var1) {
      this.nextInAccessOrder = var1;
   }

   @Override
   public long getVariableTime() {
      return (long)ACCESS_TIME.getOpaque((PWA)this);
   }

   @Override
   public void setVariableTime(long var1) {
      ACCESS_TIME.setOpaque((PWA)this, (long)var1);
   }

   @Override
   public boolean casVariableTime(long var1, long var3) {
      return this.accessTime == var1 && ACCESS_TIME.compareAndSet((PWA)this, (long)var1, (long)var3);
   }

   @Override
   public final long getAccessTime() {
      return (long)ACCESS_TIME.getOpaque((PWA)this);
   }

   @Override
   public final void setAccessTime(long var1) {
      ACCESS_TIME.setOpaque((PWA)this, (long)var1);
   }

   @Override
   public final Node<K, V> getPreviousInAccessOrder() {
      return this.previousInAccessOrder;
   }

   @Override
   public final void setPreviousInAccessOrder(Node<K, V> var1) {
      this.previousInAccessOrder = var1;
   }

   @Override
   public final Node<K, V> getNextInAccessOrder() {
      return this.nextInAccessOrder;
   }

   @Override
   public final void setNextInAccessOrder(Node<K, V> var1) {
      this.nextInAccessOrder = var1;
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new PWA<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new PWA<>(var1, (V)var2, var3, var4, var5);
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         ACCESS_TIME = var0.findVarHandle(PWA.class, "accessTime", long.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
