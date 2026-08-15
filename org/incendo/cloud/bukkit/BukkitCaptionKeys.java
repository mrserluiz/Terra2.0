package org.incendo.cloud.bukkit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.Caption;

public final class BukkitCaptionKeys {
   private static final Collection<Caption> RECOGNIZED_CAPTIONS = new LinkedList<>();
   public static final Caption ARGUMENT_PARSE_FAILURE_ENCHANTMENT = of("argument.parse.failure.enchantment");
   public static final Caption ARGUMENT_PARSE_FAILURE_MATERIAL = of("argument.parse.failure.material");
   public static final Caption ARGUMENT_PARSE_FAILURE_OFFLINEPLAYER = of("argument.parse.failure.offlineplayer");
   public static final Caption ARGUMENT_PARSE_FAILURE_PLAYER = of("argument.parse.failure.player");
   public static final Caption ARGUMENT_PARSE_FAILURE_WORLD = of("argument.parse.failure.world");
   public static final Caption ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED = of("argument.parse.failure.selector.unsupported");
   public static final Caption ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT = of("argument.parse.failure.location.invalid_format");
   public static final Caption ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE = of("argument.parse.failure.location.mixed_local_absolute");
   public static final Caption ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NAMESPACE = of("argument.parse.failure.namespacedkey.namespace");
   public static final Caption ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_KEY = of("argument.parse.failure.namespacedkey.key");
   public static final Caption ARGUMENT_PARSE_FAILURE_NAMESPACED_KEY_NEED_NAMESPACE = of("argument.parse.failure.namespacedkey.need_namespace");
   public static final Caption ARGUMENT_PARSE_FAILURE_REGISTRY_ENTRY_MISSING = of("argument.parse.failure.registry_entry.missing");

   private BukkitCaptionKeys() {
   }

   private static @NonNull Caption of(final @NonNull String key) {
      Caption caption = Caption.of(key);
      RECOGNIZED_CAPTIONS.add(caption);
      return caption;
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static @NonNull Collection<@NonNull Caption> bukkitCaptionKeys() {
      return Collections.unmodifiableCollection(RECOGNIZED_CAPTIONS);
   }
}
