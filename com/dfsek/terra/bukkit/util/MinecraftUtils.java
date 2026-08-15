package com.dfsek.terra.bukkit.util;

public final class MinecraftUtils {
   public static String stripMinecraftNamespace(String in) {
      return in.startsWith("minecraft:") ? in.substring("minecraft:".length()) : in;
   }
}
