package com.dfsek.terra.lib.commons.lang3.concurrent;

public class ConcurrentException extends Exception {
   private static final long serialVersionUID = 6622707671812226130L;

   protected ConcurrentException() {
   }

   public ConcurrentException(String msg, Throwable cause) {
      super(msg, ConcurrentUtils.checkedException(cause));
   }

   public ConcurrentException(Throwable cause) {
      super(ConcurrentUtils.checkedException(cause));
   }
}
