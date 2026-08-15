package com.dfsek.terra.lib.commons.io.function;

import com.dfsek.terra.lib.commons.io.IOExceptionList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface IOStream<T> extends IOBaseStream<T, IOStream<T>, Stream<T>> {
   static <T> IOStream<T> adapt(Stream<T> stream) {
      return IOStreamAdapter.adapt(stream);
   }

   static <T> IOStream<T> empty() {
      return IOStreamAdapter.adapt(Stream.empty());
   }

   static <T> IOStream<T> iterate(final T seed, final IOUnaryOperator<T> f) {
      Objects.requireNonNull(f);
      Iterator<T> iterator = new Iterator<T>() {
         Object t = IOStreams.NONE;

         @Override
         public boolean hasNext() {
            return true;
         }

         @Override
         public T next() throws NoSuchElementException {
            try {
               return (T)(this.t = this.t == IOStreams.NONE ? seed : f.apply((T)this.t));
            } catch (IOException e) {
               NoSuchElementException nsee = new NoSuchElementException();
               nsee.initCause(e);
               throw nsee;
            }
         }
      };
      return adapt(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1040), false));
   }

   static <T> IOStream<T> of(Iterable<T> values) {
      return values == null ? empty() : adapt(StreamSupport.stream(values.spliterator(), false));
   }

   @SafeVarargs
   static <T> IOStream<T> of(T... values) {
      return values != null && values.length != 0 ? adapt(Arrays.stream(values)) : empty();
   }

   static <T> IOStream<T> of(T t) {
      return adapt(Stream.of(t));
   }

   default boolean allMatch(IOPredicate<? super T> predicate) throws IOException {
      return this.unwrap().allMatch(t -> Erase.test(predicate, (T)t));
   }

   default boolean anyMatch(IOPredicate<? super T> predicate) throws IOException {
      return this.unwrap().anyMatch(t -> Erase.test(predicate, (T)t));
   }

   default <R, A> R collect(Collector<? super T, A, R> collector) {
      return this.unwrap().collect(collector);
   }

   default <R> R collect(IOSupplier<R> supplier, IOBiConsumer<R, ? super T> accumulator, IOBiConsumer<R, R> combiner) throws IOException {
      return this.unwrap().collect(() -> Erase.get(supplier), (t, u) -> Erase.accept(accumulator, t, u), (t, u) -> Erase.accept(combiner, t, u));
   }

   default long count() {
      return this.unwrap().count();
   }

   default IOStream<T> distinct() {
      return adapt(this.unwrap().distinct());
   }

   default IOStream<T> filter(IOPredicate<? super T> predicate) throws IOException {
      return adapt(this.unwrap().filter(t -> Erase.test(predicate, (T)t)));
   }

   default Optional<T> findAny() {
      return this.unwrap().findAny();
   }

   default Optional<T> findFirst() {
      return this.unwrap().findFirst();
   }

   default <R> IOStream<R> flatMap(IOFunction<? super T, ? extends IOStream<? extends R>> mapper) throws IOException {
      return adapt(this.unwrap().flatMap(t -> Erase.apply(mapper, (T)t).unwrap()));
   }

   default DoubleStream flatMapToDouble(IOFunction<? super T, ? extends DoubleStream> mapper) throws IOException {
      return this.unwrap().flatMapToDouble(t -> Erase.apply(mapper, (T)t));
   }

   default IntStream flatMapToInt(IOFunction<? super T, ? extends IntStream> mapper) throws IOException {
      return this.unwrap().flatMapToInt(t -> Erase.apply(mapper, (T)t));
   }

   default LongStream flatMapToLong(IOFunction<? super T, ? extends LongStream> mapper) throws IOException {
      return this.unwrap().flatMapToLong(t -> Erase.apply(mapper, (T)t));
   }

   default void forAll(IOConsumer<T> action) throws IOExceptionList {
      this.forAll(action, (i, e) -> e);
   }

   default void forAll(IOConsumer<T> action, BiFunction<Integer, IOException, IOException> exSupplier) throws IOExceptionList {
      AtomicReference<List<IOException>> causeList = new AtomicReference<>();
      AtomicInteger index = new AtomicInteger();
      IOConsumer<T> safeAction = IOStreams.toIOConsumer(action);
      this.unwrap().forEach(e -> {
         try {
            safeAction.accept((T)e);
         } catch (IOException innerEx) {
            if (causeList.get() == null) {
               causeList.set(new ArrayList<>());
            }

            if (exSupplier != null) {
               causeList.get().add(exSupplier.apply(index.get(), innerEx));
            }
         }

         index.incrementAndGet();
      });
      IOExceptionList.checkEmpty(causeList.get(), null);
   }

   default void forEach(IOConsumer<? super T> action) throws IOException {
      this.unwrap().forEach(e -> Erase.accept(action, e));
   }

   default void forEachOrdered(IOConsumer<? super T> action) throws IOException {
      this.unwrap().forEachOrdered(e -> Erase.accept(action, e));
   }

   default IOStream<T> limit(long maxSize) {
      return adapt(this.unwrap().limit(maxSize));
   }

   default <R> IOStream<R> map(IOFunction<? super T, ? extends R> mapper) throws IOException {
      return adapt(this.unwrap().map(t -> Erase.apply(mapper, (T)t)));
   }

   default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
      return this.unwrap().mapToDouble(mapper);
   }

   default IntStream mapToInt(ToIntFunction<? super T> mapper) {
      return this.unwrap().mapToInt(mapper);
   }

   default LongStream mapToLong(ToLongFunction<? super T> mapper) {
      return this.unwrap().mapToLong(mapper);
   }

   default Optional<T> max(IOComparator<? super T> comparator) throws IOException {
      return this.unwrap().max((t, u) -> Erase.compare(comparator, (T)t, (T)u));
   }

   default Optional<T> min(IOComparator<? super T> comparator) throws IOException {
      return this.unwrap().min((t, u) -> Erase.compare(comparator, (T)t, (T)u));
   }

   default boolean noneMatch(IOPredicate<? super T> predicate) throws IOException {
      return this.unwrap().noneMatch(t -> Erase.test(predicate, (T)t));
   }

   default IOStream<T> peek(IOConsumer<? super T> action) throws IOException {
      return adapt(this.unwrap().peek(t -> Erase.accept(action, t)));
   }

   default Optional<T> reduce(IOBinaryOperator<T> accumulator) throws IOException {
      return this.unwrap().reduce((t, u) -> Erase.apply(accumulator, (T)t, (T)u));
   }

   default T reduce(T identity, IOBinaryOperator<T> accumulator) throws IOException {
      return this.unwrap().reduce(identity, (t, u) -> Erase.apply(accumulator, (T)t, (T)u));
   }

   default <U> U reduce(U identity, IOBiFunction<U, ? super T, U> accumulator, IOBinaryOperator<U> combiner) throws IOException {
      return this.unwrap().reduce(identity, (t, u) -> Erase.apply(accumulator, t, (T)u), (t, u) -> Erase.apply(combiner, (U)t, (U)u));
   }

   default IOStream<T> skip(long n) {
      return adapt(this.unwrap().skip(n));
   }

   default IOStream<T> sorted() {
      return adapt(this.unwrap().sorted());
   }

   default IOStream<T> sorted(IOComparator<? super T> comparator) throws IOException {
      return adapt(this.unwrap().sorted((t, u) -> Erase.compare(comparator, (T)t, (T)u)));
   }

   default Object[] toArray() {
      return this.unwrap().toArray();
   }

   default <A> A[] toArray(IntFunction<A[]> generator) {
      return this.unwrap().toArray(generator);
   }
}
