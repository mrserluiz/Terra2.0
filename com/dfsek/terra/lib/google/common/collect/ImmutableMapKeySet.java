package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class ImmutableMapKeySet<K, V> extends IndexedImmutableSet<K> {
   private final ImmutableMap<K, V> map;

   ImmutableMapKeySet(ImmutableMap<K, V> map) {
      this.map = map;
   }

   @Override
   public int size() {
      return this.map.size();
   }

   @Override
   public UnmodifiableIterator<K> iterator() {
      return this.map.keyIterator();
   }

   @Override
   public Spliterator<K> spliterator() {
      return this.map.keySpliterator();
   }

   @Override
   public boolean contains(@Nullable Object object) {
      return this.map.containsKey(object);
   }

   @Override
   K get(int index) {
      return this.map.entrySet().asList().get(index).getKey();
   }

   @Override
   public void forEach(Consumer<? super K> action) {
      Preconditions.checkNotNull(action);
      this.map.forEach((k, v) -> action.accept(k));
   }

   @Override
   boolean isPartialView() {
      return true;
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }

   @GwtIncompatible
   @J2ktIncompatible
   private static class KeySetSerializedForm<K> implements Serializable {
      final ImmutableMap<K, ?> map;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      KeySetSerializedForm(ImmutableMap<K, ?> map) {
         this.map = map;
      }

      Object readResolve() {
         return this.map.keySet();
      }
   }
}
