package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class FWAWR<K, V> extends FWAW<K, V> {
   FWAWR() {
   }

   FWAWR(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super((K)var1, var2, (V)var3, var4, var5, var6);
   }

   FWAWR(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, (V)var2, var3, var4, var5);
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
      return (long)ACCESS_TIME.getOpaque((FWAWR)this);
   }

   @Override
   public void setVariableTime(long var1) {
      ACCESS_TIME.setOpaque((FWAWR)this, (long)var1);
   }

   @Override
   public boolean casVariableTime(long var1, long var3) {
      return this.accessTime == var1 && ACCESS_TIME.compareAndSet((FWAWR)this, (long)var1, (long)var3);
   }

   @Override
   public final boolean casWriteTime(long var1, long var3) {
      return this.writeTime == var1 && WRITE_TIME.compareAndSet((FWAWR)this, (long)var1, (long)var3);
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FWAWR<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FWAWR<>(var1, (V)var2, var3, var4, var5);
   }
}
