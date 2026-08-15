package org.incendo.cloud.permission;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class AndPermission implements Permission {
   private final Set<Permission> permissions;

   AndPermission(final @NonNull Set<Permission> permissions) {
      if (permissions.isEmpty()) {
         throw new IllegalArgumentException("AndPermission may not have an empty set of permissions");
      }

      this.permissions = Collections.unmodifiableSet(permissions);
   }

   @Override
   public @NonNull Collection<@NonNull Permission> permissions() {
      return this.permissions;
   }

   @Override
   public boolean isEmpty() {
      return false;
   }

   @Override
   public @NonNull String permissionString() {
      StringBuilder stringBuilder = new StringBuilder();
      Iterator<Permission> iterator = this.permissions.iterator();

      while (iterator.hasNext()) {
         Permission permission = iterator.next();
         stringBuilder.append('(').append(permission.permissionString()).append(')');
         if (iterator.hasNext()) {
            stringBuilder.append(" & ");
         }
      }

      return stringBuilder.toString();
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AndPermission that = (AndPermission)o;
         return this.permissions.equals(that.permissions);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.permissions());
   }

   @Override
   public @NonNull String toString() {
      return this.permissionString();
   }
}
