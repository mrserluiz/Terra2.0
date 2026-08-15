package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use NetworkBuilder to create a real instance")
@Beta
public interface Network<N, E> extends SuccessorsFunction<N>, PredecessorsFunction<N> {
   Set<N> nodes();

   Set<E> edges();

   Graph<N> asGraph();

   boolean isDirected();

   boolean allowsParallelEdges();

   boolean allowsSelfLoops();

   ElementOrder<N> nodeOrder();

   ElementOrder<E> edgeOrder();

   Set<N> adjacentNodes(N node);

   Set<N> predecessors(N node);

   Set<N> successors(N node);

   Set<E> incidentEdges(N node);

   Set<E> inEdges(N node);

   Set<E> outEdges(N node);

   int degree(N node);

   int inDegree(N node);

   int outDegree(N node);

   EndpointPair<N> incidentNodes(E edge);

   Set<E> adjacentEdges(E edge);

   Set<E> edgesConnecting(N nodeU, N nodeV);

   Set<E> edgesConnecting(EndpointPair<N> endpoints);

   Optional<E> edgeConnecting(N nodeU, N nodeV);

   Optional<E> edgeConnecting(EndpointPair<N> endpoints);

   @Nullable E edgeConnectingOrNull(N nodeU, N nodeV);

   @Nullable E edgeConnectingOrNull(EndpointPair<N> endpoints);

   boolean hasEdgeConnecting(N nodeU, N nodeV);

   boolean hasEdgeConnecting(EndpointPair<N> endpoints);

   @Override
   boolean equals(@Nullable Object object);

   @Override
   int hashCode();
}
