package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Optional;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.dfsek.terra.lib.google.common.base.Predicates;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.InlineMe;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class Iterators {
   private Iterators() {
   }

   static <T> UnmodifiableIterator<T> emptyIterator() {
      return emptyListIterator();
   }

   static <T> UnmodifiableListIterator<T> emptyListIterator() {
      return (UnmodifiableListIterator<T>)Iterators.ArrayItr.EMPTY;
   }

   static <T> Iterator<T> emptyModifiableIterator() {
      return Iterators.EmptyModifiableIterator.INSTANCE;
   }

   public static <T> UnmodifiableIterator<T> unmodifiableIterator(Iterator<? extends T> iterator) {
      Preconditions.checkNotNull(iterator);
      return iterator instanceof UnmodifiableIterator ? (UnmodifiableIterator)iterator : new UnmodifiableIterator<T>() {
         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         @ParametricNullness
         @Override
         public T next() {
            return (T)iterator.next();
         }
      };
   }

   @Deprecated
   @InlineMe(replacement = "checkNotNull(iterator)", staticImports = "com.dfsek.terra.lib.google.common.base.Preconditions.checkNotNull")
   public static <T> UnmodifiableIterator<T> unmodifiableIterator(UnmodifiableIterator<T> iterator) {
      return Preconditions.checkNotNull(iterator);
   }

   public static int size(Iterator<?> iterator) {
      long count;
      for (count = 0L; iterator.hasNext(); count++) {
         iterator.next();
      }

      return Ints.saturatedCast(count);
   }

   public static boolean contains(Iterator<?> iterator, @Nullable Object element) {
      if (element == null) {
         while (iterator.hasNext()) {
            if (iterator.next() == null) {
               return true;
            }
         }
      } else {
         while (iterator.hasNext()) {
            if (element.equals(iterator.next())) {
               return true;
            }
         }
      }

      return false;
   }

   @CanIgnoreReturnValue
   public static boolean removeAll(Iterator<?> removeFrom, Collection<?> elementsToRemove) {
      Preconditions.checkNotNull(elementsToRemove);
      boolean result = false;

      while (removeFrom.hasNext()) {
         if (elementsToRemove.contains(removeFrom.next())) {
            removeFrom.remove();
            result = true;
         }
      }

      return result;
   }

   @CanIgnoreReturnValue
   public static <T> boolean removeIf(Iterator<T> removeFrom, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(predicate);
      boolean modified = false;

      while (removeFrom.hasNext()) {
         if (predicate.apply(removeFrom.next())) {
            removeFrom.remove();
            modified = true;
         }
      }

      return modified;
   }

   @CanIgnoreReturnValue
   public static boolean retainAll(Iterator<?> removeFrom, Collection<?> elementsToRetain) {
      Preconditions.checkNotNull(elementsToRetain);
      boolean result = false;

      while (removeFrom.hasNext()) {
         if (!elementsToRetain.contains(removeFrom.next())) {
            removeFrom.remove();
            result = true;
         }
      }

      return result;
   }

   public static boolean elementsEqual(Iterator<?> iterator1, Iterator<?> iterator2) {
      while (iterator1.hasNext()) {
         if (!iterator2.hasNext()) {
            return false;
         }

         Object o1 = iterator1.next();
         Object o2 = iterator2.next();
         if (!Objects.equal(o1, o2)) {
            return false;
         }
      }

      return !iterator2.hasNext();
   }

   public static String toString(Iterator<?> iterator) {
      StringBuilder sb = new StringBuilder().append('[');
      boolean first = true;

      while (iterator.hasNext()) {
         if (!first) {
            sb.append(", ");
         }

         first = false;
         sb.append(iterator.next());
      }

      return sb.append(']').toString();
   }

   @ParametricNullness
   public static <T> T getOnlyElement(Iterator<T> iterator) {
      T first = iterator.next();
      if (!iterator.hasNext()) {
         return first;
      }

      StringBuilder sb = new StringBuilder().append("expected one element but was: <").append(first);

      for (int i = 0; i < 4 && iterator.hasNext(); i++) {
         sb.append(", ").append(iterator.next());
      }

      if (iterator.hasNext()) {
         sb.append(", ...");
      }

      sb.append('>');
      throw new IllegalArgumentException(sb.toString());
   }

   @ParametricNullness
   public static <T> T getOnlyElement(Iterator<? extends T> iterator, @ParametricNullness T defaultValue) {
      return iterator.hasNext() ? getOnlyElement((Iterator<T>)iterator) : defaultValue;
   }

   @GwtIncompatible
   public static <T> T[] toArray(Iterator<? extends T> iterator, Class<@NonNull T> type) {
      List<T> list = Lists.newArrayList(iterator);
      return Iterables.toArray(list, type);
   }

   @CanIgnoreReturnValue
   public static <T> boolean addAll(Collection<T> addTo, Iterator<? extends T> iterator) {
      Preconditions.checkNotNull(addTo);
      Preconditions.checkNotNull(iterator);
      boolean wasModified = false;

      while (iterator.hasNext()) {
         wasModified |= addTo.add((T)iterator.next());
      }

      return wasModified;
   }

   public static int frequency(Iterator<?> iterator, @Nullable Object element) {
      int count = 0;

      while (contains(iterator, element)) {
         count++;
      }

      return count;
   }

   public static <T> Iterator<T> cycle(Iterable<T> iterable) {
      Preconditions.checkNotNull(iterable);
      return new Iterator<T>() {
         Iterator<T> iterator = Iterators.emptyModifiableIterator();

         @Override
         public boolean hasNext() {
            return this.iterator.hasNext() || iterable.iterator().hasNext();
         }

         @ParametricNullness
         @Override
         public T next() {
            if (!this.iterator.hasNext()) {
               this.iterator = iterable.iterator();
               if (!this.iterator.hasNext()) {
                  throw new NoSuchElementException();
               }
            }

            return this.iterator.next();
         }

         @Override
         public void remove() {
            this.iterator.remove();
         }
      };
   }

   @SafeVarargs
   public static <T> Iterator<T> cycle(T... elements) {
      return cycle(Lists.newArrayList(elements));
   }

   private static <I extends Iterator<?>> Iterator<I> consumingForArray(@Nullable I... elements) {
      return new UnmodifiableIterator<I>() {
         int index = 0;

         @Override
         public boolean hasNext() {
            return this.index < elements.length;
         }

         public I next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            }

            I result = java.util.Objects.requireNonNull(elements[this.index]);
            elements[this.index] = null;
            this.index++;
            return result;
         }
      };
   }

   public static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b) {
      Preconditions.checkNotNull(a);
      Preconditions.checkNotNull(b);
      return concat(consumingForArray(a, b));
   }

   public static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b, Iterator<? extends T> c) {
      Preconditions.checkNotNull(a);
      Preconditions.checkNotNull(b);
      Preconditions.checkNotNull(c);
      return concat(consumingForArray(a, b, c));
   }

   public static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b, Iterator<? extends T> c, Iterator<? extends T> d) {
      Preconditions.checkNotNull(a);
      Preconditions.checkNotNull(b);
      Preconditions.checkNotNull(c);
      Preconditions.checkNotNull(d);
      return concat(consumingForArray(a, b, c, d));
   }

   @SafeVarargs
   public static <T> Iterator<T> concat(Iterator<? extends T>... inputs) {
      return concatNoDefensiveCopy(Arrays.copyOf(inputs, inputs.length));
   }

   public static <T> Iterator<T> concat(Iterator<? extends Iterator<? extends T>> inputs) {
      return new Iterators.ConcatenatedIterator<>(inputs);
   }

   static <T> Iterator<T> concatNoDefensiveCopy(Iterator<? extends T>... inputs) {
      for (Iterator<? extends T> input : Preconditions.checkNotNull(inputs)) {
         Preconditions.checkNotNull(input);
      }

      return concat(consumingForArray(inputs));
   }

   public static <T> UnmodifiableIterator<List<T>> partition(Iterator<T> iterator, int size) {
      return partitionImpl(iterator, size, false);
   }

   public static <T> UnmodifiableIterator<List<@Nullable T>> paddedPartition(Iterator<T> iterator, int size) {
      return partitionImpl(iterator, size, true);
   }

   private static <T> UnmodifiableIterator<List<@Nullable T>> partitionImpl(Iterator<T> iterator, int size, boolean pad) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkArgument(size > 0);
      return new UnmodifiableIterator<List<T>>() {
         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         public List<T> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            }

            T[] array = (T[])(new Object[size]);

            int count;
            for (count = 0; count < size && iterator.hasNext(); count++) {
               array[count] = iterator.next();
            }

            for (int i = count; i < size; i++) {
               array[i] = null;
            }

            List<T> list = Collections.unmodifiableList(Arrays.asList(array));
            return !pad && count != size ? list.subList(0, count) : list;
         }
      };
   }

   public static <T> UnmodifiableIterator<T> filter(Iterator<T> unfiltered, Predicate<? super T> retainIfTrue) {
      Preconditions.checkNotNull(unfiltered);
      Preconditions.checkNotNull(retainIfTrue);
      return new AbstractIterator<T>() {
         @Override
         protected @Nullable T computeNext() {
            while (unfiltered.hasNext()) {
               T element = unfiltered.next();
               if (retainIfTrue.apply(element)) {
                  return element;
               }
            }

            return (T)this.endOfData();
         }
      };
   }

   @GwtIncompatible
   public static <T> UnmodifiableIterator<T> filter(Iterator<?> unfiltered, Class<T> desiredType) {
      return filter((Iterator<T>)unfiltered, Predicates.instanceOf(desiredType));
   }

   public static <T> boolean any(Iterator<T> iterator, Predicate<? super T> predicate) {
      return indexOf(iterator, predicate) != -1;
   }

   public static <T> boolean all(Iterator<T> iterator, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(predicate);

      while (iterator.hasNext()) {
         T element = iterator.next();
         if (!predicate.apply(element)) {
            return false;
         }
      }

      return true;
   }

   @ParametricNullness
   public static <T> T find(Iterator<T> iterator, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkNotNull(predicate);

      while (iterator.hasNext()) {
         T t = iterator.next();
         if (predicate.apply(t)) {
            return t;
         }
      }

      throw new NoSuchElementException();
   }

   public static <T> @Nullable T find(Iterator<? extends T> iterator, Predicate<? super T> predicate, @Nullable T defaultValue) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkNotNull(predicate);

      while (iterator.hasNext()) {
         T t = (T)iterator.next();
         if (predicate.apply(t)) {
            return t;
         }
      }

      return defaultValue;
   }

   public static <T> Optional<T> tryFind(Iterator<T> iterator, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkNotNull(predicate);

      while (iterator.hasNext()) {
         T t = iterator.next();
         if (predicate.apply(t)) {
            return Optional.of(t);
         }
      }

      return Optional.absent();
   }

   public static <T> int indexOf(Iterator<T> iterator, Predicate<? super T> predicate) {
      Preconditions.checkNotNull(predicate, "predicate");

      for (int i = 0; iterator.hasNext(); i++) {
         T current = iterator.next();
         if (predicate.apply(current)) {
            return i;
         }
      }

      return -1;
   }

   public static <F, T> Iterator<T> transform(Iterator<F> fromIterator, Function<? super F, ? extends T> function) {
      Preconditions.checkNotNull(function);
      return new TransformedIterator<F, T>(fromIterator) {
         @ParametricNullness
         @Override
         T transform(@ParametricNullness F from) {
            return (T)function.apply(from);
         }
      };
   }

   @ParametricNullness
   public static <T> T get(Iterator<T> iterator, int position) {
      checkNonnegative(position);
      int skipped = advance(iterator, position);
      if (!iterator.hasNext()) {
         throw new IndexOutOfBoundsException("position (" + position + ") must be less than the number of elements that remained (" + skipped + ")");
      } else {
         return iterator.next();
      }
   }

   @ParametricNullness
   public static <T> T get(Iterator<? extends T> iterator, int position, @ParametricNullness T defaultValue) {
      checkNonnegative(position);
      advance(iterator, position);
      return getNext(iterator, defaultValue);
   }

   static void checkNonnegative(int position) {
      if (position < 0) {
         throw new IndexOutOfBoundsException("position (" + position + ") must not be negative");
      }
   }

   @ParametricNullness
   public static <T> T getNext(Iterator<? extends T> iterator, @ParametricNullness T defaultValue) {
      return (T)(iterator.hasNext() ? iterator.next() : defaultValue);
   }

   @ParametricNullness
   public static <T> T getLast(Iterator<T> iterator) {
      T current;
      do {
         current = iterator.next();
      } while (iterator.hasNext());

      return current;
   }

   @ParametricNullness
   public static <T> T getLast(Iterator<? extends T> iterator, @ParametricNullness T defaultValue) {
      return iterator.hasNext() ? getLast((Iterator<T>)iterator) : defaultValue;
   }

   @CanIgnoreReturnValue
   public static int advance(Iterator<?> iterator, int numberToAdvance) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkArgument(numberToAdvance >= 0, "numberToAdvance must be nonnegative");

      int i;
      for (i = 0; i < numberToAdvance && iterator.hasNext(); i++) {
         iterator.next();
      }

      return i;
   }

   public static <T> Iterator<T> limit(Iterator<T> iterator, int limitSize) {
      Preconditions.checkNotNull(iterator);
      Preconditions.checkArgument(limitSize >= 0, "limit is negative");
      return new Iterator<T>() {
         private int count;

         @Override
         public boolean hasNext() {
            return this.count < limitSize && iterator.hasNext();
         }

         @ParametricNullness
         @Override
         public T next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            }

            this.count++;
            return iterator.next();
         }

         @Override
         public void remove() {
            iterator.remove();
         }
      };
   }

   public static <T> Iterator<T> consumingIterator(Iterator<T> iterator) {
      Preconditions.checkNotNull(iterator);
      return new UnmodifiableIterator<T>() {
         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         @ParametricNullness
         @Override
         public T next() {
            T next = iterator.next();
            iterator.remove();
            return next;
         }

         @Override
         public String toString() {
            return "Iterators.consumingIterator(...)";
         }
      };
   }

   static <T> @Nullable T pollNext(Iterator<T> iterator) {
      if (iterator.hasNext()) {
         T result = iterator.next();
         iterator.remove();
         return result;
      } else {
         return null;
      }
   }

   static void clear(Iterator<?> iterator) {
      Preconditions.checkNotNull(iterator);

      while (iterator.hasNext()) {
         iterator.next();
         iterator.remove();
      }
   }

   @SafeVarargs
   public static <T> UnmodifiableIterator<T> forArray(T... array) {
      return forArrayWithPosition(array, 0);
   }

   static <T> UnmodifiableListIterator<T> forArrayWithPosition(T[] array, int position) {
      if (array.length == 0) {
         Preconditions.checkPositionIndex(position, array.length);
         return emptyListIterator();
      } else {
         return new Iterators.ArrayItr<>(array, position);
      }
   }

   public static <T> UnmodifiableIterator<T> singletonIterator(@ParametricNullness T value) {
      return new Iterators.SingletonIterator<>(value);
   }

   public static <T> UnmodifiableIterator<T> forEnumeration(Enumeration<T> enumeration) {
      Preconditions.checkNotNull(enumeration);
      return new UnmodifiableIterator<T>() {
         @Override
         public boolean hasNext() {
            return enumeration.hasMoreElements();
         }

         @ParametricNullness
         @Override
         public T next() {
            return enumeration.nextElement();
         }
      };
   }

   public static <T> Enumeration<T> asEnumeration(Iterator<T> iterator) {
      Preconditions.checkNotNull(iterator);
      return new Enumeration<T>() {
         @Override
         public boolean hasMoreElements() {
            return iterator.hasNext();
         }

         @ParametricNullness
         @Override
         public T nextElement() {
            return iterator.next();
         }
      };
   }

   public static <T> PeekingIterator<T> peekingIterator(Iterator<? extends T> iterator) {
      return iterator instanceof Iterators.PeekingImpl ? (Iterators.PeekingImpl)iterator : new Iterators.PeekingImpl<>(iterator);
   }

   @Deprecated
   @InlineMe(replacement = "checkNotNull(iterator)", staticImports = "com.dfsek.terra.lib.google.common.base.Preconditions.checkNotNull")
   public static <T> PeekingIterator<T> peekingIterator(PeekingIterator<T> iterator) {
      return Preconditions.checkNotNull(iterator);
   }

   public static <T> UnmodifiableIterator<T> mergeSorted(Iterable<? extends Iterator<? extends T>> iterators, Comparator<? super T> comparator) {
      Preconditions.checkNotNull(iterators, "iterators");
      Preconditions.checkNotNull(comparator, "comparator");
      return new Iterators.MergingIterator<>(iterators, comparator);
   }

   private static final class ArrayItr<T> extends AbstractIndexedListIterator<T> {
      static final UnmodifiableListIterator<Object> EMPTY = new Iterators.ArrayItr<>(new Object[0], 0);
      private final T[] array;

      ArrayItr(T[] array, int position) {
         super(array.length, position);
         this.array = array;
      }

      @ParametricNullness
      @Override
      protected T get(int index) {
         return this.array[index];
      }
   }

   private static class ConcatenatedIterator<T> implements Iterator<T> {
      private @Nullable Iterator<? extends T> toRemove;
      private Iterator<? extends T> iterator = Iterators.emptyIterator();
      private @Nullable Iterator<? extends Iterator<? extends T>> topMetaIterator;
      private @Nullable Deque<Iterator<? extends Iterator<? extends T>>> metaIterators;

      ConcatenatedIterator(Iterator<? extends Iterator<? extends T>> metaIterator) {
         this.topMetaIterator = Preconditions.checkNotNull(metaIterator);
      }

      private @Nullable Iterator<? extends Iterator<? extends T>> getTopMetaIterator() {
         while (this.topMetaIterator == null || !this.topMetaIterator.hasNext()) {
            if (this.metaIterators == null || this.metaIterators.isEmpty()) {
               return null;
            }

            this.topMetaIterator = this.metaIterators.removeFirst();
         }

         return this.topMetaIterator;
      }

      @Override
      public boolean hasNext() {
         while (!Preconditions.checkNotNull(this.iterator).hasNext()) {
            this.topMetaIterator = this.getTopMetaIterator();
            if (this.topMetaIterator == null) {
               return false;
            }

            this.iterator = (Iterator<? extends T>)this.topMetaIterator.next();
            if (this.iterator instanceof Iterators.ConcatenatedIterator) {
               Iterators.ConcatenatedIterator<T> topConcat = (Iterators.ConcatenatedIterator<T>)this.iterator;
               this.iterator = topConcat.iterator;
               if (this.metaIterators == null) {
                  this.metaIterators = new ArrayDeque<>();
               }

               this.metaIterators.addFirst(this.topMetaIterator);
               if (topConcat.metaIterators != null) {
                  while (!topConcat.metaIterators.isEmpty()) {
                     this.metaIterators.addFirst(topConcat.metaIterators.removeLast());
                  }
               }

               this.topMetaIterator = topConcat.topMetaIterator;
            }
         }

         return true;
      }

      @ParametricNullness
      @Override
      public T next() {
         if (this.hasNext()) {
            this.toRemove = this.iterator;
            return (T)this.iterator.next();
         } else {
            throw new NoSuchElementException();
         }
      }

      @Override
      public void remove() {
         if (this.toRemove == null) {
            throw new IllegalStateException("no calls to next() since the last call to remove()");
         }

         this.toRemove.remove();
         this.toRemove = null;
      }
   }

   private enum EmptyModifiableIterator implements Iterator<Object> {
      INSTANCE;

      @Override
      public boolean hasNext() {
         return false;
      }

      @Override
      public Object next() {
         throw new NoSuchElementException();
      }

      @Override
      public void remove() {
         CollectPreconditions.checkRemove(false);
      }
   }

   private static class MergingIterator<T> extends UnmodifiableIterator<T> {
      final Queue<PeekingIterator<T>> queue;

      public MergingIterator(Iterable<? extends Iterator<? extends T>> iterators, Comparator<? super T> itemComparator) {
         Comparator<PeekingIterator<T>> heapComparator = (o1, o2) -> itemComparator.compare(o1.peek(), o2.peek());
         this.queue = new PriorityQueue<>(2, heapComparator);

         for (Iterator<? extends T> iterator : iterators) {
            if (iterator.hasNext()) {
               this.queue.add(Iterators.peekingIterator(iterator));
            }
         }
      }

      @Override
      public boolean hasNext() {
         return !this.queue.isEmpty();
      }

      @ParametricNullness
      @Override
      public T next() {
         PeekingIterator<T> nextIter = this.queue.remove();
         T next = nextIter.next();
         if (nextIter.hasNext()) {
            this.queue.add(nextIter);
         }

         return next;
      }
   }

   private static class PeekingImpl<E> implements PeekingIterator<E> {
      private final Iterator<? extends E> iterator;
      private boolean hasPeeked;
      private @Nullable E peekedElement;

      public PeekingImpl(Iterator<? extends E> iterator) {
         this.iterator = Preconditions.checkNotNull(iterator);
      }

      @Override
      public boolean hasNext() {
         return this.hasPeeked || this.iterator.hasNext();
      }

      @ParametricNullness
      @Override
      public E next() {
         if (!this.hasPeeked) {
            return (E)this.iterator.next();
         }

         E result = NullnessCasts.uncheckedCastNullableTToT(this.peekedElement);
         this.hasPeeked = false;
         this.peekedElement = null;
         return result;
      }

      @Override
      public void remove() {
         Preconditions.checkState(!this.hasPeeked, "Can't remove after you've peeked at next");
         this.iterator.remove();
      }

      @ParametricNullness
      @Override
      public E peek() {
         if (!this.hasPeeked) {
            this.peekedElement = (E)this.iterator.next();
            this.hasPeeked = true;
         }

         return NullnessCasts.uncheckedCastNullableTToT(this.peekedElement);
      }
   }

   private static final class SingletonIterator<T> extends UnmodifiableIterator<T> {
      private final T value;
      private boolean done;

      SingletonIterator(T value) {
         this.value = value;
      }

      @Override
      public boolean hasNext() {
         return !this.done;
      }

      @ParametricNullness
      @Override
      public T next() {
         if (this.done) {
            throw new NoSuchElementException();
         }

         this.done = true;
         return this.value;
      }
   }
}
