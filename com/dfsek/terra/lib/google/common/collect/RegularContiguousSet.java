package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class RegularContiguousSet<C extends Comparable> extends ContiguousSet<C> {
   private final Range<C> range;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   RegularContiguousSet(Range<C> range, DiscreteDomain<C> domain) {
      super(domain);
      this.range = range;
   }

   private ContiguousSet<C> intersectionInCurrentDomain(Range<C> other) {
      return this.range.isConnected(other) ? ContiguousSet.create(this.range.intersection(other), this.domain) : new EmptyContiguousSet<>(this.domain);
   }

   @Override
   ContiguousSet<C> headSetImpl(C toElement, boolean inclusive) {
      return this.intersectionInCurrentDomain(Range.upTo(toElement, BoundType.forBoolean(inclusive)));
   }

   @Override
   ContiguousSet<C> subSetImpl(C fromElement, boolean fromInclusive, C toElement, boolean toInclusive) {
      return fromElement.compareTo(toElement) == 0 && !fromInclusive && !toInclusive
         ? new EmptyContiguousSet<>(this.domain)
         : this.intersectionInCurrentDomain(Range.range(fromElement, BoundType.forBoolean(fromInclusive), toElement, BoundType.forBoolean(toInclusive)));
   }

   @Override
   ContiguousSet<C> tailSetImpl(C fromElement, boolean inclusive) {
      return this.intersectionInCurrentDomain(Range.downTo(fromElement, BoundType.forBoolean(inclusive)));
   }

   @GwtIncompatible
   @Override
   int indexOf(@Nullable Object target) {
      if (!this.contains(target)) {
         return -1;
      }

      C c = Objects.requireNonNull((C)target);
      return (int)this.domain.distance(this.first(), c);
   }

   @Override
   public UnmodifiableIterator<C> iterator() {
      return new AbstractSequentialIterator<C>(this.first()) {
         final Comparable last = RegularContiguousSet.this.last();

         protected @Nullable C computeNext(C previous) {
            return RegularContiguousSet.equalsOrThrow(previous, this.last) ? null : RegularContiguousSet.this.domain.next(previous);
         }
      };
   }

   @GwtIncompatible
   @Override
   public UnmodifiableIterator<C> descendingIterator() {
      return new AbstractSequentialIterator<C>(this.last()) {
         final Comparable first = RegularContiguousSet.this.first();

         protected @Nullable C computeNext(C previous) {
            return RegularContiguousSet.equalsOrThrow(previous, this.first) ? null : RegularContiguousSet.this.domain.previous(previous);
         }
      };
   }

   private static boolean equalsOrThrow(Comparable<?> left, @Nullable Comparable<?> right) {
      return right != null && Range.compareOrThrow(left, right) == 0;
   }

   @Override
   boolean isPartialView() {
      return false;
   }

   public C first() {
      return Objects.requireNonNull(this.range.lowerBound.leastValueAbove(this.domain));
   }

   public C last() {
      return Objects.requireNonNull(this.range.upperBound.greatestValueBelow(this.domain));
   }

   @Override
   ImmutableList<C> createAsList() {
      return this.domain.supportsFastOffset ? new ImmutableAsList<C>() {
         ImmutableSortedSet<C> delegateCollection() {
            return RegularContiguousSet.this;
         }

         public C get(int i) {
            Preconditions.checkElementIndex(i, this.size());
            return RegularContiguousSet.this.domain.offset((C)RegularContiguousSet.this.first(), i);
         }

         @J2ktIncompatible
         @GwtIncompatible
         @Override
         Object writeReplace() {
            return super.writeReplace();
         }
      } : super.createAsList();
   }

   @Override
   public int size() {
      long distance = this.domain.distance(this.first(), this.last());
      return distance >= 2147483647L ? Integer.MAX_VALUE : (int)distance + 1;
   }

   @Override
   public boolean contains(@Nullable Object object) {
      if (object == null) {
         return false;
      }

      try {
         C c = (C)object;
         return this.range.contains(c);
      } catch (ClassCastException e) {
         return false;
      }
   }

   @Override
   public boolean containsAll(Collection<?> targets) {
      return Collections2.containsAllImpl(this, targets);
   }

   @Override
   public boolean isEmpty() {
      return false;
   }

   @Override
   public ContiguousSet<C> intersection(ContiguousSet<C> other) {
      Preconditions.checkNotNull(other);
      Preconditions.checkArgument(this.domain.equals(other.domain));
      if (other.isEmpty()) {
         return other;
      }

      C lowerEndpoint = Ordering.natural().max(this.first(), other.first());
      C upperEndpoint = Ordering.natural().min(this.last(), other.last());
      return lowerEndpoint.compareTo(upperEndpoint) <= 0
         ? ContiguousSet.create(Range.closed(lowerEndpoint, upperEndpoint), this.domain)
         : new EmptyContiguousSet<>(this.domain);
   }

   @Override
   public Range<C> range() {
      return this.range(BoundType.CLOSED, BoundType.CLOSED);
   }

   @Override
   public Range<C> range(BoundType lowerBoundType, BoundType upperBoundType) {
      return Range.create(
         this.range.lowerBound.withLowerBoundType(lowerBoundType, this.domain), this.range.upperBound.withUpperBoundType(upperBoundType, this.domain)
      );
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      }

      if (object instanceof RegularContiguousSet) {
         RegularContiguousSet<?> that = (RegularContiguousSet<?>)object;
         if (this.domain.equals(that.domain)) {
            return this.first().equals(that.first()) && this.last().equals(that.last());
         }
      }

      return super.equals(object);
   }

   @Override
   public int hashCode() {
      return Sets.hashCodeImpl(this);
   }

   @GwtIncompatible
   @J2ktIncompatible
   @Override
   Object writeReplace() {
      return new RegularContiguousSet.SerializedForm(this.range, this.domain);
   }

   @GwtIncompatible
   @J2ktIncompatible
   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Use SerializedForm");
   }

   @GwtIncompatible
   @J2ktIncompatible
   private static final class SerializedForm<C extends Comparable> implements Serializable {
      final Range<C> range;
      final DiscreteDomain<C> domain;

      private SerializedForm(Range<C> range, DiscreteDomain<C> domain) {
         this.range = range;
         this.domain = domain;
      }

      private Object readResolve() {
         return new RegularContiguousSet<>(this.range, this.domain);
      }
   }
}
