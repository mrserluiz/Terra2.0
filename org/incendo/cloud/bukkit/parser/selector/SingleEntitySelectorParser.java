package org.incendo.cloud.bukkit.parser.selector;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.data.SingleEntitySelector;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.parser.ParserDescriptor;

public final class SingleEntitySelectorParser<C> extends SelectorUtils.EntitySelectorParser<C, SingleEntitySelector> {
   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, SingleEntitySelector> singleEntitySelectorParser() {
      return ParserDescriptor.of(new SingleEntitySelectorParser<>(), SingleEntitySelector.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, SingleEntitySelector> singleEntitySelectorComponent() {
      return CommandComponent.<C, SingleEntitySelector>builder().parser(singleEntitySelectorParser());
   }

   public SingleEntitySelectorParser() {
      super(true);
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public SingleEntitySelector mapResult(final @NonNull String input, final SelectorUtils.@NonNull EntitySelectorWrapper wrapper) {
      final Entity entity = wrapper.singleEntity();
      return new SingleEntitySelector() {
         public @NonNull Entity single() {
            return entity;
         }

         @Override
         public @NonNull String inputString() {
            return input;
         }
      };
   }
}
