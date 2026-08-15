package com.dfsek.terra.lib.commons.lang3.tuple;

import java.util.Objects;
import java.util.Map.Entry;

public class ImmutablePair<L, R> extends Pair<L, R> {
   public static final ImmutablePair<?, ?>[] EMPTY_ARRAY = new ImmutablePair[0];
   private static final ImmutablePair NULL = new ImmutablePair(null, null);
   private static final long serialVersionUID = 4954918890077093841L;
   public final L left;
   public final R right;

   public static <L, R> ImmutablePair<L, R>[] emptyArray() {
      return (ImmutablePair<L, R>[])EMPTY_ARRAY;
   }

   public static <L, R> Pair<L, R> left(L left) {
      return of(left, null);
   }

   public static <L, R> ImmutablePair<L, R> nullPair() {
      return NULL;
   }

   public static <L, R> ImmutablePair<L, R> of(L left, R right) {
      return left == null && right == null ? nullPair() : new ImmutablePair<>(left, right);
   }

   public static <L, R> ImmutablePair<L, R> of(Entry<L, R> pair) {
      return pair != null ? new ImmutablePair<>(pair.getKey(), pair.getValue()) : nullPair();
   }

   public static <L, R> ImmutablePair<L, R> ofNonNull(L left, R right) {
      return of(Objects.requireNonNull(left, "left"), Objects.requireNonNull(right, "right"));
   }

   public static <L, R> Pair<L, R> right(R right) {
      return of(null, right);
   }

   public ImmutablePair(L left, R right) {
      this.left = left;
      this.right = right;
   }

   @Override
   public L getLeft() {
      return this.left;
   }

   @Override
   public R getRight() {
      return this.right;
   }

   @Override
   public R setValue(R value) {
      throw new UnsupportedOperationException();
   }
}
