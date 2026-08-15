package com.dfsek.terra.lib.commons.io.file;

import com.dfsek.terra.lib.commons.io.Charsets;
import com.dfsek.terra.lib.commons.io.FileUtils;
import com.dfsek.terra.lib.commons.io.FilenameUtils;
import com.dfsek.terra.lib.commons.io.IOUtils;
import com.dfsek.terra.lib.commons.io.RandomAccessFileMode;
import com.dfsek.terra.lib.commons.io.RandomAccessFiles;
import com.dfsek.terra.lib.commons.io.ThreadUtils;
import com.dfsek.terra.lib.commons.io.file.attribute.FileTimes;
import com.dfsek.terra.lib.commons.io.function.IOFunction;
import com.dfsek.terra.lib.commons.io.function.IOSupplier;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class PathUtils {
   private static final OpenOption[] OPEN_OPTIONS_TRUNCATE = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
   private static final OpenOption[] OPEN_OPTIONS_APPEND = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};
   public static final CopyOption[] EMPTY_COPY_OPTIONS = new CopyOption[0];
   public static final DeleteOption[] EMPTY_DELETE_OPTION_ARRAY = new DeleteOption[0];
   public static final FileAttribute<?>[] EMPTY_FILE_ATTRIBUTE_ARRAY = new FileAttribute[0];
   public static final FileVisitOption[] EMPTY_FILE_VISIT_OPTION_ARRAY = new FileVisitOption[0];
   public static final LinkOption[] EMPTY_LINK_OPTION_ARRAY = new LinkOption[0];
   @Deprecated
   public static final LinkOption[] NOFOLLOW_LINK_OPTION_ARRAY = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
   static final LinkOption NULL_LINK_OPTION = null;
   public static final OpenOption[] EMPTY_OPEN_OPTION_ARRAY = new OpenOption[0];
   public static final Path[] EMPTY_PATH_ARRAY = new Path[0];

   private static AccumulatorPathVisitor accumulate(Path directory, int maxDepth, FileVisitOption[] fileVisitOptions) throws IOException {
      return visitFileTree(
         AccumulatorPathVisitor.builder().setDirectoryPostTransformer(PathUtils::stripTrailingSeparator).get(),
         directory,
         toFileVisitOptionSet(fileVisitOptions),
         maxDepth
      );
   }

   public static Counters.PathCounters cleanDirectory(Path directory) throws IOException {
      return cleanDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
   }

   public static Counters.PathCounters cleanDirectory(Path directory, DeleteOption... deleteOptions) throws IOException {
      return visitFileTree(new CleaningPathVisitor(Counters.longPathCounters(), deleteOptions), directory).getPathCounters();
   }

   private static int compareLastModifiedTimeTo(Path file, FileTime fileTime, LinkOption... options) throws IOException {
      return getLastModifiedTime(file, options).compareTo(fileTime);
   }

   public static boolean contentEquals(FileSystem fileSystem1, FileSystem fileSystem2) throws IOException {
      if (Objects.equals(fileSystem1, fileSystem2)) {
         return true;
      }

      List<Path> sortedList1 = toSortedList(fileSystem1.getRootDirectories());
      List<Path> sortedList2 = toSortedList(fileSystem2.getRootDirectories());
      if (sortedList1.size() != sortedList2.size()) {
         return false;
      }

      for (int i = 0; i < sortedList1.size(); i++) {
         if (!directoryAndFileContentEquals(sortedList1.get(i), sortedList2.get(i))) {
            return false;
         }
      }

      return true;
   }

   public static long copy(IOSupplier<InputStream> in, Path target, CopyOption... copyOptions) throws IOException {
      InputStream inputStream = in.get();

      long var4;
      try {
         var4 = Files.copy(inputStream, target, copyOptions);
      } catch (Throwable var7) {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (inputStream != null) {
         inputStream.close();
      }

      return var4;
   }

   public static Counters.PathCounters copyDirectory(Path sourceDirectory, Path targetDirectory, CopyOption... copyOptions) throws IOException {
      Path absoluteSource = sourceDirectory.toAbsolutePath();
      return visitFileTree(new CopyDirectoryVisitor(Counters.longPathCounters(), absoluteSource, targetDirectory, copyOptions), absoluteSource)
         .getPathCounters();
   }

   public static Path copyFile(URL sourceFile, Path targetFile, CopyOption... copyOptions) throws IOException {
      copy(sourceFile::openStream, targetFile, copyOptions);
      return targetFile;
   }

   public static Path copyFileToDirectory(Path sourceFile, Path targetDirectory, CopyOption... copyOptions) throws IOException {
      Path sourceFileName = Objects.requireNonNull(sourceFile.getFileName(), "source file name");
      Path targetFile = resolve(targetDirectory, sourceFileName);
      return Files.copy(sourceFile, targetFile, copyOptions);
   }

   public static Path copyFileToDirectory(URL sourceFile, Path targetDirectory, CopyOption... copyOptions) throws IOException {
      Path resolve = targetDirectory.resolve(FilenameUtils.getName(sourceFile.getFile()));
      copy(sourceFile::openStream, resolve, copyOptions);
      return resolve;
   }

   public static Counters.PathCounters countDirectory(Path directory) throws IOException {
      return visitFileTree(CountingPathVisitor.withLongCounters(), directory).getPathCounters();
   }

   public static Counters.PathCounters countDirectoryAsBigInteger(Path directory) throws IOException {
      return visitFileTree(CountingPathVisitor.withBigIntegerCounters(), directory).getPathCounters();
   }

   public static Path createParentDirectories(Path path, FileAttribute<?>... attrs) throws IOException {
      return createParentDirectories(path, LinkOption.NOFOLLOW_LINKS, attrs);
   }

   public static Path createParentDirectories(Path path, LinkOption linkOption, FileAttribute<?>... attrs) throws IOException {
      Path parent = getParent(path);
      parent = linkOption == LinkOption.NOFOLLOW_LINKS ? parent : readIfSymbolicLink(parent);
      if (parent == null) {
         return null;
      }

      boolean exists = linkOption == null ? Files.exists(parent) : Files.exists(parent, linkOption);
      return exists ? parent : Files.createDirectories(parent, attrs);
   }

   public static Path current() {
      return Paths.get(".");
   }

   public static Counters.PathCounters delete(Path path) throws IOException {
      return delete(path, EMPTY_DELETE_OPTION_ARRAY);
   }

   public static Counters.PathCounters delete(Path path, DeleteOption... deleteOptions) throws IOException {
      return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) ? deleteDirectory(path, deleteOptions) : deleteFile(path, deleteOptions);
   }

   public static Counters.PathCounters delete(Path path, LinkOption[] linkOptions, DeleteOption... deleteOptions) throws IOException {
      return Files.isDirectory(path, linkOptions) ? deleteDirectory(path, linkOptions, deleteOptions) : deleteFile(path, linkOptions, deleteOptions);
   }

   public static Counters.PathCounters deleteDirectory(Path directory) throws IOException {
      return deleteDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
   }

   public static Counters.PathCounters deleteDirectory(Path directory, DeleteOption... deleteOptions) throws IOException {
      LinkOption[] linkOptions = noFollowLinkOptionArray();
      return withPosixFileAttributes(
         getParent(directory),
         linkOptions,
         overrideReadOnly(deleteOptions),
         pfa -> visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions), directory).getPathCounters()
      );
   }

   public static Counters.PathCounters deleteDirectory(Path directory, LinkOption[] linkOptions, DeleteOption... deleteOptions) throws IOException {
      return visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions), directory).getPathCounters();
   }

   public static Counters.PathCounters deleteFile(Path file) throws IOException {
      return deleteFile(file, EMPTY_DELETE_OPTION_ARRAY);
   }

   public static Counters.PathCounters deleteFile(Path file, DeleteOption... deleteOptions) throws IOException {
      return deleteFile(file, noFollowLinkOptionArray(), deleteOptions);
   }

   public static Counters.PathCounters deleteFile(Path file, LinkOption[] linkOptions, DeleteOption... deleteOptions) throws NoSuchFileException, IOException {
      if (Files.isDirectory(file, linkOptions)) {
         throw new NoSuchFileException(file.toString());
      }

      Counters.PathCounters pathCounts = Counters.longPathCounters();
      boolean exists = exists(file, linkOptions);
      long size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0L;

      try {
         if (Files.deleteIfExists(file)) {
            pathCounts.getFileCounter().increment();
            pathCounts.getByteCounter().add(size);
            return pathCounts;
         }
      } catch (AccessDeniedException var12) {
      }

      Path parent = getParent(file);
      PosixFileAttributes posixFileAttributes = null;

      try {
         if (overrideReadOnly(deleteOptions)) {
            posixFileAttributes = readPosixFileAttributes(parent, linkOptions);
            setReadOnly(file, false, linkOptions);
         }

         exists = exists(file, linkOptions);
         size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0L;
         if (Files.deleteIfExists(file)) {
            pathCounts.getFileCounter().increment();
            pathCounts.getByteCounter().add(size);
         }
      } finally {
         if (posixFileAttributes != null) {
            Files.setPosixFilePermissions(parent, posixFileAttributes.permissions());
         }
      }

      return pathCounts;
   }

   public static void deleteOnExit(Path path) {
      Objects.requireNonNull(path).toFile().deleteOnExit();
   }

   public static boolean directoryAndFileContentEquals(Path path1, Path path2) throws IOException {
      return directoryAndFileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
   }

   public static boolean directoryAndFileContentEquals(
      Path path1, Path path2, LinkOption[] linkOptions, OpenOption[] openOptions, FileVisitOption[] fileVisitOption
   ) throws IOException {
      if (path1 == null && path2 == null) {
         return true;
      }

      if (path1 != null && path2 != null) {
         if (notExists(path1) && notExists(path2)) {
            return true;
         }

         PathUtils.RelativeSortedPaths relativeSortedPaths = new PathUtils.RelativeSortedPaths(path1, path2, Integer.MAX_VALUE, linkOptions, fileVisitOption);
         if (!relativeSortedPaths.equals) {
            return false;
         }

         List<Path> fileList1 = relativeSortedPaths.relativeFileList1;
         List<Path> fileList2 = relativeSortedPaths.relativeFileList2;
         boolean sameFileSystem = isSameFileSystem(path1, path2);

         for (Path path : fileList1) {
            int binarySearch = sameFileSystem
               ? Collections.binarySearch(fileList2, path)
               : Collections.binarySearch(
                  fileList2, path, Comparator.comparing(p -> PathUtils.RelativeSortedPaths.extractKey(p.getFileSystem().getSeparator(), p.toString()))
               );
            if (binarySearch < 0) {
               throw new IllegalStateException("Unexpected mismatch.");
            }

            if (sameFileSystem && !fileContentEquals(path1.resolve(path), path2.resolve(path), linkOptions, openOptions)) {
               return false;
            }

            if (!fileContentEquals(path1.resolve(path.toString()), path2.resolve(path.toString()), linkOptions, openOptions)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean directoryContentEquals(Path path1, Path path2) throws IOException {
      return directoryContentEquals(path1, path2, Integer.MAX_VALUE, EMPTY_LINK_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
   }

   public static boolean directoryContentEquals(Path path1, Path path2, int maxDepth, LinkOption[] linkOptions, FileVisitOption[] fileVisitOptions) throws IOException {
      return (new PathUtils.RelativeSortedPaths(path1, path2, maxDepth, linkOptions, fileVisitOptions)).equals;
   }

   private static boolean exists(Path path, LinkOption... options) {
      return path != null && (options != null ? Files.exists(path, options) : Files.exists(path));
   }

   public static boolean fileContentEquals(Path path1, Path path2) throws IOException {
      return fileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY);
   }

   public static boolean fileContentEquals(Path path1, Path path2, LinkOption[] linkOptions, OpenOption[] openOptions) throws IOException {
      if (path1 == null && path2 == null) {
         return true;
      }

      if (path1 != null && path2 != null) {
         Path nPath1 = path1.normalize();
         Path nPath2 = path2.normalize();
         boolean path1Exists = exists(nPath1, linkOptions);
         if (path1Exists != exists(nPath2, linkOptions)) {
            return false;
         }

         if (!path1Exists) {
            return true;
         }

         if (Files.isDirectory(nPath1, linkOptions)) {
            throw new IOException("Can't compare directories, only files: " + nPath1);
         }

         if (Files.isDirectory(nPath2, linkOptions)) {
            throw new IOException("Can't compare directories, only files: " + nPath2);
         }

         if (Files.size(nPath1) != Files.size(nPath2)) {
            return false;
         }

         if (isSameFileSystem(path1, path2) && path1.equals(path2)) {
            return true;
         }

         try {
            RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(path1.toRealPath(linkOptions));

            boolean var22;
            try {
               RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(path2.toRealPath(linkOptions));

               try {
                  var22 = RandomAccessFiles.contentEquals(raf1, raf2);
               } catch (Throwable var18) {
                  if (raf2 != null) {
                     try {
                        raf2.close();
                     } catch (Throwable var15) {
                        var18.addSuppressed(var15);
                     }
                  }

                  throw var18;
               }

               if (raf2 != null) {
                  raf2.close();
               }
            } catch (Throwable var19) {
               if (raf1 != null) {
                  try {
                     raf1.close();
                  } catch (Throwable var14) {
                     var19.addSuppressed(var14);
                  }
               }

               throw var19;
            }

            if (raf1 != null) {
               raf1.close();
            }

            return var22;
         } catch (UnsupportedOperationException e) {
            InputStream inputStream1 = Files.newInputStream(nPath1, openOptions);

            boolean var10;
            try {
               InputStream inputStream2 = Files.newInputStream(nPath2, openOptions);

               try {
                  var10 = IOUtils.contentEquals(inputStream1, inputStream2);
               } catch (Throwable var16) {
                  if (inputStream2 != null) {
                     try {
                        inputStream2.close();
                     } catch (Throwable var13) {
                        var16.addSuppressed(var13);
                     }
                  }

                  throw var16;
               }

               if (inputStream2 != null) {
                  inputStream2.close();
               }
            } catch (Throwable var17) {
               if (inputStream1 != null) {
                  try {
                     inputStream1.close();
                  } catch (Throwable var12) {
                     var17.addSuppressed(var12);
                  }
               }

               throw var17;
            }

            if (inputStream1 != null) {
               inputStream1.close();
            }

            return var10;
         }
      } else {
         return false;
      }
   }

   public static Path[] filter(PathFilter filter, Path... paths) {
      Objects.requireNonNull(filter, "filter");
      return paths == null ? EMPTY_PATH_ARRAY : filterPaths(filter, Stream.of(paths), Collectors.toList()).toArray(EMPTY_PATH_ARRAY);
   }

   private static <R, A> R filterPaths(PathFilter filter, Stream<Path> stream, Collector<? super Path, A, R> collector) {
      Objects.requireNonNull(filter, "filter");
      Objects.requireNonNull(collector, "collector");
      return stream == null ? Stream.<Path>empty().collect(collector) : stream.filter(p -> {
         try {
            return p != null && filter.accept(p, readBasicFileAttributes(p)) == FileVisitResult.CONTINUE;
         } catch (IOException e) {
            return false;
         }
      }).collect(collector);
   }

   public static List<AclEntry> getAclEntryList(Path sourcePath) throws IOException {
      AclFileAttributeView fileAttributeView = getAclFileAttributeView(sourcePath);
      return fileAttributeView == null ? null : fileAttributeView.getAcl();
   }

   public static AclFileAttributeView getAclFileAttributeView(Path path, LinkOption... options) {
      return Files.getFileAttributeView(path, AclFileAttributeView.class, options);
   }

   public static String getBaseName(Path path) {
      if (path == null) {
         return null;
      }

      Path fileName = path.getFileName();
      return fileName != null ? FilenameUtils.removeExtension(fileName.toString()) : null;
   }

   public static DosFileAttributeView getDosFileAttributeView(Path path, LinkOption... options) {
      return Files.getFileAttributeView(path, DosFileAttributeView.class, options);
   }

   public static String getExtension(Path path) {
      String fileName = getFileNameString(path);
      return fileName != null ? FilenameUtils.getExtension(fileName) : null;
   }

   public static <R> R getFileName(Path path, Function<Path, R> function) {
      Path fileName = path != null ? path.getFileName() : null;
      return fileName != null ? function.apply(fileName) : null;
   }

   public static String getFileNameString(Path path) {
      return getFileName(path, Path::toString);
   }

   public static FileTime getLastModifiedFileTime(File file) throws IOException {
      return getLastModifiedFileTime(file.toPath(), null, EMPTY_LINK_OPTION_ARRAY);
   }

   public static FileTime getLastModifiedFileTime(Path path, FileTime defaultIfAbsent, LinkOption... options) throws IOException {
      return Files.exists(path) ? getLastModifiedTime(path, options) : defaultIfAbsent;
   }

   public static FileTime getLastModifiedFileTime(Path path, LinkOption... options) throws IOException {
      return getLastModifiedFileTime(path, null, options);
   }

   public static FileTime getLastModifiedFileTime(URI uri) throws IOException {
      return getLastModifiedFileTime(Paths.get(uri), null, EMPTY_LINK_OPTION_ARRAY);
   }

   public static FileTime getLastModifiedFileTime(URL url) throws IOException, URISyntaxException {
      return getLastModifiedFileTime(url.toURI());
   }

   private static FileTime getLastModifiedTime(Path path, LinkOption... options) throws IOException {
      return Files.getLastModifiedTime(Objects.requireNonNull(path, "path"), options);
   }

   private static Path getParent(Path path) {
      return path == null ? null : path.getParent();
   }

   public static PosixFileAttributeView getPosixFileAttributeView(Path path, LinkOption... options) {
      return Files.getFileAttributeView(path, PosixFileAttributeView.class, options);
   }

   public static Path getTempDirectory() {
      return Paths.get(FileUtils.getTempDirectoryPath());
   }

   public static boolean isDirectory(Path path, LinkOption... options) {
      return path != null && Files.isDirectory(path, options);
   }

   public static boolean isEmpty(Path path) throws IOException {
      return Files.isDirectory(path) ? isEmptyDirectory(path) : isEmptyFile(path);
   }

   public static boolean isEmptyDirectory(Path directory) throws IOException {
      DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);

      boolean var2;
      try {
         var2 = !directoryStream.iterator().hasNext();
      } catch (Throwable var5) {
         if (directoryStream != null) {
            try {
               directoryStream.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (directoryStream != null) {
         directoryStream.close();
      }

      return var2;
   }

   public static boolean isEmptyFile(Path file) throws IOException {
      return Files.size(file) <= 0L;
   }

   public static boolean isNewer(Path file, ChronoZonedDateTime<?> czdt, LinkOption... options) throws IOException {
      Objects.requireNonNull(czdt, "czdt");
      return isNewer(file, czdt.toInstant(), options);
   }

   public static boolean isNewer(Path file, FileTime fileTime, LinkOption... options) throws IOException {
      return notExists(file) ? false : compareLastModifiedTimeTo(file, fileTime, options) > 0;
   }

   public static boolean isNewer(Path file, Instant instant, LinkOption... options) throws IOException {
      return isNewer(file, FileTime.from(instant), options);
   }

   public static boolean isNewer(Path file, long timeMillis, LinkOption... options) throws IOException {
      return isNewer(file, FileTime.fromMillis(timeMillis), options);
   }

   public static boolean isNewer(Path file, Path reference) throws IOException {
      return isNewer(file, getLastModifiedTime(reference));
   }

   public static boolean isOlder(Path file, FileTime fileTime, LinkOption... options) throws IOException {
      return notExists(file) ? false : compareLastModifiedTimeTo(file, fileTime, options) < 0;
   }

   public static boolean isOlder(Path file, Instant instant, LinkOption... options) throws IOException {
      return isOlder(file, FileTime.from(instant), options);
   }

   public static boolean isOlder(Path file, long timeMillis, LinkOption... options) throws IOException {
      return isOlder(file, FileTime.fromMillis(timeMillis), options);
   }

   public static boolean isOlder(Path file, Path reference) throws IOException {
      return isOlder(file, getLastModifiedTime(reference));
   }

   public static boolean isPosix(Path test, LinkOption... options) {
      return exists(test, options) && readPosixFileAttributes(test, options) != null;
   }

   public static boolean isRegularFile(Path path, LinkOption... options) {
      return path != null && Files.isRegularFile(path, options);
   }

   static boolean isSameFileSystem(Path path1, Path path2) {
      return path1.getFileSystem() == path2.getFileSystem();
   }

   public static DirectoryStream<Path> newDirectoryStream(Path dir, PathFilter pathFilter) throws IOException {
      return Files.newDirectoryStream(dir, new DirectoryStreamFilter(pathFilter));
   }

   public static OutputStream newOutputStream(Path path, boolean append) throws IOException {
      return newOutputStream(path, EMPTY_LINK_OPTION_ARRAY, append ? OPEN_OPTIONS_APPEND : OPEN_OPTIONS_TRUNCATE);
   }

   static OutputStream newOutputStream(Path path, LinkOption[] linkOptions, OpenOption... openOptions) throws IOException {
      if (!exists(path, linkOptions)) {
         createParentDirectories(path, linkOptions != null && linkOptions.length > 0 ? linkOptions[0] : NULL_LINK_OPTION);
      }

      List<OpenOption> list = new ArrayList<>(Arrays.asList(openOptions != null ? openOptions : EMPTY_OPEN_OPTION_ARRAY));
      list.addAll(Arrays.asList(linkOptions != null ? linkOptions : EMPTY_LINK_OPTION_ARRAY));
      return Files.newOutputStream(path, list.toArray(EMPTY_OPEN_OPTION_ARRAY));
   }

   public static LinkOption[] noFollowLinkOptionArray() {
      return (LinkOption[])NOFOLLOW_LINK_OPTION_ARRAY.clone();
   }

   private static boolean notExists(Path path, LinkOption... options) {
      return Files.notExists(Objects.requireNonNull(path, "path"), options);
   }

   private static boolean overrideReadOnly(DeleteOption... deleteOptions) {
      return deleteOptions == null ? false : Stream.of(deleteOptions).anyMatch(e -> e == StandardDeleteOption.OVERRIDE_READ_ONLY);
   }

   public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) {
      try {
         return path == null ? null : Files.readAttributes(path, type, options);
      } catch (UnsupportedOperationException | IOException e) {
         return null;
      }
   }

   public static BasicFileAttributes readBasicFileAttributes(Path path) throws IOException {
      return Files.readAttributes(path, BasicFileAttributes.class);
   }

   public static BasicFileAttributes readBasicFileAttributes(Path path, LinkOption... options) {
      return readAttributes(path, BasicFileAttributes.class, options);
   }

   @Deprecated
   public static BasicFileAttributes readBasicFileAttributesUnchecked(Path path) {
      return readBasicFileAttributes(path, EMPTY_LINK_OPTION_ARRAY);
   }

   public static DosFileAttributes readDosFileAttributes(Path path, LinkOption... options) {
      return readAttributes(path, DosFileAttributes.class, options);
   }

   private static Path readIfSymbolicLink(Path path) throws IOException {
      return path != null ? (Files.isSymbolicLink(path) ? Files.readSymbolicLink(path) : path) : null;
   }

   public static BasicFileAttributes readOsFileAttributes(Path path, LinkOption... options) {
      PosixFileAttributes fileAttributes = readPosixFileAttributes(path, options);
      return fileAttributes != null ? fileAttributes : readDosFileAttributes(path, options);
   }

   public static PosixFileAttributes readPosixFileAttributes(Path path, LinkOption... options) {
      return readAttributes(path, PosixFileAttributes.class, options);
   }

   public static String readString(Path path, Charset charset) throws IOException {
      return new String(Files.readAllBytes(path), Charsets.toCharset(charset));
   }

   static List<Path> relativize(Collection<Path> collection, Path parent, boolean sort, Comparator<? super Path> comparator) {
      Stream<Path> stream = collection.stream().map(parent::relativize);
      if (sort) {
         stream = comparator == null ? stream.sorted() : stream.sorted(comparator);
      }

      return stream.collect(Collectors.toList());
   }

   private static Path requireExists(Path file, String fileParamName, LinkOption... options) {
      Objects.requireNonNull(file, fileParamName);
      if (!exists(file, options)) {
         throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
      } else {
         return file;
      }
   }

   static Path resolve(Path targetDirectory, Path otherPath) {
      FileSystem fileSystemTarget = targetDirectory.getFileSystem();
      FileSystem fileSystemSource = otherPath.getFileSystem();
      if (fileSystemTarget == fileSystemSource) {
         return targetDirectory.resolve(otherPath);
      }

      String separatorSource = fileSystemSource.getSeparator();
      String separatorTarget = fileSystemTarget.getSeparator();
      String otherString = otherPath.toString();
      return targetDirectory.resolve(Objects.equals(separatorSource, separatorTarget) ? otherString : otherString.replace(separatorSource, separatorTarget));
   }

   private static boolean setDosReadOnly(Path path, boolean readOnly, LinkOption... linkOptions) throws IOException {
      DosFileAttributeView dosFileAttributeView = getDosFileAttributeView(path, linkOptions);
      if (dosFileAttributeView != null) {
         dosFileAttributeView.setReadOnly(readOnly);
         return true;
      } else {
         return false;
      }
   }

   public static void setLastModifiedTime(Path sourceFile, Path targetFile) throws IOException {
      Objects.requireNonNull(sourceFile, "sourceFile");
      Files.setLastModifiedTime(targetFile, getLastModifiedTime(sourceFile));
   }

   private static boolean setPosixDeletePermissions(Path parent, boolean enableDeleteChildren, LinkOption... linkOptions) throws IOException {
      return setPosixPermissions(parent, enableDeleteChildren, Arrays.asList(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE), linkOptions);
   }

   private static boolean setPosixPermissions(Path path, boolean addPermissions, List<PosixFilePermission> updatePermissions, LinkOption... linkOptions) throws IOException {
      if (path != null) {
         Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, linkOptions);
         Set<PosixFilePermission> newPermissions = new HashSet<>(permissions);
         if (addPermissions) {
            newPermissions.addAll(updatePermissions);
         } else {
            newPermissions.removeAll(updatePermissions);
         }

         if (!newPermissions.equals(permissions)) {
            Files.setPosixFilePermissions(path, newPermissions);
         }

         return true;
      } else {
         return false;
      }
   }

   private static void setPosixReadOnlyFile(Path path, boolean readOnly, LinkOption... linkOptions) throws IOException {
      Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, linkOptions);
      List<PosixFilePermission> readPermissions = Arrays.asList(PosixFilePermission.OWNER_READ);
      List<PosixFilePermission> writePermissions = Arrays.asList(PosixFilePermission.OWNER_WRITE);
      if (readOnly) {
         permissions.addAll(readPermissions);
         permissions.removeAll(writePermissions);
      } else {
         permissions.addAll(readPermissions);
         permissions.addAll(writePermissions);
      }

      Files.setPosixFilePermissions(path, permissions);
   }

   public static Path setReadOnly(Path path, boolean readOnly, LinkOption... linkOptions) throws IOException {
      try {
         if (setDosReadOnly(path, readOnly, linkOptions)) {
            return path;
         }
      } catch (IOException var4) {
      }

      Path parent = getParent(path);
      if (!isPosix(parent, linkOptions)) {
         throw new IOException(String.format("DOS or POSIX file operations not available for '%s', linkOptions %s", path, Arrays.toString(linkOptions)));
      }

      if (readOnly) {
         setPosixReadOnlyFile(path, readOnly, linkOptions);
         setPosixDeletePermissions(parent, false, linkOptions);
      } else {
         setPosixDeletePermissions(parent, true, linkOptions);
      }

      return path;
   }

   public static long sizeOf(Path path) throws IOException {
      requireExists(path, "path");
      return Files.isDirectory(path) ? sizeOfDirectory(path) : Files.size(path);
   }

   public static BigInteger sizeOfAsBigInteger(Path path) throws IOException {
      requireExists(path, "path");
      return Files.isDirectory(path) ? sizeOfDirectoryAsBigInteger(path) : BigInteger.valueOf(Files.size(path));
   }

   public static long sizeOfDirectory(Path directory) throws IOException {
      return countDirectory(directory).getByteCounter().getLong();
   }

   public static BigInteger sizeOfDirectoryAsBigInteger(Path directory) throws IOException {
      return countDirectoryAsBigInteger(directory).getByteCounter().getBigInteger();
   }

   private static Path stripTrailingSeparator(Path dir) {
      String separator = dir.getFileSystem().getSeparator();
      String fileName = getFileNameString(dir);
      return fileName != null && fileName.endsWith(separator) ? dir.resolveSibling(fileName.substring(0, fileName.length() - 1)) : dir;
   }

   static Set<FileVisitOption> toFileVisitOptionSet(FileVisitOption... fileVisitOptions) {
      return fileVisitOptions == null ? EnumSet.noneOf(FileVisitOption.class) : Stream.of(fileVisitOptions).collect(Collectors.toSet());
   }

   private static <T> List<T> toList(Iterable<T> iterable) {
      return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
   }

   private static List<Path> toSortedList(Iterable<Path> rootDirectories) {
      List<Path> list = toList(rootDirectories);
      Collections.sort(list);
      return list;
   }

   public static Path touch(Path file) throws IOException {
      Objects.requireNonNull(file, "file");
      if (!Files.exists(file)) {
         createParentDirectories(file);
         Files.createFile(file);
      } else {
         FileTimes.setLastModifiedTime(file);
      }

      return file;
   }

   public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, Path directory) throws IOException {
      Files.walkFileTree(directory, visitor);
      return visitor;
   }

   public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, Path start, Set<FileVisitOption> options, int maxDepth) throws IOException {
      Files.walkFileTree(start, options, maxDepth, visitor);
      return visitor;
   }

   public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, String first, String... more) throws IOException {
      return visitFileTree(visitor, Paths.get(first, more));
   }

   public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, URI uri) throws IOException {
      return visitFileTree(visitor, Paths.get(uri));
   }

   public static boolean waitFor(Path file, Duration timeout, LinkOption... options) {
      Objects.requireNonNull(file, "file");
      Instant finishInstant = Instant.now().plus(timeout);
      boolean interrupted = false;
      long minSleepMillis = 100L;

      try {
         while (!exists(file, options)) {
            Instant now = Instant.now();
            if (now.isAfter(finishInstant)) {
               return false;
            }

            try {
               ThreadUtils.sleep(Duration.ofMillis(Math.min(100L, finishInstant.minusMillis(now.toEpochMilli()).toEpochMilli())));
            } catch (InterruptedException ignore) {
               interrupted = true;
            } catch (Exception ex) {
               break;
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }

      return exists(file, options);
   }

   public static Stream<Path> walk(Path start, PathFilter pathFilter, int maxDepth, boolean readAttributes, FileVisitOption... options) throws IOException {
      return Files.walk(start, maxDepth, options)
         .filter(path -> pathFilter.accept(path, readAttributes ? readBasicFileAttributesUnchecked(path) : null) == FileVisitResult.CONTINUE);
   }

   private static <R> R withPosixFileAttributes(Path path, LinkOption[] linkOptions, boolean overrideReadOnly, IOFunction<PosixFileAttributes, R> function) throws IOException {
      PosixFileAttributes posixFileAttributes = overrideReadOnly ? readPosixFileAttributes(path, linkOptions) : null;

      try {
         return function.apply(posixFileAttributes);
      } finally {
         if (posixFileAttributes != null && path != null && Files.exists(path, linkOptions)) {
            Files.setPosixFilePermissions(path, posixFileAttributes.permissions());
         }
      }
   }

   public static Path writeString(Path path, CharSequence charSequence, Charset charset, OpenOption... openOptions) throws IOException {
      Objects.requireNonNull(path, "path");
      Objects.requireNonNull(charSequence, "charSequence");
      Files.write(path, String.valueOf(charSequence).getBytes(Charsets.toCharset(charset)), openOptions);
      return path;
   }

   private PathUtils() {
   }

   private static final class RelativeSortedPaths {
      final boolean equals;
      final List<Path> relativeFileList1;
      final List<Path> relativeFileList2;

      private static boolean equalsIgnoreFileSystem(List<Path> list1, List<Path> list2) {
         if (list1.size() != list2.size()) {
            return false;
         }

         Iterator<Path> iterator1 = list1.iterator();
         Iterator<Path> iterator2 = list2.iterator();

         while (iterator1.hasNext() && iterator2.hasNext()) {
            if (!equalsIgnoreFileSystem(iterator1.next(), iterator2.next())) {
               return false;
            }
         }

         return true;
      }

      private static boolean equalsIgnoreFileSystem(Path path1, Path path2) {
         FileSystem fileSystem1 = path1.getFileSystem();
         FileSystem fileSystem2 = path2.getFileSystem();
         if (fileSystem1 == fileSystem2) {
            return path1.equals(path2);
         }

         String separator1 = fileSystem1.getSeparator();
         String separator2 = fileSystem2.getSeparator();
         String string1 = path1.toString();
         String string2 = path2.toString();
         return Objects.equals(separator1, separator2)
            ? Objects.equals(string1, string2)
            : extractKey(separator1, string1).equals(extractKey(separator2, string2));
      }

      static String extractKey(String separator, String string) {
         return string.replaceAll("\\" + separator, ">");
      }

      private RelativeSortedPaths(Path dir1, Path dir2, int maxDepth, LinkOption[] linkOptions, FileVisitOption[] fileVisitOptions) throws IOException {
         List<Path> tmpRelativeFileList1 = null;
         List<Path> tmpRelativeFileList2 = null;
         if (dir1 == null && dir2 == null) {
            this.equals = true;
         } else if (dir1 == null ^ dir2 == null) {
            this.equals = false;
         } else {
            boolean parentDirNotExists1 = Files.notExists(dir1, linkOptions);
            boolean parentDirNotExists2 = Files.notExists(dir2, linkOptions);
            if (!parentDirNotExists1 && !parentDirNotExists2) {
               AccumulatorPathVisitor visitor1 = PathUtils.accumulate(dir1, maxDepth, fileVisitOptions);
               AccumulatorPathVisitor visitor2 = PathUtils.accumulate(dir2, maxDepth, fileVisitOptions);
               if (visitor1.getDirList().size() == visitor2.getDirList().size() && visitor1.getFileList().size() == visitor2.getFileList().size()) {
                  List<Path> tmpRelativeDirList1 = visitor1.relativizeDirectories(dir1, true, null);
                  List<Path> tmpRelativeDirList2 = visitor2.relativizeDirectories(dir2, true, null);
                  if (!equalsIgnoreFileSystem(tmpRelativeDirList1, tmpRelativeDirList2)) {
                     this.equals = false;
                  } else {
                     tmpRelativeFileList1 = visitor1.relativizeFiles(dir1, true, null);
                     tmpRelativeFileList2 = visitor2.relativizeFiles(dir2, true, null);
                     this.equals = equalsIgnoreFileSystem(tmpRelativeFileList1, tmpRelativeFileList2);
                  }
               } else {
                  this.equals = false;
               }
            } else {
               this.equals = parentDirNotExists1 && parentDirNotExists2;
            }
         }

         this.relativeFileList1 = tmpRelativeFileList1;
         this.relativeFileList2 = tmpRelativeFileList2;
      }
   }
}
