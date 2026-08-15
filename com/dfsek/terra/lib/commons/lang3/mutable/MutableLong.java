package com.dfsek.terra.lib.commons.lang3.mutable;

import com.dfsek.terra.lib.commons.lang3.math.NumberUtils;

public class MutableLong extends Number implements Comparable<MutableLong>, Mutable<Number> {
   private static final long serialVersionUID = 62986528375L;
   private long value;

   public MutableLong() {
   }

   public MutableLong(long value) {
      this.value = value;
   }

   public MutableLong(Number value) {
      this.value = value.longValue();
   }

   public MutableLong(String value) {
      this.value = Long.parseLong(value);
   }

   public void add(long operand) {
      this.value += operand;
   }

   public void add(Number operand) {
      this.value = this.value + operand.longValue();
   }

   public long addAndGet(long operand) {
      this.value += operand;
      return this.value;
   }

   public long addAndGet(Number operand) {
      this.value = this.value + operand.longValue();
      return this.value;
   }

   public int compareTo(MutableLong other) {
      return NumberUtils.compare(this.value, other.value);
   }

   public void decrement() {
      this.value--;
   }

   public long decrementAndGet() {
      this.value--;
      return this.value;
   }

   @Override
   public double doubleValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof MutableLong ? this.value == ((MutableLong)obj).longValue() : false;
   }

   @Override
   public float floatValue() {
      return (float)this.value;
   }

   public long getAndAdd(long operand) {
      long last = this.value;
      this.value += operand;
      return last;
   }

   public long getAndAdd(Number operand) {
      long last = this.value;
      this.value = this.value + operand.longValue();
      return last;
   }

   public long getAndDecrement() {
      return this.value--;
   }

   public long getAndIncrement() {
      return this.value++;
   }

   public Long getValue() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return (int)(this.value ^ this.value >>> 32);
   }

   public void increment() {
      this.value++;
   }

   public long incrementAndGet() {
      this.value++;
      return this.value;
   }

   @Override
   public int intValue() {
      return (int)this.value;
   }

   @Override
   public long longValue() {
      return this.value;
   }

   public void setValue(long value) {
      this.value = value;
   }

   public void setValue(Number value) {
      this.value = value.longValue();
   }

   public void subtract(long operand) {
      this.value -= operand;
   }

   public void subtract(Number operand) {
      this.value = this.value - operand.longValue();
   }

   public Long toLong() {
      return this.longValue();
   }

   @Override
   public String toString() {
      return String.valueOf(this.value);
   }
}
