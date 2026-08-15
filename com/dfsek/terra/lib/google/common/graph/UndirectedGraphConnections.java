package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.dfsek.terra.lib.google.common.collect.Iterators;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

final class UndirectedGraphConnections<N, V> implements GraphConnections<N, V> {
   private final Map<N, V> adjacentNodeValues;

   private UndirectedGraphConnections(Map<N, V> adjacentNodeValues) {
      this.adjacentNodeValues = Preconditions.checkNotNull(adjacentNodeValues);
   }

   static <N, V> UndirectedGraphConnections<N, V> of(ElementOrder<N> incidentEdgeOrder) {
      switch (incidentEdgeOrder.type()) {
         case UNORDERED:
            return new UndirectedGraphConnections<>(new HashMap<>(2, 1.0F));
         case STABLE:
            return new UndirectedGraphConnections<>(new LinkedHashMap<>(2, 1.0F));
         default:
            throw new AssertionError(incidentEdgeOrder.type());
      }
   }

   static <N, V> UndirectedGraphConnections<N, V> ofImmutable(Map<N, V> adjacentNodeValues) {
      return new UndirectedGraphConnections<>(ImmutableMap.copyOf(adjacentNodeValues));
   }

   @Override
   public Set<N> adjacentNodes() {
      return Collections.unmodifiableSet(this.adjacentNodeValues.keySet());
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
   public Iterator<EndpointPair<N>> incidentEdgeIterator(N thisNode) {
      return Iterators.transform(this.adjacentNodeValues.keySet().iterator(), incidentNode -> EndpointPair.unordered(thisNode, (N)incidentNode));
   }

   @Override
   public @Nullable V value(N node) {
      return this.adjacentNodeValues.get(node);
   }

   @Override
   public void removePredecessor(N node) {
      V unused = this.removeSuccessor(node);
   }

   @Override
   public @Nullable V removeSuccessor(N node) {
      return this.adjacentNodeValues.remove(node);
   }

   @Override
   public void addPredecessor(N node, V value) {
      V unused = this.addSuccessor(node, value);
   }

   @Override
   public @Nullable V addSuccessor(N node, V value) {
      return this.adjacentNodeValues.put(node, value);
   }
}
