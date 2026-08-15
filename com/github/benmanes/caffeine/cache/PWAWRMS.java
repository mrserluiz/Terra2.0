package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;

final class PWAWRMS<K, V> extends PWAWR<K, V> {
   int queueType;

   PWAWRMS() {
   }

   PWAWRMS(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super((K)var1, var2, (V)var3, var4, var5, var6);
   }

   PWAWRMS(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, (V)var2, var3, var4, var5);
   }

   @Override
   public int getQueueType() {
      return this.queueType;
   }

   @Override
   public void setQueueType(int var1) {
      this.queueType = var1;
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new PWAWRMS<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new PWAWRMS<>(var1, (V)var2, var3, var4, var5);
   }
}
