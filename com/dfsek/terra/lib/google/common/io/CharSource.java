package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Ascii;
import com.dfsek.terra.lib.google.common.base.Optional;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Splitter;
import com.dfsek.terra.lib.google.common.collect.AbstractIterator;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.Lists;
import com.dfsek.terra.lib.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public abstract class CharSource {
   protected CharSource() {
   }

   public ByteSource asByteSource(Charset charset) {
      return new CharSource.AsByteSource(charset);
   }

   public abstract Reader openStream() throws IOException;

   public BufferedReader openBufferedStream() throws IOException {
      Reader reader = this.openStream();
      return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
   }

   @MustBeClosed
   public Stream<String> lines() throws IOException {
      BufferedReader reader = this.openBufferedStream();
      return reader.lines().onClose(() -> closeUnchecked(reader));
   }

   @IgnoreJRERequirement
   private static void closeUnchecked(Closeable closeable) {
      try {
         closeable.close();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   public Optional<Long> lengthIfKnown() {
      return Optional.absent();
   }

   public long length() throws IOException {
      Optional<Long> lengthIfKnown = this.lengthIfKnown();
      if (lengthIfKnown.isPresent()) {
         return lengthIfKnown.get();
      }

      Closer closer = Closer.create();

      try {
         Reader reader = closer.register(this.openStream());
         return this.countBySkipping(reader);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   private long countBySkipping(Reader reader) throws IOException {
      long count = 0L;

      long read;
      while ((read = reader.skip(Long.MAX_VALUE)) != 0L) {
         count += read;
      }

      return count;
   }

   @CanIgnoreReturnValue
   public long copyTo(Appendable appendable) throws IOException {
      Preconditions.checkNotNull(appendable);
      Closer closer = Closer.create();

      try {
         Reader reader = closer.register(this.openStream());
         return CharStreams.copy(reader, appendable);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   @CanIgnoreReturnValue
   public long copyTo(CharSink sink) throws IOException {
      Preconditions.checkNotNull(sink);
      Closer closer = Closer.create();

      try {
         Reader reader = closer.register(this.openStream());
         Writer writer = closer.register(sink.openStream());
         return CharStreams.copy(reader, writer);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public String read() throws IOException {
      Closer closer = Closer.create();

      try {
         Reader reader = closer.register(this.openStream());
         return CharStreams.toString(reader);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public @Nullable String readFirstLine() throws IOException {
      Closer closer = Closer.create();

      try {
         BufferedReader reader = closer.register(this.openBufferedStream());
         return reader.readLine();
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public ImmutableList<String> readLines() throws IOException {
      Closer closer = Closer.create();

      try {
         BufferedReader reader = closer.register(this.openBufferedStream());
         List<String> result = Lists.newArrayList();

         String line;
         while ((line = reader.readLine()) != null) {
            result.add(line);
         }

         return ImmutableList.copyOf(result);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   public <T> T readLines(LineProcessor<T> processor) throws IOException {
      Preconditions.checkNotNull(processor);
      Closer closer = Closer.create();

      try {
         Reader reader = closer.register(this.openStream());
         return CharStreams.readLines(reader, processor);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public void forEachLine(Consumer<? super String> action) throws IOException {
      try {
         Stream<String> lines = this.lines();

         try {
            lines.forEachOrdered(action);
         } catch (Throwable var6) {
            if (lines != null) {
               try {
                  lines.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (lines != null) {
            lines.close();
         }
      } catch (UncheckedIOException e) {
         throw e.getCause();
      }
   }

   public boolean isEmpty() throws IOException {
      Optional<Long> lengthIfKnown = this.lengthIfKnown();
      if (lengthIfKnown.isPresent()) {
         return lengthIfKnown.get() == 0L;
      }

      Closer closer = Closer.create();

      try {
         Reader reader = closer.register(this.openStream());
         return reader.read() == -1;
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public static CharSource concat(Iterable<? extends CharSource> sources) {
      return new CharSource.ConcatenatedCharSource(sources);
   }

   public static CharSource concat(Iterator<? extends CharSource> sources) {
      return concat(ImmutableList.copyOf(sources));
   }

   public static CharSource concat(CharSource... sources) {
      return concat(ImmutableList.copyOf(sources));
   }

   public static CharSource wrap(CharSequence charSequence) {
      return charSequence instanceof String ? new CharSource.StringCharSource((String)charSequence) : new CharSource.CharSequenceCharSource(charSequence);
   }

   public static CharSource empty() {
      return CharSource.EmptyCharSource.INSTANCE;
   }

   private final class AsByteSource extends ByteSource {
      final Charset charset;

      AsByteSource(Charset charset) {
         this.charset = Preconditions.checkNotNull(charset);
      }

      @Override
      public CharSource asCharSource(Charset charset) {
         return charset.equals(this.charset) ? CharSource.this : super.asCharSource(charset);
      }

      @Override
      public InputStream openStream() throws IOException {
         return new ReaderInputStream(CharSource.this.openStream(), this.charset, 8192);
      }

      @Override
      public String toString() {
         return CharSource.this.toString() + ".asByteSource(" + this.charset + ")";
      }
   }

   private static class CharSequenceCharSource extends CharSource {
      private static final Splitter LINE_SPLITTER = Splitter.onPattern("\r\n|\n|\r");
      protected final CharSequence seq;

      protected CharSequenceCharSource(CharSequence seq) {
         this.seq = Preconditions.checkNotNull(seq);
      }

      @Override
      public Reader openStream() {
         return new CharSequenceReader(this.seq);
      }

      @Override
      public String read() {
         return this.seq.toString();
      }

      @Override
      public boolean isEmpty() {
         return this.seq.length() == 0;
      }

      @Override
      public long length() {
         return this.seq.length();
      }

      @Override
      public Optional<Long> lengthIfKnown() {
         return Optional.of((long)this.seq.length());
      }

      private Iterator<String> linesIterator() {
         return new AbstractIterator<String>() {
            Iterator<String> lines = CharSource.CharSequenceCharSource.LINE_SPLITTER.split(CharSequenceCharSource.this.seq).iterator();

            protected @Nullable String computeNext() {
               if (this.lines.hasNext()) {
                  String next = this.lines.next();
                  if (this.lines.hasNext() || !next.isEmpty()) {
                     return next;
                  }
               }

               return this.endOfData();
            }
         };
      }

      @Override
      public Stream<String> lines() {
         return Streams.stream(this.linesIterator());
      }

      @Override
      public @Nullable String readFirstLine() {
         Iterator<String> lines = this.linesIterator();
         return lines.hasNext() ? lines.next() : null;
      }

      @Override
      public ImmutableList<String> readLines() {
         return ImmutableList.copyOf(this.linesIterator());
      }

      @ParametricNullness
      @Override
      public <T> T readLines(LineProcessor<T> processor) throws IOException {
         Iterator<String> lines = this.linesIterator();

         while (lines.hasNext() && processor.processLine(lines.next())) {
         }

         return processor.getResult();
      }

      @Override
      public String toString() {
         return "CharSource.wrap(" + Ascii.truncate(this.seq, 30, "...") + ")";
      }
   }

   private static final class ConcatenatedCharSource extends CharSource {
      private final Iterable<? extends CharSource> sources;

      ConcatenatedCharSource(Iterable<? extends CharSource> sources) {
         this.sources = Preconditions.checkNotNull(sources);
      }

      @Override
      public Reader openStream() throws IOException {
         return new MultiReader(this.sources.iterator());
      }

      @Override
      public boolean isEmpty() throws IOException {
         for (CharSource source : this.sources) {
            if (!source.isEmpty()) {
               return false;
            }
         }

         return true;
      }

      @Override
      public Optional<Long> lengthIfKnown() {
         long result = 0L;

         for (CharSource source : this.sources) {
            Optional<Long> lengthIfKnown = source.lengthIfKnown();
            if (!lengthIfKnown.isPresent()) {
               return Optional.absent();
            }

            result += lengthIfKnown.get();
         }

         return Optional.of(result);
      }

      @Override
      public long length() throws IOException {
         long result = 0L;

         for (CharSource source : this.sources) {
            result += source.length();
         }

         return result;
      }

      @Override
      public String toString() {
         return "CharSource.concat(" + this.sources + ")";
      }
   }

   private static final class EmptyCharSource extends CharSource.StringCharSource {
      private static final CharSource.EmptyCharSource INSTANCE = new CharSource.EmptyCharSource();

      private EmptyCharSource() {
         super("");
      }

      @Override
      public String toString() {
         return "CharSource.empty()";
      }
   }

   private static class StringCharSource extends CharSource.CharSequenceCharSource {
      protected StringCharSource(String seq) {
         super(seq);
      }

      @Override
      public Reader openStream() {
         return new StringReader((String)this.seq);
      }

      @Override
      public long copyTo(Appendable appendable) throws IOException {
         appendable.append(this.seq);
         return this.seq.length();
      }

      @Override
      public long copyTo(CharSink sink) throws IOException {
         Preconditions.checkNotNull(sink);
         Closer closer = Closer.create();

         try {
            Writer writer = closer.register(sink.openStream());
            writer.write((String)this.seq);
            return this.seq.length();
         } catch (Throwable e) {
            throw closer.rethrow(e);
         } finally {
            closer.close();
         }
      }
   }
}
