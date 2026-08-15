package com.dfsek.terra.lib.google.common.graph;

import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

abstract class ForwardingValueGraph<N, V> extends AbstractValueGraph<N, V> {
   abstract ValueGraph<N, V> delegate();

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

   @Override
   public Optional<V> edgeValue(N nodeU, N nodeV) {
      return this.delegate().edgeValue(nodeU, nodeV);
   }

   @Override
   public Optional<V> edgeValue(EndpointPair<N> endpoints) {
      return this.delegate().edgeValue(endpoints);
   }

   @Override
   public @Nullable V edgeValueOrDefault(N nodeU, N nodeV, @Nullable V defaultValue) {
      return this.delegate().edgeValueOrDefault(nodeU, nodeV, defaultValue);
   }

   @Override
   public @Nullable V edgeValueOrDefault(EndpointPair<N> endpoints, @Nullable V defaultValue) {
      return this.delegate().edgeValueOrDefault(endpoints, defaultValue);
   }
}
