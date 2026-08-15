package com.dfsek.terra.lib.google.common.graph;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.google.errorprone.annotations.DoNotMock;

@DoNotMock("Implement with a lambda, or use GraphBuilder to build a Graph with the desired edges")
@Beta
public interface SuccessorsFunction<N> {
   Iterable<? extends N> successors(N node);
}
