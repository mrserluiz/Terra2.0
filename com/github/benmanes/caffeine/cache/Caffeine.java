package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.ConcurrentStatsCounter;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.FormatMethod;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Caffeine<K, V> {
   static final Supplier<StatsCounter> ENABLED_STATS_COUNTER_SUPPLIER = ConcurrentStatsCounter::new;
   static final Logger logger = System.getLogger(Caffeine.class.getName());
   static final Duration MIN_DURATION = Duration.ofNanos(Long.MIN_VALUE);
   static final Duration MAX_DURATION = Duration.ofNanos(Long.MAX_VALUE);
   static final double DEFAULT_LOAD_FACTOR = 0.75;
   static final int UNSET_INT = -1;
   static final int DEFAULT_INITIAL_CAPACITY = 16;
   static final int DEFAULT_EXPIRATION_NANOS = 0;
   static final int DEFAULT_REFRESH_NANOS = 0;
   boolean strictParsing = true;
   boolean interner;
   long maximumSize = -1L;
   long maximumWeight = -1L;
   int initialCapacity = -1;
   long expireAfterWriteNanos = -1L;
   long expireAfterAccessNanos = -1L;
   long refreshAfterWriteNanos = -1L;
   @Nullable RemovalListener<? super K, ? super V> evictionListener;
   @Nullable RemovalListener<? super K, ? super V> removalListener;
   @Nullable Supplier<StatsCounter> statsCounterSupplier;
   @Nullable Weigher<? super K, ? super V> weigher;
   @Nullable Expiry<? super K, ? super V> expiry;
   @Nullable Scheduler scheduler;
   @Nullable Executor executor;
   @Nullable Ticker ticker;
   Caffeine.@Nullable Strength keyStrength;
   Caffeine.@Nullable Strength valueStrength;

   private Caffeine() {
   }

   @FormatMethod
   static void requireArgument(boolean expression, String template, @Nullable Object... args) {
      if (!expression) {
         throw new IllegalArgumentException(String.format(Locale.US, template, args));
      }
   }

   static void requireArgument(boolean expression, String message) {
      if (!expression) {
         throw new IllegalArgumentException(message);
      }
   }

   static void requireArgument(boolean expression) {
      if (!expression) {
         throw new IllegalArgumentException();
      }
   }

   static void requireState(boolean expression) {
      if (!expression) {
         throw new IllegalStateException();
      }
   }

   @FormatMethod
   static void requireState(boolean expression, String template, @Nullable Object... args) {
      if (!expression) {
         throw new IllegalStateException(String.format(Locale.US, template, args));
      }
   }

   static int ceilingPowerOfTwo(int x) {
      return 1 << -Integer.numberOfLeadingZeros(x - 1);
   }

   static long ceilingPowerOfTwo(long x) {
      return 1L << -Long.numberOfLeadingZeros(x - 1L);
   }

   static int calculateHashMapCapacity(int numMappings) {
      return (int)Math.ceil(numMappings / 0.75);
   }

   static int calculateHashMapCapacity(Iterable<?> iterable) {
      return iterable instanceof Collection ? calculateHashMapCapacity(((Collection)iterable).size()) : 16;
   }

   static long toNanosSaturated(Duration duration) {
      return duration.isNegative()
         ? (duration.compareTo(MIN_DURATION) <= 0 ? Long.MIN_VALUE : duration.toNanos())
         : (duration.compareTo(MAX_DURATION) >= 0 ? Long.MAX_VALUE : duration.toNanos());
   }

   static boolean hasMethodOverride(Class<?> clazz, Object instance, String methodName, Class<?>... parameterTypes) {
      try {
         Method instanceMethod = instance.getClass().getMethod(methodName, parameterTypes);
         Method classMethod = clazz.getMethod(methodName, parameterTypes);
         return !instanceMethod.equals(classMethod);
      } catch (NoSuchMethodException | SecurityException e) {
         logger.log(
            Level.WARNING, "Cannot determine if {0} overrides {1}({2})", instance.getClass().getSimpleName(), methodName, Arrays.toString(parameterTypes), e
         );
         return false;
      }
   }

   public static Caffeine<Object, Object> newBuilder() {
      return new Caffeine<>();
   }

   static <K> BoundedLocalCache<K, Boolean> newWeakInterner() {
      Caffeine<K, Boolean> builder = new Caffeine<K, Boolean>().executor(Runnable::run).weakKeys();
      builder.interner = true;
      return LocalCacheFactory.newBoundedLocalCache(builder, null, false);
   }

   public static Caffeine<Object, Object> from(CaffeineSpec spec) {
      Caffeine<Object, Object> builder = spec.toBuilder();
      builder.strictParsing = false;
      return builder;
   }

   public static Caffeine<Object, Object> from(String spec) {
      return from(CaffeineSpec.parse(spec));
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> initialCapacity(int initialCapacity) {
      requireState(this.initialCapacity == -1, "initial capacity was already set to %s", this.initialCapacity);
      requireArgument(initialCapacity >= 0);
      this.initialCapacity = initialCapacity;
      return this;
   }

   boolean hasInitialCapacity() {
      return this.initialCapacity != -1;
   }

   int getInitialCapacity() {
      return this.hasInitialCapacity() ? this.initialCapacity : 16;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> executor(Executor executor) {
      requireState(this.executor == null, "executor was already set to %s", this.executor);
      this.executor = Objects.requireNonNull(executor);
      return this;
   }

   Executor getExecutor() {
      return this.executor == null ? ForkJoinPool.commonPool() : this.executor;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> scheduler(Scheduler scheduler) {
      requireState(this.scheduler == null, "scheduler was already set to %s", this.scheduler);
      this.scheduler = Objects.requireNonNull(scheduler);
      return this;
   }

   Scheduler getScheduler() {
      if (this.scheduler == null || this.scheduler == Scheduler.disabledScheduler()) {
         return Scheduler.disabledScheduler();
      } else {
         return this.scheduler == Scheduler.systemScheduler() ? this.scheduler : Scheduler.guardedScheduler(this.scheduler);
      }
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> maximumSize(long maximumSize) {
      requireState(this.maximumSize == -1L, "maximum size was already set to %s", this.maximumSize);
      requireState(this.maximumWeight == -1L, "maximum weight was already set to %s", this.maximumWeight);
      requireState(this.weigher == null, "maximum size cannot be combined with weigher");
      requireArgument(maximumSize >= 0L, "maximum size must not be negative");
      this.maximumSize = maximumSize;
      return this;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> maximumWeight(long maximumWeight) {
      requireState(this.maximumWeight == -1L, "maximum weight was already set to %s", this.maximumWeight);
      requireState(this.maximumSize == -1L, "maximum size was already set to %s", this.maximumSize);
      requireArgument(maximumWeight >= 0L, "maximum weight must not be negative");
      this.maximumWeight = maximumWeight;
      return this;
   }

   @CanIgnoreReturnValue
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> weigher(Weigher<? super K1, ? super V1> weigher) {
      Objects.requireNonNull(weigher);
      requireState(this.weigher == null, "weigher was already set to %s", this.weigher);
      requireState(!this.strictParsing || this.maximumSize == -1L, "weigher cannot be combined with maximum size");
      Caffeine<K1, V1> self = this;
      self.weigher = weigher;
      return self;
   }

   boolean evicts() {
      return this.getMaximum() != -1L;
   }

   boolean isWeighted() {
      return this.weigher != null;
   }

   long getMaximum() {
      return this.isWeighted() ? this.maximumWeight : this.maximumSize;
   }

   <K1 extends K, V1 extends V> Weigher<K1, V1> getWeigher(boolean isAsync) {
      Weigher<K1, V1> delegate = this.weigher != null && this.weigher != Weigher.singletonWeigher()
         ? Weigher.boundedWeigher(this.weigher)
         : Weigher.singletonWeigher();
      return isAsync ? new Async.AsyncWeigher<>(delegate) : delegate;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> weakKeys() {
      requireState(this.keyStrength == null, "Key strength was already set to %s", this.keyStrength);
      this.keyStrength = Caffeine.Strength.WEAK;
      return this;
   }

   boolean isStrongKeys() {
      return this.keyStrength == null;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> weakValues() {
      requireState(this.valueStrength == null, "Value strength was already set to %s", this.valueStrength);
      this.valueStrength = Caffeine.Strength.WEAK;
      return this;
   }

   boolean isStrongValues() {
      return this.valueStrength == null;
   }

   boolean isWeakValues() {
      return this.valueStrength == Caffeine.Strength.WEAK;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> softValues() {
      requireState(this.valueStrength == null, "Value strength was already set to %s", this.valueStrength);
      this.valueStrength = Caffeine.Strength.SOFT;
      return this;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> expireAfterWrite(Duration duration) {
      return this.expireAfterWrite(toNanosSaturated(duration), TimeUnit.NANOSECONDS);
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> expireAfterWrite(long duration, TimeUnit unit) {
      requireState(this.expireAfterWriteNanos == -1L, "expireAfterWrite was already set to %s ns", this.expireAfterWriteNanos);
      requireState(this.expiry == null, "expireAfterWrite may not be used with variable expiration");
      requireArgument(duration >= 0L, "duration cannot be negative: %s %s", duration, unit);
      this.expireAfterWriteNanos = unit.toNanos(duration);
      return this;
   }

   long getExpiresAfterWriteNanos() {
      return this.expiresAfterWrite() ? this.expireAfterWriteNanos : 0L;
   }

   boolean expiresAfterWrite() {
      return this.expireAfterWriteNanos != -1L;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> expireAfterAccess(Duration duration) {
      return this.expireAfterAccess(toNanosSaturated(duration), TimeUnit.NANOSECONDS);
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> expireAfterAccess(long duration, TimeUnit unit) {
      requireState(this.expireAfterAccessNanos == -1L, "expireAfterAccess was already set to %s ns", this.expireAfterAccessNanos);
      requireState(this.expiry == null, "expireAfterAccess may not be used with variable expiration");
      requireArgument(duration >= 0L, "duration cannot be negative: %s %s", duration, unit);
      this.expireAfterAccessNanos = unit.toNanos(duration);
      return this;
   }

   long getExpiresAfterAccessNanos() {
      return this.expiresAfterAccess() ? this.expireAfterAccessNanos : 0L;
   }

   boolean expiresAfterAccess() {
      return this.expireAfterAccessNanos != -1L;
   }

   @CanIgnoreReturnValue
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> expireAfter(Expiry<? super K1, ? super V1> expiry) {
      Objects.requireNonNull(expiry);
      requireState(this.expiry == null, "Expiry was already set to %s", this.expiry);
      requireState(this.expireAfterAccessNanos == -1L, "Expiry may not be used with expiresAfterAccess");
      requireState(this.expireAfterWriteNanos == -1L, "Expiry may not be used with expiresAfterWrite");
      Caffeine<K1, V1> self = this;
      self.expiry = expiry;
      return self;
   }

   boolean expiresVariable() {
      return this.expiry != null;
   }

   @Nullable Expiry<K, V> getExpiry(boolean isAsync) {
      return isAsync && this.expiry != null ? new Async.AsyncExpiry<>(this.expiry) : this.expiry;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> refreshAfterWrite(Duration duration) {
      return this.refreshAfterWrite(toNanosSaturated(duration), TimeUnit.NANOSECONDS);
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> refreshAfterWrite(long duration, TimeUnit unit) {
      Objects.requireNonNull(unit);
      requireState(this.refreshAfterWriteNanos == -1L, "refreshAfterWriteNanos was already set to %s ns", this.refreshAfterWriteNanos);
      requireArgument(duration > 0L, "duration must be positive: %s %s", duration, unit);
      this.refreshAfterWriteNanos = unit.toNanos(duration);
      return this;
   }

   boolean refreshAfterWrite() {
      return this.refreshAfterWriteNanos != -1L;
   }

   long getRefreshAfterWriteNanos() {
      return this.refreshAfterWrite() ? this.refreshAfterWriteNanos : 0L;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> ticker(Ticker ticker) {
      requireState(this.ticker == null, "Ticker was already set to %s", this.ticker);
      this.ticker = Objects.requireNonNull(ticker);
      return this;
   }

   Ticker getTicker() {
      boolean useTicker = this.expiresVariable() || this.expiresAfterAccess() || this.expiresAfterWrite() || this.refreshAfterWrite();
      return useTicker ? (this.ticker == null ? Ticker.systemTicker() : this.ticker) : Ticker.disabledTicker();
   }

   @CanIgnoreReturnValue
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> evictionListener(RemovalListener<? super K1, ? super V1> evictionListener) {
      requireState(this.evictionListener == null, "eviction listener was already set to %s", this.evictionListener);
      Caffeine<K1, V1> self = this;
      self.evictionListener = Objects.requireNonNull(evictionListener);
      return self;
   }

   <K1 extends K, V1 extends V> @Nullable RemovalListener<K1, V1> getEvictionListener(boolean async) {
      RemovalListener<K1, V1> castedListener = this.evictionListener;
      return async && castedListener != null ? new Async.AsyncEvictionListener<>(castedListener) : castedListener;
   }

   @CanIgnoreReturnValue
   public <K1 extends K, V1 extends V> Caffeine<K1, V1> removalListener(RemovalListener<? super K1, ? super V1> removalListener) {
      requireState(this.removalListener == null, "removal listener was already set to %s", this.removalListener);
      Caffeine<K1, V1> self = this;
      self.removalListener = Objects.requireNonNull(removalListener);
      return self;
   }

   <K1 extends K, V1 extends V> @Nullable RemovalListener<K1, V1> getRemovalListener(boolean async) {
      RemovalListener<K1, V1> castedListener = this.removalListener;
      return async && castedListener != null ? new Async.AsyncRemovalListener<>(castedListener, this.getExecutor()) : castedListener;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> recordStats() {
      requireState(this.statsCounterSupplier == null, "Statistics recording was already set");
      this.statsCounterSupplier = ENABLED_STATS_COUNTER_SUPPLIER;
      return this;
   }

   @CanIgnoreReturnValue
   public Caffeine<K, V> recordStats(Supplier<? extends StatsCounter> statsCounterSupplier) {
      requireState(this.statsCounterSupplier == null, "Statistics recording was already set");
      this.statsCounterSupplier = () -> StatsCounter.guardedStatsCounter(statsCounterSupplier.get());
      return this;
   }

   boolean isRecordingStats() {
      return this.statsCounterSupplier != null;
   }

   Supplier<StatsCounter> getStatsCounterSupplier() {
      return this.statsCounterSupplier == null ? StatsCounter::disabledStatsCounter : this.statsCounterSupplier;
   }

   boolean isBounded() {
      return this.maximumSize != -1L
         || this.maximumWeight != -1L
         || this.expireAfterAccessNanos != -1L
         || this.expireAfterWriteNanos != -1L
         || this.expiry != null
         || this.keyStrength != null
         || this.valueStrength != null;
   }

   public <K1 extends K, V1 extends V> Cache<K1, V1> build() {
      this.requireWeightWithWeigher();
      this.requireNonLoadingCache();
      Caffeine<K1, V1> self = this;
      return this.isBounded() ? new BoundedLocalCache.BoundedLocalManualCache<>(self) : new UnboundedLocalCache.UnboundedLocalManualCache<>(self);
   }

   public <K1 extends K, V1 extends V> LoadingCache<K1, V1> build(CacheLoader<? super K1, V1> loader) {
      this.requireWeightWithWeigher();
      Caffeine<K1, V1> self = this;
      return !this.isBounded() && !this.refreshAfterWrite()
         ? new UnboundedLocalCache.UnboundedLocalLoadingCache<>(self, loader)
         : new BoundedLocalCache.BoundedLocalLoadingCache<>(self, loader);
   }

   public <K1 extends K, V1 extends V> AsyncCache<K1, V1> buildAsync() {
      requireState(this.valueStrength == null, "Weak or soft values cannot be combined with AsyncCache");
      requireState(this.isStrongKeys() || this.evictionListener == null, "Weak keys cannot be combined with eviction listener and AsyncLoadingCache");
      this.requireWeightWithWeigher();
      this.requireNonLoadingCache();
      Caffeine<K1, V1> self = this;
      return this.isBounded() ? new BoundedLocalCache.BoundedLocalAsyncCache<>(self) : new UnboundedLocalCache.UnboundedLocalAsyncCache<>(self);
   }

   public <K1 extends K, V1 extends V> AsyncLoadingCache<K1, V1> buildAsync(CacheLoader<? super K1, V1> loader) {
      return this.buildAsync((AsyncCacheLoader<? super K1, V1>)loader);
   }

   public <K1 extends K, V1 extends V> AsyncLoadingCache<K1, V1> buildAsync(AsyncCacheLoader<? super K1, V1> loader) {
      requireState(this.valueStrength == null, "Weak or soft values cannot be combined with AsyncLoadingCache");
      requireState(this.isStrongKeys() || this.evictionListener == null, "Weak keys cannot be combined with eviction listener and AsyncLoadingCache");
      this.requireWeightWithWeigher();
      Objects.requireNonNull(loader);
      Caffeine<K1, V1> self = this;
      return !this.isBounded() && !this.refreshAfterWrite()
         ? new UnboundedLocalCache.UnboundedLocalAsyncLoadingCache<>(self, loader)
         : new BoundedLocalCache.BoundedLocalAsyncLoadingCache<>(self, loader);
   }

   void requireNonLoadingCache() {
      requireState(this.refreshAfterWriteNanos == -1L, "refreshAfterWrite requires a LoadingCache");
   }

   void requireWeightWithWeigher() {
      if (this.weigher == null) {
         requireState(this.maximumWeight == -1L, "maximumWeight requires weigher");
      } else if (this.strictParsing) {
         requireState(this.maximumWeight != -1L, "weigher requires maximumWeight");
      } else if (this.maximumWeight == -1L) {
         logger.log(Level.WARNING, "ignoring weigher specified without maximumWeight");
      }
   }

   @Override
   public String toString() {
      StringBuilder s = new StringBuilder(200).append(this.getClass().getSimpleName()).append('{');
      int baseLength = s.length();
      if (this.initialCapacity != -1) {
         s.append("initialCapacity=").append(this.initialCapacity).append(", ");
      }

      if (this.maximumSize != -1L) {
         s.append("maximumSize=").append(this.maximumSize).append(", ");
      }

      if (this.maximumWeight != -1L) {
         s.append("maximumWeight=").append(this.maximumWeight).append(", ");
      }

      if (this.expireAfterWriteNanos != -1L) {
         s.append("expireAfterWrite=").append(this.expireAfterWriteNanos).append("ns, ");
      }

      if (this.expireAfterAccessNanos != -1L) {
         s.append("expireAfterAccess=").append(this.expireAfterAccessNanos).append("ns, ");
      }

      if (this.expiry != null) {
         s.append("expiry, ");
      }

      if (this.refreshAfterWriteNanos != -1L) {
         s.append("refreshAfterWrite=").append(this.refreshAfterWriteNanos).append("ns, ");
      }

      if (this.keyStrength != null) {
         s.append("keyStrength=").append(this.keyStrength.toString().toLowerCase(Locale.US)).append(", ");
      }

      if (this.valueStrength != null) {
         s.append("valueStrength=").append(this.valueStrength.toString().toLowerCase(Locale.US)).append(", ");
      }

      if (this.evictionListener != null) {
         s.append("evictionListener, ");
      }

      if (this.removalListener != null) {
         s.append("removalListener, ");
      }

      if (s.length() > baseLength) {
         s.delete(s.length() - 2, s.length());
      }

      return s.append('}').toString();
   }

   enum Strength {
      WEAK,
      SOFT;
   }
}
