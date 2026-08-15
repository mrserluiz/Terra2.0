package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableSet;
import com.dfsek.terra.lib.google.common.collect.Iterators;
import com.dfsek.terra.lib.google.common.collect.UnmodifiableIterator;
import java.util.AbstractSet;
import java.util.Map;
import org.jspecify.annotations.Nullable;

final class EdgesConnecting<E> extends AbstractSet<E> {
   private final Map<?, E> nodeToOutEdge;
   private final Object targetNode;

   EdgesConnecting(Map<?, E> nodeToEdgeMap, Object targetNode) {
      this.nodeToOutEdge = Preconditions.checkNotNull(nodeToEdgeMap);
      this.targetNode = Preconditions.checkNotNull(targetNode);
   }

   public UnmodifiableIterator<E> iterator() {
      E connectingEdge = this.getConnectingEdge();
      return connectingEdge == null ? ImmutableSet.<E>of().iterator() : Iterators.singletonIterator(connectingEdge);
   }

   @Override
   public int size() {
      return this.getConnectingEdge() == null ? 0 : 1;
   }

   @Override
   public boolean contains(@Nullable Object edge) {
      E connectingEdge = this.getConnectingEdge();
      return connectingEdge != null && connectingEdge.equals(edge);
   }

   private @Nullable E getConnectingEdge() {
      return this.nodeToOutEdge.get(this.targetNode);
   }
}
