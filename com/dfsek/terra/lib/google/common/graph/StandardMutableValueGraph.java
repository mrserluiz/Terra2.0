package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class StandardMutableValueGraph<N, V> extends StandardValueGraph<N, V> implements MutableValueGraph<N, V> {
   private final ElementOrder<N> incidentEdgeOrder;

   StandardMutableValueGraph(AbstractGraphBuilder<? super N> builder) {
      super(builder);
      this.incidentEdgeOrder = builder.incidentEdgeOrder.cast();
   }

   @Override
   public ElementOrder<N> incidentEdgeOrder() {
      return this.incidentEdgeOrder;
   }

   @CanIgnoreReturnValue
   @Override
   public boolean addNode(N node) {
      Preconditions.checkNotNull(node, "node");
      if (this.containsNode(node)) {
         return false;
      }

      this.addNodeInternal(node);
      return true;
   }

   @CanIgnoreReturnValue
   private GraphConnections<N, V> addNodeInternal(N node) {
      GraphConnections<N, V> connections = this.newConnections();
      Preconditions.checkState(this.nodeConnections.put(node, connections) == null);
      return connections;
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V putEdgeValue(N nodeU, N nodeV, V value) {
      Preconditions.checkNotNull(nodeU, "nodeU");
      Preconditions.checkNotNull(nodeV, "nodeV");
      Preconditions.checkNotNull(value, "value");
      if (!this.allowsSelfLoops()) {
         Preconditions.checkArgument(
            !nodeU.equals(nodeV),
            "Cannot add self-loop edge on node %s, as self-loops are not allowed. To construct a graph that allows self-loops, call allowsSelfLoops(true) on the Builder.",
            nodeU
         );
      }

      GraphConnections<N, V> connectionsU = this.nodeConnections.get(nodeU);
      if (connectionsU == null) {
         connectionsU = this.addNodeInternal(nodeU);
      }

      V previousValue = connectionsU.addSuccessor(nodeV, value);
      GraphConnections<N, V> connectionsV = this.nodeConnections.get(nodeV);
      if (connectionsV == null) {
         connectionsV = this.addNodeInternal(nodeV);
      }

      connectionsV.addPredecessor(nodeU, value);
      if (previousValue == null) {
         Graphs.checkPositive(++this.edgeCount);
      }

      return previousValue;
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V putEdgeValue(EndpointPair<N> endpoints, V value) {
      this.validateEndpoints(endpoints);
      return this.putEdgeValue(endpoints.nodeU(), endpoints.nodeV(), value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean removeNode(N node) {
      Preconditions.checkNotNull(node, "node");
      GraphConnections<N, V> connections = this.nodeConnections.get(node);
      if (connections == null) {
         return false;
      }

      if (this.allowsSelfLoops() && connections.removeSuccessor(node) != null) {
         connections.removePredecessor(node);
         this.edgeCount--;
      }

      for (N successor : ImmutableList.copyOf(connections.successors())) {
         Objects.requireNonNull(this.nodeConnections.getWithoutCaching(successor)).removePredecessor(node);
         Objects.requireNonNull(connections.removeSuccessor(successor));
         this.edgeCount--;
      }

      if (this.isDirected()) {
         for (N predecessor : ImmutableList.copyOf(connections.predecessors())) {
            Preconditions.checkState(Objects.requireNonNull(this.nodeConnections.getWithoutCaching(predecessor)).removeSuccessor(node) != null);
            connections.removePredecessor(predecessor);
            this.edgeCount--;
         }
      }

      this.nodeConnections.remove(node);
      Graphs.checkNonNegative(this.edgeCount);
      return true;
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V removeEdge(N nodeU, N nodeV) {
      Preconditions.checkNotNull(nodeU, "nodeU");
      Preconditions.checkNotNull(nodeV, "nodeV");
      GraphConnections<N, V> connectionsU = this.nodeConnections.get(nodeU);
      GraphConnections<N, V> connectionsV = this.nodeConnections.get(nodeV);
      if (connectionsU != null && connectionsV != null) {
         V previousValue = connectionsU.removeSuccessor(nodeV);
         if (previousValue != null) {
            connectionsV.removePredecessor(nodeU);
            Graphs.checkNonNegative(--this.edgeCount);
         }

         return previousValue;
      } else {
         return null;
      }
   }

   @CanIgnoreReturnValue
   @Override
   public @Nullable V removeEdge(EndpointPair<N> endpoints) {
      this.validateEndpoints(endpoints);
      return this.removeEdge(endpoints.nodeU(), endpoints.nodeV());
   }

   private GraphConnections<N, V> newConnections() {
      return this.isDirected() ? DirectedGraphConnections.of(this.incidentEdgeOrder) : UndirectedGraphConnections.of(this.incidentEdgeOrder);
   }
}
