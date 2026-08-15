package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.concurrent.LazyInit;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
public abstract class ImmutableMultiset<E> extends ImmutableMultisetGwtSerializationDependencies<E> implements Multiset<E> {
   @LazyInit
   private transient @Nullable ImmutableList<E> asList;
   @LazyInit
   private transient @Nullable ImmutableSet<Multiset.Entry<E>> entrySet;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = -889275714L;

   public static <E> Collector<E, ?, ImmutableMultiset<E>> toImmutableMultiset() {
      return CollectCollectors.toImmutableMultiset(Function.identity(), e -> 1);
   }

   public static <T, E> Collector<T, ?, ImmutableMultiset<E>> toImmutableMultiset(
      Function<? super T, ? extends E> elementFunction, ToIntFunction<? super T> countFunction
   ) {
      return CollectCollectors.toImmutableMultiset(elementFunction, countFunction);
   }

   public static <E> ImmutableMultiset<E> of() {
      return (ImmutableMultiset<E>)RegularImmutableMultiset.EMPTY;
   }

   public static <E> ImmutableMultiset<E> of(E e1) {
      return copyFromElements(e1);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2) {
      return copyFromElements(e1, e2);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2, E e3) {
      return copyFromElements(e1, e2, e3);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2, E e3, E e4) {
      return copyFromElements(e1, e2, e3, e4);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2, E e3, E e4, E e5) {
      return copyFromElements(e1, e2, e3, e4, e5);
   }

   public static <E> ImmutableMultiset<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E... others) {
      return new ImmutableMultiset.Builder<E>().add(e1).add(e2).add(e3).add(e4).add(e5).add(e6).add(others).build();
   }

   public static <E> ImmutableMultiset<E> copyOf(E[] elements) {
      return copyFromElements(elements);
   }

   public static <E> ImmutableMultiset<E> copyOf(Iterable<? extends E> elements) {
      if (elements instanceof ImmutableMultiset) {
         ImmutableMultiset<E> result = (ImmutableMultiset<E>)elements;
         if (!result.isPartialView()) {
            return result;
         }
      }

      Multiset<? extends E> multiset = elements instanceof Multiset ? (Multiset)elements : LinkedHashMultiset.create(elements);
      return copyFromEntries(multiset.entrySet());
   }

   public static <E> ImmutableMultiset<E> copyOf(Iterator<? extends E> elements) {
      Multiset<E> multiset = LinkedHashMultiset.create();
      Iterators.addAll(multiset, elements);
      return copyFromEntries(multiset.entrySet());
   }

   private static <E> ImmutableMultiset<E> copyFromElements(E... elements) {
      Multiset<E> multiset = LinkedHashMultiset.create();
      Collections.addAll(multiset, elements);
      return copyFromEntries(multiset.entrySet());
   }

   static <E> ImmutableMultiset<E> copyFromEntries(Collection<? extends Multiset.Entry<? extends E>> entries) {
      return entries.isEmpty() ? of() : RegularImmutableMultiset.create(entries);
   }

   ImmutableMultiset() {
   }

   @Override
   public UnmodifiableIterator<E> iterator() {
      final Iterator<Multiset.Entry<E>> entryIterator = this.entrySet().iterator();
      return new UnmodifiableIterator<E>() {
         int remaining;
         @Nullable Object element;

         @Override
         public boolean hasNext() {
            return this.remaining > 0 || entryIterator.hasNext();
         }

         @Override
         public E next() {
            if (this.remaining <= 0) {
               Multiset.Entry<E> entry = entryIterator.next();
               this.element = entry.getElement();
               this.remaining = entry.getCount();
            }

            this.remaining--;
            return Objects.requireNonNull((E)this.element);
         }
      };
   }

   @Override
   public ImmutableList<E> asList() {
      ImmutableList<E> result = this.asList;
      return result == null ? (this.asList = super.asList()) : result;
   }

   @Override
   public boolean contains(@Nullable Object object) {
      return this.count(object) > 0;
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final int add(E element, int occurrences) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final int remove(@Nullable Object element, int occurrences) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final int setCount(E element, int count) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean setCount(E element, int oldCount, int newCount) {
      throw new UnsupportedOperationException();
   }

   @GwtIncompatible
   @Override
   int copyIntoArray(@Nullable Object[] dst, int offset) {
      for (Multiset.Entry<E> entry : this.entrySet()) {
         Arrays.fill(dst, offset, offset + entry.getCount(), entry.getElement());
         offset += entry.getCount();
      }

      return offset;
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return Multisets.equalsImpl(this, object);
   }

   @Override
   public int hashCode() {
      return Sets.hashCodeImpl(this.entrySet());
   }

   @Override
   public String toString() {
      return this.entrySet().toString();
   }

   public abstract ImmutableSet<E> elementSet();

   public ImmutableSet<Multiset.Entry<E>> entrySet() {
      ImmutableSet<Multiset.Entry<E>> es = this.entrySet;
      return es == null ? (this.entrySet = this.createEntrySet()) : es;
   }

   private ImmutableSet<Multiset.Entry<E>> createEntrySet() {
      return this.isEmpty() ? ImmutableSet.of() : new ImmutableMultiset.EntrySet();
   }

   abstract Multiset.Entry<E> getEntry(int index);

   @GwtIncompatible
   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return new ImmutableMultiset.SerializedForm(this);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   public static <E> ImmutableMultiset.Builder<E> builder() {
      return new ImmutableMultiset.Builder<>();
   }

   public static class Builder<E> extends ImmutableCollection.Builder<E> {
      final Multiset<E> contents;

      public Builder() {
         this(LinkedHashMultiset.create());
      }

      Builder(Multiset<E> contents) {
         this.contents = contents;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> add(E element) {
         this.contents.add(Preconditions.checkNotNull(element));
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> add(E... elements) {
         super.add(elements);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> addCopies(E element, int occurrences) {
         this.contents.add(Preconditions.checkNotNull(element), occurrences);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> setCount(E element, int count) {
         this.contents.setCount(Preconditions.checkNotNull(element), count);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> addAll(Iterable<? extends E> elements) {
         if (elements instanceof Multiset) {
            Multiset<? extends E> multiset = (Multiset<? extends E>)elements;
            multiset.forEachEntry((e, n) -> this.contents.add(Preconditions.checkNotNull((E)e), n));
         } else {
            super.addAll(elements);
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultiset.Builder<E> addAll(Iterator<? extends E> elements) {
         super.addAll(elements);
         return this;
      }

      public ImmutableMultiset<E> build() {
         return ImmutableMultiset.copyOf(this.contents);
      }

      @VisibleForTesting
      ImmutableMultiset<E> buildJdkBacked() {
         return this.contents.isEmpty() ? ImmutableMultiset.of() : JdkBackedImmutableMultiset.create(this.contents.entrySet());
      }
   }

   static final class ElementSet<E> extends ImmutableSet.Indexed<E> {
      private final List<Multiset.Entry<E>> entries;
      private final Multiset<E> delegate;

      ElementSet(List<Multiset.Entry<E>> entries, Multiset<E> delegate) {
         this.entries = entries;
         this.delegate = delegate;
      }

      @Override
      E get(int index) {
         return this.entries.get(index).getElement();
      }

      @Override
      public boolean contains(@Nullable Object object) {
         return this.delegate.contains(object);
      }

      @Override
      boolean isPartialView() {
         return true;
      }

      @Override
      public int size() {
         return this.entries.size();
      }

      @J2ktIncompatible
      @GwtIncompatible
      @Override
      Object writeReplace() {
         return super.writeReplace();
      }
   }

   private final class EntrySet extends IndexedImmutableSet<Multiset.Entry<E>> {
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      private EntrySet() {
      }

      @Override
      boolean isPartialView() {
         return ImmutableMultiset.this.isPartialView();
      }

      Multiset.Entry<E> get(int index) {
         return ImmutableMultiset.this.getEntry(index);
      }

      @Override
      public int size() {
         return ImmutableMultiset.this.elementSet().size();
      }

      @Override
      public boolean contains(@Nullable Object o) {
         if (o instanceof Multiset.Entry) {
            Multiset.Entry<?> entry = (Multiset.Entry<?>)o;
            if (entry.getCount() <= 0) {
               return false;
            }

            int count = ImmutableMultiset.this.count(entry.getElement());
            return count == entry.getCount();
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return ImmutableMultiset.this.hashCode();
      }

      @GwtIncompatible
      @J2ktIncompatible
      @Override
      Object writeReplace() {
         return new ImmutableMultiset.EntrySetSerializedForm<>(ImmutableMultiset.this);
      }

      @GwtIncompatible
      @J2ktIncompatible
      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Use EntrySetSerializedForm");
      }
   }

   @GwtIncompatible
   @J2ktIncompatible
   static class EntrySetSerializedForm<E> implements Serializable {
      final ImmutableMultiset<E> multiset;

      EntrySetSerializedForm(ImmutableMultiset<E> multiset) {
         this.multiset = multiset;
      }

      Object readResolve() {
         return this.multiset.entrySet();
      }
   }

   @J2ktIncompatible
   static final class SerializedForm implements Serializable {
      final Object[] elements;
      final int[] counts;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      SerializedForm(Multiset<? extends Object> multiset) {
         int distinct = multiset.entrySet().size();
         this.elements = new Object[distinct];
         this.counts = new int[distinct];
         int i = 0;

         for (Multiset.Entry<? extends Object> entry : multiset.entrySet()) {
            this.elements[i] = entry.getElement();
            this.counts[i] = entry.getCount();
            i++;
         }
      }

      Object readResolve() {
         LinkedHashMultiset<Object> multiset = LinkedHashMultiset.create(this.elements.length);

         for (int i = 0; i < this.elements.length; i++) {
            multiset.add(this.elements[i], this.counts[i]);
         }

         return ImmutableMultiset.copyOf(multiset);
      }
   }
}
