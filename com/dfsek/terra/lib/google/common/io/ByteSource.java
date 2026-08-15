package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Ascii;
import com.dfsek.terra.lib.google.common.base.Optional;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.hash.Funnels;
import com.dfsek.terra.lib.google.common.hash.HashCode;
import com.dfsek.terra.lib.google.common.hash.HashFunction;
import com.dfsek.terra.lib.google.common.hash.Hasher;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@J2ktIncompatible
@GwtIncompatible
public abstract class ByteSource {
   protected ByteSource() {
   }

   public CharSource asCharSource(Charset charset) {
      return new ByteSource.AsCharSource(charset);
   }

   public abstract InputStream openStream() throws IOException;

   public InputStream openBufferedStream() throws IOException {
      InputStream in = this.openStream();
      return in instanceof BufferedInputStream ? (BufferedInputStream)in : new BufferedInputStream(in);
   }

   public ByteSource slice(long offset, long length) {
      return new ByteSource.SlicedByteSource(offset, length);
   }

   public boolean isEmpty() throws IOException {
      Optional<Long> sizeIfKnown = this.sizeIfKnown();
      if (sizeIfKnown.isPresent()) {
         return sizeIfKnown.get() == 0L;
      }

      Closer closer = Closer.create();

      try {
         InputStream in = closer.register(this.openStream());
         return in.read() == -1;
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public Optional<Long> sizeIfKnown() {
      return Optional.absent();
   }

   public long size() throws IOException {
      Optional<Long> sizeIfKnown = this.sizeIfKnown();
      if (sizeIfKnown.isPresent()) {
         return sizeIfKnown.get();
      }

      Closer closer = Closer.create();

      try {
         InputStream in = closer.register(this.openStream());
         return this.countBySkipping(in);
      } catch (IOException var18) {
      } finally {
         closer.close();
      }

      closer = Closer.create();

      try {
         InputStream in = closer.register(this.openStream());
         return ByteStreams.exhaust(in);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   private long countBySkipping(InputStream in) throws IOException {
      long count = 0L;

      long skipped;
      while ((skipped = ByteStreams.skipUpTo(in, 2147483647L)) > 0L) {
         count += skipped;
      }

      return count;
   }

   @CanIgnoreReturnValue
   public long copyTo(OutputStream output) throws IOException {
      Preconditions.checkNotNull(output);
      Closer closer = Closer.create();

      try {
         InputStream in = closer.register(this.openStream());
         return ByteStreams.copy(in, output);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   @CanIgnoreReturnValue
   public long copyTo(ByteSink sink) throws IOException {
      Preconditions.checkNotNull(sink);
      Closer closer = Closer.create();

      try {
         InputStream in = closer.register(this.openStream());
         OutputStream out = closer.register(sink.openStream());
         return ByteStreams.copy(in, out);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public byte[] read() throws IOException {
      Closer closer = Closer.create();

      try {
         InputStream in = closer.register(this.openStream());
         Optional<Long> size = this.sizeIfKnown();
         return size.isPresent() ? ByteStreams.toByteArray(in, size.get()) : ByteStreams.toByteArray(in);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   public <T> T read(ByteProcessor<T> processor) throws IOException {
      Preconditions.checkNotNull(processor);
      Closer closer = Closer.create();

      try {
         InputStream in = closer.register(this.openStream());
         return ByteStreams.readBytes(in, processor);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public HashCode hash(HashFunction hashFunction) throws IOException {
      Hasher hasher = hashFunction.newHasher();
      this.copyTo(Funnels.asOutputStream(hasher));
      return hasher.hash();
   }

   public boolean contentEquals(ByteSource other) throws IOException {
      Preconditions.checkNotNull(other);
      Closer closer = Closer.create();

      try {
         return ByteStreams.contentsEqual(closer.register(this.openStream()), closer.register(other.openStream()));
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public static ByteSource concat(Iterable<? extends ByteSource> sources) {
      return new ByteSource.ConcatenatedByteSource(sources);
   }

   public static ByteSource concat(Iterator<? extends ByteSource> sources) {
      return concat(ImmutableList.copyOf(sources));
   }

   public static ByteSource concat(ByteSource... sources) {
      return concat(ImmutableList.copyOf(sources));
   }

   public static ByteSource wrap(byte[] b) {
      return new ByteSource.ByteArrayByteSource(b);
   }

   public static ByteSource empty() {
      return ByteSource.EmptyByteSource.INSTANCE;
   }

   class AsCharSource extends CharSource {
      final Charset charset;

      AsCharSource(Charset charset) {
         this.charset = Preconditions.checkNotNull(charset);
      }

      @Override
      public ByteSource asByteSource(Charset charset) {
         return charset.equals(this.charset) ? ByteSource.this : super.asByteSource(charset);
      }

      @Override
      public Reader openStream() throws IOException {
         return new InputStreamReader(ByteSource.this.openStream(), this.charset);
      }

      @Override
      public String read() throws IOException {
         return new String(ByteSource.this.read(), this.charset);
      }

      @Override
      public String toString() {
         return ByteSource.this.toString() + ".asCharSource(" + this.charset + ")";
      }
   }

   private static class ByteArrayByteSource extends ByteSource {
      final byte[] bytes;
      final int offset;
      final int length;

      ByteArrayByteSource(byte[] bytes) {
         this(bytes, 0, bytes.length);
      }

      ByteArrayByteSource(byte[] bytes, int offset, int length) {
         this.bytes = bytes;
         this.offset = offset;
         this.length = length;
      }

      @Override
      public InputStream openStream() {
         return new ByteArrayInputStream(this.bytes, this.offset, this.length);
      }

      @Override
      public InputStream openBufferedStream() {
         return this.openStream();
      }

      @Override
      public boolean isEmpty() {
         return this.length == 0;
      }

      @Override
      public long size() {
         return this.length;
      }

      @Override
      public Optional<Long> sizeIfKnown() {
         return Optional.of((long)this.length);
      }

      @Override
      public byte[] read() {
         return Arrays.copyOfRange(this.bytes, this.offset, this.offset + this.length);
      }

      @ParametricNullness
      @Override
      public <T> T read(ByteProcessor<T> processor) throws IOException {
         processor.processBytes(this.bytes, this.offset, this.length);
         return processor.getResult();
      }

      @Override
      public long copyTo(OutputStream output) throws IOException {
         output.write(this.bytes, this.offset, this.length);
         return this.length;
      }

      @Override
      public HashCode hash(HashFunction hashFunction) throws IOException {
         return hashFunction.hashBytes(this.bytes, this.offset, this.length);
      }

      @Override
      public ByteSource slice(long offset, long length) {
         Preconditions.checkArgument(offset >= 0L, "offset (%s) may not be negative", offset);
         Preconditions.checkArgument(length >= 0L, "length (%s) may not be negative", length);
         offset = Math.min(offset, this.length);
         length = Math.min(length, this.length - offset);
         int newOffset = this.offset + (int)offset;
         return new ByteSource.ByteArrayByteSource(this.bytes, newOffset, (int)length);
      }

      @Override
      public String toString() {
         return "ByteSource.wrap(" + Ascii.truncate(BaseEncoding.base16().encode(this.bytes, this.offset, this.length), 30, "...") + ")";
      }
   }

   private static final class ConcatenatedByteSource extends ByteSource {
      final Iterable<? extends ByteSource> sources;

      ConcatenatedByteSource(Iterable<? extends ByteSource> sources) {
         this.sources = Preconditions.checkNotNull(sources);
      }

      @Override
      public InputStream openStream() throws IOException {
         return new MultiInputStream(this.sources.iterator());
      }

      @Override
      public boolean isEmpty() throws IOException {
         for (ByteSource source : this.sources) {
            if (!source.isEmpty()) {
               return false;
            }
         }

         return true;
      }

      @Override
      public Optional<Long> sizeIfKnown() {
         if (!(this.sources instanceof Collection)) {
            return Optional.absent();
         }

         long result = 0L;

         for (ByteSource source : this.sources) {
            Optional<Long> sizeIfKnown = source.sizeIfKnown();
            if (!sizeIfKnown.isPresent()) {
               return Optional.absent();
            }

            result += sizeIfKnown.get();
            if (result < 0L) {
               return Optional.of(Long.MAX_VALUE);
            }
         }

         return Optional.of(result);
      }

      @Override
      public long size() throws IOException {
         long result = 0L;

         for (ByteSource source : this.sources) {
            result += source.size();
            if (result < 0L) {
               return Long.MAX_VALUE;
            }
         }

         return result;
      }

      @Override
      public String toString() {
         return "ByteSource.concat(" + this.sources + ")";
      }
   }

   private static final class EmptyByteSource extends ByteSource.ByteArrayByteSource {
      static final ByteSource.EmptyByteSource INSTANCE = new ByteSource.EmptyByteSource();

      EmptyByteSource() {
         super(new byte[0]);
      }

      @Override
      public CharSource asCharSource(Charset charset) {
         Preconditions.checkNotNull(charset);
         return CharSource.empty();
      }

      @Override
      public byte[] read() {
         return this.bytes;
      }

      @Override
      public String toString() {
         return "ByteSource.empty()";
      }
   }

   private final class SlicedByteSource extends ByteSource {
      final long offset;
      final long length;

      SlicedByteSource(long offset, long length) {
         Preconditions.checkArgument(offset >= 0L, "offset (%s) may not be negative", offset);
         Preconditions.checkArgument(length >= 0L, "length (%s) may not be negative", length);
         this.offset = offset;
         this.length = length;
      }

      @Override
      public InputStream openStream() throws IOException {
         return this.sliceStream(ByteSource.this.openStream());
      }

      @Override
      public InputStream openBufferedStream() throws IOException {
         return this.sliceStream(ByteSource.this.openBufferedStream());
      }

      private InputStream sliceStream(InputStream in) throws IOException {
         if (this.offset > 0L) {
            long skipped;
            try {
               skipped = ByteStreams.skipUpTo(in, this.offset);
            } catch (Throwable var10) {
               Throwable e = var10;
               Closer closer = Closer.create();
               closer.register(in);

               try {
                  throw closer.rethrow(e);
               } finally {
                  closer.close();
               }
            }

            if (skipped < this.offset) {
               in.close();
               return new ByteArrayInputStream(new byte[0]);
            }
         }

         return ByteStreams.limit(in, this.length);
      }

      @Override
      public ByteSource slice(long offset, long length) {
         Preconditions.checkArgument(offset >= 0L, "offset (%s) may not be negative", offset);
         Preconditions.checkArgument(length >= 0L, "length (%s) may not be negative", length);
         long maxLength = this.length - offset;
         return maxLength <= 0L ? ByteSource.empty() : ByteSource.this.slice(this.offset + offset, Math.min(length, maxLength));
      }

      @Override
      public boolean isEmpty() throws IOException {
         return this.length == 0L || super.isEmpty();
      }

      @Override
      public Optional<Long> sizeIfKnown() {
         Optional<Long> optionalUnslicedSize = ByteSource.this.sizeIfKnown();
         if (optionalUnslicedSize.isPresent()) {
            long unslicedSize = optionalUnslicedSize.get();
            long off = Math.min(this.offset, unslicedSize);
            return Optional.of(Math.min(this.length, unslicedSize - off));
         } else {
            return Optional.absent();
         }
      }

      @Override
      public String toString() {
         return ByteSource.this.toString() + ".slice(" + this.offset + ", " + this.length + ")";
      }
   }
}
