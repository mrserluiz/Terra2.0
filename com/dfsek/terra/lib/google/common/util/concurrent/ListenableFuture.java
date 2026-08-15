package com.dfsek.terra.lib.google.common.util.concurrent;

import com.google.errorprone.annotations.DoNotMock;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.jspecify.annotations.NullMarked;

@DoNotMock("Use the methods in Futures (like immediateFuture) or SettableFuture")
@NullMarked
public interface ListenableFuture<V> extends Future<V> {
   void addListener(Runnable listener, Executor executor);
}
