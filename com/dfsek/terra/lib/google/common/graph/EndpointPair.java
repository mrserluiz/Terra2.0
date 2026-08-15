package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.Iterators;
import com.dfsek.terra.lib.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.annotations.Immutable;
import org.jspecify.annotations.Nullable;

@Immutable(containerOf = "N")
@Beta
public abstract class EndpointPair<N> implements Iterable<N> {
   private final N nodeU;
   private final N nodeV;

   private EndpointPair(N nodeU, N nodeV) {
      this.nodeU = Preconditions.checkNotNull(nodeU);
      this.nodeV = Preconditions.checkNotNull(nodeV);
   }

   public static <N> EndpointPair<N> ordered(N source, N target) {
      return new EndpointPair.Ordered<>(source, target);
   }

   public static <N> EndpointPair<N> unordered(N nodeU, N nodeV) {
      return new EndpointPair.Unordered<>(nodeV, nodeU);
   }

   static <N> EndpointPair<N> of(Graph<?> graph, N nodeU, N nodeV) {
      return graph.isDirected() ? ordered(nodeU, nodeV) : unordered(nodeU, nodeV);
   }

   static <N> EndpointPair<N> of(Network<?, ?> network, N nodeU, N nodeV) {
      return network.isDirected() ? ordered(nodeU, nodeV) : unordered(nodeU, nodeV);
   }

   public abstract N source();

   public abstract N target();

   public final N nodeU() {
      return this.nodeU;
   }

   public final N nodeV() {
      return this.nodeV;
   }

   public final N adjacentNode(N node) {
      if (node.equals(this.nodeU)) {
         return this.nodeV;
      } else if (node.equals(this.nodeV)) {
         return this.nodeU;
      } else {
         throw new IllegalArgumentException("EndpointPair " + this + " does not contain node " + node);
      }
   }

   public abstract boolean isOrdered();

   public final UnmodifiableIterator<N> iterator() {
      return Iterators.forArray(this.nodeU, this.nodeV);
   }

   @Override
   public abstract boolean equals(@Nullable Object obj);

   @Override
   public abstract int hashCode();

   private static final class Ordered<N> extends EndpointPair<N> {
      private Ordered(N source, N target) {
         super(source, target);
      }

      @Override
      public N source() {
         return this.nodeU();
      }

      @Override
      public N target() {
         return this.nodeV();
      }

      @Override
      public boolean isOrdered() {
         return true;
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj == this) {
            return true;
         }

         if (!(obj instanceof EndpointPair)) {
            return false;
         }

         EndpointPair<?> other = (EndpointPair<?>)obj;
         return this.isOrdered() != other.isOrdered() ? false : this.source().equals(other.source()) && this.target().equals(other.target());
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.source(), this.target());
      }

      @Override
      public String toString() {
         return "<" + this.source() + " -> " + this.target() + ">";
      }
   }

   private static final class Unordered<N> extends EndpointPair<N> {
      private Unordered(N nodeU, N nodeV) {
         super(nodeU, nodeV);
      }

      @Override
      public N source() {
         throw new UnsupportedOperationException(
            "Cannot call source()/target() on a EndpointPair from an undirected graph. Consider calling adjacentNode(node) if you already have a node, or nodeU()/nodeV() if you don't."
         );
      }

      @Override
      public N target() {
         throw new UnsupportedOperationException(
            "Cannot call source()/target() on a EndpointPair from an undirected graph. Consider calling adjacentNode(node) if you already have a node, or nodeU()/nodeV() if you don't."
         );
      }

      @Override
      public boolean isOrdered() {
         return false;
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof EndpointPair)) {
            return false;
         } else {
            EndpointPair<?> other = (EndpointPair<?>)obj;
            if (this.isOrdered() != other.isOrdered()) {
               return false;
            } else {
               return this.nodeU().equals(other.nodeU())
                  ? this.nodeV().equals(other.nodeV())
                  : this.nodeU().equals(other.nodeV()) && this.nodeV().equals(other.nodeU());
            }
         }
      }

      @Override
      public int hashCode() {
         return this.nodeU().hashCode() + this.nodeV().hashCode();
      }

      @Override
      public String toString() {
         return "[" + this.nodeU() + ", " + this.nodeV() + "]";
      }
   }
}
