package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Joiner;
import com.dfsek.terra.lib.google.common.base.Optional;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.dfsek.terra.lib.google.common.base.Splitter;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.ImmutableSet;
import com.dfsek.terra.lib.google.common.collect.Lists;
import com.dfsek.terra.lib.google.common.graph.SuccessorsFunction;
import com.dfsek.terra.lib.google.common.graph.Traverser;
import com.dfsek.terra.lib.google.common.hash.HashCode;
import com.dfsek.terra.lib.google.common.hash.HashFunction;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.InlineMe;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class Files {
   private static final SuccessorsFunction<File> FILE_TREE = file -> {
      if (file.isDirectory()) {
         File[] files = file.listFiles();
         if (files != null) {
            return Collections.unmodifiableList(Arrays.asList(files));
         }
      }

      return ImmutableList.of();
   };

   private Files() {
   }

   public static BufferedReader newReader(File file, Charset charset) throws FileNotFoundException {
      Preconditions.checkNotNull(file);
      Preconditions.checkNotNull(charset);
      return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
   }

   public static BufferedWriter newWriter(File file, Charset charset) throws FileNotFoundException {
      Preconditions.checkNotNull(file);
      Preconditions.checkNotNull(charset);
      return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
   }

   public static ByteSource asByteSource(File file) {
      return new Files.FileByteSource(file);
   }

   public static ByteSink asByteSink(File file, FileWriteMode... modes) {
      return new Files.FileByteSink(file, modes);
   }

   public static CharSource asCharSource(File file, Charset charset) {
      return asByteSource(file).asCharSource(charset);
   }

   public static CharSink asCharSink(File file, Charset charset, FileWriteMode... modes) {
      return asByteSink(file, modes).asCharSink(charset);
   }

   public static byte[] toByteArray(File file) throws IOException {
      return asByteSource(file).read();
   }

   @Deprecated
   @InlineMe(replacement = "Files.asCharSource(file, charset).read()", imports = "com.dfsek.terra.lib.google.common.io.Files")
   public static String toString(File file, Charset charset) throws IOException {
      return asCharSource(file, charset).read();
   }

   public static void write(byte[] from, File to) throws IOException {
      asByteSink(to).write(from);
   }

   @Deprecated
   @InlineMe(replacement = "Files.asCharSink(to, charset).write(from)", imports = "com.dfsek.terra.lib.google.common.io.Files")
   public static void write(CharSequence from, File to, Charset charset) throws IOException {
      asCharSink(to, charset).write(from);
   }

   public static void copy(File from, OutputStream to) throws IOException {
      asByteSource(from).copyTo(to);
   }

   public static void copy(File from, File to) throws IOException {
      Preconditions.checkArgument(!from.equals(to), "Source %s and destination %s must be different", from, to);
      asByteSource(from).copyTo(asByteSink(to));
   }

   @Deprecated
   @InlineMe(replacement = "Files.asCharSource(from, charset).copyTo(to)", imports = "com.dfsek.terra.lib.google.common.io.Files")
   public static void copy(File from, Charset charset, Appendable to) throws IOException {
      asCharSource(from, charset).copyTo(to);
   }

   @Deprecated
   @InlineMe(
      replacement = "Files.asCharSink(to, charset, FileWriteMode.APPEND).write(from)",
      imports = {"com.dfsek.terra.lib.google.common.io.FileWriteMode", "com.dfsek.terra.lib.google.common.io.Files"}
   )
   public static void append(CharSequence from, File to, Charset charset) throws IOException {
      asCharSink(to, charset, FileWriteMode.APPEND).write(from);
   }

   public static boolean equal(File file1, File file2) throws IOException {
      Preconditions.checkNotNull(file1);
      Preconditions.checkNotNull(file2);
      if (file1 != file2 && !file1.equals(file2)) {
         long len1 = file1.length();
         long len2 = file2.length();
         return len1 != 0L && len2 != 0L && len1 != len2 ? false : asByteSource(file1).contentEquals(asByteSource(file2));
      } else {
         return true;
      }
   }

   @Deprecated
   @Beta
   public static File createTempDir() {
      return TempFileCreator.INSTANCE.createTempDir();
   }

   public static void touch(File file) throws IOException {
      Preconditions.checkNotNull(file);
      if (!file.createNewFile() && !file.setLastModified(System.currentTimeMillis())) {
         throw new IOException("Unable to update modification time of " + file);
      }
   }

   public static void createParentDirs(File file) throws IOException {
      Preconditions.checkNotNull(file);
      File parent = file.getCanonicalFile().getParentFile();
      if (parent != null) {
         parent.mkdirs();
         if (!parent.isDirectory()) {
            throw new IOException("Unable to create parent directories of " + file);
         }
      }
   }

   public static void move(File from, File to) throws IOException {
      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(to);
      Preconditions.checkArgument(!from.equals(to), "Source %s and destination %s must be different", from, to);
      if (!from.renameTo(to)) {
         copy(from, to);
         if (!from.delete()) {
            if (!to.delete()) {
               throw new IOException("Unable to delete " + to);
            }

            throw new IOException("Unable to delete " + from);
         }
      }
   }

   @Deprecated
   @InlineMe(replacement = "Files.asCharSource(file, charset).readFirstLine()", imports = "com.dfsek.terra.lib.google.common.io.Files")
   public static @Nullable String readFirstLine(File file, Charset charset) throws IOException {
      return asCharSource(file, charset).readFirstLine();
   }

   public static List<String> readLines(File file, Charset charset) throws IOException {
      return asCharSource(file, charset).readLines(new LineProcessor<List<String>>() {
         final List<String> result = Lists.newArrayList();

         @Override
         public boolean processLine(String line) {
            this.result.add(line);
            return true;
         }

         public List<String> getResult() {
            return this.result;
         }
      });
   }

   @Deprecated
   @InlineMe(replacement = "Files.asCharSource(file, charset).readLines(callback)", imports = "com.dfsek.terra.lib.google.common.io.Files")
   @CanIgnoreReturnValue
   @ParametricNullness
   public static <T> T readLines(File file, Charset charset, LineProcessor<T> callback) throws IOException {
      return asCharSource(file, charset).readLines(callback);
   }

   @Deprecated
   @InlineMe(replacement = "Files.asByteSource(file).read(processor)", imports = "com.dfsek.terra.lib.google.common.io.Files")
   @CanIgnoreReturnValue
   @ParametricNullness
   public static <T> T readBytes(File file, ByteProcessor<T> processor) throws IOException {
      return asByteSource(file).read(processor);
   }

   @Deprecated
   @InlineMe(replacement = "Files.asByteSource(file).hash(hashFunction)", imports = "com.dfsek.terra.lib.google.common.io.Files")
   public static HashCode hash(File file, HashFunction hashFunction) throws IOException {
      return asByteSource(file).hash(hashFunction);
   }

   public static MappedByteBuffer map(File file) throws IOException {
      Preconditions.checkNotNull(file);
      return map(file, MapMode.READ_ONLY);
   }

   public static MappedByteBuffer map(File file, MapMode mode) throws IOException {
      return mapInternal(file, mode, -1L);
   }

   public static MappedByteBuffer map(File file, MapMode mode, long size) throws IOException {
      Preconditions.checkArgument(size >= 0L, "size (%s) may not be negative", size);
      return mapInternal(file, mode, size);
   }

   private static MappedByteBuffer mapInternal(File file, MapMode mode, long size) throws IOException {
      Preconditions.checkNotNull(file);
      Preconditions.checkNotNull(mode);
      Closer closer = Closer.create();

      try {
         RandomAccessFile raf = closer.register(new RandomAccessFile(file, mode == MapMode.READ_ONLY ? "r" : "rw"));
         FileChannel channel = closer.register(raf.getChannel());
         return channel.map(mode, 0L, size == -1L ? channel.size() : size);
      } catch (Throwable e) {
         throw closer.rethrow(e);
      } finally {
         closer.close();
      }
   }

   public static String simplifyPath(String pathname) {
      Preconditions.checkNotNull(pathname);
      if (pathname.length() == 0) {
         return ".";
      }

      Iterable<String> components = Splitter.on('/').omitEmptyStrings().split(pathname);
      List<String> path = new ArrayList<>();

      for (String component : components) {
         switch (component) {
            case ".":
               break;
            case "..":
               if (path.size() > 0 && !path.get(path.size() - 1).equals("..")) {
                  path.remove(path.size() - 1);
                  break;
               }

               path.add("..");
               break;
            default:
               path.add(component);
         }
      }

      String result = Joiner.on('/').join(path);
      if (pathname.charAt(0) == '/') {
         result = "/" + result;
      }

      while (result.startsWith("/../")) {
         result = result.substring(3);
      }

      if (result.equals("/..")) {
         result = "/";
      } else if (result.isEmpty()) {
         result = ".";
      }

      return result;
   }

   public static String getFileExtension(String fullName) {
      Preconditions.checkNotNull(fullName);
      String fileName = new File(fullName).getName();
      int dotIndex = fileName.lastIndexOf(46);
      return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
   }

   public static String getNameWithoutExtension(String file) {
      Preconditions.checkNotNull(file);
      String fileName = new File(file).getName();
      int dotIndex = fileName.lastIndexOf(46);
      return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
   }

   public static Traverser<File> fileTraverser() {
      return Traverser.forTree(FILE_TREE);
   }

   public static Predicate<File> isDirectory() {
      return Files.FilePredicate.IS_DIRECTORY;
   }

   public static Predicate<File> isFile() {
      return Files.FilePredicate.IS_FILE;
   }

   private static final class FileByteSink extends ByteSink {
      private final File file;
      private final ImmutableSet<FileWriteMode> modes;

      private FileByteSink(File file, FileWriteMode... modes) {
         this.file = Preconditions.checkNotNull(file);
         this.modes = ImmutableSet.copyOf(modes);
      }

      public FileOutputStream openStream() throws IOException {
         return new FileOutputStream(this.file, this.modes.contains(FileWriteMode.APPEND));
      }

      @Override
      public String toString() {
         return "Files.asByteSink(" + this.file + ", " + this.modes + ")";
      }
   }

   private static final class FileByteSource extends ByteSource {
      private final File file;

      private FileByteSource(File file) {
         this.file = Preconditions.checkNotNull(file);
      }

      public FileInputStream openStream() throws IOException {
         return new FileInputStream(this.file);
      }

      @Override
      public Optional<Long> sizeIfKnown() {
         return this.file.isFile() ? Optional.of(this.file.length()) : Optional.absent();
      }

      @Override
      public long size() throws IOException {
         if (!this.file.isFile()) {
            throw new FileNotFoundException(this.file.toString());
         } else {
            return this.file.length();
         }
      }

      @Override
      public byte[] read() throws IOException {
         Closer closer = Closer.create();

         try {
            FileInputStream in = closer.register(this.openStream());
            return ByteStreams.toByteArray(in, in.getChannel().size());
         } catch (Throwable e) {
            throw closer.rethrow(e);
         } finally {
            closer.close();
         }
      }

      @Override
      public String toString() {
         return "Files.asByteSource(" + this.file + ")";
      }
   }

   private enum FilePredicate implements Predicate<File> {
      IS_DIRECTORY {
         public boolean apply(File file) {
            return file.isDirectory();
         }

         @Override
         public String toString() {
            return "Files.isDirectory()";
         }
      },
      IS_FILE {
         public boolean apply(File file) {
            return file.isFile();
         }

         @Override
         public String toString() {
            return "Files.isFile()";
         }
      };

      FilePredicate() {
      }
   }
}
