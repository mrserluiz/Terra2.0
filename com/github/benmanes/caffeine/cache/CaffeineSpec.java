package com.github.benmanes.caffeine.cache;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class CaffeineSpec {
   static final String SPLIT_OPTIONS = ",";
   static final String SPLIT_KEY_VALUE = "=";
   final String specification;
   int initialCapacity = -1;
   long maximumWeight = -1L;
   long maximumSize = -1L;
   boolean recordStats;
   Caffeine.@Nullable Strength keyStrength;
   Caffeine.@Nullable Strength valueStrength;
   @Nullable Duration expireAfterWrite;
   @Nullable Duration expireAfterAccess;
   @Nullable Duration refreshAfterWrite;

   private CaffeineSpec(String specification) {
      this.specification = Objects.requireNonNull(specification);
      String[] options = specification.split(",");

      for (String option : options) {
         this.parseOption(option.strip());
      }
   }

   Caffeine<Object, Object> toBuilder() {
      Caffeine<Object, Object> builder = Caffeine.newBuilder();
      if (this.initialCapacity != -1) {
         builder.initialCapacity(this.initialCapacity);
      }

      if (this.maximumSize != -1L) {
         builder.maximumSize(this.maximumSize);
      }

      if (this.maximumWeight != -1L) {
         builder.maximumWeight(this.maximumWeight);
      }

      if (this.keyStrength != null) {
         Caffeine.requireState(this.keyStrength == Caffeine.Strength.WEAK);
         builder.weakKeys();
      }

      if (this.valueStrength != null) {
         if (this.valueStrength == Caffeine.Strength.WEAK) {
            builder.weakValues();
         } else if (this.valueStrength == Caffeine.Strength.SOFT) {
            builder.softValues();
         }
      }

      if (this.expireAfterWrite != null) {
         builder.expireAfterWrite(this.expireAfterWrite);
      }

      if (this.expireAfterAccess != null) {
         builder.expireAfterAccess(this.expireAfterAccess);
      }

      if (this.refreshAfterWrite != null) {
         builder.refreshAfterWrite(this.refreshAfterWrite);
      }

      if (this.recordStats) {
         builder.recordStats();
      }

      return builder;
   }

   public static CaffeineSpec parse(String specification) {
      return new CaffeineSpec(specification);
   }

   void parseOption(String option) {
      if (!option.isEmpty()) {
         String[] keyAndValue = option.split("=", 3);
         Caffeine.requireArgument(keyAndValue.length >= 1, "blank key-value pair");
         Caffeine.requireArgument(keyAndValue.length <= 2, "key-value pair %s with more than one equals sign", option);
         String key = keyAndValue[0].strip();
         String value = keyAndValue.length == 1 ? null : keyAndValue[1].strip();
         this.configure(option, key, value);
      }
   }

   void configure(String option, String key, @Nullable String value) {
      switch (key) {
         case "initialCapacity":
            this.initialCapacity(key, value);
            return;
         case "maximumSize":
            this.maximumSize(key, value);
            return;
         case "maximumWeight":
            this.maximumWeight(key, value);
            return;
         case "weakKeys":
            this.weakKeys(value);
            return;
         case "weakValues":
            this.valueStrength(key, value, Caffeine.Strength.WEAK);
            return;
         case "softValues":
            this.valueStrength(key, value, Caffeine.Strength.SOFT);
            return;
         case "expireAfterAccess":
            this.expireAfterAccess(key, value);
            return;
         case "expireAfterWrite":
            this.expireAfterWrite(key, value);
            return;
         case "refreshAfterWrite":
            this.refreshAfterWrite(key, value);
            return;
         case "recordStats":
            this.recordStats(value);
            return;
         default:
            throw new IllegalArgumentException("Invalid option " + option);
      }
   }

   void initialCapacity(String key, @Nullable String value) {
      Caffeine.requireArgument(this.initialCapacity == -1, "initial capacity was already set to %,d", this.initialCapacity);
      this.initialCapacity = parseInt(key, value);
   }

   void maximumSize(String key, @Nullable String value) {
      Caffeine.requireArgument(this.maximumSize == -1L, "maximum size was already set to %,d", this.maximumSize);
      Caffeine.requireArgument(this.maximumWeight == -1L, "maximum weight was already set to %,d", this.maximumWeight);
      this.maximumSize = parseLong(key, value);
   }

   void maximumWeight(String key, @Nullable String value) {
      Caffeine.requireArgument(this.maximumWeight == -1L, "maximum weight was already set to %,d", this.maximumWeight);
      Caffeine.requireArgument(this.maximumSize == -1L, "maximum size was already set to %,d", this.maximumSize);
      this.maximumWeight = parseLong(key, value);
   }

   void weakKeys(@Nullable String value) {
      Caffeine.requireArgument(value == null, "weak keys does not take a value");
      Caffeine.requireArgument(this.keyStrength == null, "weak keys was already set");
      this.keyStrength = Caffeine.Strength.WEAK;
   }

   void valueStrength(String key, @Nullable String value, Caffeine.Strength strength) {
      Caffeine.requireArgument(value == null, "%s does not take a value", key);
      Caffeine.requireArgument(this.valueStrength == null, "%s was already set to %s", key, this.valueStrength);
      this.valueStrength = strength;
   }

   void expireAfterAccess(String key, @Nullable String value) {
      Caffeine.requireArgument(this.expireAfterAccess == null, "expireAfterAccess was already set");
      this.expireAfterAccess = parseDuration(key, value);
   }

   void expireAfterWrite(String key, @Nullable String value) {
      Caffeine.requireArgument(this.expireAfterWrite == null, "expireAfterWrite was already set");
      this.expireAfterWrite = parseDuration(key, value);
   }

   void refreshAfterWrite(String key, @Nullable String value) {
      Caffeine.requireArgument(this.refreshAfterWrite == null, "refreshAfterWrite was already set");
      this.refreshAfterWrite = parseDuration(key, value);
   }

   void recordStats(@Nullable String value) {
      Caffeine.requireArgument(value == null, "record stats does not take a value");
      Caffeine.requireArgument(!this.recordStats, "record stats was already set");
      this.recordStats = true;
   }

   static int parseInt(String key, @Nullable String value) {
      Caffeine.requireArgument(value != null && !value.isEmpty(), "value of key %s was omitted", key);

      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException(String.format(Locale.US, "key %s value was set to %s, must be an integer", key, value), e);
      }
   }

   static long parseLong(String key, @Nullable String value) {
      Caffeine.requireArgument(value != null && !value.isEmpty(), "value of key %s was omitted", key);

      try {
         return Long.parseLong(value);
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException(String.format(Locale.US, "key %s value was set to %s, must be a long", key, value), e);
      }
   }

   static Duration parseDuration(String key, @Nullable String value) {
      Caffeine.requireArgument(value != null && !value.isEmpty(), "value of key %s omitted", key);
      Objects.requireNonNull(value);
      boolean isIsoFormat = value.contains("p") || value.contains("P");
      Duration duration = isIsoFormat ? parseIsoDuration(key, value) : parseSimpleDuration(key, value);
      Caffeine.requireArgument(!duration.isNegative(), "key %s invalid format; was %s, but the duration cannot be negative", key, value);
      return duration;
   }

   static Duration parseIsoDuration(String key, String value) {
      try {
         return Duration.parse(value);
      } catch (DateTimeParseException e) {
         throw new IllegalArgumentException(String.format(Locale.US, "key %s invalid format; was %s, but the duration cannot be parsed", key, value), e);
      }
   }

   static Duration parseSimpleDuration(String key, String value) {
      long duration = parseLong(key, value.substring(0, value.length() - 1));
      TimeUnit unit = parseTimeUnit(key, value);
      return Duration.ofNanos(unit.toNanos(duration));
   }

   static TimeUnit parseTimeUnit(String key, @Nullable String value) {
      Caffeine.requireArgument(value != null && !value.isEmpty(), "value of key %s omitted", key);
      Objects.requireNonNull(value);
      char lastChar = Character.toLowerCase(value.charAt(value.length() - 1));
      switch (lastChar) {
         case 'd':
            return TimeUnit.DAYS;
         case 'h':
            return TimeUnit.HOURS;
         case 'm':
            return TimeUnit.MINUTES;
         case 's':
            return TimeUnit.SECONDS;
         default:
            throw new IllegalArgumentException(String.format(Locale.US, "key %s invalid format; was %s, must end with one of [dDhHmMsS]", key, value));
      }
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      }

      if (!(o instanceof CaffeineSpec)) {
         return false;
      }

      CaffeineSpec spec = (CaffeineSpec)o;
      return Objects.equals(this.refreshAfterWrite, spec.refreshAfterWrite)
         && Objects.equals(this.expireAfterAccess, spec.expireAfterAccess)
         && Objects.equals(this.expireAfterWrite, spec.expireAfterWrite)
         && this.initialCapacity == spec.initialCapacity
         && this.maximumWeight == spec.maximumWeight
         && this.valueStrength == spec.valueStrength
         && this.keyStrength == spec.keyStrength
         && this.maximumSize == spec.maximumSize
         && this.recordStats == spec.recordStats;
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.initialCapacity,
         this.maximumSize,
         this.maximumWeight,
         this.keyStrength,
         this.valueStrength,
         this.recordStats,
         this.expireAfterWrite,
         this.expireAfterAccess,
         this.refreshAfterWrite
      );
   }

   public String toParsableString() {
      return this.specification;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{" + this.toParsableString() + "}";
   }
}
