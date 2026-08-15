package com.dfsek.terra.lib.google.common.graph;

import java.util.Set;

abstract class ForwardingGraph<N> extends AbstractGraph<N> {
   abstract BaseGraph<N> delegate();

   @Override
   public Set<N> nodes() {
      return this.delegate().nodes();
   }

   @Override
   protected long edgeCount() {
      return this.delegate().edges().size();
   }

   @Override
   public boolean isDirected() {
      return this.delegate().isDirected();
   }

   @Override
   public boolean allowsSelfLoops() {
      return this.delegate().allowsSelfLoops();
   }

   @Override
   public ElementOrder<N> nodeOrder() {
      return this.delegate().nodeOrder();
   }

   @Override
   public ElementOrder<N> incidentEdgeOrder() {
      return this.delegate().incidentEdgeOrder();
   }

   @Override
   public Set<N> adjacentNodes(N node) {
      return this.delegate().adjacentNodes(node);
   }

   @Override
   public Set<N> predecessors(N node) {
      return this.delegate().predecessors(node);
   }

   @Override
   public Set<N> successors(N node) {
      return this.delegate().successors(node);
   }

   @Override
   public Set<EndpointPair<N>> incidentEdges(N node) {
      return this.delegate().incidentEdges(node);
   }

   @Override
   public int degree(N node) {
      return this.delegate().degree(node);
   }

   @Override
   public int inDegree(N node) {
      return this.delegate().inDegree(node);
   }

   @Override
   public int outDegree(N node) {
      return this.delegate().outDegree(node);
   }

   @Override
   public boolean hasEdgeConnecting(N nodeU, N nodeV) {
      return this.delegate().hasEdgeConnecting(nodeU, nodeV);
   }

   @Override
   public boolean hasEdgeConnecting(EndpointPair<N> endpoints) {
      return this.delegate().hasEdgeConnecting(endpoints);
   }
}
