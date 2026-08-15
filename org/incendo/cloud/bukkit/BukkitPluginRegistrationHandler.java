package org.incendo.cloud.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.internal.BukkitHelper;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.setting.ManagerSetting;

@API(status = Status.INTERNAL)
public class BukkitPluginRegistrationHandler<C> implements CommandRegistrationHandler<C> {
   private final Map<CommandComponent<C>, BukkitPluginRegistrationHandler.RegisteredCommandData<C>> registeredCommands = new HashMap<>();
   private final Set<String> recognizedAliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
   private Map<String, Command> bukkitCommands;
   private BukkitCommandManager<C> bukkitCommandManager;
   private CommandMap commandMap;

   protected BukkitPluginRegistrationHandler() {
   }

   final void initialize(final @NonNull BukkitCommandManager<C> bukkitCommandManager) throws ReflectiveOperationException {
      Method getCommandMap = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
      getCommandMap.setAccessible(true);
      this.commandMap = (CommandMap)getCommandMap.invoke(Bukkit.getServer());
      Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
      knownCommands.setAccessible(true);
      Map<String, Command> bukkitCommands = (Map<String, Command>)knownCommands.get(this.commandMap);
      this.bukkitCommands = bukkitCommands;
      this.bukkitCommandManager = bukkitCommandManager;
   }

   @Override
   public final boolean registerCommand(final @NonNull Command<C> command) {
      CommandComponent<C> component = command.rootComponent();
      if (!(this.bukkitCommandManager.commandRegistrationHandler() instanceof CloudCommodoreManager) && this.registeredCommands.containsKey(component)) {
         return false;
      }

      String label = component.name();
      String namespacedLabel = BukkitHelper.namespacedLabel(this.bukkitCommandManager, label);
      List<String> aliases = new ArrayList<>(component.alternativeAliases());
      BukkitCommand<C> bukkitCommand = new BukkitCommand<>(label, aliases, command, component, this.bukkitCommandManager);
      if (this.bukkitCommandManager.settings().get(ManagerSetting.OVERRIDE_EXISTING_COMMANDS)) {
         this.bukkitCommands.remove(label);
         aliases.forEach(this.bukkitCommands::remove);
      }

      Set<String> newAliases = new HashSet<>();

      for (String alias : aliases) {
         String namespacedAlias = BukkitHelper.namespacedLabel(this.bukkitCommandManager, alias);
         newAliases.add(namespacedAlias);
         if (!this.bukkitCommandOrAliasExists(alias)) {
            newAliases.add(alias);
         }
      }

      if (!this.bukkitCommandExists(label)) {
         newAliases.add(label);
      }

      newAliases.add(namespacedLabel);
      this.commandMap.register(label, this.bukkitCommandManager.owningPlugin().getName().toLowerCase(Locale.ROOT), bukkitCommand);
      this.recognizedAliases.addAll(newAliases);
      if (this.bukkitCommandManager.splitAliases()) {
         newAliases.forEach(aliasx -> this.registerExternal(aliasx, command, bukkitCommand));
      }

      this.registeredCommands.put(component, new BukkitPluginRegistrationHandler.RegisteredCommandData<>(bukkitCommand, newAliases));
      return true;
   }

   @Override
   public final void unregisterRootCommand(final @NonNull CommandComponent<C> component) {
      BukkitPluginRegistrationHandler.RegisteredCommandData<C> registeredCommand = this.registeredCommands.get(component);
      if (registeredCommand != null) {
         registeredCommand.bukkit.disable();
         Set<String> registeredAliases = registeredCommand.recognizedAliases;

         for (String alias : registeredAliases) {
            this.bukkitCommands.remove(alias);
         }

         this.recognizedAliases.removeAll(registeredAliases);
         if (this.bukkitCommandManager.splitAliases()) {
            registeredAliases.forEach(this::unregisterExternal);
         }

         this.registeredCommands.remove(component);
         if (this.bukkitCommandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
         }
      }
   }

   public boolean isRecognized(final @NonNull String alias) {
      return this.recognizedAliases.contains(alias);
   }

   protected void registerExternal(final @NonNull String label, final @NonNull Command<?> command, final @NonNull BukkitCommand<C> bukkitCommand) {
   }

   @API(status = Status.STABLE, since = "1.7.0")
   protected void unregisterExternal(final @NonNull String label) {
   }

   private boolean bukkitCommandExists(final String commandLabel) {
      Command existingCommand = this.bukkitCommands.get(commandLabel);
      if (existingCommand == null) {
         return false;
      } else {
         return !(existingCommand instanceof PluginIdentifiableCommand)
            ? existingCommand.getLabel().equals(commandLabel)
            : existingCommand.getLabel().equals(commandLabel)
               && !((PluginIdentifiableCommand)existingCommand).getPlugin().getName().equalsIgnoreCase(this.bukkitCommandManager.owningPlugin().getName());
      }
   }

   private boolean bukkitCommandOrAliasExists(final String commandLabel) {
      Command command = this.bukkitCommands.get(commandLabel);
      return command instanceof PluginIdentifiableCommand
         ? !((PluginIdentifiableCommand)command).getPlugin().getName().equalsIgnoreCase(this.bukkitCommandManager.owningPlugin().getName())
         : command != null;
   }

   private static final class RegisteredCommandData<C> {
      private final BukkitCommand<C> bukkit;
      private final Set<String> recognizedAliases;

      private RegisteredCommandData(final BukkitCommand<C> bukkit, final Set<String> recognizedAliases) {
         this.bukkit = bukkit;
         Set<String> treeSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
         treeSet.addAll(recognizedAliases);
         this.recognizedAliases = Collections.unmodifiableSet(treeSet);
      }
   }
}
