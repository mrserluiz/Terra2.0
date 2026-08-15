package com.dfsek.terra.lib.commons.io.function;

import java.util.stream.Stream;

final class IOStreamAdapter<T> extends IOBaseStreamAdapter<T, IOStream<T>, Stream<T>> implements IOStream<T> {
   static <T> IOStream<T> adapt(Stream<T> delegate) {
      return delegate != null ? new IOStreamAdapter<>(delegate) : IOStream.empty();
   }

   private IOStreamAdapter(Stream<T> delegate) {
      super(delegate);
   }

   public IOStream<T> wrap(Stream<T> delegate) {
      return this.unwrap() == delegate ? this : adapt(delegate);
   }
}
