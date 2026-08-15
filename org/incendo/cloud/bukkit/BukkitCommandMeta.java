package org.incendo.cloud.bukkit;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.incendo.cloud.key.CloudKey;

@API(status = Status.STABLE, since = "2.0.0")
public final class BukkitCommandMeta {
   public static final CloudKey<String> BUKKIT_DESCRIPTION = CloudKey.of("bukkit_description", String.class);

   private BukkitCommandMeta() {
   }
}
