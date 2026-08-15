package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.Charsets;
import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.charset.CharsetEncoders;
import java.io.IOException;
import java.io.Reader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;

public class ReaderInputStream extends AbstractInputStream {
   private final Reader reader;
   private final CharsetEncoder charsetEncoder;
   private final CharBuffer encoderIn;
   private final ByteBuffer encoderOut;
   private CoderResult lastCoderResult;
   private boolean endOfInput;

   public static ReaderInputStream.Builder builder() {
      return new ReaderInputStream.Builder();
   }

   static int checkMinBufferSize(CharsetEncoder charsetEncoder, int bufferSize) {
      float minRequired = minBufferSize(charsetEncoder);
      if (bufferSize < minRequired) {
         throw new IllegalArgumentException(
            String.format("Buffer size %,d must be at least %s for a CharsetEncoder %s.", bufferSize, minRequired, charsetEncoder.charset().displayName())
         );
      } else {
         return bufferSize;
      }
   }

   static float minBufferSize(CharsetEncoder charsetEncoder) {
      return charsetEncoder.maxBytesPerChar() * 2.0F;
   }

   private static CharsetEncoder newEncoder(Charset charset) {
      return Charsets.toCharset(charset).newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
   }

   @Deprecated
   public ReaderInputStream(Reader reader) {
      this(reader, Charset.defaultCharset());
   }

   @Deprecated
   public ReaderInputStream(Reader reader, Charset charset) {
      this(reader, charset, 8192);
   }

   @Deprecated
   public ReaderInputStream(Reader reader, Charset charset, int bufferSize) {
      this(
         reader,
         Charsets.toCharset(charset).newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE),
         bufferSize
      );
   }

   @Deprecated
   public ReaderInputStream(Reader reader, CharsetEncoder charsetEncoder) {
      this(reader, charsetEncoder, 8192);
   }

   @Deprecated
   public ReaderInputStream(Reader reader, CharsetEncoder charsetEncoder, int bufferSize) {
      this.reader = reader;
      this.charsetEncoder = CharsetEncoders.toCharsetEncoder(charsetEncoder);
      this.encoderIn = CharBuffer.allocate(checkMinBufferSize(this.charsetEncoder, bufferSize));
      ((Buffer)this.encoderIn).flip();
      this.encoderOut = ByteBuffer.allocate(128);
      ((Buffer)this.encoderOut).flip();
   }

   @Deprecated
   public ReaderInputStream(Reader reader, String charsetName) {
      this(reader, charsetName, 8192);
   }

   @Deprecated
   public ReaderInputStream(Reader reader, String charsetName, int bufferSize) {
      this(reader, Charsets.toCharset(charsetName), bufferSize);
   }

   @Override
   public int available() throws IOException {
      return this.encoderOut.hasRemaining() ? this.encoderOut.remaining() : 0;
   }

   @Override
   public void close() throws IOException {
      this.reader.close();
      super.close();
   }

   private void fillBuffer() throws IOException {
      if (!this.endOfInput) {
         if (!this.endOfInput && (this.lastCoderResult == null || this.lastCoderResult.isUnderflow())) {
            this.encoderIn.compact();
            int position = this.encoderIn.position();
            int c = this.reader.read(this.encoderIn.array(), position, this.encoderIn.remaining());
            if (c == -1) {
               this.endOfInput = true;
            } else {
               ((Buffer)this.encoderIn).position(position + c);
            }

            ((Buffer)this.encoderIn).flip();
         }

         this.encoderOut.compact();
         this.lastCoderResult = this.charsetEncoder.encode(this.encoderIn, this.encoderOut, this.endOfInput);
         if (this.endOfInput) {
            this.lastCoderResult = this.charsetEncoder.flush(this.encoderOut);
         }

         if (this.lastCoderResult.isError()) {
            this.lastCoderResult.throwException();
         }

         ((Buffer)this.encoderOut).flip();
      }
   }

   CharsetEncoder getCharsetEncoder() {
      return this.charsetEncoder;
   }

   @Override
   public int read() throws IOException {
      this.checkOpen();

      while (!this.encoderOut.hasRemaining()) {
         this.fillBuffer();
         if (this.endOfInput && !this.encoderOut.hasRemaining()) {
            return -1;
         }
      }

      return this.encoderOut.get() & 0xFF;
   }

   @Override
   public int read(byte[] b) throws IOException {
      return this.read(b, 0, b.length);
   }

   @Override
   public int read(byte[] array, int off, int len) throws IOException {
      Objects.requireNonNull(array, "array");
      if (len >= 0 && off >= 0 && off + len <= array.length) {
         int read = 0;
         if (len == 0) {
            return 0;
         }

         while (len > 0) {
            if (this.encoderOut.hasRemaining()) {
               int c = Math.min(this.encoderOut.remaining(), len);
               this.encoderOut.get(array, off, c);
               off += c;
               len -= c;
               read += c;
            } else {
               if (this.endOfInput) {
                  break;
               }

               this.fillBuffer();
            }
         }

         return read == 0 && this.endOfInput ? -1 : read;
      } else {
         throw new IndexOutOfBoundsException("Array size=" + array.length + ", offset=" + off + ", length=" + len);
      }
   }

   public static class Builder extends AbstractStreamBuilder<ReaderInputStream, ReaderInputStream.Builder> {
      private CharsetEncoder charsetEncoder = ReaderInputStream.newEncoder(this.getCharset());

      public ReaderInputStream get() throws IOException {
         return new ReaderInputStream(this.getReader(), this.charsetEncoder, this.getBufferSize());
      }

      CharsetEncoder getCharsetEncoder() {
         return this.charsetEncoder;
      }

      public ReaderInputStream.Builder setCharset(Charset charset) {
         super.setCharset(charset);
         this.charsetEncoder = ReaderInputStream.newEncoder(this.getCharset());
         return this;
      }

      public ReaderInputStream.Builder setCharsetEncoder(CharsetEncoder newEncoder) {
         this.charsetEncoder = CharsetEncoders.toCharsetEncoder(newEncoder, () -> ReaderInputStream.newEncoder(this.getCharsetDefault()));
         super.setCharset(this.charsetEncoder.charset());
         return this;
      }
   }
}
