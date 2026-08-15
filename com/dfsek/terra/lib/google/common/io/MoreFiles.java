package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Optional;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicate;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.Iterables;
import com.dfsek.terra.lib.google.common.graph.Traverser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class MoreFiles {
   private MoreFiles() {
   }

   public static ByteSource asByteSource(Path path, OpenOption... options) {
      return new MoreFiles.PathByteSource(path, options);
   }

   public static ByteSink asByteSink(Path path, OpenOption... options) {
      return new MoreFiles.PathByteSink(path, options);
   }

   public static CharSource asCharSource(Path path, Charset charset, OpenOption... options) {
      return asByteSource(path, options).asCharSource(charset);
   }

   public static CharSink asCharSink(Path path, Charset charset, OpenOption... options) {
      return asByteSink(path, options).asCharSink(charset);
   }

   public static ImmutableList<Path> listFiles(Path dir) throws IOException {
      try {
         DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(dir);

         ImmutableList var2;
         try {
            var2 = ImmutableList.copyOf(stream);
         } catch (Throwable var5) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (stream != null) {
            stream.close();
         }

         return var2;
      } catch (DirectoryIteratorException e) {
         throw e.getCause();
      }
   }

   public static Traverser<Path> fileTraverser() {
      return Traverser.forTree(MoreFiles::fileTreeChildren);
   }

   private static Iterable<Path> fileTreeChildren(Path dir) {
      if (java.nio.file.Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
         try {
            return listFiles(dir);
         } catch (IOException e) {
            throw new DirectoryIteratorException(e);
         }
      } else {
         return ImmutableList.of();
      }
   }

   public static Predicate<Path> isDirectory(LinkOption... options) {
      final LinkOption[] optionsCopy = (LinkOption[])options.clone();
      return new Predicate<Path>() {
         public boolean apply(Path input) {
            return java.nio.file.Files.isDirectory(input, optionsCopy);
         }

         @Override
         public String toString() {
            return "MoreFiles.isDirectory(" + Arrays.toString(optionsCopy) + ")";
         }
      };
   }

   private static boolean isDirectory(SecureDirectoryStream<Path> dir, Path name, LinkOption... options) throws IOException {
      return dir.getFileAttributeView(name, BasicFileAttributeView.class, options).readAttributes().isDirectory();
   }

   public static Predicate<Path> isRegularFile(LinkOption... options) {
      final LinkOption[] optionsCopy = (LinkOption[])options.clone();
      return new Predicate<Path>() {
         public boolean apply(Path input) {
            return java.nio.file.Files.isRegularFile(input, optionsCopy);
         }

         @Override
         public String toString() {
            return "MoreFiles.isRegularFile(" + Arrays.toString(optionsCopy) + ")";
         }
      };
   }

   public static boolean equal(Path path1, Path path2) throws IOException {
      Preconditions.checkNotNull(path1);
      Preconditions.checkNotNull(path2);
      if (java.nio.file.Files.isSameFile(path1, path2)) {
         return true;
      }

      ByteSource source1 = asByteSource(path1);
      ByteSource source2 = asByteSource(path2);
      long len1 = source1.sizeIfKnown().or(0L);
      long len2 = source2.sizeIfKnown().or(0L);
      return len1 != 0L && len2 != 0L && len1 != len2 ? false : source1.contentEquals(source2);
   }

   public static void touch(Path path) throws IOException {
      Preconditions.checkNotNull(path);

      try {
         java.nio.file.Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
      } catch (NoSuchFileException e) {
         try {
            java.nio.file.Files.createFile(path);
         } catch (FileAlreadyExistsException var3) {
         }
      }
   }

   public static void createParentDirectories(Path path, FileAttribute<?>... attrs) throws IOException {
      Path normalizedAbsolutePath = path.toAbsolutePath().normalize();
      Path parent = normalizedAbsolutePath.getParent();
      if (parent != null) {
         if (!java.nio.file.Files.isDirectory(parent)) {
            java.nio.file.Files.createDirectories(parent, attrs);
            if (!java.nio.file.Files.isDirectory(parent)) {
               throw new IOException("Unable to create parent directories of " + path);
            }
         }
      }
   }

   public static String getFileExtension(Path path) {
      Path name = path.getFileName();
      if (name == null) {
         return "";
      }

      String fileName = name.toString();
      int dotIndex = fileName.lastIndexOf(46);
      return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
   }

   public static String getNameWithoutExtension(Path path) {
      Path name = path.getFileName();
      if (name == null) {
         return "";
      }

      String fileName = name.toString();
      int dotIndex = fileName.lastIndexOf(46);
      return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
   }

   public static void deleteRecursively(Path path, RecursiveDeleteOption... options) throws IOException {
      Path parentPath = getParentPath(path);
      if (parentPath == null) {
         throw new FileSystemException(path.toString(), null, "can't delete recursively");
      }

      Collection<IOException> exceptions = null;

      try {
         boolean sdsSupported = false;
         DirectoryStream<Path> parent = java.nio.file.Files.newDirectoryStream(parentPath);

         try {
            if (parent instanceof SecureDirectoryStream) {
               sdsSupported = true;
               exceptions = deleteRecursivelySecure((SecureDirectoryStream<Path>)parent, Objects.requireNonNull(path.getFileName()));
            }
         } catch (Throwable var9) {
            if (parent != null) {
               try {
                  parent.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (parent != null) {
            parent.close();
         }

         if (!sdsSupported) {
            checkAllowsInsecure(path, options);
            exceptions = deleteRecursivelyInsecure(path);
         }
      } catch (IOException e) {
         if (exceptions == null) {
            throw e;
         }

         exceptions.add(e);
      }

      if (exceptions != null) {
         throwDeleteFailed(path, exceptions);
      }
   }

   public static void deleteDirectoryContents(Path path, RecursiveDeleteOption... options) throws IOException {
      Collection<IOException> exceptions = null;

      try {
         DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(path);

         try {
            if (stream instanceof SecureDirectoryStream) {
               SecureDirectoryStream<Path> sds = (SecureDirectoryStream<Path>)stream;
               exceptions = deleteDirectoryContentsSecure(sds);
            } else {
               checkAllowsInsecure(path, options);
               exceptions = deleteDirectoryContentsInsecure(stream);
            }
         } catch (Throwable var7) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (IOException e) {
         if (exceptions == null) {
            throw e;
         }

         exceptions.add(e);
      }

      if (exceptions != null) {
         throwDeleteFailed(path, exceptions);
      }
   }

   private static @Nullable Collection<IOException> deleteRecursivelySecure(SecureDirectoryStream<Path> dir, Path path) {
      Collection<IOException> exceptions = null;

      try {
         if (isDirectory(dir, path, LinkOption.NOFOLLOW_LINKS)) {
            SecureDirectoryStream<Path> childDir = dir.newDirectoryStream(path, LinkOption.NOFOLLOW_LINKS);

            try {
               exceptions = deleteDirectoryContentsSecure(childDir);
            } catch (Throwable var7) {
               if (childDir != null) {
                  try {
                     childDir.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (childDir != null) {
               childDir.close();
            }

            if (exceptions == null) {
               dir.deleteDirectory(path);
            }
         } else {
            dir.deleteFile(path);
         }

         return exceptions;
      } catch (IOException e) {
         return addException(exceptions, e);
      }
   }

   private static @Nullable Collection<IOException> deleteDirectoryContentsSecure(SecureDirectoryStream<Path> dir) {
      Collection<IOException> exceptions = null;

      try {
         for (Path path : dir) {
            exceptions = concat(exceptions, deleteRecursivelySecure(dir, path.getFileName()));
         }

         return exceptions;
      } catch (DirectoryIteratorException e) {
         return addException(exceptions, e.getCause());
      }
   }

   private static @Nullable Collection<IOException> deleteRecursivelyInsecure(Path path) {
      Collection<IOException> exceptions = null;

      try {
         if (java.nio.file.Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            DirectoryStream<Path> stream = java.nio.file.Files.newDirectoryStream(path);

            try {
               exceptions = deleteDirectoryContentsInsecure(stream);
            } catch (Throwable var6) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (stream != null) {
               stream.close();
            }
         }

         if (exceptions == null) {
            java.nio.file.Files.delete(path);
         }

         return exceptions;
      } catch (IOException e) {
         return addException(exceptions, e);
      }
   }

   private static @Nullable Collection<IOException> deleteDirectoryContentsInsecure(DirectoryStream<Path> dir) {
      Collection<IOException> exceptions = null;

      try {
         for (Path entry : dir) {
            exceptions = concat(exceptions, deleteRecursivelyInsecure(entry));
         }

         return exceptions;
      } catch (DirectoryIteratorException e) {
         return addException(exceptions, e.getCause());
      }
   }

   private static @Nullable Path getParentPath(Path path) {
      Path parent = path.getParent();
      if (parent != null) {
         return parent;
      } else {
         return path.getNameCount() == 0 ? null : path.getFileSystem().getPath(".");
      }
   }

   private static void checkAllowsInsecure(Path path, RecursiveDeleteOption[] options) throws InsecureRecursiveDeleteException {
      if (!Arrays.asList(options).contains(RecursiveDeleteOption.ALLOW_INSECURE)) {
         throw new InsecureRecursiveDeleteException(path.toString());
      }
   }

   private static Collection<IOException> addException(@Nullable Collection<IOException> exceptions, IOException e) {
      if (exceptions == null) {
         exceptions = new ArrayList<>();
      }

      exceptions.add(e);
      return exceptions;
   }

   private static @Nullable Collection<IOException> concat(@Nullable Collection<IOException> exceptions, @Nullable Collection<IOException> other) {
      if (exceptions == null) {
         return other;
      }

      if (other != null) {
         exceptions.addAll(other);
      }

      return exceptions;
   }

   private static void throwDeleteFailed(Path path, Collection<IOException> exceptions) throws FileSystemException {
      NoSuchFileException pathNotFound = pathNotFound(path, exceptions);
      if (pathNotFound != null) {
         throw pathNotFound;
      }

      FileSystemException deleteFailed = new FileSystemException(
         path.toString(), null, "failed to delete one or more files; see suppressed exceptions for details"
      );

      for (IOException e : exceptions) {
         deleteFailed.addSuppressed(e);
      }

      throw deleteFailed;
   }

   private static @Nullable NoSuchFileException pathNotFound(Path path, Collection<IOException> exceptions) {
      if (exceptions.size() != 1) {
         return null;
      }

      IOException exception = Iterables.getOnlyElement(exceptions);
      if (!(exception instanceof NoSuchFileException)) {
         return null;
      }

      NoSuchFileException noSuchFileException = (NoSuchFileException)exception;
      String exceptionFile = noSuchFileException.getFile();
      if (exceptionFile == null) {
         return null;
      }

      Path parentPath = getParentPath(path);
      if (parentPath == null) {
         return null;
      }

      Path pathResolvedFromParent = parentPath.resolve(Objects.requireNonNull(path.getFileName()));
      return exceptionFile.equals(pathResolvedFromParent.toString()) ? noSuchFileException : null;
   }

   private static final class PathByteSink extends ByteSink {
      private final Path path;
      private final OpenOption[] options;

      private PathByteSink(Path path, OpenOption... options) {
         this.path = Preconditions.checkNotNull(path);
         this.options = (OpenOption[])options.clone();
      }

      @Override
      public OutputStream openStream() throws IOException {
         return java.nio.file.Files.newOutputStream(this.path, this.options);
      }

      @Override
      public String toString() {
         return "MoreFiles.asByteSink(" + this.path + ", " + Arrays.toString(this.options) + ")";
      }
   }

   private static final class PathByteSource extends ByteSource {
      private static final LinkOption[] FOLLOW_LINKS = new LinkOption[0];
      private final Path path;
      private final OpenOption[] options;
      private final boolean followLinks;

      private PathByteSource(Path path, OpenOption... options) {
         this.path = Preconditions.checkNotNull(path);
         this.options = (OpenOption[])options.clone();
         this.followLinks = followLinks(this.options);
      }

      private static boolean followLinks(OpenOption[] options) {
         for (OpenOption option : options) {
            if (option == LinkOption.NOFOLLOW_LINKS) {
               return false;
            }
         }

         return true;
      }

      @Override
      public InputStream openStream() throws IOException {
         return java.nio.file.Files.newInputStream(this.path, this.options);
      }

      private BasicFileAttributes readAttributes() throws IOException {
         return java.nio.file.Files.readAttributes(
            this.path, BasicFileAttributes.class, this.followLinks ? FOLLOW_LINKS : new LinkOption[]{LinkOption.NOFOLLOW_LINKS}
         );
      }

      @Override
      public Optional<Long> sizeIfKnown() {
         BasicFileAttributes attrs;
         try {
            attrs = this.readAttributes();
         } catch (IOException e) {
            return Optional.absent();
         }

         return !attrs.isDirectory() && !attrs.isSymbolicLink() ? Optional.of(attrs.size()) : Optional.absent();
      }

      @Override
      public long size() throws IOException {
         BasicFileAttributes attrs = this.readAttributes();
         if (attrs.isDirectory()) {
            throw new IOException("can't read: is a directory");
         } else if (attrs.isSymbolicLink()) {
            throw new IOException("can't read: is a symbolic link");
         } else {
            return attrs.size();
         }
      }

      @Override
      public byte[] read() throws IOException {
         SeekableByteChannel channel = java.nio.file.Files.newByteChannel(this.path, this.options);

         byte[] var2;
         try {
            var2 = ByteStreams.toByteArray(Channels.newInputStream(channel), channel.size());
         } catch (Throwable var5) {
            if (channel != null) {
               try {
                  channel.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (channel != null) {
            channel.close();
         }

         return var2;
      }

      @Override
      public CharSource asCharSource(Charset charset) {
         return this.options.length == 0 ? new ByteSource.AsCharSource(charset) {
            @Override
            public Stream<String> lines() throws IOException {
               return java.nio.file.Files.lines(PathByteSource.this.path, this.charset);
            }
         } : super.asCharSource(charset);
      }

      @Override
      public String toString() {
         return "MoreFiles.asByteSource(" + this.path + ", " + Arrays.toString(this.options) + ")";
      }
   }
}
