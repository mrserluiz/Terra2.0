package org.incendo.cloud.caption;

import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "Caption", generator = "Immutables")
@Immutable
final class CaptionImpl implements Caption {
   private final @NonNull String key;

   private CaptionImpl(@NonNull String key) {
      this.key = Objects.requireNonNull(key, "key");
   }

   private CaptionImpl(CaptionImpl original, @NonNull String key) {
      this.key = key;
   }

   @Override
   public @NonNull String key() {
      return this.key;
   }

   public final CaptionImpl withKey(String value) {
      String newValue = Objects.requireNonNull(value, "key");
      return this.key.equals(newValue) ? this : new CaptionImpl(this, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof CaptionImpl && this.equalTo(0, (CaptionImpl)another);
   }

   private boolean equalTo(int synthetic, CaptionImpl another) {
      return this.key.equals(another.key);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      return h + (h << 5) + this.key.hashCode();
   }

   @Override
   public String toString() {
      return "Caption{key=" + this.key + "}";
   }

   public static CaptionImpl of(@NonNull String key) {
      return new CaptionImpl(key);
   }

   public static CaptionImpl copyOf(Caption instance) {
      return instance instanceof CaptionImpl ? (CaptionImpl)instance : of(instance.key());
   }
}
