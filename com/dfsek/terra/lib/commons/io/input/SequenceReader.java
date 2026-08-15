package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class SequenceReader extends Reader {
   private Reader reader;
   private final Iterator<? extends Reader> readers;

   public SequenceReader(Iterable<? extends Reader> readers) {
      this.readers = Objects.requireNonNull(readers, "readers").iterator();
      this.reader = Uncheck.get(this::nextReader);
   }

   public SequenceReader(Reader... readers) {
      this(Arrays.asList(readers));
   }

   @Override
   public void close() throws IOException {
      while (this.nextReader() != null) {
      }
   }

   private Reader nextReader() throws IOException {
      if (this.reader != null) {
         this.reader.close();
      }

      if (this.readers.hasNext()) {
         this.reader = this.readers.next();
      } else {
         this.reader = null;
      }

      return this.reader;
   }

   @Override
   public int read() throws IOException {
      int c = -1;

      while (this.reader != null) {
         c = this.reader.read();
         if (c != -1) {
            break;
         }

         this.nextReader();
      }

      return c;
   }

   @Override
   public int read(char[] cbuf, int off, int len) throws IOException {
      Objects.requireNonNull(cbuf, "cbuf");
      if (len >= 0 && off >= 0 && off + len <= cbuf.length) {
         int count = 0;

         while (this.reader != null) {
            int readLen = this.reader.read(cbuf, off, len);
            if (readLen == -1) {
               this.nextReader();
            } else {
               count += readLen;
               off += readLen;
               len -= readLen;
               if (len <= 0) {
                  break;
               }
            }
         }

         return count > 0 ? count : -1;
      } else {
         throw new IndexOutOfBoundsException("Array Size=" + cbuf.length + ", offset=" + off + ", length=" + len);
      }
   }
}
