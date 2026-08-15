package com.dfsek.terra.lib.google.common.graph;

import java.util.Set;

interface BaseGraph<N> extends SuccessorsFunction<N>, PredecessorsFunction<N> {
   Set<N> nodes();

   Set<EndpointPair<N>> edges();

   boolean isDirected();

   boolean allowsSelfLoops();

   ElementOrder<N> nodeOrder();

   ElementOrder<N> incidentEdgeOrder();

   Set<N> adjacentNodes(N node);

   Set<N> predecessors(N node);

   Set<N> successors(N node);

   Set<EndpointPair<N>> incidentEdges(N node);

   int degree(N node);

   int inDegree(N node);

   int outDegree(N node);

   boolean hasEdgeConnecting(N nodeU, N nodeV);

   boolean hasEdgeConnecting(EndpointPair<N> endpoints);
}
