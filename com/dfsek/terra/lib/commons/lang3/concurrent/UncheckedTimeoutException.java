package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.exception.UncheckedException;

public class UncheckedTimeoutException extends UncheckedException {
   private static final long serialVersionUID = 1L;

   public UncheckedTimeoutException(Throwable cause) {
      super(cause);
   }
}
