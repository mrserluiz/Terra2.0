package org.incendo.cloud.bukkit;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;

public enum CloudBukkitCapabilities implements CloudCapability {
   BRIGADIER(
      CraftBukkitReflection.classExists("com.mojang.brigadier.tree.CommandNode") && CraftBukkitReflection.findOBCClass("command.BukkitCommandWrapper") != null
   ),
   NATIVE_BRIGADIER(CraftBukkitReflection.classExists("com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent")),
   COMMODORE_BRIGADIER(BRIGADIER.capable() && !NATIVE_BRIGADIER.capable() && !CraftBukkitReflection.classExists("org.bukkit.entity.Warden")),
   ASYNCHRONOUS_COMPLETION(CraftBukkitReflection.classExists("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent"));

   @API(status = Status.INTERNAL)
   public static final Set<CloudBukkitCapabilities> CAPABLE = Arrays.stream(values()).filter(CloudBukkitCapabilities::capable).collect(Collectors.toSet());
   private final boolean capable;

   CloudBukkitCapabilities(final boolean capable) {
      this.capable = capable;
   }

   boolean capable() {
      return this.capable;
   }

   @Override
   public @NonNull String toString() {
      return this.name();
   }
}
