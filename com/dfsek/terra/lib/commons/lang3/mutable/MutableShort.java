package com.dfsek.terra.lib.commons.lang3.mutable;

import com.dfsek.terra.lib.commons.lang3.math.NumberUtils;

public class MutableShort extends Number implements Comparable<MutableShort>, Mutable<Number> {
   private static final long serialVersionUID = -2135791679L;
   private short value;

   public MutableShort() {
   }

   public MutableShort(Number value) {
      this.value = value.shortValue();
   }

   public MutableShort(short value) {
      this.value = value;
   }

   public MutableShort(String value) {
      this.value = Short.parseShort(value);
   }

   public void add(Number operand) {
      this.value = (short)(this.value + operand.shortValue());
   }

   public void add(short operand) {
      this.value += operand;
   }

   public short addAndGet(Number operand) {
      this.value = (short)(this.value + operand.shortValue());
      return this.value;
   }

   public short addAndGet(short operand) {
      this.value += operand;
      return this.value;
   }

   public int compareTo(MutableShort other) {
      return NumberUtils.compare(this.value, other.value);
   }

   public void decrement() {
      this.value--;
   }

   public short decrementAndGet() {
      this.value--;
      return this.value;
   }

   @Override
   public double doubleValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof MutableShort ? this.value == ((MutableShort)obj).shortValue() : false;
   }

   @Override
   public float floatValue() {
      return this.value;
   }

   public short getAndAdd(Number operand) {
      short last = this.value;
      this.value = (short)(this.value + operand.shortValue());
      return last;
   }

   public short getAndAdd(short operand) {
      short last = this.value;
      this.value += operand;
      return last;
   }

   public short getAndDecrement() {
      return this.value--;
   }

   public short getAndIncrement() {
      return this.value++;
   }

   public Short getValue() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return this.value;
   }

   public void increment() {
      this.value++;
   }

   public short incrementAndGet() {
      this.value++;
      return this.value;
   }

   @Override
   public int intValue() {
      return this.value;
   }

   @Override
   public long longValue() {
      return this.value;
   }

   public void setValue(Number value) {
      this.value = value.shortValue();
   }

   public void setValue(short value) {
      this.value = value;
   }

   @Override
   public short shortValue() {
      return this.value;
   }

   public void subtract(Number operand) {
      this.value = (short)(this.value - operand.shortValue());
   }

   public void subtract(short operand) {
      this.value -= operand;
   }

   public Short toShort() {
      return this.shortValue();
   }

   @Override
   public String toString() {
      return String.valueOf(this.value);
   }
}
