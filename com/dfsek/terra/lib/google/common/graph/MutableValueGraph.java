package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jspecify.annotations.Nullable;

@Beta
public interface MutableValueGraph<N, V> extends ValueGraph<N, V> {
   @CanIgnoreReturnValue
   boolean addNode(N node);

   @CanIgnoreReturnValue
   @Nullable V putEdgeValue(N nodeU, N nodeV, V value);

   @CanIgnoreReturnValue
   @Nullable V putEdgeValue(EndpointPair<N> endpoints, V value);

   @CanIgnoreReturnValue
   boolean removeNode(N node);

   @CanIgnoreReturnValue
   @Nullable V removeEdge(N nodeU, N nodeV);

   @CanIgnoreReturnValue
   @Nullable V removeEdge(EndpointPair<N> endpoints);
}
