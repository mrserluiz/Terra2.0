package com.dfsek.terra.lib.commons.lang3;

public class BitField {
   private final int mask;
   private final int shiftCount;

   public BitField(int mask) {
      this.mask = mask;
      this.shiftCount = mask == 0 ? 0 : Integer.numberOfTrailingZeros(mask);
   }

   public int clear(int holder) {
      return holder & ~this.mask;
   }

   public byte clearByte(byte holder) {
      return (byte)this.clear(holder);
   }

   public short clearShort(short holder) {
      return (short)this.clear(holder);
   }

   public int getRawValue(int holder) {
      return holder & this.mask;
   }

   public short getShortRawValue(short holder) {
      return (short)this.getRawValue(holder);
   }

   public short getShortValue(short holder) {
      return (short)this.getValue(holder);
   }

   public int getValue(int holder) {
      return this.getRawValue(holder) >> this.shiftCount;
   }

   public boolean isAllSet(int holder) {
      return (holder & this.mask) == this.mask;
   }

   public boolean isSet(int holder) {
      return (holder & this.mask) != 0;
   }

   public int set(int holder) {
      return holder | this.mask;
   }

   public int setBoolean(int holder, boolean flag) {
      return flag ? this.set(holder) : this.clear(holder);
   }

   public byte setByte(byte holder) {
      return (byte)this.set(holder);
   }

   public byte setByteBoolean(byte holder, boolean flag) {
      return flag ? this.setByte(holder) : this.clearByte(holder);
   }

   public short setShort(short holder) {
      return (short)this.set(holder);
   }

   public short setShortBoolean(short holder, boolean flag) {
      return flag ? this.setShort(holder) : this.clearShort(holder);
   }

   public short setShortValue(short holder, short value) {
      return (short)this.setValue(holder, value);
   }

   public int setValue(int holder, int value) {
      return holder & ~this.mask | value << this.shiftCount & this.mask;
   }
}
