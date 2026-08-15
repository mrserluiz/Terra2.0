package org.incendo.cloud.parser.aggregate;

import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;

final class AggregateParserImpl<C, O> implements AggregateParser<C, O> {
   private final List<CommandComponent<C>> components;
   private final TypeToken<O> valueType;
   private final AggregateResultMapper<C, O> mapper;

   AggregateParserImpl(
      final @NonNull List<CommandComponent<C>> components, final @NonNull TypeToken<O> valueType, final @NonNull AggregateResultMapper<C, O> mapper
   ) {
      this.components = components;
      this.valueType = valueType;
      this.mapper = mapper;
   }

   @Override
   public @NonNull List<@NonNull CommandComponent<C>> components() {
      return Collections.unmodifiableList(this.components);
   }

   @Override
   public @NonNull AggregateResultMapper<C, O> mapper() {
      return this.mapper;
   }

   @Override
   public @NonNull TypeToken<O> valueType() {
      return this.valueType;
   }
}
