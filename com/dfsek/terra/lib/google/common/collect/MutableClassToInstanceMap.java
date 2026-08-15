package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Primitives;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Map.Entry;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class MutableClassToInstanceMap<B> extends ForwardingMap<Class<? extends B>, B> implements ClassToInstanceMap<B>, Serializable {
   private final Map<Class<? extends @NonNull B>, B> delegate;

   public static <B> MutableClassToInstanceMap<B> create() {
      return new MutableClassToInstanceMap<>(new HashMap<>());
   }

   public static <B> MutableClassToInstanceMap<B> create(Map<Class<? extends @NonNull B>, B> backingMap) {
      return new MutableClassToInstanceMap<>(backingMap);
   }

   private MutableClassToInstanceMap(Map<Class<? extends @NonNull B>, B> delegate) {
      this.delegate = Preconditions.checkNotNull(delegate);
   }

   @Override
   protected Map<Class<? extends @NonNull B>, B> delegate() {
      return this.delegate;
   }

   private static <B> Entry<Class<? extends @NonNull B>, B> checkedEntry(Entry<Class<? extends @NonNull B>, B> entry) {
      return new ForwardingMapEntry<Class<? extends B>, B>() {
         @Override
         protected Entry<Class<? extends @NonNull B>, B> delegate() {
            return entry;
         }

         @ParametricNullness
         @Override
         public B setValue(@ParametricNullness B value) {
            MutableClassToInstanceMap.cast(this.getKey(), value);
            return super.setValue(value);
         }
      };
   }

   @Override
   public Set<Entry<Class<? extends @NonNull B>, B>> entrySet() {
      return new ForwardingSet<Entry<Class<? extends B>, B>>() {
         @Override
         protected Set<Entry<Class<? extends @NonNull B>, B>> delegate() {
            return MutableClassToInstanceMap.this.delegate().entrySet();
         }

         @Override
         public Spliterator<Entry<Class<? extends @NonNull B>, B>> spliterator() {
            return CollectSpliterators.map(this.delegate().spliterator(), x$0 -> MutableClassToInstanceMap.checkedEntry((Entry<Class<? extends B>, B>)x$0));
         }

         @Override
         public Iterator<Entry<Class<? extends @NonNull B>, B>> iterator() {
            return new TransformedIterator<Entry<Class<? extends B>, B>, Entry<Class<? extends B>, B>>(this.delegate().iterator()) {
               Entry<Class<? extends @NonNull B>, B> transform(Entry<Class<? extends @NonNull B>, B> from) {
                  return MutableClassToInstanceMap.checkedEntry(from);
               }
            };
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

   @CanIgnoreReturnValue
   public @Nullable B put(Class<? extends @NonNull B> key, @ParametricNullness B value) {
      cast(key, value);
      return super.put(key, value);
   }

   @Override
   public void putAll(Map<? extends Class<? extends B>, ? extends B> map) {
      Map<Class<? extends B>, B> copy = new LinkedHashMap<>(map);

      for (Entry<? extends Class<? extends B>, B> entry : copy.entrySet()) {
         cast((Class<? extends B>)entry.getKey(), entry.getValue());
      }

      super.putAll(copy);
   }

   @CanIgnoreReturnValue
   @Override
   public <T extends B> @Nullable T putInstance(Class<@NonNull T> type, @ParametricNullness T value) {
      return cast(type, this.put(type, (B)value));
   }

   @Override
   public <T extends B> @Nullable T getInstance(Class<T> type) {
      return cast(type, this.get(type));
   }

   @CanIgnoreReturnValue
   private static <T> @Nullable T cast(Class<T> type, @Nullable Object value) {
      return Primitives.wrap(type).cast(value);
   }

   private Object writeReplace() {
      return new MutableClassToInstanceMap.SerializedForm<>(this.delegate());
   }

   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   private static final class SerializedForm<B> implements Serializable {
      private final Map<Class<? extends @NonNull B>, B> backingMap;
      private static final long serialVersionUID = 0L;

      SerializedForm(Map<Class<? extends @NonNull B>, B> backingMap) {
         this.backingMap = backingMap;
      }

      Object readResolve() {
         return MutableClassToInstanceMap.create(this.backingMap);
      }
   }
}
