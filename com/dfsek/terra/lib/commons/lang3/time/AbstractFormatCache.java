package com.dfsek.terra.lib.commons.lang3.time;

import com.dfsek.terra.lib.commons.lang3.LocaleUtils;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class AbstractFormatCache<F extends Format> {
   static final int NONE = -1;
   private static final ConcurrentMap<AbstractFormatCache.ArrayKey, String> cDateTimeInstanceCache = new ConcurrentHashMap<>(7);
   private final ConcurrentMap<AbstractFormatCache.ArrayKey, F> cInstanceCache = new ConcurrentHashMap<>(7);

   static String getPatternForStyle(Integer dateStyle, Integer timeStyle, Locale locale) {
      Locale safeLocale = LocaleUtils.toLocale(locale);
      AbstractFormatCache.ArrayKey key = new AbstractFormatCache.ArrayKey(dateStyle, timeStyle, safeLocale);
      return cDateTimeInstanceCache.computeIfAbsent(key, k -> {
         try {
            DateFormat formatter;
            if (dateStyle == null) {
               formatter = DateFormat.getTimeInstance(timeStyle, safeLocale);
            } else if (timeStyle == null) {
               formatter = DateFormat.getDateInstance(dateStyle, safeLocale);
            } else {
               formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle, safeLocale);
            }

            return ((SimpleDateFormat)formatter).toPattern();
         } catch (ClassCastException ex) {
            throw new IllegalArgumentException("No date time pattern for locale: " + safeLocale);
         }
      });
   }

   protected abstract F createInstance(String var1, TimeZone var2, Locale var3);

   F getDateInstance(int dateStyle, TimeZone timeZone, Locale locale) {
      return this.getDateTimeInstance(dateStyle, null, timeZone, locale);
   }

   F getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone, Locale locale) {
      return this.getDateTimeInstance(dateStyle, Integer.valueOf(timeStyle), timeZone, locale);
   }

   private F getDateTimeInstance(Integer dateStyle, Integer timeStyle, TimeZone timeZone, Locale locale) {
      locale = LocaleUtils.toLocale(locale);
      String pattern = getPatternForStyle(dateStyle, timeStyle, locale);
      return this.getInstance(pattern, timeZone, locale);
   }

   public F getInstance() {
      return this.getDateTimeInstance(3, 3, TimeZone.getDefault(), Locale.getDefault());
   }

   public F getInstance(String pattern, TimeZone timeZone, Locale locale) {
      Objects.requireNonNull(pattern, "pattern");
      TimeZone actualTimeZone = TimeZones.toTimeZone(timeZone);
      Locale actualLocale = LocaleUtils.toLocale(locale);
      AbstractFormatCache.ArrayKey key = new AbstractFormatCache.ArrayKey(pattern, actualTimeZone, actualLocale);
      return this.cInstanceCache.computeIfAbsent(key, k -> this.createInstance(pattern, actualTimeZone, actualLocale));
   }

   F getTimeInstance(int timeStyle, TimeZone timeZone, Locale locale) {
      return this.getDateTimeInstance(null, timeStyle, timeZone, locale);
   }

   private static final class ArrayKey {
      private final Object[] keys;
      private final int hashCode;

      private static int computeHashCode(Object[] keys) {
         int prime = 31;
         int result = 1;
         return 31 * result + Arrays.hashCode(keys);
      }

      ArrayKey(Object... keys) {
         this.keys = keys;
         this.hashCode = computeHashCode(keys);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (obj == null) {
            return false;
         }

         if (this.getClass() != obj.getClass()) {
            return false;
         }

         AbstractFormatCache.ArrayKey other = (AbstractFormatCache.ArrayKey)obj;
         return Arrays.deepEquals(this.keys, other.keys);
      }

      @Override
      public int hashCode() {
         return this.hashCode;
      }
   }
}
