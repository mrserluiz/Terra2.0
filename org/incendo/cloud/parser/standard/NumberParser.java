package org.incendo.cloud.parser.standard;

import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.type.range.Range;

@API(status = Status.STABLE)
public abstract class NumberParser<C, N extends Number, R extends Range<N>> implements ArgumentParser<C, N> {
   private final R range;

   protected NumberParser(final @NonNull R range) {
      this.range = Objects.requireNonNull(range, "range");
   }

   public final @NonNull R range() {
      return this.range;
   }

   public abstract boolean hasMax();

   public abstract boolean hasMin();
}
