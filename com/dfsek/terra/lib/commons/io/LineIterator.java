package com.dfsek.terra.lib.commons.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class LineIterator implements Iterator<String>, Closeable {
   private final BufferedReader bufferedReader;
   private String cachedLine;
   private boolean finished;

   @Deprecated
   public static void closeQuietly(LineIterator iterator) {
      IOUtils.closeQuietly(iterator);
   }

   public LineIterator(Reader reader) {
      Objects.requireNonNull(reader, "reader");
      if (reader instanceof BufferedReader) {
         this.bufferedReader = (BufferedReader)reader;
      } else {
         this.bufferedReader = new BufferedReader(reader);
      }
   }

   @Override
   public void close() throws IOException {
      this.finished = true;
      this.cachedLine = null;
      IOUtils.close(this.bufferedReader);
   }

   @Override
   public boolean hasNext() {
      if (this.cachedLine != null) {
         return true;
      }

      if (this.finished) {
         return false;
      }

      try {
         String line;
         do {
            line = this.bufferedReader.readLine();
            if (line == null) {
               this.finished = true;
               return false;
            }
         } while (!this.isValidLine(line));

         this.cachedLine = line;
         return true;
      } catch (IOException ioe) {
         IOUtils.closeQuietly(this, ioe::addSuppressed);
         throw new IllegalStateException(ioe);
      }
   }

   protected boolean isValidLine(String line) {
      return true;
   }

   public String next() {
      return this.nextLine();
   }

   @Deprecated
   public String nextLine() {
      if (!this.hasNext()) {
         throw new NoSuchElementException("No more lines");
      }

      String currentLine = this.cachedLine;
      this.cachedLine = null;
      return currentLine;
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("remove not supported");
   }
}
