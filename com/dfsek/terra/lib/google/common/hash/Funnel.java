package com.dfsek.terra.lib.google.common.hash;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.google.errorprone.annotations.DoNotMock;
import java.io.Serializable;

@DoNotMock("Implement with a lambda")
@Beta
public interface Funnel<T> extends Serializable {
   void funnel(@ParametricNullness T from, PrimitiveSink into);
}
