package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Optional;

abstract class AbstractGraphBuilder<N> {
   final boolean directed;
   boolean allowsSelfLoops = false;
   ElementOrder<N> nodeOrder = ElementOrder.insertion();
   ElementOrder<N> incidentEdgeOrder = ElementOrder.unordered();
   Optional<Integer> expectedNodeCount = Optional.absent();

   AbstractGraphBuilder(boolean directed) {
      this.directed = directed;
   }
}
