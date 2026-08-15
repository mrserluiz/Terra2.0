package com.dfsek.terra.lib.commons.io.function;

import com.dfsek.terra.lib.commons.io.IOExceptionList;
import com.dfsek.terra.lib.commons.io.IOIndexedException;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

@FunctionalInterface
public interface IOConsumer<T> {
   IOConsumer<?> NOOP_IO_CONSUMER = t -> {};

   static <T> void forAll(IOConsumer<T> action, Iterable<T> iterable) throws IOExceptionList {
      IOStreams.forAll(IOStreams.of(iterable), action);
   }

   static <T> void forAll(IOConsumer<T> action, Stream<T> stream) throws IOExceptionList {
      IOStreams.forAll(stream, action, IOIndexedException::new);
   }

   @SafeVarargs
   static <T> void forAll(IOConsumer<T> action, T... array) throws IOExceptionList {
      IOStreams.forAll(IOStreams.of(array), action);
   }

   static <T> void forEach(Iterable<T> iterable, IOConsumer<T> action) throws IOException {
      IOStreams.forEach(IOStreams.of(iterable), action);
   }

   static <T> void forEach(Stream<T> stream, IOConsumer<T> action) throws IOException {
      IOStreams.forEach(stream, action);
   }

   static <T> void forEach(T[] array, IOConsumer<T> action) throws IOException {
      IOStreams.forEach(IOStreams.of(array), action);
   }

   static <T> IOConsumer<T> noop() {
      return (IOConsumer<T>)NOOP_IO_CONSUMER;
   }

   void accept(T var1) throws IOException;

   default IOConsumer<T> andThen(IOConsumer<? super T> after) {
      Objects.requireNonNull(after, "after");
      return t -> {
         this.accept(t);
         after.accept(t);
      };
   }

   default Consumer<T> asConsumer() {
      return t -> Uncheck.accept(this, t);
   }
}
