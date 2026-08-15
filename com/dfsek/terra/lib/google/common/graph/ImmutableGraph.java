package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Functions;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.dfsek.terra.lib.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.InlineMe;

@Immutable(containerOf = "N")
@Beta
public class ImmutableGraph<N> extends ForwardingGraph<N> {
   private final BaseGraph<N> backingGraph;

   ImmutableGraph(BaseGraph<N> backingGraph) {
      this.backingGraph = backingGraph;
   }

   public static <N> ImmutableGraph<N> copyOf(Graph<N> graph) {
      return graph instanceof ImmutableGraph
         ? (ImmutableGraph)graph
         : new ImmutableGraph<>(new StandardValueGraph<>(GraphBuilder.from(graph), getNodeConnections(graph), graph.edges().size()));
   }

   @Deprecated
   @InlineMe(replacement = "checkNotNull(graph)", staticImports = "com.dfsek.terra.lib.google.common.base.Preconditions.checkNotNull")
   public static <N> ImmutableGraph<N> copyOf(ImmutableGraph<N> graph) {
      return Preconditions.checkNotNull(graph);
   }

   @Override
   public ElementOrder<N> incidentEdgeOrder() {
      return ElementOrder.stable();
   }

   private static <N> ImmutableMap<N, GraphConnections<N, GraphConstants.Presence>> getNodeConnections(Graph<N> graph) {
      ImmutableMap.Builder<N, GraphConnections<N, GraphConstants.Presence>> nodeConnections = ImmutableMap.builder();

      for (N node : graph.nodes()) {
         nodeConnections.put(node, connectionsOf(graph, node));
      }

      return nodeConnections.buildOrThrow();
   }

   private static <N> GraphConnections<N, GraphConstants.Presence> connectionsOf(Graph<N> graph, N node) {
      Function<N, GraphConstants.Presence> edgeValueFn = (Function<N, GraphConstants.Presence>)Functions.constant(GraphConstants.Presence.EDGE_EXISTS);
      return graph.isDirected()
         ? DirectedGraphConnections.ofImmutable(node, graph.incidentEdges(node), edgeValueFn)
         : UndirectedGraphConnections.ofImmutable(Maps.asMap(graph.adjacentNodes(node), edgeValueFn));
   }

   @Override
   BaseGraph<N> delegate() {
      return this.backingGraph;
   }

   public static class Builder<N> {
      private final MutableGraph<N> mutableGraph;

      Builder(GraphBuilder<N> graphBuilder) {
         this.mutableGraph = graphBuilder.copy().incidentEdgeOrder(ElementOrder.stable()).build();
      }

      @CanIgnoreReturnValue
      public ImmutableGraph.Builder<N> addNode(N node) {
         this.mutableGraph.addNode(node);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableGraph.Builder<N> putEdge(N nodeU, N nodeV) {
         this.mutableGraph.putEdge(nodeU, nodeV);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableGraph.Builder<N> putEdge(EndpointPair<N> endpoints) {
         this.mutableGraph.putEdge(endpoints);
         return this;
      }

      public ImmutableGraph<N> build() {
         return ImmutableGraph.copyOf(this.mutableGraph);
      }
   }
}
