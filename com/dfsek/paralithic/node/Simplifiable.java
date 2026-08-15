package com.dfsek.paralithic.node;

import org.jetbrains.annotations.NotNull;

public interface Simplifiable extends Node {
   @NotNull
   Node simplify();
}
