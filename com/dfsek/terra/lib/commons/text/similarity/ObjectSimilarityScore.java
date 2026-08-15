package com.dfsek.terra.lib.commons.text.similarity;

import java.util.function.BiFunction;

public interface ObjectSimilarityScore<T, R> extends BiFunction<T, T, R> {
   @Override
   R apply(T var1, T var2);
}
