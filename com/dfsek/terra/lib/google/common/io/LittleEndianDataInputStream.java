package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.dfsek.terra.lib.google.common.primitives.Longs;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

@J2ktIncompatible
@GwtIncompatible
public final class LittleEndianDataInputStream extends FilterInputStream implements DataInput {
   public LittleEndianDataInputStream(InputStream in) {
      super(Preconditions.checkNotNull(in));
   }

   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public String readLine() {
      throw new UnsupportedOperationException("readLine is not supported");
   }

   @Override
   public void readFully(byte[] b) throws IOException {
      ByteStreams.readFully(this, b);
   }

   @Override
   public void readFully(byte[] b, int off, int len) throws IOException {
      ByteStreams.readFully(this, b, off, len);
   }

   @Override
   public int skipBytes(int n) throws IOException {
      return (int)this.in.skip(n);
   }

   @CanIgnoreReturnValue
   @Override
   public int readUnsignedByte() throws IOException {
      int b1 = this.in.read();
      if (b1 < 0) {
         throw new EOFException();
      } else {
         return b1;
      }
   }

   @CanIgnoreReturnValue
   @Override
   public int readUnsignedShort() throws IOException {
      byte b1 = this.readAndCheckByte();
      byte b2 = this.readAndCheckByte();
      return Ints.fromBytes((byte)0, (byte)0, b2, b1);
   }

   @CanIgnoreReturnValue
   @Override
   public int readInt() throws IOException {
      byte b1 = this.readAndCheckByte();
      byte b2 = this.readAndCheckByte();
      byte b3 = this.readAndCheckByte();
      byte b4 = this.readAndCheckByte();
      return Ints.fromBytes(b4, b3, b2, b1);
   }

   @CanIgnoreReturnValue
   @Override
   public long readLong() throws IOException {
      byte b1 = this.readAndCheckByte();
      byte b2 = this.readAndCheckByte();
      byte b3 = this.readAndCheckByte();
      byte b4 = this.readAndCheckByte();
      byte b5 = this.readAndCheckByte();
      byte b6 = this.readAndCheckByte();
      byte b7 = this.readAndCheckByte();
      byte b8 = this.readAndCheckByte();
      return Longs.fromBytes(b8, b7, b6, b5, b4, b3, b2, b1);
   }

   @CanIgnoreReturnValue
   @Override
   public float readFloat() throws IOException {
      return Float.intBitsToFloat(this.readInt());
   }

   @CanIgnoreReturnValue
   @Override
   public double readDouble() throws IOException {
      return Double.longBitsToDouble(this.readLong());
   }

   @CanIgnoreReturnValue
   @Override
   public String readUTF() throws IOException {
      return new DataInputStream(this.in).readUTF();
   }

   @CanIgnoreReturnValue
   @Override
   public short readShort() throws IOException {
      return (short)this.readUnsignedShort();
   }

   @CanIgnoreReturnValue
   @Override
   public char readChar() throws IOException {
      return (char)this.readUnsignedShort();
   }

   @CanIgnoreReturnValue
   @Override
   public byte readByte() throws IOException {
      return (byte)this.readUnsignedByte();
   }

   @CanIgnoreReturnValue
   @Override
   public boolean readBoolean() throws IOException {
      return this.readUnsignedByte() != 0;
   }

   private byte readAndCheckByte() throws IOException, EOFException {
      int b1 = this.in.read();
      if (b1 == -1) {
         throw new EOFException();
      } else {
         return (byte)b1;
      }
   }
}
