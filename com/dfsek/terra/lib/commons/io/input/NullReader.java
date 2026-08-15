package com.dfsek.terra.lib.commons.io.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

public class NullReader extends Reader {
   public static final NullReader INSTANCE = new NullReader();
   private final long size;
   private long position;
   private long mark = -1L;
   private long readLimit;
   private boolean eof;
   private final boolean throwEofException;
   private final boolean markSupported;

   public NullReader() {
      this(0L, true, false);
   }

   public NullReader(long size) {
      this(size, true, false);
   }

   public NullReader(long size, boolean markSupported, boolean throwEofException) {
      this.size = size;
      this.markSupported = markSupported;
      this.throwEofException = throwEofException;
   }

   @Override
   public void close() throws IOException {
      this.eof = false;
      this.position = 0L;
      this.mark = -1L;
   }

   private int doEndOfFile() throws EOFException {
      this.eof = true;
      if (this.throwEofException) {
         throw new EOFException();
      } else {
         return -1;
      }
   }

   public long getPosition() {
      return this.position;
   }

   public long getSize() {
      return this.size;
   }

   @Override
   public synchronized void mark(int readLimit) {
      if (!this.markSupported) {
         throw UnsupportedOperationExceptions.mark();
      }

      this.mark = this.position;
      this.readLimit = readLimit;
   }

   @Override
   public boolean markSupported() {
      return this.markSupported;
   }

   protected int processChar() {
      return 0;
   }

   protected void processChars(char[] chars, int offset, int length) {
   }

   @Override
   public int read() throws IOException {
      if (this.eof) {
         throw new IOException("Read after end of file");
      }

      if (this.position == this.size) {
         return this.doEndOfFile();
      }

      this.position++;
      return this.processChar();
   }

   @Override
   public int read(char[] chars) throws IOException {
      return this.read(chars, 0, chars.length);
   }

   @Override
   public int read(char[] chars, int offset, int length) throws IOException {
      if (this.eof) {
         throw new IOException("Read after end of file");
      }

      if (this.position == this.size) {
         return this.doEndOfFile();
      }

      this.position += length;
      int returnLength = length;
      if (this.position > this.size) {
         returnLength = length - (int)(this.position - this.size);
         this.position = this.size;
      }

      this.processChars(chars, offset, returnLength);
      return returnLength;
   }

   @Override
   public synchronized void reset() throws IOException {
      if (!this.markSupported) {
         throw UnsupportedOperationExceptions.reset();
      }

      if (this.mark < 0L) {
         throw new IOException("No position has been marked");
      }

      if (this.position > this.mark + this.readLimit) {
         throw new IOException("Marked position [" + this.mark + "] is no longer valid - passed the read limit [" + this.readLimit + "]");
      }

      this.position = this.mark;
      this.eof = false;
   }

   @Override
   public long skip(long numberOfChars) throws IOException {
      if (this.eof) {
         throw new IOException("Skip after end of file");
      }

      if (this.position == this.size) {
         return this.doEndOfFile();
      }

      this.position += numberOfChars;
      long returnLength = numberOfChars;
      if (this.position > this.size) {
         returnLength = numberOfChars - (this.position - this.size);
         this.position = this.size;
      }

      return returnLength;
   }
}
