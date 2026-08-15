package com.dfsek.terra.lib.commons.text.similarity;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

final class Counter {
   public static Map<CharSequence, Integer> of(CharSequence[] tokens) {
      Map<CharSequence, Integer> map = new HashMap<>();
      Stream.of(tokens).forEach(token -> map.compute(token, (k, v) -> v != null ? v + 1 : 1));
      return map;
   }

   private Counter() {
   }
}
