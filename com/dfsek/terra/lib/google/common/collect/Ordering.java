package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.InlineMe;
import com.google.errorprone.annotations.InlineMeValidationDisabled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class Ordering<T> implements Comparator<T> {
   static final int LEFT_IS_GREATER = 1;
   static final int RIGHT_IS_GREATER = -1;

   @GwtCompatible(serializable = true)
   public static <C extends Comparable> Ordering<C> natural() {
      return NaturalOrdering.INSTANCE;
   }

   @GwtCompatible(serializable = true)
   public static <T> Ordering<T> from(Comparator<T> comparator) {
      return comparator instanceof Ordering ? (Ordering)comparator : new ComparatorOrdering<>(comparator);
   }

   @Deprecated
   @InlineMe(replacement = "checkNotNull(ordering)", staticImports = "com.dfsek.terra.lib.google.common.base.Preconditions.checkNotNull")
   @GwtCompatible(serializable = true)
   public static <T> Ordering<T> from(Ordering<T> ordering) {
      return Preconditions.checkNotNull(ordering);
   }

   @GwtCompatible(serializable = true)
   public static <T> Ordering<T> explicit(List<T> valuesInOrder) {
      return new ExplicitOrdering<>(valuesInOrder);
   }

   @GwtCompatible(serializable = true)
   public static <T> Ordering<T> explicit(T leastValue, T... remainingValuesInOrder) {
      return explicit(Lists.asList(leastValue, remainingValuesInOrder));
   }

   @GwtCompatible(serializable = true)
   public static Ordering<@Nullable Object> allEqual() {
      return AllEqualOrdering.INSTANCE;
   }

   @GwtCompatible(serializable = true)
   public static Ordering<Object> usingToString() {
      return UsingToStringOrdering.INSTANCE;
   }

   @J2ktIncompatible
   public static Ordering<@Nullable Object> arbitrary() {
      return Ordering.ArbitraryOrderingHolder.ARBITRARY_ORDERING;
   }

   protected Ordering() {
   }

   @GwtCompatible(serializable = true)
   public <S extends T> Ordering<S> reverse() {
      return new ReverseOrdering<>(this);
   }

   @GwtCompatible(serializable = true)
   public <S extends T> Ordering<@Nullable S> nullsFirst() {
      return new NullsFirstOrdering<>(this);
   }

   @GwtCompatible(serializable = true)
   public <S extends T> Ordering<@Nullable S> nullsLast() {
      return new NullsLastOrdering<>(this);
   }

   @GwtCompatible(serializable = true)
   public <F> Ordering<F> onResultOf(Function<F, ? extends T> function) {
      return new ByFunctionOrdering<>(function, this);
   }

   <T2 extends T> Ordering<Entry<T2, ?>> onKeys() {
      return this.onResultOf(Maps.keyFunction());
   }

   @GwtCompatible(serializable = true)
   public <U extends T> Ordering<U> compound(Comparator<? super U> secondaryComparator) {
      return new CompoundOrdering<>(this, Preconditions.checkNotNull(secondaryComparator));
   }

   @GwtCompatible(serializable = true)
   public static <T> Ordering<T> compound(Iterable<? extends Comparator<? super T>> comparators) {
      return new CompoundOrdering<>(comparators);
   }

   @GwtCompatible(serializable = true)
   public <S extends T> Ordering<Iterable<S>> lexicographical() {
      return new LexicographicalOrdering<>(this);
   }

   @Override
   public abstract int compare(@ParametricNullness T left, @ParametricNullness T right);

   @ParametricNullness
   public <E extends T> E min(Iterator<E> iterator) {
      E minSoFar = iterator.next();

      while (iterator.hasNext()) {
         minSoFar = this.min(minSoFar, iterator.next());
      }

      return minSoFar;
   }

   @ParametricNullness
   public <E extends T> E min(Iterable<E> iterable) {
      return this.min(iterable.iterator());
   }

   @ParametricNullness
   public <E extends T> E min(@ParametricNullness E a, @ParametricNullness E b) {
      return this.compare((T)a, (T)b) <= 0 ? a : b;
   }

   @ParametricNullness
   public <E extends T> E min(@ParametricNullness E a, @ParametricNullness E b, @ParametricNullness E c, E... rest) {
      E minSoFar = this.min(this.min(a, b), c);

      for (E r : rest) {
         minSoFar = this.min(minSoFar, r);
      }

      return minSoFar;
   }

   @ParametricNullness
   public <E extends T> E max(Iterator<E> iterator) {
      E maxSoFar = iterator.next();

      while (iterator.hasNext()) {
         maxSoFar = this.max(maxSoFar, iterator.next());
      }

      return maxSoFar;
   }

   @ParametricNullness
   public <E extends T> E max(Iterable<E> iterable) {
      return this.max(iterable.iterator());
   }

   @ParametricNullness
   public <E extends T> E max(@ParametricNullness E a, @ParametricNullness E b) {
      return this.compare((T)a, (T)b) >= 0 ? a : b;
   }

   @ParametricNullness
   public <E extends T> E max(@ParametricNullness E a, @ParametricNullness E b, @ParametricNullness E c, E... rest) {
      E maxSoFar = this.max(this.max(a, b), c);

      for (E r : rest) {
         maxSoFar = this.max(maxSoFar, r);
      }

      return maxSoFar;
   }

   public <E extends T> List<E> leastOf(Iterable<E> iterable, int k) {
      if (iterable instanceof Collection) {
         Collection<E> collection = (Collection<E>)iterable;
         if (collection.size() <= 2L * k) {
            E[] array = (E[])collection.toArray();
            Arrays.sort(array, this);
            if (array.length > k) {
               array = Arrays.copyOf(array, k);
            }

            return Collections.unmodifiableList(Arrays.asList(array));
         }
      }

      return this.leastOf(iterable.iterator(), k);
   }

   public <E extends T> List<E> leastOf(Iterator<E> iterator, int k) {
      Preconditions.checkNotNull(iterator);
      CollectPreconditions.checkNonnegative(k, "k");
      if (k == 0 || !iterator.hasNext()) {
         return Collections.emptyList();
      }

      if (k >= 1073741823) {
         ArrayList<E> list = Lists.newArrayList(iterator);
         Collections.sort(list, this);
         if (list.size() > k) {
            list.subList(k, list.size()).clear();
         }

         list.trimToSize();
         return Collections.unmodifiableList(list);
      } else {
         TopKSelector<E> selector = TopKSelector.least(k, this);
         selector.offerAll(iterator);
         return selector.topK();
      }
   }

   public <E extends T> List<E> greatestOf(Iterable<E> iterable, int k) {
      return this.reverse().leastOf(iterable, k);
   }

   public <E extends T> List<E> greatestOf(Iterator<E> iterator, int k) {
      return this.reverse().leastOf(iterator, k);
   }

   public <E extends T> List<E> sortedCopy(Iterable<E> elements) {
      E[] array = (E[])Iterables.toArray(elements);
      Arrays.sort(array, this);
      return Lists.newArrayList(Arrays.asList(array));
   }

   public <E extends T> ImmutableList<E> immutableSortedCopy(Iterable<E> elements) {
      return ImmutableList.sortedCopyOf(this, elements);
   }

   public boolean isOrdered(Iterable<? extends T> iterable) {
      Iterator<? extends T> it = iterable.iterator();
      if (it.hasNext()) {
         T prev = (T)it.next();

         while (it.hasNext()) {
            T next = (T)it.next();
            if (this.compare(prev, next) > 0) {
               return false;
            }

            prev = next;
         }
      }

      return true;
   }

   public boolean isStrictlyOrdered(Iterable<? extends T> iterable) {
      Iterator<? extends T> it = iterable.iterator();
      if (it.hasNext()) {
         T prev = (T)it.next();

         while (it.hasNext()) {
            T next = (T)it.next();
            if (this.compare(prev, next) >= 0) {
               return false;
            }

            prev = next;
         }
      }

      return true;
   }

   @Deprecated
   @InlineMe(replacement = "Collections.binarySearch(sortedList, key, this)", imports = "java.util.Collections")
   @InlineMeValidationDisabled("While binarySearch() is not final, the inlining is still safe as long as any overrides follow the contract.")
   public int binarySearch(List<? extends T> sortedList, @ParametricNullness T key) {
      return Collections.binarySearch(sortedList, key, this);
   }

   @J2ktIncompatible
   @VisibleForTesting
   static class ArbitraryOrdering extends Ordering<Object> {
      private final AtomicInteger counter = new AtomicInteger(0);
      private final ConcurrentMap<Object, Integer> uids = Platform.tryWeakKeys(new MapMaker()).makeMap();

      private Integer getUid(Object obj) {
         Integer uid = this.uids.get(obj);
         if (uid == null) {
            uid = this.counter.getAndIncrement();
            Integer alreadySet = this.uids.putIfAbsent(obj, uid);
            if (alreadySet != null) {
               uid = alreadySet;
            }
         }

         return uid;
      }

      @Override
      public int compare(@Nullable Object left, @Nullable Object right) {
         if (left == right) {
            return 0;
         } else if (left == null) {
            return -1;
         } else if (right == null) {
            return 1;
         } else {
            int leftCode = this.identityHashCode(left);
            int rightCode = this.identityHashCode(right);
            if (leftCode != rightCode) {
               return leftCode < rightCode ? -1 : 1;
            } else {
               int result = this.getUid(left).compareTo(this.getUid(right));
               if (result == 0) {
                  throw new AssertionError();
               } else {
                  return result;
               }
            }
         }
      }

      @Override
      public String toString() {
         return "Ordering.arbitrary()";
      }

      int identityHashCode(Object object) {
         return System.identityHashCode(object);
      }
   }

   @J2ktIncompatible
   private static class ArbitraryOrderingHolder {
      static final Ordering<@Nullable Object> ARBITRARY_ORDERING = new Ordering.ArbitraryOrdering();
   }

   static class IncomparableValueException extends ClassCastException {
      final Object value;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      IncomparableValueException(Object value) {
         super("Cannot compare value: " + value);
         this.value = value;
      }
   }
}
