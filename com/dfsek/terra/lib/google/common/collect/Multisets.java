package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.dfsek.terra.lib.google.common.base.Predicates;
import com.dfsek.terra.lib.google.common.math.IntMath;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.InlineMe;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Multisets {
   private Multisets() {
   }

   public static <T, E, M extends Multiset<E>> Collector<T, ?, M> toMultiset(
      Function<? super T, E> elementFunction, ToIntFunction<? super T> countFunction, Supplier<M> multisetSupplier
   ) {
      return CollectCollectors.toMultiset(elementFunction, countFunction, multisetSupplier);
   }

   public static <E> Multiset<E> unmodifiableMultiset(Multiset<? extends E> multiset) {
      return !(multiset instanceof Multisets.UnmodifiableMultiset) && !(multiset instanceof ImmutableMultiset)
         ? new Multisets.UnmodifiableMultiset<>(Preconditions.checkNotNull(multiset))
         : multiset;
   }

   @Deprecated
   @InlineMe(replacement = "checkNotNull(multiset)", staticImports = "com.dfsek.terra.lib.google.common.base.Preconditions.checkNotNull")
   public static <E> Multiset<E> unmodifiableMultiset(ImmutableMultiset<E> multiset) {
      return Preconditions.checkNotNull(multiset);
   }

   public static <E> SortedMultiset<E> unmodifiableSortedMultiset(SortedMultiset<E> sortedMultiset) {
      return new UnmodifiableSortedMultiset<>(Preconditions.checkNotNull(sortedMultiset));
   }

   public static <E> Multiset.Entry<E> immutableEntry(@ParametricNullness E e, int n) {
      return new Multisets.ImmutableEntry<>(e, n);
   }

   public static <E> Multiset<E> filter(Multiset<E> unfiltered, Predicate<? super E> predicate) {
      if (unfiltered instanceof Multisets.FilteredMultiset) {
         Multisets.FilteredMultiset<E> filtered = (Multisets.FilteredMultiset<E>)unfiltered;
         Predicate<E> combinedPredicate = Predicates.and(filtered.predicate, predicate);
         return new Multisets.FilteredMultiset<>(filtered.unfiltered, combinedPredicate);
      } else {
         return new Multisets.FilteredMultiset<>(unfiltered, predicate);
      }
   }

   static int inferDistinctElements(Iterable<?> elements) {
      return elements instanceof Multiset ? ((Multiset)elements).elementSet().size() : 11;
   }

   public static <E> Multiset<E> union(Multiset<? extends E> multiset1, Multiset<? extends E> multiset2) {
      Preconditions.checkNotNull(multiset1);
      Preconditions.checkNotNull(multiset2);
      return new Multisets.ViewMultiset<E>() {
         @Override
         public boolean contains(@Nullable Object element) {
            return multiset1.contains(element) || multiset2.contains(element);
         }

         @Override
         public boolean isEmpty() {
            return multiset1.isEmpty() && multiset2.isEmpty();
         }

         @Override
         public int count(@Nullable Object element) {
            return Math.max(multiset1.count(element), multiset2.count(element));
         }

         @Override
         Set<E> createElementSet() {
            return Sets.union(multiset1.elementSet(), multiset2.elementSet());
         }

         @Override
         Iterator<E> elementIterator() {
            throw new AssertionError("should never be called");
         }

         @Override
         Iterator<Multiset.Entry<E>> entryIterator() {
            final Iterator<? extends Multiset.Entry<? extends E>> iterator1 = multiset1.entrySet().iterator();
            final Iterator<? extends Multiset.Entry<? extends E>> iterator2 = multiset2.entrySet().iterator();
            return new AbstractIterator<Multiset.Entry<E>>() {
               protected Multiset.@Nullable Entry<E> computeNext() {
                  if (iterator1.hasNext()) {
                     Multiset.Entry<? extends E> entry1 = (Multiset.Entry<? extends E>)iterator1.next();
                     E element = (E)entry1.getElement();
                     int count = Math.max(entry1.getCount(), multiset2.count(element));
                     return Multisets.immutableEntry(element, count);
                  }

                  while (iterator2.hasNext()) {
                     Multiset.Entry<? extends E> entry2 = (Multiset.Entry<? extends E>)iterator2.next();
                     E element = (E)entry2.getElement();
                     if (!multiset1.contains(element)) {
                        return Multisets.immutableEntry(element, entry2.getCount());
                     }
                  }

                  return this.endOfData();
               }
            };
         }
      };
   }

   public static <E> Multiset<E> intersection(Multiset<E> multiset1, Multiset<?> multiset2) {
      Preconditions.checkNotNull(multiset1);
      Preconditions.checkNotNull(multiset2);
      return new Multisets.ViewMultiset<E>() {
         @Override
         public int count(@Nullable Object element) {
            int count1 = multiset1.count(element);
            return count1 == 0 ? 0 : Math.min(count1, multiset2.count(element));
         }

         @Override
         Set<E> createElementSet() {
            return Sets.intersection(multiset1.elementSet(), multiset2.elementSet());
         }

         @Override
         Iterator<E> elementIterator() {
            throw new AssertionError("should never be called");
         }

         @Override
         Iterator<Multiset.Entry<E>> entryIterator() {
            final Iterator<Multiset.Entry<E>> iterator1 = multiset1.entrySet().iterator();
            return new AbstractIterator<Multiset.Entry<E>>() {
               protected Multiset.@Nullable Entry<E> computeNext() {
                  while (iterator1.hasNext()) {
                     Multiset.Entry<E> entry1 = iterator1.next();
                     E element = entry1.getElement();
                     int count = Math.min(entry1.getCount(), multiset2.count(element));
                     if (count > 0) {
                        return Multisets.immutableEntry(element, count);
                     }
                  }

                  return this.endOfData();
               }
            };
         }
      };
   }

   public static <E> Multiset<E> sum(Multiset<? extends E> multiset1, Multiset<? extends E> multiset2) {
      Preconditions.checkNotNull(multiset1);
      Preconditions.checkNotNull(multiset2);
      return new Multisets.ViewMultiset<E>() {
         @Override
         public boolean contains(@Nullable Object element) {
            return multiset1.contains(element) || multiset2.contains(element);
         }

         @Override
         public boolean isEmpty() {
            return multiset1.isEmpty() && multiset2.isEmpty();
         }

         @Override
         public int size() {
            return IntMath.saturatedAdd(multiset1.size(), multiset2.size());
         }

         @Override
         public int count(@Nullable Object element) {
            return multiset1.count(element) + multiset2.count(element);
         }

         @Override
         Set<E> createElementSet() {
            return Sets.union(multiset1.elementSet(), multiset2.elementSet());
         }

         @Override
         Iterator<E> elementIterator() {
            throw new AssertionError("should never be called");
         }

         @Override
         Iterator<Multiset.Entry<E>> entryIterator() {
            final Iterator<? extends Multiset.Entry<? extends E>> iterator1 = multiset1.entrySet().iterator();
            final Iterator<? extends Multiset.Entry<? extends E>> iterator2 = multiset2.entrySet().iterator();
            return new AbstractIterator<Multiset.Entry<E>>() {
               protected Multiset.@Nullable Entry<E> computeNext() {
                  if (iterator1.hasNext()) {
                     Multiset.Entry<? extends E> entry1 = (Multiset.Entry<? extends E>)iterator1.next();
                     E element = (E)entry1.getElement();
                     int count = entry1.getCount() + multiset2.count(element);
                     return Multisets.immutableEntry(element, count);
                  }

                  while (iterator2.hasNext()) {
                     Multiset.Entry<? extends E> entry2 = (Multiset.Entry<? extends E>)iterator2.next();
                     E element = (E)entry2.getElement();
                     if (!multiset1.contains(element)) {
                        return Multisets.immutableEntry(element, entry2.getCount());
                     }
                  }

                  return this.endOfData();
               }
            };
         }
      };
   }

   public static <E> Multiset<E> difference(Multiset<E> multiset1, Multiset<?> multiset2) {
      Preconditions.checkNotNull(multiset1);
      Preconditions.checkNotNull(multiset2);
      return new Multisets.ViewMultiset<E>() {
         @Override
         public int count(@Nullable Object element) {
            int count1 = multiset1.count(element);
            return count1 == 0 ? 0 : Math.max(0, count1 - multiset2.count(element));
         }

         @Override
         public void clear() {
            throw new UnsupportedOperationException();
         }

         @Override
         Iterator<E> elementIterator() {
            final Iterator<Multiset.Entry<E>> iterator1 = multiset1.entrySet().iterator();
            return new AbstractIterator<E>() {
               @Override
               protected @Nullable E computeNext() {
                  while (iterator1.hasNext()) {
                     Multiset.Entry<E> entry1 = iterator1.next();
                     E element = entry1.getElement();
                     if (entry1.getCount() > multiset2.count(element)) {
                        return element;
                     }
                  }

                  return (E)this.endOfData();
               }
            };
         }

         @Override
         Iterator<Multiset.Entry<E>> entryIterator() {
            final Iterator<Multiset.Entry<E>> iterator1 = multiset1.entrySet().iterator();
            return new AbstractIterator<Multiset.Entry<E>>() {
               protected Multiset.@Nullable Entry<E> computeNext() {
                  while (iterator1.hasNext()) {
                     Multiset.Entry<E> entry1 = iterator1.next();
                     E element = entry1.getElement();
                     int count = entry1.getCount() - multiset2.count(element);
                     if (count > 0) {
                        return Multisets.immutableEntry(element, count);
                     }
                  }

                  return this.endOfData();
               }
            };
         }

         @Override
         int distinctElements() {
            return Iterators.size(this.entryIterator());
         }
      };
   }

   @CanIgnoreReturnValue
   public static boolean containsOccurrences(Multiset<?> superMultiset, Multiset<?> subMultiset) {
      Preconditions.checkNotNull(superMultiset);
      Preconditions.checkNotNull(subMultiset);

      for (Multiset.Entry<?> entry : subMultiset.entrySet()) {
         int superCount = superMultiset.count(entry.getElement());
         if (superCount < entry.getCount()) {
            return false;
         }
      }

      return true;
   }

   @CanIgnoreReturnValue
   public static boolean retainOccurrences(Multiset<?> multisetToModify, Multiset<?> multisetToRetain) {
      return retainOccurrencesImpl(multisetToModify, multisetToRetain);
   }

   private static <E> boolean retainOccurrencesImpl(Multiset<E> multisetToModify, Multiset<?> occurrencesToRetain) {
      Preconditions.checkNotNull(multisetToModify);
      Preconditions.checkNotNull(occurrencesToRetain);
      Iterator<Multiset.Entry<E>> entryIterator = multisetToModify.entrySet().iterator();
      boolean changed = false;

      while (entryIterator.hasNext()) {
         Multiset.Entry<E> entry = entryIterator.next();
         int retainCount = occurrencesToRetain.count(entry.getElement());
         if (retainCount == 0) {
            entryIterator.remove();
            changed = true;
         } else if (retainCount < entry.getCount()) {
            multisetToModify.setCount(entry.getElement(), retainCount);
            changed = true;
         }
      }

      return changed;
   }

   @CanIgnoreReturnValue
   public static boolean removeOccurrences(Multiset<?> multisetToModify, Iterable<?> occurrencesToRemove) {
      if (occurrencesToRemove instanceof Multiset) {
         return removeOccurrences(multisetToModify, (Multiset<?>)occurrencesToRemove);
      }

      Preconditions.checkNotNull(multisetToModify);
      Preconditions.checkNotNull(occurrencesToRemove);
      boolean changed = false;

      for (Object o : occurrencesToRemove) {
         changed |= multisetToModify.remove(o);
      }

      return changed;
   }

   @CanIgnoreReturnValue
   public static boolean removeOccurrences(Multiset<?> multisetToModify, Multiset<?> occurrencesToRemove) {
      Preconditions.checkNotNull(multisetToModify);
      Preconditions.checkNotNull(occurrencesToRemove);
      boolean changed = false;
      Iterator<? extends Multiset.Entry<?>> entryIterator = multisetToModify.entrySet().iterator();

      while (entryIterator.hasNext()) {
         Multiset.Entry<?> entry = (Multiset.Entry<?>)entryIterator.next();
         int removeCount = occurrencesToRemove.count(entry.getElement());
         if (removeCount >= entry.getCount()) {
            entryIterator.remove();
            changed = true;
         } else if (removeCount > 0) {
            multisetToModify.remove(entry.getElement(), removeCount);
            changed = true;
         }
      }

      return changed;
   }

   static boolean equalsImpl(Multiset<?> multiset, @Nullable Object object) {
      if (object == multiset) {
         return true;
      }

      if (object instanceof Multiset) {
         Multiset<?> that = (Multiset<?>)object;
         if (multiset.size() == that.size() && multiset.entrySet().size() == that.entrySet().size()) {
            for (Multiset.Entry<?> entry : that.entrySet()) {
               if (multiset.count(entry.getElement()) != entry.getCount()) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static <E> boolean addAllImpl(Multiset<E> self, Collection<? extends E> elements) {
      Preconditions.checkNotNull(self);
      Preconditions.checkNotNull(elements);
      if (elements instanceof Multiset) {
         return addAllImpl(self, (Multiset<? extends E>)elements);
      } else {
         return elements.isEmpty() ? false : Iterators.addAll(self, elements.iterator());
      }
   }

   private static <E> boolean addAllImpl(Multiset<E> self, Multiset<? extends E> elements) {
      if (elements.isEmpty()) {
         return false;
      }

      elements.forEachEntry(self::add);
      return true;
   }

   static boolean removeAllImpl(Multiset<?> self, Collection<?> elementsToRemove) {
      Collection<?> collection = elementsToRemove instanceof Multiset ? ((Multiset)elementsToRemove).elementSet() : elementsToRemove;
      return self.elementSet().removeAll(collection);
   }

   static boolean retainAllImpl(Multiset<?> self, Collection<?> elementsToRetain) {
      Preconditions.checkNotNull(elementsToRetain);
      Collection<?> collection = elementsToRetain instanceof Multiset ? ((Multiset)elementsToRetain).elementSet() : elementsToRetain;
      return self.elementSet().retainAll(collection);
   }

   static <E> int setCountImpl(Multiset<E> self, @ParametricNullness E element, int count) {
      CollectPreconditions.checkNonnegative(count, "count");
      int oldCount = self.count(element);
      int delta = count - oldCount;
      if (delta > 0) {
         self.add(element, delta);
      } else if (delta < 0) {
         self.remove(element, -delta);
      }

      return oldCount;
   }

   static <E> boolean setCountImpl(Multiset<E> self, @ParametricNullness E element, int oldCount, int newCount) {
      CollectPreconditions.checkNonnegative(oldCount, "oldCount");
      CollectPreconditions.checkNonnegative(newCount, "newCount");
      if (self.count(element) == oldCount) {
         self.setCount(element, newCount);
         return true;
      } else {
         return false;
      }
   }

   static <E> Iterator<E> elementIterator(Iterator<Multiset.Entry<E>> entryIterator) {
      return new TransformedIterator<Multiset.Entry<E>, E>(entryIterator) {
         @ParametricNullness
         E transform(Multiset.Entry<E> entry) {
            return entry.getElement();
         }
      };
   }

   static <E> Iterator<E> iteratorImpl(Multiset<E> multiset) {
      return new Multisets.MultisetIteratorImpl<>(multiset, multiset.entrySet().iterator());
   }

   static <E> Spliterator<E> spliteratorImpl(Multiset<E> multiset) {
      Spliterator<Multiset.Entry<E>> entrySpliterator = multiset.entrySet().spliterator();
      return CollectSpliterators.flatMap(
         entrySpliterator,
         entry -> Collections.nCopies(entry.getCount(), entry.getElement()).spliterator(),
         64 | entrySpliterator.characteristics() & 1296,
         multiset.size()
      );
   }

   static int linearTimeSizeImpl(Multiset<?> multiset) {
      long size = 0L;

      for (Multiset.Entry<?> entry : multiset.entrySet()) {
         size += entry.getCount();
      }

      return Ints.saturatedCast(size);
   }

   public static <E> ImmutableMultiset<E> copyHighestCountFirst(Multiset<E> multiset) {
      Multiset.Entry<E>[] entries = multiset.entrySet().toArray(new Multiset.Entry[0]);
      Arrays.sort(entries, Multisets.DecreasingCount.INSTANCE);
      return ImmutableMultiset.copyFromEntries(Arrays.asList(entries));
   }

   abstract static class AbstractEntry<E> implements Multiset.Entry<E> {
      @Override
      public boolean equals(@Nullable Object object) {
         if (!(object instanceof Multiset.Entry)) {
            return false;
         }

         Multiset.Entry<?> that = (Multiset.Entry<?>)object;
         return this.getCount() == that.getCount() && Objects.equal(this.getElement(), that.getElement());
      }

      @Override
      public int hashCode() {
         E e = this.getElement();
         return (e == null ? 0 : e.hashCode()) ^ this.getCount();
      }

      @Override
      public String toString() {
         String text = String.valueOf(this.getElement());
         int n = this.getCount();
         return n == 1 ? text : text + " x " + n;
      }
   }

   private static final class DecreasingCount implements Comparator<Multiset.Entry<?>> {
      static final Comparator<Multiset.Entry<?>> INSTANCE = new Multisets.DecreasingCount();

      public int compare(Multiset.Entry<?> entry1, Multiset.Entry<?> entry2) {
         return entry2.getCount() - entry1.getCount();
      }
   }

   abstract static class ElementSet<E> extends Sets.ImprovedAbstractSet<E> {
      abstract Multiset<E> multiset();

      @Override
      public void clear() {
         this.multiset().clear();
      }

      @Override
      public boolean contains(@Nullable Object o) {
         return this.multiset().contains(o);
      }

      @Override
      public boolean containsAll(Collection<?> c) {
         return this.multiset().containsAll(c);
      }

      @Override
      public boolean isEmpty() {
         return this.multiset().isEmpty();
      }

      @Override
      public abstract Iterator<E> iterator();

      @Override
      public boolean remove(@Nullable Object o) {
         return this.multiset().remove(o, Integer.MAX_VALUE) > 0;
      }

      @Override
      public int size() {
         return this.multiset().entrySet().size();
      }
   }

   abstract static class EntrySet<E> extends Sets.ImprovedAbstractSet<Multiset.Entry<E>> {
      abstract Multiset<E> multiset();

      @Override
      public boolean contains(@Nullable Object o) {
         if (o instanceof Multiset.Entry) {
            Multiset.Entry<?> entry = (Multiset.Entry<?>)o;
            if (entry.getCount() <= 0) {
               return false;
            }

            int count = this.multiset().count(entry.getElement());
            return count == entry.getCount();
         } else {
            return false;
         }
      }

      @Override
      public boolean remove(Object object) {
         if (object instanceof Multiset.Entry) {
            Multiset.Entry<?> entry = (Multiset.Entry<?>)object;
            Object element = entry.getElement();
            int entryCount = entry.getCount();
            if (entryCount != 0) {
               Multiset<Object> multiset = this.multiset();
               return multiset.setCount(element, entryCount, 0);
            }
         }

         return false;
      }

      @Override
      public void clear() {
         this.multiset().clear();
      }
   }

   private static final class FilteredMultiset<E> extends Multisets.ViewMultiset<E> {
      final Multiset<E> unfiltered;
      final Predicate<? super E> predicate;

      FilteredMultiset(Multiset<E> unfiltered, Predicate<? super E> predicate) {
         this.unfiltered = Preconditions.checkNotNull(unfiltered);
         this.predicate = Preconditions.checkNotNull(predicate);
      }

      public UnmodifiableIterator<E> iterator() {
         return Iterators.filter(this.unfiltered.iterator(), this.predicate);
      }

      @Override
      Set<E> createElementSet() {
         return Sets.filter(this.unfiltered.elementSet(), this.predicate);
      }

      @Override
      Iterator<E> elementIterator() {
         throw new AssertionError("should never be called");
      }

      @Override
      Set<Multiset.Entry<E>> createEntrySet() {
         return Sets.filter(this.unfiltered.entrySet(), entry -> this.predicate.apply(entry.getElement()));
      }

      @Override
      Iterator<Multiset.Entry<E>> entryIterator() {
         throw new AssertionError("should never be called");
      }

      @Override
      public int count(@Nullable Object element) {
         int count = this.unfiltered.count(element);
         if (count > 0) {
            E e = (E)element;
            return this.predicate.apply(e) ? count : 0;
         } else {
            return 0;
         }
      }

      @Override
      public int add(@ParametricNullness E element, int occurrences) {
         Preconditions.checkArgument(this.predicate.apply(element), "Element %s does not match predicate %s", element, this.predicate);
         return this.unfiltered.add(element, occurrences);
      }

      @Override
      public int remove(@Nullable Object element, int occurrences) {
         CollectPreconditions.checkNonnegative(occurrences, "occurrences");
         if (occurrences == 0) {
            return this.count(element);
         } else {
            return this.contains(element) ? this.unfiltered.remove(element, occurrences) : 0;
         }
      }
   }

   static class ImmutableEntry<E> extends Multisets.AbstractEntry<E> implements Serializable {
      @ParametricNullness
      private final E element;
      private final int count;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      ImmutableEntry(@ParametricNullness E element, int count) {
         this.element = element;
         this.count = count;
         CollectPreconditions.checkNonnegative(count, "count");
      }

      @ParametricNullness
      @Override
      public final E getElement() {
         return this.element;
      }

      @Override
      public final int getCount() {
         return this.count;
      }

      public Multisets.@Nullable ImmutableEntry<E> nextInBucket() {
         return null;
      }
   }

   static final class MultisetIteratorImpl<E> implements Iterator<E> {
      private final Multiset<E> multiset;
      private final Iterator<Multiset.Entry<E>> entryIterator;
      private Multiset.@Nullable Entry<E> currentEntry;
      private int laterCount;
      private int totalCount;
      private boolean canRemove;

      MultisetIteratorImpl(Multiset<E> multiset, Iterator<Multiset.Entry<E>> entryIterator) {
         this.multiset = multiset;
         this.entryIterator = entryIterator;
      }

      @Override
      public boolean hasNext() {
         return this.laterCount > 0 || this.entryIterator.hasNext();
      }

      @ParametricNullness
      @Override
      public E next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         }

         if (this.laterCount == 0) {
            this.currentEntry = this.entryIterator.next();
            this.totalCount = this.laterCount = this.currentEntry.getCount();
         }

         this.laterCount--;
         this.canRemove = true;
         return java.util.Objects.requireNonNull(this.currentEntry).getElement();
      }

      @Override
      public void remove() {
         CollectPreconditions.checkRemove(this.canRemove);
         if (this.totalCount == 1) {
            this.entryIterator.remove();
         } else {
            this.multiset.remove(java.util.Objects.requireNonNull(this.currentEntry).getElement());
         }

         this.totalCount--;
         this.canRemove = false;
      }
   }

   static class UnmodifiableMultiset<E> extends ForwardingMultiset<E> implements Serializable {
      final Multiset<? extends E> delegate;
      @LazyInit
      transient @Nullable Set<E> elementSet;
      @LazyInit
      transient @Nullable Set<Multiset.Entry<E>> entrySet;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      UnmodifiableMultiset(Multiset<? extends E> delegate) {
         this.delegate = delegate;
      }

      @Override
      protected Multiset<E> delegate() {
         return (Multiset<E>)this.delegate;
      }

      Set<E> createElementSet() {
         return Collections.unmodifiableSet(this.delegate.elementSet());
      }

      @Override
      public Set<E> elementSet() {
         Set<E> es = this.elementSet;
         return es == null ? (this.elementSet = this.createElementSet()) : es;
      }

      @Override
      public Set<Multiset.Entry<E>> entrySet() {
         Set<Multiset.Entry<E>> es = this.entrySet;
         return es == null ? (this.entrySet = Collections.unmodifiableSet(this.delegate.entrySet())) : es;
      }

      @Override
      public Iterator<E> iterator() {
         return Iterators.unmodifiableIterator(this.delegate.iterator());
      }

      @Override
      public boolean add(@ParametricNullness E element) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int add(@ParametricNullness E element, int occurrences) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(Collection<? extends E> elementsToAdd) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(@Nullable Object element) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int remove(@Nullable Object element, int occurrences) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(Collection<?> elementsToRemove) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeIf(java.util.function.Predicate<? super E> filter) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> elementsToRetain) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }

      @Override
      public int setCount(@ParametricNullness E element, int count) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean setCount(@ParametricNullness E element, int oldCount, int newCount) {
         throw new UnsupportedOperationException();
      }
   }

   private abstract static class ViewMultiset<E> extends AbstractMultiset<E> {
      private ViewMultiset() {
      }

      @Override
      public int size() {
         return Multisets.linearTimeSizeImpl(this);
      }

      @Override
      public void clear() {
         this.elementSet().clear();
      }

      @Override
      public Iterator<E> iterator() {
         return Multisets.iteratorImpl(this);
      }

      @Override
      int distinctElements() {
         return this.elementSet().size();
      }
   }
}
