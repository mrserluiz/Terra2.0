package com.dfsek.terra.lib.google.common.hash;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

abstract class AbstractHasher implements Hasher {
   @CanIgnoreReturnValue
   @Override
   public final Hasher putBoolean(boolean b) {
      return this.putByte((byte)(b ? 1 : 0));
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putDouble(double d) {
      return this.putLong(Double.doubleToRawLongBits(d));
   }

   @CanIgnoreReturnValue
   @Override
   public final Hasher putFloat(float f) {
      return this.putInt(Float.floatToRawIntBits(f));
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putUnencodedChars(CharSequence charSequence) {
      int i = 0;

      for (int len = charSequence.length(); i < len; i++) {
         this.putChar(charSequence.charAt(i));
      }

      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putString(CharSequence charSequence, Charset charset) {
      return this.putBytes(charSequence.toString().getBytes(charset));
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putBytes(byte[] bytes) {
      return this.putBytes(bytes, 0, bytes.length);
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putBytes(byte[] bytes, int off, int len) {
      Preconditions.checkPositionIndexes(off, off + len, bytes.length);

      for (int i = 0; i < len; i++) {
         this.putByte(bytes[off + i]);
      }

      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putBytes(ByteBuffer b) {
      if (b.hasArray()) {
         this.putBytes(b.array(), b.arrayOffset() + b.position(), b.remaining());
         Java8Compatibility.position(b, b.limit());
      } else {
         for (int remaining = b.remaining(); remaining > 0; remaining--) {
            this.putByte(b.get());
         }
      }

      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putShort(short s) {
      this.putByte((byte)s);
      this.putByte((byte)(s >>> 8));
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putInt(int i) {
      this.putByte((byte)i);
      this.putByte((byte)(i >>> 8));
      this.putByte((byte)(i >>> 16));
      this.putByte((byte)(i >>> 24));
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putLong(long l) {
      for (int i = 0; i < 64; i += 8) {
         this.putByte((byte)(l >>> i));
      }

      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public Hasher putChar(char c) {
      this.putByte((byte)c);
      this.putByte((byte)(c >>> '\b'));
      return this;
   }

   @CanIgnoreReturnValue
   @Override
   public <T> Hasher putObject(@ParametricNullness T instance, Funnel<? super T> funnel) {
      funnel.funnel(instance, this);
      return this;
   }
}
