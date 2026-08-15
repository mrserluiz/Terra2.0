package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.ReferenceQueue;

class PSR<K, V> extends PS<K, V> {
   protected static final VarHandle WRITE_TIME;
   volatile long writeTime;

   PSR() {
   }

   PSR(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      super((K)var1, var2, (V)var3, var4, var5, var6);
      WRITE_TIME.set((PSR)this, (long)(var6 & -2L));
   }

   PSR(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      super(var1, (V)var2, var3, var4, var5);
      WRITE_TIME.set((PSR)this, (long)(var5 & -2L));
   }

   @Override
   public final long getWriteTime() {
      return (long)WRITE_TIME.getOpaque((PSR)this);
   }

   @Override
   public final void setWriteTime(long var1) {
      WRITE_TIME.set((PSR)this, (long)var1);
   }

   @Override
   public final boolean casWriteTime(long var1, long var3) {
      return this.writeTime == var1 && WRITE_TIME.compareAndSet((PSR)this, (long)var1, (long)var3);
   }

   @Override
   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new PSR<>((K)var1, var2, (V)var3, var4, var5, var6);
   }

   @Override
   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new PSR<>(var1, (V)var2, var3, var4, var5);
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         WRITE_TIME = var0.findVarHandle(PSR.class, "writeTime", long.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
