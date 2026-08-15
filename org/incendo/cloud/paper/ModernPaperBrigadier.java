package org.incendo.cloud.paper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandRegistrationFlag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionChecker;
import org.incendo.cloud.bukkit.PluginHolder;
import org.incendo.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.bukkit.internal.BukkitHelper;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.internal.CommandRegistrationHandler;

final class ModernPaperBrigadier<C, B> implements CommandRegistrationHandler<C>, BrigadierManagerHolder<C, CommandSourceStack> {
   private final CommandManager<C> manager;
   private final Runnable lockRegistration;
   private final PluginMetaHolder metaHolder;
   private final CloudBrigadierManager<C, CommandSourceStack> brigadierManager;
   private final Map<String, Set<String>> aliases = new ConcurrentHashMap<>();
   private final Set<Command<C>> registeredCommands = new HashSet<>();
   private volatile @Nullable Commands commands;
   private static @MonotonicNonNull Method commandnodeRemoveMethod = null;
   private static @MonotonicNonNull Field commandsInvalidField = null;

   ModernPaperBrigadier(final Class<B> baseType, final CommandManager<C> manager, final SenderMapper<B, C> senderMapper, final Runnable lockRegistration) {
      this.manager = manager;
      this.lockRegistration = lockRegistration;
      if (manager instanceof PluginMetaHolder) {
         this.metaHolder = (PluginMetaHolder)manager;
      } else {
         if (!(manager instanceof PluginHolder)) {
            throw new IllegalArgumentException(manager.toString());
         }

         this.metaHolder = PluginMetaHolder.fromPluginHolder((PluginHolder)manager);
      }

      this.brigadierManager = new CloudBrigadierManager<>(
         this.manager,
         SenderMapper.create(
            source -> baseType.equals(CommandSender.class) ? senderMapper.map((B)source.getSender()) : senderMapper.map((B)source),
            sender -> baseType.equals(CommandSender.class)
               ? new BukkitBackwardsBrigadierSenderMapper<C, CommandSourceStack>(senderMapper).apply(sender)
               : (CommandSourceStack)senderMapper.reverse(sender)
         )
      );
      BukkitBrigadierMapper<C> mapper = new BukkitBrigadierMapper<>(Logger.getLogger(this.metaHolder.owningPluginMeta().getName()), this.brigadierManager);
      mapper.registerBuiltInMappings();
      PaperBrigadierMappings.register(mapper);
   }

   void registerPlugin(final Plugin plugin) {
      plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::register);
   }

   void registerBootstrap(final BootstrapContext context) {
      context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, this::register);
   }

   private void register(final ReloadableRegistrarEvent<Commands> event) {
      this.lockRegistration.run();
      Commands commands = (Commands)event.registrar();
      this.commands = commands;
      this.aliases.clear();

      for (CommandNode<C> rootNode : this.manager.commandTree().rootNodes()) {
         this.registerCommand(commands, rootNode);
      }
   }

   private void registerCommand(final Commands commands, final CommandNode<C> rootNode) {
      Set<String> registered = commands.registerWithFlags(
         this.metaHolder.owningPluginMeta(),
         this.createRootNode(rootNode, rootNode.component().name()),
         this.findBukkitDescription(rootNode),
         new ArrayList<>(rootNode.component().alternativeAliases()),
         new HashSet<>(Collections.singletonList(CommandRegistrationFlag.FLATTEN_ALIASES))
      );
      this.aliases.put(rootNode.component().name(), registered);
   }

   private LiteralCommandNode<CommandSourceStack> createRootNode(final CommandNode<C> rootNode, final String label) {
      BrigadierPermissionChecker<C> permissionChecker = (sender, permission) -> this.manager.commandTree().getNamedNode(rootNode.component().name()) == null
         ? false
         : this.manager.testPermission(sender, permission).allowed();
      return this.brigadierManager
         .literalBrigadierNodeFactory()
         .createNode(
            label,
            rootNode,
            new CloudBrigadierCommand<>(
               this.manager, this.brigadierManager, command -> BukkitHelper.stripNamespace(this.metaHolder.owningPluginMeta().getName(), command)
            ),
            permissionChecker
         );
   }

   private String findBukkitDescription(final CommandNode<C> node) {
      if (node.command() != null) {
         return BukkitHelper.description(node.command());
      }

      for (CommandNode<C> child : node.children()) {
         String result = this.findBukkitDescription(child);
         if (result != null) {
            return result;
         }
      }

      return null;
   }

   @Override
   public boolean hasBrigadierManager() {
      return true;
   }

   @Override
   public @NonNull CloudBrigadierManager<C, CommandSourceStack> brigadierManager() {
      return this.brigadierManager;
   }

   @Override
   public boolean registerCommand(final Command<C> command) {
      if (!this.registeredCommands.add(command)) {
         return true;
      }

      Commands commands = this.commands;
      if (commands == null) {
         return true;
      }

      if (this.aliases.containsKey(command.rootComponent().name())) {
         CommandDispatcher<CommandSourceStack> dispatcher = unsafeGet(commands, Commands::getDispatcher);
         Set<String> registered = this.aliases.get(command.rootComponent().name());
         LiteralCommandNode<CommandSourceStack> newRoot = this.createRootNode(
            this.manager.commandTree().getNamedNode(command.rootComponent().name()), command.rootComponent().name()
         );

         for (String label : registered) {
            com.mojang.brigadier.tree.CommandNode<CommandSourceStack> node = dispatcher.getRoot().getChild(label);

            for (com.mojang.brigadier.tree.CommandNode<CommandSourceStack> newChild : newRoot.getChildren()) {
               node.addChild(newChild);
            }
         }
      } else {
         unsafeOperation(commands, cmds -> this.registerCommand(cmds, this.manager.commandTree().getNamedNode(command.rootComponent().name())));
      }

      this.resendCommands();
      Set<String> registered = this.aliases.get(command.rootComponent().name());
      boolean ret = registered != null && !registered.isEmpty();
      if (!ret) {
         this.registeredCommands.remove(command);
      }

      return ret;
   }

   private void unregisterRoot(final Commands commands, final String label) {
      Set<String> removed = this.aliases.remove(label);
      if (removed != null && !removed.isEmpty()) {
         this.registeredCommands.removeIf(command -> command.rootComponent().name().equals(label));

         try {
            if (commandnodeRemoveMethod == null) {
               commandnodeRemoveMethod = com.mojang.brigadier.tree.CommandNode.class.getMethod("removeCommand", String.class);
               commandnodeRemoveMethod.setAccessible(true);
            }
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to find removeCommand method", e);
         }

         unsafeOperation(commands, cmds -> {
            CommandDispatcher<CommandSourceStack> dispatcher = cmds.getDispatcher();
            RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();

            for (String removedLabel : removed) {
               try {
                  commandnodeRemoveMethod.invoke(root, removedLabel);
               } catch (ReflectiveOperationException e) {
                  throw new RuntimeException("Failed to delete node " + removedLabel, e);
               }
            }
         });
      }
   }

   @Override
   public void unregisterRootCommand(final CommandComponent<C> rootCommand) {
      Commands commands = this.commands;
      if (commands != null) {
         this.unregisterRoot(commands, rootCommand.name());
         this.resendCommands();
      }
   }

   private void resendCommands() {
      for (Player player : this.metaHolder.owningPlugin().getServer().getOnlinePlayers()) {
         player.updateCommands();
      }
   }

   private static void unsafeOperation(final Commands commands, final Consumer<Commands> task) {
      unsafeGet(commands, cmds -> {
         task.accept(cmds);
         return null;
      });
   }

   private static <T> T unsafeGet(final Commands commands, final Function<Commands, T> task) {
      try {
         if (commandsInvalidField == null) {
            commandsInvalidField = commands.getClass().getDeclaredField("invalid");
            commandsInvalidField.setAccessible(true);
         }

         boolean prev = commandsInvalidField.getBoolean(commands);

         try {
            commandsInvalidField.setBoolean(commands, false);
            return task.apply(commands);
         } finally {
            commandsInvalidField.setBoolean(commands, prev);
         }
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException("Failed to perform unsafe command operation", e);
      }
   }
}
