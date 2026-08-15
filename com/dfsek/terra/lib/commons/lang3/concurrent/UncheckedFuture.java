package com.dfsek.terra.lib.commons.lang3.concurrent;

import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface UncheckedFuture<V> extends Future<V> {
   static <T> Stream<UncheckedFuture<T>> map(Collection<Future<T>> futures) {
      return futures.stream().map(UncheckedFuture::on);
   }

   static <T> Collection<UncheckedFuture<T>> on(Collection<Future<T>> futures) {
      return map(futures).collect(Collectors.toList());
   }

   static <T> UncheckedFuture<T> on(Future<T> future) {
      return new UncheckedFutureImpl<>(future);
   }

   @Override
   V get();

   @Override
   V get(long var1, TimeUnit var3);
}
