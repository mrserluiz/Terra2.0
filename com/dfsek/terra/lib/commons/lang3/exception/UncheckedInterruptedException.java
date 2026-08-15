package com.dfsek.terra.lib.commons.lang3.exception;

public class UncheckedInterruptedException extends UncheckedException {
   private static final long serialVersionUID = 1L;

   public UncheckedInterruptedException(Throwable cause) {
      super(cause);
   }
}
