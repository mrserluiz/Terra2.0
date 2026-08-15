package org.incendo.cloud.bukkit.internal;

import java.lang.reflect.Method;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class BukkitBackwardsBrigadierSenderMapper<C, S> implements Function<C, S> {
   private static final Class<?> VANILLA_COMMAND_WRAPPER_CLASS = CraftBukkitReflection.needOBCClass("command.VanillaCommandWrapper");
   private static final Method GET_LISTENER_METHOD = CraftBukkitReflection.needMethod(VANILLA_COMMAND_WRAPPER_CLASS, "getListener", CommandSender.class);
   private final SenderMapper<?, C> senderMapper;

   public BukkitBackwardsBrigadierSenderMapper(final @NonNull SenderMapper<?, C> senderMapper) {
      this.senderMapper = senderMapper;
   }

   @Override
   public S apply(final @NonNull C cloud) {
      try {
         return (S)GET_LISTENER_METHOD.invoke(null, this.senderMapper.reverse(cloud));
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }
}
