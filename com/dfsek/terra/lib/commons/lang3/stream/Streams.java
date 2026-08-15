package com.dfsek.terra.lib.commons.lang3.stream;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.function.Failable;
import com.dfsek.terra.lib.commons.lang3.function.FailableConsumer;
import com.dfsek.terra.lib.commons.lang3.function.FailableFunction;
import com.dfsek.terra.lib.commons.lang3.function.FailablePredicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collector.Characteristics;

public class Streams {
   public static <T> Streams.FailableStream<T> failableStream(Collection<T> stream) {
      return failableStream(of(stream));
   }

   public static <T> Streams.FailableStream<T> failableStream(Stream<T> stream) {
      return new Streams.FailableStream<>(stream);
   }

   public static <T> Streams.FailableStream<T> failableStream(T value) {
      return failableStream(streamOf(value));
   }

   @SafeVarargs
   public static <T> Streams.FailableStream<T> failableStream(T... values) {
      return failableStream(of(values));
   }

   public static <E> Stream<E> instancesOf(Class<? super E> clazz, Collection<? super E> collection) {
      return instancesOf(clazz, of(collection));
   }

   private static <E> Stream<E> instancesOf(Class<? super E> clazz, Stream<?> stream) {
      return (Stream<E>)of(stream).filter(clazz::isInstance);
   }

   public static <E> Stream<E> nonNull(Collection<E> collection) {
      return of(collection).filter(Objects::nonNull);
   }

   public static <E> Stream<E> nonNull(E array) {
      return nonNull(streamOf(array));
   }

   @SafeVarargs
   public static <E> Stream<E> nonNull(E... array) {
      return nonNull(of(array));
   }

   public static <E> Stream<E> nonNull(Stream<E> stream) {
      return of(stream).filter(Objects::nonNull);
   }

   public static <E> Stream<E> of(Collection<E> collection) {
      return collection == null ? Stream.empty() : collection.stream();
   }

   public static <E> Stream<E> of(Enumeration<E> enumeration) {
      return StreamSupport.stream(new Streams.EnumerationSpliterator<>(Long.MAX_VALUE, 16, enumeration), false);
   }

   public static <E> Stream<E> of(Iterable<E> iterable) {
      return iterable == null ? Stream.empty() : StreamSupport.stream(iterable.spliterator(), false);
   }

   public static <E> Stream<E> of(Iterator<E> iterator) {
      return iterator == null ? Stream.empty() : StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 16), false);
   }

   private static <E> Stream<E> of(Stream<E> stream) {
      return stream == null ? Stream.empty() : stream;
   }

   @SafeVarargs
   public static <T> Stream<T> of(T... values) {
      return values == null ? Stream.empty() : Stream.of(values);
   }

   @Deprecated
   public static <E> Streams.FailableStream<E> stream(Collection<E> collection) {
      return failableStream(collection);
   }

   @Deprecated
   public static <T> Streams.FailableStream<T> stream(Stream<T> stream) {
      return failableStream(stream);
   }

   private static <T> Stream<T> streamOf(T value) {
      return value == null ? Stream.empty() : Stream.of(value);
   }

   public static <T> Collector<T, ?, T[]> toArray(Class<T> elementType) {
      return new Streams.ArrayCollector<>(elementType);
   }

   public static class ArrayCollector<E> implements Collector<E, List<E>, E[]> {
      private static final Set<Characteristics> characteristics = Collections.emptySet();
      private final Class<E> elementType;

      public ArrayCollector(Class<E> elementType) {
         this.elementType = Objects.requireNonNull(elementType, "elementType");
      }

      @Override
      public BiConsumer<List<E>, E> accumulator() {
         return List::add;
      }

      @Override
      public Set<Characteristics> characteristics() {
         return characteristics;
      }

      @Override
      public BinaryOperator<List<E>> combiner() {
         return (left, right) -> {
            left.addAll(right);
            return left;
         };
      }

      @Override
      public Function<List<E>, E[]> finisher() {
         return (Function<List<E>, E[]>)(list -> list.toArray(ArrayUtils.newInstance(this.elementType, list.size())));
      }

      @Override
      public Supplier<List<E>> supplier() {
         return ArrayList::new;
      }
   }

   private static final class EnumerationSpliterator<T> extends AbstractSpliterator<T> {
      private final Enumeration<T> enumeration;

      protected EnumerationSpliterator(long estimatedSize, int additionalCharacteristics, Enumeration<T> enumeration) {
         super(estimatedSize, additionalCharacteristics);
         this.enumeration = Objects.requireNonNull(enumeration, "enumeration");
      }

      @Override
      public void forEachRemaining(Consumer<? super T> action) {
         while (this.enumeration.hasMoreElements()) {
            this.next(action);
         }
      }

      private boolean next(Consumer<? super T> action) {
         action.accept(this.enumeration.nextElement());
         return true;
      }

      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
         return this.enumeration.hasMoreElements() && this.next(action);
      }
   }

   public static class FailableStream<T> {
      private Stream<T> stream;
      private boolean terminated;

      public FailableStream(Stream<T> stream) {
         this.stream = stream;
      }

      public boolean allMatch(FailablePredicate<T, ?> predicate) {
         this.assertNotTerminated();
         return this.stream().allMatch(Failable.asPredicate(predicate));
      }

      public boolean anyMatch(FailablePredicate<T, ?> predicate) {
         this.assertNotTerminated();
         return this.stream().anyMatch(Failable.asPredicate(predicate));
      }

      protected void assertNotTerminated() {
         if (this.terminated) {
            throw new IllegalStateException("This stream is already terminated.");
         }
      }

      public <A, R> R collect(Collector<? super T, A, R> collector) {
         this.makeTerminated();
         return this.stream().collect(collector);
      }

      public <A, R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
         this.makeTerminated();
         return this.stream().collect(supplier, accumulator, combiner);
      }

      public Streams.FailableStream<T> filter(FailablePredicate<T, ?> predicate) {
         this.assertNotTerminated();
         this.stream = this.stream.filter(Failable.asPredicate(predicate));
         return this;
      }

      public void forEach(FailableConsumer<T, ?> action) {
         this.makeTerminated();
         this.stream().forEach(Failable.asConsumer(action));
      }

      protected void makeTerminated() {
         this.assertNotTerminated();
         this.terminated = true;
      }

      public <R> Streams.FailableStream<R> map(FailableFunction<T, R, ?> mapper) {
         this.assertNotTerminated();
         return new Streams.FailableStream<>(this.stream.map(Failable.asFunction(mapper)));
      }

      public T reduce(T identity, BinaryOperator<T> accumulator) {
         this.makeTerminated();
         return this.stream().reduce(identity, accumulator);
      }

      public Stream<T> stream() {
         return this.stream;
      }
   }
}
