package org.incendo.cloud.help;

import io.leangen.geantyref.GenericTypeReflector;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.help.result.HelpQueryResult;
import org.incendo.cloud.help.result.IndexCommandResult;
import org.incendo.cloud.help.result.MultipleCommandResult;
import org.incendo.cloud.help.result.VerboseCommandResult;
import org.incendo.cloud.internal.CommandInputTokenizer;
import org.incendo.cloud.internal.CommandNode;

@API(status = Status.STABLE)
public class StandardHelpHandler<C> implements HelpHandler<C> {
   private final CommandManager<C> commandManager;
   private final CommandPredicate<C> commandFilter;

   public StandardHelpHandler(final @NonNull CommandManager<C> commandManager, final @NonNull CommandPredicate<C> commandPredicate) {
      this.commandManager = commandManager;
      this.commandFilter = commandPredicate;
   }

   @Override
   public @NonNull HelpQueryResult<C> query(final @NonNull HelpQuery<C> query) {
      List<CommandEntry<C>> commands = this.commands(query.sender());
      if (query.query().replace(" ", "").isEmpty()) {
         return IndexCommandResult.of(query, commands);
      }

      List<String> queryFragments = new CommandInputTokenizer(query.query()).tokenize();
      String rootFragment = queryFragments.get(0);
      List<Command<C>> availableCommands = new LinkedList<>();
      Set<String> availableCommandLabels = new HashSet<>();
      boolean exactMatch = false;

      for (CommandEntry<C> entry : commands) {
         Command<C> command = entry.command();
         CommandComponent<C> component = command.rootComponent();

         for (String alias : component.aliases()) {
            if (alias.toLowerCase(Locale.ENGLISH).startsWith(rootFragment.toLowerCase(Locale.ENGLISH))) {
               availableCommands.add(command);
               availableCommandLabels.add(component.name());
               break;
            }
         }

         for (String alias : component.aliases()) {
            if (alias.equalsIgnoreCase(rootFragment)) {
               exactMatch = true;
               break;
            }
         }

         if (rootFragment.equalsIgnoreCase(component.name())) {
            availableCommandLabels.clear();
            availableCommands.clear();
            availableCommandLabels.add(component.name());
            availableCommands.add(command);
            break;
         }
      }

      if (availableCommands.isEmpty()) {
         return IndexCommandResult.of(query, Collections.emptyList());
      }

      if (exactMatch && availableCommandLabels.size() <= 1) {
         CommandNode<C> node = this.commandManager.commandTree().getNamedNode(availableCommandLabels.iterator().next());
         List<CommandComponent<C>> traversedNodes = new LinkedList<>();
         CommandNode<C> head = node;
         int index = 0;

         label112:
         while (head != null && this.isNodeVisible(head)) {
            index++;
            traversedNodes.add(head.component());
            if (head.component() != null
               && head.command() != null
               && (head.isLeaf() || index == queryFragments.size())
               && this.isAllowed(query.sender(), head.command())) {
               return VerboseCommandResult.of(
                  query, CommandEntry.of(head.command(), this.commandManager.commandSyntaxFormatter().apply(query.sender(), head.command().components(), null))
               );
            }

            if (head.children().size() == 1) {
               head = head.children().get(0);
            } else {
               if (index < queryFragments.size()) {
                  CommandNode<C> potentialVariable = null;

                  for (CommandNode<C> child : head.children()) {
                     if (child.component() != null && child.component().type() == CommandComponent.ComponentType.LITERAL) {
                        for (String childAlias : child.component().aliases()) {
                           if (childAlias.equalsIgnoreCase(queryFragments.get(index))) {
                              head = child;
                              continue label112;
                           }
                        }
                     } else if (child.component() != null) {
                        potentialVariable = child;
                     }
                  }

                  if (potentialVariable != null) {
                     head = potentialVariable;
                     continue;
                  }
               }

               String currentDescription = this.commandManager.commandSyntaxFormatter().apply(query.sender(), traversedNodes, null);
               List<String> childSuggestions = new LinkedList<>();

               for (CommandNode<C> child : head.children()) {
                  if (this.isNodeVisible(child)) {
                     List<CommandComponent<C>> traversedNodesSub = new LinkedList<>(traversedNodes);
                     if (child.component() == null || child.command() == null || this.isAllowed(query.sender(), child.command())) {
                        traversedNodesSub.add(child.component());
                        childSuggestions.add(this.commandManager.commandSyntaxFormatter().apply(query.sender(), traversedNodesSub, child));
                     }
                  }
               }

               return MultipleCommandResult.of(query, currentDescription, childSuggestions);
            }
         }

         return IndexCommandResult.of(query, Collections.emptyList());
      } else {
         return IndexCommandResult.of(
            query,
            availableCommands.stream()
               .map(
                  command -> CommandEntry.of(
                     (Command<C>)command, this.commandManager.commandSyntaxFormatter().apply(query.sender(), command.components(), null)
                  )
               )
               .sorted()
               .filter(entry -> this.isAllowed(query.sender(), entry.command()))
               .collect(Collectors.toList())
         );
      }
   }

   protected @NonNull List<@NonNull CommandEntry<C>> commands(final @NonNull C sender) {
      return this.commandManager
         .commands()
         .stream()
         .filter(this.commandFilter)
         .filter(command -> this.isAllowed(sender, (Command<C>)command))
         .map(command -> CommandEntry.of((Command<C>)command, this.commandManager.commandSyntaxFormatter().apply(sender, command.components(), null)))
         .sorted()
         .collect(Collectors.toList());
   }

   private boolean isAllowed(final C sender, final Command<C> command) {
      return command.senderType().isPresent() && !GenericTypeReflector.isSuperType(command.senderType().get().getType(), sender.getClass())
         ? false
         : this.commandManager.testPermission(sender, command.commandPermission()).allowed();
   }

   protected boolean isNodeVisible(final @NonNull CommandNode<C> node) {
      CommandComponent<C> component = node.component();
      if (component != null) {
         Command<C> owningCommand = node.command();
         if (owningCommand != null && this.commandFilter.test(owningCommand)) {
            return true;
         }
      }

      for (CommandNode<C> childNode : node.children()) {
         if (this.isNodeVisible(childNode)) {
            return true;
         }
      }

      return false;
   }
}
