package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.Iterables;
import com.dfsek.terra.lib.google.common.collect.Iterators;
import com.dfsek.terra.lib.google.common.collect.Sets;
import com.dfsek.terra.lib.google.common.collect.UnmodifiableIterator;
import com.dfsek.terra.lib.google.common.math.IntMath;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

abstract class AbstractDirectedNetworkConnections<N, E> implements NetworkConnections<N, E> {
   final Map<E, N> inEdgeMap;
   final Map<E, N> outEdgeMap;
   private int selfLoopCount;

   AbstractDirectedNetworkConnections(Map<E, N> inEdgeMap, Map<E, N> outEdgeMap, int selfLoopCount) {
      this.inEdgeMap = Preconditions.checkNotNull(inEdgeMap);
      this.outEdgeMap = Preconditions.checkNotNull(outEdgeMap);
      this.selfLoopCount = Graphs.checkNonNegative(selfLoopCount);
      Preconditions.checkState(selfLoopCount <= inEdgeMap.size() && selfLoopCount <= outEdgeMap.size());
   }

   @Override
   public Set<N> adjacentNodes() {
      return Sets.union(this.predecessors(), this.successors());
   }

   @Override
   public Set<E> incidentEdges() {
      return new AbstractSet<E>() {
         public UnmodifiableIterator<E> iterator() {
            Iterable<E> incidentEdges = AbstractDirectedNetworkConnections.this.selfLoopCount == 0
               ? Iterables.concat(AbstractDirectedNetworkConnections.this.inEdgeMap.keySet(), AbstractDirectedNetworkConnections.this.outEdgeMap.keySet())
               : Sets.union(AbstractDirectedNetworkConnections.this.inEdgeMap.keySet(), AbstractDirectedNetworkConnections.this.outEdgeMap.keySet());
            return Iterators.unmodifiableIterator(incidentEdges.iterator());
         }

         @Override
         public int size() {
            return IntMath.saturatedAdd(
               AbstractDirectedNetworkConnections.this.inEdgeMap.size(),
               AbstractDirectedNetworkConnections.this.outEdgeMap.size() - AbstractDirectedNetworkConnections.this.selfLoopCount
            );
         }

         @Override
         public boolean contains(@Nullable Object obj) {
            return AbstractDirectedNetworkConnections.this.inEdgeMap.containsKey(obj) || AbstractDirectedNetworkConnections.this.outEdgeMap.containsKey(obj);
         }
      };
   }

   @Override
   public Set<E> inEdges() {
      return Collections.unmodifiableSet(this.inEdgeMap.keySet());
   }

   @Override
   public Set<E> outEdges() {
      return Collections.unmodifiableSet(this.outEdgeMap.keySet());
   }

   @Override
   public N adjacentNode(E edge) {
      return Objects.requireNonNull(this.outEdgeMap.get(edge));
   }

   @Override
   public N removeInEdge(E edge, boolean isSelfLoop) {
      if (isSelfLoop) {
         Graphs.checkNonNegative(--this.selfLoopCount);
      }

      N previousNode = this.inEdgeMap.remove(edge);
      return Objects.requireNonNull(previousNode);
   }

   @Override
   public N removeOutEdge(E edge) {
      N previousNode = this.outEdgeMap.remove(edge);
      return Objects.requireNonNull(previousNode);
   }

   @Override
   public void addInEdge(E edge, N node, boolean isSelfLoop) {
      Preconditions.checkNotNull(edge);
      Preconditions.checkNotNull(node);
      if (isSelfLoop) {
         Graphs.checkPositive(++this.selfLoopCount);
      }

      N previousNode = this.inEdgeMap.put(edge, node);
      Preconditions.checkState(previousNode == null);
   }

   @Override
   public void addOutEdge(E edge, N node) {
      Preconditions.checkNotNull(edge);
      Preconditions.checkNotNull(node);
      N previousNode = this.outEdgeMap.put(edge, node);
      Preconditions.checkState(previousNode == null);
   }
}
