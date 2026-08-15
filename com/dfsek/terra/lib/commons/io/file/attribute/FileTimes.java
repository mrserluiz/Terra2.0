package com.dfsek.terra.lib.commons.io.file.attribute;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class FileTimes {
   private static final BigDecimal LONG_MIN_VALUE_BD = BigDecimal.valueOf(Long.MIN_VALUE);
   private static final BigDecimal LONG_MAX_VALUE_BD = BigDecimal.valueOf(Long.MAX_VALUE);
   private static final MathContext MATH_CONTEXT = new MathContext(0, RoundingMode.FLOOR);
   public static final FileTime EPOCH = FileTime.from(Instant.EPOCH);
   static final long UNIX_TO_NTFS_OFFSET = -116444736000000000L;
   private static final BigDecimal UNIX_TO_NTFS_OFFSET_BD = BigDecimal.valueOf(-116444736000000000L);
   private static final long HUNDRED_NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1L) / 100L;
   private static final BigDecimal HUNDRED_NANOS_PER_SECOND_BD = BigDecimal.valueOf(HUNDRED_NANOS_PER_SECOND);
   static final long HUNDRED_NANOS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1L) / 100L;
   static final BigDecimal HUNDRED_NANOS_PER_MILLISECOND_BD = BigDecimal.valueOf(HUNDRED_NANOS_PER_MILLISECOND);
   private static final long HUNDRED = 100L;
   private static final BigDecimal HUNDRED_BD = BigDecimal.valueOf(100L);

   public static FileTime fromUnixTime(long time) {
      return FileTime.from(time, TimeUnit.SECONDS);
   }

   public static boolean isUnixTime(FileTime time) {
      return isUnixTime(toUnixTime(time));
   }

   public static boolean isUnixTime(long seconds) {
      return -2147483648L <= seconds && seconds <= 2147483647L;
   }

   public static FileTime minusMillis(FileTime fileTime, long millisToSubtract) {
      return FileTime.from(fileTime.toInstant().minusMillis(millisToSubtract));
   }

   public static FileTime minusNanos(FileTime fileTime, long nanosToSubtract) {
      return FileTime.from(fileTime.toInstant().minusNanos(nanosToSubtract));
   }

   public static FileTime minusSeconds(FileTime fileTime, long secondsToSubtract) {
      return FileTime.from(fileTime.toInstant().minusSeconds(secondsToSubtract));
   }

   public static FileTime now() {
      return FileTime.from(Instant.now());
   }

   static Date ntfsTimeToDate(BigDecimal ntfsTime) {
      return new Date(ntfsTimeToInstant(ntfsTime).toEpochMilli());
   }

   public static Date ntfsTimeToDate(long ntfsTime) {
      return ntfsTimeToDate(BigDecimal.valueOf(ntfsTime));
   }

   public static FileTime ntfsTimeToFileTime(long ntfsTime) {
      return FileTime.from(ntfsTimeToInstant(ntfsTime));
   }

   static Instant ntfsTimeToInstant(BigDecimal ntfsTime) {
      BigDecimal javaHundredsNanos = ntfsTime.add(UNIX_TO_NTFS_OFFSET_BD);
      BigDecimal[] dar = javaHundredsNanos.divideAndRemainder(HUNDRED_NANOS_PER_SECOND_BD, MATH_CONTEXT);
      return Instant.ofEpochSecond(dar[0].longValueExact(), dar[1].multiply(HUNDRED_BD).longValueExact());
   }

   static Instant ntfsTimeToInstant(long ntfsTime) {
      return ntfsTimeToInstant(BigDecimal.valueOf(ntfsTime));
   }

   public static FileTime plusMillis(FileTime fileTime, long millisToAdd) {
      return FileTime.from(fileTime.toInstant().plusMillis(millisToAdd));
   }

   public static FileTime plusNanos(FileTime fileTime, long nanosToSubtract) {
      return FileTime.from(fileTime.toInstant().plusNanos(nanosToSubtract));
   }

   public static FileTime plusSeconds(FileTime fileTime, long secondsToAdd) {
      return FileTime.from(fileTime.toInstant().plusSeconds(secondsToAdd));
   }

   public static void setLastModifiedTime(Path path) throws IOException {
      Files.setLastModifiedTime(path, now());
   }

   public static Date toDate(FileTime fileTime) {
      return fileTime != null ? new Date(fileTime.toMillis()) : null;
   }

   public static FileTime toFileTime(Date date) {
      return date != null ? FileTime.fromMillis(date.getTime()) : null;
   }

   public static long toNtfsTime(Date date) {
      return toNtfsTime(date.getTime());
   }

   public static long toNtfsTime(FileTime fileTime) {
      return toNtfsTime(fileTime.toInstant());
   }

   static long toNtfsTime(Instant instant) {
      BigDecimal javaHundredNanos = BigDecimal.valueOf(instant.getEpochSecond())
         .multiply(HUNDRED_NANOS_PER_SECOND_BD)
         .add(BigDecimal.valueOf(instant.getNano() / 100));
      return javaHundredNanos.subtract(UNIX_TO_NTFS_OFFSET_BD).longValueExact();
   }

   public static long toNtfsTime(long javaTime) {
      BigDecimal javaHundredNanos = BigDecimal.valueOf(javaTime).multiply(HUNDRED_NANOS_PER_MILLISECOND_BD);
      BigDecimal ntfsTime = javaHundredNanos.subtract(UNIX_TO_NTFS_OFFSET_BD);
      if (ntfsTime.compareTo(LONG_MAX_VALUE_BD) >= 0) {
         return Long.MAX_VALUE;
      } else {
         return ntfsTime.compareTo(LONG_MIN_VALUE_BD) <= 0 ? Long.MIN_VALUE : ntfsTime.longValue();
      }
   }

   public static long toUnixTime(FileTime fileTime) {
      return fileTime != null ? fileTime.to(TimeUnit.SECONDS) : 0L;
   }

   private FileTimes() {
   }
}
