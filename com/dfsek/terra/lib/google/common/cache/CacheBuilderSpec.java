package com.dfsek.terra.lib.google.common.cache;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.MoreObjects;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Splitter;
import com.dfsek.terra.lib.google.common.base.Strings;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
public final class CacheBuilderSpec {
   private static final Splitter KEYS_SPLITTER = Splitter.on(',').trimResults();
   private static final Splitter KEY_VALUE_SPLITTER = Splitter.on('=').trimResults();
   private static final ImmutableMap<String, CacheBuilderSpec.ValueParser> VALUE_PARSERS = ImmutableMap.<String, CacheBuilderSpec.ValueParser>builder()
      .put("initialCapacity", new CacheBuilderSpec.InitialCapacityParser())
      .put("maximumSize", new CacheBuilderSpec.MaximumSizeParser())
      .put("maximumWeight", new CacheBuilderSpec.MaximumWeightParser())
      .put("concurrencyLevel", new CacheBuilderSpec.ConcurrencyLevelParser())
      .put("weakKeys", new CacheBuilderSpec.KeyStrengthParser(LocalCache.Strength.WEAK))
      .put("softValues", new CacheBuilderSpec.ValueStrengthParser(LocalCache.Strength.SOFT))
      .put("weakValues", new CacheBuilderSpec.ValueStrengthParser(LocalCache.Strength.WEAK))
      .put("recordStats", new CacheBuilderSpec.RecordStatsParser())
      .put("expireAfterAccess", new CacheBuilderSpec.AccessDurationParser())
      .put("expireAfterWrite", new CacheBuilderSpec.WriteDurationParser())
      .put("refreshAfterWrite", new CacheBuilderSpec.RefreshDurationParser())
      .put("refreshInterval", new CacheBuilderSpec.RefreshDurationParser())
      .buildOrThrow();
   @VisibleForTesting
   @Nullable Integer initialCapacity;
   @VisibleForTesting
   @Nullable Long maximumSize;
   @VisibleForTesting
   @Nullable Long maximumWeight;
   @VisibleForTesting
   @Nullable Integer concurrencyLevel;
   @VisibleForTesting
   LocalCache.@Nullable Strength keyStrength;
   @VisibleForTesting
   LocalCache.@Nullable Strength valueStrength;
   @VisibleForTesting
   @Nullable Boolean recordStats;
   @VisibleForTesting
   long writeExpirationDuration;
   @VisibleForTesting
   @Nullable TimeUnit writeExpirationTimeUnit;
   @VisibleForTesting
   long accessExpirationDuration;
   @VisibleForTesting
   @Nullable TimeUnit accessExpirationTimeUnit;
   @VisibleForTesting
   long refreshDuration;
   @VisibleForTesting
   @Nullable TimeUnit refreshTimeUnit;
   private final String specification;

   private CacheBuilderSpec(String specification) {
      this.specification = specification;
   }

   public static CacheBuilderSpec parse(String cacheBuilderSpecification) {
      CacheBuilderSpec spec = new CacheBuilderSpec(cacheBuilderSpecification);
      if (!cacheBuilderSpecification.isEmpty()) {
         for (String keyValuePair : KEYS_SPLITTER.split(cacheBuilderSpecification)) {
            List<String> keyAndValue = ImmutableList.copyOf(KEY_VALUE_SPLITTER.split(keyValuePair));
            Preconditions.checkArgument(!keyAndValue.isEmpty(), "blank key-value pair");
            Preconditions.checkArgument(keyAndValue.size() <= 2, "key-value pair %s with more than one equals sign", keyValuePair);
            String key = keyAndValue.get(0);
            CacheBuilderSpec.ValueParser valueParser = VALUE_PARSERS.get(key);
            Preconditions.checkArgument(valueParser != null, "unknown key %s", key);
            String value = keyAndValue.size() == 1 ? null : keyAndValue.get(1);
            valueParser.parse(spec, key, value);
         }
      }

      return spec;
   }

   public static CacheBuilderSpec disableCaching() {
      return parse("maximumSize=0");
   }

   CacheBuilder<Object, Object> toCacheBuilder() {
      CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
      if (this.initialCapacity != null) {
         builder.initialCapacity(this.initialCapacity);
      }

      if (this.maximumSize != null) {
         builder.maximumSize(this.maximumSize);
      }

      if (this.maximumWeight != null) {
         builder.maximumWeight(this.maximumWeight);
      }

      if (this.concurrencyLevel != null) {
         builder.concurrencyLevel(this.concurrencyLevel);
      }

      if (this.keyStrength != null) {
         switch (this.keyStrength) {
            case WEAK:
               builder.weakKeys();
               break;
            default:
               throw new AssertionError();
         }
      }

      if (this.valueStrength != null) {
         switch (this.valueStrength) {
            case WEAK:
               builder.weakValues();
               break;
            case SOFT:
               builder.softValues();
               break;
            default:
               throw new AssertionError();
         }
      }

      if (this.recordStats != null && this.recordStats) {
         builder.recordStats();
      }

      if (this.writeExpirationTimeUnit != null) {
         builder.expireAfterWrite(this.writeExpirationDuration, this.writeExpirationTimeUnit);
      }

      if (this.accessExpirationTimeUnit != null) {
         builder.expireAfterAccess(this.accessExpirationDuration, this.accessExpirationTimeUnit);
      }

      if (this.refreshTimeUnit != null) {
         builder.refreshAfterWrite(this.refreshDuration, this.refreshTimeUnit);
      }

      return builder;
   }

   public String toParsableString() {
      return this.specification;
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).addValue(this.toParsableString()).toString();
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(
         this.initialCapacity,
         this.maximumSize,
         this.maximumWeight,
         this.concurrencyLevel,
         this.keyStrength,
         this.valueStrength,
         this.recordStats,
         durationInNanos(this.writeExpirationDuration, this.writeExpirationTimeUnit),
         durationInNanos(this.accessExpirationDuration, this.accessExpirationTimeUnit),
         durationInNanos(this.refreshDuration, this.refreshTimeUnit)
      );
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof CacheBuilderSpec)) {
         return false;
      }

      CacheBuilderSpec that = (CacheBuilderSpec)obj;
      return Objects.equal(this.initialCapacity, that.initialCapacity)
         && Objects.equal(this.maximumSize, that.maximumSize)
         && Objects.equal(this.maximumWeight, that.maximumWeight)
         && Objects.equal(this.concurrencyLevel, that.concurrencyLevel)
         && Objects.equal(this.keyStrength, that.keyStrength)
         && Objects.equal(this.valueStrength, that.valueStrength)
         && Objects.equal(this.recordStats, that.recordStats)
         && Objects.equal(
            durationInNanos(this.writeExpirationDuration, this.writeExpirationTimeUnit),
            durationInNanos(that.writeExpirationDuration, that.writeExpirationTimeUnit)
         )
         && Objects.equal(
            durationInNanos(this.accessExpirationDuration, this.accessExpirationTimeUnit),
            durationInNanos(that.accessExpirationDuration, that.accessExpirationTimeUnit)
         )
         && Objects.equal(durationInNanos(this.refreshDuration, this.refreshTimeUnit), durationInNanos(that.refreshDuration, that.refreshTimeUnit));
   }

   private static @Nullable Long durationInNanos(long duration, @Nullable TimeUnit unit) {
      return unit == null ? null : unit.toNanos(duration);
   }

   private static String format(String format, Object... args) {
      return String.format(Locale.ROOT, format, args);
   }

   static class AccessDurationParser extends CacheBuilderSpec.DurationParser {
      @Override
      protected void parseDuration(CacheBuilderSpec spec, long duration, TimeUnit unit) {
         Preconditions.checkArgument(spec.accessExpirationTimeUnit == null, "expireAfterAccess already set");
         spec.accessExpirationDuration = duration;
         spec.accessExpirationTimeUnit = unit;
      }
   }

   static class ConcurrencyLevelParser extends CacheBuilderSpec.IntegerParser {
      @Override
      protected void parseInteger(CacheBuilderSpec spec, int value) {
         Preconditions.checkArgument(spec.concurrencyLevel == null, "concurrency level was already set to %s", spec.concurrencyLevel);
         spec.concurrencyLevel = value;
      }
   }

   abstract static class DurationParser implements CacheBuilderSpec.ValueParser {
      protected abstract void parseDuration(CacheBuilderSpec spec, long duration, TimeUnit unit);

      @Override
      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("value of key " + key + " omitted");
         }

         try {
            char lastChar = value.charAt(value.length() - 1);
            TimeUnit timeUnit;
            switch (lastChar) {
               case 'd':
                  timeUnit = TimeUnit.DAYS;
                  break;
               case 'h':
                  timeUnit = TimeUnit.HOURS;
                  break;
               case 'm':
                  timeUnit = TimeUnit.MINUTES;
                  break;
               case 's':
                  timeUnit = TimeUnit.SECONDS;
                  break;
               default:
                  throw new IllegalArgumentException(CacheBuilderSpec.format("key %s invalid unit: was %s, must end with one of [dhms]", key, value));
            }

            long duration = Long.parseLong(value.substring(0, value.length() - 1));
            this.parseDuration(spec, duration, timeUnit);
         } catch (NumberFormatException e) {
            throw new IllegalArgumentException(CacheBuilderSpec.format("key %s value set to %s, must be integer", key, value));
         }
      }
   }

   static class InitialCapacityParser extends CacheBuilderSpec.IntegerParser {
      @Override
      protected void parseInteger(CacheBuilderSpec spec, int value) {
         Preconditions.checkArgument(spec.initialCapacity == null, "initial capacity was already set to %s", spec.initialCapacity);
         spec.initialCapacity = value;
      }
   }

   abstract static class IntegerParser implements CacheBuilderSpec.ValueParser {
      protected abstract void parseInteger(CacheBuilderSpec spec, int value);

      @Override
      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("value of key " + key + " omitted");
         }

         try {
            this.parseInteger(spec, Integer.parseInt(value));
         } catch (NumberFormatException e) {
            throw new IllegalArgumentException(CacheBuilderSpec.format("key %s value set to %s, must be integer", key, value), e);
         }
      }
   }

   static class KeyStrengthParser implements CacheBuilderSpec.ValueParser {
      private final LocalCache.Strength strength;

      public KeyStrengthParser(LocalCache.Strength strength) {
         this.strength = strength;
      }

      @Override
      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         Preconditions.checkArgument(value == null, "key %s does not take values", key);
         Preconditions.checkArgument(spec.keyStrength == null, "%s was already set to %s", key, spec.keyStrength);
         spec.keyStrength = this.strength;
      }
   }

   abstract static class LongParser implements CacheBuilderSpec.ValueParser {
      protected abstract void parseLong(CacheBuilderSpec spec, long value);

      @Override
      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("value of key " + key + " omitted");
         }

         try {
            this.parseLong(spec, Long.parseLong(value));
         } catch (NumberFormatException e) {
            throw new IllegalArgumentException(CacheBuilderSpec.format("key %s value set to %s, must be integer", key, value), e);
         }
      }
   }

   static class MaximumSizeParser extends CacheBuilderSpec.LongParser {
      @Override
      protected void parseLong(CacheBuilderSpec spec, long value) {
         Preconditions.checkArgument(spec.maximumSize == null, "maximum size was already set to %s", spec.maximumSize);
         Preconditions.checkArgument(spec.maximumWeight == null, "maximum weight was already set to %s", spec.maximumWeight);
         spec.maximumSize = value;
      }
   }

   static class MaximumWeightParser extends CacheBuilderSpec.LongParser {
      @Override
      protected void parseLong(CacheBuilderSpec spec, long value) {
         Preconditions.checkArgument(spec.maximumWeight == null, "maximum weight was already set to %s", spec.maximumWeight);
         Preconditions.checkArgument(spec.maximumSize == null, "maximum size was already set to %s", spec.maximumSize);
         spec.maximumWeight = value;
      }
   }

   static class RecordStatsParser implements CacheBuilderSpec.ValueParser {
      @Override
      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         Preconditions.checkArgument(value == null, "recordStats does not take values");
         Preconditions.checkArgument(spec.recordStats == null, "recordStats already set");
         spec.recordStats = true;
      }
   }

   static class RefreshDurationParser extends CacheBuilderSpec.DurationParser {
      @Override
      protected void parseDuration(CacheBuilderSpec spec, long duration, TimeUnit unit) {
         Preconditions.checkArgument(spec.refreshTimeUnit == null, "refreshAfterWrite already set");
         spec.refreshDuration = duration;
         spec.refreshTimeUnit = unit;
      }
   }

   private interface ValueParser {
      void parse(CacheBuilderSpec spec, String key, @Nullable String value);
   }

   static class ValueStrengthParser implements CacheBuilderSpec.ValueParser {
      private final LocalCache.Strength strength;

      public ValueStrengthParser(LocalCache.Strength strength) {
         this.strength = strength;
      }

      @Override
      public void parse(CacheBuilderSpec spec, String key, @Nullable String value) {
         Preconditions.checkArgument(value == null, "key %s does not take values", key);
         Preconditions.checkArgument(spec.valueStrength == null, "%s was already set to %s", key, spec.valueStrength);
         spec.valueStrength = this.strength;
      }
   }

   static class WriteDurationParser extends CacheBuilderSpec.DurationParser {
      @Override
      protected void parseDuration(CacheBuilderSpec spec, long duration, TimeUnit unit) {
         Preconditions.checkArgument(spec.writeExpirationTimeUnit == null, "expireAfterWrite already set");
         spec.writeExpirationDuration = duration;
         spec.writeExpirationTimeUnit = unit;
      }
   }
}
