package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@Beta
public abstract class AbstractValueGraph<N, V> extends AbstractBaseGraph<N> implements ValueGraph<N, V> {
   @Override
   public Graph<N> asGraph() {
      return new AbstractGraph<N>() {
         @Override
         public Set<N> nodes() {
            return AbstractValueGraph.this.nodes();
         }

         @Override
         public Set<EndpointPair<N>> edges() {
            return AbstractValueGraph.this.edges();
         }

         @Override
         public boolean isDirected() {
            return AbstractValueGraph.this.isDirected();
         }

         @Override
         public boolean allowsSelfLoops() {
            return AbstractValueGraph.this.allowsSelfLoops();
         }

         @Override
         public ElementOrder<N> nodeOrder() {
            return AbstractValueGraph.this.nodeOrder();
         }

         @Override
         public ElementOrder<N> incidentEdgeOrder() {
            return AbstractValueGraph.this.incidentEdgeOrder();
         }

         @Override
         public Set<N> adjacentNodes(N node) {
            return AbstractValueGraph.this.adjacentNodes(node);
         }

         @Override
         public Set<N> predecessors(N node) {
            return AbstractValueGraph.this.predecessors(node);
         }

         @Override
         public Set<N> successors(N node) {
            return AbstractValueGraph.this.successors(node);
         }

         @Override
         public int degree(N node) {
            return AbstractValueGraph.this.degree(node);
         }

         @Override
         public int inDegree(N node) {
            return AbstractValueGraph.this.inDegree(node);
         }

         @Override
         public int outDegree(N node) {
            return AbstractValueGraph.this.outDegree(node);
         }
      };
   }

   @Override
   public Optional<V> edgeValue(N nodeU, N nodeV) {
      return Optional.ofNullable(this.edgeValueOrDefault(nodeU, nodeV, null));
   }

   @Override
   public Optional<V> edgeValue(EndpointPair<N> endpoints) {
      return Optional.ofNullable(this.edgeValueOrDefault(endpoints, null));
   }

   @Override
   public final boolean equals(@Nullable Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof ValueGraph)) {
         return false;
      }

      ValueGraph<?, ?> other = (ValueGraph<?, ?>)obj;
      return this.isDirected() == other.isDirected() && this.nodes().equals(other.nodes()) && edgeValueMap(this).equals(edgeValueMap(other));
   }

   @Override
   public final int hashCode() {
      return edgeValueMap(this).hashCode();
   }

   @Override
   public String toString() {
      return "isDirected: "
         + this.isDirected()
         + ", allowsSelfLoops: "
         + this.allowsSelfLoops()
         + ", nodes: "
         + this.nodes()
         + ", edges: "
         + edgeValueMap(this);
   }

   private static <N, V> Map<EndpointPair<N>, V> edgeValueMap(ValueGraph<N, V> graph) {
      return Maps.asMap(graph.edges(), edge -> Objects.requireNonNull(graph.edgeValueOrDefault(edge.nodeU(), edge.nodeV(), null)));
   }
}
