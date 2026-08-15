package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableSet;
import com.dfsek.terra.lib.google.common.collect.Iterators;
import com.dfsek.terra.lib.google.common.collect.Sets;
import com.dfsek.terra.lib.google.common.collect.UnmodifiableIterator;
import com.dfsek.terra.lib.google.common.math.IntMath;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import java.util.AbstractSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

abstract class AbstractBaseGraph<N> implements BaseGraph<N> {
   protected long edgeCount() {
      long degreeSum = 0L;

      for (N node : this.nodes()) {
         degreeSum += this.degree(node);
      }

      Preconditions.checkState((degreeSum & 1L) == 0L);
      return degreeSum >>> 1;
   }

   @Override
   public Set<EndpointPair<N>> edges() {
      return new AbstractSet<EndpointPair<N>>() {
         public UnmodifiableIterator<EndpointPair<N>> iterator() {
            return EndpointPairIterator.of(AbstractBaseGraph.this);
         }

         @Override
         public int size() {
            return Ints.saturatedCast(AbstractBaseGraph.this.edgeCount());
         }

         @Override
         public boolean remove(@Nullable Object o) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean contains(@Nullable Object obj) {
            if (!(obj instanceof EndpointPair)) {
               return false;
            }

            EndpointPair<?> endpointPair = (EndpointPair<?>)obj;
            return AbstractBaseGraph.this.isOrderingCompatible(endpointPair)
               && AbstractBaseGraph.this.nodes().contains(endpointPair.nodeU())
               && AbstractBaseGraph.this.successors(endpointPair.nodeU()).contains(endpointPair.nodeV());
         }
      };
   }

   @Override
   public ElementOrder<N> incidentEdgeOrder() {
      return ElementOrder.unordered();
   }

   @Override
   public Set<EndpointPair<N>> incidentEdges(N node) {
      Preconditions.checkNotNull(node);
      Preconditions.checkArgument(this.nodes().contains(node), "Node %s is not an element of this graph.", node);
      IncidentEdgeSet<N> incident = new IncidentEdgeSet<N>(this, node) {
         public UnmodifiableIterator<EndpointPair<N>> iterator() {
            return this.graph.isDirected()
               ? Iterators.unmodifiableIterator(
                  Iterators.concat(
                     Iterators.transform(this.graph.predecessors(this.node).iterator(), predecessor -> EndpointPair.ordered((N)predecessor, this.node)),
                     Iterators.transform(
                        Sets.difference(this.graph.successors(this.node), ImmutableSet.of(this.node)).iterator(),
                        successor -> EndpointPair.ordered(this.node, (N)successor)
                     )
                  )
               )
               : Iterators.unmodifiableIterator(
                  Iterators.transform(this.graph.adjacentNodes(this.node).iterator(), adjacentNode -> EndpointPair.unordered(this.node, (N)adjacentNode))
               );
         }
      };
      return this.nodeInvalidatableSet(incident, node);
   }

   @Override
   public int degree(N node) {
      if (this.isDirected()) {
         return IntMath.saturatedAdd(this.predecessors(node).size(), this.successors(node).size());
      }

      Set<N> neighbors = this.adjacentNodes(node);
      int selfLoopCount = this.allowsSelfLoops() && neighbors.contains(node) ? 1 : 0;
      return IntMath.saturatedAdd(neighbors.size(), selfLoopCount);
   }

   @Override
   public int inDegree(N node) {
      return this.isDirected() ? this.predecessors(node).size() : this.degree(node);
   }

   @Override
   public int outDegree(N node) {
      return this.isDirected() ? this.successors(node).size() : this.degree(node);
   }

   @Override
   public boolean hasEdgeConnecting(N nodeU, N nodeV) {
      Preconditions.checkNotNull(nodeU);
      Preconditions.checkNotNull(nodeV);
      return this.nodes().contains(nodeU) && this.successors(nodeU).contains(nodeV);
   }

   @Override
   public boolean hasEdgeConnecting(EndpointPair<N> endpoints) {
      Preconditions.checkNotNull(endpoints);
      if (!this.isOrderingCompatible(endpoints)) {
         return false;
      }

      N nodeU = endpoints.nodeU();
      N nodeV = endpoints.nodeV();
      return this.nodes().contains(nodeU) && this.successors(nodeU).contains(nodeV);
   }

   protected final void validateEndpoints(EndpointPair<?> endpoints) {
      Preconditions.checkNotNull(endpoints);
      Preconditions.checkArgument(this.isOrderingCompatible(endpoints), "Mismatch: endpoints' ordering is not compatible with directionality of the graph");
   }

   protected final boolean isOrderingCompatible(EndpointPair<?> endpoints) {
      return endpoints.isOrdered() == this.isDirected();
   }

   protected final <T> Set<T> nodeInvalidatableSet(Set<T> set, N node) {
      return InvalidatableSet.of(
         set, () -> this.nodes().contains(node), () -> String.format("Node %s that was used to generate this set is no longer in the graph.", node)
      );
   }

   protected final <T> Set<T> nodePairInvalidatableSet(Set<T> set, N nodeU, N nodeV) {
      return InvalidatableSet.of(
         set,
         () -> this.nodes().contains(nodeU) && this.nodes().contains(nodeV),
         () -> String.format("Node %s or node %s that were used to generate this set are no longer in the graph.", nodeU, nodeV)
      );
   }
}
