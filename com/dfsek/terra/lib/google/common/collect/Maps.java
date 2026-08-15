package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Converter;
import com.dfsek.terra.lib.google.common.base.Equivalence;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.dfsek.terra.lib.google.common.base.Predicates;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import com.google.j2objc.annotations.Weak;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class Maps {
   private Maps() {
   }

   static <K> com.dfsek.terra.lib.google.common.base.Function<Entry<K, ?>, K> keyFunction() {
      return Maps.EntryFunction.KEY;
   }

   static <V> com.dfsek.terra.lib.google.common.base.Function<Entry<?, V>, V> valueFunction() {
      return Maps.EntryFunction.VALUE;
   }

   static <K, V> Iterator<K> keyIterator(Iterator<Entry<K, V>> entryIterator) {
      return new TransformedIterator<Entry<K, V>, K>(entryIterator) {
         @ParametricNullness
         K transform(Entry<K, V> entry) {
            return entry.getKey();
         }
      };
   }

   static <K, V> Iterator<V> valueIterator(Iterator<Entry<K, V>> entryIterator) {
      return new TransformedIterator<Entry<K, V>, V>(entryIterator) {
         @ParametricNullness
         V transform(Entry<K, V> entry) {
            return entry.getValue();
         }
      };
   }

   @GwtCompatible(serializable = true)
   public static <K extends Enum<K>, V> ImmutableMap<K, V> immutableEnumMap(Map<K, ? extends V> map) {
      if (map instanceof ImmutableEnumMap) {
         return (ImmutableEnumMap)map;
      }

      Iterator<? extends Entry<K, ? extends V>> entryItr = map.entrySet().iterator();
      if (!entryItr.hasNext()) {
         return ImmutableMap.of();
      }

      Entry<K, ? extends V> entry1 = (Entry<K, ? extends V>)entryItr.next();
      K key1 = (K)entry1.getKey();
      V value1 = (V)entry1.getValue();
      CollectPreconditions.checkEntryNotNull(key1, value1);
      EnumMap<K, V> enumMap = new EnumMap<>(Collections.singletonMap(key1, value1));

      while (entryItr.hasNext()) {
         Entry<K, ? extends V> entry = (Entry<K, ? extends V>)entryItr.next();
         K key = (K)entry.getKey();
         V value = (V)entry.getValue();
         CollectPreconditions.checkEntryNotNull(key, value);
         enumMap.put(key, value);
      }

      return ImmutableEnumMap.asImmutable(enumMap);
   }

   public static <T, K extends Enum<K>, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableEnumMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction
   ) {
      return CollectCollectors.toImmutableEnumMap(keyFunction, valueFunction);
   }

   public static <T, K extends Enum<K>, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableEnumMap(
      Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction
   ) {
      return CollectCollectors.toImmutableEnumMap(keyFunction, valueFunction, mergeFunction);
   }

   public static <K, V> HashMap<K, V> newHashMap() {
      return new HashMap<>();
   }

   public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> map) {
      return new HashMap<>(map);
   }

   public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize) {
      return new HashMap<>(capacity(expectedSize));
   }

   static int capacity(int expectedSize) {
      if (expectedSize < 3) {
         CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
         return expectedSize + 1;
      } else {
         return expectedSize < 1073741824 ? (int)Math.ceil(expectedSize / 0.75) : Integer.MAX_VALUE;
      }
   }

   public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
      return new LinkedHashMap<>();
   }

   public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<? extends K, ? extends V> map) {
      return new LinkedHashMap<>(map);
   }

   public static <K, V> LinkedHashMap<K, V> newLinkedHashMapWithExpectedSize(int expectedSize) {
      return new LinkedHashMap<>(capacity(expectedSize));
   }

   public static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
      return new ConcurrentHashMap<>();
   }

   public static <K extends Comparable, V> TreeMap<K, V> newTreeMap() {
      return new TreeMap<>();
   }

   public static <K, V> TreeMap<K, V> newTreeMap(SortedMap<K, ? extends V> map) {
      return new TreeMap<>(map);
   }

   public static <C, K extends C, V> TreeMap<K, V> newTreeMap(@Nullable Comparator<C> comparator) {
      return new TreeMap<>(comparator);
   }

   public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap(Class<K> type) {
      return new EnumMap<>(Preconditions.checkNotNull(type));
   }

   public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap(Map<K, ? extends V> map) {
      return new EnumMap<>(map);
   }

   public static <K, V> IdentityHashMap<K, V> newIdentityHashMap() {
      return new IdentityHashMap<>();
   }

   public static <K, V> MapDifference<K, V> difference(Map<? extends K, ? extends V> left, Map<? extends K, ? extends V> right) {
      if (left instanceof SortedMap) {
         SortedMap<K, ? extends V> sortedLeft = (SortedMap<K, ? extends V>)left;
         return difference(sortedLeft, right);
      } else {
         return difference(left, right, Equivalence.equals());
      }
   }

   public static <K, V> MapDifference<K, V> difference(
      Map<? extends K, ? extends V> left, Map<? extends K, ? extends V> right, Equivalence<? super @NonNull V> valueEquivalence
   ) {
      Preconditions.checkNotNull(valueEquivalence);
      Map<K, V> onlyOnLeft = newLinkedHashMap();
      Map<K, V> onlyOnRight = new LinkedHashMap<>(right);
      Map<K, V> onBoth = newLinkedHashMap();
      Map<K, MapDifference.ValueDifference<V>> differences = newLinkedHashMap();
      doDifference(left, right, valueEquivalence, onlyOnLeft, onlyOnRight, onBoth, differences);
      return new Maps.MapDifferenceImpl<>(onlyOnLeft, onlyOnRight, onBoth, differences);
   }

   public static <K, V> SortedMapDifference<K, V> difference(SortedMap<K, ? extends V> left, Map<? extends K, ? extends V> right) {
      Preconditions.checkNotNull(left);
      Preconditions.checkNotNull(right);
      Comparator<? super K> comparator = orNaturalOrder(left.comparator());
      SortedMap<K, V> onlyOnLeft = newTreeMap(comparator);
      SortedMap<K, V> onlyOnRight = newTreeMap(comparator);
      onlyOnRight.putAll(right);
      SortedMap<K, V> onBoth = newTreeMap(comparator);
      SortedMap<K, MapDifference.ValueDifference<V>> differences = newTreeMap(comparator);
      doDifference(left, right, Equivalence.equals(), onlyOnLeft, onlyOnRight, onBoth, differences);
      return new Maps.SortedMapDifferenceImpl<>(onlyOnLeft, onlyOnRight, onBoth, differences);
   }

   private static <K, V> void doDifference(
      Map<? extends K, ? extends V> left,
      Map<? extends K, ? extends V> right,
      Equivalence<? super @NonNull V> valueEquivalence,
      Map<K, V> onlyOnLeft,
      Map<K, V> onlyOnRight,
      Map<K, V> onBoth,
      Map<K, MapDifference.ValueDifference<V>> differences
   ) {
      for (Entry<? extends K, ? extends V> entry : left.entrySet()) {
         K leftKey = (K)entry.getKey();
         V leftValue = (V)entry.getValue();
         if (right.containsKey(leftKey)) {
            V rightValue = NullnessCasts.uncheckedCastNullableTToT(onlyOnRight.remove(leftKey));
            if (valueEquivalence.equivalent(leftValue, rightValue)) {
               onBoth.put(leftKey, leftValue);
            } else {
               differences.put(leftKey, Maps.ValueDifferenceImpl.create(leftValue, rightValue));
            }
         } else {
            onlyOnLeft.put(leftKey, leftValue);
         }
      }
   }

   private static <K, V> Map<K, V> unmodifiableMap(Map<K, ? extends V> map) {
      return map instanceof SortedMap ? Collections.unmodifiableSortedMap((SortedMap<K, ? extends V>)map) : Collections.unmodifiableMap(map);
   }

   static <E> Comparator<? super E> orNaturalOrder(@Nullable Comparator<? super E> comparator) {
      return comparator != null ? comparator : Ordering.natural();
   }

   public static <K, V> Map<K, V> asMap(Set<K> set, com.dfsek.terra.lib.google.common.base.Function<? super K, V> function) {
      return new Maps.AsMapView<>(set, function);
   }

   public static <K, V> SortedMap<K, V> asMap(SortedSet<K> set, com.dfsek.terra.lib.google.common.base.Function<? super K, V> function) {
      return new Maps.SortedAsMapView<>(set, function);
   }

   @GwtIncompatible
   public static <K, V> NavigableMap<K, V> asMap(NavigableSet<K> set, com.dfsek.terra.lib.google.common.base.Function<? super K, V> function) {
      return new Maps.NavigableAsMapView<>(set, function);
   }

   static <K, V> Iterator<Entry<K, V>> asMapEntryIterator(Set<K> set, com.dfsek.terra.lib.google.common.base.Function<? super K, V> function) {
      return new TransformedIterator<K, Entry<K, V>>(set.iterator()) {
         Entry<K, V> transform(@ParametricNullness K key) {
            return Maps.immutableEntry(key, function.apply(key));
         }
      };
   }

   private static <E> Set<E> removeOnlySet(Set<E> set) {
      return new ForwardingSet<E>() {
         @Override
         protected Set<E> delegate() {
            return set;
         }

         @Override
         public boolean add(@ParametricNullness E element) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean addAll(Collection<? extends E> es) {
            throw new UnsupportedOperationException();
         }
      };
   }

   private static <E> SortedSet<E> removeOnlySortedSet(SortedSet<E> set) {
      return new ForwardingSortedSet<E>() {
         @Override
         protected SortedSet<E> delegate() {
            return set;
         }

         @Override
         public boolean add(@ParametricNullness E element) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean addAll(Collection<? extends E> es) {
            throw new UnsupportedOperationException();
         }

         @Override
         public SortedSet<E> headSet(@ParametricNullness E toElement) {
            return Maps.removeOnlySortedSet(super.headSet(toElement));
         }

         @Override
         public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
            return Maps.removeOnlySortedSet(super.subSet(fromElement, toElement));
         }

         @Override
         public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
            return Maps.removeOnlySortedSet(super.tailSet(fromElement));
         }
      };
   }

   @GwtIncompatible
   private static <E> NavigableSet<E> removeOnlyNavigableSet(NavigableSet<E> set) {
      return new ForwardingNavigableSet<E>() {
         @Override
         protected NavigableSet<E> delegate() {
            return set;
         }

         @Override
         public boolean add(@ParametricNullness E element) {
            throw new UnsupportedOperationException();
         }

         @Override
         public boolean addAll(Collection<? extends E> es) {
            throw new UnsupportedOperationException();
         }

         @Override
         public SortedSet<E> headSet(@ParametricNullness E toElement) {
            return Maps.removeOnlySortedSet(super.headSet(toElement));
         }

         @Override
         public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
            return Maps.removeOnlyNavigableSet(super.headSet(toElement, inclusive));
         }

         @Override
         public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
            return Maps.removeOnlySortedSet(super.subSet(fromElement, toElement));
         }

         @Override
         public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
            return Maps.removeOnlyNavigableSet(super.subSet(fromElement, fromInclusive, toElement, toInclusive));
         }

         @Override
         public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
            return Maps.removeOnlySortedSet(super.tailSet(fromElement));
         }

         @Override
         public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
            return Maps.removeOnlyNavigableSet(super.tailSet(fromElement, inclusive));
         }

         @Override
         public NavigableSet<E> descendingSet() {
            return Maps.removeOnlyNavigableSet(super.descendingSet());
         }
      };
   }

   public static <K, V> ImmutableMap<K, V> toMap(Iterable<K> keys, com.dfsek.terra.lib.google.common.base.Function<? super K, V> valueFunction) {
      return toMap(keys.iterator(), valueFunction);
   }

   public static <K, V> ImmutableMap<K, V> toMap(Iterator<K> keys, com.dfsek.terra.lib.google.common.base.Function<? super K, V> valueFunction) {
      Preconditions.checkNotNull(valueFunction);
      ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();

      while (keys.hasNext()) {
         K key = keys.next();
         builder.put(key, valueFunction.apply(key));
      }

      return builder.buildKeepingLast();
   }

   @CanIgnoreReturnValue
   public static <K, V> ImmutableMap<K, V> uniqueIndex(Iterable<V> values, com.dfsek.terra.lib.google.common.base.Function<? super V, K> keyFunction) {
      return values instanceof Collection
         ? uniqueIndex(values.iterator(), keyFunction, ImmutableMap.builderWithExpectedSize(((Collection)values).size()))
         : uniqueIndex(values.iterator(), keyFunction);
   }

   @CanIgnoreReturnValue
   public static <K, V> ImmutableMap<K, V> uniqueIndex(Iterator<V> values, com.dfsek.terra.lib.google.common.base.Function<? super V, K> keyFunction) {
      return uniqueIndex(values, keyFunction, ImmutableMap.builder());
   }

   private static <K, V> ImmutableMap<K, V> uniqueIndex(
      Iterator<V> values, com.dfsek.terra.lib.google.common.base.Function<? super V, K> keyFunction, ImmutableMap.Builder<K, V> builder
   ) {
      Preconditions.checkNotNull(keyFunction);

      while (values.hasNext()) {
         V value = values.next();
         builder.put(keyFunction.apply(value), value);
      }

      try {
         return builder.buildOrThrow();
      } catch (IllegalArgumentException duplicateKeys) {
         throw new IllegalArgumentException(duplicateKeys.getMessage() + ". To index multiple values under a key, use Multimaps.index.");
      }
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static ImmutableMap<String, String> fromProperties(Properties properties) {
      ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
      Enumeration<?> e = properties.propertyNames();

      while (e.hasMoreElements()) {
         String key = Objects.requireNonNull((String)e.nextElement());
         builder.put(key, Objects.requireNonNull(properties.getProperty(key)));
      }

      return builder.buildOrThrow();
   }

   @GwtCompatible(serializable = true)
   public static <K, V> Entry<K, V> immutableEntry(@ParametricNullness K key, @ParametricNullness V value) {
      return new ImmutableEntry<>(key, value);
   }

   static <K, V> Set<Entry<K, V>> unmodifiableEntrySet(Set<Entry<K, V>> entrySet) {
      return new Maps.UnmodifiableEntrySet<>(Collections.unmodifiableSet(entrySet));
   }

   static <K, V> Entry<K, V> unmodifiableEntry(Entry<? extends K, ? extends V> entry) {
      Preconditions.checkNotNull(entry);
      return new AbstractMapEntry<K, V>() {
         @ParametricNullness
         @Override
         public K getKey() {
            return (K)entry.getKey();
         }

         @ParametricNullness
         @Override
         public V getValue() {
            return (V)entry.getValue();
         }
      };
   }

   static <K, V> UnmodifiableIterator<Entry<K, V>> unmodifiableEntryIterator(Iterator<Entry<K, V>> entryIterator) {
      return new UnmodifiableIterator<Entry<K, V>>() {
         @Override
         public boolean hasNext() {
            return entryIterator.hasNext();
         }

         public Entry<K, V> next() {
            return Maps.unmodifiableEntry(entryIterator.next());
         }
      };
   }

   public static <A, B> Converter<A, B> asConverter(BiMap<A, B> bimap) {
      return new Maps.BiMapConverter<>(bimap);
   }

   @J2ktIncompatible
   public static <K, V> BiMap<K, V> synchronizedBiMap(BiMap<K, V> bimap) {
      return Synchronized.biMap(bimap, null);
   }

   public static <K, V> BiMap<K, V> unmodifiableBiMap(BiMap<? extends K, ? extends V> bimap) {
      return new Maps.UnmodifiableBiMap<>(bimap, null);
   }

   public static <K, V1, V2> Map<K, V2> transformValues(Map<K, V1> fromMap, com.dfsek.terra.lib.google.common.base.Function<? super V1, V2> function) {
      Preconditions.checkNotNull(function);
      return transformEntries(fromMap, (key, value) -> function.apply(value));
   }

   public static <K, V1, V2> SortedMap<K, V2> transformValues(
      SortedMap<K, V1> fromMap, com.dfsek.terra.lib.google.common.base.Function<? super V1, V2> function
   ) {
      Preconditions.checkNotNull(function);
      return transformEntries(fromMap, (key, value) -> function.apply(value));
   }

   @GwtIncompatible
   public static <K, V1, V2> NavigableMap<K, V2> transformValues(
      NavigableMap<K, V1> fromMap, com.dfsek.terra.lib.google.common.base.Function<? super V1, V2> function
   ) {
      Preconditions.checkNotNull(function);
      return transformEntries(fromMap, (key, value) -> function.apply(value));
   }

   public static <K, V1, V2> Map<K, V2> transformEntries(Map<K, V1> fromMap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
      return new Maps.TransformedEntriesMap<>(fromMap, transformer);
   }

   public static <K, V1, V2> SortedMap<K, V2> transformEntries(SortedMap<K, V1> fromMap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
      return new Maps.TransformedEntriesSortedMap<>(fromMap, transformer);
   }

   @GwtIncompatible
   public static <K, V1, V2> NavigableMap<K, V2> transformEntries(NavigableMap<K, V1> fromMap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
      return new Maps.TransformedEntriesNavigableMap<>(fromMap, transformer);
   }

   static <V2, K, V1> Entry<K, V2> transformEntry(Maps.EntryTransformer<? super K, ? super V1, V2> transformer, Entry<K, V1> entry) {
      Preconditions.checkNotNull(transformer);
      Preconditions.checkNotNull(entry);
      return new AbstractMapEntry<K, V2>() {
         @ParametricNullness
         @Override
         public K getKey() {
            return entry.getKey();
         }

         @ParametricNullness
         @Override
         public V2 getValue() {
            return transformer.transformEntry(entry.getKey(), entry.getValue());
         }
      };
   }

   static <K, V1, V2> com.dfsek.terra.lib.google.common.base.Function<Entry<K, V1>, Entry<K, V2>> asEntryToEntryFunction(
      Maps.EntryTransformer<? super K, ? super V1, V2> transformer
   ) {
      Preconditions.checkNotNull(transformer);
      return entry -> transformEntry(transformer, entry);
   }

   static <K> Predicate<Entry<K, ?>> keyPredicateOnEntries(Predicate<? super K> keyPredicate) {
      return Predicates.compose(keyPredicate, keyFunction());
   }

   static <V> Predicate<Entry<?, V>> valuePredicateOnEntries(Predicate<? super V> valuePredicate) {
      return Predicates.compose(valuePredicate, valueFunction());
   }

   public static <K, V> Map<K, V> filterKeys(Map<K, V> unfiltered, Predicate<? super K> keyPredicate) {
      Preconditions.checkNotNull(keyPredicate);
      Predicate<Entry<K, ?>> entryPredicate = keyPredicateOnEntries(keyPredicate);
      return unfiltered instanceof Maps.AbstractFilteredMap
         ? filterFiltered((Maps.AbstractFilteredMap<K, V>)unfiltered, entryPredicate)
         : new Maps.FilteredKeyMap<>(Preconditions.checkNotNull(unfiltered), keyPredicate, entryPredicate);
   }

   public static <K, V> SortedMap<K, V> filterKeys(SortedMap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
      return filterEntries(unfiltered, keyPredicateOnEntries(keyPredicate));
   }

   @GwtIncompatible
   public static <K, V> NavigableMap<K, V> filterKeys(NavigableMap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
      return filterEntries(unfiltered, keyPredicateOnEntries(keyPredicate));
   }

   public static <K, V> BiMap<K, V> filterKeys(BiMap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
      Preconditions.checkNotNull(keyPredicate);
      return filterEntries(unfiltered, keyPredicateOnEntries(keyPredicate));
   }

   public static <K, V> Map<K, V> filterValues(Map<K, V> unfiltered, Predicate<? super V> valuePredicate) {
      return filterEntries(unfiltered, valuePredicateOnEntries(valuePredicate));
   }

   public static <K, V> SortedMap<K, V> filterValues(SortedMap<K, V> unfiltered, Predicate<? super V> valuePredicate) {
      return filterEntries(unfiltered, valuePredicateOnEntries(valuePredicate));
   }

   @GwtIncompatible
   public static <K, V> NavigableMap<K, V> filterValues(NavigableMap<K, V> unfiltered, Predicate<? super V> valuePredicate) {
      return filterEntries(unfiltered, valuePredicateOnEntries(valuePredicate));
   }

   public static <K, V> BiMap<K, V> filterValues(BiMap<K, V> unfiltered, Predicate<? super V> valuePredicate) {
      return filterEntries(unfiltered, valuePredicateOnEntries(valuePredicate));
   }

   public static <K, V> Map<K, V> filterEntries(Map<K, V> unfiltered, Predicate<? super Entry<K, V>> entryPredicate) {
      Preconditions.checkNotNull(entryPredicate);
      return unfiltered instanceof Maps.AbstractFilteredMap
         ? filterFiltered((Maps.AbstractFilteredMap<K, V>)unfiltered, entryPredicate)
         : new Maps.FilteredEntryMap<>(Preconditions.checkNotNull(unfiltered), entryPredicate);
   }

   public static <K, V> SortedMap<K, V> filterEntries(SortedMap<K, V> unfiltered, Predicate<? super Entry<K, V>> entryPredicate) {
      Preconditions.checkNotNull(entryPredicate);
      return unfiltered instanceof Maps.FilteredEntrySortedMap
         ? filterFiltered((Maps.FilteredEntrySortedMap<K, V>)unfiltered, entryPredicate)
         : new Maps.FilteredEntrySortedMap<>(Preconditions.checkNotNull(unfiltered), entryPredicate);
   }

   @GwtIncompatible
   public static <K, V> NavigableMap<K, V> filterEntries(NavigableMap<K, V> unfiltered, Predicate<? super Entry<K, V>> entryPredicate) {
      Preconditions.checkNotNull(entryPredicate);
      return unfiltered instanceof Maps.FilteredEntryNavigableMap
         ? filterFiltered((Maps.FilteredEntryNavigableMap<K, V>)unfiltered, entryPredicate)
         : new Maps.FilteredEntryNavigableMap<>(Preconditions.checkNotNull(unfiltered), entryPredicate);
   }

   public static <K, V> BiMap<K, V> filterEntries(BiMap<K, V> unfiltered, Predicate<? super Entry<K, V>> entryPredicate) {
      Preconditions.checkNotNull(unfiltered);
      Preconditions.checkNotNull(entryPredicate);
      return unfiltered instanceof Maps.FilteredEntryBiMap
         ? filterFiltered((Maps.FilteredEntryBiMap<K, V>)unfiltered, entryPredicate)
         : new Maps.FilteredEntryBiMap<>(unfiltered, entryPredicate);
   }

   private static <K, V> Map<K, V> filterFiltered(Maps.AbstractFilteredMap<K, V> map, Predicate<? super Entry<K, V>> entryPredicate) {
      return new Maps.FilteredEntryMap<>(map.unfiltered, Predicates.and(map.predicate, entryPredicate));
   }

   private static <K, V> SortedMap<K, V> filterFiltered(Maps.FilteredEntrySortedMap<K, V> map, Predicate<? super Entry<K, V>> entryPredicate) {
      Predicate<Entry<K, V>> predicate = Predicates.and(map.predicate, entryPredicate);
      return new Maps.FilteredEntrySortedMap<>(map.sortedMap(), predicate);
   }

   @GwtIncompatible
   private static <K, V> NavigableMap<K, V> filterFiltered(Maps.FilteredEntryNavigableMap<K, V> map, Predicate<? super Entry<K, V>> entryPredicate) {
      Predicate<Entry<K, V>> predicate = Predicates.and(map.entryPredicate, entryPredicate);
      return new Maps.FilteredEntryNavigableMap<>(map.unfiltered, predicate);
   }

   private static <K, V> BiMap<K, V> filterFiltered(Maps.FilteredEntryBiMap<K, V> map, Predicate<? super Entry<K, V>> entryPredicate) {
      Predicate<Entry<K, V>> predicate = Predicates.and(map.predicate, entryPredicate);
      return new Maps.FilteredEntryBiMap<>(map.unfiltered(), predicate);
   }

   @GwtIncompatible
   public static <K, V> NavigableMap<K, V> unmodifiableNavigableMap(NavigableMap<K, ? extends V> map) {
      Preconditions.checkNotNull(map);
      return map instanceof Maps.UnmodifiableNavigableMap ? map : new Maps.UnmodifiableNavigableMap<>(map);
   }

   private static <K, V> @Nullable Entry<K, V> unmodifiableOrNull(@Nullable Entry<K, ? extends V> entry) {
      return entry == null ? null : unmodifiableEntry(entry);
   }

   @GwtIncompatible
   @J2ktIncompatible
   public static <K, V> NavigableMap<K, V> synchronizedNavigableMap(NavigableMap<K, V> navigableMap) {
      return Synchronized.navigableMap(navigableMap);
   }

   static <V> @Nullable V safeGet(Map<?, V> map, @Nullable Object key) {
      Preconditions.checkNotNull(map);

      try {
         return map.get(key);
      } catch (ClassCastException | NullPointerException e) {
         return null;
      }
   }

   static boolean safeContainsKey(Map<?, ?> map, @Nullable Object key) {
      Preconditions.checkNotNull(map);

      try {
         return map.containsKey(key);
      } catch (ClassCastException | NullPointerException e) {
         return false;
      }
   }

   static <V> @Nullable V safeRemove(Map<?, V> map, @Nullable Object key) {
      Preconditions.checkNotNull(map);

      try {
         return map.remove(key);
      } catch (ClassCastException | NullPointerException e) {
         return null;
      }
   }

   static boolean containsKeyImpl(Map<?, ?> map, @Nullable Object key) {
      return Iterators.contains(keyIterator(map.entrySet().iterator()), key);
   }

   static boolean containsValueImpl(Map<?, ?> map, @Nullable Object value) {
      return Iterators.contains(valueIterator(map.entrySet().iterator()), value);
   }

   static <K, V> boolean containsEntryImpl(Collection<Entry<K, V>> c, @Nullable Object o) {
      return !(o instanceof Entry) ? false : c.contains(unmodifiableEntry((Entry<? extends K, ? extends V>)o));
   }

   static <K, V> boolean removeEntryImpl(Collection<Entry<K, V>> c, @Nullable Object o) {
      return !(o instanceof Entry) ? false : c.remove(unmodifiableEntry((Entry<? extends K, ? extends V>)o));
   }

   static boolean equalsImpl(Map<?, ?> map, @Nullable Object object) {
      if (map == object) {
         return true;
      } else if (object instanceof Map) {
         Map<?, ?> o = (Map<?, ?>)object;
         return map.entrySet().equals(o.entrySet());
      } else {
         return false;
      }
   }

   static String toStringImpl(Map<?, ?> map) {
      StringBuilder sb = Collections2.newStringBuilderForCollection(map.size()).append('{');
      boolean first = true;

      for (Entry<?, ?> entry : map.entrySet()) {
         if (!first) {
            sb.append(", ");
         }

         first = false;
         sb.append(entry.getKey()).append('=').append(entry.getValue());
      }

      return sb.append('}').toString();
   }

   static <K, V> void putAllImpl(Map<K, V> self, Map<? extends K, ? extends V> map) {
      for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
         self.put((K)entry.getKey(), (V)entry.getValue());
      }
   }

   static <K> @Nullable K keyOrNull(@Nullable Entry<K, ?> entry) {
      return entry == null ? null : entry.getKey();
   }

   static <V> @Nullable V valueOrNull(@Nullable Entry<?, V> entry) {
      return entry == null ? null : entry.getValue();
   }

   static <E> ImmutableMap<E, Integer> indexMap(Collection<E> list) {
      ImmutableMap.Builder<E, Integer> builder = new ImmutableMap.Builder<>(list.size());
      int i = 0;

      for (E e : list) {
         builder.put(e, i++);
      }

      return builder.buildOrThrow();
   }

   @GwtIncompatible
   public static <K extends Comparable<? super K>, V> NavigableMap<K, V> subMap(NavigableMap<K, V> map, Range<K> range) {
      if (map.comparator() != null && map.comparator() != Ordering.natural() && range.hasLowerBound() && range.hasUpperBound()) {
         Preconditions.checkArgument(
            map.comparator().compare(range.lowerEndpoint(), range.upperEndpoint()) <= 0,
            "map is using a custom comparator which is inconsistent with the natural ordering."
         );
      }

      if (range.hasLowerBound() && range.hasUpperBound()) {
         return map.subMap(range.lowerEndpoint(), range.lowerBoundType() == BoundType.CLOSED, range.upperEndpoint(), range.upperBoundType() == BoundType.CLOSED);
      } else if (range.hasLowerBound()) {
         return map.tailMap(range.lowerEndpoint(), range.lowerBoundType() == BoundType.CLOSED);
      } else {
         return range.hasUpperBound() ? map.headMap(range.upperEndpoint(), range.upperBoundType() == BoundType.CLOSED) : Preconditions.checkNotNull(map);
      }
   }

   private abstract static class AbstractFilteredMap<K, V> extends Maps.ViewCachingAbstractMap<K, V> {
      final Map<K, V> unfiltered;
      final Predicate<? super Entry<K, V>> predicate;

      AbstractFilteredMap(Map<K, V> unfiltered, Predicate<? super Entry<K, V>> predicate) {
         this.unfiltered = unfiltered;
         this.predicate = predicate;
      }

      boolean apply(@Nullable Object key, @ParametricNullness V value) {
         K k = (K)key;
         return this.predicate.apply(Maps.immutableEntry(k, value));
      }

      @Override
      public @Nullable V put(@ParametricNullness K key, @ParametricNullness V value) {
         Preconditions.checkArgument(this.apply(key, value));
         return this.unfiltered.put(key, value);
      }

      @Override
      public void putAll(Map<? extends K, ? extends V> map) {
         for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            Preconditions.checkArgument(this.apply(entry.getKey(), (V)entry.getValue()));
         }

         this.unfiltered.putAll(map);
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return this.unfiltered.containsKey(key) && this.apply(key, this.unfiltered.get(key));
      }

      @Override
      public @Nullable V get(@Nullable Object key) {
         V value = this.unfiltered.get(key);
         return value != null && this.apply(key, value) ? value : null;
      }

      @Override
      public boolean isEmpty() {
         return this.entrySet().isEmpty();
      }

      @Override
      public @Nullable V remove(@Nullable Object key) {
         return this.containsKey(key) ? this.unfiltered.remove(key) : null;
      }

      @Override
      Collection<V> createValues() {
         return new Maps.FilteredMapValues<>(this, this.unfiltered, this.predicate);
      }
   }

   private static class AsMapView<K, V> extends Maps.ViewCachingAbstractMap<K, V> {
      private final Set<K> set;
      final com.dfsek.terra.lib.google.common.base.Function<? super K, V> function;

      Set<K> backingSet() {
         return this.set;
      }

      AsMapView(Set<K> set, com.dfsek.terra.lib.google.common.base.Function<? super K, V> function) {
         this.set = Preconditions.checkNotNull(set);
         this.function = Preconditions.checkNotNull(function);
      }

      @Override
      public Set<K> createKeySet() {
         return Maps.removeOnlySet(this.backingSet());
      }

      @Override
      Collection<V> createValues() {
         return Collections2.transform(this.set, this.function);
      }

      @Override
      public int size() {
         return this.backingSet().size();
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return this.backingSet().contains(key);
      }

      @Override
      public @Nullable V get(@Nullable Object key) {
         return this.getOrDefault(key, null);
      }

      @Override
      public @Nullable V getOrDefault(@Nullable Object key, @Nullable V defaultValue) {
         if (Collections2.safeContains(this.backingSet(), key)) {
            K k = (K)key;
            return this.function.apply(k);
         } else {
            return defaultValue;
         }
      }

      @Override
      public @Nullable V remove(@Nullable Object key) {
         if (this.backingSet().remove(key)) {
            K k = (K)key;
            return this.function.apply(k);
         } else {
            return null;
         }
      }

      @Override
      public void clear() {
         this.backingSet().clear();
      }

      @Override
      protected Set<Entry<K, V>> createEntrySet() {
         class EntrySetImpl extends Maps.EntrySet<K, V> {
            @Override
            Map<K, V> map() {
               return AsMapView.this;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
               return Maps.asMapEntryIterator(AsMapView.this.backingSet(), AsMapView.this.function);
            }
         }

         return new EntrySetImpl();
      }

      @Override
      public void forEach(BiConsumer<? super K, ? super V> action) {
         Preconditions.checkNotNull(action);
         this.backingSet().forEach(k -> action.accept(k, this.function.apply(k)));
      }
   }

   private static final class BiMapConverter<A, B> extends Converter<A, B> implements Serializable {
      private final BiMap<A, B> bimap;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      BiMapConverter(BiMap<A, B> bimap) {
         this.bimap = Preconditions.checkNotNull(bimap);
      }

      @Override
      protected B doForward(A a) {
         return convert(this.bimap, a);
      }

      @Override
      protected A doBackward(B b) {
         return convert(this.bimap.inverse(), b);
      }

      private static <X, Y> Y convert(BiMap<X, Y> bimap, X input) {
         Y output = bimap.get(input);
         Preconditions.checkArgument(output != null, "No non-null mapping present for input: %s", input);
         return output;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (object instanceof Maps.BiMapConverter) {
            Maps.BiMapConverter<?, ?> that = (Maps.BiMapConverter<?, ?>)object;
            return this.bimap.equals(that.bimap);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.bimap.hashCode();
      }

      @Override
      public String toString() {
         return "Maps.asConverter(" + this.bimap + ")";
      }
   }

   @GwtIncompatible
   abstract static class DescendingMap<K, V> extends ForwardingMap<K, V> implements NavigableMap<K, V> {
      @LazyInit
      private transient @Nullable Comparator<? super K> comparator;
      @LazyInit
      private transient @Nullable Set<Entry<K, V>> entrySet;
      @LazyInit
      private transient @Nullable NavigableSet<K> navigableKeySet;

      abstract NavigableMap<K, V> forward();

      @Override
      protected final Map<K, V> delegate() {
         return this.forward();
      }

      @Override
      public Comparator<? super K> comparator() {
         Comparator<? super K> result = this.comparator;
         if (result == null) {
            Comparator<? super K> forwardCmp = this.forward().comparator();
            if (forwardCmp == null) {
               forwardCmp = Ordering.natural();
            }

            result = this.comparator = reverse(forwardCmp);
         }

         return result;
      }

      private static <T> Ordering<T> reverse(Comparator<T> forward) {
         return Ordering.from(forward).reverse();
      }

      @ParametricNullness
      @Override
      public K firstKey() {
         return this.forward().lastKey();
      }

      @ParametricNullness
      @Override
      public K lastKey() {
         return this.forward().firstKey();
      }

      @Override
      public @Nullable Entry<K, V> lowerEntry(@ParametricNullness K key) {
         return this.forward().higherEntry(key);
      }

      @Override
      public @Nullable K lowerKey(@ParametricNullness K key) {
         return this.forward().higherKey(key);
      }

      @Override
      public @Nullable Entry<K, V> floorEntry(@ParametricNullness K key) {
         return this.forward().ceilingEntry(key);
      }

      @Override
      public @Nullable K floorKey(@ParametricNullness K key) {
         return this.forward().ceilingKey(key);
      }

      @Override
      public @Nullable Entry<K, V> ceilingEntry(@ParametricNullness K key) {
         return this.forward().floorEntry(key);
      }

      @Override
      public @Nullable K ceilingKey(@ParametricNullness K key) {
         return this.forward().floorKey(key);
      }

      @Override
      public @Nullable Entry<K, V> higherEntry(@ParametricNullness K key) {
         return this.forward().lowerEntry(key);
      }

      @Override
      public @Nullable K higherKey(@ParametricNullness K key) {
         return this.forward().lowerKey(key);
      }

      @Override
      public @Nullable Entry<K, V> firstEntry() {
         return this.forward().lastEntry();
      }

      @Override
      public @Nullable Entry<K, V> lastEntry() {
         return this.forward().firstEntry();
      }

      @Override
      public @Nullable Entry<K, V> pollFirstEntry() {
         return this.forward().pollLastEntry();
      }

      @Override
      public @Nullable Entry<K, V> pollLastEntry() {
         return this.forward().pollFirstEntry();
      }

      @Override
      public NavigableMap<K, V> descendingMap() {
         return this.forward();
      }

      @Override
      public Set<Entry<K, V>> entrySet() {
         Set<Entry<K, V>> result = this.entrySet;
         return result == null ? (this.entrySet = this.createEntrySet()) : result;
      }

      abstract Iterator<Entry<K, V>> entryIterator();

      Set<Entry<K, V>> createEntrySet() {
         class EntrySetImpl extends Maps.EntrySet<K, V> {
            @Override
            Map<K, V> map() {
               return DescendingMap.this;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
               return DescendingMap.this.entryIterator();
            }
         }

         return new EntrySetImpl();
      }

      @Override
      public Set<K> keySet() {
         return this.navigableKeySet();
      }

      @Override
      public NavigableSet<K> navigableKeySet() {
         NavigableSet<K> result = this.navigableKeySet;
         return result == null ? (this.navigableKeySet = new Maps.NavigableKeySet<>(this)) : result;
      }

      @Override
      public NavigableSet<K> descendingKeySet() {
         return this.forward().navigableKeySet();
      }

      @Override
      public NavigableMap<K, V> subMap(@ParametricNullness K fromKey, boolean fromInclusive, @ParametricNullness K toKey, boolean toInclusive) {
         return this.forward().subMap(toKey, toInclusive, fromKey, fromInclusive).descendingMap();
      }

      @Override
      public SortedMap<K, V> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
         return this.subMap(fromKey, true, toKey, false);
      }

      @Override
      public NavigableMap<K, V> headMap(@ParametricNullness K toKey, boolean inclusive) {
         return this.forward().tailMap(toKey, inclusive).descendingMap();
      }

      @Override
      public SortedMap<K, V> headMap(@ParametricNullness K toKey) {
         return this.headMap(toKey, false);
      }

      @Override
      public NavigableMap<K, V> tailMap(@ParametricNullness K fromKey, boolean inclusive) {
         return this.forward().headMap(fromKey, inclusive).descendingMap();
      }

      @Override
      public SortedMap<K, V> tailMap(@ParametricNullness K fromKey) {
         return this.tailMap(fromKey, true);
      }

      @Override
      public Collection<V> values() {
         return new Maps.Values<>(this);
      }

      @Override
      public String toString() {
         return this.standardToString();
      }
   }

   private enum EntryFunction implements com.dfsek.terra.lib.google.common.base.Function<Entry<?, ?>, Object> {
      KEY {
         public @Nullable Object apply(Entry<?, ?> entry) {
            return entry.getKey();
         }
      },
      VALUE {
         public @Nullable Object apply(Entry<?, ?> entry) {
            return entry.getValue();
         }
      };

      EntryFunction() {
      }
   }

   abstract static class EntrySet<K, V> extends Sets.ImprovedAbstractSet<Entry<K, V>> {
      abstract Map<K, V> map();

      @Override
      public int size() {
         return this.map().size();
      }

      @Override
      public void clear() {
         this.map().clear();
      }

      @Override
      public boolean contains(@Nullable Object o) {
         if (!(o instanceof Entry)) {
            return false;
         }

         Entry<?, ?> entry = (Entry<?, ?>)o;
         Object key = entry.getKey();
         V value = Maps.safeGet(this.map(), key);
         return com.dfsek.terra.lib.google.common.base.Objects.equal(value, entry.getValue()) && (value != null || this.map().containsKey(key));
      }

      @Override
      public boolean isEmpty() {
         return this.map().isEmpty();
      }

      @Override
      public boolean remove(@Nullable Object o) {
         if (this.contains(o) && o instanceof Entry) {
            Entry<?, ?> entry = (Entry<?, ?>)o;
            return this.map().keySet().remove(entry.getKey());
         } else {
            return false;
         }
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         try {
            return super.removeAll(Preconditions.checkNotNull(c));
         } catch (UnsupportedOperationException e) {
            return Sets.removeAllImpl(this, c.iterator());
         }
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         try {
            return super.retainAll(Preconditions.checkNotNull(c));
         } catch (UnsupportedOperationException e) {
            Set<Object> keys = Sets.newHashSetWithExpectedSize(c.size());

            for (Object o : c) {
               if (this.contains(o) && o instanceof Entry) {
                  Entry<?, ?> entry = (Entry<?, ?>)o;
                  keys.add(entry.getKey());
               }
            }

            return this.map().keySet().retainAll(keys);
         }
      }
   }

   @FunctionalInterface
   public interface EntryTransformer<K, V1, V2> {
      @ParametricNullness
      V2 transformEntry(@ParametricNullness K key, @ParametricNullness V1 value);
   }

   static final class FilteredEntryBiMap<K, V> extends Maps.FilteredEntryMap<K, V> implements BiMap<K, V> {
      @RetainedWith
      private final BiMap<V, K> inverse;

      private static <K, V> Predicate<Entry<V, K>> inversePredicate(Predicate<? super Entry<K, V>> forwardPredicate) {
         return input -> forwardPredicate.apply(Maps.immutableEntry(input.getValue(), input.getKey()));
      }

      FilteredEntryBiMap(BiMap<K, V> delegate, Predicate<? super Entry<K, V>> predicate) {
         super(delegate, predicate);
         this.inverse = new Maps.FilteredEntryBiMap<>(delegate.inverse(), inversePredicate(predicate), this);
      }

      private FilteredEntryBiMap(BiMap<K, V> delegate, Predicate<? super Entry<K, V>> predicate, BiMap<V, K> inverse) {
         super(delegate, predicate);
         this.inverse = inverse;
      }

      BiMap<K, V> unfiltered() {
         return (BiMap<K, V>)this.unfiltered;
      }

      @Override
      public @Nullable V forcePut(@ParametricNullness K key, @ParametricNullness V value) {
         Preconditions.checkArgument(this.apply(key, value));
         return this.unfiltered().forcePut(key, value);
      }

      @Override
      public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
         this.unfiltered().replaceAll((key, value) -> this.predicate.apply(Maps.immutableEntry((K)key, (V)value)) ? function.apply(key, value) : value);
      }

      @Override
      public BiMap<V, K> inverse() {
         return this.inverse;
      }

      @Override
      public Set<V> values() {
         return this.inverse.keySet();
      }
   }

   static class FilteredEntryMap<K, V> extends Maps.AbstractFilteredMap<K, V> {
      final Set<Entry<K, V>> filteredEntrySet;

      FilteredEntryMap(Map<K, V> unfiltered, Predicate<? super Entry<K, V>> entryPredicate) {
         super(unfiltered, entryPredicate);
         this.filteredEntrySet = Sets.filter(unfiltered.entrySet(), this.predicate);
      }

      @Override
      protected Set<Entry<K, V>> createEntrySet() {
         return new Maps.FilteredEntryMap.EntrySet();
      }

      @Override
      Set<K> createKeySet() {
         return new Maps.FilteredEntryMap.KeySet();
      }

      static <K, V> boolean removeAllKeys(Map<K, V> map, Predicate<? super Entry<K, V>> entryPredicate, Collection<?> keyCollection) {
         Iterator<Entry<K, V>> entryItr = map.entrySet().iterator();
         boolean result = false;

         while (entryItr.hasNext()) {
            Entry<K, V> entry = entryItr.next();
            if (entryPredicate.apply(entry) && keyCollection.contains(entry.getKey())) {
               entryItr.remove();
               result = true;
            }
         }

         return result;
      }

      static <K, V> boolean retainAllKeys(Map<K, V> map, Predicate<? super Entry<K, V>> entryPredicate, Collection<?> keyCollection) {
         Iterator<Entry<K, V>> entryItr = map.entrySet().iterator();
         boolean result = false;

         while (entryItr.hasNext()) {
            Entry<K, V> entry = entryItr.next();
            if (entryPredicate.apply(entry) && !keyCollection.contains(entry.getKey())) {
               entryItr.remove();
               result = true;
            }
         }

         return result;
      }

      private class EntrySet extends ForwardingSet<Entry<K, V>> {
         private EntrySet() {
         }

         @Override
         protected Set<Entry<K, V>> delegate() {
            return FilteredEntryMap.this.filteredEntrySet;
         }

         @Override
         public Iterator<Entry<K, V>> iterator() {
            return new TransformedIterator<Entry<K, V>, Entry<K, V>>(FilteredEntryMap.this.filteredEntrySet.iterator()) {
               Entry<K, V> transform(Entry<K, V> entry) {
                  return new ForwardingMapEntry<K, V>() {
                     @Override
                     protected Entry<K, V> delegate() {
                        return entry;
                     }

                     @ParametricNullness
                     @Override
                     public V setValue(@ParametricNullness V newValue) {
                        Preconditions.checkArgument(FilteredEntryMap.this.apply(this.getKey(), newValue));
                        return (V)super.setValue(newValue);
                     }
                  };
               }
            };
         }
      }

      class KeySet extends Maps.KeySet<K, V> {
         KeySet() {
            super(FilteredEntryMap.this);
         }

         @Override
         public boolean remove(@Nullable Object o) {
            if (FilteredEntryMap.this.containsKey(o)) {
               FilteredEntryMap.this.unfiltered.remove(o);
               return true;
            } else {
               return false;
            }
         }

         @Override
         public boolean removeAll(Collection<?> collection) {
            return Maps.FilteredEntryMap.removeAllKeys(FilteredEntryMap.this.unfiltered, FilteredEntryMap.this.predicate, collection);
         }

         @Override
         public boolean retainAll(Collection<?> collection) {
            return Maps.FilteredEntryMap.retainAllKeys(FilteredEntryMap.this.unfiltered, FilteredEntryMap.this.predicate, collection);
         }

         @Override
         public @Nullable Object[] toArray() {
            return Lists.newArrayList(this.iterator()).toArray();
         }

         @Override
         public <T> T[] toArray(T[] array) {
            return (T[])Lists.newArrayList(this.iterator()).toArray(array);
         }
      }
   }

   @GwtIncompatible
   private static class FilteredEntryNavigableMap<K, V> extends AbstractNavigableMap<K, V> {
      private final NavigableMap<K, V> unfiltered;
      private final Predicate<? super Entry<K, V>> entryPredicate;
      private final Map<K, V> filteredDelegate;

      FilteredEntryNavigableMap(NavigableMap<K, V> unfiltered, Predicate<? super Entry<K, V>> entryPredicate) {
         this.unfiltered = Preconditions.checkNotNull(unfiltered);
         this.entryPredicate = entryPredicate;
         this.filteredDelegate = new Maps.FilteredEntryMap<>(unfiltered, entryPredicate);
      }

      @Override
      public @Nullable Comparator<? super K> comparator() {
         return this.unfiltered.comparator();
      }

      @Override
      public NavigableSet<K> navigableKeySet() {
         return new Maps.NavigableKeySet<K, V>(this) {
            @Override
            public boolean removeAll(Collection<?> collection) {
               return Maps.FilteredEntryMap.removeAllKeys(FilteredEntryNavigableMap.this.unfiltered, FilteredEntryNavigableMap.this.entryPredicate, collection);
            }

            @Override
            public boolean retainAll(Collection<?> collection) {
               return Maps.FilteredEntryMap.retainAllKeys(FilteredEntryNavigableMap.this.unfiltered, FilteredEntryNavigableMap.this.entryPredicate, collection);
            }
         };
      }

      @Override
      public Collection<V> values() {
         return new Maps.FilteredMapValues<>(this, this.unfiltered, this.entryPredicate);
      }

      @Override
      Iterator<Entry<K, V>> entryIterator() {
         return Iterators.filter(this.unfiltered.entrySet().iterator(), this.entryPredicate);
      }

      @Override
      Iterator<Entry<K, V>> descendingEntryIterator() {
         return Iterators.filter(this.unfiltered.descendingMap().entrySet().iterator(), this.entryPredicate);
      }

      @Override
      public int size() {
         return this.filteredDelegate.size();
      }

      @Override
      public boolean isEmpty() {
         return !Iterables.any(this.unfiltered.entrySet(), this.entryPredicate);
      }

      @Override
      public @Nullable V get(@Nullable Object key) {
         return this.filteredDelegate.get(key);
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return this.filteredDelegate.containsKey(key);
      }

      @Override
      public @Nullable V put(@ParametricNullness K key, @ParametricNullness V value) {
         return this.filteredDelegate.put(key, value);
      }

      @Override
      public @Nullable V remove(@Nullable Object key) {
         return this.filteredDelegate.remove(key);
      }

      @Override
      public void putAll(Map<? extends K, ? extends V> m) {
         this.filteredDelegate.putAll(m);
      }

      @Override
      public void clear() {
         this.filteredDelegate.clear();
      }

      @Override
      public Set<Entry<K, V>> entrySet() {
         return this.filteredDelegate.entrySet();
      }

      @Override
      public @Nullable Entry<K, V> pollFirstEntry() {
         return Iterables.removeFirstMatching(this.unfiltered.entrySet(), this.entryPredicate);
      }

      @Override
      public @Nullable Entry<K, V> pollLastEntry() {
         return Iterables.removeFirstMatching(this.unfiltered.descendingMap().entrySet(), this.entryPredicate);
      }

      @Override
      public NavigableMap<K, V> descendingMap() {
         return Maps.filterEntries(this.unfiltered.descendingMap(), this.entryPredicate);
      }

      @Override
      public NavigableMap<K, V> subMap(@ParametricNullness K fromKey, boolean fromInclusive, @ParametricNullness K toKey, boolean toInclusive) {
         return Maps.filterEntries(this.unfiltered.subMap(fromKey, fromInclusive, toKey, toInclusive), this.entryPredicate);
      }

      @Override
      public NavigableMap<K, V> headMap(@ParametricNullness K toKey, boolean inclusive) {
         return Maps.filterEntries(this.unfiltered.headMap(toKey, inclusive), this.entryPredicate);
      }

      @Override
      public NavigableMap<K, V> tailMap(@ParametricNullness K fromKey, boolean inclusive) {
         return Maps.filterEntries(this.unfiltered.tailMap(fromKey, inclusive), this.entryPredicate);
      }
   }

   private static class FilteredEntrySortedMap<K, V> extends Maps.FilteredEntryMap<K, V> implements SortedMap<K, V> {
      FilteredEntrySortedMap(SortedMap<K, V> unfiltered, Predicate<? super Entry<K, V>> entryPredicate) {
         super(unfiltered, entryPredicate);
      }

      SortedMap<K, V> sortedMap() {
         return (SortedMap<K, V>)this.unfiltered;
      }

      public SortedSet<K> keySet() {
         return (SortedSet<K>)super.keySet();
      }

      SortedSet<K> createKeySet() {
         return new Maps.FilteredEntrySortedMap.SortedKeySet();
      }

      @Override
      public @Nullable Comparator<? super K> comparator() {
         return this.sortedMap().comparator();
      }

      @ParametricNullness
      @Override
      public K firstKey() {
         return this.keySet().iterator().next();
      }

      @ParametricNullness
      @Override
      public K lastKey() {
         SortedMap<K, V> headMap = this.sortedMap();

         while (true) {
            K key = headMap.lastKey();
            if (this.apply(key, NullnessCasts.uncheckedCastNullableTToT(this.unfiltered.get(key)))) {
               return key;
            }

            headMap = this.sortedMap().headMap(key);
         }
      }

      @Override
      public SortedMap<K, V> headMap(@ParametricNullness K toKey) {
         return new Maps.FilteredEntrySortedMap<>(this.sortedMap().headMap(toKey), this.predicate);
      }

      @Override
      public SortedMap<K, V> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
         return new Maps.FilteredEntrySortedMap<>(this.sortedMap().subMap(fromKey, toKey), this.predicate);
      }

      @Override
      public SortedMap<K, V> tailMap(@ParametricNullness K fromKey) {
         return new Maps.FilteredEntrySortedMap<>(this.sortedMap().tailMap(fromKey), this.predicate);
      }

      class SortedKeySet extends Maps.FilteredEntryMap<K, V>.KeySet implements SortedSet<K> {
         @Override
         public @Nullable Comparator<? super K> comparator() {
            return FilteredEntrySortedMap.this.sortedMap().comparator();
         }

         @Override
         public SortedSet<K> subSet(@ParametricNullness K fromElement, @ParametricNullness K toElement) {
            return (SortedSet<K>)FilteredEntrySortedMap.this.subMap(fromElement, toElement).keySet();
         }

         @Override
         public SortedSet<K> headSet(@ParametricNullness K toElement) {
            return (SortedSet<K>)FilteredEntrySortedMap.this.headMap(toElement).keySet();
         }

         @Override
         public SortedSet<K> tailSet(@ParametricNullness K fromElement) {
            return (SortedSet<K>)FilteredEntrySortedMap.this.tailMap(fromElement).keySet();
         }

         @ParametricNullness
         @Override
         public K first() {
            return FilteredEntrySortedMap.this.firstKey();
         }

         @ParametricNullness
         @Override
         public K last() {
            return FilteredEntrySortedMap.this.lastKey();
         }
      }
   }

   private static class FilteredKeyMap<K, V> extends Maps.AbstractFilteredMap<K, V> {
      final Predicate<? super K> keyPredicate;

      FilteredKeyMap(Map<K, V> unfiltered, Predicate<? super K> keyPredicate, Predicate<? super Entry<K, V>> entryPredicate) {
         super(unfiltered, entryPredicate);
         this.keyPredicate = keyPredicate;
      }

      @Override
      protected Set<Entry<K, V>> createEntrySet() {
         return Sets.filter(this.unfiltered.entrySet(), this.predicate);
      }

      @Override
      Set<K> createKeySet() {
         return Sets.filter(this.unfiltered.keySet(), this.keyPredicate);
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return this.unfiltered.containsKey(key) && this.keyPredicate.apply((K)key);
      }
   }

   private static final class FilteredMapValues<K, V> extends Maps.Values<K, V> {
      final Map<K, V> unfiltered;
      final Predicate<? super Entry<K, V>> predicate;

      FilteredMapValues(Map<K, V> filteredMap, Map<K, V> unfiltered, Predicate<? super Entry<K, V>> predicate) {
         super(filteredMap);
         this.unfiltered = unfiltered;
         this.predicate = predicate;
      }

      @Override
      public boolean remove(@Nullable Object o) {
         Iterator<Entry<K, V>> entryItr = this.unfiltered.entrySet().iterator();

         while (entryItr.hasNext()) {
            Entry<K, V> entry = entryItr.next();
            if (this.predicate.apply(entry) && com.dfsek.terra.lib.google.common.base.Objects.equal(entry.getValue(), o)) {
               entryItr.remove();
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         Iterator<Entry<K, V>> entryItr = this.unfiltered.entrySet().iterator();
         boolean result = false;

         while (entryItr.hasNext()) {
            Entry<K, V> entry = entryItr.next();
            if (this.predicate.apply(entry) && collection.contains(entry.getValue())) {
               entryItr.remove();
               result = true;
            }
         }

         return result;
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         Iterator<Entry<K, V>> entryItr = this.unfiltered.entrySet().iterator();
         boolean result = false;

         while (entryItr.hasNext()) {
            Entry<K, V> entry = entryItr.next();
            if (this.predicate.apply(entry) && !collection.contains(entry.getValue())) {
               entryItr.remove();
               result = true;
            }
         }

         return result;
      }

      @Override
      public @Nullable Object[] toArray() {
         return Lists.newArrayList(this.iterator()).toArray();
      }

      @Override
      public <T> T[] toArray(T[] array) {
         return (T[])Lists.newArrayList(this.iterator()).toArray(array);
      }
   }

   abstract static class IteratorBasedAbstractMap<K, V> extends AbstractMap<K, V> {
      @Override
      public abstract int size();

      abstract Iterator<Entry<K, V>> entryIterator();

      Spliterator<Entry<K, V>> entrySpliterator() {
         return Spliterators.spliterator(this.entryIterator(), this.size(), 65);
      }

      @Override
      public Set<Entry<K, V>> entrySet() {
         return new Maps.EntrySet<K, V>() {
            @Override
            Map<K, V> map() {
               return IteratorBasedAbstractMap.this;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
               return IteratorBasedAbstractMap.this.entryIterator();
            }

            @Override
            public Spliterator<Entry<K, V>> spliterator() {
               return IteratorBasedAbstractMap.this.entrySpliterator();
            }

            @Override
            public void forEach(Consumer<? super Entry<K, V>> action) {
               IteratorBasedAbstractMap.this.forEachEntry(action);
            }
         };
      }

      void forEachEntry(Consumer<? super Entry<K, V>> action) {
         this.entryIterator().forEachRemaining(action);
      }

      @Override
      public void clear() {
         Iterators.clear(this.entryIterator());
      }
   }

   static class KeySet<K, V> extends Sets.ImprovedAbstractSet<K> {
      @Weak
      final Map<K, V> map;

      KeySet(Map<K, V> map) {
         this.map = Preconditions.checkNotNull(map);
      }

      Map<K, V> map() {
         return this.map;
      }

      @Override
      public Iterator<K> iterator() {
         return Maps.keyIterator(this.map().entrySet().iterator());
      }

      @Override
      public void forEach(Consumer<? super K> action) {
         Preconditions.checkNotNull(action);
         this.map.forEach((k, v) -> action.accept(k));
      }

      @Override
      public int size() {
         return this.map().size();
      }

      @Override
      public boolean isEmpty() {
         return this.map().isEmpty();
      }

      @Override
      public boolean contains(@Nullable Object o) {
         return this.map().containsKey(o);
      }

      @Override
      public boolean remove(@Nullable Object o) {
         if (this.contains(o)) {
            this.map().remove(o);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void clear() {
         this.map().clear();
      }
   }

   static class MapDifferenceImpl<K, V> implements MapDifference<K, V> {
      final Map<K, V> onlyOnLeft;
      final Map<K, V> onlyOnRight;
      final Map<K, V> onBoth;
      final Map<K, MapDifference.ValueDifference<V>> differences;

      MapDifferenceImpl(Map<K, V> onlyOnLeft, Map<K, V> onlyOnRight, Map<K, V> onBoth, Map<K, MapDifference.ValueDifference<V>> differences) {
         this.onlyOnLeft = Maps.unmodifiableMap(onlyOnLeft);
         this.onlyOnRight = Maps.unmodifiableMap(onlyOnRight);
         this.onBoth = Maps.unmodifiableMap(onBoth);
         this.differences = Maps.unmodifiableMap(differences);
      }

      @Override
      public boolean areEqual() {
         return this.onlyOnLeft.isEmpty() && this.onlyOnRight.isEmpty() && this.differences.isEmpty();
      }

      @Override
      public Map<K, V> entriesOnlyOnLeft() {
         return this.onlyOnLeft;
      }

      @Override
      public Map<K, V> entriesOnlyOnRight() {
         return this.onlyOnRight;
      }

      @Override
      public Map<K, V> entriesInCommon() {
         return this.onBoth;
      }

      @Override
      public Map<K, MapDifference.ValueDifference<V>> entriesDiffering() {
         return this.differences;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (object == this) {
            return true;
         }

         if (!(object instanceof MapDifference)) {
            return false;
         }

         MapDifference<?, ?> other = (MapDifference<?, ?>)object;
         return this.entriesOnlyOnLeft().equals(other.entriesOnlyOnLeft())
            && this.entriesOnlyOnRight().equals(other.entriesOnlyOnRight())
            && this.entriesInCommon().equals(other.entriesInCommon())
            && this.entriesDiffering().equals(other.entriesDiffering());
      }

      @Override
      public int hashCode() {
         return com.dfsek.terra.lib.google.common.base.Objects.hashCode(
            this.entriesOnlyOnLeft(), this.entriesOnlyOnRight(), this.entriesInCommon(), this.entriesDiffering()
         );
      }

      @Override
      public String toString() {
         if (this.areEqual()) {
            return "equal";
         }

         StringBuilder result = new StringBuilder("not equal");
         if (!this.onlyOnLeft.isEmpty()) {
            result.append(": only on left=").append(this.onlyOnLeft);
         }

         if (!this.onlyOnRight.isEmpty()) {
            result.append(": only on right=").append(this.onlyOnRight);
         }

         if (!this.differences.isEmpty()) {
            result.append(": value differences=").append(this.differences);
         }

         return result.toString();
      }
   }

   @GwtIncompatible
   private static final class NavigableAsMapView<K, V> extends AbstractNavigableMap<K, V> {
      private final NavigableSet<K> set;
      private final com.dfsek.terra.lib.google.common.base.Function<? super K, V> function;

      NavigableAsMapView(NavigableSet<K> ks, com.dfsek.terra.lib.google.common.base.Function<? super K, V> vFunction) {
         this.set = Preconditions.checkNotNull(ks);
         this.function = Preconditions.checkNotNull(vFunction);
      }

      @Override
      public NavigableMap<K, V> subMap(@ParametricNullness K fromKey, boolean fromInclusive, @ParametricNullness K toKey, boolean toInclusive) {
         return Maps.asMap(this.set.subSet(fromKey, fromInclusive, toKey, toInclusive), this.function);
      }

      @Override
      public NavigableMap<K, V> headMap(@ParametricNullness K toKey, boolean inclusive) {
         return Maps.asMap(this.set.headSet(toKey, inclusive), this.function);
      }

      @Override
      public NavigableMap<K, V> tailMap(@ParametricNullness K fromKey, boolean inclusive) {
         return Maps.asMap(this.set.tailSet(fromKey, inclusive), this.function);
      }

      @Override
      public @Nullable Comparator<? super K> comparator() {
         return this.set.comparator();
      }

      @Override
      public @Nullable V get(@Nullable Object key) {
         return this.getOrDefault(key, null);
      }

      @Override
      public @Nullable V getOrDefault(@Nullable Object key, @Nullable V defaultValue) {
         if (Collections2.safeContains(this.set, key)) {
            K k = (K)key;
            return this.function.apply(k);
         } else {
            return defaultValue;
         }
      }

      @Override
      public void clear() {
         this.set.clear();
      }

      @Override
      Iterator<Entry<K, V>> entryIterator() {
         return Maps.asMapEntryIterator(this.set, this.function);
      }

      @Override
      Spliterator<Entry<K, V>> entrySpliterator() {
         return CollectSpliterators.map(this.set.spliterator(), e -> Maps.immutableEntry((K)e, this.function.apply(e)));
      }

      @Override
      public void forEach(BiConsumer<? super K, ? super V> action) {
         this.set.forEach(k -> action.accept(k, this.function.apply(k)));
      }

      @Override
      Iterator<Entry<K, V>> descendingEntryIterator() {
         return this.descendingMap().entrySet().iterator();
      }

      @Override
      public NavigableSet<K> navigableKeySet() {
         return Maps.removeOnlyNavigableSet(this.set);
      }

      @Override
      public int size() {
         return this.set.size();
      }

      @Override
      public NavigableMap<K, V> descendingMap() {
         return Maps.asMap(this.set.descendingSet(), this.function);
      }
   }

   @GwtIncompatible
   static class NavigableKeySet<K, V> extends Maps.SortedKeySet<K, V> implements NavigableSet<K> {
      NavigableKeySet(NavigableMap<K, V> map) {
         super(map);
      }

      NavigableMap<K, V> map() {
         return (NavigableMap<K, V>)this.map;
      }

      @Override
      public @Nullable K lower(@ParametricNullness K e) {
         return this.map().lowerKey(e);
      }

      @Override
      public @Nullable K floor(@ParametricNullness K e) {
         return this.map().floorKey(e);
      }

      @Override
      public @Nullable K ceiling(@ParametricNullness K e) {
         return this.map().ceilingKey(e);
      }

      @Override
      public @Nullable K higher(@ParametricNullness K e) {
         return this.map().higherKey(e);
      }

      @Override
      public @Nullable K pollFirst() {
         return Maps.keyOrNull(this.map().pollFirstEntry());
      }

      @Override
      public @Nullable K pollLast() {
         return Maps.keyOrNull(this.map().pollLastEntry());
      }

      @Override
      public NavigableSet<K> descendingSet() {
         return this.map().descendingKeySet();
      }

      @Override
      public Iterator<K> descendingIterator() {
         return this.descendingSet().iterator();
      }

      @Override
      public NavigableSet<K> subSet(@ParametricNullness K fromElement, boolean fromInclusive, @ParametricNullness K toElement, boolean toInclusive) {
         return this.map().subMap(fromElement, fromInclusive, toElement, toInclusive).navigableKeySet();
      }

      @Override
      public SortedSet<K> subSet(@ParametricNullness K fromElement, @ParametricNullness K toElement) {
         return this.subSet(fromElement, true, toElement, false);
      }

      @Override
      public NavigableSet<K> headSet(@ParametricNullness K toElement, boolean inclusive) {
         return this.map().headMap(toElement, inclusive).navigableKeySet();
      }

      @Override
      public SortedSet<K> headSet(@ParametricNullness K toElement) {
         return this.headSet(toElement, false);
      }

      @Override
      public NavigableSet<K> tailSet(@ParametricNullness K fromElement, boolean inclusive) {
         return this.map().tailMap(fromElement, inclusive).navigableKeySet();
      }

      @Override
      public SortedSet<K> tailSet(@ParametricNullness K fromElement) {
         return this.tailSet(fromElement, true);
      }
   }

   private static class SortedAsMapView<K, V> extends Maps.AsMapView<K, V> implements SortedMap<K, V> {
      SortedAsMapView(SortedSet<K> set, com.dfsek.terra.lib.google.common.base.Function<? super K, V> function) {
         super(set, function);
      }

      SortedSet<K> backingSet() {
         return (SortedSet<K>)super.backingSet();
      }

      @Override
      public @Nullable Comparator<? super K> comparator() {
         return this.backingSet().comparator();
      }

      @Override
      public Set<K> keySet() {
         return Maps.removeOnlySortedSet(this.backingSet());
      }

      @Override
      public SortedMap<K, V> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
         return Maps.asMap(this.backingSet().subSet(fromKey, toKey), this.function);
      }

      @Override
      public SortedMap<K, V> headMap(@ParametricNullness K toKey) {
         return Maps.asMap(this.backingSet().headSet(toKey), this.function);
      }

      @Override
      public SortedMap<K, V> tailMap(@ParametricNullness K fromKey) {
         return Maps.asMap(this.backingSet().tailSet(fromKey), this.function);
      }

      @ParametricNullness
      @Override
      public K firstKey() {
         return this.backingSet().first();
      }

      @ParametricNullness
      @Override
      public K lastKey() {
         return this.backingSet().last();
      }
   }

   static class SortedKeySet<K, V> extends Maps.KeySet<K, V> implements SortedSet<K> {
      SortedKeySet(SortedMap<K, V> map) {
         super(map);
      }

      SortedMap<K, V> map() {
         return (SortedMap<K, V>)super.map();
      }

      @Override
      public @Nullable Comparator<? super K> comparator() {
         return this.map().comparator();
      }

      @Override
      public SortedSet<K> subSet(@ParametricNullness K fromElement, @ParametricNullness K toElement) {
         return new Maps.SortedKeySet<>(this.map().subMap(fromElement, toElement));
      }

      @Override
      public SortedSet<K> headSet(@ParametricNullness K toElement) {
         return new Maps.SortedKeySet<>(this.map().headMap(toElement));
      }

      @Override
      public SortedSet<K> tailSet(@ParametricNullness K fromElement) {
         return new Maps.SortedKeySet<>(this.map().tailMap(fromElement));
      }

      @ParametricNullness
      @Override
      public K first() {
         return this.map().firstKey();
      }

      @ParametricNullness
      @Override
      public K last() {
         return this.map().lastKey();
      }
   }

   static class SortedMapDifferenceImpl<K, V> extends Maps.MapDifferenceImpl<K, V> implements SortedMapDifference<K, V> {
      SortedMapDifferenceImpl(
         SortedMap<K, V> onlyOnLeft, SortedMap<K, V> onlyOnRight, SortedMap<K, V> onBoth, SortedMap<K, MapDifference.ValueDifference<V>> differences
      ) {
         super(onlyOnLeft, onlyOnRight, onBoth, differences);
      }

      @Override
      public SortedMap<K, MapDifference.ValueDifference<V>> entriesDiffering() {
         return (SortedMap<K, MapDifference.ValueDifference<V>>)super.entriesDiffering();
      }

      @Override
      public SortedMap<K, V> entriesInCommon() {
         return (SortedMap<K, V>)super.entriesInCommon();
      }

      @Override
      public SortedMap<K, V> entriesOnlyOnLeft() {
         return (SortedMap<K, V>)super.entriesOnlyOnLeft();
      }

      @Override
      public SortedMap<K, V> entriesOnlyOnRight() {
         return (SortedMap<K, V>)super.entriesOnlyOnRight();
      }
   }

   static class TransformedEntriesMap<K, V1, V2> extends Maps.IteratorBasedAbstractMap<K, V2> {
      final Map<K, V1> fromMap;
      final Maps.EntryTransformer<? super K, ? super V1, V2> transformer;

      TransformedEntriesMap(Map<K, V1> fromMap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
         this.fromMap = Preconditions.checkNotNull(fromMap);
         this.transformer = Preconditions.checkNotNull(transformer);
      }

      @Override
      public int size() {
         return this.fromMap.size();
      }

      @Override
      public boolean containsKey(@Nullable Object key) {
         return this.fromMap.containsKey(key);
      }

      @Override
      public @Nullable V2 get(@Nullable Object key) {
         return this.getOrDefault(key, null);
      }

      @Override
      public @Nullable V2 getOrDefault(@Nullable Object key, @Nullable V2 defaultValue) {
         V1 value = this.fromMap.get(key);
         return value == null && !this.fromMap.containsKey(key)
            ? defaultValue
            : this.transformer.transformEntry((K)key, NullnessCasts.uncheckedCastNullableTToT(value));
      }

      @Override
      public @Nullable V2 remove(@Nullable Object key) {
         return this.fromMap.containsKey(key)
            ? this.transformer.transformEntry((K)key, NullnessCasts.uncheckedCastNullableTToT(this.fromMap.remove(key)))
            : null;
      }

      @Override
      public void clear() {
         this.fromMap.clear();
      }

      @Override
      public Set<K> keySet() {
         return this.fromMap.keySet();
      }

      @Override
      Iterator<Entry<K, V2>> entryIterator() {
         return Iterators.transform(this.fromMap.entrySet().iterator(), Maps.asEntryToEntryFunction(this.transformer));
      }

      @Override
      Spliterator<Entry<K, V2>> entrySpliterator() {
         return CollectSpliterators.map(this.fromMap.entrySet().spliterator(), Maps.asEntryToEntryFunction(this.transformer));
      }

      @Override
      public void forEach(BiConsumer<? super K, ? super V2> action) {
         Preconditions.checkNotNull(action);
         this.fromMap.forEach((k, v1) -> action.accept(k, this.transformer.transformEntry(k, v1)));
      }

      @Override
      public Collection<V2> values() {
         return new Maps.Values<>(this);
      }
   }

   @GwtIncompatible
   private static class TransformedEntriesNavigableMap<K, V1, V2> extends Maps.TransformedEntriesSortedMap<K, V1, V2> implements NavigableMap<K, V2> {
      TransformedEntriesNavigableMap(NavigableMap<K, V1> fromMap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
         super(fromMap, transformer);
      }

      @Override
      public @Nullable Entry<K, V2> ceilingEntry(@ParametricNullness K key) {
         return this.transformEntry(this.fromMap().ceilingEntry(key));
      }

      @Override
      public @Nullable K ceilingKey(@ParametricNullness K key) {
         return this.fromMap().ceilingKey(key);
      }

      @Override
      public NavigableSet<K> descendingKeySet() {
         return this.fromMap().descendingKeySet();
      }

      @Override
      public NavigableMap<K, V2> descendingMap() {
         return Maps.transformEntries(this.fromMap().descendingMap(), this.transformer);
      }

      @Override
      public @Nullable Entry<K, V2> firstEntry() {
         return this.transformEntry(this.fromMap().firstEntry());
      }

      @Override
      public @Nullable Entry<K, V2> floorEntry(@ParametricNullness K key) {
         return this.transformEntry(this.fromMap().floorEntry(key));
      }

      @Override
      public @Nullable K floorKey(@ParametricNullness K key) {
         return this.fromMap().floorKey(key);
      }

      public NavigableMap<K, V2> headMap(@ParametricNullness K toKey) {
         return this.headMap(toKey, false);
      }

      @Override
      public NavigableMap<K, V2> headMap(@ParametricNullness K toKey, boolean inclusive) {
         return Maps.transformEntries(this.fromMap().headMap(toKey, inclusive), this.transformer);
      }

      @Override
      public @Nullable Entry<K, V2> higherEntry(@ParametricNullness K key) {
         return this.transformEntry(this.fromMap().higherEntry(key));
      }

      @Override
      public @Nullable K higherKey(@ParametricNullness K key) {
         return this.fromMap().higherKey(key);
      }

      @Override
      public @Nullable Entry<K, V2> lastEntry() {
         return this.transformEntry(this.fromMap().lastEntry());
      }

      @Override
      public @Nullable Entry<K, V2> lowerEntry(@ParametricNullness K key) {
         return this.transformEntry(this.fromMap().lowerEntry(key));
      }

      @Override
      public @Nullable K lowerKey(@ParametricNullness K key) {
         return this.fromMap().lowerKey(key);
      }

      @Override
      public NavigableSet<K> navigableKeySet() {
         return this.fromMap().navigableKeySet();
      }

      @Override
      public @Nullable Entry<K, V2> pollFirstEntry() {
         return this.transformEntry(this.fromMap().pollFirstEntry());
      }

      @Override
      public @Nullable Entry<K, V2> pollLastEntry() {
         return this.transformEntry(this.fromMap().pollLastEntry());
      }

      @Override
      public NavigableMap<K, V2> subMap(@ParametricNullness K fromKey, boolean fromInclusive, @ParametricNullness K toKey, boolean toInclusive) {
         return Maps.transformEntries(this.fromMap().subMap(fromKey, fromInclusive, toKey, toInclusive), this.transformer);
      }

      public NavigableMap<K, V2> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
         return this.subMap(fromKey, true, toKey, false);
      }

      public NavigableMap<K, V2> tailMap(@ParametricNullness K fromKey) {
         return this.tailMap(fromKey, true);
      }

      @Override
      public NavigableMap<K, V2> tailMap(@ParametricNullness K fromKey, boolean inclusive) {
         return Maps.transformEntries(this.fromMap().tailMap(fromKey, inclusive), this.transformer);
      }

      private @Nullable Entry<K, V2> transformEntry(@Nullable Entry<K, V1> entry) {
         return entry == null ? null : Maps.transformEntry(this.transformer, entry);
      }

      protected NavigableMap<K, V1> fromMap() {
         return (NavigableMap<K, V1>)super.fromMap();
      }
   }

   static class TransformedEntriesSortedMap<K, V1, V2> extends Maps.TransformedEntriesMap<K, V1, V2> implements SortedMap<K, V2> {
      protected SortedMap<K, V1> fromMap() {
         return (SortedMap<K, V1>)this.fromMap;
      }

      TransformedEntriesSortedMap(SortedMap<K, V1> fromMap, Maps.EntryTransformer<? super K, ? super V1, V2> transformer) {
         super(fromMap, transformer);
      }

      @Override
      public @Nullable Comparator<? super K> comparator() {
         return this.fromMap().comparator();
      }

      @ParametricNullness
      @Override
      public K firstKey() {
         return this.fromMap().firstKey();
      }

      @Override
      public SortedMap<K, V2> headMap(@ParametricNullness K toKey) {
         return Maps.transformEntries(this.fromMap().headMap(toKey), this.transformer);
      }

      @ParametricNullness
      @Override
      public K lastKey() {
         return this.fromMap().lastKey();
      }

      @Override
      public SortedMap<K, V2> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
         return Maps.transformEntries(this.fromMap().subMap(fromKey, toKey), this.transformer);
      }

      @Override
      public SortedMap<K, V2> tailMap(@ParametricNullness K fromKey) {
         return Maps.transformEntries(this.fromMap().tailMap(fromKey), this.transformer);
      }
   }

   private static class UnmodifiableBiMap<K, V> extends ForwardingMap<K, V> implements BiMap<K, V>, Serializable {
      final Map<K, V> unmodifiableMap;
      final BiMap<? extends K, ? extends V> delegate;
      @LazyInit
      @RetainedWith
      @Nullable BiMap<V, K> inverse;
      @LazyInit
      transient @Nullable Set<V> values;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      UnmodifiableBiMap(BiMap<? extends K, ? extends V> delegate, @Nullable BiMap<V, K> inverse) {
         this.unmodifiableMap = Collections.unmodifiableMap(delegate);
         this.delegate = delegate;
         this.inverse = inverse;
      }

      @Override
      protected Map<K, V> delegate() {
         return this.unmodifiableMap;
      }

      @Override
      public @Nullable V forcePut(@ParametricNullness K key, @ParametricNullness V value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V putIfAbsent(K key, V value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(@Nullable Object key, @Nullable Object value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean replace(K key, V oldValue, V newValue) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V replace(K key, V value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V computeIfPresent(K key, BiFunction<? super K, ? super @NonNull V, ? extends @Nullable V> remappingFunction) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V compute(K key, BiFunction<? super K, ? super @Nullable V, ? extends @Nullable V> remappingFunction) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V merge(K key, @NonNull V value, BiFunction<? super @NonNull V, ? super @NonNull V, ? extends @Nullable V> function) {
         throw new UnsupportedOperationException();
      }

      @Override
      public BiMap<V, K> inverse() {
         BiMap<V, K> result = this.inverse;
         return result == null ? (this.inverse = new Maps.UnmodifiableBiMap<>(this.delegate.inverse(), this)) : result;
      }

      @Override
      public Set<V> values() {
         Set<V> result = this.values;
         return result == null ? (this.values = Collections.unmodifiableSet(this.delegate.values())) : result;
      }
   }

   static class UnmodifiableEntries<K, V> extends ForwardingCollection<Entry<K, V>> {
      private final Collection<Entry<K, V>> entries;

      UnmodifiableEntries(Collection<Entry<K, V>> entries) {
         this.entries = entries;
      }

      @Override
      protected Collection<Entry<K, V>> delegate() {
         return this.entries;
      }

      @Override
      public Iterator<Entry<K, V>> iterator() {
         return Maps.unmodifiableEntryIterator(this.entries.iterator());
      }

      @Override
      public @Nullable Object[] toArray() {
         return this.standardToArray();
      }

      @Override
      public <T> T[] toArray(T[] array) {
         return (T[])this.standardToArray(array);
      }
   }

   static class UnmodifiableEntrySet<K, V> extends Maps.UnmodifiableEntries<K, V> implements Set<Entry<K, V>> {
      UnmodifiableEntrySet(Set<Entry<K, V>> entries) {
         super(entries);
      }

      @Override
      public boolean equals(@Nullable Object object) {
         return Sets.equalsImpl(this, object);
      }

      @Override
      public int hashCode() {
         return Sets.hashCodeImpl(this);
      }
   }

   @GwtIncompatible
   static class UnmodifiableNavigableMap<K, V> extends ForwardingSortedMap<K, V> implements NavigableMap<K, V>, Serializable {
      private final NavigableMap<K, ? extends V> delegate;
      @LazyInit
      private transient Maps.@Nullable UnmodifiableNavigableMap<K, V> descendingMap;

      UnmodifiableNavigableMap(NavigableMap<K, ? extends V> delegate) {
         this.delegate = delegate;
      }

      UnmodifiableNavigableMap(NavigableMap<K, ? extends V> delegate, Maps.UnmodifiableNavigableMap<K, V> descendingMap) {
         this.delegate = delegate;
         this.descendingMap = descendingMap;
      }

      @Override
      protected SortedMap<K, V> delegate() {
         return Collections.unmodifiableSortedMap(this.delegate);
      }

      @Override
      public @Nullable Entry<K, V> lowerEntry(@ParametricNullness K key) {
         return Maps.unmodifiableOrNull(this.delegate.lowerEntry(key));
      }

      @Override
      public @Nullable K lowerKey(@ParametricNullness K key) {
         return this.delegate.lowerKey(key);
      }

      @Override
      public @Nullable Entry<K, V> floorEntry(@ParametricNullness K key) {
         return Maps.unmodifiableOrNull(this.delegate.floorEntry(key));
      }

      @Override
      public @Nullable K floorKey(@ParametricNullness K key) {
         return this.delegate.floorKey(key);
      }

      @Override
      public @Nullable Entry<K, V> ceilingEntry(@ParametricNullness K key) {
         return Maps.unmodifiableOrNull(this.delegate.ceilingEntry(key));
      }

      @Override
      public @Nullable K ceilingKey(@ParametricNullness K key) {
         return this.delegate.ceilingKey(key);
      }

      @Override
      public @Nullable Entry<K, V> higherEntry(@ParametricNullness K key) {
         return Maps.unmodifiableOrNull(this.delegate.higherEntry(key));
      }

      @Override
      public @Nullable K higherKey(@ParametricNullness K key) {
         return this.delegate.higherKey(key);
      }

      @Override
      public @Nullable Entry<K, V> firstEntry() {
         return Maps.unmodifiableOrNull(this.delegate.firstEntry());
      }

      @Override
      public @Nullable Entry<K, V> lastEntry() {
         return Maps.unmodifiableOrNull(this.delegate.lastEntry());
      }

      @Override
      public final @Nullable Entry<K, V> pollFirstEntry() {
         throw new UnsupportedOperationException();
      }

      @Override
      public final @Nullable Entry<K, V> pollLastEntry() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V putIfAbsent(K key, V value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(@Nullable Object key, @Nullable Object value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean replace(K key, V oldValue, V newValue) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V replace(K key, V value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V computeIfPresent(K key, BiFunction<? super K, ? super @NonNull V, ? extends @Nullable V> remappingFunction) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V compute(K key, BiFunction<? super K, ? super @Nullable V, ? extends @Nullable V> remappingFunction) {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable V merge(K key, @NonNull V value, BiFunction<? super @NonNull V, ? super @NonNull V, ? extends @Nullable V> function) {
         throw new UnsupportedOperationException();
      }

      @Override
      public NavigableMap<K, V> descendingMap() {
         Maps.UnmodifiableNavigableMap<K, V> result = this.descendingMap;
         return result == null ? (this.descendingMap = new Maps.UnmodifiableNavigableMap<>(this.delegate.descendingMap(), this)) : result;
      }

      @Override
      public Set<K> keySet() {
         return this.navigableKeySet();
      }

      @Override
      public NavigableSet<K> navigableKeySet() {
         return Sets.unmodifiableNavigableSet(this.delegate.navigableKeySet());
      }

      @Override
      public NavigableSet<K> descendingKeySet() {
         return Sets.unmodifiableNavigableSet(this.delegate.descendingKeySet());
      }

      @Override
      public SortedMap<K, V> subMap(@ParametricNullness K fromKey, @ParametricNullness K toKey) {
         return this.subMap(fromKey, true, toKey, false);
      }

      @Override
      public NavigableMap<K, V> subMap(@ParametricNullness K fromKey, boolean fromInclusive, @ParametricNullness K toKey, boolean toInclusive) {
         return Maps.unmodifiableNavigableMap(this.delegate.subMap(fromKey, fromInclusive, toKey, toInclusive));
      }

      @Override
      public SortedMap<K, V> headMap(@ParametricNullness K toKey) {
         return this.headMap(toKey, false);
      }

      @Override
      public NavigableMap<K, V> headMap(@ParametricNullness K toKey, boolean inclusive) {
         return Maps.unmodifiableNavigableMap(this.delegate.headMap(toKey, inclusive));
      }

      @Override
      public SortedMap<K, V> tailMap(@ParametricNullness K fromKey) {
         return this.tailMap(fromKey, true);
      }

      @Override
      public NavigableMap<K, V> tailMap(@ParametricNullness K fromKey, boolean inclusive) {
         return Maps.unmodifiableNavigableMap(this.delegate.tailMap(fromKey, inclusive));
      }
   }

   static class ValueDifferenceImpl<V> implements MapDifference.ValueDifference<V> {
      @ParametricNullness
      private final V left;
      @ParametricNullness
      private final V right;

      static <V> MapDifference.ValueDifference<V> create(@ParametricNullness V left, @ParametricNullness V right) {
         return new Maps.ValueDifferenceImpl<>(left, right);
      }

      private ValueDifferenceImpl(@ParametricNullness V left, @ParametricNullness V right) {
         this.left = left;
         this.right = right;
      }

      @ParametricNullness
      @Override
      public V leftValue() {
         return this.left;
      }

      @ParametricNullness
      @Override
      public V rightValue() {
         return this.right;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (!(object instanceof MapDifference.ValueDifference)) {
            return false;
         }

         MapDifference.ValueDifference<?> that = (MapDifference.ValueDifference<?>)object;
         return com.dfsek.terra.lib.google.common.base.Objects.equal(this.left, that.leftValue())
            && com.dfsek.terra.lib.google.common.base.Objects.equal(this.right, that.rightValue());
      }

      @Override
      public int hashCode() {
         return com.dfsek.terra.lib.google.common.base.Objects.hashCode(this.left, this.right);
      }

      @Override
      public String toString() {
         return "(" + this.left + ", " + this.right + ")";
      }
   }

   static class Values<K, V> extends AbstractCollection<V> {
      @Weak
      final Map<K, V> map;

      Values(Map<K, V> map) {
         this.map = Preconditions.checkNotNull(map);
      }

      final Map<K, V> map() {
         return this.map;
      }

      @Override
      public Iterator<V> iterator() {
         return Maps.valueIterator(this.map().entrySet().iterator());
      }

      @Override
      public void forEach(Consumer<? super V> action) {
         Preconditions.checkNotNull(action);
         this.map.forEach((k, v) -> action.accept(v));
      }

      @Override
      public boolean remove(@Nullable Object o) {
         try {
            return super.remove(o);
         } catch (UnsupportedOperationException e) {
            for (Entry<K, V> entry : this.map().entrySet()) {
               if (com.dfsek.terra.lib.google.common.base.Objects.equal(o, entry.getValue())) {
                  this.map().remove(entry.getKey());
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         try {
            return super.removeAll(Preconditions.checkNotNull(c));
         } catch (UnsupportedOperationException e) {
            Set<K> toRemove = Sets.newHashSet();

            for (Entry<K, V> entry : this.map().entrySet()) {
               if (c.contains(entry.getValue())) {
                  toRemove.add(entry.getKey());
               }
            }

            return this.map().keySet().removeAll(toRemove);
         }
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         try {
            return super.retainAll(Preconditions.checkNotNull(c));
         } catch (UnsupportedOperationException e) {
            Set<K> toRetain = Sets.newHashSet();

            for (Entry<K, V> entry : this.map().entrySet()) {
               if (c.contains(entry.getValue())) {
                  toRetain.add(entry.getKey());
               }
            }

            return this.map().keySet().retainAll(toRetain);
         }
      }

      @Override
      public int size() {
         return this.map().size();
      }

      @Override
      public boolean isEmpty() {
         return this.map().isEmpty();
      }

      @Override
      public boolean contains(@Nullable Object o) {
         return this.map().containsValue(o);
      }

      @Override
      public void clear() {
         this.map().clear();
      }
   }

   @GwtCompatible
   abstract static class ViewCachingAbstractMap<K, V> extends AbstractMap<K, V> {
      @LazyInit
      private transient @Nullable Set<Entry<K, V>> entrySet;
      @LazyInit
      private transient @Nullable Set<K> keySet;
      @LazyInit
      private transient @Nullable Collection<V> values;

      abstract Set<Entry<K, V>> createEntrySet();

      @Override
      public Set<Entry<K, V>> entrySet() {
         Set<Entry<K, V>> result = this.entrySet;
         return result == null ? (this.entrySet = this.createEntrySet()) : result;
      }

      @Override
      public Set<K> keySet() {
         Set<K> result = this.keySet;
         return result == null ? (this.keySet = this.createKeySet()) : result;
      }

      Set<K> createKeySet() {
         return new Maps.KeySet<>(this);
      }

      @Override
      public Collection<V> values() {
         Collection<V> result = this.values;
         return result == null ? (this.values = this.createValues()) : result;
      }

      Collection<V> createValues() {
         return new Maps.Values<>(this);
      }
   }
}
