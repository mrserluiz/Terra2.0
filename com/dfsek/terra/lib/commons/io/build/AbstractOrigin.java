package com.dfsek.terra.lib.commons.io.build;

import com.dfsek.terra.lib.commons.io.IORandomAccessFile;
import com.dfsek.terra.lib.commons.io.IOUtils;
import com.dfsek.terra.lib.commons.io.RandomAccessFileMode;
import com.dfsek.terra.lib.commons.io.RandomAccessFiles;
import com.dfsek.terra.lib.commons.io.file.spi.FileSystemProviders;
import com.dfsek.terra.lib.commons.io.input.BufferedFileChannelInputStream;
import com.dfsek.terra.lib.commons.io.input.CharSequenceInputStream;
import com.dfsek.terra.lib.commons.io.input.CharSequenceReader;
import com.dfsek.terra.lib.commons.io.input.ReaderInputStream;
import com.dfsek.terra.lib.commons.io.output.RandomAccessFileOutputStream;
import com.dfsek.terra.lib.commons.io.output.WriterOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractOrigin<T, B extends AbstractOrigin<T, B>> extends AbstractSupplier<T, B> {
   final T origin;

   protected AbstractOrigin(T origin) {
      this.origin = Objects.requireNonNull(origin, "origin");
   }

   @Override
   public T get() {
      return this.origin;
   }

   public byte[] getByteArray() throws IOException {
      return Files.readAllBytes(this.getPath());
   }

   public byte[] getByteArray(long position, int length) throws IOException {
      byte[] bytes = this.getByteArray();
      int start = Math.toIntExact(position);
      if (start >= 0 && length >= 0 && start + length >= 0 && start + length <= bytes.length) {
         return Arrays.copyOfRange(bytes, start, start + length);
      } else {
         throw new IllegalArgumentException("Couldn't read array (start: " + start + ", length: " + length + ", data length: " + bytes.length + ").");
      }
   }

   public CharSequence getCharSequence(Charset charset) throws IOException {
      return new String(this.getByteArray(), charset);
   }

   public File getFile() {
      throw new UnsupportedOperationException(
         String.format("%s#getFile() for %s origin %s", this.getSimpleClassName(), this.origin.getClass().getSimpleName(), this.origin)
      );
   }

   public InputStream getInputStream(OpenOption... options) throws IOException {
      return Files.newInputStream(this.getPath(), options);
   }

   public OutputStream getOutputStream(OpenOption... options) throws IOException {
      return Files.newOutputStream(this.getPath(), options);
   }

   public Path getPath() {
      throw new UnsupportedOperationException(
         String.format("%s#getPath() for %s origin %s", this.getSimpleClassName(), this.origin.getClass().getSimpleName(), this.origin)
      );
   }

   public RandomAccessFile getRandomAccessFile(OpenOption... openOption) throws FileNotFoundException {
      return RandomAccessFileMode.valueOf(openOption).create(this.getFile());
   }

   public Reader getReader(Charset charset) throws IOException {
      return Files.newBufferedReader(this.getPath(), charset);
   }

   private String getSimpleClassName() {
      return this.getClass().getSimpleName();
   }

   public Writer getWriter(Charset charset, OpenOption... options) throws IOException {
      return Files.newBufferedWriter(this.getPath(), charset, options);
   }

   public long size() throws IOException {
      return Files.size(this.getPath());
   }

   @Override
   public String toString() {
      return this.getSimpleClassName() + "[" + this.origin.toString() + "]";
   }

   public abstract static class AbstractRandomAccessFileOrigin<T extends RandomAccessFile, B extends AbstractOrigin.AbstractRandomAccessFileOrigin<T, B>>
      extends AbstractOrigin<T, B> {
      public AbstractRandomAccessFileOrigin(T origin) {
         super(origin);
      }

      @Override
      public byte[] getByteArray() throws IOException {
         long longLen = ((RandomAccessFile)this.origin).length();
         if (longLen > 2147483647L) {
            throw new IllegalStateException("Origin too large.");
         } else {
            return RandomAccessFiles.read((RandomAccessFile)this.origin, 0L, (int)longLen);
         }
      }

      @Override
      public byte[] getByteArray(long position, int length) throws IOException {
         return RandomAccessFiles.read((RandomAccessFile)this.origin, position, length);
      }

      @Override
      public CharSequence getCharSequence(Charset charset) throws IOException {
         return new String(this.getByteArray(), charset);
      }

      @Override
      public InputStream getInputStream(OpenOption... options) throws IOException {
         return BufferedFileChannelInputStream.builder().setFileChannel(((RandomAccessFile)this.origin).getChannel()).get();
      }

      @Override
      public OutputStream getOutputStream(OpenOption... options) throws IOException {
         return RandomAccessFileOutputStream.builder().setRandomAccessFile((RandomAccessFile)this.origin).get();
      }

      @Override
      public T getRandomAccessFile(OpenOption... openOption) {
         return (T)((RandomAccessFile)this.get());
      }

      @Override
      public Reader getReader(Charset charset) throws IOException {
         return new InputStreamReader(this.getInputStream(), charset);
      }

      @Override
      public Writer getWriter(Charset charset, OpenOption... options) throws IOException {
         return new OutputStreamWriter(this.getOutputStream(options), charset);
      }

      @Override
      public long size() throws IOException {
         return ((RandomAccessFile)this.origin).length();
      }
   }

   public static class ByteArrayOrigin extends AbstractOrigin<byte[], AbstractOrigin.ByteArrayOrigin> {
      public ByteArrayOrigin(byte[] origin) {
         super(origin);
      }

      @Override
      public byte[] getByteArray() {
         return (byte[])this.get();
      }

      @Override
      public InputStream getInputStream(OpenOption... options) throws IOException {
         return new ByteArrayInputStream(this.origin);
      }

      @Override
      public Reader getReader(Charset charset) throws IOException {
         return new InputStreamReader(this.getInputStream(), charset);
      }

      @Override
      public long size() throws IOException {
         return this.origin.length;
      }
   }

   public static class CharSequenceOrigin extends AbstractOrigin<CharSequence, AbstractOrigin.CharSequenceOrigin> {
      public CharSequenceOrigin(CharSequence origin) {
         super(origin);
      }

      @Override
      public byte[] getByteArray() {
         return this.origin.toString().getBytes(Charset.defaultCharset());
      }

      @Override
      public CharSequence getCharSequence(Charset charset) {
         return (CharSequence)this.get();
      }

      @Override
      public InputStream getInputStream(OpenOption... options) throws IOException {
         return CharSequenceInputStream.builder().setCharSequence(this.getCharSequence(Charset.defaultCharset())).get();
      }

      @Override
      public Reader getReader(Charset charset) throws IOException {
         return new CharSequenceReader((CharSequence)this.get());
      }

      @Override
      public long size() throws IOException {
         return this.origin.length();
      }
   }

   public static class FileOrigin extends AbstractOrigin<File, AbstractOrigin.FileOrigin> {
      public FileOrigin(File origin) {
         super(origin);
      }

      @Override
      public byte[] getByteArray(long position, int length) throws IOException {
         RandomAccessFile raf = RandomAccessFileMode.READ_ONLY.create(this.origin);

         byte[] var5;
         try {
            var5 = RandomAccessFiles.read(raf, position, length);
         } catch (Throwable var8) {
            if (raf != null) {
               try {
                  raf.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (raf != null) {
            raf.close();
         }

         return var5;
      }

      @Override
      public File getFile() {
         return (File)this.get();
      }

      @Override
      public Path getPath() {
         return ((File)this.get()).toPath();
      }
   }

   public static class IORandomAccessFileOrigin
      extends AbstractOrigin.AbstractRandomAccessFileOrigin<IORandomAccessFile, AbstractOrigin.IORandomAccessFileOrigin> {
      public IORandomAccessFileOrigin(IORandomAccessFile origin) {
         super(origin);
      }

      @Override
      public File getFile() {
         return ((IORandomAccessFile)this.get()).getFile();
      }

      @Override
      public Path getPath() {
         return this.getFile().toPath();
      }
   }

   public static class InputStreamOrigin extends AbstractOrigin<InputStream, AbstractOrigin.InputStreamOrigin> {
      public InputStreamOrigin(InputStream origin) {
         super(origin);
      }

      @Override
      public byte[] getByteArray() throws IOException {
         return IOUtils.toByteArray(this.origin);
      }

      @Override
      public InputStream getInputStream(OpenOption... options) {
         return (InputStream)this.get();
      }

      @Override
      public Reader getReader(Charset charset) throws IOException {
         return new InputStreamReader(this.getInputStream(), charset);
      }
   }

   public static class OutputStreamOrigin extends AbstractOrigin<OutputStream, AbstractOrigin.OutputStreamOrigin> {
      public OutputStreamOrigin(OutputStream origin) {
         super(origin);
      }

      @Override
      public OutputStream getOutputStream(OpenOption... options) {
         return (OutputStream)this.get();
      }

      @Override
      public Writer getWriter(Charset charset, OpenOption... options) throws IOException {
         return new OutputStreamWriter(this.origin, charset);
      }
   }

   public static class PathOrigin extends AbstractOrigin<Path, AbstractOrigin.PathOrigin> {
      public PathOrigin(Path origin) {
         super(origin);
      }

      @Override
      public byte[] getByteArray(long position, int length) throws IOException {
         return RandomAccessFileMode.READ_ONLY.apply(this.origin, raf -> RandomAccessFiles.read(raf, position, length));
      }

      @Override
      public File getFile() {
         return ((Path)this.get()).toFile();
      }

      @Override
      public Path getPath() {
         return (Path)this.get();
      }
   }

   public static class RandomAccessFileOrigin extends AbstractOrigin.AbstractRandomAccessFileOrigin<RandomAccessFile, AbstractOrigin.RandomAccessFileOrigin> {
      public RandomAccessFileOrigin(RandomAccessFile origin) {
         super(origin);
      }
   }

   public static class ReaderOrigin extends AbstractOrigin<Reader, AbstractOrigin.ReaderOrigin> {
      public ReaderOrigin(Reader origin) {
         super(origin);
      }

      @Override
      public byte[] getByteArray() throws IOException {
         return IOUtils.toByteArray(this.origin, Charset.defaultCharset());
      }

      @Override
      public CharSequence getCharSequence(Charset charset) throws IOException {
         return IOUtils.toString(this.origin);
      }

      @Override
      public InputStream getInputStream(OpenOption... options) throws IOException {
         return ReaderInputStream.builder().setReader(this.origin).setCharset(Charset.defaultCharset()).get();
      }

      @Override
      public Reader getReader(Charset charset) throws IOException {
         return (Reader)this.get();
      }
   }

   public static class URIOrigin extends AbstractOrigin<URI, AbstractOrigin.URIOrigin> {
      private static final String SCHEME_HTTPS = "https";
      private static final String SCHEME_HTTP = "http";

      public URIOrigin(URI origin) {
         super(origin);
      }

      @Override
      public File getFile() {
         return this.getPath().toFile();
      }

      @Override
      public InputStream getInputStream(OpenOption... options) throws IOException {
         URI uri = (URI)this.get();
         String scheme = uri.getScheme();
         FileSystemProvider fileSystemProvider = FileSystemProviders.installed().getFileSystemProvider(scheme);
         if (fileSystemProvider != null) {
            return Files.newInputStream(fileSystemProvider.getPath(uri), options);
         } else {
            return !"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)
               ? Files.newInputStream(this.getPath(), options)
               : uri.toURL().openStream();
         }
      }

      @Override
      public Path getPath() {
         return Paths.get((URI)this.get());
      }
   }

   public static class WriterOrigin extends AbstractOrigin<Writer, AbstractOrigin.WriterOrigin> {
      public WriterOrigin(Writer origin) {
         super(origin);
      }

      @Override
      public OutputStream getOutputStream(OpenOption... options) throws IOException {
         return WriterOutputStream.builder().setWriter(this.origin).setCharset(Charset.defaultCharset()).get();
      }

      @Override
      public Writer getWriter(Charset charset, OpenOption... options) throws IOException {
         return (Writer)this.get();
      }
   }
}
