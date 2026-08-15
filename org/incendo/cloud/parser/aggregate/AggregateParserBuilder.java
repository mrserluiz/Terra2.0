package org.incendo.cloud.parser.aggregate;

import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.STABLE)
public class AggregateParserBuilder<C> {
   private final List<CommandComponent<C>> components;

   AggregateParserBuilder(final @NonNull List<? extends CommandComponent<C>> components) {
      this.components = Collections.unmodifiableList(components);
   }

   AggregateParserBuilder() {
      this.components = Collections.emptyList();
   }

   public final <O> AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withMapper(
      final @NonNull TypeToken<O> valueType, final @NonNull AggregateResultMapper<C, O> mapper
   ) {
      return new AggregateParserBuilder.MappedAggregateParserBuilder<>(this.components(), valueType, mapper);
   }

   public final <O> AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withMapper(
      final @NonNull Class<O> valueType, final @NonNull AggregateResultMapper<C, O> mapper
   ) {
      return new AggregateParserBuilder.MappedAggregateParserBuilder<>(this.components(), TypeToken.get(valueType), mapper);
   }

   public final <O> AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withDirectMapper(
      final @NonNull Class<O> valueType, final AggregateResultMapper.@NonNull DirectSuccessMapper<C, O> mapper
   ) {
      return new AggregateParserBuilder.MappedAggregateParserBuilder<>(this.components(), TypeToken.get(valueType), mapper);
   }

   public final <O> AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withDirectMapper(
      final @NonNull TypeToken<O> valueType, final AggregateResultMapper.@NonNull DirectSuccessMapper<C, O> mapper
   ) {
      return new AggregateParserBuilder.MappedAggregateParserBuilder<>(this.components(), valueType, mapper);
   }

   public @NonNull AggregateParserBuilder<C> withComponent(final @NonNull CommandComponent<C> component) {
      List<CommandComponent<C>> components = new ArrayList<>(this.components());
      components.add(component);
      return new AggregateParserBuilder<>(components);
   }

   public <T> @NonNull AggregateParserBuilder<C> withComponent(final @NonNull String name, final @NonNull ParserDescriptor<C, T> parserDescriptor) {
      return this.withComponent(CommandComponent.<C, T>builder().name(name).parser(parserDescriptor).build());
   }

   public <T> @NonNull AggregateParserBuilder<C> withComponent(
      final @NonNull String name, final @NonNull ParserDescriptor<C, T> parserDescriptor, final @NonNull SuggestionProvider<C> suggestionProvider
   ) {
      return this.withComponent(CommandComponent.<C, T>builder().name(name).parser(parserDescriptor).suggestionProvider(suggestionProvider).build());
   }

   public <T> @NonNull AggregateParserBuilder<C> withComponent(final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<C, T> parserDescriptor) {
      return this.withComponent(CommandComponent.<C, T>builder().key(name).parser(parserDescriptor).build());
   }

   public <T> @NonNull AggregateParserBuilder<C> withComponent(
      final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<C, T> parserDescriptor, final @NonNull SuggestionProvider<C> suggestionProvider
   ) {
      return this.withComponent(CommandComponent.<C, T>builder().key(name).parser(parserDescriptor).suggestionProvider(suggestionProvider).build());
   }

   final @NonNull List<@NonNull CommandComponent<C>> components() {
      return this.components;
   }

   public static final class MappedAggregateParserBuilder<C, O> extends AggregateParserBuilder<C> {
      private final AggregateResultMapper<C, O> mapper;
      private final TypeToken<O> valueType;

      MappedAggregateParserBuilder(
         final @NonNull List<CommandComponent<C>> components, final @NonNull TypeToken<O> valueType, final @NonNull AggregateResultMapper<C, O> mapper
      ) {
         super(components);
         this.valueType = valueType;
         this.mapper = mapper;
      }

      public AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withComponent(final @NonNull CommandComponent<C> component) {
         List<CommandComponent<C>> components = new ArrayList<>(this.components());
         components.add(component);
         return new AggregateParserBuilder.MappedAggregateParserBuilder<>(components, this.valueType, this.mapper);
      }

      public <T> AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withComponent(
         final @NonNull String name, final @NonNull ParserDescriptor<C, T> parserDescriptor
      ) {
         return this.withComponent(CommandComponent.<C, T>builder().name(name).parser(parserDescriptor).build());
      }

      public <T> AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withComponent(
         final @NonNull String name, final @NonNull ParserDescriptor<C, T> parserDescriptor, final @NonNull SuggestionProvider<C> suggestionProvider
      ) {
         return this.withComponent(CommandComponent.<C, T>builder().name(name).parser(parserDescriptor).suggestionProvider(suggestionProvider).build());
      }

      public <T> AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withComponent(
         final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<C, T> parserDescriptor
      ) {
         return this.withComponent(CommandComponent.<C, T>builder().key(name).parser(parserDescriptor).build());
      }

      public <T> AggregateParserBuilder.@NonNull MappedAggregateParserBuilder<C, O> withComponent(
         final @NonNull CloudKey<T> name, final @NonNull ParserDescriptor<C, T> parserDescriptor, final @NonNull SuggestionProvider<C> suggestionProvider
      ) {
         return this.withComponent(CommandComponent.<C, T>builder().key(name).parser(parserDescriptor).suggestionProvider(suggestionProvider).build());
      }

      public @NonNull AggregateParser<C, O> build() {
         return new AggregateParserImpl<>(this.components(), this.valueType, this.mapper);
      }
   }
}
