package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@GwtCompatible(serializable = true, emulated = true)
public final class HashMultimap<K, V> extends HashMultimapGwtSerializationDependencies<K, V> {
   private static final int DEFAULT_VALUES_PER_KEY = 2;
   @VisibleForTesting
   transient int expectedValuesPerKey = 2;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <K, V> HashMultimap<K, V> create() {
      return new HashMultimap<>();
   }

   public static <K, V> HashMultimap<K, V> create(int expectedKeys, int expectedValuesPerKey) {
      return new HashMultimap<>(expectedKeys, expectedValuesPerKey);
   }

   public static <K, V> HashMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
      return new HashMultimap<>(multimap);
   }

   private HashMultimap() {
      this(12, 2);
   }

   private HashMultimap(int expectedKeys, int expectedValuesPerKey) {
      super(Platform.newHashMapWithExpectedSize(expectedKeys));
      Preconditions.checkArgument(expectedValuesPerKey >= 0);
      this.expectedValuesPerKey = expectedValuesPerKey;
   }

   private HashMultimap(Multimap<? extends K, ? extends V> multimap) {
      super(Platform.newHashMapWithExpectedSize(multimap.keySet().size()));
      this.putAll(multimap);
   }

   @Override
   Set<V> createCollection() {
      return Platform.newHashSetWithExpectedSize(this.expectedValuesPerKey);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      Serialization.writeMultimap(this, stream);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.expectedValuesPerKey = 2;
      int distinctKeys = Serialization.readCount(stream);
      Map<K, Collection<V>> map = Platform.newHashMapWithExpectedSize(12);
      this.setMap(map);
      Serialization.populateMultimap(this, stream, distinctKeys);
   }
}
