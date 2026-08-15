package org.incendo.cloud;

import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface SenderMapper<B, M> {
   @NonNull M map(@NonNull B base);

   @NonNull B reverse(@NonNull M mapped);

   static <B, M> @NonNull SenderMapper<B, M> create(
      final @NonNull Function<@NonNull B, @NonNull M> map, final @NonNull Function<@NonNull M, @NonNull B> reverse
   ) {
      return new SenderMapperImpl<>(map, reverse);
   }

   static <S> @NonNull SenderMapper<S, S> identity() {
      return (SenderMapper<S, S>)SenderMapperImpl.IDENTITY;
   }
}
