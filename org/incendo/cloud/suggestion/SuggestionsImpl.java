package org.incendo.cloud.suggestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "Suggestions", generator = "Immutables")
@Immutable
final class SuggestionsImpl<C, S extends Suggestion> implements Suggestions<C, S> {
   private final @NonNull CommandContext<C> commandContext;
   private final @NonNull List<S> list;
   private final @NonNull CommandInput commandInput;

   private SuggestionsImpl(@NonNull CommandContext<C> commandContext, Iterable<? extends S> list, @NonNull CommandInput commandInput) {
      this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
      this.list = createUnmodifiableList(false, createSafeList(list, true, false));
      this.commandInput = Objects.requireNonNull(commandInput, "commandInput");
   }

   private SuggestionsImpl(SuggestionsImpl<C, S> original, @NonNull CommandContext<C> commandContext, @NonNull List<S> list, @NonNull CommandInput commandInput) {
      this.commandContext = commandContext;
      this.list = list;
      this.commandInput = commandInput;
   }

   @Override
   public @NonNull CommandContext<C> commandContext() {
      return this.commandContext;
   }

   @Override
   public @NonNull List<S> list() {
      return this.list;
   }

   @Override
   public @NonNull CommandInput commandInput() {
      return this.commandInput;
   }

   public final SuggestionsImpl<C, S> withCommandContext(CommandContext<C> value) {
      if (this.commandContext == value) {
         return this;
      }

      CommandContext<C> newValue = Objects.requireNonNull(value, "commandContext");
      return new SuggestionsImpl<>(this, newValue, this.list, this.commandInput);
   }

   @SafeVarargs
   public final SuggestionsImpl<C, S> withList(S... elements) {
      List<S> newValue = createUnmodifiableList(false, createSafeList(Arrays.asList(elements), true, false));
      return new SuggestionsImpl<>(this, this.commandContext, newValue, this.commandInput);
   }

   public final SuggestionsImpl<C, S> withList(Iterable<? extends S> elements) {
      if (this.list == elements) {
         return this;
      }

      List<S> newValue = createUnmodifiableList(false, createSafeList(elements, true, false));
      return new SuggestionsImpl<>(this, this.commandContext, newValue, this.commandInput);
   }

   public final SuggestionsImpl<C, S> withCommandInput(CommandInput value) {
      if (this.commandInput == value) {
         return this;
      }

      CommandInput newValue = Objects.requireNonNull(value, "commandInput");
      return new SuggestionsImpl<>(this, this.commandContext, this.list, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof SuggestionsImpl && this.equalTo(0, (SuggestionsImpl<?, ?>)another);
   }

   private boolean equalTo(int synthetic, SuggestionsImpl<?, ?> another) {
      return this.commandContext.equals(another.commandContext) && this.list.equals(another.list) && this.commandInput.equals(another.commandInput);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.commandContext.hashCode();
      h += (h << 5) + this.list.hashCode();
      return h + (h << 5) + this.commandInput.hashCode();
   }

   @Override
   public String toString() {
      return "Suggestions{commandContext=" + this.commandContext + ", list=" + this.list + ", commandInput=" + this.commandInput + "}";
   }

   public static <C, S extends Suggestion> SuggestionsImpl<C, S> of(
      @NonNull CommandContext<C> commandContext, @NonNull List<S> list, @NonNull CommandInput commandInput
   ) {
      return of(commandContext, (Iterable<? extends S>)list, commandInput);
   }

   public static <C, S extends Suggestion> SuggestionsImpl<C, S> of(
      @NonNull CommandContext<C> commandContext, Iterable<? extends S> list, @NonNull CommandInput commandInput
   ) {
      return new SuggestionsImpl<>(commandContext, list, commandInput);
   }

   public static <C, S extends Suggestion> SuggestionsImpl<C, S> copyOf(Suggestions<C, S> instance) {
      return instance instanceof SuggestionsImpl ? (SuggestionsImpl)instance : of(instance.commandContext(), instance.list(), instance.commandInput());
   }

   private static <T> List<T> createSafeList(Iterable<? extends T> iterable, boolean checkNulls, boolean skipNulls) {
      ArrayList<T> list;
      if (iterable instanceof Collection) {
         int size = ((Collection)iterable).size();
         if (size == 0) {
            return Collections.emptyList();
         }

         list = new ArrayList<>(size);
      } else {
         list = new ArrayList<>();
      }

      for (T element : iterable) {
         if (!skipNulls || element != null) {
            if (checkNulls) {
               Objects.requireNonNull(element, "element");
            }

            list.add(element);
         }
      }

      return list;
   }

   private static <T> List<T> createUnmodifiableList(boolean clone, List<T> list) {
      switch (list.size()) {
         case 0:
            return Collections.emptyList();
         case 1:
            return Collections.singletonList(list.get(0));
         default:
            if (clone) {
               return Collections.unmodifiableList(new ArrayList<>(list));
            } else {
               if (list instanceof ArrayList) {
                  ((ArrayList)list).trimToSize();
               }

               return Collections.unmodifiableList(list);
            }
      }
   }
}
