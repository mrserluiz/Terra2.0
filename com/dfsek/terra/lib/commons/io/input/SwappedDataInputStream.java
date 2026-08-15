package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.EndianUtils;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class SwappedDataInputStream extends ProxyInputStream implements DataInput {
   public SwappedDataInputStream(InputStream input) {
      super(input);
   }

   @Override
   public boolean readBoolean() throws IOException, EOFException {
      return 0 != this.readByte();
   }

   @Override
   public byte readByte() throws IOException, EOFException {
      return (byte)this.in.read();
   }

   @Override
   public char readChar() throws IOException, EOFException {
      return (char)this.readShort();
   }

   @Override
   public double readDouble() throws IOException, EOFException {
      return EndianUtils.readSwappedDouble(this.in);
   }

   @Override
   public float readFloat() throws IOException, EOFException {
      return EndianUtils.readSwappedFloat(this.in);
   }

   @Override
   public void readFully(byte[] data) throws IOException, EOFException {
      this.readFully(data, 0, data.length);
   }

   @Override
   public void readFully(byte[] data, int offset, int length) throws IOException, EOFException {
      int remaining = length;

      while (remaining > 0) {
         int location = offset + length - remaining;
         int count = this.read(data, location, remaining);
         if (-1 == count) {
            throw new EOFException();
         }

         remaining -= count;
      }
   }

   @Override
   public int readInt() throws IOException, EOFException {
      return EndianUtils.readSwappedInteger(this.in);
   }

   @Override
   public String readLine() throws IOException, EOFException {
      throw UnsupportedOperationExceptions.method("readLine");
   }

   @Override
   public long readLong() throws IOException, EOFException {
      return EndianUtils.readSwappedLong(this.in);
   }

   @Override
   public short readShort() throws IOException, EOFException {
      return EndianUtils.readSwappedShort(this.in);
   }

   @Override
   public int readUnsignedByte() throws IOException, EOFException {
      return this.in.read();
   }

   @Override
   public int readUnsignedShort() throws IOException, EOFException {
      return EndianUtils.readSwappedUnsignedShort(this.in);
   }

   @Override
   public String readUTF() throws IOException, EOFException {
      throw UnsupportedOperationExceptions.method("readUTF");
   }

   @Override
   public int skipBytes(int count) throws IOException {
      return (int)this.in.skip(count);
   }
}
