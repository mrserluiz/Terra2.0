package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import java.util.Set;

@Beta
abstract class GraphsBridgeMethods {
   public static <N> Graph<N> transitiveClosure(Graph<N> graph) {
      return Graphs.transitiveClosure(graph);
   }

   public static <N> Set<N> reachableNodes(Graph<N> graph, N node) {
      return Graphs.reachableNodes(graph, node);
   }
}
