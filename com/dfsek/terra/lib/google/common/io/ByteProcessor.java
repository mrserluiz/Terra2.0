package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.io.IOException;

@DoNotMock("Implement it normally")
@J2ktIncompatible
@GwtIncompatible
public interface ByteProcessor<T> {
   @CanIgnoreReturnValue
   boolean processBytes(byte[] buf, int off, int len) throws IOException;

   @ParametricNullness
   T getResult();
}
