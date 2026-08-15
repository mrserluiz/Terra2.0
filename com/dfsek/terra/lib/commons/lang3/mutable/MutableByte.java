package com.dfsek.terra.lib.commons.lang3.mutable;

import com.dfsek.terra.lib.commons.lang3.math.NumberUtils;

public class MutableByte extends Number implements Comparable<MutableByte>, Mutable<Number> {
   private static final long serialVersionUID = -1585823265L;
   private byte value;

   public MutableByte() {
   }

   public MutableByte(byte value) {
      this.value = value;
   }

   public MutableByte(Number value) {
      this.value = value.byteValue();
   }

   public MutableByte(String value) {
      this.value = Byte.parseByte(value);
   }

   public void add(byte operand) {
      this.value += operand;
   }

   public void add(Number operand) {
      this.value = (byte)(this.value + operand.byteValue());
   }

   public byte addAndGet(byte operand) {
      this.value += operand;
      return this.value;
   }

   public byte addAndGet(Number operand) {
      this.value = (byte)(this.value + operand.byteValue());
      return this.value;
   }

   @Override
   public byte byteValue() {
      return this.value;
   }

   public int compareTo(MutableByte other) {
      return NumberUtils.compare(this.value, other.value);
   }

   public void decrement() {
      this.value--;
   }

   public byte decrementAndGet() {
      this.value--;
      return this.value;
   }

   @Override
   public double doubleValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof MutableByte ? this.value == ((MutableByte)obj).byteValue() : false;
   }

   @Override
   public float floatValue() {
      return this.value;
   }

   public byte getAndAdd(byte operand) {
      byte last = this.value;
      this.value += operand;
      return last;
   }

   public byte getAndAdd(Number operand) {
      byte last = this.value;
      this.value = (byte)(this.value + operand.byteValue());
      return last;
   }

   public byte getAndDecrement() {
      return this.value--;
   }

   public byte getAndIncrement() {
      return this.value++;
   }

   public Byte getValue() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return this.value;
   }

   public void increment() {
      this.value++;
   }

   public byte incrementAndGet() {
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

   public void setValue(byte value) {
      this.value = value;
   }

   public void setValue(Number value) {
      this.value = value.byteValue();
   }

   public void subtract(byte operand) {
      this.value -= operand;
   }

   public void subtract(Number operand) {
      this.value = (byte)(this.value - operand.byteValue());
   }

   public Byte toByte() {
      return this.byteValue();
   }

   @Override
   public String toString() {
      return String.valueOf(this.value);
   }
}
