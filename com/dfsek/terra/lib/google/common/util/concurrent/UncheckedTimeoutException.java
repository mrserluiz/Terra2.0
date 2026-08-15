package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public class UncheckedTimeoutException extends RuntimeException {
   private static final long serialVersionUID = 0L;

   public UncheckedTimeoutException() {
   }

   public UncheckedTimeoutException(@Nullable String message) {
      super(message);
   }

   public UncheckedTimeoutException(@Nullable Throwable cause) {
      super(cause);
   }

   public UncheckedTimeoutException(@Nullable String message, @Nullable Throwable cause) {
      super(message, cause);
   }
}
