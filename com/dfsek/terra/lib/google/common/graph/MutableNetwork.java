package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
public interface MutableNetwork<N, E> extends Network<N, E> {
   @CanIgnoreReturnValue
   boolean addNode(N node);

   @CanIgnoreReturnValue
   boolean addEdge(N nodeU, N nodeV, E edge);

   @CanIgnoreReturnValue
   boolean addEdge(EndpointPair<N> endpoints, E edge);

   @CanIgnoreReturnValue
   boolean removeNode(N node);

   @CanIgnoreReturnValue
   boolean removeEdge(E edge);
}
