package com.dfsek.terra.lib.google.common.hash;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Beta
public interface PrimitiveSink {
   @CanIgnoreReturnValue
   PrimitiveSink putByte(byte b);

   @CanIgnoreReturnValue
   PrimitiveSink putBytes(byte[] bytes);

   @CanIgnoreReturnValue
   PrimitiveSink putBytes(byte[] bytes, int off, int len);

   @CanIgnoreReturnValue
   PrimitiveSink putBytes(ByteBuffer bytes);

   @CanIgnoreReturnValue
   PrimitiveSink putShort(short s);

   @CanIgnoreReturnValue
   PrimitiveSink putInt(int i);

   @CanIgnoreReturnValue
   PrimitiveSink putLong(long l);

   @CanIgnoreReturnValue
   PrimitiveSink putFloat(float f);

   @CanIgnoreReturnValue
   PrimitiveSink putDouble(double d);

   @CanIgnoreReturnValue
   PrimitiveSink putBoolean(boolean b);

   @CanIgnoreReturnValue
   PrimitiveSink putChar(char c);

   @CanIgnoreReturnValue
   PrimitiveSink putUnencodedChars(CharSequence charSequence);

   @CanIgnoreReturnValue
   PrimitiveSink putString(CharSequence charSequence, Charset charset);
}
