package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Spliterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
final class ImmutableEnumMap<K extends Enum<K>, V> extends ImmutableMap.IteratorBasedImmutableMap<K, V> {
   private final transient EnumMap<K, V> delegate;

   static <K extends Enum<K>, V> ImmutableMap<K, V> asImmutable(EnumMap<K, V> map) {
      switch (map.size()) {
         case 0:
            return ImmutableMap.of();
         case 1:
            Entry<K, V> entry = Iterables.getOnlyElement(map.entrySet());
            return ImmutableMap.of(entry.getKey(), entry.getValue());
         default:
            return new ImmutableEnumMap<>(map);
      }
   }

   private ImmutableEnumMap(EnumMap<K, V> delegate) {
      this.delegate = delegate;
      Preconditions.checkArgument(!delegate.isEmpty());
   }

   @Override
   UnmodifiableIterator<K> keyIterator() {
      return Iterators.unmodifiableIterator(this.delegate.keySet().iterator());
   }

   @Override
   Spliterator<K> keySpliterator() {
      return this.delegate.keySet().spliterator();
   }

   @Override
   public int size() {
      return this.delegate.size();
   }

   @Override
   public boolean containsKey(@Nullable Object key) {
      return this.delegate.containsKey(key);
   }

   @Override
   public @Nullable V get(@Nullable Object key) {
      return this.delegate.get(key);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      }

      if (object instanceof ImmutableEnumMap) {
         object = ((ImmutableEnumMap)object).delegate;
      }

      return this.delegate.equals(object);
   }

   @Override
   UnmodifiableIterator<Entry<K, V>> entryIterator() {
      return Maps.unmodifiableEntryIterator(this.delegate.entrySet().iterator());
   }

   @Override
   Spliterator<Entry<K, V>> entrySpliterator() {
      return CollectSpliterators.map(this.delegate.entrySet().spliterator(), Maps::unmodifiableEntry);
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      this.delegate.forEach(action);
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return new ImmutableEnumMap.EnumSerializedForm<>(this.delegate);
   }

   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use EnumSerializedForm");
   }

   @J2ktIncompatible
   private static class EnumSerializedForm<K extends Enum<K>, V> implements Serializable {
      final EnumMap<K, V> delegate;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      EnumSerializedForm(EnumMap<K, V> delegate) {
         this.delegate = delegate;
      }

      Object readResolve() {
         return new ImmutableEnumMap(this.delegate);
      }
   }
}
