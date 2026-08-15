package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true)
final class JdkBackedImmutableSet<E> extends IndexedImmutableSet<E> {
   private final Set<?> delegate;
   private final ImmutableList<E> delegateList;

   JdkBackedImmutableSet(Set<?> delegate, ImmutableList<E> delegateList) {
      this.delegate = delegate;
      this.delegateList = delegateList;
   }

   @Override
   E get(int index) {
      return this.delegateList.get(index);
   }

   @Override
   public boolean contains(@Nullable Object object) {
      return this.delegate.contains(object);
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   public int size() {
      return this.delegateList.size();
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }
}
