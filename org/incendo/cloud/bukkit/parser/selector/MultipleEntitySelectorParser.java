package org.incendo.cloud.bukkit.parser.selector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.data.MultipleEntitySelector;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.parser.ParserDescriptor;

public final class MultipleEntitySelectorParser<C> extends SelectorUtils.EntitySelectorParser<C, MultipleEntitySelector> {
   private final boolean allowEmpty;

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, MultipleEntitySelector> multipleEntitySelectorParser() {
      return multipleEntitySelectorParser(true);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, MultipleEntitySelector> multipleEntitySelectorParser(final boolean allowEmpty) {
      return ParserDescriptor.of(new MultipleEntitySelectorParser<>(allowEmpty), MultipleEntitySelector.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, MultipleEntitySelector> multipleEntitySelectorComponent() {
      return CommandComponent.<C, MultipleEntitySelector>builder().parser(multipleEntitySelectorParser());
   }

   @API(status = Status.STABLE, since = "1.8.0")
   public MultipleEntitySelectorParser(final boolean allowEmpty) {
      super(false);
      this.allowEmpty = allowEmpty;
   }

   public MultipleEntitySelectorParser() {
      this(true);
   }

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public MultipleEntitySelector mapResult(final @NonNull String input, final SelectorUtils.@NonNull EntitySelectorWrapper wrapper) {
      final List<Entity> entities = wrapper.entities();
      if (entities.isEmpty() && !this.allowEmpty) {
         new SelectorUtils.SelectorParser.Thrower(NO_ENTITIES_EXCEPTION_TYPE.get()).throwIt();
      }

      return new MultipleEntitySelector() {
         @Override
         public @NonNull String inputString() {
            return input;
         }

         @Override
         public @NonNull Collection<Entity> values() {
            return Collections.unmodifiableCollection(entities);
         }
      };
   }
}
