package com.dfsek.terra.lib.commons.io.function;

import com.dfsek.terra.lib.commons.io.IOExceptionList;
import com.dfsek.terra.lib.commons.io.IOIndexedException;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class IOStreams {
   static final Object NONE = new Object();

   static <T> void forAll(Stream<T> stream, IOConsumer<T> action) throws IOExceptionList {
      forAll(stream, action, (i, e) -> e);
   }

   static <T> void forAll(Stream<T> stream, IOConsumer<T> action, BiFunction<Integer, IOException, IOException> exSupplier) throws IOExceptionList {
      IOStream.adapt(stream).forAll(action, IOIndexedException::new);
   }

   static <T> void forEach(Stream<T> stream, IOConsumer<T> action) throws IOException {
      IOConsumer<T> actualAction = toIOConsumer(action);
      of(stream).forEach(e -> Erase.accept(actualAction, (T)e));
   }

   static <T> Stream<T> of(Iterable<T> values) {
      return values == null ? Stream.empty() : StreamSupport.stream(values.spliterator(), false);
   }

   static <T> Stream<T> of(Stream<T> stream) {
      return stream == null ? Stream.empty() : stream;
   }

   @SafeVarargs
   static <T> Stream<T> of(T... values) {
      return values == null ? Stream.empty() : Stream.of(values);
   }

   static <T> IOConsumer<T> toIOConsumer(IOConsumer<T> action) {
      return action != null ? action : IOConsumer.noop();
   }

   private IOStreams() {
   }
}
