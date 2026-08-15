package org.incendo.cloud.permission;

import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.key.CloudKeyHolder;

@FunctionalInterface
@API(status = Status.STABLE)
public interface PredicatePermission<C> extends Permission, CloudKeyHolder<Void> {
   static <C> PredicatePermission<C> of(final @NonNull CloudKey<Void> key, final @NonNull Predicate<C> predicate) {
      return new WrappingPredicatePermission<>(key, predicate);
   }

   static <C> PredicatePermission<C> of(final @NonNull Predicate<C> predicate) {
      return new PredicatePermission<C>() {
         @Override
         public @NonNull PermissionResult testPermission(final @NonNull C sender) {
            return PermissionResult.of(predicate.test(sender), this);
         }
      };
   }

   @Override
   default @NonNull CloudKey<Void> key() {
      return CloudKey.of(this.getClass().getSimpleName());
   }

   @Override
   default @NonNull String permissionString() {
      return this.key().name();
   }

   @API(status = Status.STABLE)
   @NonNull PermissionResult testPermission(@NonNull C sender);

   @Override
   default boolean isEmpty() {
      return false;
   }
}
