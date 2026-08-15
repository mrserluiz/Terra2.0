package com.dfsek.terra.lib.commons.text.similarity;

import java.util.Objects;

public interface SimilarityInput<E> {
   static SimilarityInput<Character> input(CharSequence cs) {
      return new SimilarityCharacterInput(cs);
   }

   static <T> SimilarityInput<T> input(Object input) {
      if (input instanceof SimilarityInput) {
         return (SimilarityInput<T>)input;
      } else if (input instanceof CharSequence) {
         return (SimilarityInput<T>)input((CharSequence)input);
      } else {
         throw new IllegalArgumentException(Objects.requireNonNull(input, "input").getClass().getName());
      }
   }

   E at(int var1);

   int length();
}
