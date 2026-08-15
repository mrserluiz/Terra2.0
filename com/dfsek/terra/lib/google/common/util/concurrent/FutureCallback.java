package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface FutureCallback<V> {
   void onSuccess(@ParametricNullness V result);

   void onFailure(Throwable t);
}
