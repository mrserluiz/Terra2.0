package com.dfsek.terra.lib.commons.lang3.util;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Objects;
import java.util.stream.IntStream;

public final class FluentBitSet implements Cloneable, Serializable {
   private static final long serialVersionUID = 1L;
   private final BitSet bitSet;

   public FluentBitSet() {
      this(new BitSet());
   }

   public FluentBitSet(BitSet set) {
      this.bitSet = Objects.requireNonNull(set, "set");
   }

   public FluentBitSet(int nbits) {
      this(new BitSet(nbits));
   }

   public FluentBitSet and(BitSet set) {
      this.bitSet.and(set);
      return this;
   }

   public FluentBitSet and(FluentBitSet set) {
      this.bitSet.and(set.bitSet);
      return this;
   }

   public FluentBitSet andNot(BitSet set) {
      this.bitSet.andNot(set);
      return this;
   }

   public FluentBitSet andNot(FluentBitSet set) {
      this.bitSet.andNot(set.bitSet);
      return this;
   }

   public BitSet bitSet() {
      return this.bitSet;
   }

   public int cardinality() {
      return this.bitSet.cardinality();
   }

   public FluentBitSet clear() {
      this.bitSet.clear();
      return this;
   }

   public FluentBitSet clear(int... bitIndexArray) {
      for (int e : bitIndexArray) {
         this.bitSet.clear(e);
      }

      return this;
   }

   public FluentBitSet clear(int bitIndex) {
      this.bitSet.clear(bitIndex);
      return this;
   }

   public FluentBitSet clear(int fromIndex, int toIndex) {
      this.bitSet.clear(fromIndex, toIndex);
      return this;
   }

   @Override
   public Object clone() {
      return new FluentBitSet((BitSet)this.bitSet.clone());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof FluentBitSet)) {
         return false;
      }

      FluentBitSet other = (FluentBitSet)obj;
      return Objects.equals(this.bitSet, other.bitSet);
   }

   public FluentBitSet flip(int bitIndex) {
      this.bitSet.flip(bitIndex);
      return this;
   }

   public FluentBitSet flip(int fromIndex, int toIndex) {
      this.bitSet.flip(fromIndex, toIndex);
      return this;
   }

   public boolean get(int bitIndex) {
      return this.bitSet.get(bitIndex);
   }

   public FluentBitSet get(int fromIndex, int toIndex) {
      return new FluentBitSet(this.bitSet.get(fromIndex, toIndex));
   }

   @Override
   public int hashCode() {
      return this.bitSet.hashCode();
   }

   public boolean intersects(BitSet set) {
      return this.bitSet.intersects(set);
   }

   public boolean intersects(FluentBitSet set) {
      return this.bitSet.intersects(set.bitSet);
   }

   public boolean isEmpty() {
      return this.bitSet.isEmpty();
   }

   public int length() {
      return this.bitSet.length();
   }

   public int nextClearBit(int fromIndex) {
      return this.bitSet.nextClearBit(fromIndex);
   }

   public int nextSetBit(int fromIndex) {
      return this.bitSet.nextSetBit(fromIndex);
   }

   public FluentBitSet or(BitSet set) {
      this.bitSet.or(set);
      return this;
   }

   public FluentBitSet or(FluentBitSet... set) {
      for (FluentBitSet e : set) {
         this.bitSet.or(e.bitSet);
      }

      return this;
   }

   public FluentBitSet or(FluentBitSet set) {
      this.bitSet.or(set.bitSet);
      return this;
   }

   public int previousClearBit(int fromIndex) {
      return this.bitSet.previousClearBit(fromIndex);
   }

   public int previousSetBit(int fromIndex) {
      return this.bitSet.previousSetBit(fromIndex);
   }

   public FluentBitSet set(int... bitIndexArray) {
      for (int e : bitIndexArray) {
         this.bitSet.set(e);
      }

      return this;
   }

   public FluentBitSet set(int bitIndex) {
      this.bitSet.set(bitIndex);
      return this;
   }

   public FluentBitSet set(int bitIndex, boolean value) {
      this.bitSet.set(bitIndex, value);
      return this;
   }

   public FluentBitSet set(int fromIndex, int toIndex) {
      this.bitSet.set(fromIndex, toIndex);
      return this;
   }

   public FluentBitSet set(int fromIndex, int toIndex, boolean value) {
      this.bitSet.set(fromIndex, toIndex, value);
      return this;
   }

   public FluentBitSet setInclusive(int fromIndex, int toIndex) {
      this.bitSet.set(fromIndex, toIndex + 1);
      return this;
   }

   public int size() {
      return this.bitSet.size();
   }

   public IntStream stream() {
      return this.bitSet.stream();
   }

   public byte[] toByteArray() {
      return this.bitSet.toByteArray();
   }

   public long[] toLongArray() {
      return this.bitSet.toLongArray();
   }

   @Override
   public String toString() {
      return this.bitSet.toString();
   }

   public FluentBitSet xor(BitSet set) {
      this.bitSet.xor(set);
      return this;
   }

   public FluentBitSet xor(FluentBitSet set) {
      this.bitSet.xor(set.bitSet);
      return this;
   }
}
