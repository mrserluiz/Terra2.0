package com.dfsek.terra.lib.google.common.graph;

import java.util.AbstractSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

abstract class IncidentEdgeSet<N> extends AbstractSet<EndpointPair<N>> {
   final N node;
   final BaseGraph<N> graph;

   IncidentEdgeSet(BaseGraph<N> graph, N node) {
      this.graph = graph;
      this.node = node;
   }

   @Override
   public boolean remove(@Nullable Object o) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int size() {
      return this.graph.isDirected()
         ? this.graph.inDegree(this.node) + this.graph.outDegree(this.node) - (this.graph.successors(this.node).contains(this.node) ? 1 : 0)
         : this.graph.adjacentNodes(this.node).size();
   }

   @Override
   public boolean contains(@Nullable Object obj) {
      if (!(obj instanceof EndpointPair)) {
         return false;
      }

      EndpointPair<?> endpointPair = (EndpointPair<?>)obj;
      if (this.graph.isDirected()) {
         if (!endpointPair.isOrdered()) {
            return false;
         }

         Object source = endpointPair.source();
         Object target = endpointPair.target();
         return this.node.equals(source) && this.graph.successors(this.node).contains(target)
            || this.node.equals(target) && this.graph.predecessors(this.node).contains(source);
      } else {
         if (endpointPair.isOrdered()) {
            return false;
         }

         Set<N> adjacent = this.graph.adjacentNodes(this.node);
         Object nodeU = endpointPair.nodeU();
         Object nodeV = endpointPair.nodeV();
         return this.node.equals(nodeV) && adjacent.contains(nodeU) || this.node.equals(nodeU) && adjacent.contains(nodeV);
      }
   }
}
