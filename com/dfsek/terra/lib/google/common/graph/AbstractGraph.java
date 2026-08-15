package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import org.jspecify.annotations.Nullable;

@Beta
public abstract class AbstractGraph<N> extends AbstractBaseGraph<N> implements Graph<N> {
   @Override
   public final boolean equals(@Nullable Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof Graph)) {
         return false;
      }

      Graph<?> other = (Graph<?>)obj;
      return this.isDirected() == other.isDirected() && this.nodes().equals(other.nodes()) && this.edges().equals(other.edges());
   }

   @Override
   public final int hashCode() {
      return this.edges().hashCode();
   }

   @Override
   public String toString() {
      return "isDirected: " + this.isDirected() + ", allowsSelfLoops: " + this.allowsSelfLoops() + ", nodes: " + this.nodes() + ", edges: " + this.edges();
   }
}
