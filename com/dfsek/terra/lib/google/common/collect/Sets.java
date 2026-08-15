package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.dfsek.terra.lib.google.common.base.Predicates;
import com.dfsek.terra.lib.google.common.math.IntMath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.InlineMe;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class Sets {
   private Sets() {
   }

   @GwtCompatible(serializable = true)
   public static <E extends Enum<E>> ImmutableSet<E> immutableEnumSet(E anElement, E... otherElements) {
      return ImmutableEnumSet.asImmutable(EnumSet.of(anElement, otherElements));
   }

   @GwtCompatible(serializable = true)
   public static <E extends Enum<E>> ImmutableSet<E> immutableEnumSet(Iterable<E> elements) {
      if (elements instanceof ImmutableEnumSet) {
         return (ImmutableEnumSet)elements;
      } else if (elements instanceof Collection) {
         Collection<E> collection = (Collection<E>)elements;
         return collection.isEmpty() ? ImmutableSet.of() : ImmutableEnumSet.asImmutable(EnumSet.copyOf(collection));
      } else {
         Iterator<E> itr = elements.iterator();
         if (itr.hasNext()) {
            EnumSet<E> enumSet = EnumSet.of(itr.next());
            Iterators.addAll(enumSet, itr);
            return ImmutableEnumSet.asImmutable(enumSet);
         } else {
            return ImmutableSet.of();
         }
      }
   }

   public static <E extends Enum<E>> Collector<E, ?, ImmutableSet<E>> toImmutableEnumSet() {
      return CollectCollectors.toImmutableEnumSet();
   }

   public static <E extends Enum<E>> EnumSet<E> newEnumSet(Iterable<E> iterable, Class<E> elementType) {
      EnumSet<E> set = EnumSet.noneOf(elementType);
      Iterables.addAll(set, iterable);
      return set;
   }

   public static <E> HashSet<E> newHashSet() {
      return new HashSet<>();
   }

   public static <E> HashSet<E> newHashSet(E... elements) {
      HashSet<E> set = newHashSetWithExpectedSize(elements.length);
      Collections.addAll(set, elements);
      return set;
   }

   public static <E> HashSet<E> newHashSet(Iterable<? extends E> elements) {
      return elements instanceof Collection ? new HashSet<>((Collection<? extends E>)elements) : newHashSet(elements.iterator());
   }

   public static <E> HashSet<E> newHashSet(Iterator<? extends E> elements) {
      HashSet<E> set = newHashSet();
      Iterators.addAll(set, elements);
      return set;
   }

   public static <E> HashSet<E> newHashSetWithExpectedSize(int expectedSize) {
      return new HashSet<>(Maps.capacity(expectedSize));
   }

   public static <E> Set<E> newConcurrentHashSet() {
      return Platform.newConcurrentHashSet();
   }

   public static <E> Set<E> newConcurrentHashSet(Iterable<? extends E> elements) {
      Set<E> set = newConcurrentHashSet();
      Iterables.addAll(set, elements);
      return set;
   }

   public static <E> LinkedHashSet<E> newLinkedHashSet() {
      return new LinkedHashSet<>();
   }

   public static <E> LinkedHashSet<E> newLinkedHashSet(Iterable<? extends E> elements) {
      if (elements instanceof Collection) {
         return new LinkedHashSet<>((Collection<? extends E>)elements);
      }

      LinkedHashSet<E> set = newLinkedHashSet();
      Iterables.addAll(set, elements);
      return set;
   }

   public static <E> LinkedHashSet<E> newLinkedHashSetWithExpectedSize(int expectedSize) {
      return new LinkedHashSet<>(Maps.capacity(expectedSize));
   }

   public static <E extends Comparable> TreeSet<E> newTreeSet() {
      return new TreeSet<>();
   }

   public static <E extends Comparable> TreeSet<E> newTreeSet(Iterable<? extends E> elements) {
      TreeSet<E> set = newTreeSet();
      Iterables.addAll(set, elements);
      return set;
   }

   public static <E> TreeSet<E> newTreeSet(Comparator<? super E> comparator) {
      return new TreeSet<>(Preconditions.checkNotNull(comparator));
   }

   public static <E> Set<E> newIdentityHashSet() {
      return Collections.newSetFromMap(Maps.newIdentityHashMap());
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <E> CopyOnWriteArraySet<E> newCopyOnWriteArraySet() {
      return new CopyOnWriteArraySet<>();
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <E> CopyOnWriteArraySet<E> newCopyOnWriteArraySet(Iterable<? extends E> elements) {
      Collection<? extends E> elementsCollection = elements instanceof Collection ? (Collection)elements : Lists.newArrayList(elements);
      return new CopyOnWriteArraySet<>(elementsCollection);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <E extends Enum<E>> EnumSet<E> complementOf(Collection<E> collection) {
      if (collection instanceof EnumSet) {
         return EnumSet.complementOf((EnumSet<E>)collection);
      }

      Preconditions.checkArgument(!collection.isEmpty(), "collection is empty; use the other version of this method");
      Class<E> type = collection.iterator().next().getDeclaringClass();
      return makeComplementByHand(collection, type);
   }

   @J2ktIncompatible
   @GwtIncompatible
   public static <E extends Enum<E>> EnumSet<E> complementOf(Collection<E> collection, Class<E> type) {
      Preconditions.checkNotNull(collection);
      return collection instanceof EnumSet ? EnumSet.complementOf((EnumSet<E>)collection) : makeComplementByHand(collection, type);
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static <E extends Enum<E>> EnumSet<E> makeComplementByHand(Collection<E> collection, Class<E> type) {
      EnumSet<E> result = EnumSet.allOf(type);
      result.removeAll(collection);
      return result;
   }

   @Deprecated
   @InlineMe(replacement = "Collections.newSetFromMap(map)", imports = "java.util.Collections")
   public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
      return Collections.newSetFromMap(map);
   }

   public static <E> Sets.SetView<E> union(Set<? extends E> set1, Set<? extends E> set2) {
      Preconditions.checkNotNull(set1, "set1");
      Preconditions.checkNotNull(set2, "set2");
      return new Sets.SetView<E>() {
         @Override
         public int size() {
            int size = set1.size();

            for (E e : set2) {
               if (!set1.contains(e)) {
                  size++;
               }
            }

            return size;
         }

         @Override
         public boolean isEmpty() {
            return set1.isEmpty() && set2.isEmpty();
         }

         @Override
         public UnmodifiableIterator<E> iterator() {
            return new AbstractIterator<E>() {
               final Iterator<? extends E> itr1 = set1.iterator();
               final Iterator<? extends E> itr2 = set2.iterator();

               @Override
               protected @Nullable E computeNext() {
                  if (this.itr1.hasNext()) {
                     return (E)this.itr1.next();
                  }

                  while (this.itr2.hasNext()) {
                     E e = (E)this.itr2.next();
                     if (!set1.contains(e)) {
                        return e;
                     }
                  }

                  return (E)this.endOfData();
               }
            };
         }

         @Override
         public Stream<E> stream() {
            return Stream.concat(set1.stream(), set2.stream().filter(e -> !set1.contains(e)));
         }

         @Override
         public Stream<E> parallelStream() {
            return (Stream<E>)this.stream().parallel();
         }

         @Override
         public boolean contains(@Nullable Object object) {
            return set1.contains(object) || set2.contains(object);
         }

         @Override
         public <S extends Set<E>> S copyInto(S set) {
            set.addAll(set1);
            set.addAll(set2);
            return set;
         }

         @Override
         int upperBoundSize() {
            return upperBoundSize(set1) + upperBoundSize(set2);
         }
      };
   }

   public static <E> Sets.SetView<E> intersection(Set<E> set1, Set<?> set2) {
      Preconditions.checkNotNull(set1, "set1");
      Preconditions.checkNotNull(set2, "set2");
      return new Sets.SetView<E>() {
         @Override
         public UnmodifiableIterator<E> iterator() {
            return new AbstractIterator<E>() {
               final Iterator<E> itr = set1.iterator();

               @Override
               protected @Nullable E computeNext() {
                  while (this.itr.hasNext()) {
                     E e = this.itr.next();
                     if (set2.contains(e)) {
                        return e;
                     }
                  }

                  return (E)this.endOfData();
               }
            };
         }

         @Override
         public Stream<E> stream() {
            return set1.stream().filter(set2::contains);
         }

         @Override
         public Stream<E> parallelStream() {
            return set1.parallelStream().filter(set2::contains);
         }

         @Override
         public int size() {
            int size = 0;

            for (E e : set1) {
               if (set2.contains(e)) {
                  size++;
               }
            }

            return size;
         }

         @Override
         public boolean isEmpty() {
            return Collections.disjoint(set2, set1);
         }

         @Override
         public boolean contains(@Nullable Object object) {
            return set1.contains(object) && set2.contains(object);
         }

         @Override
         public boolean containsAll(Collection<?> collection) {
            return set1.containsAll(collection) && set2.containsAll(collection);
         }

         @Override
         int upperBoundSize() {
            return Math.min(upperBoundSize(set1), upperBoundSize(set2));
         }
      };
   }

   public static <E> Sets.SetView<E> difference(Set<E> set1, Set<?> set2) {
      Preconditions.checkNotNull(set1, "set1");
      Preconditions.checkNotNull(set2, "set2");
      return new Sets.SetView<E>() {
         @Override
         public UnmodifiableIterator<E> iterator() {
            return new AbstractIterator<E>() {
               final Iterator<E> itr = set1.iterator();

               @Override
               protected @Nullable E computeNext() {
                  while (this.itr.hasNext()) {
                     E e = this.itr.next();
                     if (!set2.contains(e)) {
                        return e;
                     }
                  }

                  return (E)this.endOfData();
               }
            };
         }

         @Override
         public Stream<E> stream() {
            return set1.stream().filter(e -> !set2.contains(e));
         }

         @Override
         public Stream<E> parallelStream() {
            return set1.parallelStream().filter(e -> !set2.contains(e));
         }

         @Override
         public int size() {
            int size = 0;

            for (E e : set1) {
               if (!set2.contains(e)) {
                  size++;
               }
            }

            return size;
         }

         @Override
         public boolean isEmpty() {
            return set2.containsAll(set1);
         }

         @Override
         public boolean contains(@Nullable Object element) {
            return set1.contains(element) && !set2.contains(element);
         }

         @Override
         int upperBoundSize() {
            return upperBoundSize(set1);
         }
      };
   }

   public static <E> Sets.SetView<E> symmetricDifference(Set<? extends E> set1, Set<? extends E> set2) {
      Preconditions.checkNotNull(set1, "set1");
      Preconditions.checkNotNull(set2, "set2");
      return new Sets.SetView<E>() {
         @Override
         public UnmodifiableIterator<E> iterator() {
            final Iterator<? extends E> itr1 = set1.iterator();
            final Iterator<? extends E> itr2 = set2.iterator();
            return new AbstractIterator<E>() {
               @Override
               public @Nullable E computeNext() {
                  while (itr1.hasNext()) {
                     E elem1 = (E)itr1.next();
                     if (!set2.contains(elem1)) {
                        return elem1;
                     }
                  }

                  while (itr2.hasNext()) {
                     E elem2 = (E)itr2.next();
                     if (!set1.contains(elem2)) {
                        return elem2;
                     }
                  }

                  return (E)this.endOfData();
               }
            };
         }

         @Override
         public int size() {
            int size = 0;

            for (E e : set1) {
               if (!set2.contains(e)) {
                  size++;
               }
            }

            for (E e : set2) {
               if (!set1.contains(e)) {
                  size++;
               }
            }

            return size;
         }

         @Override
         public boolean isEmpty() {
            return set1.equals(set2);
         }

         @Override
         public boolean contains(@Nullable Object element) {
            return set1.contains(element) ^ set2.contains(element);
         }

         @Override
         int upperBoundSize() {
            return upperBoundSize(set1) + upperBoundSize(set2);
         }
      };
   }

   public static <E> Set<E> filter(Set<E> unfiltered, Predicate<? super E> predicate) {
      if (unfiltered instanceof SortedSet) {
         return filter((SortedSet<E>)unfiltered, predicate);
      } else if (unfiltered instanceof Sets.FilteredSet) {
         Sets.FilteredSet<E> filtered = (Sets.FilteredSet<E>)unfiltered;
         Predicate<E> combinedPredicate = Predicates.and(filtered.predicate, predicate);
         return new Sets.FilteredSet<>((Set<E>)filtered.unfiltered, combinedPredicate);
      } else {
         return new Sets.FilteredSet<>(Preconditions.checkNotNull(unfiltered), Preconditions.checkNotNull(predicate));
      }
   }

   public static <E> SortedSet<E> filter(SortedSet<E> unfiltered, Predicate<? super E> predicate) {
      if (unfiltered instanceof Sets.FilteredSet) {
         Sets.FilteredSet<E> filtered = (Sets.FilteredSet<E>)unfiltered;
         Predicate<E> combinedPredicate = Predicates.and(filtered.predicate, predicate);
         return new Sets.FilteredSortedSet<>((SortedSet<E>)filtered.unfiltered, combinedPredicate);
      } else {
         return new Sets.FilteredSortedSet<>(Preconditions.checkNotNull(unfiltered), Preconditions.checkNotNull(predicate));
      }
   }

   @GwtIncompatible
   public static <E> NavigableSet<E> filter(NavigableSet<E> unfiltered, Predicate<? super E> predicate) {
      if (unfiltered instanceof Sets.FilteredSet) {
         Sets.FilteredSet<E> filtered = (Sets.FilteredSet<E>)unfiltered;
         Predicate<E> combinedPredicate = Predicates.and(filtered.predicate, predicate);
         return new Sets.FilteredNavigableSet<>((NavigableSet<E>)filtered.unfiltered, combinedPredicate);
      } else {
         return new Sets.FilteredNavigableSet<>(Preconditions.checkNotNull(unfiltered), Preconditions.checkNotNull(predicate));
      }
   }

   public static <B> Set<List<B>> cartesianProduct(List<? extends Set<? extends B>> sets) {
      return Sets.CartesianSet.create(sets);
   }

   @SafeVarargs
   public static <B> Set<List<B>> cartesianProduct(Set<? extends B>... sets) {
      return cartesianProduct(Arrays.asList(sets));
   }

   @GwtCompatible(serializable = false)
   public static <E> Set<Set<E>> powerSet(Set<E> set) {
      return new Sets.PowerSet<>(set);
   }

   public static <E> Set<Set<E>> combinations(Set<E> set, int size) {
      final ImmutableMap<E, Integer> index = Maps.indexMap(set);
      CollectPreconditions.checkNonnegative(size, "size");
      Preconditions.checkArgument(size <= index.size(), "size (%s) must be <= set.size() (%s)", size, index.size());
      if (size == 0) {
         return ImmutableSet.of(ImmutableSet.of());
      } else {
         return size == index.size() ? ImmutableSet.of(index.keySet()) : new AbstractSet<Set<E>>() {
            @Override
            public boolean contains(@Nullable Object o) {
               if (!(o instanceof Set)) {
                  return false;
               }

               Set<?> s = (Set<?>)o;
               return s.size() == size && index.keySet().containsAll(s);
            }

            @Override
            public Iterator<Set<E>> iterator() {
               return new AbstractIterator<Set<E>>() {
                  final BitSet bits = new BitSet(index.size());

                  protected @Nullable Set<E> computeNext() {
                     if (this.bits.isEmpty()) {
                        this.bits.set(0, size);
                     } else {
                        int firstSetBit = this.bits.nextSetBit(0);
                        int bitToFlip = this.bits.nextClearBit(firstSetBit);
                        if (bitToFlip == index.size()) {
                           return this.endOfData();
                        }

                        this.bits.set(0, bitToFlip - firstSetBit - 1);
                        this.bits.clear(bitToFlip - firstSetBit - 1, bitToFlip);
                        this.bits.set(bitToFlip);
                     }

                     final BitSet copy = (BitSet)this.bits.clone();
                     return new AbstractSet<E>() {
                        @Override
                        public boolean contains(@Nullable Object o) {
                           Integer i = index.get(o);
                           return i != null && copy.get(i);
                        }

                        @Override
                        public Iterator<E> iterator() {
                           return new AbstractIterator<E>() {
                              int i = -1;

                              @Override
                              protected @Nullable E computeNext() {
                                 this.i = copy.nextSetBit(this.i + 1);
                                 return (E)(this.i == -1 ? this.endOfData() : index.keySet().asList().get(this.i));
                              }
                           };
                        }

                        @Override
                        public int size() {
                           return size;
                        }
                     };
                  }
               };
            }

            @Override
            public int size() {
               return IntMath.binomial(index.size(), size);
            }

            @Override
            public String toString() {
               return "Sets.combinations(" + index.keySet() + ", " + size + ")";
            }
         };
      }
   }

   static int hashCodeImpl(Set<?> s) {
      int hashCode = 0;

      for (Object o : s) {
         hashCode += o != null ? o.hashCode() : 0;
         hashCode = ~(~hashCode);
      }

      return hashCode;
   }

   static boolean equalsImpl(Set<?> s, @Nullable Object object) {
      if (s == object) {
         return true;
      }

      if (object instanceof Set) {
         Set<?> o = (Set<?>)object;

         try {
            return s.size() == o.size() && s.containsAll(o);
         } catch (NullPointerException | ClassCastException ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static <E> NavigableSet<E> unmodifiableNavigableSet(NavigableSet<E> set) {
      return !(set instanceof ImmutableCollection) && !(set instanceof Sets.UnmodifiableNavigableSet) ? new Sets.UnmodifiableNavigableSet<>(set) : set;
   }

   @GwtIncompatible
   @J2ktIncompatible
   public static <E> NavigableSet<E> synchronizedNavigableSet(NavigableSet<E> navigableSet) {
      return Synchronized.navigableSet(navigableSet);
   }

   static boolean removeAllImpl(Set<?> set, Iterator<?> iterator) {
      boolean changed = false;

      while (iterator.hasNext()) {
         changed |= set.remove(iterator.next());
      }

      return changed;
   }

   static boolean removeAllImpl(Set<?> set, Collection<?> collection) {
      Preconditions.checkNotNull(collection);
      if (collection instanceof Multiset) {
         collection = ((Multiset)collection).elementSet();
      }

      return collection instanceof Set && collection.size() > set.size()
         ? Iterators.removeAll(set.iterator(), collection)
         : removeAllImpl(set, collection.iterator());
   }

   @GwtIncompatible
   public static <K extends Comparable<? super K>> NavigableSet<K> subSet(NavigableSet<K> set, Range<K> range) {
      if (set.comparator() != null && set.comparator() != Ordering.natural() && range.hasLowerBound() && range.hasUpperBound()) {
         Preconditions.checkArgument(
            set.comparator().compare(range.lowerEndpoint(), range.upperEndpoint()) <= 0,
            "set is using a custom comparator which is inconsistent with the natural ordering."
         );
      }

      if (range.hasLowerBound() && range.hasUpperBound()) {
         return set.subSet(range.lowerEndpoint(), range.lowerBoundType() == BoundType.CLOSED, range.upperEndpoint(), range.upperBoundType() == BoundType.CLOSED);
      } else if (range.hasLowerBound()) {
         return set.tailSet(range.lowerEndpoint(), range.lowerBoundType() == BoundType.CLOSED);
      } else {
         return range.hasUpperBound() ? set.headSet(range.upperEndpoint(), range.upperBoundType() == BoundType.CLOSED) : Preconditions.checkNotNull(set);
      }
   }

   private static final class CartesianSet<E> extends ForwardingCollection<List<E>> implements Set<List<E>> {
      private final transient ImmutableList<ImmutableSet<E>> axes;
      private final transient CartesianList<E> delegate;

      static <E> Set<List<E>> create(List<? extends Set<? extends E>> sets) {
         ImmutableList.Builder<ImmutableSet<E>> axesBuilder = new ImmutableList.Builder<>(sets.size());

         for (Set<? extends E> set : sets) {
            ImmutableSet<E> copy = ImmutableSet.copyOf(set);
            if (copy.isEmpty()) {
               return ImmutableSet.of();
            }

            axesBuilder.add(copy);
         }

         final ImmutableList<ImmutableSet<E>> axes = axesBuilder.build();
         ImmutableList<List<E>> listAxes = new ImmutableList<List<E>>() {
            @Override
            public int size() {
               return axes.size();
            }

            public List<E> get(int index) {
               return axes.get(index).asList();
            }

            @Override
            boolean isPartialView() {
               return true;
            }

            @J2ktIncompatible
            @GwtIncompatible
            @Override
            Object writeReplace() {
               return super.writeReplace();
            }
         };
         return new Sets.CartesianSet<>(axes, new CartesianList<>(listAxes));
      }

      private CartesianSet(ImmutableList<ImmutableSet<E>> axes, CartesianList<E> delegate) {
         this.axes = axes;
         this.delegate = delegate;
      }

      @Override
      protected Collection<List<E>> delegate() {
         return this.delegate;
      }

      @Override
      public boolean contains(@Nullable Object object) {
         if (!(object instanceof List)) {
            return false;
         }

         List<?> list = (List<?>)object;
         if (list.size() != this.axes.size()) {
            return false;
         }

         int i = 0;

         for (Object o : list) {
            if (!this.axes.get(i).contains(o)) {
               return false;
            }

            i++;
         }

         return true;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (object instanceof Sets.CartesianSet) {
            Sets.CartesianSet<?> that = (Sets.CartesianSet<?>)object;
            return this.axes.equals(that.axes);
         }

         if (!(object instanceof Set)) {
            return false;
         }

         Set<?> that = (Set<?>)object;
         return this.size() == that.size() && this.containsAll(that);
      }

      @Override
      public int hashCode() {
         int adjust = this.size() - 1;

         for (int i = 0; i < this.axes.size(); i++) {
            adjust *= 31;
            adjust = ~(~adjust);
         }

         int hash = 1;

         for (Set<E> axis : this.axes) {
            hash = 31 * hash + this.size() / axis.size() * axis.hashCode();
            hash = ~(~hash);
         }

         hash += adjust;
         return ~(~hash);
      }
   }

   @GwtIncompatible
   static class DescendingSet<E> extends ForwardingNavigableSet<E> {
      private final NavigableSet<E> forward;

      DescendingSet(NavigableSet<E> forward) {
         this.forward = forward;
      }

      @Override
      protected NavigableSet<E> delegate() {
         return this.forward;
      }

      @Override
      public @Nullable E lower(@ParametricNullness E e) {
         return this.forward.higher(e);
      }

      @Override
      public @Nullable E floor(@ParametricNullness E e) {
         return this.forward.ceiling(e);
      }

      @Override
      public @Nullable E ceiling(@ParametricNullness E e) {
         return this.forward.floor(e);
      }

      @Override
      public @Nullable E higher(@ParametricNullness E e) {
         return this.forward.lower(e);
      }

      @Override
      public @Nullable E pollFirst() {
         return this.forward.pollLast();
      }

      @Override
      public @Nullable E pollLast() {
         return this.forward.pollFirst();
      }

      @Override
      public NavigableSet<E> descendingSet() {
         return this.forward;
      }

      @Override
      public Iterator<E> descendingIterator() {
         return this.forward.iterator();
      }

      @Override
      public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
         return this.forward.subSet(toElement, toInclusive, fromElement, fromInclusive).descendingSet();
      }

      @Override
      public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
         return this.standardSubSet(fromElement, toElement);
      }

      @Override
      public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
         return this.forward.tailSet(toElement, inclusive).descendingSet();
      }

      @Override
      public SortedSet<E> headSet(@ParametricNullness E toElement) {
         return this.standardHeadSet(toElement);
      }

      @Override
      public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
         return this.forward.headSet(fromElement, inclusive).descendingSet();
      }

      @Override
      public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
         return this.standardTailSet(fromElement);
      }

      @Override
      public Comparator<? super E> comparator() {
         Comparator<? super E> forwardComparator = this.forward.comparator();
         return forwardComparator == null ? Ordering.natural().reverse() : reverse(forwardComparator);
      }

      private static <T> Ordering<T> reverse(Comparator<T> forward) {
         return Ordering.from(forward).reverse();
      }

      @ParametricNullness
      @Override
      public E first() {
         return this.forward.last();
      }

      @ParametricNullness
      @Override
      public E last() {
         return this.forward.first();
      }

      @Override
      public Iterator<E> iterator() {
         return this.forward.descendingIterator();
      }

      @Override
      public @Nullable Object[] toArray() {
         return this.standardToArray();
      }

      @Override
      public <T> T[] toArray(T[] array) {
         return (T[])this.standardToArray(array);
      }

      @Override
      public String toString() {
         return this.standardToString();
      }
   }

   @GwtIncompatible
   private static class FilteredNavigableSet<E> extends Sets.FilteredSortedSet<E> implements NavigableSet<E> {
      FilteredNavigableSet(NavigableSet<E> unfiltered, Predicate<? super E> predicate) {
         super(unfiltered, predicate);
      }

      NavigableSet<E> unfiltered() {
         return (NavigableSet<E>)this.unfiltered;
      }

      @Override
      public @Nullable E lower(@ParametricNullness E e) {
         return Iterators.find(this.unfiltered().headSet(e, false).descendingIterator(), this.predicate, null);
      }

      @Override
      public @Nullable E floor(@ParametricNullness E e) {
         return Iterators.find(this.unfiltered().headSet(e, true).descendingIterator(), this.predicate, null);
      }

      @Override
      public @Nullable E ceiling(@ParametricNullness E e) {
         return Iterables.find(this.unfiltered().tailSet(e, true), this.predicate, null);
      }

      @Override
      public @Nullable E higher(@ParametricNullness E e) {
         return Iterables.find(this.unfiltered().tailSet(e, false), this.predicate, null);
      }

      @Override
      public @Nullable E pollFirst() {
         return Iterables.removeFirstMatching(this.unfiltered(), this.predicate);
      }

      @Override
      public @Nullable E pollLast() {
         return Iterables.removeFirstMatching(this.unfiltered().descendingSet(), this.predicate);
      }

      @Override
      public NavigableSet<E> descendingSet() {
         return Sets.filter(this.unfiltered().descendingSet(), this.predicate);
      }

      @Override
      public Iterator<E> descendingIterator() {
         return Iterators.filter(this.unfiltered().descendingIterator(), this.predicate);
      }

      @ParametricNullness
      @Override
      public E last() {
         return Iterators.find(this.unfiltered().descendingIterator(), this.predicate);
      }

      @Override
      public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
         return Sets.filter(this.unfiltered().subSet(fromElement, fromInclusive, toElement, toInclusive), this.predicate);
      }

      @Override
      public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
         return Sets.filter(this.unfiltered().headSet(toElement, inclusive), this.predicate);
      }

      @Override
      public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
         return Sets.filter(this.unfiltered().tailSet(fromElement, inclusive), this.predicate);
      }
   }

   private static class FilteredSet<E> extends Collections2.FilteredCollection<E> implements Set<E> {
      FilteredSet(Set<E> unfiltered, Predicate<? super E> predicate) {
         super(unfiltered, predicate);
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

   private static class FilteredSortedSet<E> extends Sets.FilteredSet<E> implements SortedSet<E> {
      FilteredSortedSet(SortedSet<E> unfiltered, Predicate<? super E> predicate) {
         super(unfiltered, predicate);
      }

      @Override
      public @Nullable Comparator<? super E> comparator() {
         return ((SortedSet)this.unfiltered).comparator();
      }

      @Override
      public SortedSet<E> subSet(@ParametricNullness E fromElement, @ParametricNullness E toElement) {
         return new Sets.FilteredSortedSet<>(((SortedSet)this.unfiltered).subSet(fromElement, toElement), this.predicate);
      }

      @Override
      public SortedSet<E> headSet(@ParametricNullness E toElement) {
         return new Sets.FilteredSortedSet<>(((SortedSet)this.unfiltered).headSet(toElement), this.predicate);
      }

      @Override
      public SortedSet<E> tailSet(@ParametricNullness E fromElement) {
         return new Sets.FilteredSortedSet<>(((SortedSet)this.unfiltered).tailSet(fromElement), this.predicate);
      }

      @ParametricNullness
      @Override
      public E first() {
         return Iterators.find(this.unfiltered.iterator(), this.predicate);
      }

      @ParametricNullness
      @Override
      public E last() {
         SortedSet<E> sortedUnfiltered = (SortedSet<E>)this.unfiltered;

         while (true) {
            E element = sortedUnfiltered.last();
            if (this.predicate.apply(element)) {
               return element;
            }

            sortedUnfiltered = sortedUnfiltered.headSet(element);
         }
      }
   }

   abstract static class ImprovedAbstractSet<E> extends AbstractSet<E> {
      @Override
      public boolean removeAll(Collection<?> c) {
         return Sets.removeAllImpl(this, c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         return super.retainAll(Preconditions.checkNotNull(c));
      }
   }

   private static final class PowerSet<E> extends AbstractSet<Set<E>> {
      final ImmutableMap<E, Integer> inputSet;

      PowerSet(Set<E> input) {
         Preconditions.checkArgument(input.size() <= 30, "Too many elements to create power set: %s > 30", input.size());
         this.inputSet = Maps.indexMap(input);
      }

      @Override
      public int size() {
         return 1 << this.inputSet.size();
      }

      @Override
      public boolean isEmpty() {
         return false;
      }

      @Override
      public Iterator<Set<E>> iterator() {
         return new AbstractIndexedListIterator<Set<E>>(this.size()) {
            protected Set<E> get(int setBits) {
               return new Sets.SubSet<>(PowerSet.this.inputSet, setBits);
            }
         };
      }

      @Override
      public boolean contains(@Nullable Object obj) {
         if (obj instanceof Set) {
            Set<?> set = (Set<?>)obj;
            return this.inputSet.keySet().containsAll(set);
         } else {
            return false;
         }
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj instanceof Sets.PowerSet) {
            Sets.PowerSet<?> that = (Sets.PowerSet<?>)obj;
            return this.inputSet.keySet().equals(that.inputSet.keySet());
         } else {
            return super.equals(obj);
         }
      }

      @Override
      public int hashCode() {
         return this.inputSet.keySet().hashCode() << this.inputSet.size() - 1;
      }

      @Override
      public String toString() {
         return "powerSet(" + this.inputSet + ")";
      }
   }

   public abstract static class SetView<E> extends AbstractSet<E> {
      private SetView() {
      }

      public ImmutableSet<E> immutableCopy() {
         int upperBoundSize = this.upperBoundSize();
         if (upperBoundSize == 0) {
            return ImmutableSet.of();
         }

         ImmutableSet.Builder<E> builder = ImmutableSet.builderWithExpectedSize(upperBoundSize);

         for (E element : this) {
            builder.add(Preconditions.checkNotNull(element));
         }

         return builder.build();
      }

      @CanIgnoreReturnValue
      public <S extends Set<E>> S copyInto(S set) {
         set.addAll(this);
         return set;
      }

      @Deprecated
      @CanIgnoreReturnValue
      @DoNotCall("Always throws UnsupportedOperationException")
      @Override
      public final boolean add(@ParametricNullness E e) {
         throw new UnsupportedOperationException();
      }

      @Deprecated
      @CanIgnoreReturnValue
      @DoNotCall("Always throws UnsupportedOperationException")
      @Override
      public final boolean remove(@Nullable Object object) {
         throw new UnsupportedOperationException();
      }

      @Deprecated
      @CanIgnoreReturnValue
      @DoNotCall("Always throws UnsupportedOperationException")
      @Override
      public final boolean addAll(Collection<? extends E> newElements) {
         throw new UnsupportedOperationException();
      }

      @Deprecated
      @CanIgnoreReturnValue
      @DoNotCall("Always throws UnsupportedOperationException")
      @Override
      public final boolean removeAll(Collection<?> oldElements) {
         throw new UnsupportedOperationException();
      }

      @Deprecated
      @CanIgnoreReturnValue
      @DoNotCall("Always throws UnsupportedOperationException")
      @Override
      public final boolean removeIf(java.util.function.Predicate<? super E> filter) {
         throw new UnsupportedOperationException();
      }

      @Deprecated
      @CanIgnoreReturnValue
      @DoNotCall("Always throws UnsupportedOperationException")
      @Override
      public final boolean retainAll(Collection<?> elementsToKeep) {
         throw new UnsupportedOperationException();
      }

      @Deprecated
      @DoNotCall("Always throws UnsupportedOperationException")
      @Override
      public final void clear() {
         throw new UnsupportedOperationException();
      }

      public abstract UnmodifiableIterator<E> iterator();

      abstract int upperBoundSize();

      static int upperBoundSize(Set<?> set) {
         return set instanceof Sets.SetView ? ((Sets.SetView)set).upperBoundSize() : set.size();
      }
   }

   private static final class SubSet<E> extends AbstractSet<E> {
      private final ImmutableMap<E, Integer> inputSet;
      private final int mask;

      SubSet(ImmutableMap<E, Integer> inputSet, int mask) {
         this.inputSet = inputSet;
         this.mask = mask;
      }

      @Override
      public Iterator<E> iterator() {
         return new UnmodifiableIterator<E>() {
            final ImmutableList<E> elements = SubSet.this.inputSet.keySet().asList();
            int remainingSetBits = SubSet.this.mask;

            @Override
            public boolean hasNext() {
               return this.remainingSetBits != 0;
            }

            @Override
            public E next() {
               int index = Integer.numberOfTrailingZeros(this.remainingSetBits);
               if (index == 32) {
                  throw new NoSuchElementException();
               }

               this.remainingSetBits &= ~(1 << index);
               return this.elements.get(index);
            }
         };
      }

      @Override
      public int size() {
         return Integer.bitCount(this.mask);
      }

      @Override
      public boolean contains(@Nullable Object o) {
         Integer index = this.inputSet.get(o);
         return index != null && (this.mask & 1 << index) != 0;
      }
   }

   static final class UnmodifiableNavigableSet<E> extends ForwardingSortedSet<E> implements NavigableSet<E>, Serializable {
      private final NavigableSet<E> delegate;
      private final SortedSet<E> unmodifiableDelegate;
      @LazyInit
      private transient Sets.@Nullable UnmodifiableNavigableSet<E> descendingSet;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      UnmodifiableNavigableSet(NavigableSet<E> delegate) {
         this.delegate = Preconditions.checkNotNull(delegate);
         this.unmodifiableDelegate = Collections.unmodifiableSortedSet(delegate);
      }

      @Override
      protected SortedSet<E> delegate() {
         return this.unmodifiableDelegate;
      }

      @Override
      public boolean removeIf(java.util.function.Predicate<? super E> filter) {
         throw new UnsupportedOperationException();
      }

      @Override
      public Stream<E> stream() {
         return this.delegate.stream();
      }

      @Override
      public Stream<E> parallelStream() {
         return this.delegate.parallelStream();
      }

      @Override
      public void forEach(Consumer<? super E> action) {
         this.delegate.forEach(action);
      }

      @Override
      public @Nullable E lower(@ParametricNullness E e) {
         return this.delegate.lower(e);
      }

      @Override
      public @Nullable E floor(@ParametricNullness E e) {
         return this.delegate.floor(e);
      }

      @Override
      public @Nullable E ceiling(@ParametricNullness E e) {
         return this.delegate.ceiling(e);
      }

      @Override
      public @Nullable E higher(@ParametricNullness E e) {
         return this.delegate.higher(e);
      }

      @Override
      public @Nullable E pollFirst() {
         throw new UnsupportedOperationException();
      }

      @Override
      public @Nullable E pollLast() {
         throw new UnsupportedOperationException();
      }

      @Override
      public NavigableSet<E> descendingSet() {
         Sets.UnmodifiableNavigableSet<E> result = this.descendingSet;
         if (result == null) {
            result = this.descendingSet = new Sets.UnmodifiableNavigableSet<>(this.delegate.descendingSet());
            result.descendingSet = this;
         }

         return result;
      }

      @Override
      public Iterator<E> descendingIterator() {
         return Iterators.unmodifiableIterator(this.delegate.descendingIterator());
      }

      @Override
      public NavigableSet<E> subSet(@ParametricNullness E fromElement, boolean fromInclusive, @ParametricNullness E toElement, boolean toInclusive) {
         return Sets.unmodifiableNavigableSet(this.delegate.subSet(fromElement, fromInclusive, toElement, toInclusive));
      }

      @Override
      public NavigableSet<E> headSet(@ParametricNullness E toElement, boolean inclusive) {
         return Sets.unmodifiableNavigableSet(this.delegate.headSet(toElement, inclusive));
      }

      @Override
      public NavigableSet<E> tailSet(@ParametricNullness E fromElement, boolean inclusive) {
         return Sets.unmodifiableNavigableSet(this.delegate.tailSet(fromElement, inclusive));
      }
   }
}
