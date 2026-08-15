package org.incendo.cloud.syntax;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.internal.CommandNode;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.incendo.cloud.permission.Permission;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public class StandardCommandSyntaxFormatter<C> implements CommandSyntaxFormatter<C> {
   private final CommandManager<C> manager;

   public StandardCommandSyntaxFormatter(final @NonNull CommandManager<C> manager) {
      this.manager = manager;
   }

   @Override
   public final @NonNull String apply(
      final @Nullable C sender, final @NonNull List<@NonNull CommandComponent<C>> commandComponents, final @Nullable CommandNode<C> node
   ) {
      return this.apply(commandComponents, node, n -> {
         if (sender == null) {
            return true;
         }

         Map<Type, Permission> accessMap = n.nodeMeta().getOrDefault(CommandNode.META_KEY_ACCESS, Collections.emptyMap());

         for (Entry<Type, Permission> entry : accessMap.entrySet()) {
            if (GenericTypeReflector.isSuperType(entry.getKey(), sender.getClass()) && this.manager.testPermission(sender, entry.getValue()).allowed()) {
               return true;
            }
         }

         return false;
      });
   }

   private @NonNull String apply(
      final @NonNull List<@NonNull CommandComponent<C>> commandComponents,
      final @Nullable CommandNode<C> node,
      final @NonNull Predicate<@NonNull CommandNode<C>> filter
   ) {
      StandardCommandSyntaxFormatter.FormattingInstance formattingInstance = this.createInstance();
      Iterator<CommandComponent<C>> iterator = commandComponents.iterator();

      while (iterator.hasNext()) {
         CommandComponent<C> commandComponent = iterator.next();
         if (commandComponent.type() == CommandComponent.ComponentType.LITERAL) {
            formattingInstance.appendLiteral(commandComponent);
         } else if (commandComponent.parser() instanceof AggregateParser) {
            AggregateParser<?, ?> aggregateParser = (AggregateParser<?, ?>)commandComponent.parser();
            formattingInstance.appendAggregate(commandComponent, aggregateParser);
         } else if (commandComponent.type() == CommandComponent.ComponentType.FLAG) {
            formattingInstance.appendFlag((CommandFlagParser<?>)commandComponent.parser());
         } else if (commandComponent.required()) {
            formattingInstance.appendRequired(commandComponent);
         } else {
            formattingInstance.appendOptional(commandComponent);
         }

         if (iterator.hasNext()) {
            formattingInstance.appendBlankSpace();
         }
      }

      for (CommandNode<C> tail = node; tail != null && !tail.isLeaf() && filter.test(tail); tail = tail.children().get(0)) {
         if (tail.children().size() > 1) {
            formattingInstance.appendBlankSpace();
            Iterator<CommandNode<C>> childIterator = tail.children().stream().filter(filter).iterator();

            while (childIterator.hasNext()) {
               CommandNode<C> child = childIterator.next();
               if (child.component() != null) {
                  switch (child.component().type()) {
                     case LITERAL:
                        formattingInstance.appendName(child.component().name());
                        break;
                     case REQUIRED_VARIABLE:
                        formattingInstance.appendRequired(child.component());
                        break;
                     case OPTIONAL_VARIABLE:
                        formattingInstance.appendOptional(child.component());
                  }

                  if (childIterator.hasNext()) {
                     formattingInstance.appendPipe();
                  }
               }
            }
            break;
         }

         if (!filter.test(tail.children().get(0))) {
            break;
         }

         CommandComponent<C> component = tail.children().get(0).component();
         if (component.parser() instanceof AggregateParser) {
            AggregateParser<?, ?> aggregateParser = (AggregateParser<?, ?>)component.parser();
            formattingInstance.appendBlankSpace();
            formattingInstance.appendAggregate(component, aggregateParser);
         } else if (component.type() == CommandComponent.ComponentType.FLAG) {
            formattingInstance.appendBlankSpace();
            formattingInstance.appendFlag((CommandFlagParser<?>)component.parser());
         } else if (component.type() == CommandComponent.ComponentType.LITERAL) {
            formattingInstance.appendBlankSpace();
            formattingInstance.appendLiteral(component);
         } else {
            formattingInstance.appendBlankSpace();
            if (component.required()) {
               formattingInstance.appendRequired(component);
            } else {
               formattingInstance.appendOptional(component);
            }
         }
      }

      return formattingInstance.toString();
   }

   protected StandardCommandSyntaxFormatter.@NonNull FormattingInstance createInstance() {
      return new StandardCommandSyntaxFormatter.FormattingInstance();
   }

   @API(status = Status.STABLE)
   public static class FormattingInstance {
      private final StringBuilder builder = new StringBuilder();

      protected FormattingInstance() {
      }

      @Override
      public final @NonNull String toString() {
         return this.builder.toString();
      }

      public void appendLiteral(final @NonNull CommandComponent<?> literal) {
         this.appendName(literal.name());
      }

      @API(status = Status.STABLE)
      public void appendAggregate(final @NonNull CommandComponent<?> component, final @NonNull AggregateParser<?, ?> parser) {
         String prefix = component.required() ? this.requiredPrefix() : this.optionalPrefix();
         String suffix = component.required() ? this.requiredSuffix() : this.optionalSuffix();
         this.builder.append(prefix);
         Iterator<? extends CommandComponent<?>> innerComponents = parser.components().iterator();

         while (innerComponents.hasNext()) {
            CommandComponent<?> innerComponent = (CommandComponent<?>)innerComponents.next();
            this.builder.append(prefix);
            this.appendName(innerComponent.name());
            this.builder.append(suffix);
            if (innerComponents.hasNext()) {
               this.builder.append(' ');
            }
         }

         this.builder.append(suffix);
      }

      public void appendFlag(final @NonNull CommandFlagParser<?> flagParser) {
         this.builder.append(this.optionalPrefix());
         Iterator<CommandFlag<?>> flagIterator = flagParser.flags().iterator();

         while (flagIterator.hasNext()) {
            CommandFlag<?> flag = flagIterator.next();
            this.appendName(String.format("--%s", flag.name()));
            if (flag.commandComponent() != null) {
               this.builder.append(' ');
               this.builder.append(this.optionalPrefix());
               this.appendName(flag.commandComponent().name());
               this.builder.append(this.optionalSuffix());
            }

            if (flagIterator.hasNext()) {
               this.appendBlankSpace();
               this.appendPipe();
               this.appendBlankSpace();
            }
         }

         this.builder.append(this.optionalSuffix());
      }

      public void appendRequired(final @NonNull CommandComponent<?> argument) {
         this.builder.append(this.requiredPrefix());
         this.appendName(argument.name());
         this.builder.append(this.requiredSuffix());
      }

      public void appendOptional(final @NonNull CommandComponent<?> argument) {
         this.builder.append(this.optionalPrefix());
         this.appendName(argument.name());
         this.builder.append(this.optionalSuffix());
      }

      public void appendPipe() {
         this.builder.append("|");
      }

      public void appendName(final @NonNull String name) {
         this.builder.append(name);
      }

      public @NonNull String requiredPrefix() {
         return "<";
      }

      public @NonNull String requiredSuffix() {
         return ">";
      }

      public @NonNull String optionalPrefix() {
         return "[";
      }

      public @NonNull String optionalSuffix() {
         return "]";
      }

      public void appendBlankSpace() {
         this.builder.append(' ');
      }
   }
}
