package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use GraphBuilder to create a real instance")
@Beta
public interface Graph<N> extends BaseGraph<N> {
   @Override
   Set<N> nodes();

   @Override
   Set<EndpointPair<N>> edges();

   @Override
   boolean isDirected();

   @Override
   boolean allowsSelfLoops();

   @Override
   ElementOrder<N> nodeOrder();

   @Override
   ElementOrder<N> incidentEdgeOrder();

   @Override
   Set<N> adjacentNodes(N node);

   @Override
   Set<N> predecessors(N node);

   @Override
   Set<N> successors(N node);

   @Override
   Set<EndpointPair<N>> incidentEdges(N node);

   @Override
   int degree(N node);

   @Override
   int inDegree(N node);

   @Override
   int outDegree(N node);

   @Override
   boolean hasEdgeConnecting(N nodeU, N nodeV);

   @Override
   boolean hasEdgeConnecting(EndpointPair<N> endpoints);

   @Override
   boolean equals(@Nullable Object object);

   @Override
   int hashCode();
}
