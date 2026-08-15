package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.AbstractIterator;
import com.dfsek.terra.lib.google.common.collect.UnmodifiableIterator;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

abstract class MultiEdgesConnecting<E> extends AbstractSet<E> {
   private final Map<E, ?> outEdgeToNode;
   private final Object targetNode;

   MultiEdgesConnecting(Map<E, ?> outEdgeToNode, Object targetNode) {
      this.outEdgeToNode = Preconditions.checkNotNull(outEdgeToNode);
      this.targetNode = Preconditions.checkNotNull(targetNode);
   }

   public UnmodifiableIterator<E> iterator() {
      final Iterator<? extends Entry<E, ?>> entries = this.outEdgeToNode.entrySet().iterator();
      return new AbstractIterator<E>() {
         @Override
         protected @Nullable E computeNext() {
            while (entries.hasNext()) {
               Entry<E, ?> entry = (Entry<E, ?>)entries.next();
               if (MultiEdgesConnecting.this.targetNode.equals(entry.getValue())) {
                  return entry.getKey();
               }
            }

            return (E)this.endOfData();
         }
      };
   }

   @Override
   public boolean contains(@Nullable Object edge) {
      return this.targetNode.equals(this.outEdgeToNode.get(edge));
   }
}
