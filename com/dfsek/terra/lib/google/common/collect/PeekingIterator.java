package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Iterator;

@DoNotMock("Use Iterators.peekingIterator")
@GwtCompatible
public interface PeekingIterator<E> extends Iterator<E> {
   @ParametricNullness
   E peek();

   @CanIgnoreReturnValue
   @ParametricNullness
   @Override
   E next();

   @Override
   void remove();
}
