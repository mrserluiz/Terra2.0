package com.dfsek.terra.lib.google.common.graph;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Iterator;
import java.util.Set;
import org.jspecify.annotations.Nullable;

interface GraphConnections<N, V> {
   Set<N> adjacentNodes();

   Set<N> predecessors();

   Set<N> successors();

   Iterator<EndpointPair<N>> incidentEdgeIterator(N thisNode);

   @Nullable V value(N node);

   void removePredecessor(N node);

   @CanIgnoreReturnValue
   @Nullable V removeSuccessor(N node);

   void addPredecessor(N node, V value);

   @CanIgnoreReturnValue
   @Nullable V addSuccessor(N node, V value);
}
