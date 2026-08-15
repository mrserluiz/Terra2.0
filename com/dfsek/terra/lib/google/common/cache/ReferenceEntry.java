package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
interface ReferenceEntry<K, V> {
   LocalCache.@Nullable ValueReference<K, V> getValueReference();

   void setValueReference(LocalCache.ValueReference<K, V> valueReference);

   @Nullable ReferenceEntry<K, V> getNext();

   int getHash();

   @Nullable K getKey();

   long getAccessTime();

   void setAccessTime(long time);

   ReferenceEntry<K, V> getNextInAccessQueue();

   void setNextInAccessQueue(ReferenceEntry<K, V> next);

   ReferenceEntry<K, V> getPreviousInAccessQueue();

   void setPreviousInAccessQueue(ReferenceEntry<K, V> previous);

   long getWriteTime();

   void setWriteTime(long time);

   ReferenceEntry<K, V> getNextInWriteQueue();

   void setNextInWriteQueue(ReferenceEntry<K, V> next);

   ReferenceEntry<K, V> getPreviousInWriteQueue();

   void setPreviousInWriteQueue(ReferenceEntry<K, V> previous);
}
