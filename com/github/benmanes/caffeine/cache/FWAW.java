package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.ReferenceQueue;

class FWAW<K, V> extends FWA<K, V> {
   protected static final VarHandle WRITE_TIME;
   volatile long writeTime;
   Node<K, V> previousInWriteOrder;
   Node<K, V> nextInWriteOrder;

   FWAW() {
   }

   FWAW(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super((K)var1, var2, (V)var3, var4, var5, var6);
      WRITE_TIME.set((FWAW)this, (long)(var6 & -2L));
   }

   FWAW(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, (V)var2, var3, var4, var5);
      WRITE_TIME.set((FWAW)this, (long)(var5 & -2L));
   }

   @Override
   public Node<K, V> getPreviousInVariableOrder() {
      return this.previousInWriteOrder;
   }

   @Override
   public void setPreviousInVariableOrder(Node<K, V> var1) {
      this.previousInWriteOrder = var1;
   }

   @Override
   public Node<K, V> getNextInVariableOrder() {
      return this.nextInWriteOrder;
   }

   @Override
   public void setNextInVariableOrder(Node<K, V> var1) {
      this.nextInWriteOrder = var1;
   }

   @Override
   public long getVariableTime() {
      return (long)WRITE_TIME.getOpaque((FWAW)this);
   }

   @Override
   public void setVariableTime(long var1) {
      WRITE_TIME.setOpaque((FWAW)this, (long)var1);
   }

   @Override
   public boolean casVariableTime(long var1, long var3) {
      return this.writeTime == var1 && WRITE_TIME.compareAndSet((FWAW)this, (long)var1, (long)var3);
   }

   @Override
   public final long getWriteTime() {
      return (long)WRITE_TIME.getOpaque((FWAW)this);
   }

   @Override
   public final void setWriteTime(long var1) {
      WRITE_TIME.set((FWAW)this, (long)var1);
   }

   @Override
   public final Node<K, V> getPreviousInWriteOrder() {
      return this.previousInWriteOrder;
   }

   @Override
   public final void setPreviousInWriteOrder(Node<K, V> var1) {
      this.previousInWriteOrder = var1;
   }

   @Override
   public final Node<K, V> getNextInWriteOrder() {
      return this.nextInWriteOrder;
   }

   @Override
   public final void setNextInWriteOrder(Node<K, V> var1) {
      this.nextInWriteOrder = var1;
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FWAW<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FWAW<>(var1, (V)var2, var3, var4, var5);
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         WRITE_TIME = var0.findVarHandle(FWAW.class, "writeTime", long.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
