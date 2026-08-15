package org.incendo.cloud.paper;

import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.suggestion.SuggestionListener;
import org.incendo.cloud.paper.suggestion.SuggestionListenerFactory;
import org.incendo.cloud.state.RegistrationState;

public class LegacyPaperCommandManager<C> extends BukkitCommandManager<C> {
   private @Nullable BrigadierManagerHolder<C, ?> brigadierManagerHolder = null;

   @API(status = Status.STABLE, since = "2.0.0")
   public LegacyPaperCommandManager(
      final @NonNull Plugin owningPlugin,
      final @NonNull ExecutionCoordinator<C> commandExecutionCoordinator,
      final @NonNull SenderMapper<CommandSender, C> senderMapper
   ) throws BukkitCommandManager.InitializationException {
      super(owningPlugin, commandExecutionCoordinator, senderMapper);
      this.registerCommandPreProcessor(new PaperCommandPreprocessor<>(this, this.senderMapper(), Function.identity()));
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static @NonNull LegacyPaperCommandManager<@NonNull CommandSender> createNative(
      final @NonNull Plugin owningPlugin, final @NonNull ExecutionCoordinator<CommandSender> commandExecutionCoordinator
   ) throws BukkitCommandManager.InitializationException {
      return new LegacyPaperCommandManager<>(owningPlugin, commandExecutionCoordinator, SenderMapper.identity());
   }

   @Override
   public synchronized void registerBrigadier() throws BukkitCommandManager.BrigadierInitializationException {
      this.registerBrigadier(true);
   }

   @Deprecated
   public synchronized void registerLegacyPaperBrigadier() throws BukkitCommandManager.BrigadierInitializationException {
      this.registerBrigadier(false);
   }

   private void registerBrigadier(final boolean allowModern) {
      this.requireState(RegistrationState.BEFORE_REGISTRATION);
      this.checkBrigadierCompatibility();
      if (this.brigadierManagerHolder != null) {
         throw new IllegalStateException("Brigadier is already registered! Holder: " + this.brigadierManagerHolder);
      }

      if (!this.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
         super.registerBrigadier();
      } else if (allowModern && CraftBukkitReflection.classExists("io.papermc.paper.command.brigadier.CommandSourceStack")) {
         try {
            ModernPaperBrigadier<C, CommandSender> brig = new ModernPaperBrigadier<>(
               CommandSender.class, this, this.senderMapper(), () -> this.lockRegistration()
            );
            this.brigadierManagerHolder = brig;
            brig.registerPlugin(this.owningPlugin());
            this.commandRegistrationHandler(brig);
         } catch (Exception e) {
            throw new BukkitCommandManager.BrigadierInitializationException("Failed to register ModernPaperBrigadier", e);
         }
      } else {
         try {
            this.brigadierManagerHolder = new LegacyPaperBrigadier<>(this);
            Bukkit.getPluginManager().registerEvents((Listener)this.brigadierManagerHolder, this.owningPlugin());
            this.brigadierManagerHolder.brigadierManager().settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
         } catch (Exception e) {
            throw new BukkitCommandManager.BrigadierInitializationException("Failed to register LegacyPaperBrigadier", e);
         }
      }
   }

   @API(status = Status.STABLE, since = "2.0.0")
   @Override
   public boolean hasBrigadierManager() {
      return this.brigadierManagerHolder != null || super.hasBrigadierManager();
   }

   @API(status = Status.STABLE, since = "2.0.0")
   @Override
   public @NonNull CloudBrigadierManager<C, ?> brigadierManager() {
      return this.brigadierManagerHolder != null ? this.brigadierManagerHolder.brigadierManager() : super.brigadierManager();
   }

   public void registerAsynchronousCompletions() throws IllegalStateException {
      this.requireState(RegistrationState.BEFORE_REGISTRATION);
      if (!this.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
         throw new IllegalStateException("Failed to register asynchronous command completion listener.");
      }

      SuggestionListenerFactory<C> suggestionListenerFactory = SuggestionListenerFactory.create(this);
      SuggestionListener<C> suggestionListener = suggestionListenerFactory.createListener();
      Bukkit.getServer().getPluginManager().registerEvents(suggestionListener, this.owningPlugin());
   }
}
