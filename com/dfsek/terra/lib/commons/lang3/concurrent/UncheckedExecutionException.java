package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.exception.UncheckedException;

public class UncheckedExecutionException extends UncheckedException {
   private static final long serialVersionUID = 1L;

   public UncheckedExecutionException(Throwable cause) {
      super(cause);
   }
}
