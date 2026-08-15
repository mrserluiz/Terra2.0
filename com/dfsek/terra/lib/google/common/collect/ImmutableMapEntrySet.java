package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Spliterator;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
abstract class ImmutableMapEntrySet<K, V> extends ImmutableSet.CachingAsList<Entry<K, V>> {
   abstract ImmutableMap<K, V> map();

   @Override
   public int size() {
      return this.map().size();
   }

   @Override
   public boolean contains(@Nullable Object object) {
      if (!(object instanceof Entry)) {
         return false;
      }

      Entry<?, ?> entry = (Entry<?, ?>)object;
      V value = this.map().get(entry.getKey());
      return value != null && value.equals(entry.getValue());
   }

   @Override
   boolean isPartialView() {
      return this.map().isPartialView();
   }

   @GwtIncompatible
   @Override
   boolean isHashCodeFast() {
      return this.map().isHashCodeFast();
   }

   @Override
   public int hashCode() {
      return this.map().hashCode();
   }

   @GwtIncompatible
   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return new ImmutableMapEntrySet.EntrySetSerializedForm<>(this.map());
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use EntrySetSerializedForm");
   }

   @GwtIncompatible
   @J2ktIncompatible
   private static class EntrySetSerializedForm<K, V> implements Serializable {
      final ImmutableMap<K, V> map;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      EntrySetSerializedForm(ImmutableMap<K, V> map) {
         this.map = map;
      }

      Object readResolve() {
         return this.map.entrySet();
      }
   }

   static final class RegularEntrySet<K, V> extends ImmutableMapEntrySet<K, V> {
      private final transient ImmutableMap<K, V> map;
      private final transient ImmutableList<Entry<K, V>> entries;

      RegularEntrySet(ImmutableMap<K, V> map, Entry<K, V>[] entries) {
         this(map, ImmutableList.asImmutableList(entries));
      }

      RegularEntrySet(ImmutableMap<K, V> map, ImmutableList<Entry<K, V>> entries) {
         this.map = map;
         this.entries = entries;
      }

      @Override
      ImmutableMap<K, V> map() {
         return this.map;
      }

      @GwtIncompatible("not used in GWT")
      @Override
      int copyIntoArray(@Nullable Object[] dst, int offset) {
         return this.entries.copyIntoArray(dst, offset);
      }

      @Override
      public UnmodifiableIterator<Entry<K, V>> iterator() {
         return this.entries.iterator();
      }

      @Override
      public Spliterator<Entry<K, V>> spliterator() {
         return this.entries.spliterator();
      }

      @Override
      public void forEach(Consumer<? super Entry<K, V>> action) {
         this.entries.forEach(action);
      }

      @Override
      ImmutableList<Entry<K, V>> createAsList() {
         return new RegularImmutableAsList<>(this, this.entries);
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }
}
