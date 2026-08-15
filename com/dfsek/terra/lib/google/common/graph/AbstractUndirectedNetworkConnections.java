package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

abstract class AbstractUndirectedNetworkConnections<N, E> implements NetworkConnections<N, E> {
   final Map<E, N> incidentEdgeMap;

   AbstractUndirectedNetworkConnections(Map<E, N> incidentEdgeMap) {
      this.incidentEdgeMap = Preconditions.checkNotNull(incidentEdgeMap);
   }

   @Override
   public Set<N> predecessors() {
      return this.adjacentNodes();
   }

   @Override
   public Set<N> successors() {
      return this.adjacentNodes();
   }

   @Override
   public Set<E> incidentEdges() {
      return Collections.unmodifiableSet(this.incidentEdgeMap.keySet());
   }

   @Override
   public Set<E> inEdges() {
      return this.incidentEdges();
   }

   @Override
   public Set<E> outEdges() {
      return this.incidentEdges();
   }

   @Override
   public N adjacentNode(E edge) {
      return Objects.requireNonNull(this.incidentEdgeMap.get(edge));
   }

   @Override
   public @Nullable N removeInEdge(E edge, boolean isSelfLoop) {
      return !isSelfLoop ? this.removeOutEdge(edge) : null;
   }

   @Override
   public N removeOutEdge(E edge) {
      N previousNode = this.incidentEdgeMap.remove(edge);
      return Objects.requireNonNull(previousNode);
   }

   @Override
   public void addInEdge(E edge, N node, boolean isSelfLoop) {
      if (!isSelfLoop) {
         this.addOutEdge(edge, node);
      }
   }

   @Override
   public void addOutEdge(E edge, N node) {
      N previousNode = this.incidentEdgeMap.put(edge, node);
      Preconditions.checkState(previousNode == null);
   }
}
