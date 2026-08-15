package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Predicate;
import java.util.Map.Entry;

@GwtCompatible
interface FilteredMultimap<K, V> extends Multimap<K, V> {
   Multimap<K, V> unfiltered();

   Predicate<? super Entry<K, V>> entryPredicate();
}
