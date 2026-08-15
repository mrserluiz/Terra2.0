package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.Charsets;
import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.charset.CharsetEncoders;
import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;

public class CharSequenceInputStream extends InputStream {
   private static final int NO_MARK = -1;
   private final ByteBuffer bBuf;
   private int bBufMark;
   private final CharBuffer cBuf;
   private int cBufMark;
   private final CharsetEncoder charsetEncoder;

   public static CharSequenceInputStream.Builder builder() {
      return new CharSequenceInputStream.Builder();
   }

   private static CharsetEncoder newEncoder(Charset charset) {
      return Charsets.toCharset(charset).newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
   }

   @Deprecated
   public CharSequenceInputStream(CharSequence cs, Charset charset) {
      this(cs, charset, 8192);
   }

   @Deprecated
   public CharSequenceInputStream(CharSequence cs, Charset charset, int bufferSize) {
      this(cs, bufferSize, newEncoder(charset));
   }

   private CharSequenceInputStream(CharSequence cs, int bufferSize, CharsetEncoder charsetEncoder) {
      this.charsetEncoder = charsetEncoder;
      this.bBuf = ByteBuffer.allocate(ReaderInputStream.checkMinBufferSize(charsetEncoder, bufferSize));
      ((Buffer)this.bBuf).flip();
      this.cBuf = CharBuffer.wrap(cs);
      this.cBufMark = -1;
      this.bBufMark = -1;

      try {
         this.fillBuffer();
      } catch (CharacterCodingException ex) {
         ((Buffer)this.bBuf).clear();
         ((Buffer)this.bBuf).flip();
         ((Buffer)this.cBuf).rewind();
      }
   }

   @Deprecated
   public CharSequenceInputStream(CharSequence cs, String charset) {
      this(cs, charset, 8192);
   }

   @Deprecated
   public CharSequenceInputStream(CharSequence cs, String charset, int bufferSize) {
      this(cs, Charsets.toCharset(charset), bufferSize);
   }

   @Override
   public int available() throws IOException {
      return this.bBuf.remaining();
   }

   @Override
   public void close() throws IOException {
      ((Buffer)this.bBuf).position(this.bBuf.limit());
   }

   private void fillBuffer() throws CharacterCodingException {
      this.bBuf.compact();
      CoderResult result = this.charsetEncoder.encode(this.cBuf, this.bBuf, true);
      if (result.isError()) {
         result.throwException();
      }

      ((Buffer)this.bBuf).flip();
   }

   CharsetEncoder getCharsetEncoder() {
      return this.charsetEncoder;
   }

   @Override
   public synchronized void mark(int readLimit) {
      this.cBufMark = this.cBuf.position();
      this.bBufMark = this.bBuf.position();
      ((Buffer)this.cBuf).mark();
      ((Buffer)this.bBuf).mark();
   }

   @Override
   public boolean markSupported() {
      return true;
   }

   @Override
   public int read() throws IOException {
      while (!this.bBuf.hasRemaining()) {
         this.fillBuffer();
         if (!this.bBuf.hasRemaining() && !this.cBuf.hasRemaining()) {
            return -1;
         }
      }

      return this.bBuf.get() & 0xFF;
   }

   @Override
   public int read(byte[] b) throws IOException {
      return this.read(b, 0, b.length);
   }

   @Override
   public int read(byte[] array, int off, int len) throws IOException {
      Objects.requireNonNull(array, "array");
      if (len < 0 || off + len > array.length) {
         throw new IndexOutOfBoundsException("Array Size=" + array.length + ", offset=" + off + ", length=" + len);
      }

      if (len == 0) {
         return 0;
      }

      if (!this.bBuf.hasRemaining() && !this.cBuf.hasRemaining()) {
         return -1;
      }

      int bytesRead = 0;

      while (len > 0) {
         if (this.bBuf.hasRemaining()) {
            int chunk = Math.min(this.bBuf.remaining(), len);
            this.bBuf.get(array, off, chunk);
            off += chunk;
            len -= chunk;
            bytesRead += chunk;
         } else {
            this.fillBuffer();
            if (!this.bBuf.hasRemaining() && !this.cBuf.hasRemaining()) {
               break;
            }
         }
      }

      return bytesRead == 0 && !this.cBuf.hasRemaining() ? -1 : bytesRead;
   }

   @Override
   public synchronized void reset() throws IOException {
      if (this.cBufMark != -1) {
         if (this.cBuf.position() != 0) {
            this.charsetEncoder.reset();
            ((Buffer)this.cBuf).rewind();
            ((Buffer)this.bBuf).rewind();
            ((Buffer)this.bBuf).limit(0);

            while (this.cBuf.position() < this.cBufMark) {
               ((Buffer)this.bBuf).rewind();
               ((Buffer)this.bBuf).limit(0);
               this.fillBuffer();
            }
         }

         if (this.cBuf.position() != this.cBufMark) {
            throw new IllegalStateException("Unexpected CharBuffer position: actual=" + this.cBuf.position() + " expected=" + this.cBufMark);
         }

         ((Buffer)this.bBuf).position(this.bBufMark);
         this.cBufMark = -1;
         this.bBufMark = -1;
      }

      this.mark(0);
   }

   @Override
   public long skip(long n) throws IOException {
      long skipped;
      for (skipped = 0L; n > 0L && this.available() > 0; skipped++) {
         this.read();
         n--;
      }

      return skipped;
   }

   public static class Builder extends AbstractStreamBuilder<CharSequenceInputStream, CharSequenceInputStream.Builder> {
      private CharsetEncoder charsetEncoder = CharSequenceInputStream.newEncoder(this.getCharset());

      public CharSequenceInputStream get() {
         return Uncheck.get(() -> new CharSequenceInputStream(this.getCharSequence(), this.getBufferSize(), this.charsetEncoder));
      }

      CharsetEncoder getCharsetEncoder() {
         return this.charsetEncoder;
      }

      public CharSequenceInputStream.Builder setCharset(Charset charset) {
         super.setCharset(charset);
         this.charsetEncoder = CharSequenceInputStream.newEncoder(this.getCharset());
         return this;
      }

      public CharSequenceInputStream.Builder setCharsetEncoder(CharsetEncoder newEncoder) {
         this.charsetEncoder = CharsetEncoders.toCharsetEncoder(newEncoder, () -> CharSequenceInputStream.newEncoder(this.getCharsetDefault()));
         super.setCharset(this.charsetEncoder.charset());
         return this;
      }
   }
}
