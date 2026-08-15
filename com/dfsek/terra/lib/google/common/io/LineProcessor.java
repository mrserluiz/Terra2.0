package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;

@J2ktIncompatible
@GwtIncompatible
public interface LineProcessor<T> {
   @CanIgnoreReturnValue
   boolean processLine(String line) throws IOException;

   @ParametricNullness
   T getResult();
}
