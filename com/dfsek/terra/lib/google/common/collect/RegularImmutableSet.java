package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import java.util.Spliterator;
import java.util.Spliterators;
import org.jspecify.annotations.Nullable;

@GwtCompatible(serializable = true, emulated = true)
final class RegularImmutableSet<E> extends ImmutableSet.CachingAsList<E> {
   private static final Object[] EMPTY_ARRAY = new Object[0];
   static final RegularImmutableSet<Object> EMPTY = new RegularImmutableSet<>(EMPTY_ARRAY, 0, EMPTY_ARRAY, 0);
   private final transient Object[] elements;
   private final transient int hashCode;
   @VisibleForTesting
   final transient @Nullable Object[] table;
   private final transient int mask;

   RegularImmutableSet(Object[] elements, int hashCode, @Nullable Object[] table, int mask) {
      this.elements = elements;
      this.hashCode = hashCode;
      this.table = table;
      this.mask = mask;
   }

   @Override
   public boolean contains(Object target) {
      Object[] table = this.table;
      if (target != null && table.length != 0) {
         int i = Hashing.smearedHash(target);

         while (true) {
            i &= this.mask;
            Object candidate = table[i];
            if (candidate == null) {
               return false;
            }

            if (candidate.equals(target)) {
               return true;
            }

            i++;
         }
      } else {
         return false;
      }
   }

   @Override
   public int size() {
      return this.elements.length;
   }

   @Override
   public UnmodifiableIterator<E> iterator() {
      return Iterators.forArray((E[])this.elements);
   }

   @Override
   public Spliterator<E> spliterator() {
      return Spliterators.spliterator(this.elements, 1297);
   }

   @Override
   Object[] internalArray() {
      return this.elements;
   }

   @Override
   int internalArrayStart() {
      return 0;
   }

   @Override
   int internalArrayEnd() {
      return this.elements.length;
   }

   @Override
   int copyIntoArray(@Nullable Object[] dst, int offset) {
      System.arraycopy(this.elements, 0, dst, offset, this.elements.length);
      return offset + this.elements.length;
   }

   @Override
   ImmutableList<E> createAsList() {
      return this.table.length == 0 ? ImmutableList.of() : new RegularImmutableAsList<>(this, this.elements);
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @Override
   boolean isHashCodeFast() {
      return true;
   }

   @J2ktIncompatible
   @GwtIncompatible
   @Override
   Object writeReplace() {
      return super.writeReplace();
   }
}
