package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public interface SortedMultiset<E> extends SortedMultisetBridge<E>, SortedIterable<E> {
   @Override
   Comparator<? super E> comparator();

   Multiset.@Nullable Entry<E> firstEntry();

   Multiset.@Nullable Entry<E> lastEntry();

   Multiset.@Nullable Entry<E> pollFirstEntry();

   Multiset.@Nullable Entry<E> pollLastEntry();

   NavigableSet<E> elementSet();

   @Override
   Set<Multiset.Entry<E>> entrySet();

   @Override
   Iterator<E> iterator();

   SortedMultiset<E> descendingMultiset();

   SortedMultiset<E> headMultiset(@ParametricNullness E upperBound, BoundType boundType);

   SortedMultiset<E> subMultiset(@ParametricNullness E lowerBound, BoundType lowerBoundType, @ParametricNullness E upperBound, BoundType upperBoundType);

   SortedMultiset<E> tailMultiset(@ParametricNullness E lowerBound, BoundType boundType);
}
