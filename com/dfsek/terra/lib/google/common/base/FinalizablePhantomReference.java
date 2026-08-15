package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.lang.ref.PhantomReference;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public abstract class FinalizablePhantomReference<T> extends PhantomReference<T> implements FinalizableReference {
   protected FinalizablePhantomReference(@Nullable T referent, FinalizableReferenceQueue queue) {
      super(referent, queue.queue);
      queue.cleanUp();
   }
}
