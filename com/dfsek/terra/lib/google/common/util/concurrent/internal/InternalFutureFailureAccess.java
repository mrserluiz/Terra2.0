package com.dfsek.terra.lib.google.common.util.concurrent.internal;

public abstract class InternalFutureFailureAccess {
   protected InternalFutureFailureAccess() {
   }

   protected abstract Throwable tryInternalFastPathGetFailure();
}
