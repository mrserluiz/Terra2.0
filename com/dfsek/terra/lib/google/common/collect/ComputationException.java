package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@Deprecated
@GwtCompatible
public class ComputationException extends RuntimeException {
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public ComputationException(@Nullable Throwable cause) {
      super(cause);
   }
}
