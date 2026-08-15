package com.dfsek.tectonic.api.exception;

import com.dfsek.tectonic.api.depth.DepthTracker;

public class ValueMissingException extends ConfigException {
   private static final long serialVersionUID = 4229997553358413812L;

   public ValueMissingException(String message, DepthTracker tracker) {
      super(message, tracker);
   }

   public ValueMissingException(String message, Throwable cause, DepthTracker tracker) {
      super(message, cause, tracker);
   }
}
