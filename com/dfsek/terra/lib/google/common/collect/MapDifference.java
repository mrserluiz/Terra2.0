package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use Maps.difference")
@GwtCompatible
public interface MapDifference<K, V> {
   boolean areEqual();

   Map<K, V> entriesOnlyOnLeft();

   Map<K, V> entriesOnlyOnRight();

   Map<K, V> entriesInCommon();

   Map<K, MapDifference.ValueDifference<V>> entriesDiffering();

   @Override
   boolean equals(@Nullable Object object);

   @Override
   int hashCode();

   @DoNotMock("Use Maps.difference")
   interface ValueDifference<V> {
      @ParametricNullness
      V leftValue();

      @ParametricNullness
      V rightValue();

      @Override
      boolean equals(@Nullable Object other);

      @Override
      int hashCode();
   }
}
