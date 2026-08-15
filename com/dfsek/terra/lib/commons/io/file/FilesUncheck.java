package com.dfsek.terra.lib.commons.io.file;

import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public final class FilesUncheck {
   public static long copy(InputStream in, Path target, CopyOption... options) {
      return Uncheck.<InputStream, Path, CopyOption[], Long>apply(Files::copy, in, target, options);
   }

   public static long copy(Path source, OutputStream out) {
      return Uncheck.<Path, OutputStream, Long>apply(Files::copy, source, out);
   }

   public static Path copy(Path source, Path target, CopyOption... options) {
      return Uncheck.apply(Files::copy, source, target, options);
   }

   public static Path createDirectories(Path dir, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::createDirectories, dir, attrs);
   }

   public static Path createDirectory(Path dir, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::createDirectory, dir, attrs);
   }

   public static Path createFile(Path path, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::createFile, path, attrs);
   }

   public static Path createLink(Path link, Path existing) {
      return Uncheck.apply(Files::createLink, link, existing);
   }

   public static Path createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::createSymbolicLink, link, target, attrs);
   }

   public static Path createTempDirectory(Path dir, String prefix, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::createTempDirectory, dir, prefix, attrs);
   }

   public static Path createTempDirectory(String prefix, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::createTempDirectory, prefix, attrs);
   }

   public static Path createTempFile(Path dir, String prefix, String suffix, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::createTempFile, dir, prefix, suffix, attrs);
   }

   public static Path createTempFile(String prefix, String suffix, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::createTempFile, prefix, suffix, attrs);
   }

   public static void delete(Path path) {
      Uncheck.accept(Files::delete, path);
   }

   public static boolean deleteIfExists(Path path) {
      return Uncheck.apply(Files::deleteIfExists, path);
   }

   public static Stream<Path> find(Path start, int maxDepth, BiPredicate<Path, BasicFileAttributes> matcher, FileVisitOption... options) {
      return Uncheck.apply(Files::find, start, maxDepth, matcher, options);
   }

   public static Object getAttribute(Path path, String attribute, LinkOption... options) {
      return Uncheck.apply(Files::getAttribute, path, attribute, options);
   }

   public static FileStore getFileStore(Path path) {
      return Uncheck.apply(Files::getFileStore, path);
   }

   public static FileTime getLastModifiedTime(Path path, LinkOption... options) {
      return Uncheck.apply(Files::getLastModifiedTime, path, options);
   }

   public static UserPrincipal getOwner(Path path, LinkOption... options) {
      return Uncheck.apply(Files::getOwner, path, options);
   }

   public static Set<PosixFilePermission> getPosixFilePermissions(Path path, LinkOption... options) {
      return Uncheck.apply(Files::getPosixFilePermissions, path, options);
   }

   public static boolean isHidden(Path path) {
      return Uncheck.apply(Files::isHidden, path);
   }

   public static boolean isSameFile(Path path, Path path2) {
      return Uncheck.apply(Files::isSameFile, path, path2);
   }

   public static Stream<String> lines(Path path) {
      return Uncheck.apply(Files::lines, path);
   }

   public static Stream<String> lines(Path path, Charset cs) {
      return Uncheck.apply(Files::lines, path, cs);
   }

   public static Stream<Path> list(Path dir) {
      return Uncheck.apply(Files::list, dir);
   }

   public static Path move(Path source, Path target, CopyOption... options) {
      return Uncheck.apply(Files::move, source, target, options);
   }

   public static BufferedReader newBufferedReader(Path path) {
      return Uncheck.apply(Files::newBufferedReader, path);
   }

   public static BufferedReader newBufferedReader(Path path, Charset cs) {
      return Uncheck.apply(Files::newBufferedReader, path, cs);
   }

   public static BufferedWriter newBufferedWriter(Path path, Charset cs, OpenOption... options) {
      return Uncheck.apply(Files::newBufferedWriter, path, cs, options);
   }

   public static BufferedWriter newBufferedWriter(Path path, OpenOption... options) {
      return Uncheck.apply(Files::newBufferedWriter, path, options);
   }

   public static SeekableByteChannel newByteChannel(Path path, OpenOption... options) {
      return Uncheck.apply(Files::newByteChannel, path, options);
   }

   public static SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) {
      return Uncheck.apply(Files::newByteChannel, path, options, attrs);
   }

   public static DirectoryStream<Path> newDirectoryStream(Path dir) {
      return Uncheck.apply(Files::newDirectoryStream, dir);
   }

   public static DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) {
      return Uncheck.apply(Files::newDirectoryStream, dir, filter);
   }

   public static DirectoryStream<Path> newDirectoryStream(Path dir, String glob) {
      return Uncheck.apply(Files::newDirectoryStream, dir, glob);
   }

   public static InputStream newInputStream(Path path, OpenOption... options) {
      return Uncheck.apply(Files::newInputStream, path, options);
   }

   public static OutputStream newOutputStream(Path path, OpenOption... options) {
      return Uncheck.apply(Files::newOutputStream, path, options);
   }

   public static String probeContentType(Path path) {
      return Uncheck.apply(Files::probeContentType, path);
   }

   public static byte[] readAllBytes(Path path) {
      return Uncheck.apply(Files::readAllBytes, path);
   }

   public static List<String> readAllLines(Path path) {
      return Uncheck.apply(Files::readAllLines, path);
   }

   public static List<String> readAllLines(Path path, Charset cs) {
      return Uncheck.apply(Files::readAllLines, path, cs);
   }

   public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) {
      return Uncheck.apply(Files::readAttributes, path, type, options);
   }

   public static Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) {
      return Uncheck.apply(Files::readAttributes, path, attributes, options);
   }

   public static Path readSymbolicLink(Path link) {
      return Uncheck.apply(Files::readSymbolicLink, link);
   }

   public static Path setAttribute(Path path, String attribute, Object value, LinkOption... options) {
      return Uncheck.apply(Files::setAttribute, path, attribute, value, options);
   }

   public static Path setLastModifiedTime(Path path, FileTime time) {
      return Uncheck.apply(Files::setLastModifiedTime, path, time);
   }

   public static Path setOwner(Path path, UserPrincipal owner) {
      return Uncheck.apply(Files::setOwner, path, owner);
   }

   public static Path setPosixFilePermissions(Path path, Set<PosixFilePermission> perms) {
      return Uncheck.apply(Files::setPosixFilePermissions, path, perms);
   }

   public static long size(Path path) {
      return Uncheck.apply(Files::size, path);
   }

   public static Stream<Path> walk(Path start, FileVisitOption... options) {
      return Uncheck.apply(Files::walk, start, options);
   }

   public static Stream<Path> walk(Path start, int maxDepth, FileVisitOption... options) {
      return Uncheck.apply(Files::walk, start, maxDepth, options);
   }

   public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor) {
      return Uncheck.apply(Files::walkFileTree, start, visitor);
   }

   public static Path walkFileTree(Path start, Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor) {
      return Uncheck.apply(Files::walkFileTree, start, options, maxDepth, visitor);
   }

   public static Path write(Path path, byte[] bytes, OpenOption... options) {
      return Uncheck.apply(Files::write, path, bytes, options);
   }

   public static Path write(Path path, Iterable<? extends CharSequence> lines, Charset cs, OpenOption... options) {
      return Uncheck.apply(Files::write, path, lines, cs, options);
   }

   public static Path write(Path path, Iterable<? extends CharSequence> lines, OpenOption... options) {
      return Uncheck.apply(Files::write, path, lines, options);
   }

   private FilesUncheck() {
   }
}
