package com.dfsek.terra.lib.commons.lang3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LocaleUtils {
   private static final char UNDERSCORE = '_';
   private static final String UNDETERMINED = "und";
   private static final char DASH = '-';
   private static final ConcurrentMap<String, List<Locale>> cLanguagesByCountry = new ConcurrentHashMap<>();
   private static final ConcurrentMap<String, List<Locale>> cCountriesByLanguage = new ConcurrentHashMap<>();

   public static List<Locale> availableLocaleList() {
      return LocaleUtils.SyncAvoid.AVAILABLE_LOCALE_LIST;
   }

   private static List<Locale> availableLocaleList(Predicate<Locale> predicate) {
      return availableLocaleList().stream().filter(predicate).collect(Collectors.toList());
   }

   public static Set<Locale> availableLocaleSet() {
      return LocaleUtils.SyncAvoid.AVAILABLE_LOCALE_SET;
   }

   public static List<Locale> countriesByLanguage(String languageCode) {
      return languageCode == null
         ? Collections.emptyList()
         : cCountriesByLanguage.computeIfAbsent(
            languageCode,
            lc -> Collections.unmodifiableList(
               availableLocaleList(locale -> languageCode.equals(locale.getLanguage()) && !locale.getCountry().isEmpty() && locale.getVariant().isEmpty())
            )
         );
   }

   public static boolean isAvailableLocale(Locale locale) {
      return availableLocaleSet().contains(locale);
   }

   private static boolean isISO3166CountryCode(String str) {
      return StringUtils.isAllUpperCase(str) && str.length() == 2;
   }

   private static boolean isISO639LanguageCode(String str) {
      return StringUtils.isAllLowerCase(str) && (str.length() == 2 || str.length() == 3);
   }

   public static boolean isLanguageUndetermined(Locale locale) {
      return locale == null || "und".equals(locale.toLanguageTag());
   }

   private static boolean isNumericAreaCode(String str) {
      return StringUtils.isNumeric(str) && str.length() == 3;
   }

   public static List<Locale> languagesByCountry(String countryCode) {
      return countryCode == null
         ? Collections.emptyList()
         : cLanguagesByCountry.computeIfAbsent(
            countryCode,
            k -> Collections.unmodifiableList(availableLocaleList(locale -> countryCode.equals(locale.getCountry()) && locale.getVariant().isEmpty()))
         );
   }

   public static List<Locale> localeLookupList(Locale locale) {
      return localeLookupList(locale, locale);
   }

   public static List<Locale> localeLookupList(Locale locale, Locale defaultLocale) {
      List<Locale> list = new ArrayList<>(4);
      if (locale != null) {
         list.add(locale);
         if (!locale.getVariant().isEmpty()) {
            list.add(new Locale(locale.getLanguage(), locale.getCountry()));
         }

         if (!locale.getCountry().isEmpty()) {
            list.add(new Locale(locale.getLanguage(), ""));
         }

         if (!list.contains(defaultLocale)) {
            list.add(defaultLocale);
         }
      }

      return Collections.unmodifiableList(list);
   }

   private static Locale parseLocale(String str) {
      if (isISO639LanguageCode(str)) {
         return new Locale(str);
      }

      int limit = 3;
      char separator = (char)(str.indexOf(95) != -1 ? 95 : 45);
      String[] segments = str.split(String.valueOf(separator), 3);
      String language = segments[0];
      if (segments.length == 2) {
         String country = segments[1];
         if (isISO639LanguageCode(language) && isISO3166CountryCode(country) || isNumericAreaCode(country)) {
            return new Locale(language, country);
         }
      } else if (segments.length == 3) {
         String country = segments[1];
         String variant = segments[2];
         if (isISO639LanguageCode(language) && (country.isEmpty() || isISO3166CountryCode(country) || isNumericAreaCode(country)) && !variant.isEmpty()) {
            return new Locale(language, country, variant);
         }
      }

      throw new IllegalArgumentException("Invalid locale format: " + str);
   }

   public static Locale toLocale(Locale locale) {
      return locale != null ? locale : Locale.getDefault();
   }

   public static Locale toLocale(String str) {
      if (str == null) {
         return null;
      } else if (str.isEmpty()) {
         return new Locale("", "");
      } else if (str.contains("#")) {
         throw new IllegalArgumentException("Invalid locale format: " + str);
      } else {
         int len = str.length();
         if (len < 2) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
         } else {
            char ch0 = str.charAt(0);
            if (ch0 != '_' && ch0 != '-') {
               return parseLocale(str);
            } else if (len < 3) {
               throw new IllegalArgumentException("Invalid locale format: " + str);
            } else {
               char ch1 = str.charAt(1);
               char ch2 = str.charAt(2);
               if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
                  throw new IllegalArgumentException("Invalid locale format: " + str);
               } else if (len == 3) {
                  return new Locale("", str.substring(1, 3));
               } else if (len < 5) {
                  throw new IllegalArgumentException("Invalid locale format: " + str);
               } else if (str.charAt(3) != ch0) {
                  throw new IllegalArgumentException("Invalid locale format: " + str);
               } else {
                  return new Locale("", str.substring(1, 3), str.substring(4));
               }
            }
         }
      }
   }

   static class SyncAvoid {
      private static final List<Locale> AVAILABLE_LOCALE_LIST;
      private static final Set<Locale> AVAILABLE_LOCALE_SET;

      static {
         List<Locale> list = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales()));
         AVAILABLE_LOCALE_LIST = Collections.unmodifiableList(list);
         AVAILABLE_LOCALE_SET = Collections.unmodifiableSet(new HashSet<>(list));
      }
   }
}
