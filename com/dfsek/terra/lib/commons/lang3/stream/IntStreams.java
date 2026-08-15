package com.dfsek.terra.lib.commons.lang3.stream;

import java.util.stream.IntStream;

public class IntStreams {
   public static IntStream range(int endExclusive) {
      return IntStream.range(0, endExclusive);
   }

   public static IntStream rangeClosed(int endInclusive) {
      return IntStream.rangeClosed(0, endInclusive);
   }
}
