package org.incendo.cloud.parser.aggregate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.key.MutableCloudKeyContainer;

@API(status = Status.STABLE)
public interface AggregateParsingContext<C> extends MutableCloudKeyContainer {
   static <C> @NonNull AggregateParsingContext<C> argumentContext(final @NonNull AggregateParser<C, ?> parser) {
      return new AggregateParsingContextImpl<>(parser);
   }
}
