package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public class ExecutionError extends Error {
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   @Deprecated
   protected ExecutionError() {
   }

   @Deprecated
   protected ExecutionError(@Nullable String message) {
      super(message);
   }

   public ExecutionError(@Nullable String message, @Nullable Error cause) {
      super(message, cause);
   }

   public ExecutionError(@Nullable Error cause) {
      super(cause);
   }
}
