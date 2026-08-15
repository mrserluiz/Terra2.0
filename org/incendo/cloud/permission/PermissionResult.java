package org.incendo.cloud.permission;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface PermissionResult {
   boolean allowed();

   default boolean denied() {
      return !this.allowed();
   }

   @NonNull Permission permission();

   static @NonNull PermissionResult of(final boolean result, final @NonNull Permission permission) {
      return PermissionResultImpl.of(result, permission);
   }

   static @NonNull PermissionResult allowed(final @NonNull Permission permission) {
      return PermissionResultImpl.of(true, permission);
   }

   static @NonNull PermissionResult denied(final @NonNull Permission permission) {
      return PermissionResultImpl.of(false, permission);
   }
}
