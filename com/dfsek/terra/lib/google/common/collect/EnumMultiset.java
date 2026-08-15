package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.ObjIntConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
@J2ktIncompatible
public final class EnumMultiset<E extends Enum<E>> extends AbstractMultiset<E> implements Serializable {
   private transient Class<E> type;
   private transient E[] enumConstants;
   private transient int[] counts;
   private transient int distinctElements;
   private transient long size;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <E extends Enum<E>> EnumMultiset<E> create(Class<E> type) {
      return new EnumMultiset<>(type);
   }

   public static <E extends Enum<E>> EnumMultiset<E> create(Iterable<E> elements) {
      Iterator<E> iterator = elements.iterator();
      Preconditions.checkArgument(iterator.hasNext(), "EnumMultiset constructor passed empty Iterable");
      EnumMultiset<E> multiset = new EnumMultiset<>(iterator.next().getDeclaringClass());
      Iterables.addAll(multiset, elements);
      return multiset;
   }

   public static <E extends Enum<E>> EnumMultiset<E> create(Iterable<E> elements, Class<E> type) {
      EnumMultiset<E> result = create(type);
      Iterables.addAll(result, elements);
      return result;
   }

   private EnumMultiset(Class<E> type) {
      this.type = type;
      Preconditions.checkArgument(type.isEnum());
      this.enumConstants = type.getEnumConstants();
      this.counts = new int[this.enumConstants.length];
   }

   private boolean isActuallyE(@Nullable Object o) {
      if (!(o instanceof Enum)) {
         return false;
      }

      Enum<?> e = (Enum<?>)o;
      int index = e.ordinal();
      return index < this.enumConstants.length && this.enumConstants[index] == e;
   }

   private void checkIsE(Object element) {
      Preconditions.checkNotNull(element);
      if (!this.isActuallyE(element)) {
         throw new ClassCastException("Expected an " + this.type + " but got " + element);
      }
   }

   @Override
   int distinctElements() {
      return this.distinctElements;
   }

   @Override
   public int size() {
      return Ints.saturatedCast(this.size);
   }

   @Override
   public int count(@Nullable Object element) {
      if (element != null && this.isActuallyE(element)) {
         Enum<?> e = (Enum<?>)element;
         return this.counts[e.ordinal()];
      } else {
         return 0;
      }
   }

   @CanIgnoreReturnValue
   public int add(E element, int occurrences) {
      this.checkIsE(element);
      CollectPreconditions.checkNonnegative(occurrences, "occurrences");
      if (occurrences == 0) {
         return this.count(element);
      }

      int index = element.ordinal();
      int oldCount = this.counts[index];
      long newCount = (long)oldCount + occurrences;
      Preconditions.checkArgument(newCount <= 2147483647L, "too many occurrences: %s", newCount);
      this.counts[index] = (int)newCount;
      if (oldCount == 0) {
         this.distinctElements++;
      }

      this.size += occurrences;
      return oldCount;
   }

   @CanIgnoreReturnValue
   @Override
   public int remove(@Nullable Object element, int occurrences) {
      if (element != null && this.isActuallyE(element)) {
         Enum<?> e = (Enum<?>)element;
         CollectPreconditions.checkNonnegative(occurrences, "occurrences");
         if (occurrences == 0) {
            return this.count(element);
         }

         int index = e.ordinal();
         int oldCount = this.counts[index];
         if (oldCount == 0) {
            return 0;
         }

         if (oldCount <= occurrences) {
            this.counts[index] = 0;
            this.distinctElements--;
            this.size -= oldCount;
         } else {
            this.counts[index] = oldCount - occurrences;
            this.size -= occurrences;
         }

         return oldCount;
      } else {
         return 0;
      }
   }

   @CanIgnoreReturnValue
   public int setCount(E element, int count) {
      this.checkIsE(element);
      CollectPreconditions.checkNonnegative(count, "count");
      int index = element.ordinal();
      int oldCount = this.counts[index];
      this.counts[index] = count;
      this.size += count - oldCount;
      if (oldCount == 0 && count > 0) {
         this.distinctElements++;
      } else if (oldCount > 0 && count == 0) {
         this.distinctElements--;
      }

      return oldCount;
   }

   @Override
   public void clear() {
      Arrays.fill(this.counts, 0);
      this.size = 0L;
      this.distinctElements = 0;
   }

   @Override
   Iterator<E> elementIterator() {
      return new EnumMultiset<E>.Itr<E>() {
         E output(int index) {
            return EnumMultiset.this.enumConstants[index];
         }
      };
   }

   @Override
   Iterator<Multiset.Entry<E>> entryIterator() {
      return new EnumMultiset<E>.Itr<Multiset.Entry<E>>() {
         Multiset.Entry<E> output(int index) {
            return new Multisets.AbstractEntry<E>() {
               public E getElement() {
                  return EnumMultiset.this.enumConstants[index];
               }

               @Override
               public int getCount() {
                  return EnumMultiset.this.counts[index];
               }
            };
         }
      };
   }

   @Override
   public void forEachEntry(ObjIntConsumer<? super E> action) {
      Preconditions.checkNotNull(action);

      for (int i = 0; i < this.enumConstants.length; i++) {
         if (this.counts[i] > 0) {
            action.accept(this.enumConstants[i], this.counts[i]);
         }
      }
   }

   @Override
   public Iterator<E> iterator() {
      return Multisets.iteratorImpl(this);
   }

   @GwtIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(this.type);
      Serialization.writeMultiset(this, stream);
   }

   @GwtIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      Class<E> localType = Objects.requireNonNull((Class<E>)stream.readObject());
      this.type = localType;
      this.enumConstants = this.type.getEnumConstants();
      this.counts = new int[this.enumConstants.length];
      Serialization.populateMultiset(this, stream);
   }

   abstract class Itr<T> implements Iterator<T> {
      int index = 0;
      int toRemove = -1;

      abstract T output(int index);

      @Override
      public boolean hasNext() {
         while (this.index < EnumMultiset.this.enumConstants.length) {
            if (EnumMultiset.this.counts[this.index] > 0) {
               return true;
            }

            this.index++;
         }

         return false;
      }

      @Override
      public T next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         }

         T result = this.output(this.index);
         this.toRemove = this.index++;
         return result;
      }

      @Override
      public void remove() {
         CollectPreconditions.checkRemove(this.toRemove >= 0);
         if (EnumMultiset.this.counts[this.toRemove] > 0) {
            EnumMultiset.this.distinctElements--;
            EnumMultiset.this.size -= EnumMultiset.this.counts[this.toRemove];
            EnumMultiset.this.counts[this.toRemove] = 0;
         }

         this.toRemove = -1;
      }
   }
}
