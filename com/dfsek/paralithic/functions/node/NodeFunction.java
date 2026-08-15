package com.dfsek.paralithic.functions.node;

import com.dfsek.paralithic.functions.Function;
import com.dfsek.paralithic.node.Node;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface NodeFunction extends Function {
   @NotNull
   @Contract("_ -> new")
   Node createNode(@NotNull List<Node> var1);
}
