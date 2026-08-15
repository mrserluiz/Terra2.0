package com.dfsek.terra.lib.commons.lang3;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class Range<T> implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Comparator<T> comparator;
   private transient int hashCode;
   private final T maximum;
   private final T minimum;
   private transient String toString;

   @Deprecated
   public static <T extends Comparable<? super T>> Range<T> between(T fromInclusive, T toInclusive) {
      return of(fromInclusive, toInclusive, null);
   }

   @Deprecated
   public static <T> Range<T> between(T fromInclusive, T toInclusive, Comparator<T> comparator) {
      return new Range<>(fromInclusive, toInclusive, comparator);
   }

   public static <T extends Comparable<? super T>> Range<T> is(T element) {
      return of(element, element, null);
   }

   public static <T> Range<T> is(T element, Comparator<T> comparator) {
      return of(element, element, comparator);
   }

   public static <T extends Comparable<? super T>> Range<T> of(T fromInclusive, T toInclusive) {
      return of(fromInclusive, toInclusive, null);
   }

   public static <T> Range<T> of(T fromInclusive, T toInclusive, Comparator<T> comparator) {
      return new Range<>(fromInclusive, toInclusive, comparator);
   }

   Range(T element1, T element2, Comparator<T> comp) {
      Objects.requireNonNull(element1, "element1");
      Objects.requireNonNull(element2, "element2");
      if (comp == null) {
         this.comparator = Range.ComparableComparator.INSTANCE;
      } else {
         this.comparator = comp;
      }

      if (this.comparator.compare(element1, element2) < 1) {
         this.minimum = element1;
         this.maximum = element2;
      } else {
         this.minimum = element2;
         this.maximum = element1;
      }
   }

   public boolean contains(T element) {
      return element == null ? false : this.comparator.compare(element, this.minimum) > -1 && this.comparator.compare(element, this.maximum) < 1;
   }

   public boolean containsRange(Range<T> otherRange) {
      return otherRange == null ? false : this.contains(otherRange.minimum) && this.contains(otherRange.maximum);
   }

   public int elementCompareTo(T element) {
      Objects.requireNonNull(element, "element");
      if (this.isAfter(element)) {
         return -1;
      } else {
         return this.isBefore(element) ? 1 : 0;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         Range<T> range = (Range<T>)obj;
         return this.minimum.equals(range.minimum) && this.maximum.equals(range.maximum);
      } else {
         return false;
      }
   }

   public T fit(T element) {
      Objects.requireNonNull(element, "element");
      if (this.isAfter(element)) {
         return this.minimum;
      } else {
         return this.isBefore(element) ? this.maximum : element;
      }
   }

   public Comparator<T> getComparator() {
      return this.comparator;
   }

   public T getMaximum() {
      return this.maximum;
   }

   public T getMinimum() {
      return this.minimum;
   }

   @Override
   public int hashCode() {
      int result = this.hashCode;
      if (this.hashCode == 0) {
         int var2 = 17;
         int var3 = 37 * var2 + this.getClass().hashCode();
         int var4 = 37 * var3 + this.minimum.hashCode();
         result = 37 * var4 + this.maximum.hashCode();
         this.hashCode = result;
      }

      return result;
   }

   public Range<T> intersectionWith(Range<T> other) {
      if (!this.isOverlappedBy(other)) {
         throw new IllegalArgumentException(String.format("Cannot calculate intersection with non-overlapping range %s", other));
      }

      if (this.equals(other)) {
         return this;
      }

      T min = this.getComparator().compare(this.minimum, other.minimum) < 0 ? other.minimum : this.minimum;
      T max = this.getComparator().compare(this.maximum, other.maximum) < 0 ? this.maximum : other.maximum;
      return of(min, max, this.getComparator());
   }

   public boolean isAfter(T element) {
      return element == null ? false : this.comparator.compare(element, this.minimum) < 0;
   }

   public boolean isAfterRange(Range<T> otherRange) {
      return otherRange == null ? false : this.isAfter(otherRange.maximum);
   }

   public boolean isBefore(T element) {
      return element == null ? false : this.comparator.compare(element, this.maximum) > 0;
   }

   public boolean isBeforeRange(Range<T> otherRange) {
      return otherRange == null ? false : this.isBefore(otherRange.minimum);
   }

   public boolean isEndedBy(T element) {
      return element == null ? false : this.comparator.compare(element, this.maximum) == 0;
   }

   public boolean isNaturalOrdering() {
      return this.comparator == Range.ComparableComparator.INSTANCE;
   }

   public boolean isOverlappedBy(Range<T> otherRange) {
      return otherRange == null ? false : otherRange.contains(this.minimum) || otherRange.contains(this.maximum) || this.contains(otherRange.minimum);
   }

   public boolean isStartedBy(T element) {
      return element == null ? false : this.comparator.compare(element, this.minimum) == 0;
   }

   @Override
   public String toString() {
      if (this.toString == null) {
         this.toString = "[" + this.minimum + ".." + this.maximum + "]";
      }

      return this.toString;
   }

   public String toString(String format) {
      return String.format(format, this.minimum, this.maximum, this.comparator);
   }

   private enum ComparableComparator implements Comparator {
      INSTANCE;

      @Override
      public int compare(Object obj1, Object obj2) {
         return ((Comparable)obj1).compareTo(obj2);
      }
   }
}
