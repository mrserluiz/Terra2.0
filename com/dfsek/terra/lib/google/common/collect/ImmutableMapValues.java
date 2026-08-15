package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Spliterator;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class ImmutableMapValues<K, V> extends ImmutableCollection<V> {
   private final ImmutableMap<K, V> map;

   ImmutableMapValues(ImmutableMap<K, V> map) {
      this.map = map;
   }

   @Override
   public int size() {
      return this.map.size();
   }

   @Override
   public UnmodifiableIterator<V> iterator() {
      return new UnmodifiableIterator<V>() {
         final UnmodifiableIterator<Entry<K, V>> entryItr = ImmutableMapValues.this.map.entrySet().iterator();

         @Override
         public boolean hasNext() {
            return this.entryItr.hasNext();
         }

         @Override
         public V next() {
            return this.entryItr.next().getValue();
         }
      };
   }

   @Override
   public Spliterator<V> spliterator() {
      return CollectSpliterators.map(this.map.entrySet().spliterator(), Entry::getValue);
   }

   @Override
   public boolean contains(@Nullable Object object) {
      return object != null && Iterators.contains(this.iterator(), object);
   }

   @Override
   boolean isPartialView() {
      return true;
   }

   @Override
   public ImmutableList<V> asList() {
      final ImmutableList<Entry<K, V>> entryList = this.map.entrySet().asList();
      return new ImmutableAsList<V>() {
         @Override
         public V get(int index) {
            return entryList.get(index).getValue();
         }

         @Override
         ImmutableCollection<V> delegateCollection() {
            return ImmutableMapValues.this;
         }

         @J2ktIncompatible
         @GwtIncompatible
         @Override
         Object writeReplace() {
            return super.writeReplace();
         }
      };
   }

   @GwtIncompatible
   @Override
   public void forEach(Consumer<? super V> action) {
      Preconditions.checkNotNull(action);
      this.map.forEach((k, v) -> action.accept(v));
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }

   @GwtIncompatible
   @J2ktIncompatible
   private static class SerializedForm<V> implements Serializable {
      final ImmutableMap<?, V> map;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      SerializedForm(ImmutableMap<?, V> map) {
         this.map = map;
      }

      Object readResolve() {
         return this.map.values();
      }
   }
}
