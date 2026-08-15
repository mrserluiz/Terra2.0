package com.dfsek.terra.lib.google.common.graph;

final class StandardMutableGraph<N> extends ForwardingGraph<N> implements MutableGraph<N> {
   private final MutableValueGraph<N, GraphConstants.Presence> backingValueGraph;

   StandardMutableGraph(AbstractGraphBuilder<? super N> builder) {
      this.backingValueGraph = new StandardMutableValueGraph<>(builder);
   }

   @Override
   BaseGraph<N> delegate() {
      return this.backingValueGraph;
   }

   @Override
   public boolean addNode(N node) {
      return this.backingValueGraph.addNode(node);
   }

   @Override
   public boolean putEdge(N nodeU, N nodeV) {
      return this.backingValueGraph.putEdgeValue(nodeU, nodeV, GraphConstants.Presence.EDGE_EXISTS) == null;
   }

   @Override
   public boolean putEdge(EndpointPair<N> endpoints) {
      this.validateEndpoints(endpoints);
      return this.putEdge(endpoints.nodeU(), endpoints.nodeV());
   }

   @Override
   public boolean removeNode(N node) {
      return this.backingValueGraph.removeNode(node);
   }

   @Override
   public boolean removeEdge(N nodeU, N nodeV) {
      return this.backingValueGraph.removeEdge(nodeU, nodeV) != null;
   }

   @Override
   public boolean removeEdge(EndpointPair<N> endpoints) {
      this.validateEndpoints(endpoints);
      return this.removeEdge(endpoints.nodeU(), endpoints.nodeV());
   }
}
