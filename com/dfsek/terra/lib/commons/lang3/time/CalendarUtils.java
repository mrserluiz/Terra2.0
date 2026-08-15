package com.dfsek.terra.lib.commons.lang3.time;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CalendarUtils {
   public static final CalendarUtils INSTANCE = getInstance();
   private final Calendar calendar;
   private final Locale locale;

   public static CalendarUtils getInstance() {
      return new CalendarUtils(Calendar.getInstance());
   }

   static CalendarUtils getInstance(Locale locale) {
      return new CalendarUtils(Calendar.getInstance(locale), locale);
   }

   public static LocalDateTime toLocalDateTime(Calendar calendar) {
      return LocalDateTime.ofInstant(calendar.toInstant(), toZoneId(calendar));
   }

   public static OffsetDateTime toOffsetDateTime(Calendar calendar) {
      return OffsetDateTime.ofInstant(calendar.toInstant(), toZoneId(calendar));
   }

   public static ZonedDateTime toZonedDateTime(Calendar calendar) {
      return ZonedDateTime.ofInstant(calendar.toInstant(), toZoneId(calendar));
   }

   private static ZoneId toZoneId(Calendar calendar) {
      return calendar.getTimeZone().toZoneId();
   }

   public CalendarUtils(Calendar calendar) {
      this(calendar, Locale.getDefault());
   }

   CalendarUtils(Calendar calendar, Locale locale) {
      this.calendar = Objects.requireNonNull(calendar, "calendar");
      this.locale = Objects.requireNonNull(locale, "locale");
   }

   public int getDayOfMonth() {
      return this.calendar.get(5);
   }

   public int getDayOfYear() {
      return this.calendar.get(6);
   }

   public int getMonth() {
      return this.calendar.get(2);
   }

   String[] getMonthDisplayNames(int style) {
      Map<String, Integer> displayNames = this.calendar.getDisplayNames(2, style, this.locale);
      if (displayNames == null) {
         return null;
      }

      String[] monthNames = new String[displayNames.size()];
      displayNames.forEach((k, v) -> monthNames[v] = k);
      return monthNames;
   }

   String[] getStandaloneLongMonthNames() {
      return this.getMonthDisplayNames(32770);
   }

   String[] getStandaloneShortMonthNames() {
      return this.getMonthDisplayNames(32769);
   }

   public int getYear() {
      return this.calendar.get(1);
   }

   public LocalDateTime toLocalDateTime() {
      return toLocalDateTime(this.calendar);
   }

   public OffsetDateTime toOffsetDateTime() {
      return toOffsetDateTime(this.calendar);
   }

   public ZonedDateTime toZonedDateTime() {
      return toZonedDateTime(this.calendar);
   }
}
