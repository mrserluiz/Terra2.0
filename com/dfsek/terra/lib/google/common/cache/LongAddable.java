package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@GwtCompatible
interface LongAddable {
   void increment();

   void add(long x);

   long sum();
}
