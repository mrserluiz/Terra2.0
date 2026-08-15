package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.io.DataOutput;

@J2ktIncompatible
@GwtIncompatible
public interface ByteArrayDataOutput extends DataOutput {
   @Override
   void write(int b);

   @Override
   void write(byte[] b);

   @Override
   void write(byte[] b, int off, int len);

   @Override
   void writeBoolean(boolean v);

   @Override
   void writeByte(int v);

   @Override
   void writeShort(int v);

   @Override
   void writeChar(int v);

   @Override
   void writeInt(int v);

   @Override
   void writeLong(long v);

   @Override
   void writeFloat(float v);

   @Override
   void writeDouble(double v);

   @Override
   void writeChars(String s);

   @Override
   void writeUTF(String s);

   @Deprecated
   @Override
   void writeBytes(String s);

   byte[] toByteArray();
}
