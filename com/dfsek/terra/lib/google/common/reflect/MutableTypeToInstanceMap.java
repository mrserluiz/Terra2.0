package com.dfsek.terra.lib.google.common.reflect;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ForwardingMap;
import com.dfsek.terra.lib.google.common.collect.ForwardingMapEntry;
import com.dfsek.terra.lib.google.common.collect.ForwardingSet;
import com.dfsek.terra.lib.google.common.collect.Iterators;
import com.dfsek.terra.lib.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MutableTypeToInstanceMap<B> extends ForwardingMap<TypeToken<? extends B>, B> implements TypeToInstanceMap<B> {
   private final Map<TypeToken<? extends @NonNull B>, B> backingMap = Maps.newHashMap();

   @Override
   public <T extends B> @Nullable T getInstance(Class<T> type) {
      return this.trustedGet(TypeToken.of(type));
   }

   @Override
   public <T extends B> @Nullable T getInstance(TypeToken<T> type) {
      return this.trustedGet(type.rejectTypeVariables());
   }

   @CanIgnoreReturnValue
   @Override
   public <T extends B> @Nullable T putInstance(Class<@NonNull T> type, @ParametricNullness T value) {
      return this.trustedPut(TypeToken.of(type), value);
   }

   @CanIgnoreReturnValue
   @Override
   public <T extends B> @Nullable T putInstance(TypeToken<@NonNull T> type, @ParametricNullness T value) {
      return this.trustedPut(type.rejectTypeVariables(), value);
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   public @Nullable B put(TypeToken<? extends @NonNull B> key, @ParametricNullness B value) {
      throw new UnsupportedOperationException("Please use putInstance() instead.");
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public void putAll(Map<? extends TypeToken<? extends @NonNull B>, ? extends B> map) {
      throw new UnsupportedOperationException("Please use putInstance() instead.");
   }

   @Override
   public Set<Entry<TypeToken<? extends @NonNull B>, B>> entrySet() {
      return MutableTypeToInstanceMap.UnmodifiableEntry.transformEntries(super.entrySet());
   }

   @Override
   protected Map<TypeToken<? extends @NonNull B>, B> delegate() {
      return this.backingMap;
   }

   private <T extends B> @Nullable T trustedPut(TypeToken<@NonNull T> type, @ParametricNullness T value) {
      return (T)this.backingMap.put(type, (B)value);
   }

   private <T extends B> @Nullable T trustedGet(TypeToken<T> type) {
      return (T)this.backingMap.get(type);
   }

   private static final class UnmodifiableEntry<K, V> extends ForwardingMapEntry<K, V> {
      private final Entry<K, V> delegate;

      static <K, V> Set<Entry<K, V>> transformEntries(Set<Entry<K, V>> entries) {
         return new ForwardingSet<Entry<K, V>>() {
            @Override
            protected Set<Entry<K, V>> delegate() {
               return entries;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
               return MutableTypeToInstanceMap.UnmodifiableEntry.transformEntries(super.iterator());
            }

            @Override
            public Object[] toArray() {
               return this.standardToArray();
            }

            @Override
            public <T> T[] toArray(T[] array) {
               return (T[])this.standardToArray(array);
            }
         };
      }

      private static <K, V> Iterator<Entry<K, V>> transformEntries(Iterator<Entry<K, V>> entries) {
         return Iterators.transform(entries, MutableTypeToInstanceMap.UnmodifiableEntry::new);
      }

      private UnmodifiableEntry(Entry<K, V> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
      }

      @Override
      protected Entry<K, V> delegate() {
         return this.delegate;
      }

      @ParametricNullness
      @Override
      public V setValue(@ParametricNullness V value) {
         throw new UnsupportedOperationException();
      }
   }
}
