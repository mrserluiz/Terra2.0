package org.incendo.cloud.permission;

import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.key.CloudKey;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
final class WrappingPredicatePermission<C> implements PredicatePermission<C> {
   private final CloudKey<Void> key;
   private final Predicate<C> predicate;

   WrappingPredicatePermission(final @NonNull CloudKey<Void> key, final @NonNull Predicate<C> predicate) {
      this.key = key;
      this.predicate = predicate;
   }

   @Override
   public @NonNull PermissionResult testPermission(final @NonNull C sender) {
      return PermissionResult.of(this.predicate.test(sender), this);
   }

   @Override
   public @NonNull CloudKey<Void> key() {
      return this.key;
   }

   @Override
   public String toString() {
      return this.key.name();
   }
}
