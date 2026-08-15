package com.dfsek.terra.lib.commons.lang3.time;

import com.dfsek.terra.lib.commons.lang3.StringUtils;
import com.dfsek.terra.lib.commons.lang3.Validate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Stream;

public class DurationFormatUtils {
   private static final int MINUTES_PER_HOUR = 60;
   private static final int SECONDS_PER_MINUTES = 60;
   private static final int HOURS_PER_DAY = 24;
   public static final String ISO_EXTENDED_FORMAT_PATTERN = "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.SSS'S'";
   static final String y = "y";
   static final String M = "M";
   static final String d = "d";
   static final String H = "H";
   static final String m = "m";
   static final String s = "s";
   static final String S = "S";

   static String format(
      DurationFormatUtils.Token[] tokens, long years, long months, long days, long hours, long minutes, long seconds, long milliseconds, boolean padWithZeros
   ) {
      StringBuilder buffer = new StringBuilder();
      boolean lastOutputSeconds = false;
      boolean lastOutputZero = false;
      int optionalStart = -1;
      boolean firstOptionalNonLiteral = false;
      int optionalIndex = -1;
      boolean inOptional = false;

      for (DurationFormatUtils.Token token : tokens) {
         Object value = token.getValue();
         boolean isLiteral = value instanceof StringBuilder;
         int count = token.getCount();
         if (optionalIndex != token.optionalIndex) {
            optionalIndex = token.optionalIndex;
            if (optionalIndex > -1) {
               optionalStart = buffer.length();
               lastOutputZero = false;
               inOptional = true;
               firstOptionalNonLiteral = false;
            } else {
               inOptional = false;
            }
         }

         if (isLiteral) {
            if (!inOptional || !lastOutputZero) {
               buffer.append(value.toString());
            }
         } else if (value.equals("y")) {
            lastOutputSeconds = false;
            lastOutputZero = years == 0L;
            if (!inOptional || !lastOutputZero) {
               buffer.append(paddedValue(years, padWithZeros, count));
            }
         } else if (value.equals("M")) {
            lastOutputSeconds = false;
            lastOutputZero = months == 0L;
            if (!inOptional || !lastOutputZero) {
               buffer.append(paddedValue(months, padWithZeros, count));
            }
         } else if (value.equals("d")) {
            lastOutputSeconds = false;
            lastOutputZero = days == 0L;
            if (!inOptional || !lastOutputZero) {
               buffer.append(paddedValue(days, padWithZeros, count));
            }
         } else if (value.equals("H")) {
            lastOutputSeconds = false;
            lastOutputZero = hours == 0L;
            if (!inOptional || !lastOutputZero) {
               buffer.append(paddedValue(hours, padWithZeros, count));
            }
         } else if (value.equals("m")) {
            lastOutputSeconds = false;
            lastOutputZero = minutes == 0L;
            if (!inOptional || !lastOutputZero) {
               buffer.append(paddedValue(minutes, padWithZeros, count));
            }
         } else if (value.equals("s")) {
            lastOutputSeconds = true;
            lastOutputZero = seconds == 0L;
            if (!inOptional || !lastOutputZero) {
               buffer.append(paddedValue(seconds, padWithZeros, count));
            }
         } else if (value.equals("S")) {
            lastOutputZero = milliseconds == 0L;
            if (!inOptional || !lastOutputZero) {
               if (lastOutputSeconds) {
                  int width = padWithZeros ? Math.max(3, count) : 3;
                  buffer.append(paddedValue(milliseconds, true, width));
               } else {
                  buffer.append(paddedValue(milliseconds, padWithZeros, count));
               }
            }

            lastOutputSeconds = false;
         }

         if (inOptional && !isLiteral && !firstOptionalNonLiteral) {
            firstOptionalNonLiteral = true;
            if (lastOutputZero) {
               buffer.delete(optionalStart, buffer.length());
            }
         }
      }

      return buffer.toString();
   }

   public static String formatDuration(long durationMillis, String format) {
      return formatDuration(durationMillis, format, true);
   }

   public static String formatDuration(long durationMillis, String format, boolean padWithZeros) {
      Validate.inclusiveBetween(0L, Long.MAX_VALUE, durationMillis, "durationMillis must not be negative");
      DurationFormatUtils.Token[] tokens = lexx(format);
      long days = 0L;
      long hours = 0L;
      long minutes = 0L;
      long seconds = 0L;
      long milliseconds = durationMillis;
      if (DurationFormatUtils.Token.containsTokenWithValue(tokens, "d")) {
         days = milliseconds / 86400000L;
         milliseconds -= days * 86400000L;
      }

      if (DurationFormatUtils.Token.containsTokenWithValue(tokens, "H")) {
         hours = milliseconds / 3600000L;
         milliseconds -= hours * 3600000L;
      }

      if (DurationFormatUtils.Token.containsTokenWithValue(tokens, "m")) {
         minutes = milliseconds / 60000L;
         milliseconds -= minutes * 60000L;
      }

      if (DurationFormatUtils.Token.containsTokenWithValue(tokens, "s")) {
         seconds = milliseconds / 1000L;
         milliseconds -= seconds * 1000L;
      }

      return format(tokens, 0L, 0L, days, hours, minutes, seconds, milliseconds, padWithZeros);
   }

   public static String formatDurationHMS(long durationMillis) {
      return formatDuration(durationMillis, "HH:mm:ss.SSS");
   }

   public static String formatDurationISO(long durationMillis) {
      return formatDuration(durationMillis, "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.SSS'S'", false);
   }

   public static String formatDurationWords(long durationMillis, boolean suppressLeadingZeroElements, boolean suppressTrailingZeroElements) {
      String duration = formatDuration(durationMillis, "d' days 'H' hours 'm' minutes 's' seconds'");
      if (suppressLeadingZeroElements) {
         duration = " " + duration;
         String tmp = StringUtils.replaceOnce(duration, " 0 days", "");
         if (tmp.length() != duration.length()) {
            duration = tmp;
            tmp = StringUtils.replaceOnce(duration, " 0 hours", "");
            if (tmp.length() != duration.length()) {
               duration = tmp;
               tmp = StringUtils.replaceOnce(duration, " 0 minutes", "");
               duration = tmp;
            }
         }

         if (!duration.isEmpty()) {
            duration = duration.substring(1);
         }
      }

      if (suppressTrailingZeroElements) {
         String tmp = StringUtils.replaceOnce(duration, " 0 seconds", "");
         if (tmp.length() != duration.length()) {
            duration = tmp;
            tmp = StringUtils.replaceOnce(duration, " 0 minutes", "");
            if (tmp.length() != duration.length()) {
               duration = tmp;
               tmp = StringUtils.replaceOnce(duration, " 0 hours", "");
               if (tmp.length() != duration.length()) {
                  duration = StringUtils.replaceOnce(tmp, " 0 days", "");
               }
            }
         }
      }

      duration = " " + duration;
      duration = StringUtils.replaceOnce(duration, " 1 seconds", " 1 second");
      duration = StringUtils.replaceOnce(duration, " 1 minutes", " 1 minute");
      duration = StringUtils.replaceOnce(duration, " 1 hours", " 1 hour");
      duration = StringUtils.replaceOnce(duration, " 1 days", " 1 day");
      return duration.trim();
   }

   public static String formatPeriod(long startMillis, long endMillis, String format) {
      return formatPeriod(startMillis, endMillis, format, true, TimeZone.getDefault());
   }

   public static String formatPeriod(long startMillis, long endMillis, String format, boolean padWithZeros, TimeZone timezone) {
      Validate.isTrue(startMillis <= endMillis, "startMillis must not be greater than endMillis");
      DurationFormatUtils.Token[] tokens = lexx(format);
      Calendar start = Calendar.getInstance(timezone);
      start.setTime(new Date(startMillis));
      Calendar end = Calendar.getInstance(timezone);
      end.setTime(new Date(endMillis));
      long milliseconds = end.get(14) - start.get(14);
      int seconds = end.get(13) - start.get(13);
      int minutes = end.get(12) - start.get(12);
      int hours = end.get(11) - start.get(11);
      int days = end.get(5) - start.get(5);
      int months = end.get(2) - start.get(2);
      int years = end.get(1) - start.get(1);

      while (milliseconds < 0L) {
         milliseconds += 1000L;
         seconds--;
      }

      while (seconds < 0) {
         seconds += 60;
         minutes--;
      }

      while (minutes < 0) {
         minutes += 60;
         hours--;
      }

      while (hours < 0) {
         hours += 24;
         days--;
      }

      if (!DurationFormatUtils.Token.containsTokenWithValue(tokens, "M")) {
         if (!DurationFormatUtils.Token.containsTokenWithValue(tokens, "y")) {
            int target = end.get(1);
            if (months < 0) {
               target--;
            }

            while (start.get(1) != target) {
               days += start.getActualMaximum(6) - start.get(6);
               if (start instanceof GregorianCalendar && start.get(2) == 1 && start.get(5) == 29) {
                  days++;
               }

               start.add(1, 1);
               days += start.get(6);
            }

            years = 0;
         }

         while (start.get(2) != end.get(2)) {
            days += start.getActualMaximum(5);
            start.add(2, 1);
         }

         months = 0;

         while (days < 0) {
            days += start.getActualMaximum(5);
            months--;
            start.add(2, 1);
         }
      } else {
         while (days < 0) {
            days += start.getActualMaximum(5);
            months--;
            start.add(2, 1);
         }

         while (months < 0) {
            months += 12;
            years--;
         }

         if (!DurationFormatUtils.Token.containsTokenWithValue(tokens, "y") && years != 0) {
            while (years != 0) {
               months += 12 * years;
               years = 0;
            }
         }
      }

      if (!DurationFormatUtils.Token.containsTokenWithValue(tokens, "d")) {
         hours += 24 * days;
         days = 0;
      }

      if (!DurationFormatUtils.Token.containsTokenWithValue(tokens, "H")) {
         minutes += 60 * hours;
         hours = 0;
      }

      if (!DurationFormatUtils.Token.containsTokenWithValue(tokens, "m")) {
         seconds += 60 * minutes;
         minutes = 0;
      }

      if (!DurationFormatUtils.Token.containsTokenWithValue(tokens, "s")) {
         milliseconds += 1000L * seconds;
         seconds = 0;
      }

      return format(tokens, years, months, days, hours, minutes, seconds, milliseconds, padWithZeros);
   }

   public static String formatPeriodISO(long startMillis, long endMillis) {
      return formatPeriod(startMillis, endMillis, "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.SSS'S'", false, TimeZone.getDefault());
   }

   static DurationFormatUtils.Token[] lexx(String format) {
      ArrayList<DurationFormatUtils.Token> list = new ArrayList<>(format.length());
      boolean inLiteral = false;
      StringBuilder buffer = null;
      DurationFormatUtils.Token previous = null;
      boolean inOptional = false;
      int optionalIndex = -1;

      for (int i = 0; i < format.length(); i++) {
         char ch = format.charAt(i);
         if (inLiteral && ch != '\'') {
            buffer.append(ch);
         } else {
            String value = null;
            switch (ch) {
               case '\'':
                  if (inLiteral) {
                     buffer = null;
                     inLiteral = false;
                  } else {
                     buffer = new StringBuilder();
                     list.add(new DurationFormatUtils.Token(buffer, inOptional, optionalIndex));
                     inLiteral = true;
                  }
                  break;
               case 'H':
                  value = "H";
                  break;
               case 'M':
                  value = "M";
                  break;
               case 'S':
                  value = "S";
                  break;
               case '[':
                  if (inOptional) {
                     throw new IllegalArgumentException("Nested optional block at index: " + i);
                  }

                  optionalIndex++;
                  inOptional = true;
                  break;
               case ']':
                  if (!inOptional) {
                     throw new IllegalArgumentException("Attempting to close unopened optional block at index: " + i);
                  }

                  inOptional = false;
                  break;
               case 'd':
                  value = "d";
                  break;
               case 'm':
                  value = "m";
                  break;
               case 's':
                  value = "s";
                  break;
               case 'y':
                  value = "y";
                  break;
               default:
                  if (buffer == null) {
                     buffer = new StringBuilder();
                     list.add(new DurationFormatUtils.Token(buffer, inOptional, optionalIndex));
                  }

                  buffer.append(ch);
            }

            if (value != null) {
               if (previous != null && previous.getValue().equals(value)) {
                  previous.increment();
               } else {
                  DurationFormatUtils.Token token = new DurationFormatUtils.Token(value, inOptional, optionalIndex);
                  list.add(token);
                  previous = token;
               }

               buffer = null;
            }
         }
      }

      if (inLiteral) {
         throw new IllegalArgumentException("Unmatched quote in format: " + format);
      } else if (inOptional) {
         throw new IllegalArgumentException("Unmatched optional in format: " + format);
      } else {
         return list.toArray(DurationFormatUtils.Token.EMPTY_ARRAY);
      }
   }

   private static String paddedValue(long value, boolean padWithZeros, int count) {
      String longString = Long.toString(value);
      return padWithZeros ? StringUtils.leftPad(longString, count, '0') : longString;
   }

   static class Token {
      private static final DurationFormatUtils.Token[] EMPTY_ARRAY = new DurationFormatUtils.Token[0];
      private final CharSequence value;
      private int count;
      private int optionalIndex = -1;

      static boolean containsTokenWithValue(DurationFormatUtils.Token[] tokens, Object value) {
         return Stream.of(tokens).anyMatch(token -> token.getValue() == value);
      }

      Token(CharSequence value, boolean optional, int optionalIndex) {
         this.value = Objects.requireNonNull(value, "value");
         this.count = 1;
         if (optional) {
            this.optionalIndex = optionalIndex;
         }
      }

      @Override
      public boolean equals(Object obj2) {
         if (obj2 instanceof DurationFormatUtils.Token) {
            DurationFormatUtils.Token tok2 = (DurationFormatUtils.Token)obj2;
            if (this.value.getClass() != tok2.value.getClass()) {
               return false;
            } else if (this.count != tok2.count) {
               return false;
            } else if (this.value instanceof StringBuilder) {
               return this.value.toString().equals(tok2.value.toString());
            } else {
               return this.value instanceof Number ? this.value.equals(tok2.value) : this.value == tok2.value;
            }
         } else {
            return false;
         }
      }

      int getCount() {
         return this.count;
      }

      Object getValue() {
         return this.value;
      }

      @Override
      public int hashCode() {
         return this.value.hashCode();
      }

      void increment() {
         this.count++;
      }

      @Override
      public String toString() {
         return StringUtils.repeat(this.value.toString(), this.count);
      }
   }
}
