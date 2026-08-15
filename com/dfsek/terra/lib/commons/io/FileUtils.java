package com.dfsek.terra.lib.commons.io;

import com.dfsek.terra.lib.commons.io.file.AccumulatorPathVisitor;
import com.dfsek.terra.lib.commons.io.file.Counters;
import com.dfsek.terra.lib.commons.io.file.PathFilter;
import com.dfsek.terra.lib.commons.io.file.PathUtils;
import com.dfsek.terra.lib.commons.io.file.StandardDeleteOption;
import com.dfsek.terra.lib.commons.io.filefilter.FileEqualsFileFilter;
import com.dfsek.terra.lib.commons.io.filefilter.FileFileFilter;
import com.dfsek.terra.lib.commons.io.filefilter.IOFileFilter;
import com.dfsek.terra.lib.commons.io.filefilter.SuffixFileFilter;
import com.dfsek.terra.lib.commons.io.filefilter.TrueFileFilter;
import com.dfsek.terra.lib.commons.io.function.IOConsumer;
import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

public class FileUtils {
   private static final String PROTOCOL_FILE = "file";
   public static final long ONE_KB = 1024L;
   public static final BigInteger ONE_KB_BI = BigInteger.valueOf(1024L);
   public static final long ONE_MB = 1048576L;
   public static final BigInteger ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);
   public static final long ONE_GB = 1073741824L;
   public static final BigInteger ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);
   public static final long ONE_TB = 1099511627776L;
   public static final BigInteger ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);
   public static final long ONE_PB = 1125899906842624L;
   public static final BigInteger ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);
   public static final long ONE_EB = 1152921504606846976L;
   public static final BigInteger ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);
   public static final BigInteger ONE_ZB = BigInteger.valueOf(1024L).multiply(BigInteger.valueOf(1152921504606846976L));
   public static final BigInteger ONE_YB = ONE_KB_BI.multiply(ONE_ZB);
   public static final File[] EMPTY_FILE_ARRAY = new File[0];

   public static String byteCountToDisplaySize(BigInteger size) {
      Objects.requireNonNull(size, "size");
      String displaySize;
      if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_EB_BI) + " EB";
      } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_PB_BI) + " PB";
      } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_TB_BI) + " TB";
      } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_GB_BI) + " GB";
      } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_MB_BI) + " MB";
      } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_KB_BI) + " KB";
      } else {
         displaySize = size + " bytes";
      }

      return displaySize;
   }

   public static String byteCountToDisplaySize(long size) {
      return byteCountToDisplaySize(BigInteger.valueOf(size));
   }

   public static String byteCountToDisplaySize(Number size) {
      return byteCountToDisplaySize(size.longValue());
   }

   private static void checkExists(File file, boolean strict) throws FileNotFoundException {
      Objects.requireNonNull(file, "file");
      if (strict && !file.exists()) {
         throw new FileNotFoundException(file.toString());
      }
   }

   private static void checkFileExists(File file, String name) throws FileNotFoundException {
      Objects.requireNonNull(file, name);
      if (!file.isFile()) {
         if (file.exists()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
         }

         if (!Files.isSymbolicLink(file.toPath())) {
            throw new FileNotFoundException("Source '" + file + "' does not exist");
         }
      }
   }

   private static File checkIsFile(File file, String name) {
      if (file.isFile()) {
         return file;
      } else {
         throw new IllegalArgumentException(String.format("Parameter '%s' is not a file: %s", name, file));
      }
   }

   public static Checksum checksum(File file, Checksum checksum) throws IOException {
      checkFileExists(file, "file");
      Objects.requireNonNull(checksum, "checksum");
      InputStream inputStream = new CheckedInputStream(Files.newInputStream(file.toPath()), checksum);

      try {
         IOUtils.consume(inputStream);
      } catch (Throwable var6) {
         try {
            inputStream.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      inputStream.close();
      return checksum;
   }

   public static long checksumCRC32(File file) throws IOException {
      return checksum(file, new CRC32()).getValue();
   }

   public static void cleanDirectory(File directory) throws IOException {
      IOConsumer.forAll(f -> forceDelete(f, false), listFiles(directory, null));
   }

   private static void cleanDirectoryOnExit(File directory) throws IOException {
      IOConsumer.forAll(FileUtils::forceDeleteOnExit, listFiles(directory, null));
   }

   public static boolean contentEquals(File file1, File file2) throws IOException {
      if (file1 == null && file2 == null) {
         return true;
      }

      if (file1 != null && file2 != null) {
         boolean file1Exists = file1.exists();
         if (file1Exists != file2.exists()) {
            return false;
         } else if (!file1Exists) {
            return true;
         } else {
            checkIsFile(file1, "file1");
            checkIsFile(file2, "file2");
            if (file1.length() != file2.length()) {
               return false;
            } else {
               return file1.getCanonicalFile().equals(file2.getCanonicalFile()) ? true : PathUtils.fileContentEquals(file1.toPath(), file2.toPath());
            }
         }
      } else {
         return false;
      }
   }

   public static boolean contentEqualsIgnoreEOL(File file1, File file2, String charsetName) throws IOException {
      if (file1 == null && file2 == null) {
         return true;
      }

      if (file1 != null && file2 != null) {
         boolean file1Exists = file1.exists();
         if (file1Exists != file2.exists()) {
            return false;
         }

         if (!file1Exists) {
            return true;
         }

         checkFileExists(file1, "file1");
         checkFileExists(file2, "file2");
         if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
            return true;
         }

         Charset charset = Charsets.toCharset(charsetName);
         Reader input1 = new InputStreamReader(Files.newInputStream(file1.toPath()), charset);

         boolean var7;
         try {
            Reader input2 = new InputStreamReader(Files.newInputStream(file2.toPath()), charset);

            try {
               var7 = IOUtils.contentEqualsIgnoreEOL(input1, input2);
            } catch (Throwable var11) {
               try {
                  input2.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }

               throw var11;
            }

            input2.close();
         } catch (Throwable var12) {
            try {
               input1.close();
            } catch (Throwable var9) {
               var12.addSuppressed(var9);
            }

            throw var12;
         }

         input1.close();
         return var7;
      } else {
         return false;
      }
   }

   public static File[] convertFileCollectionToFileArray(Collection<File> files) {
      return files.toArray(EMPTY_FILE_ARRAY);
   }

   public static void copyDirectory(File srcDir, File destDir) throws IOException {
      copyDirectory(srcDir, destDir, true);
   }

   public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
      copyDirectory(srcDir, destDir, null, preserveFileDate);
   }

   public static void copyDirectory(File srcDir, File destDir, FileFilter filter) throws IOException {
      copyDirectory(srcDir, destDir, filter, true);
   }

   public static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate) throws IOException {
      copyDirectory(srcDir, destDir, filter, preserveFileDate, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);
   }

   public static void copyDirectory(File srcDir, File destDir, FileFilter fileFilter, boolean preserveFileDate, CopyOption... copyOptions) throws IOException {
      Objects.requireNonNull(destDir, "destination");
      requireDirectoryExists(srcDir, "srcDir");
      requireCanonicalPathsNotEquals(srcDir, destDir);
      List<String> exclusionList = null;
      String srcDirCanonicalPath = srcDir.getCanonicalPath();
      String destDirCanonicalPath = destDir.getCanonicalPath();
      if (destDirCanonicalPath.startsWith(srcDirCanonicalPath)) {
         File[] srcFiles = listFiles(srcDir, fileFilter);
         if (srcFiles.length > 0) {
            exclusionList = new ArrayList<>(srcFiles.length);

            for (File srcFile : srcFiles) {
               exclusionList.add(new File(destDir, srcFile.getName()).getCanonicalPath());
            }
         }
      }

      doCopyDirectory(srcDir, destDir, fileFilter, exclusionList, preserveFileDate, copyOptions);
   }

   public static void copyDirectoryToDirectory(File sourceDir, File destinationDir) throws IOException {
      Objects.requireNonNull(sourceDir, "sourceDir");
      requireDirectoryIfExists(destinationDir, "destinationDir");
      copyDirectory(sourceDir, new File(destinationDir, sourceDir.getName()), true);
   }

   public static void copyFile(File srcFile, File destFile) throws IOException {
      copyFile(srcFile, destFile, StandardCopyOption.REPLACE_EXISTING);
   }

   public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
      copyFile(srcFile, destFile, preserveFileDate, StandardCopyOption.REPLACE_EXISTING);
   }

   public static void copyFile(File srcFile, File destFile, boolean preserveFileDate, CopyOption... copyOptions) throws IOException {
      Objects.requireNonNull(destFile, "destination");
      checkFileExists(srcFile, "srcFile");
      requireCanonicalPathsNotEquals(srcFile, destFile);
      createParentDirectories(destFile);
      if (destFile.exists()) {
         checkFileExists(destFile, "destFile");
      }

      Path srcPath = srcFile.toPath();
      Files.copy(srcPath, destFile.toPath(), copyOptions);
      if (preserveFileDate && !Files.isSymbolicLink(srcPath) && !setTimes(srcFile, destFile)) {
         throw new IOException("Cannot set the file time.");
      }
   }

   public static void copyFile(File srcFile, File destFile, CopyOption... copyOptions) throws IOException {
      copyFile(srcFile, destFile, true, copyOptions);
   }

   public static long copyFile(File input, OutputStream output) throws IOException {
      InputStream fis = Files.newInputStream(input.toPath());

      long var3;
      try {
         var3 = IOUtils.copyLarge(fis, output);
      } catch (Throwable var6) {
         if (fis != null) {
            try {
               fis.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (fis != null) {
         fis.close();
      }

      return var3;
   }

   public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
      copyFileToDirectory(srcFile, destDir, true);
   }

   public static void copyFileToDirectory(File sourceFile, File destinationDir, boolean preserveFileDate) throws IOException {
      Objects.requireNonNull(sourceFile, "sourceFile");
      requireDirectoryIfExists(destinationDir, "destinationDir");
      copyFile(sourceFile, new File(destinationDir, sourceFile.getName()), preserveFileDate);
   }

   public static void copyInputStreamToFile(InputStream source, File destination) throws IOException {
      InputStream inputStream = source;

      try {
         copyToFile(inputStream, destination);
      } catch (Throwable var6) {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (inputStream != null) {
         inputStream.close();
      }
   }

   public static void copyToDirectory(File sourceFile, File destinationDir) throws IOException {
      Objects.requireNonNull(sourceFile, "sourceFile");
      if (sourceFile.isFile()) {
         copyFileToDirectory(sourceFile, destinationDir);
      } else {
         if (!sourceFile.isDirectory()) {
            throw new FileNotFoundException("The source " + sourceFile + " does not exist");
         }

         copyDirectoryToDirectory(sourceFile, destinationDir);
      }
   }

   public static void copyToDirectory(Iterable<File> sourceIterable, File destinationDir) throws IOException {
      Objects.requireNonNull(sourceIterable, "sourceIterable");

      for (File src : sourceIterable) {
         copyFileToDirectory(src, destinationDir);
      }
   }

   public static void copyToFile(InputStream inputStream, File file) throws IOException {
      OutputStream out = newOutputStream(file, false);

      try {
         IOUtils.copy(inputStream, out);
      } catch (Throwable var6) {
         if (out != null) {
            try {
               out.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (out != null) {
         out.close();
      }
   }

   public static void copyURLToFile(URL source, File destination) throws IOException {
      Path path = destination.toPath();
      PathUtils.createParentDirectories(path);
      PathUtils.copy(source::openStream, path, StandardCopyOption.REPLACE_EXISTING);
   }

   public static void copyURLToFile(URL source, File destination, int connectionTimeoutMillis, int readTimeoutMillis) throws IOException {
      CloseableURLConnection urlConnection = CloseableURLConnection.open(source);

      try {
         urlConnection.setConnectTimeout(connectionTimeoutMillis);
         urlConnection.setReadTimeout(readTimeoutMillis);
         InputStream stream = urlConnection.getInputStream();

         try {
            copyInputStreamToFile(stream, destination);
         } catch (Throwable var10) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (Throwable var11) {
         if (urlConnection != null) {
            try {
               urlConnection.close();
            } catch (Throwable var8) {
               var11.addSuppressed(var8);
            }
         }

         throw var11;
      }

      if (urlConnection != null) {
         urlConnection.close();
      }
   }

   public static File createParentDirectories(File file) throws IOException {
      return mkdirs(getParentFile(file));
   }

   public static File current() {
      return PathUtils.current().toFile();
   }

   static String decodeUrl(String url) {
      String decoded = url;
      if (url != null && url.indexOf(37) >= 0) {
         int n = url.length();
         StringBuilder builder = new StringBuilder();
         ByteBuffer byteBuffer = ByteBuffer.allocate(n);
         int i = 0;

         while (true) {
            while (true) {
               if (i >= n) {
                  decoded = builder.toString();
                  return decoded;
               }

               if (url.charAt(i) != '%') {
                  break;
               }

               try {
                  while (true) {
                     byte octet = (byte)Integer.parseInt(url.substring(i + 1, i + 3), 16);
                     byteBuffer.put(octet);
                     i += 3;
                     if (i >= n || url.charAt(i) != '%') {
                        break;
                     }
                  }
               } catch (IndexOutOfBoundsException | NumberFormatException var10) {
                  break;
               } finally {
                  if (byteBuffer.position() > 0) {
                     ((Buffer)byteBuffer).flip();
                     builder.append(StandardCharsets.UTF_8.decode(byteBuffer).toString());
                     ((Buffer)byteBuffer).clear();
                  }
               }
            }

            builder.append(url.charAt(i++));
         }
      } else {
         return decoded;
      }
   }

   public static File delete(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      Files.delete(file.toPath());
      return file;
   }

   public static void deleteDirectory(File directory) throws IOException {
      Objects.requireNonNull(directory, "directory");
      if (directory.exists()) {
         if (!isSymlink(directory)) {
            cleanDirectory(directory);
         }

         delete(directory);
      }
   }

   private static void deleteDirectoryOnExit(File directory) throws IOException {
      if (directory.exists()) {
         directory.deleteOnExit();
         if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
         }
      }
   }

   public static boolean deleteQuietly(File file) {
      if (file == null) {
         return false;
      }

      try {
         if (file.isDirectory()) {
            cleanDirectory(file);
         }
      } catch (Exception var3) {
      }

      try {
         return file.delete();
      } catch (Exception ignored) {
         return false;
      }
   }

   public static boolean directoryContains(File directory, File child) throws IOException {
      requireDirectoryExists(directory, "directory");
      return child != null && child.exists() ? FilenameUtils.directoryContains(directory.getCanonicalPath(), child.getCanonicalPath()) : false;
   }

   private static void doCopyDirectory(
      File srcDir, File destDir, FileFilter fileFilter, List<String> exclusionList, boolean preserveDirDate, CopyOption... copyOptions
   ) throws IOException {
      File[] srcFiles = listFiles(srcDir, fileFilter);
      requireDirectoryIfExists(destDir, "destDir");
      mkdirs(destDir);

      for (File srcFile : srcFiles) {
         File dstFile = new File(destDir, srcFile.getName());
         if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
            if (srcFile.isDirectory()) {
               doCopyDirectory(srcFile, dstFile, fileFilter, exclusionList, preserveDirDate, copyOptions);
            } else {
               copyFile(srcFile, dstFile, preserveDirDate, copyOptions);
            }
         }
      }

      if (preserveDirDate) {
         setTimes(srcDir, destDir);
      }
   }

   public static void forceDelete(File file) throws IOException {
      forceDelete(file, true);
   }

   private static void forceDelete(File file, boolean strict) throws IOException {
      checkExists(file, strict);

      Counters.PathCounters deleteCounters;
      try {
         deleteCounters = PathUtils.delete(file.toPath(), PathUtils.EMPTY_LINK_OPTION_ARRAY, StandardDeleteOption.OVERRIDE_READ_ONLY);
      } catch (NoSuchFileException e) {
         FileNotFoundException nioEx = new FileNotFoundException("Cannot delete file: " + file);
         nioEx.initCause(e);
         throw nioEx;
      } catch (IOException e) {
         throw new IOException("Cannot delete file: " + file, e);
      }

      if (deleteCounters.getFileCounter().get() < 1L && deleteCounters.getDirectoryCounter().get() < 1L) {
         throw new FileNotFoundException("File does not exist: " + file);
      }
   }

   public static void forceDeleteOnExit(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      if (file.isDirectory()) {
         deleteDirectoryOnExit(file);
      } else {
         file.deleteOnExit();
      }
   }

   public static void forceMkdir(File directory) throws IOException {
      mkdirs(directory);
   }

   public static void forceMkdirParent(File file) throws IOException {
      forceMkdir(getParentFile(Objects.requireNonNull(file, "file")));
   }

   public static File getFile(File directory, String... names) {
      Objects.requireNonNull(directory, "directory");
      Objects.requireNonNull(names, "names");
      File file = directory;

      for (String name : names) {
         file = new File(file, name);
      }

      return file;
   }

   public static File getFile(String... names) {
      Objects.requireNonNull(names, "names");
      File file = null;

      for (String name : names) {
         if (file == null) {
            file = new File(name);
         } else {
            file = new File(file, name);
         }
      }

      return file;
   }

   private static File getParentFile(File file) {
      return file == null ? null : file.getParentFile();
   }

   public static File getTempDirectory() {
      return new File(getTempDirectoryPath());
   }

   public static String getTempDirectoryPath() {
      return System.getProperty("java.io.tmpdir");
   }

   public static File getUserDirectory() {
      return new File(getUserDirectoryPath());
   }

   public static String getUserDirectoryPath() {
      return System.getProperty("user.home");
   }

   public static boolean isDirectory(File file, LinkOption... options) {
      return file != null && Files.isDirectory(file.toPath(), options);
   }

   public static boolean isEmptyDirectory(File directory) throws IOException {
      return PathUtils.isEmptyDirectory(directory.toPath());
   }

   public static boolean isFileNewer(File file, ChronoLocalDate chronoLocalDate) {
      return isFileNewer(file, chronoLocalDate, LocalTime.MAX);
   }

   public static boolean isFileNewer(File file, ChronoLocalDate chronoLocalDate, LocalTime localTime) {
      Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
      Objects.requireNonNull(localTime, "localTime");
      return isFileNewer(file, chronoLocalDate.atTime(localTime));
   }

   public static boolean isFileNewer(File file, ChronoLocalDate chronoLocalDate, OffsetTime offsetTime) {
      Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
      Objects.requireNonNull(offsetTime, "offsetTime");
      return isFileNewer(file, chronoLocalDate.atTime(offsetTime.toLocalTime()));
   }

   public static boolean isFileNewer(File file, ChronoLocalDateTime<?> chronoLocalDateTime) {
      return isFileNewer(file, chronoLocalDateTime, ZoneId.systemDefault());
   }

   public static boolean isFileNewer(File file, ChronoLocalDateTime<?> chronoLocalDateTime, ZoneId zoneId) {
      Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
      Objects.requireNonNull(zoneId, "zoneId");
      return isFileNewer(file, chronoLocalDateTime.atZone(zoneId));
   }

   public static boolean isFileNewer(File file, ChronoZonedDateTime<?> chronoZonedDateTime) {
      Objects.requireNonNull(file, "file");
      Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
      return Uncheck.getAsBoolean(() -> PathUtils.isNewer(file.toPath(), chronoZonedDateTime));
   }

   public static boolean isFileNewer(File file, Date date) {
      Objects.requireNonNull(date, "date");
      return isFileNewer(file, date.getTime());
   }

   public static boolean isFileNewer(File file, File reference) {
      return Uncheck.getAsBoolean(() -> PathUtils.isNewer(file.toPath(), reference.toPath()));
   }

   public static boolean isFileNewer(File file, FileTime fileTime) throws IOException {
      Objects.requireNonNull(file, "file");
      return PathUtils.isNewer(file.toPath(), fileTime);
   }

   public static boolean isFileNewer(File file, Instant instant) {
      Objects.requireNonNull(instant, "instant");
      return Uncheck.getAsBoolean(() -> PathUtils.isNewer(file.toPath(), instant));
   }

   public static boolean isFileNewer(File file, long timeMillis) {
      Objects.requireNonNull(file, "file");
      return Uncheck.getAsBoolean(() -> PathUtils.isNewer(file.toPath(), timeMillis));
   }

   public static boolean isFileNewer(File file, OffsetDateTime offsetDateTime) {
      Objects.requireNonNull(offsetDateTime, "offsetDateTime");
      return isFileNewer(file, offsetDateTime.toInstant());
   }

   public static boolean isFileOlder(File file, ChronoLocalDate chronoLocalDate) {
      return isFileOlder(file, chronoLocalDate, LocalTime.MAX);
   }

   public static boolean isFileOlder(File file, ChronoLocalDate chronoLocalDate, LocalTime localTime) {
      Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
      Objects.requireNonNull(localTime, "localTime");
      return isFileOlder(file, chronoLocalDate.atTime(localTime));
   }

   public static boolean isFileOlder(File file, ChronoLocalDate chronoLocalDate, OffsetTime offsetTime) {
      Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
      Objects.requireNonNull(offsetTime, "offsetTime");
      return isFileOlder(file, chronoLocalDate.atTime(offsetTime.toLocalTime()));
   }

   public static boolean isFileOlder(File file, ChronoLocalDateTime<?> chronoLocalDateTime) {
      return isFileOlder(file, chronoLocalDateTime, ZoneId.systemDefault());
   }

   public static boolean isFileOlder(File file, ChronoLocalDateTime<?> chronoLocalDateTime, ZoneId zoneId) {
      Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
      Objects.requireNonNull(zoneId, "zoneId");
      return isFileOlder(file, chronoLocalDateTime.atZone(zoneId));
   }

   public static boolean isFileOlder(File file, ChronoZonedDateTime<?> chronoZonedDateTime) {
      Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
      return isFileOlder(file, chronoZonedDateTime.toInstant());
   }

   public static boolean isFileOlder(File file, Date date) {
      Objects.requireNonNull(date, "date");
      return isFileOlder(file, date.getTime());
   }

   public static boolean isFileOlder(File file, File reference) throws FileNotFoundException {
      return Uncheck.getAsBoolean(() -> PathUtils.isOlder(file.toPath(), reference.toPath()));
   }

   public static boolean isFileOlder(File file, FileTime fileTime) throws IOException {
      Objects.requireNonNull(file, "file");
      return PathUtils.isOlder(file.toPath(), fileTime);
   }

   public static boolean isFileOlder(File file, Instant instant) {
      Objects.requireNonNull(instant, "instant");
      return Uncheck.getAsBoolean(() -> PathUtils.isOlder(file.toPath(), instant));
   }

   public static boolean isFileOlder(File file, long timeMillis) {
      Objects.requireNonNull(file, "file");
      return Uncheck.getAsBoolean(() -> PathUtils.isOlder(file.toPath(), timeMillis));
   }

   public static boolean isFileOlder(File file, OffsetDateTime offsetDateTime) {
      Objects.requireNonNull(offsetDateTime, "offsetDateTime");
      return isFileOlder(file, offsetDateTime.toInstant());
   }

   private static boolean isFileProtocol(URL url) {
      return "file".equalsIgnoreCase(url.getProtocol());
   }

   public static boolean isRegularFile(File file, LinkOption... options) {
      return file != null && Files.isRegularFile(file.toPath(), options);
   }

   public static boolean isSymlink(File file) {
      return file != null && Files.isSymbolicLink(file.toPath());
   }

   public static Iterator<File> iterateFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
      return listFiles(directory, fileFilter, dirFilter).iterator();
   }

   public static Iterator<File> iterateFiles(File directory, String[] extensions, boolean recursive) {
      return StreamIterator.iterator(Uncheck.get(() -> streamFiles(directory, recursive, extensions)));
   }

   public static Iterator<File> iterateFilesAndDirs(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
      return listFilesAndDirs(directory, fileFilter, dirFilter).iterator();
   }

   public static long lastModified(File file) throws IOException {
      return lastModifiedFileTime(file).toMillis();
   }

   public static FileTime lastModifiedFileTime(File file) throws IOException {
      return Files.getLastModifiedTime(Objects.requireNonNull(file, "file").toPath());
   }

   public static long lastModifiedUnchecked(File file) {
      return Uncheck.apply(FileUtils::lastModified, file);
   }

   public static LineIterator lineIterator(File file) throws IOException {
      return lineIterator(file, null);
   }

   public static LineIterator lineIterator(File file, String charsetName) throws IOException {
      InputStream inputStream = null;

      try {
         inputStream = Files.newInputStream(file.toPath());
         return IOUtils.lineIterator(inputStream, charsetName);
      } catch (IOException | RuntimeException ex) {
         IOUtils.closeQuietly(inputStream, ex::addSuppressed);
         throw ex;
      }
   }

   private static AccumulatorPathVisitor listAccumulate(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter, FileVisitOption... options) throws IOException {
      boolean isDirFilterSet = dirFilter != null;
      FileEqualsFileFilter rootDirFilter = new FileEqualsFileFilter(directory);
      PathFilter dirPathFilter = isDirFilterSet ? rootDirFilter.or(dirFilter) : rootDirFilter;
      AccumulatorPathVisitor visitor = ((AccumulatorPathVisitor.Builder)AccumulatorPathVisitor.builder()
            .setPathCounters(Counters.noopPathCounters())
            .setFileFilter(fileFilter)
            .setDirectoryFilter(dirPathFilter)
            .setVisitFileFailedFunction((p, e) -> FileVisitResult.CONTINUE))
         .get();
      Set<FileVisitOption> optionSet = new HashSet<>();
      if (options != null) {
         Collections.addAll(optionSet, options);
      }

      Files.walkFileTree(directory.toPath(), optionSet, toMaxDepth(isDirFilterSet), visitor);
      return visitor;
   }

   private static File[] listFiles(File directory, FileFilter fileFilter) throws IOException {
      requireDirectoryExists(directory, "directory");
      File[] files = directory.listFiles(fileFilter);
      if (files == null) {
         throw new IOException("Unknown I/O error listing contents of directory: " + directory);
      } else {
         return files;
      }
   }

   public static Collection<File> listFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
      AccumulatorPathVisitor visitor = Uncheck.apply(
         d -> listAccumulate(d, FileFileFilter.INSTANCE.and(fileFilter), dirFilter, FileVisitOption.FOLLOW_LINKS), directory
      );
      return toList(visitor.getFileList().stream().map(Path::toFile));
   }

   private static void listFiles(File directory, List<File> files, boolean recursive, FilenameFilter filter) {
      File[] listFiles = directory.listFiles();
      if (listFiles != null) {
         List<File> dirs = recursive ? new ArrayList<>() : null;
         Arrays.stream(listFiles).forEach(f -> {
            if (recursive && f.isDirectory()) {
               dirs.add(f);
            } else if (f.isFile() && filter.accept(directory, f.getName())) {
               files.add(f);
            }
         });
         if (recursive) {
            dirs.forEach(d -> listFiles(d, files, true, filter));
         }
      }
   }

   public static Collection<File> listFiles(File directory, String[] extensions, boolean recursive) {
      List<File> files = new ArrayList<>();
      FilenameFilter filter = extensions != null ? toSuffixFileFilter(extensions) : TrueFileFilter.INSTANCE;
      listFiles(directory, files, recursive, filter);
      return files;
   }

   public static Collection<File> listFilesAndDirs(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
      AccumulatorPathVisitor visitor = Uncheck.apply(d -> listAccumulate(d, fileFilter, dirFilter, FileVisitOption.FOLLOW_LINKS), directory);
      List<Path> list = visitor.getFileList();
      list.addAll(visitor.getDirList());
      return toList(list.stream().map(Path::toFile));
   }

   private static File mkdirs(File directory) throws IOException {
      if (directory != null && !directory.mkdirs() && !directory.isDirectory()) {
         throw new IOException("Cannot create directory '" + directory + "'.");
      } else {
         return directory;
      }
   }

   public static void moveDirectory(File srcDir, File destDir) throws IOException {
      Objects.requireNonNull(destDir, "destination");
      requireDirectoryExists(srcDir, "srcDir");
      requireAbsent(destDir, "destDir");
      if (!srcDir.renameTo(destDir)) {
         if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath() + File.separator)) {
            throw new IOException("Cannot move directory: " + srcDir + " to a subdirectory of itself: " + destDir);
         }

         copyDirectory(srcDir, destDir);
         deleteDirectory(srcDir);
         if (srcDir.exists()) {
            throw new IOException("Failed to delete original directory '" + srcDir + "' after copy to '" + destDir + "'");
         }
      }
   }

   public static void moveDirectoryToDirectory(File source, File destDir, boolean createDestDir) throws IOException {
      validateMoveParameters(source, destDir);
      if (!destDir.isDirectory()) {
         if (destDir.exists()) {
            throw new IOException("Destination '" + destDir + "' is not a directory");
         }

         if (!createDestDir) {
            throw new FileNotFoundException("Destination directory '" + destDir + "' does not exist [createDestDir=" + false + "]");
         }

         mkdirs(destDir);
      }

      moveDirectory(source, new File(destDir, source.getName()));
   }

   public static void moveFile(File srcFile, File destFile) throws IOException {
      moveFile(srcFile, destFile, StandardCopyOption.COPY_ATTRIBUTES);
   }

   public static void moveFile(File srcFile, File destFile, CopyOption... copyOptions) throws IOException {
      Objects.requireNonNull(destFile, "destination");
      checkFileExists(srcFile, "srcFile");
      requireAbsent(destFile, "destFile");
      boolean rename = srcFile.renameTo(destFile);
      if (!rename) {
         copyFile(srcFile, destFile, false, copyOptions);
         if (!srcFile.delete()) {
            deleteQuietly(destFile);
            throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
         }
      }
   }

   public static void moveFileToDirectory(File srcFile, File destDir, boolean createDestDir) throws IOException {
      validateMoveParameters(srcFile, destDir);
      if (!destDir.exists() && createDestDir) {
         mkdirs(destDir);
      }

      requireDirectoryExists(destDir, "destDir");
      moveFile(srcFile, new File(destDir, srcFile.getName()));
   }

   public static void moveToDirectory(File src, File destDir, boolean createDestDir) throws IOException {
      validateMoveParameters(src, destDir);
      if (src.isDirectory()) {
         moveDirectoryToDirectory(src, destDir, createDestDir);
      } else {
         moveFileToDirectory(src, destDir, createDestDir);
      }
   }

   public static OutputStream newOutputStream(File file, boolean append) throws IOException {
      return PathUtils.newOutputStream(Objects.requireNonNull(file, "file").toPath(), append);
   }

   public static FileInputStream openInputStream(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      return new FileInputStream(file);
   }

   public static FileOutputStream openOutputStream(File file) throws IOException {
      return openOutputStream(file, false);
   }

   public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
      Objects.requireNonNull(file, "file");
      if (file.exists()) {
         checkIsFile(file, "file");
      } else {
         createParentDirectories(file);
      }

      return new FileOutputStream(file, append);
   }

   public static byte[] readFileToByteArray(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      return Files.readAllBytes(file.toPath());
   }

   @Deprecated
   public static String readFileToString(File file) throws IOException {
      return readFileToString(file, Charset.defaultCharset());
   }

   public static String readFileToString(File file, Charset charsetName) throws IOException {
      return IOUtils.toString(() -> Files.newInputStream(file.toPath()), Charsets.toCharset(charsetName));
   }

   public static String readFileToString(File file, String charsetName) throws IOException {
      return readFileToString(file, Charsets.toCharset(charsetName));
   }

   @Deprecated
   public static List<String> readLines(File file) throws IOException {
      return readLines(file, Charset.defaultCharset());
   }

   public static List<String> readLines(File file, Charset charset) throws IOException {
      return Files.readAllLines(file.toPath(), charset);
   }

   public static List<String> readLines(File file, String charsetName) throws IOException {
      return readLines(file, Charsets.toCharset(charsetName));
   }

   private static void requireAbsent(File file, String name) throws FileExistsException {
      if (file.exists()) {
         throw new FileExistsException(String.format("File element in parameter '%s' already exists: '%s'", name, file));
      }
   }

   private static void requireCanonicalPathsNotEquals(File file1, File file2) throws IOException {
      String canonicalPath = file1.getCanonicalPath();
      if (canonicalPath.equals(file2.getCanonicalPath())) {
         throw new IllegalArgumentException(String.format("File canonical paths are equal: '%s' (file1='%s', file2='%s')", canonicalPath, file1, file2));
      }
   }

   private static void requireDirectoryExists(File directory, String name) throws FileNotFoundException {
      Objects.requireNonNull(directory, name);
      if (!directory.isDirectory()) {
         if (directory.exists()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
         } else {
            throw new FileNotFoundException("Directory '" + directory + "' does not exist.");
         }
      }
   }

   private static void requireDirectoryIfExists(File directory, String name) {
      Objects.requireNonNull(directory, name);
      if (directory.exists() && !directory.isDirectory()) {
         throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
      }
   }

   private static boolean setTimes(File sourceFile, File targetFile) {
      Objects.requireNonNull(sourceFile, "sourceFile");
      Objects.requireNonNull(targetFile, "targetFile");

      try {
         BasicFileAttributes srcAttr = Files.readAttributes(sourceFile.toPath(), BasicFileAttributes.class);
         BasicFileAttributeView destAttrView = Files.getFileAttributeView(targetFile.toPath(), BasicFileAttributeView.class);
         destAttrView.setTimes(srcAttr.lastModifiedTime(), srcAttr.lastAccessTime(), srcAttr.creationTime());
         return true;
      } catch (IOException ignored) {
         return targetFile.setLastModified(sourceFile.lastModified());
      }
   }

   public static long sizeOf(File file) {
      return Uncheck.getAsLong(() -> PathUtils.sizeOf(file.toPath()));
   }

   public static BigInteger sizeOfAsBigInteger(File file) {
      return Uncheck.get(() -> PathUtils.sizeOfAsBigInteger(file.toPath()));
   }

   public static long sizeOfDirectory(File directory) {
      try {
         requireDirectoryExists(directory, "directory");
      } catch (FileNotFoundException e) {
         throw new UncheckedIOException(e);
      }

      return Uncheck.getAsLong(() -> PathUtils.sizeOfDirectory(directory.toPath()));
   }

   public static BigInteger sizeOfDirectoryAsBigInteger(File directory) {
      try {
         requireDirectoryExists(directory, "directory");
      } catch (FileNotFoundException e) {
         throw new UncheckedIOException(e);
      }

      return Uncheck.get(() -> PathUtils.sizeOfDirectoryAsBigInteger(directory.toPath()));
   }

   public static Stream<File> streamFiles(File directory, boolean recursive, String... extensions) throws IOException {
      IOFileFilter filter = extensions == null ? FileFileFilter.INSTANCE : FileFileFilter.INSTANCE.and(toSuffixFileFilter(extensions));
      return PathUtils.walk(directory.toPath(), filter, toMaxDepth(recursive), false, FileVisitOption.FOLLOW_LINKS).map(Path::toFile);
   }

   public static File toFile(URL url) {
      if (url != null && isFileProtocol(url)) {
         String fileName = url.getFile().replace('/', File.separatorChar);
         return new File(decodeUrl(fileName));
      } else {
         return null;
      }
   }

   public static File[] toFiles(URL... urls) {
      if (IOUtils.length(urls) == 0) {
         return EMPTY_FILE_ARRAY;
      }

      File[] files = new File[urls.length];

      for (int i = 0; i < urls.length; i++) {
         URL url = urls[i];
         if (url != null) {
            if (!isFileProtocol(url)) {
               throw new IllegalArgumentException("Can only convert file URL to a File: " + url);
            }

            files[i] = toFile(url);
         }
      }

      return files;
   }

   private static List<File> toList(Stream<File> stream) {
      return stream.collect(Collectors.toList());
   }

   private static int toMaxDepth(boolean recursive) {
      return recursive ? Integer.MAX_VALUE : 1;
   }

   private static String[] toSuffixes(String... extensions) {
      return Stream.of(Objects.requireNonNull(extensions, "extensions")).map(s -> (String)(s.charAt(0) == '.' ? s : "." + s)).toArray(String[]::new);
   }

   private static SuffixFileFilter toSuffixFileFilter(String... extensions) {
      return new SuffixFileFilter(toSuffixes(extensions));
   }

   public static void touch(File file) throws IOException {
      PathUtils.touch(Objects.requireNonNull(file, "file").toPath());
   }

   public static URL[] toURLs(File... files) throws IOException {
      Objects.requireNonNull(files, "files");
      URL[] urls = new URL[files.length];

      for (int i = 0; i < urls.length; i++) {
         urls[i] = files[i].toURI().toURL();
      }

      return urls;
   }

   private static void validateMoveParameters(File source, File destination) throws FileNotFoundException {
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(destination, "destination");
      if (!source.exists()) {
         throw new FileNotFoundException("Source '" + source + "' does not exist");
      }
   }

   public static boolean waitFor(File file, int seconds) {
      Objects.requireNonNull(file, "file");
      return PathUtils.waitFor(file.toPath(), Duration.ofSeconds(seconds), PathUtils.EMPTY_LINK_OPTION_ARRAY);
   }

   @Deprecated
   public static void write(File file, CharSequence data) throws IOException {
      write(file, data, Charset.defaultCharset(), false);
   }

   @Deprecated
   public static void write(File file, CharSequence data, boolean append) throws IOException {
      write(file, data, Charset.defaultCharset(), append);
   }

   public static void write(File file, CharSequence data, Charset charset) throws IOException {
      write(file, data, charset, false);
   }

   public static void write(File file, CharSequence data, Charset charset, boolean append) throws IOException {
      writeStringToFile(file, Objects.toString(data, null), charset, append);
   }

   public static void write(File file, CharSequence data, String charsetName) throws IOException {
      write(file, data, charsetName, false);
   }

   public static void write(File file, CharSequence data, String charsetName, boolean append) throws IOException {
      write(file, data, Charsets.toCharset(charsetName), append);
   }

   public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
      writeByteArrayToFile(file, data, false);
   }

   public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
      writeByteArrayToFile(file, data, 0, data.length, append);
   }

   public static void writeByteArrayToFile(File file, byte[] data, int off, int len) throws IOException {
      writeByteArrayToFile(file, data, off, len, false);
   }

   public static void writeByteArrayToFile(File file, byte[] data, int off, int len, boolean append) throws IOException {
      OutputStream out = newOutputStream(file, append);

      try {
         out.write(data, off, len);
      } catch (Throwable var9) {
         if (out != null) {
            try {
               out.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (out != null) {
         out.close();
      }
   }

   public static void writeLines(File file, Collection<?> lines) throws IOException {
      writeLines(file, null, lines, null, false);
   }

   public static void writeLines(File file, Collection<?> lines, boolean append) throws IOException {
      writeLines(file, null, lines, null, append);
   }

   public static void writeLines(File file, Collection<?> lines, String lineEnding) throws IOException {
      writeLines(file, null, lines, lineEnding, false);
   }

   public static void writeLines(File file, Collection<?> lines, String lineEnding, boolean append) throws IOException {
      writeLines(file, null, lines, lineEnding, append);
   }

   public static void writeLines(File file, String charsetName, Collection<?> lines) throws IOException {
      writeLines(file, charsetName, lines, null, false);
   }

   public static void writeLines(File file, String charsetName, Collection<?> lines, boolean append) throws IOException {
      writeLines(file, charsetName, lines, null, append);
   }

   public static void writeLines(File file, String charsetName, Collection<?> lines, String lineEnding) throws IOException {
      writeLines(file, charsetName, lines, lineEnding, false);
   }

   public static void writeLines(File file, String charsetName, Collection<?> lines, String lineEnding, boolean append) throws IOException {
      OutputStream out = new BufferedOutputStream(newOutputStream(file, append));

      try {
         IOUtils.writeLines(lines, lineEnding, out, charsetName);
      } catch (Throwable var9) {
         try {
            out.close();
         } catch (Throwable var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }

      out.close();
   }

   @Deprecated
   public static void writeStringToFile(File file, String data) throws IOException {
      writeStringToFile(file, data, Charset.defaultCharset(), false);
   }

   @Deprecated
   public static void writeStringToFile(File file, String data, boolean append) throws IOException {
      writeStringToFile(file, data, Charset.defaultCharset(), append);
   }

   public static void writeStringToFile(File file, String data, Charset charset) throws IOException {
      writeStringToFile(file, data, charset, false);
   }

   public static void writeStringToFile(File file, String data, Charset charset, boolean append) throws IOException {
      OutputStream out = newOutputStream(file, append);

      try {
         IOUtils.write(data, out, charset);
      } catch (Throwable var8) {
         if (out != null) {
            try {
               out.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (out != null) {
         out.close();
      }
   }

   public static void writeStringToFile(File file, String data, String charsetName) throws IOException {
      writeStringToFile(file, data, charsetName, false);
   }

   public static void writeStringToFile(File file, String data, String charsetName, boolean append) throws IOException {
      writeStringToFile(file, data, Charsets.toCharset(charsetName), append);
   }
}
