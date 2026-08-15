package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
@J2ktIncompatible
public final class EnumHashBiMap<K extends Enum<K>, V> extends AbstractBiMap<K, V> {
   transient Class<K> keyTypeOrObjectUnderJ2cl;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <K extends Enum<K>, V> EnumHashBiMap<K, V> create(Class<K> keyType) {
      return new EnumHashBiMap<>(keyType);
   }

   public static <K extends Enum<K>, V> EnumHashBiMap<K, V> create(Map<K, ? extends V> map) {
      EnumHashBiMap<K, V> bimap = create(EnumBiMap.inferKeyTypeOrObjectUnderJ2cl(map));
      bimap.putAll(map);
      return bimap;
   }

   private EnumHashBiMap(Class<K> keyType) {
      super(new EnumMap<>(keyType), new HashMap<>());
      this.keyTypeOrObjectUnderJ2cl = keyType;
   }

   K checkKey(K key) {
      return Preconditions.checkNotNull(key);
   }

   @CanIgnoreReturnValue
   public @Nullable V put(K key, @ParametricNullness V value) {
      return super.put(key, value);
   }

   @CanIgnoreReturnValue
   public @Nullable V forcePut(K key, @ParametricNullness V value) {
      return super.forcePut(key, value);
   }

   @GwtIncompatible
   public Class<K> keyType() {
      return this.keyTypeOrObjectUnderJ2cl;
   }

   @GwtIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(this.keyTypeOrObjectUnderJ2cl);
      Serialization.writeMap(this, stream);
   }

   @GwtIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.keyTypeOrObjectUnderJ2cl = Objects.requireNonNull((Class<K>)stream.readObject());
      this.setDelegates(new EnumMap<>(this.keyTypeOrObjectUnderJ2cl), new HashMap<>());
      Serialization.populateMap(this, stream);
   }
}
