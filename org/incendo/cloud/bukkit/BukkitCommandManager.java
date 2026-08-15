package org.incendo.cloud.bukkit;

import java.util.logging.Level;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.state.RegistrationState;

public abstract class BukkitCommandManager<C>
   extends CommandManager<C>
   implements BrigadierManagerHolder<C, Object>,
   SenderMapperHolder<CommandSender, C>,
   PluginHolder {
   private final Plugin owningPlugin;
   private final SenderMapper<CommandSender, C> senderMapper;
   private boolean splitAliases = false;

   @API(status = Status.INTERNAL, since = "2.0.0")
   protected BukkitCommandManager(
      final @NonNull Plugin owningPlugin,
      final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
      final @NonNull SenderMapper<CommandSender, C> senderMapper
   ) throws BukkitCommandManager.InitializationException {
      super(commandExecutionCoordinator, new BukkitPluginRegistrationHandler<>());

      try {
         ((BukkitPluginRegistrationHandler)this.commandRegistrationHandler()).initialize(this);
      } catch (ReflectiveOperationException exception) {
         throw new BukkitCommandManager.InitializationException("Failed to initialize command registration handler", exception);
      }

      this.owningPlugin = owningPlugin;
      this.senderMapper = senderMapper;
      CloudBukkitCapabilities.CAPABLE.forEach(x$0 -> this.registerCapability(x$0));
      this.registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
      this.registerCommandPreProcessor(new BukkitCommandPreprocessor<>(this));
      BukkitParsers.register(this);
      this.owningPlugin.getServer().getPluginManager().registerEvents(new CloudBukkitListener<>(this), this.owningPlugin);
      this.registerDefaultExceptionHandlers();
      this.captionRegistry().registerProvider(new BukkitDefaultCaptionsProvider<>());
   }

   @Override
   public final @NonNull Plugin owningPlugin() {
      return this.owningPlugin;
   }

   @Override
   public final @NonNull SenderMapper<CommandSender, C> senderMapper() {
      return this.senderMapper;
   }

   @Override
   public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
      return permission.isEmpty() ? true : this.senderMapper.reverse(sender).hasPermission(permission);
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   protected final boolean splitAliases() {
      return this.splitAliases;
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   protected final void splitAliases(final boolean value) {
      this.requireState(RegistrationState.BEFORE_REGISTRATION);
      this.splitAliases = value;
   }

   protected final void checkBrigadierCompatibility() throws BukkitCommandManager.BrigadierInitializationException {
      if (!this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
         throw new BukkitCommandManager.BrigadierInitializationException(
            "Missing capability "
               + CloudBukkitCapabilities.class.getSimpleName()
               + "."
               + CloudBukkitCapabilities.BRIGADIER
               + " (Minecraft version too old? Brigadier was added in 1.13). See the Javadocs for more details"
         );
      }
   }

   public synchronized void registerBrigadier() throws BukkitCommandManager.BrigadierInitializationException {
      this.requireState(RegistrationState.BEFORE_REGISTRATION);
      this.checkBrigadierCompatibility();
      if (!this.hasCapability(CloudBukkitCapabilities.COMMODORE_BRIGADIER)) {
         throw new BukkitCommandManager.BrigadierInitializationException(
            "Missing capability "
               + CloudBukkitCapabilities.class.getSimpleName()
               + "."
               + CloudBukkitCapabilities.COMMODORE_BRIGADIER
               + " (Minecraft version too new). See the Javadocs for more details"
         );
      }

      CommandRegistrationHandler<C> handler = this.commandRegistrationHandler();
      if (handler instanceof CloudCommodoreManager) {
         throw new IllegalStateException("Brigadier is already registered! Holder: " + handler);
      }

      try {
         CloudCommodoreManager<C> cloudCommodoreManager = new CloudCommodoreManager<>(this);
         cloudCommodoreManager.initialize(this);
         this.commandRegistrationHandler(cloudCommodoreManager);
         this.splitAliases(true);
      } catch (Exception e) {
         throw new BukkitCommandManager.BrigadierInitializationException("Unexpected exception initializing " + CloudCommodoreManager.class.getSimpleName(), e);
      }
   }

   @API(status = Status.STABLE, since = "2.0.0")
   @Override
   public boolean hasBrigadierManager() {
      return this.commandRegistrationHandler() instanceof CloudCommodoreManager;
   }

   @API(status = Status.STABLE, since = "2.0.0")
   @Override
   public @NonNull CloudBrigadierManager<C, ?> brigadierManager() {
      if (this.commandRegistrationHandler() instanceof CloudCommodoreManager) {
         return ((CloudCommodoreManager)this.commandRegistrationHandler()).brigadierManager();
      } else {
         throw new BrigadierManagerHolder.BrigadierManagerNotPresent(
            "The CloudBrigadierManager is either not supported in the current environment, or it is not enabled."
         );
      }
   }

   private void registerDefaultExceptionHandlers() {
      this.registerDefaultExceptionHandlers(
         triplet -> this.senderMapper()
            .reverse(triplet.first().sender())
            .sendMessage(ChatColor.RED + triplet.first().formatCaption(triplet.second(), triplet.third())),
         pair -> this.owningPlugin().getLogger().log(Level.SEVERE, pair.first(), pair.second())
      );
   }

   final void lockIfBrigadierCapable() {
      if (this.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
         this.lockRegistration();
      }
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static final class BrigadierInitializationException extends IllegalStateException {
      @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
      public BrigadierInitializationException(final @NonNull String reason) {
         super(reason);
      }

      @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
      public BrigadierInitializationException(final @NonNull String reason, final @Nullable Throwable cause) {
         super(reason, cause);
      }
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static final class InitializationException extends IllegalStateException {
      @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
      public InitializationException(final String message, final @Nullable Throwable cause) {
         super(message, cause);
      }
   }
}
