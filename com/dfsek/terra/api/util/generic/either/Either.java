package com.dfsek.terra.api.util.generic.either;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Either<L, R> {
   private final L left;
   private final R right;
   private final boolean leftPresent;

   private Either(L left, R right, boolean leftPresent) {
      this.left = left;
      this.right = right;
      this.leftPresent = leftPresent;
   }

   @NotNull
   @Contract("_ -> new")
   public static <L1, R1> Either<L1, R1> left(L1 left) {
      return (Either<L1, R1>)(new Either<>(Objects.requireNonNull(left), null, true));
   }

   @NotNull
   @Contract("_ -> new")
   public static <L1, R1> Either<L1, R1> right(R1 right) {
      return (Either<L1, R1>)(new Either<>(null, Objects.requireNonNull(right), false));
   }

   @NotNull
   @Contract("_ -> this")
   public Either<L, R> ifLeft(Consumer<L> action) {
      if (this.leftPresent) {
         action.accept(this.left);
      }

      return this;
   }

   @NotNull
   @Contract("_ -> this")
   public Either<L, R> ifRight(Consumer<R> action) {
      if (!this.leftPresent) {
         action.accept(this.right);
      }

      return this;
   }

   @NotNull
   public Optional<L> getLeft() {
      return this.leftPresent ? Optional.of(this.left) : Optional.empty();
   }

   @NotNull
   public Optional<R> getRight() {
      return !this.leftPresent ? Optional.of(this.right) : Optional.empty();
   }

   public boolean hasLeft() {
      return this.leftPresent;
   }

   public boolean hasRight() {
      return !this.leftPresent;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.left, this.right);
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Either<?, ?> that)
         ? false
         : this.leftPresent && that.leftPresent && Objects.equals(this.left, that.left)
            || !this.leftPresent && !that.leftPresent && Objects.equals(this.right, that.right);
   }
}
