package com.dfsek.terra.api.util.generic.pair;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Pair<L, R> {
   private static final Pair<?, ?> NULL = new Pair(null, null);
   private final L left;
   private final R right;

   private Pair(L left, R right) {
      this.left = left;
      this.right = right;
   }

   public static <L, R, T> Function<Pair<L, R>, Pair<T, R>> mapLeft(Function<L, T> function) {
      return pair -> of(function.apply(pair.left), pair.right);
   }

   public static <L, R, T> Function<Pair<L, R>, Pair<L, T>> mapRight(Function<R, T> function) {
      return pair -> of(pair.left, function.apply(pair.right));
   }

   public static <L> Predicate<Pair<L, ?>> testLeft(Predicate<L> predicate) {
      return pair -> predicate.test(pair.left);
   }

   public static <R> Predicate<Pair<?, R>> testRight(Predicate<R> predicate) {
      return pair -> predicate.test(pair.right);
   }

   public static <L> Consumer<Pair<L, ?>> consumeLeft(Consumer<L> consumer) {
      return pair -> consumer.accept(pair.left);
   }

   public static <R> Consumer<Pair<?, R>> consumeRight(Consumer<R> consumer) {
      return pair -> consumer.accept(pair.right);
   }

   public static <R> Function<Pair<?, R>, R> unwrapRight() {
      return pair -> pair.right;
   }

   public static <L> Function<Pair<L, ?>, L> unwrapLeft() {
      return pair -> pair.left;
   }

   @Contract("_, _ -> new")
   public static <L1, R1> Pair<L1, R1> of(L1 left, R1 right) {
      return new Pair<>(left, right);
   }

   @Contract("-> new")
   public static <L1, R1> Pair<L1, R1> ofNull() {
      return (Pair<L1, R1>)NULL;
   }

   @NotNull
   @Contract("-> new")
   public Pair.Mutable<L, R> mutable() {
      return Pair.Mutable.of(this.left, this.right);
   }

   public R getRight() {
      return this.right;
   }

   public L getLeft() {
      return this.left;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.left, this.right);
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Pair<?, ?> that) ? false : Objects.equals(this.left, that.left) && Objects.equals(this.right, that.right);
   }

   public Pair<L, R> apply(BiConsumer<L, R> consumer) {
      consumer.accept(this.left, this.right);
      return this;
   }

   @Override
   public String toString() {
      return String.format("{%s,%s}", this.left, this.right);
   }

   public static class Mutable<L, R> {
      private L left;
      private R right;

      private Mutable(L left, R right) {
         this.left = left;
         this.right = right;
      }

      @NotNull
      @Contract("_, _ -> new")
      public static <L1, R1> Pair.Mutable<L1, R1> of(L1 left, R1 right) {
         return new Pair.Mutable<>(left, right);
      }

      @Contract("-> new")
      public Pair<L, R> immutable() {
         return Pair.of(this.left, this.right);
      }

      public L getLeft() {
         return this.left;
      }

      public void setLeft(L left) {
         this.left = left;
      }

      public R getRight() {
         return this.right;
      }

      public void setRight(R right) {
         this.right = right;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.left, this.right);
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof Pair.Mutable<?, ?> that) ? false : Objects.equals(this.left, that.left) && Objects.equals(this.right, that.right);
      }
   }
}
