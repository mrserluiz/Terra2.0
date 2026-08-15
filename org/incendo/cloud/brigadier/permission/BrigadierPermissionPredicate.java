package org.incendo.cloud.brigadier.permission;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.permission.Permission;

@API(status = Status.INTERNAL, since = "2.0.0")
public final class BrigadierPermissionPredicate<C, S> implements Predicate<S> {
   private final SenderMapper<S, C> senderMapper;
   private final BrigadierPermissionChecker<C> permissionChecker;
   private final CommandNode<?> node;

   public BrigadierPermissionPredicate(
      final @NonNull SenderMapper<S, C> senderMapper, final @NonNull BrigadierPermissionChecker<C> permissionChecker, final @NonNull CommandNode<?> node
   ) {
      this.senderMapper = senderMapper;
      this.permissionChecker = permissionChecker;
      this.node = node;
   }

   @Override
   public boolean test(final @NonNull S source) {
      C cloudSender = this.senderMapper.map(source);
      Map<Type, Permission> accessMap = this.node.nodeMeta().getOrDefault(CommandNode.META_KEY_ACCESS, Collections.emptyMap());

      for (Entry<Type, Permission> entry : accessMap.entrySet()) {
         if (GenericTypeReflector.isSuperType(entry.getKey(), cloudSender.getClass()) && this.permissionChecker.hasPermission(cloudSender, entry.getValue())) {
            return true;
         }
      }

      return false;
   }
}
