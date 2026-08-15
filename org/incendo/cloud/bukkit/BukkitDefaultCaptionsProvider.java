package org.incendo.cloud.bukkit;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.caption.DelegatingCaptionProvider;

public final class BukkitDefaultCaptionsProvider<C> extends DelegatingCaptionProvider<C> {
   public static final String ARGUMENT_PARSE_FAILURE_ENCHANTMENT = "'<input>' is not a valid enchantment";
   public static final String ARGUMENT_PARSE_FAILURE_MATERIAL = "'<input>' is not a valid material name";
   public static final String ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER = "No player found for input '<input>'";
   public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "No player found for input '<input>'";
   public static final String ARGUMENT_PARSE_FAILURE_WORLD = "'<input>' is not a valid Minecraft world";
   public static final String ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED = "Entity selector argument type not supported below Minecraft 1.13.";
   public static final String ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT = "'<input>' is not a valid location. Required format is '<x> <y> <z>'";
   public static final String ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE = "Cannot mix local and absolute coordinates. (either all coordinates use '^' or none do)";
   public static final String ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE = "Invalid namespace '<input>'. Must be [a-z0-9._-]";
   public static final String ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY = "Invalid key '<input>'. Must be [a-z0-9/._-]";
   public static final String ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE = "Invalid input '<input>', requires an explicit namespace.";
   public static final String ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_MISSING = "No such entry '<input>' in '<registry>' registry.";
   private static final CaptionProvider<?> PROVIDER = CaptionProvider.constantProvider()
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_ENCHANTMENT, "'<input>' is not a valid enchantment")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_MATERIAL, "'<input>' is not a valid material name")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER, "No player found for input '<input>'")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER, "No player found for input '<input>'")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_WORLD, "'<input>' is not a valid Minecraft world")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED, "Entity selector argument type not supported below Minecraft 1.13.")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT, "'<input>' is not a valid location. Required format is '<x> <y> <z>'")
      .putCaption(
         BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE,
         "Cannot mix local and absolute coordinates. (either all coordinates use '^' or none do)"
      )
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE, "Invalid namespace '<input>'. Must be [a-z0-9._-]")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY, "Invalid key '<input>'. Must be [a-z0-9/._-]")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE, "Invalid input '<input>', requires an explicit namespace.")
      .putCaption(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_MISSING, "No such entry '<input>' in '<registry>' registry.")
      .build();

   @Override
   public @NonNull CaptionProvider<C> delegate() {
      return (CaptionProvider<C>)PROVIDER;
   }
}
