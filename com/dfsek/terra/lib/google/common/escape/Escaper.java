package com.dfsek.terra.lib.google.common.escape;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Function;
import com.google.errorprone.annotations.DoNotMock;

@DoNotMock("Use Escapers.nullEscaper() or another methods from the *Escapers classes")
@GwtCompatible
public abstract class Escaper {
   private final Function<String, String> asFunction = this::escape;

   protected Escaper() {
   }

   public abstract String escape(String string);

   public final Function<String, String> asFunction() {
      return this.asFunction;
   }
}
