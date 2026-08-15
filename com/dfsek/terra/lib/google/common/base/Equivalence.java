package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.InlineMe;
import java.io.Serializable;
import java.util.function.BiPredicate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class Equivalence<T> implements BiPredicate<T, T> {
   protected Equivalence() {
   }

   public final boolean equivalent(@Nullable T a, @Nullable T b) {
      if (a == b) {
         return true;
      } else {
         return a != null && b != null ? this.doEquivalent(a, b) : false;
      }
   }

   @Deprecated
   @InlineMe(replacement = "this.equivalent(t, u)")
   @Override
   public final boolean test(@Nullable T t, @Nullable T u) {
      return this.equivalent(t, u);
   }

   @ForOverride
   protected abstract boolean doEquivalent(T a, T b);

   public final int hash(@Nullable T t) {
      return t == null ? 0 : this.doHash(t);
   }

   @ForOverride
   protected abstract int doHash(T t);

   public final <F> Equivalence<F> onResultOf(Function<? super F, ? extends @Nullable T> function) {
      return new FunctionalEquivalence<>(function, this);
   }

   public final <S extends T> Equivalence.Wrapper<S> wrap(@ParametricNullness S reference) {
      return new Equivalence.Wrapper<>(this, reference);
   }

   @GwtCompatible(serializable = true)
   public final <S extends T> Equivalence<Iterable<S>> pairwise() {
      return new PairwiseEquivalence<>(this);
   }

   public final Predicate<@Nullable T> equivalentTo(@Nullable T target) {
      return new Equivalence.EquivalentToPredicate<>(this, target);
   }

   public static Equivalence<Object> equals() {
      return Equivalence.Equals.INSTANCE;
   }

   public static Equivalence<Object> identity() {
      return Equivalence.Identity.INSTANCE;
   }

   static final class Equals extends Equivalence<Object> implements Serializable {
      static final Equivalence.Equals INSTANCE = new Equivalence.Equals();
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean doEquivalent(Object a, Object b) {
         return a.equals(b);
      }

      @Override
      protected int doHash(Object o) {
         return o.hashCode();
      }

      private Object readResolve() {
         return INSTANCE;
      }
   }

   private static final class EquivalentToPredicate<T> implements Predicate<T>, Serializable {
      private final Equivalence<T> equivalence;
      private final @Nullable T target;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      EquivalentToPredicate(Equivalence<T> equivalence, @Nullable T target) {
         this.equivalence = Preconditions.checkNotNull(equivalence);
         this.target = target;
      }

      @Override
      public boolean apply(@Nullable T input) {
         return this.equivalence.equivalent(input, this.target);
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof Equivalence.EquivalentToPredicate)) {
            return false;
         }

         Equivalence.EquivalentToPredicate<?> that = (Equivalence.EquivalentToPredicate<?>)obj;
         return this.equivalence.equals(that.equivalence) && Objects.equal(this.target, that.target);
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.equivalence, this.target);
      }

      @Override
      public String toString() {
         return this.equivalence + ".equivalentTo(" + this.target + ")";
      }
   }

   static final class Identity extends Equivalence<Object> implements Serializable {
      static final Equivalence.Identity INSTANCE = new Equivalence.Identity();
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean doEquivalent(Object a, Object b) {
         return false;
      }

      @Override
      protected int doHash(Object o) {
         return System.identityHashCode(o);
      }

      private Object readResolve() {
         return INSTANCE;
      }
   }

   public static final class Wrapper<T> implements Serializable {
      private final Equivalence<? super @NonNull T> equivalence;
      @ParametricNullness
      private final T reference;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      private Wrapper(Equivalence<? super @NonNull T> equivalence, @ParametricNullness T reference) {
         this.equivalence = Preconditions.checkNotNull(equivalence);
         this.reference = reference;
      }

      @ParametricNullness
      public T get() {
         return this.reference;
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (obj == this) {
            return true;
         }

         if (obj instanceof Equivalence.Wrapper) {
            Equivalence.Wrapper<?> that = (Equivalence.Wrapper<?>)obj;
            if (this.equivalence.equals(that.equivalence)) {
               Equivalence<Object> equivalence = this.equivalence;
               return equivalence.equivalent(this.reference, that.reference);
            }
         }

         return false;
      }

      @Override
      public int hashCode() {
         return this.equivalence.hash(this.reference);
      }

      @Override
      public String toString() {
         return this.equivalence + ".wrap(" + this.reference + ")";
      }
   }
}
