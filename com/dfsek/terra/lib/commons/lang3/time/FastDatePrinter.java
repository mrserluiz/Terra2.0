package com.dfsek.terra.lib.commons.lang3.time;

import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import com.dfsek.terra.lib.commons.lang3.LocaleUtils;
import com.dfsek.terra.lib.commons.lang3.exception.ExceptionUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FastDatePrinter implements DatePrinter, Serializable {
   private static final FastDatePrinter.Rule[] EMPTY_RULE_ARRAY = new FastDatePrinter.Rule[0];
   private static final long serialVersionUID = 1L;
   public static final int FULL = 0;
   public static final int LONG = 1;
   public static final int MEDIUM = 2;
   public static final int SHORT = 3;
   private static final int MAX_DIGITS = 10;
   private static final ConcurrentMap<FastDatePrinter.TimeZoneDisplayKey, String> cTimeZoneDisplayCache = new ConcurrentHashMap<>(7);
   private final String pattern;
   private final TimeZone timeZone;
   private final Locale locale;
   private transient FastDatePrinter.Rule[] rules;
   private transient int maxLengthEstimate;

   private static void appendDigits(Appendable buffer, int value) throws IOException {
      buffer.append((char)(value / 10 + 48));
      buffer.append((char)(value % 10 + 48));
   }

   private static void appendFullDigits(Appendable buffer, int value, int minFieldWidth) throws IOException {
      if (value < 10000) {
         int nDigits = 4;
         if (value < 1000) {
            nDigits--;
            if (value < 100) {
               nDigits--;
               if (value < 10) {
                  nDigits--;
               }
            }
         }

         for (int i = minFieldWidth - nDigits; i > 0; i--) {
            buffer.append('0');
         }

         switch (nDigits) {
            case 4:
               buffer.append((char)(value / 1000 + 48));
               value %= 1000;
            case 3:
               if (value >= 100) {
                  buffer.append((char)(value / 100 + 48));
                  value %= 100;
               } else {
                  buffer.append('0');
               }
            case 2:
               if (value >= 10) {
                  buffer.append((char)(value / 10 + 48));
                  value %= 10;
               } else {
                  buffer.append('0');
               }
            case 1:
               buffer.append((char)(value + 48));
         }
      } else {
         char[] work = new char[10];
         int digit = 0;

         while (value != 0) {
            work[digit++] = (char)(value % 10 + 48);
            value /= 10;
         }

         while (digit < minFieldWidth) {
            buffer.append('0');
            minFieldWidth--;
         }

         while (--digit >= 0) {
            buffer.append(work[digit]);
         }
      }
   }

   static String getTimeZoneDisplay(TimeZone tz, boolean daylight, int style, Locale locale) {
      FastDatePrinter.TimeZoneDisplayKey key = new FastDatePrinter.TimeZoneDisplayKey(tz, daylight, style, locale);
      return cTimeZoneDisplayCache.computeIfAbsent(key, k -> tz.getDisplayName(daylight, style, locale));
   }

   protected FastDatePrinter(String pattern, TimeZone timeZone, Locale locale) {
      this.pattern = pattern;
      this.timeZone = timeZone;
      this.locale = LocaleUtils.toLocale(locale);
      this.init();
   }

   private <B extends Appendable> B applyRules(Calendar calendar, B buf) {
      try {
         for (FastDatePrinter.Rule rule : this.rules) {
            rule.appendTo(buf, calendar);
         }
      } catch (IOException ioe) {
         ExceptionUtils.asRuntimeException(ioe);
      }

      return buf;
   }

   @Deprecated
   protected StringBuffer applyRules(Calendar calendar, StringBuffer buf) {
      return this.applyRules(calendar, (StringBuffer)buf);
   }

   private String applyRulesToString(Calendar c) {
      return this.applyRules(c, new StringBuilder(this.maxLengthEstimate)).toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof FastDatePrinter)) {
         return false;
      }

      FastDatePrinter other = (FastDatePrinter)obj;
      return this.pattern.equals(other.pattern) && this.timeZone.equals(other.timeZone) && this.locale.equals(other.locale);
   }

   @Override
   public String format(Calendar calendar) {
      return this.format(calendar, new StringBuilder(this.maxLengthEstimate)).toString();
   }

   @Override
   public <B extends Appendable> B format(Calendar calendar, B buf) {
      if (!calendar.getTimeZone().equals(this.timeZone)) {
         calendar = (Calendar)calendar.clone();
         calendar.setTimeZone(this.timeZone);
      }

      return this.applyRules(calendar, buf);
   }

   @Override
   public StringBuffer format(Calendar calendar, StringBuffer buf) {
      return this.format(calendar.getTime(), buf);
   }

   @Override
   public String format(Date date) {
      Calendar c = this.newCalendar();
      c.setTime(date);
      return this.applyRulesToString(c);
   }

   @Override
   public <B extends Appendable> B format(Date date, B buf) {
      Calendar c = this.newCalendar();
      c.setTime(date);
      return this.applyRules(c, buf);
   }

   @Override
   public StringBuffer format(Date date, StringBuffer buf) {
      Calendar c = this.newCalendar();
      c.setTime(date);
      return this.applyRules(c, (StringBuffer)buf);
   }

   @Override
   public String format(long millis) {
      Calendar c = this.newCalendar();
      c.setTimeInMillis(millis);
      return this.applyRulesToString(c);
   }

   @Override
   public <B extends Appendable> B format(long millis, B buf) {
      Calendar c = this.newCalendar();
      c.setTimeInMillis(millis);
      return this.applyRules(c, buf);
   }

   @Override
   public StringBuffer format(long millis, StringBuffer buf) {
      Calendar c = this.newCalendar();
      c.setTimeInMillis(millis);
      return this.applyRules(c, (StringBuffer)buf);
   }

   String format(Object obj) {
      if (obj instanceof Date) {
         return this.format((Date)obj);
      } else if (obj instanceof Calendar) {
         return this.format((Calendar)obj);
      } else if (obj instanceof Long) {
         return this.format(((Long)obj).longValue());
      } else {
         throw new IllegalArgumentException("Unknown class: " + ClassUtils.getName(obj, "<null>"));
      }
   }

   @Deprecated
   @Override
   public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
      if (obj instanceof Date) {
         return this.format((Date)obj, toAppendTo);
      } else if (obj instanceof Calendar) {
         return this.format((Calendar)obj, toAppendTo);
      } else if (obj instanceof Long) {
         return this.format((Long)obj, toAppendTo);
      } else {
         throw new IllegalArgumentException("Unknown class: " + ClassUtils.getName(obj, "<null>"));
      }
   }

   @Override
   public Locale getLocale() {
      return this.locale;
   }

   public int getMaxLengthEstimate() {
      return this.maxLengthEstimate;
   }

   @Override
   public String getPattern() {
      return this.pattern;
   }

   @Override
   public TimeZone getTimeZone() {
      return this.timeZone;
   }

   @Override
   public int hashCode() {
      return this.pattern.hashCode() + 13 * (this.timeZone.hashCode() + 13 * this.locale.hashCode());
   }

   private void init() {
      List<FastDatePrinter.Rule> rulesList = this.parsePattern();
      this.rules = rulesList.toArray(EMPTY_RULE_ARRAY);
      int len = 0;
      int i = this.rules.length;

      while (--i >= 0) {
         len += this.rules[i].estimateLength();
      }

      this.maxLengthEstimate = len;
   }

   private Calendar newCalendar() {
      return Calendar.getInstance(this.timeZone, this.locale);
   }

   protected List<FastDatePrinter.Rule> parsePattern() {
      DateFormatSymbols symbols = new DateFormatSymbols(this.locale);
      List<FastDatePrinter.Rule> rules = new ArrayList<>();
      String[] ERAs = symbols.getEras();
      String[] months = symbols.getMonths();
      String[] shortMonths = symbols.getShortMonths();
      String[] weekdays = symbols.getWeekdays();
      String[] shortWeekdays = symbols.getShortWeekdays();
      String[] AmPmStrings = symbols.getAmPmStrings();
      int length = this.pattern.length();
      int[] indexRef = new int[1];

      for (int i = 0; i < length; i++) {
         indexRef[0] = i;
         String token = this.parseToken(this.pattern, indexRef);
         i = indexRef[0];
         int tokenLen = token.length();
         if (tokenLen == 0) {
            break;
         }

         char c = token.charAt(0);
         FastDatePrinter.Rule rule;
         switch (c) {
            case '\'':
               String sub = token.substring(1);
               if (sub.length() == 1) {
                  rule = new FastDatePrinter.CharacterLiteral(sub.charAt(0));
               } else {
                  rule = new FastDatePrinter.StringLiteral(sub);
               }
               break;
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
            case '/':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case 'A':
            case 'B':
            case 'C':
            case 'I':
            case 'J':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'T':
            case 'U':
            case 'V':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '_':
            case '`':
            case 'b':
            case 'c':
            case 'e':
            case 'f':
            case 'g':
            case 'i':
            case 'j':
            case 'l':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 't':
            case 'v':
            case 'x':
            default:
               throw new IllegalArgumentException("Illegal pattern component: " + token);
            case 'D':
               rule = this.selectNumberRule(6, tokenLen);
               break;
            case 'E':
               rule = new FastDatePrinter.TextField(7, tokenLen < 4 ? shortWeekdays : weekdays);
               break;
            case 'F':
               rule = this.selectNumberRule(8, tokenLen);
               break;
            case 'G':
               rule = new FastDatePrinter.TextField(0, ERAs);
               break;
            case 'H':
               rule = this.selectNumberRule(11, tokenLen);
               break;
            case 'K':
               rule = this.selectNumberRule(10, tokenLen);
               break;
            case 'L':
               if (tokenLen >= 4) {
                  rule = new FastDatePrinter.TextField(2, CalendarUtils.getInstance(this.locale).getStandaloneLongMonthNames());
               } else if (tokenLen == 3) {
                  rule = new FastDatePrinter.TextField(2, CalendarUtils.getInstance(this.locale).getStandaloneShortMonthNames());
               } else if (tokenLen == 2) {
                  rule = FastDatePrinter.TwoDigitMonthField.INSTANCE;
               } else {
                  rule = FastDatePrinter.UnpaddedMonthField.INSTANCE;
               }
               break;
            case 'M':
               if (tokenLen >= 4) {
                  rule = new FastDatePrinter.TextField(2, months);
               } else if (tokenLen == 3) {
                  rule = new FastDatePrinter.TextField(2, shortMonths);
               } else if (tokenLen == 2) {
                  rule = FastDatePrinter.TwoDigitMonthField.INSTANCE;
               } else {
                  rule = FastDatePrinter.UnpaddedMonthField.INSTANCE;
               }
               break;
            case 'S':
               rule = this.selectNumberRule(14, tokenLen);
               break;
            case 'W':
               rule = this.selectNumberRule(4, tokenLen);
               break;
            case 'X':
               rule = FastDatePrinter.Iso8601_Rule.getRule(tokenLen);
               break;
            case 'Y':
            case 'y':
               if (tokenLen == 2) {
                  rule = FastDatePrinter.TwoDigitYearField.INSTANCE;
               } else {
                  rule = this.selectNumberRule(1, Math.max(tokenLen, 4));
               }

               if (c == 'Y') {
                  rule = new FastDatePrinter.WeekYear((FastDatePrinter.NumberRule)rule);
               }
               break;
            case 'Z':
               if (tokenLen == 1) {
                  rule = FastDatePrinter.TimeZoneNumberRule.INSTANCE_NO_COLON;
               } else if (tokenLen == 2) {
                  rule = FastDatePrinter.Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
               } else {
                  rule = FastDatePrinter.TimeZoneNumberRule.INSTANCE_COLON;
               }
               break;
            case 'a':
               rule = new FastDatePrinter.TextField(9, AmPmStrings);
               break;
            case 'd':
               rule = this.selectNumberRule(5, tokenLen);
               break;
            case 'h':
               rule = new FastDatePrinter.TwelveHourField(this.selectNumberRule(10, tokenLen));
               break;
            case 'k':
               rule = new FastDatePrinter.TwentyFourHourField(this.selectNumberRule(11, tokenLen));
               break;
            case 'm':
               rule = this.selectNumberRule(12, tokenLen);
               break;
            case 's':
               rule = this.selectNumberRule(13, tokenLen);
               break;
            case 'u':
               rule = new FastDatePrinter.DayInWeekField(this.selectNumberRule(7, tokenLen));
               break;
            case 'w':
               rule = this.selectNumberRule(3, tokenLen);
               break;
            case 'z':
               if (tokenLen >= 4) {
                  rule = new FastDatePrinter.TimeZoneNameRule(this.timeZone, this.locale, 1);
               } else {
                  rule = new FastDatePrinter.TimeZoneNameRule(this.timeZone, this.locale, 0);
               }
         }

         rules.add(rule);
      }

      return rules;
   }

   protected String parseToken(String pattern, int[] indexRef) {
      StringBuilder buf = new StringBuilder();
      int i = indexRef[0];
      int length = pattern.length();
      char c = pattern.charAt(i);
      if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
         buf.append(c);

         while (i + 1 < length) {
            char peek = pattern.charAt(i + 1);
            if (peek != c) {
               break;
            }

            buf.append(c);
            i++;
         }
      } else {
         buf.append('\'');
         boolean inLiteral = false;

         while (i < length) {
            c = pattern.charAt(i);
            if (c == '\'') {
               if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
                  i++;
                  buf.append(c);
               } else {
                  inLiteral = !inLiteral;
               }
            } else {
               if (!inLiteral && (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
                  i--;
                  break;
               }

               buf.append(c);
            }

            i++;
         }
      }

      indexRef[0] = i;
      return buf.toString();
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      this.init();
   }

   protected FastDatePrinter.NumberRule selectNumberRule(int field, int padding) {
      switch (padding) {
         case 1:
            return new FastDatePrinter.UnpaddedNumberField(field);
         case 2:
            return new FastDatePrinter.TwoDigitNumberField(field);
         default:
            return new FastDatePrinter.PaddedNumberField(field, padding);
      }
   }

   @Override
   public String toString() {
      return "FastDatePrinter[" + this.pattern + "," + this.locale + "," + this.timeZone.getID() + "]";
   }

   private static class CharacterLiteral implements FastDatePrinter.Rule {
      private final char value;

      CharacterLiteral(char value) {
         this.value = value;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         buffer.append(this.value);
      }

      @Override
      public int estimateLength() {
         return 1;
      }
   }

   private static class DayInWeekField implements FastDatePrinter.NumberRule {
      private final FastDatePrinter.NumberRule rule;

      DayInWeekField(FastDatePrinter.NumberRule rule) {
         this.rule = rule;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         int value = calendar.get(7);
         this.rule.appendTo(buffer, value == 1 ? 7 : value - 1);
      }

      @Override
      public void appendTo(Appendable buffer, int value) throws IOException {
         this.rule.appendTo(buffer, value);
      }

      @Override
      public int estimateLength() {
         return this.rule.estimateLength();
      }
   }

   private static class Iso8601_Rule implements FastDatePrinter.Rule {
      static final FastDatePrinter.Iso8601_Rule ISO8601_HOURS = new FastDatePrinter.Iso8601_Rule(3);
      static final FastDatePrinter.Iso8601_Rule ISO8601_HOURS_MINUTES = new FastDatePrinter.Iso8601_Rule(5);
      static final FastDatePrinter.Iso8601_Rule ISO8601_HOURS_COLON_MINUTES = new FastDatePrinter.Iso8601_Rule(6);
      private final int length;

      static FastDatePrinter.Iso8601_Rule getRule(int tokenLen) {
         switch (tokenLen) {
            case 1:
               return ISO8601_HOURS;
            case 2:
               return ISO8601_HOURS_MINUTES;
            case 3:
               return ISO8601_HOURS_COLON_MINUTES;
            default:
               throw new IllegalArgumentException("invalid number of X");
         }
      }

      Iso8601_Rule(int length) {
         this.length = length;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         int offset = calendar.get(15) + calendar.get(16);
         if (offset == 0) {
            buffer.append("Z");
         } else {
            if (offset < 0) {
               buffer.append('-');
               offset = -offset;
            } else {
               buffer.append('+');
            }

            int hours = offset / 3600000;
            FastDatePrinter.appendDigits(buffer, hours);
            if (this.length >= 5) {
               if (this.length == 6) {
                  buffer.append(':');
               }

               int minutes = offset / 60000 - 60 * hours;
               FastDatePrinter.appendDigits(buffer, minutes);
            }
         }
      }

      @Override
      public int estimateLength() {
         return this.length;
      }
   }

   private interface NumberRule extends FastDatePrinter.Rule {
      void appendTo(Appendable var1, int var2) throws IOException;
   }

   private static final class PaddedNumberField implements FastDatePrinter.NumberRule {
      private final int field;
      private final int size;

      PaddedNumberField(int field, int size) {
         if (size < 3) {
            throw new IllegalArgumentException();
         }

         this.field = field;
         this.size = size;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         this.appendTo(buffer, calendar.get(this.field));
      }

      @Override
      public void appendTo(Appendable buffer, int value) throws IOException {
         FastDatePrinter.appendFullDigits(buffer, value, this.size);
      }

      @Override
      public int estimateLength() {
         return this.size;
      }
   }

   private interface Rule {
      void appendTo(Appendable var1, Calendar var2) throws IOException;

      int estimateLength();
   }

   private static class StringLiteral implements FastDatePrinter.Rule {
      private final String value;

      StringLiteral(String value) {
         this.value = value;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         buffer.append(this.value);
      }

      @Override
      public int estimateLength() {
         return this.value.length();
      }
   }

   private static class TextField implements FastDatePrinter.Rule {
      private final int field;
      private final String[] values;

      TextField(int field, String[] values) {
         this.field = field;
         this.values = values;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         buffer.append(this.values[calendar.get(this.field)]);
      }

      @Override
      public int estimateLength() {
         int max = 0;
         int i = this.values.length;

         while (--i >= 0) {
            int len = this.values[i].length();
            if (len > max) {
               max = len;
            }
         }

         return max;
      }
   }

   private static class TimeZoneDisplayKey {
      private final TimeZone timeZone;
      private final int style;
      private final Locale locale;

      TimeZoneDisplayKey(TimeZone timeZone, boolean daylight, int style, Locale locale) {
         this.timeZone = timeZone;
         if (daylight) {
            this.style = style | -2147483648;
         } else {
            this.style = style;
         }

         this.locale = LocaleUtils.toLocale(locale);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof FastDatePrinter.TimeZoneDisplayKey)) {
            return false;
         }

         FastDatePrinter.TimeZoneDisplayKey other = (FastDatePrinter.TimeZoneDisplayKey)obj;
         return this.timeZone.equals(other.timeZone) && this.style == other.style && this.locale.equals(other.locale);
      }

      @Override
      public int hashCode() {
         return (this.style * 31 + this.locale.hashCode()) * 31 + this.timeZone.hashCode();
      }
   }

   private static class TimeZoneNameRule implements FastDatePrinter.Rule {
      private final Locale locale;
      private final int style;
      private final String standard;
      private final String daylight;

      TimeZoneNameRule(TimeZone timeZone, Locale locale, int style) {
         this.locale = LocaleUtils.toLocale(locale);
         this.style = style;
         this.standard = FastDatePrinter.getTimeZoneDisplay(timeZone, false, style, locale);
         this.daylight = FastDatePrinter.getTimeZoneDisplay(timeZone, true, style, locale);
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         TimeZone zone = calendar.getTimeZone();
         boolean daylight = calendar.get(16) != 0;
         buffer.append(FastDatePrinter.getTimeZoneDisplay(zone, daylight, this.style, this.locale));
      }

      @Override
      public int estimateLength() {
         return Math.max(this.standard.length(), this.daylight.length());
      }
   }

   private static class TimeZoneNumberRule implements FastDatePrinter.Rule {
      static final FastDatePrinter.TimeZoneNumberRule INSTANCE_COLON = new FastDatePrinter.TimeZoneNumberRule(true);
      static final FastDatePrinter.TimeZoneNumberRule INSTANCE_NO_COLON = new FastDatePrinter.TimeZoneNumberRule(false);
      private final boolean colon;

      TimeZoneNumberRule(boolean colon) {
         this.colon = colon;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         int offset = calendar.get(15) + calendar.get(16);
         if (offset < 0) {
            buffer.append('-');
            offset = -offset;
         } else {
            buffer.append('+');
         }

         int hours = offset / 3600000;
         FastDatePrinter.appendDigits(buffer, hours);
         if (this.colon) {
            buffer.append(':');
         }

         int minutes = offset / 60000 - 60 * hours;
         FastDatePrinter.appendDigits(buffer, minutes);
      }

      @Override
      public int estimateLength() {
         return 5;
      }
   }

   private static class TwelveHourField implements FastDatePrinter.NumberRule {
      private final FastDatePrinter.NumberRule rule;

      TwelveHourField(FastDatePrinter.NumberRule rule) {
         this.rule = rule;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         int value = calendar.get(10);
         if (value == 0) {
            value = calendar.getLeastMaximum(10) + 1;
         }

         this.rule.appendTo(buffer, value);
      }

      @Override
      public void appendTo(Appendable buffer, int value) throws IOException {
         this.rule.appendTo(buffer, value);
      }

      @Override
      public int estimateLength() {
         return this.rule.estimateLength();
      }
   }

   private static class TwentyFourHourField implements FastDatePrinter.NumberRule {
      private final FastDatePrinter.NumberRule rule;

      TwentyFourHourField(FastDatePrinter.NumberRule rule) {
         this.rule = rule;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         int value = calendar.get(11);
         if (value == 0) {
            value = calendar.getMaximum(11) + 1;
         }

         this.rule.appendTo(buffer, value);
      }

      @Override
      public void appendTo(Appendable buffer, int value) throws IOException {
         this.rule.appendTo(buffer, value);
      }

      @Override
      public int estimateLength() {
         return this.rule.estimateLength();
      }
   }

   private static class TwoDigitMonthField implements FastDatePrinter.NumberRule {
      static final FastDatePrinter.TwoDigitMonthField INSTANCE = new FastDatePrinter.TwoDigitMonthField();

      TwoDigitMonthField() {
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         this.appendTo(buffer, calendar.get(2) + 1);
      }

      @Override
      public final void appendTo(Appendable buffer, int value) throws IOException {
         FastDatePrinter.appendDigits(buffer, value);
      }

      @Override
      public int estimateLength() {
         return 2;
      }
   }

   private static class TwoDigitNumberField implements FastDatePrinter.NumberRule {
      private final int field;

      TwoDigitNumberField(int field) {
         this.field = field;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         this.appendTo(buffer, calendar.get(this.field));
      }

      @Override
      public final void appendTo(Appendable buffer, int value) throws IOException {
         if (value < 100) {
            FastDatePrinter.appendDigits(buffer, value);
         } else {
            FastDatePrinter.appendFullDigits(buffer, value, 2);
         }
      }

      @Override
      public int estimateLength() {
         return 2;
      }
   }

   private static class TwoDigitYearField implements FastDatePrinter.NumberRule {
      static final FastDatePrinter.TwoDigitYearField INSTANCE = new FastDatePrinter.TwoDigitYearField();

      TwoDigitYearField() {
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         this.appendTo(buffer, calendar.get(1) % 100);
      }

      @Override
      public final void appendTo(Appendable buffer, int value) throws IOException {
         FastDatePrinter.appendDigits(buffer, value % 100);
      }

      @Override
      public int estimateLength() {
         return 2;
      }
   }

   private static class UnpaddedMonthField implements FastDatePrinter.NumberRule {
      static final FastDatePrinter.UnpaddedMonthField INSTANCE = new FastDatePrinter.UnpaddedMonthField();

      UnpaddedMonthField() {
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         this.appendTo(buffer, calendar.get(2) + 1);
      }

      @Override
      public final void appendTo(Appendable buffer, int value) throws IOException {
         if (value < 10) {
            buffer.append((char)(value + 48));
         } else {
            FastDatePrinter.appendDigits(buffer, value);
         }
      }

      @Override
      public int estimateLength() {
         return 2;
      }
   }

   private static class UnpaddedNumberField implements FastDatePrinter.NumberRule {
      private final int field;

      UnpaddedNumberField(int field) {
         this.field = field;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         this.appendTo(buffer, calendar.get(this.field));
      }

      @Override
      public final void appendTo(Appendable buffer, int value) throws IOException {
         if (value < 10) {
            buffer.append((char)(value + 48));
         } else if (value < 100) {
            FastDatePrinter.appendDigits(buffer, value);
         } else {
            FastDatePrinter.appendFullDigits(buffer, value, 1);
         }
      }

      @Override
      public int estimateLength() {
         return 4;
      }
   }

   private static class WeekYear implements FastDatePrinter.NumberRule {
      private final FastDatePrinter.NumberRule rule;

      WeekYear(FastDatePrinter.NumberRule rule) {
         this.rule = rule;
      }

      @Override
      public void appendTo(Appendable buffer, Calendar calendar) throws IOException {
         this.rule.appendTo(buffer, calendar.getWeekYear());
      }

      @Override
      public void appendTo(Appendable buffer, int value) throws IOException {
         this.rule.appendTo(buffer, value);
      }

      @Override
      public int estimateLength() {
         return this.rule.estimateLength();
      }
   }
}
