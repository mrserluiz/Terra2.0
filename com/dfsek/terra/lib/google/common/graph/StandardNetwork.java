package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

class StandardNetwork<N, E> extends AbstractNetwork<N, E> {
   private final boolean isDirected;
   private final boolean allowsParallelEdges;
   private final boolean allowsSelfLoops;
   private final ElementOrder<N> nodeOrder;
   private final ElementOrder<E> edgeOrder;
   final MapIteratorCache<N, NetworkConnections<N, E>> nodeConnections;
   final MapIteratorCache<E, N> edgeToReferenceNode;

   StandardNetwork(NetworkBuilder<? super N, ? super E> builder) {
      this(builder, builder.nodeOrder.createMap(builder.expectedNodeCount.or(10)), builder.edgeOrder.createMap(builder.expectedEdgeCount.or(20)));
   }

   StandardNetwork(NetworkBuilder<? super N, ? super E> builder, Map<N, NetworkConnections<N, E>> nodeConnections, Map<E, N> edgeToReferenceNode) {
      this.isDirected = builder.directed;
      this.allowsParallelEdges = builder.allowsParallelEdges;
      this.allowsSelfLoops = builder.allowsSelfLoops;
      this.nodeOrder = builder.nodeOrder.cast();
      this.edgeOrder = builder.edgeOrder.cast();
      this.nodeConnections = nodeConnections instanceof TreeMap ? new MapRetrievalCache<>(nodeConnections) : new MapIteratorCache<>(nodeConnections);
      this.edgeToReferenceNode = new MapIteratorCache<>(edgeToReferenceNode);
   }

   @Override
   public Set<N> nodes() {
      return this.nodeConnections.unmodifiableKeySet();
   }

   @Override
   public Set<E> edges() {
      return this.edgeToReferenceNode.unmodifiableKeySet();
   }

   @Override
   public boolean isDirected() {
      return this.isDirected;
   }

   @Override
   public boolean allowsParallelEdges() {
      return this.allowsParallelEdges;
   }

   @Override
   public boolean allowsSelfLoops() {
      return this.allowsSelfLoops;
   }

   @Override
   public ElementOrder<N> nodeOrder() {
      return this.nodeOrder;
   }

   @Override
   public ElementOrder<E> edgeOrder() {
      return this.edgeOrder;
   }

   @Override
   public Set<E> incidentEdges(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).incidentEdges(), node);
   }

   @Override
   public EndpointPair<N> incidentNodes(E edge) {
      N nodeU = this.checkedReferenceNode(edge);
      N nodeV = Objects.requireNonNull(this.nodeConnections.get(nodeU)).adjacentNode(edge);
      return EndpointPair.of(this, nodeU, nodeV);
   }

   @Override
   public Set<N> adjacentNodes(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).adjacentNodes(), node);
   }

   @Override
   public Set<E> edgesConnecting(N nodeU, N nodeV) {
      NetworkConnections<N, E> connectionsU = this.checkedConnections(nodeU);
      if (!this.allowsSelfLoops && nodeU == nodeV) {
         return ImmutableSet.of();
      }

      Preconditions.checkArgument(this.containsNode(nodeV), "Node %s is not an element of this graph.", nodeV);
      return this.nodePairInvalidatableSet(connectionsU.edgesConnecting(nodeV), nodeU, nodeV);
   }

   @Override
   public Set<E> inEdges(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).inEdges(), node);
   }

   @Override
   public Set<E> outEdges(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).outEdges(), node);
   }

   @Override
   public Set<N> predecessors(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).predecessors(), node);
   }

   @Override
   public Set<N> successors(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).successors(), node);
   }

   final NetworkConnections<N, E> checkedConnections(N node) {
      NetworkConnections<N, E> connections = this.nodeConnections.get(node);
      if (connections == null) {
         Preconditions.checkNotNull(node);
         throw new IllegalArgumentException(String.format("Node %s is not an element of this graph.", node));
      } else {
         return connections;
      }
   }

   final N checkedReferenceNode(E edge) {
      N referenceNode = this.edgeToReferenceNode.get(edge);
      if (referenceNode == null) {
         Preconditions.checkNotNull(edge);
         throw new IllegalArgumentException(String.format("Edge %s is not an element of this graph.", edge));
      } else {
         return referenceNode;
      }
   }

   final boolean containsNode(N node) {
      return this.nodeConnections.containsKey(node);
   }

   final boolean containsEdge(E edge) {
      return this.edgeToReferenceNode.containsKey(edge);
   }
}
