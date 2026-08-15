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
@Generated(from = "CaptionVariable", generator = "Immutables")
@Immutable
final class CaptionVariableImpl implements CaptionVariable {
   private final @NonNull String key;
   private final @NonNull String value;

   private CaptionVariableImpl(@NonNull String key, @NonNull String value) {
      this.key = Objects.requireNonNull(key, "key");
      this.value = Objects.requireNonNull(value, "value");
   }

   private CaptionVariableImpl(CaptionVariableImpl original, @NonNull String key, @NonNull String value) {
      this.key = key;
      this.value = value;
   }

   @Override
   public @NonNull String key() {
      return this.key;
   }

   @Override
   public @NonNull String value() {
      return this.value;
   }

   public final CaptionVariableImpl withKey(String value) {
      String newValue = Objects.requireNonNull(value, "key");
      return this.key.equals(newValue) ? this : new CaptionVariableImpl(this, newValue, this.value);
   }

   public final CaptionVariableImpl withValue(String value) {
      String newValue = Objects.requireNonNull(value, "value");
      return this.value.equals(newValue) ? this : new CaptionVariableImpl(this, this.key, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof CaptionVariableImpl && this.equalTo(0, (CaptionVariableImpl)another);
   }

   private boolean equalTo(int synthetic, CaptionVariableImpl another) {
      return this.key.equals(another.key) && this.value.equals(another.value);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.key.hashCode();
      return h + (h << 5) + this.value.hashCode();
   }

   @Override
   public String toString() {
      return "CaptionVariable{key=" + this.key + ", value=" + this.value + "}";
   }

   public static CaptionVariableImpl of(@NonNull String key, @NonNull String value) {
      return new CaptionVariableImpl(key, value);
   }

   public static CaptionVariableImpl copyOf(CaptionVariable instance) {
      return instance instanceof CaptionVariableImpl ? (CaptionVariableImpl)instance : of(instance.key(), instance.value());
   }
}
