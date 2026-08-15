package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.dfsek.terra.lib.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.InlineMe;
import java.util.Objects;

@Immutable(containerOf = {"N", "V"})
@Beta
public final class ImmutableValueGraph<N, V> extends StandardValueGraph<N, V> {
   private ImmutableValueGraph(ValueGraph<N, V> graph) {
      super(ValueGraphBuilder.from(graph), getNodeConnections(graph), graph.edges().size());
   }

   public static <N, V> ImmutableValueGraph<N, V> copyOf(ValueGraph<N, V> graph) {
      return graph instanceof ImmutableValueGraph ? (ImmutableValueGraph)graph : new ImmutableValueGraph<>(graph);
   }

   @Deprecated
   @InlineMe(replacement = "checkNotNull(graph)", staticImports = "com.dfsek.terra.lib.google.common.base.Preconditions.checkNotNull")
   public static <N, V> ImmutableValueGraph<N, V> copyOf(ImmutableValueGraph<N, V> graph) {
      return Preconditions.checkNotNull(graph);
   }

   @Override
   public ElementOrder<N> incidentEdgeOrder() {
      return ElementOrder.stable();
   }

   public ImmutableGraph<N> asGraph() {
      return new ImmutableGraph<>(this);
   }

   private static <N, V> ImmutableMap<N, GraphConnections<N, V>> getNodeConnections(ValueGraph<N, V> graph) {
      ImmutableMap.Builder<N, GraphConnections<N, V>> nodeConnections = ImmutableMap.builder();

      for (N node : graph.nodes()) {
         nodeConnections.put(node, connectionsOf(graph, node));
      }

      return nodeConnections.buildOrThrow();
   }

   private static <N, V> GraphConnections<N, V> connectionsOf(ValueGraph<N, V> graph, N node) {
      Function<N, V> successorNodeToValueFn = successorNode -> Objects.requireNonNull(graph.edgeValueOrDefault(node, successorNode, null));
      return graph.isDirected()
         ? DirectedGraphConnections.ofImmutable(node, graph.incidentEdges(node), successorNodeToValueFn)
         : UndirectedGraphConnections.ofImmutable(Maps.asMap(graph.adjacentNodes(node), successorNodeToValueFn));
   }

   public static class Builder<N, V> {
      private final MutableValueGraph<N, V> mutableValueGraph;

      Builder(ValueGraphBuilder<N, V> graphBuilder) {
         this.mutableValueGraph = graphBuilder.copy().incidentEdgeOrder(ElementOrder.stable()).build();
      }

      @CanIgnoreReturnValue
      public ImmutableValueGraph.Builder<N, V> addNode(N node) {
         this.mutableValueGraph.addNode(node);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableValueGraph.Builder<N, V> putEdgeValue(N nodeU, N nodeV, V value) {
         this.mutableValueGraph.putEdgeValue(nodeU, nodeV, value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableValueGraph.Builder<N, V> putEdgeValue(EndpointPair<N> endpoints, V value) {
         this.mutableValueGraph.putEdgeValue(endpoints, value);
         return this;
      }

      public ImmutableValueGraph<N, V> build() {
         return ImmutableValueGraph.copyOf(this.mutableValueGraph);
      }
   }
}
