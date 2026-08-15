package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;

@FunctionalInterface
@GwtCompatible
public interface RemovalListener<K, V> {
   void onRemoval(RemovalNotification<K, V> notification);
}
