package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class CharStreams {
   private static final int DEFAULT_BUF_SIZE = 2048;

   static CharBuffer createBuffer() {
      return CharBuffer.allocate(2048);
   }

   private CharStreams() {
   }

   @CanIgnoreReturnValue
   public static long copy(Readable from, Appendable to) throws IOException {
      if (from instanceof Reader) {
         return to instanceof StringBuilder ? copyReaderToBuilder((Reader)from, (StringBuilder)to) : copyReaderToWriter((Reader)from, asWriter(to));
      }

      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(to);
      long total = 0L;
      CharBuffer buf = createBuffer();

      while (from.read(buf) != -1) {
         Java8Compatibility.flip(buf);
         to.append(buf);
         total += buf.remaining();
         Java8Compatibility.clear(buf);
      }

      return total;
   }

   @CanIgnoreReturnValue
   static long copyReaderToBuilder(Reader from, StringBuilder to) throws IOException {
      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(to);
      char[] buf = new char[2048];
      long total = 0L;

      int nRead;
      while ((nRead = from.read(buf)) != -1) {
         to.append(buf, 0, nRead);
         total += nRead;
      }

      return total;
   }

   @CanIgnoreReturnValue
   static long copyReaderToWriter(Reader from, Writer to) throws IOException {
      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(to);
      char[] buf = new char[2048];
      long total = 0L;

      int nRead;
      while ((nRead = from.read(buf)) != -1) {
         to.write(buf, 0, nRead);
         total += nRead;
      }

      return total;
   }

   public static String toString(Readable r) throws IOException {
      return toStringBuilder(r).toString();
   }

   private static StringBuilder toStringBuilder(Readable r) throws IOException {
      StringBuilder sb = new StringBuilder();
      if (r instanceof Reader) {
         copyReaderToBuilder((Reader)r, sb);
      } else {
         copy(r, sb);
      }

      return sb;
   }

   public static List<String> readLines(Readable r) throws IOException {
      List<String> result = new ArrayList<>();
      LineReader lineReader = new LineReader(r);

      String line;
      while ((line = lineReader.readLine()) != null) {
         result.add(line);
      }

      return result;
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   public static <T> T readLines(Readable readable, LineProcessor<T> processor) throws IOException {
      Preconditions.checkNotNull(readable);
      Preconditions.checkNotNull(processor);
      LineReader lineReader = new LineReader(readable);

      String line;
      while ((line = lineReader.readLine()) != null && processor.processLine(line)) {
      }

      return processor.getResult();
   }

   @CanIgnoreReturnValue
   public static long exhaust(Readable readable) throws IOException {
      long total = 0L;
      CharBuffer buf = createBuffer();

      long read;
      while ((read = readable.read(buf)) != -1L) {
         total += read;
         Java8Compatibility.clear(buf);
      }

      return total;
   }

   public static void skipFully(Reader reader, long n) throws IOException {
      Preconditions.checkNotNull(reader);

      while (n > 0L) {
         long amt = reader.skip(n);
         if (amt == 0L) {
            throw new EOFException();
         }

         n -= amt;
      }
   }

   public static Writer nullWriter() {
      return CharStreams.NullWriter.INSTANCE;
   }

   public static Writer asWriter(Appendable target) {
      return target instanceof Writer ? (Writer)target : new AppendableWriter(target);
   }

   private static final class NullWriter extends Writer {
      private static final CharStreams.NullWriter INSTANCE = new CharStreams.NullWriter();

      @Override
      public void write(int c) {
      }

      @Override
      public void write(char[] cbuf) {
         Preconditions.checkNotNull(cbuf);
      }

      @Override
      public void write(char[] cbuf, int off, int len) {
         Preconditions.checkPositionIndexes(off, off + len, cbuf.length);
      }

      @Override
      public void write(String str) {
         Preconditions.checkNotNull(str);
      }

      @Override
      public void write(String str, int off, int len) {
         Preconditions.checkPositionIndexes(off, off + len, str.length());
      }

      @Override
      public Writer append(@Nullable CharSequence csq) {
         return this;
      }

      @Override
      public Writer append(@Nullable CharSequence csq, int start, int end) {
         Preconditions.checkPositionIndexes(start, end, csq == null ? "null".length() : csq.length());
         return this;
      }

      @Override
      public Writer append(char c) {
         return this;
      }

      @Override
      public void flush() {
      }

      @Override
      public void close() {
      }

      @Override
      public String toString() {
         return "CharStreams.nullWriter()";
      }
   }
}
