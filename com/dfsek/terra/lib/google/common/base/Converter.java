package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.InlineMe;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.Serializable;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class Converter<A, B> implements Function<A, B> {
   private final boolean handleNullAutomatically;
   @LazyInit
   @RetainedWith
   private transient @Nullable Converter<B, A> reverse;

   protected Converter() {
      this(true);
   }

   Converter(boolean handleNullAutomatically) {
      this.handleNullAutomatically = handleNullAutomatically;
   }

   @ForOverride
   protected abstract B doForward(A a);

   @ForOverride
   protected abstract A doBackward(B b);

   public final @Nullable B convert(@Nullable A a) {
      return this.correctedDoForward(a);
   }

   @Nullable B correctedDoForward(@Nullable A a) {
      if (this.handleNullAutomatically) {
         return a == null ? null : Preconditions.checkNotNull(this.doForward(a));
      } else {
         return this.unsafeDoForward(a);
      }
   }

   @Nullable A correctedDoBackward(@Nullable B b) {
      if (this.handleNullAutomatically) {
         return b == null ? null : Preconditions.checkNotNull(this.doBackward(b));
      } else {
         return this.unsafeDoBackward(b);
      }
   }

   private @Nullable B unsafeDoForward(@Nullable A a) {
      return this.doForward(NullnessCasts.uncheckedCastNullableTToT(a));
   }

   private @Nullable A unsafeDoBackward(@Nullable B b) {
      return this.doBackward(NullnessCasts.uncheckedCastNullableTToT(b));
   }

   public Iterable<B> convertAll(Iterable<? extends A> fromIterable) {
      Preconditions.checkNotNull(fromIterable, "fromIterable");
      return () -> new Iterator<B>() {
         private final Iterator<? extends A> fromIterator = fromIterable.iterator();

         @Override
         public boolean hasNext() {
            return this.fromIterator.hasNext();
         }

         @Override
         public B next() {
            return (B)Converter.this.convert((A)this.fromIterator.next());
         }

         @Override
         public void remove() {
            this.fromIterator.remove();
         }
      };
   }

   @CheckReturnValue
   public Converter<B, A> reverse() {
      Converter<B, A> result = this.reverse;
      return result == null ? (this.reverse = (Converter<B, A>)(new Converter.ReverseConverter<>(this))) : result;
   }

   public final <C> Converter<A, C> andThen(Converter<B, C> secondConverter) {
      return this.doAndThen(secondConverter);
   }

   <C> Converter<A, C> doAndThen(Converter<B, C> secondConverter) {
      return new Converter.ConverterComposition<>(this, Preconditions.checkNotNull(secondConverter));
   }

   @Deprecated
   @InlineMe(replacement = "this.convert(a)")
   @Override
   public final B apply(A a) {
      return this.convert(a);
   }

   @Override
   public boolean equals(@Nullable Object object) {
      return super.equals(object);
   }

   public static <A, B> Converter<A, B> from(Function<? super A, ? extends B> forwardFunction, Function<? super B, ? extends A> backwardFunction) {
      return new Converter.FunctionBasedConverter<>(forwardFunction, backwardFunction);
   }

   public static <T> Converter<T, T> identity() {
      return (Converter.IdentityConverter)Converter.IdentityConverter.INSTANCE;
   }

   private static final class ConverterComposition<A, B, C> extends Converter<A, C> implements Serializable {
      final Converter<A, B> first;
      final Converter<B, C> second;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      ConverterComposition(Converter<A, B> first, Converter<B, C> second) {
         this.first = first;
         this.second = second;
      }

      @Override
      protected C doForward(A a) {
         throw new AssertionError();
      }

      @Override
      protected A doBackward(C c) {
         throw new AssertionError();
      }

      @Override
      @Nullable C correctedDoForward(@Nullable A a) {
         return this.second.correctedDoForward(this.first.correctedDoForward(a));
      }

      @Override
      @Nullable A correctedDoBackward(@Nullable C c) {
         return this.first.correctedDoBackward(this.second.correctedDoBackward(c));
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (!(object instanceof Converter.ConverterComposition)) {
            return false;
         }

         Converter.ConverterComposition<?, ?, ?> that = (Converter.ConverterComposition<?, ?, ?>)object;
         return this.first.equals(that.first) && this.second.equals(that.second);
      }

      @Override
      public int hashCode() {
         return 31 * this.first.hashCode() + this.second.hashCode();
      }

      @Override
      public String toString() {
         return this.first + ".andThen(" + this.second + ")";
      }
   }

   private static final class FunctionBasedConverter<A, B> extends Converter<A, B> implements Serializable {
      private final Function<? super A, ? extends B> forwardFunction;
      private final Function<? super B, ? extends A> backwardFunction;

      private FunctionBasedConverter(Function<? super A, ? extends B> forwardFunction, Function<? super B, ? extends A> backwardFunction) {
         this.forwardFunction = Preconditions.checkNotNull(forwardFunction);
         this.backwardFunction = Preconditions.checkNotNull(backwardFunction);
      }

      @Override
      protected B doForward(A a) {
         return (B)this.forwardFunction.apply(a);
      }

      @Override
      protected A doBackward(B b) {
         return (A)this.backwardFunction.apply(b);
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (!(object instanceof Converter.FunctionBasedConverter)) {
            return false;
         }

         Converter.FunctionBasedConverter<?, ?> that = (Converter.FunctionBasedConverter<?, ?>)object;
         return this.forwardFunction.equals(that.forwardFunction) && this.backwardFunction.equals(that.backwardFunction);
      }

      @Override
      public int hashCode() {
         return this.forwardFunction.hashCode() * 31 + this.backwardFunction.hashCode();
      }

      @Override
      public String toString() {
         return "Converter.from(" + this.forwardFunction + ", " + this.backwardFunction + ")";
      }
   }

   private static final class IdentityConverter<T> extends Converter<T, T> implements Serializable {
      static final Converter<?, ?> INSTANCE = new Converter.IdentityConverter();
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      @Override
      protected T doForward(T t) {
         return t;
      }

      @Override
      protected T doBackward(T t) {
         return t;
      }

      public Converter.IdentityConverter<T> reverse() {
         return this;
      }

      @Override
      <S> Converter<T, S> doAndThen(Converter<T, S> otherConverter) {
         return Preconditions.checkNotNull(otherConverter, "otherConverter");
      }

      @Override
      public String toString() {
         return "Converter.identity()";
      }

      private Object readResolve() {
         return INSTANCE;
      }
   }

   private static final class ReverseConverter<A, B> extends Converter<B, A> implements Serializable {
      final Converter<A, B> original;
      @GwtIncompatible
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      ReverseConverter(Converter<A, B> original) {
         this.original = original;
      }

      @Override
      protected A doForward(B b) {
         throw new AssertionError();
      }

      @Override
      protected B doBackward(A a) {
         throw new AssertionError();
      }

      @Override
      @Nullable A correctedDoForward(@Nullable B b) {
         return this.original.correctedDoBackward(b);
      }

      @Override
      @Nullable B correctedDoBackward(@Nullable A a) {
         return this.original.correctedDoForward(a);
      }

      @Override
      public Converter<A, B> reverse() {
         return this.original;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (object instanceof Converter.ReverseConverter) {
            Converter.ReverseConverter<?, ?> that = (Converter.ReverseConverter<?, ?>)object;
            return this.original.equals(that.original);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return ~this.original.hashCode();
      }

      @Override
      public String toString() {
         return this.original + ".reverse()";
      }
   }
}
