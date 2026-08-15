package org.incendo.cloud.permission;

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
@Generated(from = "PermissionResult", generator = "Immutables")
@Immutable
final class PermissionResultImpl implements PermissionResult {
   private final boolean allowed;
   private final @NonNull Permission permission;

   private PermissionResultImpl(boolean allowed, @NonNull Permission permission) {
      this.allowed = allowed;
      this.permission = Objects.requireNonNull(permission, "permission");
   }

   private PermissionResultImpl(PermissionResultImpl original, boolean allowed, @NonNull Permission permission) {
      this.allowed = allowed;
      this.permission = permission;
   }

   @Override
   public boolean allowed() {
      return this.allowed;
   }

   @Override
   public @NonNull Permission permission() {
      return this.permission;
   }

   public final PermissionResultImpl withAllowed(boolean value) {
      return this.allowed == value ? this : new PermissionResultImpl(this, value, this.permission);
   }

   public final PermissionResultImpl withPermission(Permission value) {
      if (this.permission == value) {
         return this;
      }

      Permission newValue = Objects.requireNonNull(value, "permission");
      return new PermissionResultImpl(this, this.allowed, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof PermissionResultImpl && this.equalTo(0, (PermissionResultImpl)another);
   }

   private boolean equalTo(int synthetic, PermissionResultImpl another) {
      return this.allowed == another.allowed && this.permission.equals(another.permission);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + Boolean.hashCode(this.allowed);
      return h + (h << 5) + this.permission.hashCode();
   }

   @Override
   public String toString() {
      return "PermissionResult{allowed=" + this.allowed + ", permission=" + this.permission + "}";
   }

   public static PermissionResultImpl of(boolean allowed, @NonNull Permission permission) {
      return new PermissionResultImpl(allowed, permission);
   }

   public static PermissionResultImpl copyOf(PermissionResult instance) {
      return instance instanceof PermissionResultImpl ? (PermissionResultImpl)instance : of(instance.allowed(), instance.permission());
   }
}
