package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jspecify.annotations.Nullable;

class StandardValueGraph<N, V> extends AbstractValueGraph<N, V> {
   private final boolean isDirected;
   private final boolean allowsSelfLoops;
   private final ElementOrder<N> nodeOrder;
   final MapIteratorCache<N, GraphConnections<N, V>> nodeConnections;
   long edgeCount;

   StandardValueGraph(AbstractGraphBuilder<? super N> builder) {
      this(builder, builder.nodeOrder.createMap(builder.expectedNodeCount.or(10)), 0L);
   }

   StandardValueGraph(AbstractGraphBuilder<? super N> builder, Map<N, GraphConnections<N, V>> nodeConnections, long edgeCount) {
      this.isDirected = builder.directed;
      this.allowsSelfLoops = builder.allowsSelfLoops;
      this.nodeOrder = builder.nodeOrder.cast();
      this.nodeConnections = nodeConnections instanceof TreeMap ? new MapRetrievalCache<>(nodeConnections) : new MapIteratorCache<>(nodeConnections);
      this.edgeCount = Graphs.checkNonNegative(edgeCount);
   }

   @Override
   public Set<N> nodes() {
      return this.nodeConnections.unmodifiableKeySet();
   }

   @Override
   public boolean isDirected() {
      return this.isDirected;
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
   public Set<N> adjacentNodes(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).adjacentNodes(), node);
   }

   @Override
   public Set<N> predecessors(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).predecessors(), node);
   }

   @Override
   public Set<N> successors(N node) {
      return this.nodeInvalidatableSet(this.checkedConnections(node).successors(), node);
   }

   @Override
   public Set<EndpointPair<N>> incidentEdges(N node) {
      final GraphConnections<N, V> connections = this.checkedConnections(node);
      IncidentEdgeSet<N> incident = new IncidentEdgeSet<N>(this, node) {
         @Override
         public Iterator<EndpointPair<N>> iterator() {
            return connections.incidentEdgeIterator(this.node);
         }
      };
      return this.nodeInvalidatableSet(incident, node);
   }

   @Override
   public boolean hasEdgeConnecting(N nodeU, N nodeV) {
      return this.hasEdgeConnectingInternal(Preconditions.checkNotNull(nodeU), Preconditions.checkNotNull(nodeV));
   }

   @Override
   public boolean hasEdgeConnecting(EndpointPair<N> endpoints) {
      Preconditions.checkNotNull(endpoints);
      return this.isOrderingCompatible(endpoints) && this.hasEdgeConnectingInternal(endpoints.nodeU(), endpoints.nodeV());
   }

   @Override
   public @Nullable V edgeValueOrDefault(N nodeU, N nodeV, @Nullable V defaultValue) {
      return this.edgeValueOrDefaultInternal(Preconditions.checkNotNull(nodeU), Preconditions.checkNotNull(nodeV), defaultValue);
   }

   @Override
   public @Nullable V edgeValueOrDefault(EndpointPair<N> endpoints, @Nullable V defaultValue) {
      this.validateEndpoints(endpoints);
      return this.edgeValueOrDefaultInternal(endpoints.nodeU(), endpoints.nodeV(), defaultValue);
   }

   @Override
   protected long edgeCount() {
      return this.edgeCount;
   }

   private final GraphConnections<N, V> checkedConnections(N node) {
      GraphConnections<N, V> connections = this.nodeConnections.get(node);
      if (connections == null) {
         Preconditions.checkNotNull(node);
         throw new IllegalArgumentException("Node " + node + " is not an element of this graph.");
      } else {
         return connections;
      }
   }

   final boolean containsNode(@Nullable N node) {
      return this.nodeConnections.containsKey(node);
   }

   private final boolean hasEdgeConnectingInternal(N nodeU, N nodeV) {
      GraphConnections<N, V> connectionsU = this.nodeConnections.get(nodeU);
      return connectionsU != null && connectionsU.successors().contains(nodeV);
   }

   private final @Nullable V edgeValueOrDefaultInternal(N nodeU, N nodeV, @Nullable V defaultValue) {
      GraphConnections<N, V> connectionsU = this.nodeConnections.get(nodeU);
      V value = connectionsU == null ? null : connectionsU.value(nodeV);
      return value == null ? defaultValue : value;
   }
}
