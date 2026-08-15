package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.collect.BiMap;
import com.dfsek.terra.lib.google.common.collect.HashBiMap;
import com.dfsek.terra.lib.google.common.collect.ImmutableBiMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

final class UndirectedNetworkConnections<N, E> extends AbstractUndirectedNetworkConnections<N, E> {
   UndirectedNetworkConnections(Map<E, N> incidentEdgeMap) {
      super(incidentEdgeMap);
   }

   static <N, E> UndirectedNetworkConnections<N, E> of() {
      return new UndirectedNetworkConnections<>(HashBiMap.create(2));
   }

   static <N, E> UndirectedNetworkConnections<N, E> ofImmutable(Map<E, N> incidentEdges) {
      return new UndirectedNetworkConnections<>(ImmutableBiMap.copyOf(incidentEdges));
   }

   @Override
   public Set<N> adjacentNodes() {
      return Collections.unmodifiableSet(((BiMap)this.incidentEdgeMap).values());
   }

   @Override
   public Set<E> edgesConnecting(N node) {
      return new EdgesConnecting<>(((BiMap)this.incidentEdgeMap).inverse(), node);
   }
}
