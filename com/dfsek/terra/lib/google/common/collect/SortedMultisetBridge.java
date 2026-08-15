package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import java.util.SortedSet;

@GwtIncompatible
interface SortedMultisetBridge<E> extends Multiset<E> {
   SortedSet<E> elementSet();
}
