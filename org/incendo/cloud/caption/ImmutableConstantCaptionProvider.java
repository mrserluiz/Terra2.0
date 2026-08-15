package org.incendo.cloud.caption;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.STABLE, consumers = "org.incendo.cloud.*")
@Generated(from = "ConstantCaptionProvider", generator = "Immutables")
@Immutable
public final class ImmutableConstantCaptionProvider<C> extends ConstantCaptionProvider<C> {
   private final @NonNull Map<Caption, String> captions;

   private ImmutableConstantCaptionProvider(Map<? extends Caption, ? extends String> captions) {
      this.captions = createUnmodifiableMap(true, false, captions);
   }

   private ImmutableConstantCaptionProvider(ImmutableConstantCaptionProvider<C> original, @NonNull Map<Caption, String> captions) {
      this.captions = captions;
   }

   @Override
   public @NonNull Map<Caption, String> captions() {
      return this.captions;
   }

   public final ImmutableConstantCaptionProvider<C> withCaptions(Map<? extends Caption, ? extends String> entries) {
      if (this.captions == entries) {
         return this;
      }

      Map<Caption, String> newValue = createUnmodifiableMap(true, false, entries);
      return new ImmutableConstantCaptionProvider<>(this, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof ImmutableConstantCaptionProvider && this.equalTo(0, (ImmutableConstantCaptionProvider<?>)another);
   }

   private boolean equalTo(int synthetic, ImmutableConstantCaptionProvider<?> another) {
      return this.captions.equals(another.captions);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      return h + (h << 5) + this.captions.hashCode();
   }

   @Override
   public String toString() {
      return "ConstantCaptionProvider{captions=" + this.captions + "}";
   }

   public static <C> ImmutableConstantCaptionProvider<C> of(Map<? extends Caption, ? extends String> captions) {
      return new ImmutableConstantCaptionProvider<>(captions);
   }

   public static <C> ImmutableConstantCaptionProvider<C> copyOf(ConstantCaptionProvider<C> instance) {
      return instance instanceof ImmutableConstantCaptionProvider ? (ImmutableConstantCaptionProvider)instance : builder().from(instance).build();
   }

   public static <C> ImmutableConstantCaptionProvider.Builder<C> builder() {
      return new ImmutableConstantCaptionProvider.Builder<>();
   }

   private static <K, V> Map<K, V> createUnmodifiableMap(boolean checkNulls, boolean skipNulls, Map<? extends K, ? extends V> map) {
      switch (map.size()) {
         case 0:
            return Collections.emptyMap();
         case 1:
            Entry<? extends K, ? extends V> e = map.entrySet().iterator().next();
            K k = (K)e.getKey();
            V v = (V)e.getValue();
            if (checkNulls) {
               Objects.requireNonNull(k, "key");
               Objects.requireNonNull(v, v == null ? "value for key: " + k : null);
            }

            if (!skipNulls || k != null && v != null) {
               return Collections.singletonMap(k, v);
            }

            return Collections.emptyMap();
         default:
            Map<K, V> linkedMap = new LinkedHashMap<>(map.size() * 4 / 3 + 1);
            if (!skipNulls && !checkNulls) {
               linkedMap.putAll(map);
            } else {
               for (Entry<? extends K, ? extends V> e : map.entrySet()) {
                  K k = (K)e.getKey();
                  V v = (V)e.getValue();
                  if (skipNulls) {
                     if (k == null || v == null) {
                        continue;
                     }
                  } else if (checkNulls) {
                     Objects.requireNonNull(k, "key");
                     Objects.requireNonNull(v, v == null ? "value for key: " + k : null);
                  }

                  linkedMap.put(k, v);
               }
            }

            return Collections.unmodifiableMap(linkedMap);
      }
   }

   @Generated(from = "ConstantCaptionProvider", generator = "Immutables")
   @NotThreadSafe
   public static final class Builder<C> {
      private Map<Caption, String> captions = null;

      private Builder() {
      }

      @CanIgnoreReturnValue
      public final ImmutableConstantCaptionProvider.Builder<C> from(ConstantCaptionProvider<C> instance) {
         Objects.requireNonNull(instance, "instance");
         this.putAllCaptions(instance.captions());
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableConstantCaptionProvider.Builder<C> putCaption(Caption key, String value) {
         if (this.captions == null) {
            this.captions = new LinkedHashMap<>();
         }

         this.captions.put(Objects.requireNonNull(key, "captions key"), Objects.requireNonNull(value, value == null ? "captions value for key: " + key : null));
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableConstantCaptionProvider.Builder<C> putCaption(Entry<? extends Caption, ? extends String> entry) {
         if (this.captions == null) {
            this.captions = new LinkedHashMap<>();
         }

         Caption k = entry.getKey();
         String v = entry.getValue();
         this.captions.put(Objects.requireNonNull(k, "captions key"), Objects.requireNonNull(v, v == null ? "captions value for key: " + k : null));
         return this;
      }

      @CanIgnoreReturnValue
      public final ImmutableConstantCaptionProvider.Builder<C> captions(Map<? extends Caption, ? extends String> entries) {
         this.captions = new LinkedHashMap<>();
         return this.putAllCaptions(entries);
      }

      @CanIgnoreReturnValue
      public final ImmutableConstantCaptionProvider.Builder<C> putAllCaptions(Map<? extends Caption, ? extends String> entries) {
         if (this.captions == null) {
            this.captions = new LinkedHashMap<>();
         }

         for (Entry<? extends Caption, ? extends String> e : entries.entrySet()) {
            Caption k = e.getKey();
            String v = e.getValue();
            this.captions.put(Objects.requireNonNull(k, "captions key"), Objects.requireNonNull(v, v == null ? "captions value for key: " + k : null));
         }

         return this;
      }

      public ImmutableConstantCaptionProvider<C> build() {
         return new ImmutableConstantCaptionProvider<>(
            null, this.captions == null ? Collections.emptyMap() : ImmutableConstantCaptionProvider.createUnmodifiableMap(false, false, this.captions)
         );
      }
   }
}
