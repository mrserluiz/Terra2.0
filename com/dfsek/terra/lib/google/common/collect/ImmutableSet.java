package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.math.IntMath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
public abstract class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {
   static final int SPLITERATOR_CHARACTERISTICS = 1297;
   static final int MAX_TABLE_SIZE = 1073741824;
   private static final double DESIRED_LOAD_FACTOR = 0.7;
   private static final int CUTOFF = 751619276;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = -889275714L;

   public static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
      return CollectCollectors.toImmutableSet();
   }

   public static <E> ImmutableSet<E> of() {
      return (ImmutableSet<E>)RegularImmutableSet.EMPTY;
   }

   public static <E> ImmutableSet<E> of(E e1) {
      return new SingletonImmutableSet<>(e1);
   }

   public static <E> ImmutableSet<E> of(E e1, E e2) {
      return new ImmutableSet.RegularSetBuilderImpl<E>(2).add(e1).add(e2).review().build();
   }

   public static <E> ImmutableSet<E> of(E e1, E e2, E e3) {
      return new ImmutableSet.RegularSetBuilderImpl<E>(3).add(e1).add(e2).add(e3).review().build();
   }

   public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4) {
      return new ImmutableSet.RegularSetBuilderImpl<E>(4).add(e1).add(e2).add(e3).add(e4).review().build();
   }

   public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5) {
      return new ImmutableSet.RegularSetBuilderImpl<E>(5).add(e1).add(e2).add(e3).add(e4).add(e5).review().build();
   }

   @SafeVarargs
   public static <E> ImmutableSet<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E... others) {
      Preconditions.checkArgument(others.length <= 2147483641, "the total number of elements must fit in an int");
      ImmutableSet.SetBuilderImpl<E> builder = new ImmutableSet.RegularSetBuilderImpl<>(6 + others.length);
      builder = builder.add(e1).add(e2).add(e3).add(e4).add(e5).add(e6);

      for (int i = 0; i < others.length; i++) {
         builder = builder.add(others[i]);
      }

      return builder.review().build();
   }

   public static <E> ImmutableSet<E> copyOf(Collection<? extends E> elements) {
      if (elements instanceof ImmutableSet && !(elements instanceof SortedSet)) {
         ImmutableSet<E> set = (ImmutableSet<E>)elements;
         if (!set.isPartialView()) {
            return set;
         }
      } else if (elements instanceof EnumSet) {
         return copyOfEnumSet((EnumSet<?>)elements);
      }

      if (elements.isEmpty()) {
         return of();
      }

      E[] array = (E[])elements.toArray();
      int expectedSize = elements instanceof Set ? array.length : estimatedSizeForUnknownDuplication(array.length);
      return fromArrayWithExpectedSize(array, expectedSize);
   }

   public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
      return elements instanceof Collection ? copyOf((Collection<? extends E>)elements) : copyOf(elements.iterator());
   }

   public static <E> ImmutableSet<E> copyOf(Iterator<? extends E> elements) {
      if (!elements.hasNext()) {
         return of();
      }

      E first = (E)elements.next();
      return !elements.hasNext() ? of(first) : new ImmutableSet.Builder<E>().add(first).addAll(elements).build();
   }

   public static <E> ImmutableSet<E> copyOf(E[] elements) {
      return fromArrayWithExpectedSize(elements, estimatedSizeForUnknownDuplication(elements.length));
   }

   private static <E> ImmutableSet<E> fromArrayWithExpectedSize(E[] elements, int expectedSize) {
      switch (elements.length) {
         case 0:
            return of();
         case 1:
            return of(elements[0]);
         default:
            ImmutableSet.SetBuilderImpl<E> builder = new ImmutableSet.RegularSetBuilderImpl<>(expectedSize);

            for (int i = 0; i < elements.length; i++) {
               builder = builder.add(elements[i]);
            }

            return builder.review().build();
      }
   }

   private static ImmutableSet copyOfEnumSet(EnumSet<?> enumSet) {
      return ImmutableEnumSet.asImmutable(EnumSet.copyOf(enumSet));
   }

   ImmutableSet() {
   }

   boolean isHashCodeFast() {
      return false;
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      } else {
         return object instanceof ImmutableSet && this.isHashCodeFast() && ((ImmutableSet)object).isHashCodeFast() && this.hashCode() != object.hashCode()
            ? false
            : Sets.equalsImpl(this, object);
      }
   }

   @Override
   public int hashCode() {
      return Sets.hashCodeImpl(this);
   }

   @Override
   public abstract UnmodifiableIterator<E> iterator();

   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return new ImmutableSet.SerializedForm(this.toArray());
   }

   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   public static <E> ImmutableSet.Builder<E> builder() {
      return new ImmutableSet.Builder<>();
   }

   public static <E> ImmutableSet.Builder<E> builderWithExpectedSize(int expectedSize) {
      CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
      return new ImmutableSet.Builder<>(expectedSize);
   }

   static int chooseTableSize(int setSize) {
      setSize = Math.max(setSize, 2);
      if (setSize >= 751619276) {
         Preconditions.checkArgument(setSize < 1073741824, "collection too large");
         return 1073741824;
      }

      int tableSize = Integer.highestOneBit(setSize - 1) << 1;

      while (tableSize * 0.7 < setSize) {
         tableSize <<= 1;
      }

      return tableSize;
   }

   private static int estimatedSizeForUnknownDuplication(int inputElementsIncludingAnyDuplicates) {
      return inputElementsIncludingAnyDuplicates < 4
         ? inputElementsIncludingAnyDuplicates
         : Math.max(4, IntMath.sqrt(inputElementsIncludingAnyDuplicates, RoundingMode.CEILING));
   }

   public static class Builder<E> extends ImmutableCollection.Builder<E> {
      private ImmutableSet.@Nullable SetBuilderImpl<E> impl;
      boolean forceCopy;

      public Builder() {
         this(0);
      }

      Builder(int capacity) {
         if (capacity > 0) {
            this.impl = new ImmutableSet.RegularSetBuilderImpl<>(capacity);
         } else {
            this.impl = ImmutableSet.EmptySetBuilderImpl.instance();
         }
      }

      Builder(boolean subclass) {
         this.impl = null;
      }

      @VisibleForTesting
      void forceJdk() {
         this.impl = new ImmutableSet.JdkBackedSetBuilderImpl<>(this.impl);
      }

      final void copyIfNecessary() {
         if (this.forceCopy) {
            this.copy();
            this.forceCopy = false;
         }
      }

      void copy() {
         Objects.requireNonNull(this.impl);
         this.impl = this.impl.copy();
      }

      @CanIgnoreReturnValue
      public ImmutableSet.Builder<E> add(E element) {
         Objects.requireNonNull(this.impl);
         Preconditions.checkNotNull(element);
         this.copyIfNecessary();
         this.impl = this.impl.add(element);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSet.Builder<E> add(E... elements) {
         super.add(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSet.Builder<E> addAll(Iterable<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableSet.Builder<E> addAll(Iterator<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableSet.Builder<E> combine(ImmutableSet.Builder<E> other) {
         Objects.requireNonNull(this.impl);
         Objects.requireNonNull(other.impl);
         this.copyIfNecessary();
         this.impl = this.impl.combine(other.impl);
         return this;
      }

      public ImmutableSet<E> build() {
         Objects.requireNonNull(this.impl);
         this.forceCopy = true;
         this.impl = this.impl.review();
         return this.impl.build();
      }
   }

   @GwtCompatible
   abstract static class CachingAsList<E> extends ImmutableSet<E> {
      @LazyInit
      @RetainedWith
      private transient @Nullable ImmutableList<E> asList;

      @Override
      public ImmutableList<E> asList() {
         ImmutableList<E> result = this.asList;
         return result == null ? (this.asList = this.createAsList()) : result;
      }

      ImmutableList<E> createAsList() {
         return new RegularImmutableAsList<>(this, this.toArray());
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }

   private static final class EmptySetBuilderImpl<E> extends ImmutableSet.SetBuilderImpl<E> {
      private static final ImmutableSet.EmptySetBuilderImpl<Object> INSTANCE = new ImmutableSet.EmptySetBuilderImpl<>();

      static <E> ImmutableSet.SetBuilderImpl<E> instance() {
         return (ImmutableSet.SetBuilderImpl<E>)INSTANCE;
      }

      private EmptySetBuilderImpl() {
         super(0);
      }

      @Override
      ImmutableSet.SetBuilderImpl<E> add(E e) {
         return new ImmutableSet.RegularSetBuilderImpl<E>(4).add(e);
      }

      @Override
      ImmutableSet.SetBuilderImpl<E> copy() {
         return this;
      }

      @Override
      ImmutableSet<E> build() {
         return ImmutableSet.of();
      }
   }

   abstract static class Indexed<E> extends ImmutableSet.CachingAsList<E> {
      abstract E get(int index);

      @Override
      public UnmodifiableIterator<E> iterator() {
         return this.asList().iterator();
      }

      @Override
      public Spliterator<E> spliterator() {
         return CollectSpliterators.indexed(this.size(), 1297, this::get);
      }

      @Override
      public void forEach(Consumer<? super E> consumer) {
         Preconditions.checkNotNull(consumer);
         int n = this.size();

         for (int i = 0; i < n; i++) {
            consumer.accept(this.get(i));
         }
      }

      @Override
      int copyIntoArray(@Nullable Object[] dst, int offset) {
         return this.asList().copyIntoArray(dst, offset);
      }

      @Override
      ImmutableList<E> createAsList() {
         return new ImmutableAsList<E>() {
            @Override
            public E get(int index) {
               return (E)Indexed.this.get(index);
            }

            ImmutableSet.Indexed<E> delegateCollection() {
               return Indexed.this;
            }

            @J2ktIncompatible
            @GwtIncompatible
            @Override
            Object writeReplace() {
               return super.writeReplace();
            }
         };
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }

   private static final class JdkBackedSetBuilderImpl<E> extends ImmutableSet.SetBuilderImpl<E> {
      private final Set<Object> delegate = Sets.newHashSetWithExpectedSize(this.distinct);

      JdkBackedSetBuilderImpl(ImmutableSet.SetBuilderImpl<E> toCopy) {
         super(toCopy);

         for (int i = 0; i < this.distinct; i++) {
            this.delegate.add(Objects.requireNonNull(this.dedupedElements[i]));
         }
      }

      @Override
      ImmutableSet.SetBuilderImpl<E> add(E e) {
         Preconditions.checkNotNull(e);
         if (this.delegate.add(e)) {
            this.addDedupedElement(e);
         }

         return this;
      }

      @Override
      ImmutableSet.SetBuilderImpl<E> copy() {
         return new ImmutableSet.JdkBackedSetBuilderImpl<>(this);
      }

      @Override
      ImmutableSet<E> build() {
         switch (this.distinct) {
            case 0:
               return ImmutableSet.of();
            case 1:
               return ImmutableSet.of(Objects.requireNonNull(this.dedupedElements[0]));
            default:
               return new JdkBackedImmutableSet<>(this.delegate, ImmutableList.asImmutableList(this.dedupedElements, this.distinct));
         }
      }
   }

   private static final class RegularSetBuilderImpl<E> extends ImmutableSet.SetBuilderImpl<E> {
      private @Nullable Object @Nullable [] hashTable;
      private int maxRunBeforeFallback;
      private int expandTableThreshold;
      private int hashCode;
      static final int MAX_RUN_MULTIPLIER = 13;

      RegularSetBuilderImpl(int expectedCapacity) {
         super(expectedCapacity);
         this.hashTable = null;
         this.maxRunBeforeFallback = 0;
         this.expandTableThreshold = 0;
      }

      RegularSetBuilderImpl(ImmutableSet.RegularSetBuilderImpl<E> toCopy) {
         super(toCopy);
         this.hashTable = toCopy.hashTable == null ? null : (Object[])toCopy.hashTable.clone();
         this.maxRunBeforeFallback = toCopy.maxRunBeforeFallback;
         this.expandTableThreshold = toCopy.expandTableThreshold;
         this.hashCode = toCopy.hashCode;
      }

      @Override
      ImmutableSet.SetBuilderImpl<E> add(E e) {
         Preconditions.checkNotNull(e);
         if (this.hashTable == null) {
            if (this.distinct == 0) {
               this.addDedupedElement(e);
               return this;
            } else {
               this.ensureTableCapacity(this.dedupedElements.length);
               E elem = this.dedupedElements[0];
               this.distinct--;
               return this.insertInHashTable(elem).add(e);
            }
         } else {
            return this.insertInHashTable(e);
         }
      }

      private ImmutableSet.SetBuilderImpl<E> insertInHashTable(E e) {
         Objects.requireNonNull(this.hashTable);
         int eHash = e.hashCode();
         int i0 = Hashing.smear(eHash);
         int mask = this.hashTable.length - 1;

         for (int i = i0; i - i0 < this.maxRunBeforeFallback; i++) {
            int index = i & mask;
            Object tableEntry = this.hashTable[index];
            if (tableEntry == null) {
               this.addDedupedElement(e);
               this.hashTable[index] = e;
               this.hashCode += eHash;
               this.ensureTableCapacity(this.distinct);
               return this;
            }

            if (tableEntry.equals(e)) {
               return this;
            }
         }

         return new ImmutableSet.JdkBackedSetBuilderImpl<>(this).add(e);
      }

      @Override
      ImmutableSet.SetBuilderImpl<E> copy() {
         return new ImmutableSet.RegularSetBuilderImpl<>(this);
      }

      @Override
      ImmutableSet.SetBuilderImpl<E> review() {
         if (this.hashTable == null) {
            return this;
         }

         int targetTableSize = ImmutableSet.chooseTableSize(this.distinct);
         if (targetTableSize * 2 < this.hashTable.length) {
            this.hashTable = rebuildHashTable(targetTableSize, this.dedupedElements, this.distinct);
            this.maxRunBeforeFallback = maxRunBeforeFallback(targetTableSize);
            this.expandTableThreshold = (int)(0.7 * targetTableSize);
         }

         return hashFloodingDetected(this.hashTable) ? new ImmutableSet.JdkBackedSetBuilderImpl<>(this) : this;
      }

      @Override
      ImmutableSet<E> build() {
         switch (this.distinct) {
            case 0:
               return ImmutableSet.of();
            case 1:
               return ImmutableSet.of(Objects.requireNonNull(this.dedupedElements[0]));
            default:
               Object[] elements = this.distinct == this.dedupedElements.length ? this.dedupedElements : Arrays.copyOf(this.dedupedElements, this.distinct);
               return new RegularImmutableSet<>(elements, this.hashCode, Objects.requireNonNull(this.hashTable), this.hashTable.length - 1);
         }
      }

      static Object[] rebuildHashTable(int newTableSize, Object[] elements, int n) {
         Object[] hashTable = new Object[newTableSize];
         int mask = hashTable.length - 1;

         for (int i = 0; i < n; i++) {
            Object e = Objects.requireNonNull(elements[i]);
            int j0 = Hashing.smear(e.hashCode());
            int j = j0;

            while (true) {
               int index = j & mask;
               if (hashTable[index] == null) {
                  hashTable[index] = e;
                  break;
               }

               j++;
            }
         }

         return hashTable;
      }

      void ensureTableCapacity(int minCapacity) {
         int newTableSize;
         if (this.hashTable == null) {
            newTableSize = ImmutableSet.chooseTableSize(minCapacity);
            this.hashTable = new Object[newTableSize];
         } else {
            if (minCapacity <= this.expandTableThreshold || this.hashTable.length >= 1073741824) {
               return;
            }

            newTableSize = this.hashTable.length * 2;
            this.hashTable = rebuildHashTable(newTableSize, this.dedupedElements, this.distinct);
         }

         this.maxRunBeforeFallback = maxRunBeforeFallback(newTableSize);
         this.expandTableThreshold = (int)(0.7 * newTableSize);
      }

      static boolean hashFloodingDetected(@Nullable Object[] hashTable) {
         int maxRunBeforeFallback = maxRunBeforeFallback(hashTable.length);
         int mask = hashTable.length - 1;
         int knownRunStart = 0;
         int knownRunEnd = 0;

         label35:
         while (knownRunStart < hashTable.length) {
            if (knownRunStart == knownRunEnd && hashTable[knownRunStart] == null) {
               if (hashTable[knownRunStart + maxRunBeforeFallback - 1 & mask] == null) {
                  knownRunStart += maxRunBeforeFallback;
               } else {
                  knownRunStart++;
               }

               knownRunEnd = knownRunStart;
            } else {
               for (int j = knownRunStart + maxRunBeforeFallback - 1; j >= knownRunEnd; j--) {
                  if (hashTable[j & mask] == null) {
                     knownRunEnd = knownRunStart + maxRunBeforeFallback;
                     knownRunStart = j + 1;
                     continue label35;
                  }
               }

               return true;
            }
         }

         return false;
      }

      static int maxRunBeforeFallback(int tableSize) {
         return 13 * IntMath.log2(tableSize, RoundingMode.UNNECESSARY);
      }
   }

   @J2ktIncompatible
   private static class SerializedForm implements Serializable {
      final Object[] elements;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      SerializedForm(Object[] elements) {
         this.elements = elements;
      }

      Object readResolve() {
         return ImmutableSet.copyOf(this.elements);
      }
   }

   private abstract static class SetBuilderImpl<E> {
      E[] dedupedElements;
      int distinct;

      SetBuilderImpl(int expectedCapacity) {
         this.dedupedElements = (E[])(new Object[expectedCapacity]);
         this.distinct = 0;
      }

      SetBuilderImpl(ImmutableSet.SetBuilderImpl<E> toCopy) {
         this.dedupedElements = Arrays.copyOf(toCopy.dedupedElements, toCopy.dedupedElements.length);
         this.distinct = toCopy.distinct;
      }

      private void ensureCapacity(int minCapacity) {
         if (minCapacity > this.dedupedElements.length) {
            int newCapacity = ImmutableCollection.Builder.expandedCapacity(this.dedupedElements.length, minCapacity);
            this.dedupedElements = Arrays.copyOf(this.dedupedElements, newCapacity);
         }
      }

      final void addDedupedElement(E e) {
         this.ensureCapacity(this.distinct + 1);
         this.dedupedElements[this.distinct++] = e;
      }

      abstract ImmutableSet.SetBuilderImpl<E> add(E e);

      final ImmutableSet.SetBuilderImpl<E> combine(ImmutableSet.SetBuilderImpl<E> other) {
         ImmutableSet.SetBuilderImpl<E> result = this;

         for (int i = 0; i < other.distinct; i++) {
            result = result.add(Objects.requireNonNull(other.dedupedElements[i]));
         }

         return result;
      }

      abstract ImmutableSet.SetBuilderImpl<E> copy();

      ImmutableSet.SetBuilderImpl<E> review() {
         return this;
      }

      abstract ImmutableSet<E> build();
   }
}
