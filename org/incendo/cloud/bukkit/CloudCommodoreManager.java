package org.incendo.cloud.bukkit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;

class CloudCommodoreManager<C> extends BukkitPluginRegistrationHandler<C> {
   private final BukkitCommandManager<C> commandManager;
   private final CloudBrigadierManager<C, Object> brigadierManager;
   private final Commodore commodore;

   CloudCommodoreManager(final @NonNull BukkitCommandManager<C> commandManager) {
      if (!CommodoreProvider.isSupported()) {
         throw new IllegalStateException("CommodoreProvider reports isSupported = false");
      }

      this.commandManager = commandManager;
      this.commodore = CommodoreProvider.getCommodore(commandManager.owningPlugin());
      this.brigadierManager = new CloudBrigadierManager<>(commandManager, SenderMapper.create(sender -> {
         CommandSender bukkitSender = getBukkitSender(sender);
         return this.commandManager.senderMapper().map(bukkitSender);
      }, new BukkitBackwardsBrigadierSenderMapper<>(this.commandManager.senderMapper())));
      BukkitBrigadierMapper<C> mapper = new BukkitBrigadierMapper<>(this.commandManager.owningPlugin().getLogger(), this.brigadierManager);
      mapper.registerBuiltInMappings();
   }

   @Override
   protected void registerExternal(final @NonNull String label, final @NonNull Command<?> command, final @NonNull BukkitCommand<C> bukkitCommand) {
      this.registerWithCommodore(label, (Command<C>)command);
   }

   @Override
   protected void unregisterExternal(final @NonNull String label) {
      this.unregisterWithCommodore(label);
   }

   protected @NonNull CloudBrigadierManager<C, Object> brigadierManager() {
      return this.brigadierManager;
   }

   private void registerWithCommodore(final @NonNull String label, final @NonNull Command<C> command) {
      LiteralCommandNode<?> literalCommandNode = this.brigadierManager
         .literalBrigadierNodeFactory()
         .createNode(
            label,
            command,
            o -> 1,
            (sender, commandPermission) -> this.commandManager.commandTree().getNamedNode(label) == null
               ? false
               : this.commandManager.testPermission(sender, commandPermission).allowed()
         );
      CommandNode existingNode = this.getDispatcher().findNode(Collections.singletonList(label));
      if (existingNode != null) {
         this.mergeChildren(existingNode, literalCommandNode);
      } else {
         this.commodore.register(literalCommandNode);
      }
   }

   private void unregisterWithCommodore(final @NonNull String label) {
      CommandDispatcher<?> dispatcher = this.getDispatcher();
      CommandNode node = dispatcher.findNode(Collections.singletonList(label));
      if (node != null) {
         try {
            Class<?> commodoreImpl = this.commodore.getClass();

            Method removeChild;
            try {
               removeChild = commodoreImpl.getDeclaredMethod("removeChild", RootCommandNode.class, String.class);
            } catch (NoSuchMethodException ex) {
               removeChild = commodoreImpl.getSuperclass().getDeclaredMethod("removeChild", RootCommandNode.class, String.class);
            }

            removeChild.setAccessible(true);
            removeChild.invoke(null, dispatcher.getRoot(), node.getName());
            Field registeredNodesField = commodoreImpl.getDeclaredField("registeredNodes");
            registeredNodesField.setAccessible(true);
            List<?> registeredNodes = (List<?>)registeredNodesField.get(this.commodore);
            registeredNodes.remove(node);
         } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to unregister command '%s' with commodore", label), e);
         }
      }
   }

   private void mergeChildren(final CommandNode<?> existingNode, final CommandNode<?> node) {
      for (CommandNode child : node.getChildren()) {
         CommandNode<?> existingChild = existingNode.getChild(child.getName());
         if (existingChild == null) {
            existingNode.addChild(child);
         } else {
            this.mergeChildren(existingChild, child);
         }
      }
   }

   private CommandDispatcher<?> getDispatcher() {
      try {
         Method getDispatcherMethod = this.commodore.getClass().getDeclaredMethod("getDispatcher");
         getDispatcherMethod.setAccessible(true);
         return (CommandDispatcher<?>)getDispatcherMethod.invoke(this.commodore);
      } catch (ReflectiveOperationException ex) {
         throw new RuntimeException(ex);
      }
   }

   private static CommandSender getBukkitSender(final @NonNull Object commandSourceStack) {
      Objects.requireNonNull(commandSourceStack, "commandSourceStack");

      try {
         Method getBukkitSenderMethod = commandSourceStack.getClass().getDeclaredMethod("getBukkitSender");
         getBukkitSenderMethod.setAccessible(true);
         return (CommandSender)getBukkitSenderMethod.invoke(commandSourceStack);
      } catch (ReflectiveOperationException ex) {
         throw new RuntimeException(ex);
      }
   }
}
