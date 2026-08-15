package com.dfsek.terra.lib.commons.io.function;

import java.util.Objects;
import java.util.stream.BaseStream;

abstract class IOBaseStreamAdapter<T, S extends IOBaseStream<T, S, B>, B extends BaseStream<T, B>> implements IOBaseStream<T, S, B> {
   private final B delegate;

   IOBaseStreamAdapter(B delegate) {
      this.delegate = Objects.requireNonNull(delegate, "delegate");
   }

   @Override
   public B unwrap() {
      return this.delegate;
   }
}
