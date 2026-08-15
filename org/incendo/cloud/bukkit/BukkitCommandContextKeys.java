package org.incendo.cloud.bukkit;

import io.leangen.geantyref.TypeToken;
import java.util.concurrent.Executor;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.key.CloudKey;

public final class BukkitCommandContextKeys {
   public static final CloudKey<CommandSender> BUKKIT_COMMAND_SENDER = CloudKey.of("BukkitCommandSender", TypeToken.get(CommandSender.class));
   @API(status = Status.STABLE, since = "2.0.0")
   public static final CloudKey<Executor> SENDER_SCHEDULER_EXECUTOR = CloudKey.of("SenderSchedulerExecutor", Executor.class);

   private BukkitCommandContextKeys() {
   }
}
