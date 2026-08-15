package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.AbstractIterator;
import com.dfsek.terra.lib.google.common.collect.ImmutableSet;
import com.dfsek.terra.lib.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

abstract class EndpointPairIterator<N> extends AbstractIterator<EndpointPair<N>> {
   private final BaseGraph<N> graph;
   private final Iterator<N> nodeIterator;
   @Nullable N node = (N)null;
   Iterator<N> successorIterator = ImmutableSet.<N>of().iterator();

   static <N> EndpointPairIterator<N> of(BaseGraph<N> graph) {
      return graph.isDirected() ? new EndpointPairIterator.Directed<>(graph) : new EndpointPairIterator.Undirected<>(graph);
   }

   private EndpointPairIterator(BaseGraph<N> graph) {
      this.graph = graph;
      this.nodeIterator = graph.nodes().iterator();
   }

   final boolean advance() {
      Preconditions.checkState(!this.successorIterator.hasNext());
      if (!this.nodeIterator.hasNext()) {
         return false;
      }

      this.node = this.nodeIterator.next();
      this.successorIterator = this.graph.successors(this.node).iterator();
      return true;
   }

   private static final class Directed<N> extends EndpointPairIterator<N> {
      private Directed(BaseGraph<N> graph) {
         super(graph);
      }

      protected @Nullable EndpointPair<N> computeNext() {
         while (!this.successorIterator.hasNext()) {
            if (!this.advance()) {
               return this.endOfData();
            }
         }

         return EndpointPair.ordered(Objects.requireNonNull(this.node), this.successorIterator.next());
      }
   }

   private static final class Undirected<N> extends EndpointPairIterator<N> {
      private @Nullable Set<@Nullable N> visitedNodes;

      private Undirected(BaseGraph<N> graph) {
         super(graph);
         this.visitedNodes = Sets.newHashSetWithExpectedSize(graph.nodes().size() + 1);
      }

      protected @Nullable EndpointPair<N> computeNext() {
         do {
            Objects.requireNonNull(this.visitedNodes);

            while (this.successorIterator.hasNext()) {
               N otherNode = this.successorIterator.next();
               if (!this.visitedNodes.contains(otherNode)) {
                  return EndpointPair.unordered(Objects.requireNonNull(this.node), otherNode);
               }
            }

            this.visitedNodes.add(this.node);
         } while (this.advance());

         this.visitedNodes = null;
         return this.endOfData();
      }
   }
}
