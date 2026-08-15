package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

@Beta
public interface MutableGraph<N> extends Graph<N> {
   @CanIgnoreReturnValue
   boolean addNode(N node);

   @CanIgnoreReturnValue
   boolean putEdge(N nodeU, N nodeV);

   @CanIgnoreReturnValue
   boolean putEdge(EndpointPair<N> endpoints);

   @CanIgnoreReturnValue
   boolean removeNode(N node);

   @CanIgnoreReturnValue
   boolean removeEdge(N nodeU, N nodeV);

   @CanIgnoreReturnValue
   boolean removeEdge(EndpointPair<N> endpoints);
}
