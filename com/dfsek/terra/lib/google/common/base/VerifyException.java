package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public class VerifyException extends RuntimeException {
   public VerifyException() {
   }

   public VerifyException(@Nullable String message) {
      super(message);
   }

   public VerifyException(@Nullable Throwable cause) {
      super(cause);
   }

   public VerifyException(@Nullable String message, @Nullable Throwable cause) {
      super(message, cause);
   }
}
