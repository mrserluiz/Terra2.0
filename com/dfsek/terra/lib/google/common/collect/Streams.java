package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Optional;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.math.LongMath;
import com.google.errorprone.annotations.InlineMe;
import com.google.errorprone.annotations.InlineMeValidationDisabled;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterator.OfDouble;
import java.util.Spliterator.OfInt;
import java.util.Spliterator.OfLong;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Streams {
   public static <T> Stream<T> stream(Iterable<T> iterable) {
      return iterable instanceof Collection ? ((Collection)iterable).stream() : StreamSupport.stream(iterable.spliterator(), false);
   }

   @Deprecated
   @InlineMe(replacement = "collection.stream()")
   public static <T> Stream<T> stream(Collection<T> collection) {
      return collection.stream();
   }

   public static <T> Stream<T> stream(Iterator<T> iterator) {
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
   }

   public static <T> Stream<T> stream(Optional<T> optional) {
      return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();
   }

   @Beta
   @InlineMe(replacement = "optional.stream()")
   @InlineMeValidationDisabled("Java 9+ API only")
   public static <T> Stream<T> stream(java.util.Optional<T> optional) {
      return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();
   }

   @Beta
   @InlineMe(replacement = "optional.stream()")
   @InlineMeValidationDisabled("Java 9+ API only")
   public static IntStream stream(OptionalInt optional) {
      return optional.isPresent() ? IntStream.of(optional.getAsInt()) : IntStream.empty();
   }

   @Beta
   @InlineMe(replacement = "optional.stream()")
   @InlineMeValidationDisabled("Java 9+ API only")
   public static LongStream stream(OptionalLong optional) {
      return optional.isPresent() ? LongStream.of(optional.getAsLong()) : LongStream.empty();
   }

   @Beta
   @InlineMe(replacement = "optional.stream()")
   @InlineMeValidationDisabled("Java 9+ API only")
   public static DoubleStream stream(OptionalDouble optional) {
      return optional.isPresent() ? DoubleStream.of(optional.getAsDouble()) : DoubleStream.empty();
   }

   private static void closeAll(BaseStream<?, ?>[] toClose) {
      Exception exception = null;

      for (BaseStream<?, ?> stream : toClose) {
         try {
            stream.close();
         } catch (Exception e) {
            if (exception == null) {
               exception = e;
            } else {
               exception.addSuppressed(e);
            }
         }
      }

      if (exception != null) {
         SneakyThrows.sneakyThrow(exception);
      }
   }

   @SafeVarargs
   public static <T> Stream<T> concat(Stream<? extends T>... streams) {
      boolean isParallel = false;
      int characteristics = 336;
      long estimatedSize = 0L;
      ImmutableList.Builder<Spliterator<? extends T>> splitrsBuilder = new ImmutableList.Builder<>(streams.length);

      for (Stream<? extends T> stream : streams) {
         isParallel |= stream.isParallel();
         Spliterator<? extends T> splitr = stream.spliterator();
         splitrsBuilder.add(splitr);
         characteristics &= splitr.characteristics();
         estimatedSize = LongMath.saturatedAdd(estimatedSize, splitr.estimateSize());
      }

      return StreamSupport.stream(
            CollectSpliterators.flatMap(splitrsBuilder.build().spliterator(), splitrx -> splitrx, characteristics, estimatedSize), isParallel
         )
         .onClose(() -> closeAll(streams));
   }

   public static IntStream concat(IntStream... streams) {
      boolean isParallel = false;
      int characteristics = 336;
      long estimatedSize = 0L;
      ImmutableList.Builder<OfInt> splitrsBuilder = new ImmutableList.Builder<>(streams.length);

      for (IntStream stream : streams) {
         isParallel |= stream.isParallel();
         OfInt splitr = stream.spliterator();
         splitrsBuilder.add(splitr);
         characteristics &= splitr.characteristics();
         estimatedSize = LongMath.saturatedAdd(estimatedSize, splitr.estimateSize());
      }

      return StreamSupport.intStream(
            CollectSpliterators.flatMapToInt(splitrsBuilder.build().spliterator(), splitrx -> splitrx, characteristics, estimatedSize), isParallel
         )
         .onClose(() -> closeAll(streams));
   }

   public static LongStream concat(LongStream... streams) {
      boolean isParallel = false;
      int characteristics = 336;
      long estimatedSize = 0L;
      ImmutableList.Builder<OfLong> splitrsBuilder = new ImmutableList.Builder<>(streams.length);

      for (LongStream stream : streams) {
         isParallel |= stream.isParallel();
         OfLong splitr = stream.spliterator();
         splitrsBuilder.add(splitr);
         characteristics &= splitr.characteristics();
         estimatedSize = LongMath.saturatedAdd(estimatedSize, splitr.estimateSize());
      }

      return StreamSupport.longStream(
            CollectSpliterators.flatMapToLong(splitrsBuilder.build().spliterator(), splitrx -> splitrx, characteristics, estimatedSize), isParallel
         )
         .onClose(() -> closeAll(streams));
   }

   public static DoubleStream concat(DoubleStream... streams) {
      boolean isParallel = false;
      int characteristics = 336;
      long estimatedSize = 0L;
      ImmutableList.Builder<OfDouble> splitrsBuilder = new ImmutableList.Builder<>(streams.length);

      for (DoubleStream stream : streams) {
         isParallel |= stream.isParallel();
         OfDouble splitr = stream.spliterator();
         splitrsBuilder.add(splitr);
         characteristics &= splitr.characteristics();
         estimatedSize = LongMath.saturatedAdd(estimatedSize, splitr.estimateSize());
      }

      return StreamSupport.doubleStream(
            CollectSpliterators.flatMapToDouble(splitrsBuilder.build().spliterator(), splitrx -> splitrx, characteristics, estimatedSize), isParallel
         )
         .onClose(() -> closeAll(streams));
   }

   @Beta
   public static <A, B, R> Stream<R> zip(Stream<A> streamA, Stream<B> streamB, BiFunction<? super A, ? super B, R> function) {
      Preconditions.checkNotNull(streamA);
      Preconditions.checkNotNull(streamB);
      Preconditions.checkNotNull(function);
      boolean isParallel = streamA.isParallel() || streamB.isParallel();
      Spliterator<A> splitrA = streamA.spliterator();
      Spliterator<B> splitrB = streamB.spliterator();
      int characteristics = splitrA.characteristics() & splitrB.characteristics() & 80;
      final Iterator<A> itrA = Spliterators.iterator(splitrA);
      final Iterator<B> itrB = Spliterators.iterator(splitrB);
      return StreamSupport.stream(new AbstractSpliterator<R>(Math.min(splitrA.estimateSize(), splitrB.estimateSize()), characteristics) {
         @Override
         public boolean tryAdvance(Consumer<? super R> action) {
            if (itrA.hasNext() && itrB.hasNext()) {
               action.accept(function.apply(itrA.next(), itrB.next()));
               return true;
            } else {
               return false;
            }
         }
      }, isParallel).onClose(streamA::close).onClose(streamB::close);
   }

   @Beta
   public static <A, B> void forEachPair(Stream<A> streamA, Stream<B> streamB, BiConsumer<? super A, ? super B> consumer) {
      Preconditions.checkNotNull(consumer);
      if (!streamA.isParallel() && !streamB.isParallel()) {
         Iterator<A> iterA = streamA.iterator();
         Iterator<B> iterB = streamB.iterator();

         while (iterA.hasNext() && iterB.hasNext()) {
            consumer.accept(iterA.next(), iterB.next());
         }
      } else {
         zip(streamA, streamB, Streams.TemporaryPair::new).forEach(pair -> consumer.accept(pair.a, pair.b));
      }
   }

   public static <T, R> Stream<R> mapWithIndex(Stream<T> stream, Streams.FunctionWithIndex<? super T, ? extends R> function) {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(function);
      boolean isParallel = stream.isParallel();
      Spliterator<T> fromSpliterator = stream.spliterator();

      class Splitr extends Streams.MapWithIndexSpliterator<Spliterator<T>, R, Splitr> implements Consumer<T> {
         @Nullable Object holder;

         Splitr(Spliterator<T> splitr, long index) {
            super(splitr, index);
         }

         @Override
         public void accept(@ParametricNullness T t) {
            this.holder = t;
         }

         @Override
         public boolean tryAdvance(Consumer<? super R> action) {
            if (this.fromSpliterator.tryAdvance(this)) {
               try {
                  action.accept((R)function.apply(NullnessCasts.uncheckedCastNullableTToT((T)this.holder), this.index++));
                  return true;
               } finally {
                  this.holder = null;
               }
            } else {
               return false;
            }
         }

         Splitr createSplit(Spliterator<T> from, long i) {
            return new Splitr(from, i);
         }
      }

      if (!fromSpliterator.hasCharacteristics(16384)) {
         final Iterator<T> fromIterator = Spliterators.iterator(fromSpliterator);
         return StreamSupport.stream(new AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) {
            long index = 0L;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (fromIterator.hasNext()) {
                  action.accept((R)function.apply(fromIterator.next(), this.index++));
                  return true;
               } else {
                  return false;
               }
            }
         }, isParallel).onClose(stream::close);
      } else {
         return StreamSupport.stream(new Splitr(fromSpliterator, 0L), isParallel).onClose(stream::close);
      }
   }

   public static <R> Stream<R> mapWithIndex(IntStream stream, Streams.IntFunctionWithIndex<R> function) {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(function);
      boolean isParallel = stream.isParallel();
      OfInt fromSpliterator = stream.spliterator();

      class Splitr extends Streams.MapWithIndexSpliterator<OfInt, R, Splitr> implements IntConsumer {
         int holder;

         Splitr(OfInt splitr, long index) {
            super(splitr, index);
         }

         @Override
         public void accept(int t) {
            this.holder = t;
         }

         @Override
         public boolean tryAdvance(Consumer<? super R> action) {
            if (this.fromSpliterator.tryAdvance(this)) {
               action.accept(function.apply(this.holder, this.index++));
               return true;
            } else {
               return false;
            }
         }

         Splitr createSplit(OfInt from, long i) {
            return new Splitr(from, i);
         }
      }

      if (!fromSpliterator.hasCharacteristics(16384)) {
         final java.util.PrimitiveIterator.OfInt fromIterator = Spliterators.iterator(fromSpliterator);
         return StreamSupport.stream(new AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) {
            long index = 0L;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (fromIterator.hasNext()) {
                  action.accept(function.apply(fromIterator.nextInt(), this.index++));
                  return true;
               } else {
                  return false;
               }
            }
         }, isParallel).onClose(stream::close);
      } else {
         return StreamSupport.stream(new Splitr(fromSpliterator, 0L), isParallel).onClose(stream::close);
      }
   }

   public static <R> Stream<R> mapWithIndex(LongStream stream, Streams.LongFunctionWithIndex<R> function) {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(function);
      boolean isParallel = stream.isParallel();
      OfLong fromSpliterator = stream.spliterator();

      class Splitr extends Streams.MapWithIndexSpliterator<OfLong, R, Splitr> implements LongConsumer {
         long holder;

         Splitr(OfLong splitr, long index) {
            super(splitr, index);
         }

         @Override
         public void accept(long t) {
            this.holder = t;
         }

         @Override
         public boolean tryAdvance(Consumer<? super R> action) {
            if (this.fromSpliterator.tryAdvance(this)) {
               action.accept(function.apply(this.holder, this.index++));
               return true;
            } else {
               return false;
            }
         }

         Splitr createSplit(OfLong from, long i) {
            return new Splitr(from, i);
         }
      }

      if (!fromSpliterator.hasCharacteristics(16384)) {
         final java.util.PrimitiveIterator.OfLong fromIterator = Spliterators.iterator(fromSpliterator);
         return StreamSupport.stream(new AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) {
            long index = 0L;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (fromIterator.hasNext()) {
                  action.accept(function.apply(fromIterator.nextLong(), this.index++));
                  return true;
               } else {
                  return false;
               }
            }
         }, isParallel).onClose(stream::close);
      } else {
         return StreamSupport.stream(new Splitr(fromSpliterator, 0L), isParallel).onClose(stream::close);
      }
   }

   public static <R> Stream<R> mapWithIndex(DoubleStream stream, Streams.DoubleFunctionWithIndex<R> function) {
      Preconditions.checkNotNull(stream);
      Preconditions.checkNotNull(function);
      boolean isParallel = stream.isParallel();
      OfDouble fromSpliterator = stream.spliterator();

      class Splitr extends Streams.MapWithIndexSpliterator<OfDouble, R, Splitr> implements DoubleConsumer {
         double holder;

         Splitr(OfDouble splitr, long index) {
            super(splitr, index);
         }

         @Override
         public void accept(double t) {
            this.holder = t;
         }

         @Override
         public boolean tryAdvance(Consumer<? super R> action) {
            if (this.fromSpliterator.tryAdvance(this)) {
               action.accept(function.apply(this.holder, this.index++));
               return true;
            } else {
               return false;
            }
         }

         Splitr createSplit(OfDouble from, long i) {
            return new Splitr(from, i);
         }
      }

      if (!fromSpliterator.hasCharacteristics(16384)) {
         final java.util.PrimitiveIterator.OfDouble fromIterator = Spliterators.iterator(fromSpliterator);
         return StreamSupport.stream(new AbstractSpliterator<R>(fromSpliterator.estimateSize(), fromSpliterator.characteristics() & 80) {
            long index = 0L;

            @Override
            public boolean tryAdvance(Consumer<? super R> action) {
               if (fromIterator.hasNext()) {
                  action.accept(function.apply(fromIterator.nextDouble(), this.index++));
                  return true;
               } else {
                  return false;
               }
            }
         }, isParallel).onClose(stream::close);
      } else {
         return StreamSupport.stream(new Splitr(fromSpliterator, 0L), isParallel).onClose(stream::close);
      }
   }

   public static <T> java.util.Optional<T> findLast(Stream<T> stream) {
      class OptionalState {
         boolean set = false;
         @Nullable T value = (T)null;

         void set(T value) {
            this.set = true;
            this.value = value;
         }

         T get() {
            return Objects.requireNonNull(this.value);
         }
      }

      OptionalState state = new OptionalState();
      Deque<Spliterator<T>> splits = new ArrayDeque<>();
      splits.addLast(stream.spliterator());

      while (!splits.isEmpty()) {
         Spliterator<T> spliterator = splits.removeLast();
         if (spliterator.getExactSizeIfKnown() != 0L) {
            if (spliterator.hasCharacteristics(16384)) {
               while (true) {
                  Spliterator<T> prefix = spliterator.trySplit();
                  if (prefix == null || prefix.getExactSizeIfKnown() == 0L) {
                     break;
                  }

                  if (spliterator.getExactSizeIfKnown() == 0L) {
                     spliterator = prefix;
                     break;
                  }
               }

               spliterator.forEachRemaining(state::set);
               return java.util.Optional.of((T)state.get());
            }

            Spliterator<T> prefix = spliterator.trySplit();
            if (prefix != null && prefix.getExactSizeIfKnown() != 0L) {
               splits.addLast(prefix);
               splits.addLast(spliterator);
            } else {
               spliterator.forEachRemaining(state::set);
               if (state.set) {
                  return java.util.Optional.of((T)state.get());
               }
            }
         }
      }

      return java.util.Optional.empty();
   }

   public static OptionalInt findLast(IntStream stream) {
      java.util.Optional<Integer> boxedLast = findLast(stream.boxed());
      return boxedLast.map(OptionalInt::of).orElse(OptionalInt.empty());
   }

   public static OptionalLong findLast(LongStream stream) {
      java.util.Optional<Long> boxedLast = findLast(stream.boxed());
      return boxedLast.map(OptionalLong::of).orElse(OptionalLong.empty());
   }

   public static OptionalDouble findLast(DoubleStream stream) {
      java.util.Optional<Double> boxedLast = findLast(stream.boxed());
      return boxedLast.map(OptionalDouble::of).orElse(OptionalDouble.empty());
   }

   private Streams() {
   }

   public interface DoubleFunctionWithIndex<R> {
      @ParametricNullness
      R apply(double from, long index);
   }

   public interface FunctionWithIndex<T, R> {
      @ParametricNullness
      R apply(@ParametricNullness T from, long index);
   }

   public interface IntFunctionWithIndex<R> {
      @ParametricNullness
      R apply(int from, long index);
   }

   public interface LongFunctionWithIndex<R> {
      @ParametricNullness
      R apply(long from, long index);
   }

   private abstract static class MapWithIndexSpliterator<F extends Spliterator<?>, R, S extends Streams.MapWithIndexSpliterator<F, R, S>>
      implements Spliterator<R> {
      final F fromSpliterator;
      long index;

      MapWithIndexSpliterator(F fromSpliterator, long index) {
         this.fromSpliterator = fromSpliterator;
         this.index = index;
      }

      abstract S createSplit(F from, long i);

      public @Nullable S trySplit() {
         Spliterator<?> splitOrNull = this.fromSpliterator.trySplit();
         if (splitOrNull == null) {
            return null;
         }

         F split = (F)splitOrNull;
         S result = this.createSplit(split, this.index);
         this.index = this.index + split.getExactSizeIfKnown();
         return result;
      }

      @Override
      public long estimateSize() {
         return this.fromSpliterator.estimateSize();
      }

      @Override
      public int characteristics() {
         return this.fromSpliterator.characteristics() & 16464;
      }
   }

   private static class TemporaryPair<A, B> {
      @ParametricNullness
      final A a;
      @ParametricNullness
      final B b;

      TemporaryPair(@ParametricNullness A a, @ParametricNullness B b) {
         this.a = a;
         this.b = b;
      }
   }
}
