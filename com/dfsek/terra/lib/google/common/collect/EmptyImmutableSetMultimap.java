package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.util.Collection;

@GwtCompatible(serializable = true)
class EmptyImmutableSetMultimap extends ImmutableSetMultimap<Object, Object> {
   static final EmptyImmutableSetMultimap INSTANCE = new EmptyImmutableSetMultimap();
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   private EmptyImmutableSetMultimap() {
      super(ImmutableMap.of(), 0, null);
   }

   @Override
   public ImmutableMap<Object, Collection<Object>> asMap() {
      return super.asMap();
   }

   private Object readResolve() {
      return INSTANCE;
   }
}
