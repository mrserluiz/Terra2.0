package com.dfsek.terra.lib.commons.text.similarity;

public interface SimilarityScore<R> extends ObjectSimilarityScore<CharSequence, R> {
   R apply(CharSequence var1, CharSequence var2);
}
