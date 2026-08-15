package com.dfsek.terra.lib.commons.lang3.stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

public final class LangCollectors {
   private static final Set<Characteristics> CH_NOID = Collections.emptySet();

   public static <T, R, A> R collect(Collector<? super T, A, R> collector, T... array) {
      return Arrays.<T>stream(array).collect(collector);
   }

   public static Collector<Object, ?, String> joining() {
      return new LangCollectors.SimpleCollector<>(StringBuilder::new, StringBuilder::append, StringBuilder::append, StringBuilder::toString, CH_NOID);
   }

   public static Collector<Object, ?, String> joining(CharSequence delimiter) {
      return joining(delimiter, "", "");
   }

   public static Collector<Object, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
      return joining(delimiter, prefix, suffix, Objects::toString);
   }

   public static Collector<Object, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix, Function<Object, String> toString) {
      return new LangCollectors.SimpleCollector<>(
         () -> new StringJoiner(delimiter, prefix, suffix), (a, t) -> a.add(toString.apply(t)), StringJoiner::merge, StringJoiner::toString, CH_NOID
      );
   }

   private LangCollectors() {
   }

   private static final class SimpleCollector<T, A, R> implements Collector<T, A, R> {
      private final BiConsumer<A, T> accumulator;
      private final Set<Characteristics> characteristics;
      private final BinaryOperator<A> combiner;
      private final Function<A, R> finisher;
      private final Supplier<A> supplier;

      private SimpleCollector(
         Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher, Set<Characteristics> characteristics
      ) {
         this.supplier = supplier;
         this.accumulator = accumulator;
         this.combiner = combiner;
         this.finisher = finisher;
         this.characteristics = characteristics;
      }

      @Override
      public BiConsumer<A, T> accumulator() {
         return this.accumulator;
      }

      @Override
      public Set<Characteristics> characteristics() {
         return this.characteristics;
      }

      @Override
      public BinaryOperator<A> combiner() {
         return this.combiner;
      }

      @Override
      public Function<A, R> finisher() {
         return this.finisher;
      }

      @Override
      public Supplier<A> supplier() {
         return this.supplier;
      }
   }
}
