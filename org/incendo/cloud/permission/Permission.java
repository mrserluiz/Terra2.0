package org.incendo.cloud.permission;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;

@API(status = Status.STABLE)
@Immutable
public interface Permission {
   Permission EMPTY = permission("");

   static @NonNull Permission permission(final @NonNull String permission) {
      return PermissionImpl.of(permission);
   }

   static @NonNull Permission of(final @NonNull String permission) {
      return permission(permission);
   }

   static @NonNull Permission empty() {
      return EMPTY;
   }

   static @NonNull Permission allOf(final @NonNull Collection<@NonNull Permission> permissions) {
      Set<Permission> objects = new HashSet<>();

      for (Permission permission : permissions) {
         if (permission instanceof AndPermission) {
            objects.addAll(permission.permissions());
         } else {
            objects.add(permission);
         }
      }

      return new AndPermission(objects);
   }

   static @NonNull Permission allOf(final @NonNull Permission @NonNull ... permissions) {
      return allOf(Arrays.asList(permissions));
   }

   static @NonNull Permission anyOf(final @NonNull Collection<@NonNull Permission> permissions) {
      Set<Permission> objects = new HashSet<>();

      for (Permission permission : permissions) {
         if (permission instanceof OrPermission) {
            objects.addAll(permission.permissions());
         } else {
            objects.add(permission);
         }
      }

      return new OrPermission(objects);
   }

   static @NonNull Permission anyOf(final @NonNull Permission @NonNull ... permissions) {
      return anyOf(Arrays.asList(permissions));
   }

   default @NonNull Collection<@NonNull Permission> permissions() {
      return Collections.singleton(this);
   }

   @NonNull String permissionString();

   @API(status = Status.STABLE)
   default boolean isEmpty() {
      return this.permissionString().isEmpty();
   }

   @API(status = Status.STABLE)
   default @NonNull Permission or(final @NonNull Permission other) {
      Objects.requireNonNull(other, "other");
      Set<Permission> permission = new HashSet<>(2);
      permission.add(this);
      permission.add(other);
      return anyOf(permission);
   }

   @API(status = Status.STABLE)
   default @NonNull Permission or(final @NonNull Permission @NonNull ... other) {
      Objects.requireNonNull(other, "other");
      Set<Permission> permission = new HashSet<>(other.length + 1);
      permission.add(this);
      permission.addAll(Arrays.asList(other));
      return anyOf(permission);
   }

   @API(status = Status.STABLE)
   default @NonNull Permission and(final @NonNull Permission other) {
      Objects.requireNonNull(other, "other");
      Set<Permission> permission = new HashSet<>(2);
      permission.add(this);
      permission.add(other);
      return allOf(permission);
   }

   @API(status = Status.STABLE)
   default @NonNull Permission and(final @NonNull Permission @NonNull ... other) {
      Objects.requireNonNull(other, "other");
      Set<Permission> permission = new HashSet<>(other.length + 1);
      permission.add(this);
      permission.addAll(Arrays.asList(other));
      return allOf(permission);
   }
}
