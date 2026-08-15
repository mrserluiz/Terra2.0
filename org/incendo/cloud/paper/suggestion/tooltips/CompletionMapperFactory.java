package org.incendo.cloud.paper.suggestion.tooltips;

import net.kyori.adventure.audience.Audience;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.INTERNAL, since = "2.0.0")
public interface CompletionMapperFactory {
   static @NonNull CompletionMapperFactory detectingRelocation() {
      return new CompletionMapperFactory.CompletionMapperFactoryImpl();
   }

   @NonNull CompletionMapper createMapper();

   final class CompletionMapperFactoryImpl implements CompletionMapperFactory {
      private CompletionMapperFactoryImpl() {
      }

      @Override
      public @NonNull CompletionMapper createMapper() {
         return Audience.class.isAssignableFrom(Player.class) ? new NativeCompletionMapper() : new ReflectiveCompletionMapper();
      }
   }
}
