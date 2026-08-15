package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@FunctionalInterface
@GwtCompatible
public interface AsyncCallable<V> {
   ListenableFuture<V> call() throws Exception;
}
