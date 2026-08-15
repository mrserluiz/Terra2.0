package com.dfsek.terra.lib.commons.lang3.exception;

public class UncheckedIllegalAccessException extends UncheckedReflectiveOperationException {
   private static final long serialVersionUID = 1L;

   public UncheckedIllegalAccessException(Throwable cause) {
      super(cause);
   }
}
