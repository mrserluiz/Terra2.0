package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Collector.Characteristics;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class CollectCollectors {
   private static final Collector<Object, ?, ImmutableList<Object>> TO_IMMUTABLE_LIST = Collector.of(
      ImmutableList::builder, ImmutableList.Builder::add, ImmutableList.Builder::combine, ImmutableList.Builder::build
   );
   private static final Collector<Object, ?, ImmutableSet<Object>> TO_IMMUTABLE_SET = Collector.of(
      ImmutableSet::builder, ImmutableSet.Builder::add, ImmutableSet.Builder::combine, ImmutableSet.Builder::build
   );
   @GwtIncompatible
   private static final Collector<Range<Comparable<?>>, ?, ImmutableRangeSet<Comparable<?>>> TO_IMMUTABLE_RANGE_SET = Collector.of(
      ImmutableRangeSet::builder, ImmutableRangeSet.Builder::add, ImmutableRangeSet.Builder::combine, ImmutableRangeSet.Builder::build
   );

   static <E> Collector<E, ?, ImmutableList<E>> toImmutableList() {
      return (Collector<E, ?, ImmutableList<E>>)TO_IMMUTABLE_LIST;
   }

   static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
      return (Collector<E, ?, ImmutableSet<E>>)TO_IMMUTABLE_SET;
   }

   static <E> Collector<E, ?, ImmutableSortedSet<E>> toImmutableSortedSet(Comparator<? super E> comparator) {
      Preconditions.checkNotNull(comparator);
      return Collector.of(
         () -> new ImmutableSortedSet.Builder<>(comparator),
         ImmutableSortedSet.Builder::add,
         ImmutableSortedSet.Builder::combine,
         ImmutableSortedSet.Builder::build
      );
   }

   static <E extends Enum<E>> Collector<E, ?, ImmutableSet<E>> toImmutableEnumSet() {
      return (Collector<E, ?, ImmutableSet<E>>)CollectCollectors.EnumSetAccumulator.TO_IMMUTABLE_ENUM_SET;
   }

   private static <E extends Enum<E>> Collector<E, CollectCollectors.EnumSetAccumulator<E>, ImmutableSet<E>> toImmutableEnumSetGeneric() {
      return Collector.of(
         () -> new CollectCollectors.EnumSetAccumulator<>(),
         CollectCollectors.EnumSetAccumulator::add,
         CollectCollectors.EnumSetAccumulator::combine,
         CollectCollectors.EnumSetAccumulator::toImmutableSet,
         Characteristics.UNORDERED
      );
   }

   @GwtIncompatible
   static <E extends Comparable<? super E>> Collector<Range<E>, ?, ImmutableRangeSet<E>> toImmutableRangeSet() {
      return TO_IMMUTABLE_RANGE_SET;
   }

   static <T, E> Collector<T, ?, ImmutableMultiset<E>> toImmutableMultiset(
      Function<? super T, ? extends E> elementFunction, ToIntFunction<? super T> countFunction
   ) {
      Preconditions.checkNotNull(elementFunction);
      Preconditions.checkNotNull(countFunction);
      return Collector.of(
         LinkedHashMultiset::create,
         (multiset, t) -> multiset.add(Preconditions.checkNotNull((E)elementFunction.apply(t)), countFunction.applyAsInt(t)),
         (multiset1, multiset2) -> {
            multiset1.addAll(multiset2);
            return multiset1;
         },
         multiset -> ImmutableMultiset.copyFromEntries(multiset.entrySet())
      );
   }

   static <T, E, M extends Multiset<E>> Collector<T, ?, M> toMultiset(
      Function<? super T, E> elementFunction, ToIntFunction<? super T> countFunction, Supplier<M> multisetSupplier
   ) {
      Preconditions.checkNotNull(elementFunction);
      Preconditions.checkNotNull(countFunction);
      Preconditions.checkNotNull(multisetSupplier);
      return Collector.of(multisetSupplier, (ms, t) -> ms.add(elementFunction.apply(t), countFunction.applyAsInt(t)), (ms1, ms2) -> {
         ms1.addAll(ms2);
         return ms1;
      });
   }

   static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(
         ImmutableMap.Builder::new,
         (builder, input) -> builder.put((K)keyFunction.apply(input), (V)valueFunction.apply(input)),
         ImmutableMap.Builder::combine,
         ImmutableMap.Builder::buildOrThrow
      );
   }

   static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      Preconditions.checkNotNull(mergeFunction);
      return Collectors.collectingAndThen(Collectors.toMap(keyFunction, valueFunction, mergeFunction, LinkedHashMap::new), ImmutableMap::copyOf);
   }

   static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toImmutableSortedMap(
      Comparator<? super K> comparator, Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(comparator);
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(
         () -> new ImmutableSortedMap.Builder<>(comparator),
         (builder, input) -> builder.put((K)keyFunction.apply(input), (V)valueFunction.apply(input)),
         ImmutableSortedMap.Builder::combine,
         ImmutableSortedMap.Builder::buildOrThrow,
         Characteristics.UNORDERED
      );
   }

   static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toImmutableSortedMap(
      Comparator<? super K> comparator,
      Function<? super T, ? extends K> keyFunction,
      Function<? super T, ? extends V> valueFunction,
      BinaryOperator<V> mergeFunction
   ) {
      Preconditions.checkNotNull(comparator);
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      Preconditions.checkNotNull(mergeFunction);
      return Collectors.collectingAndThen(
         Collectors.toMap(keyFunction, valueFunction, mergeFunction, () -> new TreeMap<>(comparator)), ImmutableSortedMap::copyOfSorted
      );
   }

   static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toImmutableBiMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(
         ImmutableBiMap.Builder::new,
         (builder, input) -> builder.put((K)keyFunction.apply(input), (V)valueFunction.apply(input)),
         ImmutableBiMap.Builder::combine,
         ImmutableBiMap.Builder::buildOrThrow
      );
   }

   static <T, K extends Enum<K>, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableEnumMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(() -> new CollectCollectors.EnumMapAccumulator((v1, v2) -> {
         throw new IllegalArgumentException("Multiple values for key: " + v1 + ", " + v2);
      }), (accum, t) -> {
         K key = (K)keyFunction.apply(t);
         V newValue = (V)valueFunction.apply(t);
         accum.put(Preconditions.checkNotNull(key, "Null key for input %s", t), Preconditions.checkNotNull(newValue, "Null value for input %s", t));
      }, CollectCollectors.EnumMapAccumulator::combine, CollectCollectors.EnumMapAccumulator::toImmutableMap, Characteristics.UNORDERED);
   }

   static <T, K extends Enum<K>, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableEnumMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      Preconditions.checkNotNull(mergeFunction);
      return Collector.of(() -> new CollectCollectors.EnumMapAccumulator<>(mergeFunction), (accum, t) -> {
         K key = (K)keyFunction.apply(t);
         V newValue = (V)valueFunction.apply(t);
         accum.put(Preconditions.checkNotNull(key, "Null key for input %s", t), Preconditions.checkNotNull(newValue, "Null value for input %s", t));
      }, CollectCollectors.EnumMapAccumulator::combine, CollectCollectors.EnumMapAccumulator::toImmutableMap);
   }

   @GwtIncompatible
   static <T, K extends Comparable<? super K>, V> Collector<T, ?, ImmutableRangeMap<K, V>> toImmutableRangeMap(
      Function<? super T, Range<K>> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      return Collector.of(
         ImmutableRangeMap::builder,
         (builder, input) -> builder.put(keyFunction.apply(input), (V)valueFunction.apply(input)),
         ImmutableRangeMap.Builder::combine,
         ImmutableRangeMap.Builder::build
      );
   }

   static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> toImmutableListMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction, "keyFunction");
      Preconditions.checkNotNull(valueFunction, "valueFunction");
      return Collector.of(
         ImmutableListMultimap::builder,
         (builder, t) -> builder.put((K)keyFunction.apply(t), (V)valueFunction.apply(t)),
         ImmutableListMultimap.Builder::combine,
         ImmutableListMultimap.Builder::build
      );
   }

   static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> flatteningToImmutableListMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends Stream<? extends V>> valuesFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valuesFunction);
      return Collectors.collectingAndThen(
         flatteningToMultimap(
            input -> Preconditions.checkNotNull(keyFunction.apply(input)),
            input -> valuesFunction.apply(input).peek(Preconditions::checkNotNull),
            MultimapBuilder.linkedHashKeys().arrayListValues()::build
         ),
         ImmutableListMultimap::copyOf
      );
   }

   static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> toImmutableSetMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      Preconditions.checkNotNull(keyFunction, "keyFunction");
      Preconditions.checkNotNull(valueFunction, "valueFunction");
      return Collector.of(
         ImmutableSetMultimap::builder,
         (builder, t) -> builder.put((K)keyFunction.apply(t), (V)valueFunction.apply(t)),
         ImmutableSetMultimap.Builder::combine,
         ImmutableSetMultimap.Builder::build
      );
   }

   static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> flatteningToImmutableSetMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends Stream<? extends V>> valuesFunction
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valuesFunction);
      return Collectors.collectingAndThen(
         flatteningToMultimap(
            input -> Preconditions.checkNotNull(keyFunction.apply(input)),
            input -> valuesFunction.apply(input).peek(Preconditions::checkNotNull),
            MultimapBuilder.linkedHashKeys().linkedHashSetValues()::build
         ),
         ImmutableSetMultimap::copyOf
      );
   }

   static <T, K, V, M extends Multimap<K, V>> Collector<T, ?, M> toMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, Supplier<M> multimapSupplier
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      Preconditions.checkNotNull(multimapSupplier);
      return Collector.of(
         multimapSupplier, (multimap, input) -> multimap.put((K)keyFunction.apply(input), (V)valueFunction.apply(input)), (multimap1, multimap2) -> {
            multimap1.putAll(multimap2);
            return multimap1;
         }
      );
   }

   static <T, K, V, M extends Multimap<K, V>> Collector<T, ?, M> flatteningToMultimap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends Stream<? extends V>> valueFunction, Supplier<M> multimapSupplier
   ) {
      Preconditions.checkNotNull(keyFunction);
      Preconditions.checkNotNull(valueFunction);
      Preconditions.checkNotNull(multimapSupplier);
      return Collector.of(multimapSupplier, (multimap, input) -> {
         K key = (K)keyFunction.apply(input);
         Collection<V> valuesForKey = multimap.get(key);
         valueFunction.apply(input).forEachOrdered(valuesForKey::add);
      }, (multimap1, multimap2) -> {
         multimap1.putAll(multimap2);
         return multimap1;
      });
   }

   private CollectCollectors() {
   }

   private static class EnumMapAccumulator<K extends Enum<K>, V> {
      private final BinaryOperator<V> mergeFunction;
      private @Nullable EnumMap<K, V> map = null;

      EnumMapAccumulator(BinaryOperator<V> mergeFunction) {
         this.mergeFunction = mergeFunction;
      }

      void put(K key, V value) {
         if (this.map == null) {
            this.map = new EnumMap<>(Collections.singletonMap(key, value));
         } else {
            this.map.merge(key, value, this.mergeFunction);
         }
      }

      CollectCollectors.EnumMapAccumulator<K, V> combine(CollectCollectors.EnumMapAccumulator<K, V> other) {
         if (this.map == null) {
            return other;
         }

         if (other.map == null) {
            return this;
         }

         other.map.forEach(this::put);
         return this;
      }

      ImmutableMap<K, V> toImmutableMap() {
         return this.map == null ? ImmutableMap.of() : ImmutableEnumMap.asImmutable(this.map);
      }
   }

   private static final class EnumSetAccumulator<E extends Enum<E>> {
      static final Collector<Enum<?>, ?, ImmutableSet<? extends Enum<?>>> TO_IMMUTABLE_ENUM_SET = (Collector<Enum<?>, ?, ImmutableSet<? extends Enum<?>>>)CollectCollectors.toImmutableEnumSetGeneric();
      private @Nullable EnumSet<E> set;

      private EnumSetAccumulator() {
      }

      void add(E e) {
         if (this.set == null) {
            this.set = EnumSet.of(e);
         } else {
            this.set.add(e);
         }
      }

      CollectCollectors.EnumSetAccumulator<E> combine(CollectCollectors.EnumSetAccumulator<E> other) {
         if (this.set == null) {
            return other;
         }

         if (other.set == null) {
            return this;
         }

         this.set.addAll(other.set);
         return this;
      }

      ImmutableSet<E> toImmutableSet() {
         if (this.set == null) {
            return ImmutableSet.of();
         }

         ImmutableSet<E> ret = ImmutableEnumSet.asImmutable(this.set);
         this.set = null;
         return ret;
      }
   }
}
