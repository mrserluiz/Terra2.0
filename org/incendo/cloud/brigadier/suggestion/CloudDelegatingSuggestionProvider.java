package org.incendo.cloud.brigadier.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.internal.CommandNode;

@API(status = Status.INTERNAL, since = "2.0.0")
public final class CloudDelegatingSuggestionProvider<C, S> implements SuggestionProvider<S> {
   private final BrigadierSuggestionFactory<C, S> brigadierSuggestionFactory;
   private final CommandNode<C> node;

   public CloudDelegatingSuggestionProvider(final @NonNull BrigadierSuggestionFactory<C, S> suggestionFactory, final @NonNull CommandNode<C> node) {
      this.brigadierSuggestionFactory = suggestionFactory;
      this.node = node;
   }

   public @NonNull CompletableFuture<Suggestions> getSuggestions(final @NonNull CommandContext<S> context, final @NonNull SuggestionsBuilder builder) throws CommandSyntaxException {
      return this.brigadierSuggestionFactory.buildSuggestions(context, this.node.parent(), builder);
   }
}
