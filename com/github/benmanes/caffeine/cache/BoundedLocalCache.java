package com.github.benmanes.caffeine.cache;

import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;

abstract class BoundedLocalCache<K, V> extends BLCHeader.DrainStatusRef implements LocalCache<K, V> {
   static final Logger logger = System.getLogger(BoundedLocalCache.class.getName());
   static final int NCPU = Runtime.getRuntime().availableProcessors();
   static final int WRITE_BUFFER_MIN = 4;
   static final int WRITE_BUFFER_MAX = 128 * Caffeine.ceilingPowerOfTwo(NCPU);
   static final int WRITE_BUFFER_RETRIES = 100;
   static final long MAXIMUM_CAPACITY = 9223372034707292160L;
   static final double PERCENT_MAIN = 0.99;
   static final double PERCENT_MAIN_PROTECTED = 0.8;
   static final double HILL_CLIMBER_RESTART_THRESHOLD = 0.05;
   static final double HILL_CLIMBER_STEP_PERCENT = 0.0625;
   static final double HILL_CLIMBER_STEP_DECAY_RATE = 0.98;
   static final int ADMIT_HASHDOS_THRESHOLD = 6;
   static final int QUEUE_TRANSFER_THRESHOLD = 1000;
   static final long EXPIRE_WRITE_TOLERANCE = TimeUnit.SECONDS.toNanos(1L);
   static final long MAXIMUM_EXPIRY = 4611686018427387903L;
   static final long WARN_AFTER_LOCK_WAIT_NANOS = TimeUnit.SECONDS.toNanos(30L);
   static final int MAX_PUT_SPIN_WAIT_ATTEMPTS = 1023;
   static final VarHandle REFRESHES = findVarHandle(BoundedLocalCache.class, "refreshes", ConcurrentMap.class);
   final @Nullable RemovalListener<K, V> evictionListener;
   final @Nullable AsyncCacheLoader<K, V> cacheLoader;
   final MpscGrowableArrayQueue<Runnable> writeBuffer;
   final ConcurrentHashMap<Object, Node<K, V>> data;
   final BoundedLocalCache.PerformCleanupTask drainBuffersTask;
   final Consumer<Node<K, V>> accessPolicy;
   final Buffer<Node<K, V>> readBuffer;
   final NodeFactory<K, V> nodeFactory;
   final ReentrantLock evictionLock;
   final Weigher<K, V> weigher;
   final Executor executor;
   final boolean isWeighted;
   final boolean isAsync;
   @Nullable Set<K> keySet;
   @Nullable Collection<V> values;
   @Nullable Set<Entry<K, V>> entrySet;
   volatile @Nullable ConcurrentMap<Object, CompletableFuture<?>> refreshes;

   protected BoundedLocalCache(Caffeine<K, V> builder, @Nullable AsyncCacheLoader<K, V> cacheLoader, boolean isAsync) {
      this.isAsync = isAsync;
      this.cacheLoader = cacheLoader;
      this.executor = builder.getExecutor();
      this.isWeighted = builder.isWeighted();
      this.evictionLock = new ReentrantLock();
      this.weigher = builder.getWeigher(isAsync);
      this.drainBuffersTask = new BoundedLocalCache.PerformCleanupTask(this);
      this.nodeFactory = NodeFactory.newFactory(builder, isAsync);
      this.evictionListener = builder.getEvictionListener(isAsync);
      this.data = new ConcurrentHashMap<>(builder.getInitialCapacity());
      this.readBuffer = !this.evicts() && !this.collectKeys() && !this.collectValues() && !this.expiresAfterAccess()
         ? Buffer.disabled()
         : new BoundedBuffer<>();
      this.accessPolicy = !this.evicts() && !this.expiresAfterAccess() ? e -> {} : this::onAccess;
      this.writeBuffer = new MpscGrowableArrayQueue<>(4, WRITE_BUFFER_MAX);
      if (this.evicts()) {
         this.setMaximumSize(builder.getMaximum());
      }
   }

   void requireIsAlive(Object key, Node<?, ?> node) {
      if (!node.isAlive()) {
         throw new IllegalStateException(this.brokenEqualityMessage(key, node));
      }
   }

   void logIfAlive(Node<?, ?> node) {
      if (node.isAlive()) {
         String message = this.brokenEqualityMessage(node.getKeyReference(), node);
         logger.log(Level.ERROR, message, new IllegalStateException());
      }
   }

   String brokenEqualityMessage(Object key, Node<?, ?> node) {
      return String.format(
         Locale.US,
         "An invalid state was detected, occurring when the key's equals or hashCode was modified while residing in the cache. This violation of the Map contract can lead to non-deterministic behavior (key: %s, key type: %s, node type: %s, cache type: %s).",
         key,
         key.getClass().getName(),
         node.getClass().getSimpleName(),
         this.getClass().getSimpleName()
      );
   }

   @Override
   public boolean isAsync() {
      return this.isAsync;
   }

   final boolean isComputingAsync(@Nullable V value) {
      return this.isAsync && !Async.isReady((CompletableFuture<?>)value);
   }

   @GuardedBy("evictionLock")
   protected AccessOrderDeque<Node<K, V>> accessOrderWindowDeque() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected AccessOrderDeque<Node<K, V>> accessOrderProbationDeque() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected WriteOrderDeque<Node<K, V>> writeOrderDeque() {
      throw new UnsupportedOperationException();
   }

   @Override
   public final Executor executor() {
      return this.executor;
   }

   @Override
   public ConcurrentMap<Object, CompletableFuture<?>> refreshes() {
      ConcurrentMap<Object, CompletableFuture<?>> pending = this.refreshes;
      if (pending == null) {
         pending = new ConcurrentHashMap<>();
         if (!REFRESHES.compareAndSet((BoundedLocalCache)this, (Void)null, (ConcurrentMap)pending)) {
            pending = Objects.requireNonNull(this.refreshes);
         }
      }

      return pending;
   }

   void discardRefresh(Object keyReference) {
      ConcurrentMap<Object, CompletableFuture<?>> pending = this.refreshes;
      if (pending != null && pending.containsKey(keyReference)) {
         pending.remove(keyReference);
      }
   }

   @Override
   public Object referenceKey(K key) {
      return this.nodeFactory.newLookupKey(key);
   }

   @Override
   public boolean isPendingEviction(K key) {
      Node<K, V> node = this.data.get(this.nodeFactory.newLookupKey(key));
      return node != null && (node.getValue() == null || this.hasExpired(node, this.expirationTicker().read()));
   }

   @Override
   public boolean isRecordingStats() {
      return false;
   }

   @Override
   public StatsCounter statsCounter() {
      return StatsCounter.disabledStatsCounter();
   }

   @Override
   public Ticker statsTicker() {
      return Ticker.disabledTicker();
   }

   protected RemovalListener<K, V> removalListener() {
      return null;
   }

   protected boolean hasRemovalListener() {
      return false;
   }

   @Override
   public void notifyRemoval(@Nullable K key, @Nullable V value, RemovalCause cause) {
      if (this.hasRemovalListener()) {
         Runnable task = () -> {
            try {
               this.removalListener().onRemoval(key, value, cause);
            } catch (Throwable t) {
               logger.log(Level.WARNING, "Exception thrown by removal listener", t);
            }
         };

         try {
            this.executor.execute(task);
         } catch (Throwable t) {
            logger.log(Level.ERROR, "Exception thrown when submitting removal listener", t);
            task.run();
         }
      }
   }

   void notifyEviction(@Nullable K key, @Nullable V value, RemovalCause cause) {
      if (this.evictionListener != null) {
         try {
            this.evictionListener.onRemoval(key, value, cause);
         } catch (Throwable t) {
            logger.log(Level.WARNING, "Exception thrown by eviction listener", t);
         }
      }
   }

   protected boolean collectKeys() {
      return false;
   }

   protected boolean collectValues() {
      return false;
   }

   protected ReferenceQueue<K> keyReferenceQueue() {
      return null;
   }

   protected ReferenceQueue<V> valueReferenceQueue() {
      return null;
   }

   protected @Nullable Pacer pacer() {
      return null;
   }

   protected boolean expiresVariable() {
      return false;
   }

   protected boolean expiresAfterAccess() {
      return false;
   }

   protected long expiresAfterAccessNanos() {
      throw new UnsupportedOperationException();
   }

   protected void setExpiresAfterAccessNanos(long expireAfterAccessNanos) {
      throw new UnsupportedOperationException();
   }

   protected boolean expiresAfterWrite() {
      return false;
   }

   protected long expiresAfterWriteNanos() {
      throw new UnsupportedOperationException();
   }

   protected void setExpiresAfterWriteNanos(long expireAfterWriteNanos) {
      throw new UnsupportedOperationException();
   }

   protected boolean refreshAfterWrite() {
      return false;
   }

   protected long refreshAfterWriteNanos() {
      throw new UnsupportedOperationException();
   }

   protected void setRefreshAfterWriteNanos(long refreshAfterWriteNanos) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Expiry<K, V> expiry() {
      return null;
   }

   public Ticker expirationTicker() {
      return Ticker.disabledTicker();
   }

   protected TimerWheel<K, V> timerWheel() {
      throw new UnsupportedOperationException();
   }

   protected boolean evicts() {
      return false;
   }

   protected boolean isWeighted() {
      return this.weigher != Weigher.singletonWeigher();
   }

   protected FrequencySketch<K> frequencySketch() {
      throw new UnsupportedOperationException();
   }

   protected boolean fastpath() {
      return false;
   }

   protected long maximum() {
      throw new UnsupportedOperationException();
   }

   protected long windowMaximum() {
      throw new UnsupportedOperationException();
   }

   protected long mainProtectedMaximum() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setMaximum(long maximum) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setWindowMaximum(long maximum) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setMainProtectedMaximum(long maximum) {
      throw new UnsupportedOperationException();
   }

   protected long weightedSize() {
      throw new UnsupportedOperationException();
   }

   protected long windowWeightedSize() {
      throw new UnsupportedOperationException();
   }

   protected long mainProtectedWeightedSize() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setWeightedSize(long weightedSize) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setWindowWeightedSize(long weightedSize) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setMainProtectedWeightedSize(long weightedSize) {
      throw new UnsupportedOperationException();
   }

   protected int hitsInSample() {
      throw new UnsupportedOperationException();
   }

   protected int missesInSample() {
      throw new UnsupportedOperationException();
   }

   protected int sampleCount() {
      throw new UnsupportedOperationException();
   }

   protected double stepSize() {
      throw new UnsupportedOperationException();
   }

   protected double previousSampleHitRate() {
      throw new UnsupportedOperationException();
   }

   protected long adjustment() {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setHitsInSample(int hitCount) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setMissesInSample(int missCount) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setSampleCount(int sampleCount) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setStepSize(double stepSize) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setPreviousSampleHitRate(double hitRate) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   protected void setAdjustment(long amount) {
      throw new UnsupportedOperationException();
   }

   @GuardedBy("evictionLock")
   void setMaximumSize(long maximum) {
      Caffeine.requireArgument(maximum >= 0L, "maximum must not be negative");
      if (maximum != this.maximum()) {
         long max = Math.min(maximum, 9223372034707292160L);
         long window = max - (long)(0.99 * max);
         long mainProtected = (long)(0.8 * (max - window));
         this.setMaximum(max);
         this.setWindowMaximum(window);
         this.setMainProtectedMaximum(mainProtected);
         this.setHitsInSample(0);
         this.setMissesInSample(0);
         this.setStepSize(-0.0625 * max);
         if (this.frequencySketch() != null && !this.isWeighted() && this.weightedSize() >= max >>> 1) {
            this.frequencySketch().ensureCapacity(max);
         }
      }
   }

   @GuardedBy("evictionLock")
   void evictEntries() {
      if (this.evicts()) {
         Node<K, V> candidate = this.evictFromWindow();
         this.evictFromMain(candidate);
      }
   }

   @GuardedBy("evictionLock")
   @Nullable Node<K, V> evictFromWindow() {
      Node<K, V> first = null;
      Node<K, V> node = this.accessOrderWindowDeque().peekFirst();

      while (this.windowWeightedSize() > this.windowMaximum() && node != null) {
         Node<K, V> next = node.getNextInAccessOrder();
         if (node.getPolicyWeight() != 0) {
            node.makeMainProbation();
            this.accessOrderWindowDeque().remove(node);
            this.accessOrderProbationDeque().offerLast(node);
            if (first == null) {
               first = node;
            }

            this.setWindowWeightedSize(this.windowWeightedSize() - node.getPolicyWeight());
         }

         node = next;
      }

      return first;
   }

   @GuardedBy("evictionLock")
   void evictFromMain(@Var @Nullable Node<K, V> candidate) {
      int victimQueue = 1;
      int candidateQueue = 1;
      Node<K, V> victim = this.accessOrderProbationDeque().peekFirst();

      while (this.weightedSize() > this.maximum()) {
         if (candidate == null && candidateQueue == 1) {
            candidate = this.accessOrderWindowDeque().peekFirst();
            candidateQueue = 0;
         }

         if (candidate == null && victim == null) {
            if (victimQueue == 1) {
               victim = this.accessOrderProtectedDeque().peekFirst();
               victimQueue = 2;
            } else {
               if (victimQueue != 2) {
                  break;
               }

               victim = this.accessOrderWindowDeque().peekFirst();
               victimQueue = 0;
            }
         } else if (victim != null && victim.getPolicyWeight() == 0) {
            victim = victim.getNextInAccessOrder();
         } else if (candidate != null && candidate.getPolicyWeight() == 0) {
            candidate = candidate.getNextInAccessOrder();
         } else if (victim == null) {
            Objects.requireNonNull(candidate);
            Node<K, V> previous = candidate.getNextInAccessOrder();
            Node<K, V> evict = candidate;
            candidate = previous;
            this.evictEntry(evict, RemovalCause.SIZE, 0L);
         } else if (candidate == null) {
            Node<K, V> evict = victim;
            victim = victim.getNextInAccessOrder();
            this.evictEntry(evict, RemovalCause.SIZE, 0L);
         } else if (candidate == victim) {
            victim = victim.getNextInAccessOrder();
            this.evictEntry(candidate, RemovalCause.SIZE, 0L);
            candidate = null;
         } else {
            K victimKey = victim.getKey();
            K candidateKey = candidate.getKey();
            if (victimKey == null) {
               Node<K, V> evict = victim;
               victim = victim.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.COLLECTED, 0L);
            } else if (candidateKey == null) {
               Node<K, V> evict = candidate;
               candidate = candidate.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.COLLECTED, 0L);
            } else if (!victim.isAlive()) {
               Node<K, V> evict = victim;
               victim = victim.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
            } else if (!candidate.isAlive()) {
               Node<K, V> evict = candidate;
               candidate = candidate.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
            } else if (candidate.getPolicyWeight() > this.maximum()) {
               Node<K, V> evict = candidate;
               candidate = candidate.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
            } else if (this.admit(candidateKey, victimKey)) {
               Node<K, V> evict = victim;
               victim = victim.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
               candidate = candidate.getNextInAccessOrder();
            } else {
               Node<K, V> evict = candidate;
               candidate = candidate.getNextInAccessOrder();
               this.evictEntry(evict, RemovalCause.SIZE, 0L);
            }
         }
      }
   }

   @GuardedBy("evictionLock")
   boolean admit(K candidateKey, K victimKey) {
      int victimFreq = this.frequencySketch().frequency(victimKey);
      int candidateFreq = this.frequencySketch().frequency(candidateKey);
      if (candidateFreq > victimFreq) {
         return true;
      } else if (candidateFreq >= 6) {
         int random = ThreadLocalRandom.current().nextInt();
         return (random & 127) == 0;
      } else {
         return false;
      }
   }

   @GuardedBy("evictionLock")
   void expireEntries() {
      long now = this.expirationTicker().read();
      this.expireAfterAccessEntries(now);
      this.expireAfterWriteEntries(now);
      this.expireVariableEntries(now);
      Pacer pacer = this.pacer();
      if (pacer != null) {
         long delay = this.getExpirationDelay(now);
         if (delay == Long.MAX_VALUE) {
            pacer.cancel();
         } else {
            pacer.schedule(this.executor, this.drainBuffersTask, now, delay);
         }
      }
   }

   @GuardedBy("evictionLock")
   void expireAfterAccessEntries(long now) {
      if (this.expiresAfterAccess()) {
         this.expireAfterAccessEntries(now, this.accessOrderWindowDeque());
         if (this.evicts()) {
            this.expireAfterAccessEntries(now, this.accessOrderProbationDeque());
            this.expireAfterAccessEntries(now, this.accessOrderProtectedDeque());
         }
      }
   }

   @GuardedBy("evictionLock")
   void expireAfterAccessEntries(long now, AccessOrderDeque<Node<K, V>> accessOrderDeque) {
      long duration = this.expiresAfterAccessNanos();

      Node<K, V> node;
      do {
         node = accessOrderDeque.peekFirst();
      } while (node != null && now - node.getAccessTime() >= duration && this.evictEntry(node, RemovalCause.EXPIRED, now));
   }

   @GuardedBy("evictionLock")
   void expireAfterWriteEntries(long now) {
      if (this.expiresAfterWrite()) {
         long duration = this.expiresAfterWriteNanos();

         Node<K, V> node;
         do {
            node = this.writeOrderDeque().peekFirst();
         } while (node != null && now - node.getWriteTime() >= duration && this.evictEntry(node, RemovalCause.EXPIRED, now));
      }
   }

   @GuardedBy("evictionLock")
   void expireVariableEntries(long now) {
      if (this.expiresVariable()) {
         this.timerWheel().advance(this, now);
      }
   }

   @GuardedBy("evictionLock")
   long getExpirationDelay(long now) {
      long delay = Long.MAX_VALUE;
      if (this.expiresAfterAccess()) {
         Node<K, V> node = this.accessOrderWindowDeque().peekFirst();
         if (node != null) {
            delay = Math.min(delay, this.expiresAfterAccessNanos() - (now - node.getAccessTime()));
         }

         if (this.evicts()) {
            node = this.accessOrderProbationDeque().peekFirst();
            if (node != null) {
               delay = Math.min(delay, this.expiresAfterAccessNanos() - (now - node.getAccessTime()));
            }

            node = this.accessOrderProtectedDeque().peekFirst();
            if (node != null) {
               delay = Math.min(delay, this.expiresAfterAccessNanos() - (now - node.getAccessTime()));
            }
         }
      }

      if (this.expiresAfterWrite()) {
         Node<K, V> node = this.writeOrderDeque().peekFirst();
         if (node != null) {
            delay = Math.min(delay, this.expiresAfterWriteNanos() - (now - node.getWriteTime()));
         }
      }

      if (this.expiresVariable()) {
         delay = Math.min(delay, this.timerWheel().getExpirationDelay());
      }

      return delay;
   }

   boolean hasExpired(Node<K, V> node, long now) {
      return this.isComputingAsync(node.getValue())
         ? false
         : (this.expiresAfterAccess() && now - node.getAccessTime() >= this.expiresAfterAccessNanos())
            | (this.expiresAfterWrite() && now - node.getWriteTime() >= this.expiresAfterWriteNanos())
            | (this.expiresVariable() && now - node.getVariableTime() >= 0L);
   }

   @GuardedBy("evictionLock")
   boolean evictEntry(Node<K, V> node, RemovalCause cause, long now) {
      K key = node.getKey();
      V[] value = (V[])(new Object[1]);
      boolean[] removed = new boolean[1];
      boolean[] resurrect = new boolean[1];
      RemovalCause[] actualCause = new RemovalCause[1];
      Object keyReference = node.getKeyReference();
      this.data.computeIfPresent(keyReference, (k, n) -> {
         if (n != node) {
            return n;
         }

         synchronized (n) {
            value[0] = n.getValue();
            if (key != null && value[0] != null) {
               if (cause == RemovalCause.COLLECTED) {
                  resurrect[0] = true;
                  return n;
               }

               actualCause[0] = cause;
            } else {
               actualCause[0] = RemovalCause.COLLECTED;
            }

            if (actualCause[0] == RemovalCause.EXPIRED) {
               boolean expired = false;
               if (this.expiresAfterAccess()) {
                  expired |= now - n.getAccessTime() >= this.expiresAfterAccessNanos();
               }

               if (this.expiresAfterWrite()) {
                  expired |= now - n.getWriteTime() >= this.expiresAfterWriteNanos();
               }

               if (this.expiresVariable()) {
                  expired |= now - node.getVariableTime() >= 0L;
               }

               if (!expired) {
                  resurrect[0] = true;
                  return n;
               }
            } else if (actualCause[0] == RemovalCause.SIZE) {
               int weight = node.getWeight();
               if (weight == 0) {
                  resurrect[0] = true;
                  return n;
               }
            }

            this.notifyEviction(key, value[0], actualCause[0]);
            this.discardRefresh(keyReference);
            removed[0] = true;
            node.retire();
            return null;
         }
      });
      if (resurrect[0]) {
         return false;
      }

      if (!node.inWindow() || !this.evicts() && !this.expiresAfterAccess()) {
         if (this.evicts()) {
            if (node.inMainProbation()) {
               this.accessOrderProbationDeque().remove(node);
            } else {
               this.accessOrderProtectedDeque().remove(node);
            }
         }
      } else {
         this.accessOrderWindowDeque().remove(node);
      }

      if (this.expiresAfterWrite()) {
         this.writeOrderDeque().remove(node);
      } else if (this.expiresVariable()) {
         this.timerWheel().deschedule(node);
      }

      synchronized (node) {
         this.logIfAlive(node);
         this.makeDead(node);
      }

      if (removed[0]) {
         this.statsCounter().recordEviction(node.getWeight(), actualCause[0]);
         this.notifyRemoval(key, value[0], actualCause[0]);
      }

      return true;
   }

   @GuardedBy("evictionLock")
   void climb() {
      if (this.evicts()) {
         this.determineAdjustment();
         this.demoteFromMainProtected();
         long amount = this.adjustment();
         if (amount != 0L) {
            if (amount > 0L) {
               this.increaseWindow();
            } else {
               this.decreaseWindow();
            }
         }
      }
   }

   @GuardedBy("evictionLock")
   void determineAdjustment() {
      if (this.frequencySketch().isNotInitialized()) {
         this.setPreviousSampleHitRate(0.0);
         this.setMissesInSample(0);
         this.setHitsInSample(0);
      } else {
         int requestCount = this.hitsInSample() + this.missesInSample();
         if (requestCount >= this.frequencySketch().sampleSize) {
            double hitRate = (double)this.hitsInSample() / requestCount;
            double hitRateChange = hitRate - this.previousSampleHitRate();
            double amount = hitRateChange >= 0.0 ? this.stepSize() : -this.stepSize();
            double nextStepSize = Math.abs(hitRateChange) >= 0.05 ? 0.0625 * this.maximum() * (amount >= 0.0 ? 1 : -1) : 0.98 * amount;
            this.setPreviousSampleHitRate(hitRate);
            this.setAdjustment((long)amount);
            this.setStepSize(nextStepSize);
            this.setMissesInSample(0);
            this.setHitsInSample(0);
         }
      }
   }

   @GuardedBy("evictionLock")
   void increaseWindow() {
      if (this.mainProtectedMaximum() != 0L) {
         long quota = Math.min(this.adjustment(), this.mainProtectedMaximum());
         this.setMainProtectedMaximum(this.mainProtectedMaximum() - quota);
         this.setWindowMaximum(this.windowMaximum() + quota);
         this.demoteFromMainProtected();

         for (int i = 0; i < 1000; i++) {
            Node<K, V> candidate = this.accessOrderProbationDeque().peekFirst();
            boolean probation = true;
            if (candidate == null || quota < candidate.getPolicyWeight()) {
               candidate = this.accessOrderProtectedDeque().peekFirst();
               probation = false;
            }

            if (candidate == null) {
               break;
            }

            int weight = candidate.getPolicyWeight();
            if (quota < weight) {
               break;
            }

            quota -= weight;
            if (probation) {
               this.accessOrderProbationDeque().remove(candidate);
            } else {
               this.setMainProtectedWeightedSize(this.mainProtectedWeightedSize() - weight);
               this.accessOrderProtectedDeque().remove(candidate);
            }

            this.setWindowWeightedSize(this.windowWeightedSize() + weight);
            this.accessOrderWindowDeque().offerLast(candidate);
            candidate.makeWindow();
         }

         this.setMainProtectedMaximum(this.mainProtectedMaximum() + quota);
         this.setWindowMaximum(this.windowMaximum() - quota);
         this.setAdjustment(quota);
      }
   }

   @GuardedBy("evictionLock")
   void decreaseWindow() {
      if (this.windowMaximum() > 1L) {
         long quota = Math.min(-this.adjustment(), Math.max(0L, this.windowMaximum() - 1L));
         this.setMainProtectedMaximum(this.mainProtectedMaximum() + quota);
         this.setWindowMaximum(this.windowMaximum() - quota);

         for (int i = 0; i < 1000; i++) {
            Node<K, V> candidate = this.accessOrderWindowDeque().peekFirst();
            if (candidate == null) {
               break;
            }

            int weight = candidate.getPolicyWeight();
            if (quota < weight) {
               break;
            }

            quota -= weight;
            this.setWindowWeightedSize(this.windowWeightedSize() - weight);
            this.accessOrderWindowDeque().remove(candidate);
            this.accessOrderProbationDeque().offerLast(candidate);
            candidate.makeMainProbation();
         }

         this.setMainProtectedMaximum(this.mainProtectedMaximum() - quota);
         this.setWindowMaximum(this.windowMaximum() + quota);
         this.setAdjustment(-quota);
      }
   }

   @GuardedBy("evictionLock")
   void demoteFromMainProtected() {
      long mainProtectedMaximum = this.mainProtectedMaximum();
      long mainProtectedWeightedSize = this.mainProtectedWeightedSize();
      if (mainProtectedWeightedSize > mainProtectedMaximum) {
         for (int i = 0; i < 1000 && mainProtectedWeightedSize > mainProtectedMaximum; i++) {
            Node<K, V> demoted = this.accessOrderProtectedDeque().poll();
            if (demoted == null) {
               break;
            }

            demoted.makeMainProbation();
            this.accessOrderProbationDeque().offerLast(demoted);
            mainProtectedWeightedSize -= demoted.getPolicyWeight();
         }

         this.setMainProtectedWeightedSize(mainProtectedWeightedSize);
      }
   }

   @Nullable V afterRead(Node<K, V> node, long now, boolean recordHit) {
      if (recordHit) {
         this.statsCounter().recordHits(1);
      }

      boolean delayable = this.skipReadBuffer() || this.readBuffer.offer(node) != 1;
      if (this.shouldDrainBuffers(delayable)) {
         this.scheduleDrainBuffers();
      }

      return this.refreshIfNeeded(node, now);
   }

   boolean skipReadBuffer() {
      return this.fastpath() && this.frequencySketch().isNotInitialized();
   }

   @Nullable V refreshIfNeeded(Node<K, V> node, long now) {
      if (!this.refreshAfterWrite()) {
         return null;
      }

      long writeTime = node.getWriteTime();
      long refreshWriteTime = writeTime | 1L;
      Object keyReference = node.getKeyReference();
      K key;
      V oldValue;
      ConcurrentMap<Object, CompletableFuture<?>> refreshes;
      if (now - writeTime > this.refreshAfterWriteNanos()
         && keyReference != null
         && (key = node.getKey()) != null
         && (oldValue = node.getValue()) != null
         && !this.isComputingAsync(oldValue)
         && (writeTime & 1L) == 0L
         && !(refreshes = this.refreshes()).containsKey(keyReference)
         && node.isAlive()
         && node.casWriteTime(writeTime, refreshWriteTime)) {
         long[] startTime = new long[1];
         CompletableFuture<? extends V>[] refreshFuture = new CompletableFuture[1];

         try {
            refreshes.computeIfAbsent(keyReference, k -> {
               try {
                  startTime[0] = this.statsTicker().read();
                  if (this.isAsync) {
                     CompletableFuture<V> future = (CompletableFuture<V>)oldValue;
                     if (!Async.isReady(future)) {
                        return null;
                     }

                     Objects.requireNonNull(this.cacheLoader);
                     CompletableFuture<? extends V> refresh = this.cacheLoader.asyncReload(key, future.join(), this.executor);
                     refreshFuture[0] = Objects.requireNonNull(refresh, "Null future");
                  } else {
                     Objects.requireNonNull(this.cacheLoader);
                     CompletableFuture<? extends V> refresh = this.cacheLoader.asyncReload(key, oldValue, this.executor);
                     refreshFuture[0] = Objects.requireNonNull(refresh, "Null future");
                  }

                  return refreshFuture[0];
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  logger.log(Level.WARNING, "Exception thrown when submitting refresh task", e);
                  return null;
               } catch (Throwable e) {
                  logger.log(Level.WARNING, "Exception thrown when submitting refresh task", e);
                  return null;
               }
            });
         } finally {
            node.casWriteTime(refreshWriteTime, writeTime);
         }

         if (refreshFuture[0] == null) {
            return null;
         }

         CompletableFuture<V> refreshed = refreshFuture[0].handle((newValue, error) -> {
            long loadTime = this.statsTicker().read() - startTime[0];
            if (error != null) {
               if (!(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
                  logger.log(Level.WARNING, "Exception thrown during refresh", error);
               }

               refreshes.remove(keyReference, refreshFuture[0]);
               this.statsCounter().recordLoadFailure(loadTime);
               return null;
            } else {
               V value = (V)(this.isAsync && newValue != null ? refreshFuture[0] : newValue);
               RemovalCause[] cause = new RemovalCause[1];
               V result = this.compute(key, (k, currentValue) -> {
                  if (currentValue == null) {
                     if (value != null) {
                        cause[0] = RemovalCause.EXPLICIT;
                     }

                     return null;
                  } else {
                     if (currentValue == value) {
                        return (V)currentValue;
                     }

                     if (this.isAsync && newValue == Async.getIfReady((CompletableFuture<V>)currentValue)) {
                        return (V)currentValue;
                     }

                     if (currentValue == oldValue && node.getWriteTime() == writeTime) {
                        return value;
                     }

                     cause[0] = RemovalCause.REPLACED;
                     return (V)currentValue;
                  }
               }, this.expiry(), false, true);
               if (cause[0] != null) {
                  this.notifyRemoval(key, value, cause[0]);
               }

               if (newValue == null) {
                  this.statsCounter().recordLoadFailure(loadTime);
               } else {
                  this.statsCounter().recordLoadSuccess(loadTime);
               }

               refreshes.remove(keyReference, refreshFuture[0]);
               return result;
            }
         });
         return Async.getIfReady(refreshed);
      } else {
         return null;
      }
   }

   long expireAfterCreate(@Nullable K key, @Nullable V value, Expiry<? super K, ? super V> expiry, long now) {
      if (this.expiresVariable() && key != null && value != null) {
         long duration = Math.max(0L, expiry.expireAfterCreate(key, value, now));
         return this.isAsync ? now + duration : now + Math.min(duration, 4611686018427387903L);
      } else {
         return 0L;
      }
   }

   long expireAfterUpdate(Node<K, V> node, @Nullable K key, @Nullable V value, Expiry<? super K, ? super V> expiry, long now) {
      if (this.expiresVariable() && key != null && value != null) {
         long currentDuration = Math.max(1L, node.getVariableTime() - now);
         long duration = Math.max(0L, expiry.expireAfterUpdate(key, value, now, currentDuration));
         return this.isAsync ? now + duration : now + Math.min(duration, 4611686018427387903L);
      } else {
         return 0L;
      }
   }

   long expireAfterRead(Node<K, V> node, @Nullable K key, @Nullable V value, Expiry<K, V> expiry, long now) {
      if (this.expiresVariable() && key != null && value != null) {
         long currentDuration = Math.max(0L, node.getVariableTime() - now);
         long duration = Math.max(0L, expiry.expireAfterRead(key, value, now, currentDuration));
         return this.isAsync ? now + duration : now + Math.min(duration, 4611686018427387903L);
      } else {
         return 0L;
      }
   }

   void tryExpireAfterRead(Node<K, V> node, @Nullable K key, @Nullable V value, Expiry<K, V> expiry, long now) {
      if (this.expiresVariable() && key != null && value != null) {
         long variableTime = node.getVariableTime();
         long currentDuration = Math.max(1L, variableTime - now);
         if (!this.isAsync || currentDuration <= 4611686018427387903L) {
            long duration = Math.max(0L, expiry.expireAfterRead(key, value, now, currentDuration));
            if (duration != currentDuration) {
               long expirationTime = this.isAsync ? now + duration : now + Math.min(duration, 4611686018427387903L);
               node.casVariableTime(variableTime, expirationTime);
            }
         }
      }
   }

   void setVariableTime(Node<K, V> node, long expirationTime) {
      if (this.expiresVariable()) {
         node.setVariableTime(expirationTime);
      }
   }

   void setWriteTime(Node<K, V> node, long now) {
      if (this.expiresAfterWrite() || this.refreshAfterWrite()) {
         node.setWriteTime(now & -2L);
      }
   }

   void setAccessTime(Node<K, V> node, long now) {
      if (this.expiresAfterAccess()) {
         node.setAccessTime(now);
      }
   }

   boolean exceedsWriteTimeTolerance(Node<K, V> node, long varTime, long now) {
      long variableTime = node.getVariableTime();
      long tolerance = EXPIRE_WRITE_TOLERANCE;
      long writeTime = node.getWriteTime();
      return this.expiresAfterWrite() && (this.expiresAfterWriteNanos() <= tolerance || Math.abs(now - writeTime) > tolerance)
         || this.refreshAfterWrite() && (this.refreshAfterWriteNanos() <= tolerance || Math.abs(now - writeTime) > tolerance)
         || this.expiresVariable() && Math.abs(varTime - variableTime) > tolerance;
   }

   void afterWrite(Runnable task) {
      for (int i = 0; i < 100; i++) {
         if (this.writeBuffer.offer(task)) {
            this.scheduleAfterWrite();
            return;
         }

         this.scheduleDrainBuffers();
         Thread.onSpinWait();
      }

      this.lock();

      try {
         this.maintenance(task);
      } catch (RuntimeException e) {
         logger.log(Level.ERROR, "Exception thrown when performing the maintenance task", e);
      } finally {
         this.evictionLock.unlock();
      }

      this.rescheduleCleanUpIfIncomplete();
   }

   void lock() {
      long remainingNanos = WARN_AFTER_LOCK_WAIT_NANOS;
      long end = System.nanoTime() + remainingNanos;
      boolean interrupted = false;

      while (true) {
         try {
            if (!this.evictionLock.tryLock(remainingNanos, TimeUnit.NANOSECONDS)) {
               logger.log(
                  Level.WARNING,
                  "The cache is experiencing excessive wait times for acquiring the eviction lock. This may indicate that a long-running computation has halted eviction when trying to remove the victim entry. Consider using AsyncCache to decouple the computation from the map operation.",
                  new TimeoutException()
               );
               this.evictionLock.lock();
               return;
            }
         } catch (InterruptedException e) {
            remainingNanos = end - System.nanoTime();
            interrupted = true;
            continue;
         } finally {
            if (interrupted) {
               Thread.currentThread().interrupt();
            }
         }

         return;
      }
   }

   void scheduleAfterWrite() {
      int drainStatus = this.drainStatusOpaque();

      while (true) {
         switch (drainStatus) {
            case 0:
               this.casDrainStatus(0, 1);
               this.scheduleDrainBuffers();
               return;
            case 1:
               this.scheduleDrainBuffers();
               return;
            case 2:
               if (this.casDrainStatus(2, 3)) {
                  return;
               }

               drainStatus = this.drainStatusAcquire();
               break;
            case 3:
               return;
            default:
               throw new IllegalStateException("Invalid drain status: " + drainStatus);
         }
      }
   }

   void scheduleDrainBuffers() {
      if (this.drainStatusOpaque() < 2) {
         if (this.evictionLock.tryLock()) {
            try {
               int drainStatus = this.drainStatusOpaque();
               if (drainStatus < 2) {
                  this.setDrainStatusRelease(2);
                  this.executor.execute(this.drainBuffersTask);
                  return;
               }
            } catch (Throwable t) {
               logger.log(Level.WARNING, "Exception thrown when submitting maintenance task", t);
               this.maintenance(null);
               return;
            } finally {
               this.evictionLock.unlock();
            }
         }
      }
   }

   @Override
   public void cleanUp() {
      try {
         this.performCleanUp(null);
      } catch (RuntimeException e) {
         logger.log(Level.ERROR, "Exception thrown when performing the maintenance task", e);
      }
   }

   void performCleanUp(@Nullable Runnable task) {
      this.evictionLock.lock();

      try {
         this.maintenance(task);
      } finally {
         this.evictionLock.unlock();
      }

      this.rescheduleCleanUpIfIncomplete();
   }

   void rescheduleCleanUpIfIncomplete() {
      if (this.drainStatusOpaque() == 1) {
         if (this.executor == ForkJoinPool.commonPool()) {
            this.scheduleDrainBuffers();
         } else {
            Pacer pacer = this.pacer();
            if (pacer != null && !pacer.isScheduled() && this.evictionLock.tryLock()) {
               try {
                  if (this.drainStatusOpaque() == 1 && !pacer.isScheduled()) {
                     pacer.schedule(this.executor, this.drainBuffersTask, this.expirationTicker().read(), Pacer.TOLERANCE);
                  }
               } finally {
                  this.evictionLock.unlock();
               }
            }
         }
      }
   }

   @GuardedBy("evictionLock")
   void maintenance(@Nullable Runnable task) {
      this.setDrainStatusRelease(2);

      try {
         this.drainReadBuffer();
         this.drainWriteBuffer();
         if (task != null) {
            task.run();
         }

         this.drainKeyReferences();
         this.drainValueReferences();
         this.expireEntries();
         this.evictEntries();
         this.climb();
      } finally {
         if (this.drainStatusOpaque() != 2 || !this.casDrainStatus(2, 0)) {
            this.setDrainStatusOpaque(1);
         }
      }
   }

   @GuardedBy("evictionLock")
   void drainKeyReferences() {
      if (this.collectKeys()) {
         Reference<? extends K> keyRef;
         while ((keyRef = this.keyReferenceQueue().poll()) != null) {
            Node<K, V> node = this.data.get(keyRef);
            if (node != null) {
               this.evictEntry(node, RemovalCause.COLLECTED, 0L);
            }
         }
      }
   }

   @GuardedBy("evictionLock")
   void drainValueReferences() {
      if (this.collectValues()) {
         Reference<? extends V> valueRef;
         while ((valueRef = this.valueReferenceQueue().poll()) != null) {
            References.InternalReference<V> ref = (References.InternalReference<V>)valueRef;
            Node<K, V> node = this.data.get(ref.getKeyReference());
            if (node != null && valueRef == node.getValueReference()) {
               this.evictEntry(node, RemovalCause.COLLECTED, 0L);
            }
         }
      }
   }

   @GuardedBy("evictionLock")
   void drainReadBuffer() {
      if (!this.skipReadBuffer()) {
         this.readBuffer.drainTo(this.accessPolicy);
      }
   }

   @GuardedBy("evictionLock")
   void onAccess(Node<K, V> node) {
      if (this.evicts()) {
         K key = node.getKey();
         if (key == null) {
            return;
         }

         this.frequencySketch().increment(key);
         if (node.inWindow()) {
            reorder(this.accessOrderWindowDeque(), node);
         } else if (node.inMainProbation()) {
            this.reorderProbation(node);
         } else {
            reorder(this.accessOrderProtectedDeque(), node);
         }

         this.setHitsInSample(this.hitsInSample() + 1);
      } else if (this.expiresAfterAccess()) {
         reorder(this.accessOrderWindowDeque(), node);
      }

      if (this.expiresVariable()) {
         this.timerWheel().reschedule(node);
      }
   }

   @GuardedBy("evictionLock")
   void reorderProbation(Node<K, V> node) {
      if (this.accessOrderProbationDeque().contains(node)) {
         if (node.getPolicyWeight() > this.mainProtectedMaximum()) {
            reorder(this.accessOrderProbationDeque(), node);
         } else {
            this.setMainProtectedWeightedSize(this.mainProtectedWeightedSize() + node.getPolicyWeight());
            this.accessOrderProbationDeque().remove(node);
            this.accessOrderProtectedDeque().offerLast(node);
            node.makeMainProtected();
         }
      }
   }

   static <K, V> void reorder(LinkedDeque<Node<K, V>> deque, Node<K, V> node) {
      if (deque.contains(node)) {
         deque.moveToBack(node);
      }
   }

   @GuardedBy("evictionLock")
   void drainWriteBuffer() {
      for (int i = 0; i <= WRITE_BUFFER_MAX; i++) {
         Runnable task = this.writeBuffer.poll();
         if (task == null) {
            return;
         }

         task.run();
      }

      this.setDrainStatusOpaque(3);
   }

   @GuardedBy("evictionLock")
   void makeDead(Node<K, V> node) {
      synchronized (node) {
         if (!node.isDead()) {
            if (this.evicts()) {
               if (node.inWindow()) {
                  this.setWindowWeightedSize(this.windowWeightedSize() - node.getWeight());
               } else if (node.inMainProtected()) {
                  this.setMainProtectedWeightedSize(this.mainProtectedWeightedSize() - node.getWeight());
               }

               this.setWeightedSize(this.weightedSize() - node.getWeight());
            }

            node.die();
         }
      }
   }

   @Override
   public boolean isEmpty() {
      return this.data.isEmpty();
   }

   @Override
   public int size() {
      return this.data.size();
   }

   @Override
   public long estimatedSize() {
      return this.data.mappingCount();
   }

   @Override
   public void clear() {
      this.evictionLock.lock();

      Deque<Node<K, V>> entries;
      try {
         this.readBuffer.drainTo(e -> {});

         Runnable task;
         while ((task = this.writeBuffer.poll()) != null) {
            task.run();
         }

         Pacer pacer = this.pacer();
         if (pacer != null) {
            pacer.cancel();
         }

         long now = this.expirationTicker().read();
         int threshold = WRITE_BUFFER_MAX / 2;
         entries = new ArrayDeque<>(this.data.values());

         while (!entries.isEmpty() && this.writeBuffer.size() < threshold) {
            this.removeNode(entries.poll(), now);
         }
      } finally {
         this.evictionLock.unlock();
      }

      boolean var10 = false;

      for (Node<K, V> node : entries) {
         K key = node.getKey();
         if (key == null) {
            var10 = true;
         } else {
            this.remove(key);
         }
      }

      if (this.collectKeys() && var10) {
         this.cleanUp();
      }
   }

   @GuardedBy("evictionLock")
   void removeNode(Node<K, V> node, long now) {
      K key = node.getKey();
      RemovalCause[] cause = new RemovalCause[1];
      Object keyReference = node.getKeyReference();
      V[] value = (V[])(new Object[1]);
      this.data.computeIfPresent(keyReference, (k, n) -> {
         if (n != node) {
            return n;
         }

         synchronized (n) {
            value[0] = n.getValue();
            if (key == null || value[0] == null) {
               cause[0] = RemovalCause.COLLECTED;
            } else if (this.hasExpired((Node<K, V>)n, now)) {
               cause[0] = RemovalCause.EXPIRED;
            } else {
               cause[0] = RemovalCause.EXPLICIT;
            }

            if (cause[0].wasEvicted()) {
               this.notifyEviction(key, value[0], cause[0]);
            }

            this.discardRefresh(node.getKeyReference());
            node.retire();
            return null;
         }
      });
      if (!node.inWindow() || !this.evicts() && !this.expiresAfterAccess()) {
         if (this.evicts()) {
            if (node.inMainProbation()) {
               this.accessOrderProbationDeque().remove(node);
            } else {
               this.accessOrderProtectedDeque().remove(node);
            }
         }
      } else {
         this.accessOrderWindowDeque().remove(node);
      }

      if (this.expiresAfterWrite()) {
         this.writeOrderDeque().remove(node);
      } else if (this.expiresVariable()) {
         this.timerWheel().deschedule(node);
      }

      synchronized (node) {
         this.logIfAlive(node);
         this.makeDead(node);
      }

      if (cause[0] != null) {
         this.notifyRemoval(key, value[0], cause[0]);
      }
   }

   @Override
   public boolean containsKey(Object key) {
      Node<K, V> node = this.data.get(this.nodeFactory.newLookupKey(key));
      return node != null && node.getValue() != null && !this.hasExpired(node, this.expirationTicker().read());
   }

   @Override
   public boolean containsValue(Object value) {
      Objects.requireNonNull(value);
      long now = this.expirationTicker().read();

      for (Node<K, V> node : this.data.values()) {
         if (node.containsValue(value) && !this.hasExpired(node, now) && node.getKey() != null) {
            return true;
         }
      }

      return false;
   }

   @Override
   public @Nullable V get(Object key) {
      return this.getIfPresent(key, false);
   }

   @Override
   public @Nullable V getIfPresent(Object key, boolean recordStats) {
      Node<K, V> node = this.data.get(this.nodeFactory.newLookupKey(key));
      if (node == null) {
         if (recordStats) {
            this.statsCounter().recordMisses(1);
         }

         if (this.drainStatusOpaque() == 1) {
            this.scheduleDrainBuffers();
         }

         return null;
      } else {
         V value = node.getValue();
         long now = this.expirationTicker().read();
         if (this.hasExpired(node, now) || this.collectValues() && value == null) {
            if (recordStats) {
               this.statsCounter().recordMisses(1);
            }

            this.scheduleDrainBuffers();
            return null;
         } else {
            if (!this.isComputingAsync(value)) {
               K castedKey = (K)key;
               this.setAccessTime(node, now);
               this.tryExpireAfterRead(node, castedKey, value, this.expiry(), now);
            }

            V refreshed = this.afterRead(node, now, recordStats);
            return refreshed == null ? value : refreshed;
         }
      }
   }

   @Override
   public @Nullable V getIfPresentQuietly(Object key) {
      Node<K, V> node = this.data.get(this.nodeFactory.newLookupKey(key));
      V value;
      return node != null && (value = node.getValue()) != null && !this.hasExpired(node, this.expirationTicker().read()) ? value : null;
   }

   public @Nullable K getKey(K key) {
      Node<K, V> node = this.data.get(this.nodeFactory.newLookupKey(key));
      if (node == null) {
         if (this.drainStatusOpaque() == 1) {
            this.scheduleDrainBuffers();
         }

         return null;
      } else {
         this.afterRead(node, 0L, false);
         return node.getKey();
      }
   }

   @Override
   public Map<K, V> getAllPresent(Iterable<? extends K> keys) {
      LinkedHashMap<K, V> result = new LinkedHashMap<>(Caffeine.calculateHashMapCapacity(keys));

      for (K key : keys) {
         result.put(key, null);
      }

      int uniqueKeys = result.size();
      long now = this.expirationTicker().read();
      Iterator<Entry<K, V>> iter = result.entrySet().iterator();

      while (iter.hasNext()) {
         Entry<K, V> entry = iter.next();
         Node<K, V> node = this.data.get(this.nodeFactory.newLookupKey(entry.getKey()));
         V value;
         if (node != null && (value = node.getValue()) != null && !this.hasExpired(node, now)) {
            if (!this.isComputingAsync(value)) {
               this.tryExpireAfterRead(node, entry.getKey(), value, this.expiry(), now);
               this.setAccessTime(node, now);
            }

            V refreshed = this.afterRead(node, now, false);
            entry.setValue(refreshed == null ? value : refreshed);
         } else {
            iter.remove();
         }
      }

      this.statsCounter().recordHits(result.size());
      this.statsCounter().recordMisses(uniqueKeys - result.size());
      return Collections.unmodifiableMap(result);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> map) {
      map.forEach(this::put);
   }

   @Override
   public @Nullable V put(K key, V value) {
      return this.put(key, value, this.expiry(), false);
   }

   @Override
   public @Nullable V putIfAbsent(K key, V value) {
      return this.put(key, value, this.expiry(), true);
   }

   @Nullable V put(K key, V value, Expiry<K, V> expiry, boolean onlyIfAbsent) {
      Objects.requireNonNull(key);
      Node<K, V> node = null;
      long now = this.expirationTicker().read();
      int newWeight = this.weigher.weigh(key, value);
      Object lookupKey = this.nodeFactory.newLookupKey(key);
      int attempts = 1;

      Node<K, V> prior;
      int oldWeight;
      boolean expired;
      boolean mayUpdate;
      boolean exceedsTolerance;
      V oldValue;
      while (true) {
         prior = this.data.get(lookupKey);
         if (prior == null) {
            if (node == null) {
               node = this.nodeFactory.newNode(key, this.keyReferenceQueue(), value, this.valueReferenceQueue(), newWeight, now);
               oldValue = (V)(this.isComputingAsync(value) ? now + 6917529027641081854L : now);
               this.setVariableTime(node, this.expireAfterCreate(key, value, expiry, now));
               this.setAccessTime(node, (long)oldValue);
               this.setWriteTime(node, (long)oldValue);
            }

            prior = this.data.putIfAbsent(node.getKeyReference(), node);
            if (prior == null) {
               this.afterWrite(new BoundedLocalCache.AddTask(node, newWeight));
               return null;
            }

            if (onlyIfAbsent) {
               oldValue = prior.getValue();
               if (oldValue != null && !this.hasExpired(prior, now)) {
                  if (!this.isComputingAsync(oldValue)) {
                     this.tryExpireAfterRead(prior, key, oldValue, this.expiry(), now);
                     this.setAccessTime(prior, now);
                  }

                  this.afterRead(prior, now, false);
                  return oldValue;
               }
            }
         } else if (onlyIfAbsent) {
            oldValue = prior.getValue();
            if (oldValue != null && !this.hasExpired(prior, now)) {
               if (!this.isComputingAsync(oldValue)) {
                  this.tryExpireAfterRead(prior, key, oldValue, this.expiry(), now);
                  this.setAccessTime(prior, now);
               }

               this.afterRead(prior, now, false);
               return oldValue;
            }
         }

         if (!prior.isAlive()) {
            if ((attempts & 1023) != 0) {
               Thread.onSpinWait();
            } else {
               this.data.computeIfPresent(lookupKey, (k, n) -> {
                  this.requireIsAlive(key, (Node<?, ?>)n);
                  return n;
               });
            }
         } else {
            expired = false;
            mayUpdate = true;
            exceedsTolerance = false;
            synchronized (prior) {
               if (prior.isAlive()) {
                  oldValue = prior.getValue();
                  oldWeight = prior.getWeight();
                  long varTime;
                  if (oldValue == null) {
                     varTime = this.expireAfterCreate(key, value, expiry, now);
                     this.notifyEviction(key, null, RemovalCause.COLLECTED);
                  } else if (this.hasExpired(prior, now)) {
                     expired = true;
                     varTime = this.expireAfterCreate(key, value, expiry, now);
                     this.notifyEviction(key, oldValue, RemovalCause.EXPIRED);
                  } else if (onlyIfAbsent) {
                     mayUpdate = false;
                     varTime = this.expireAfterRead(prior, key, value, expiry, now);
                  } else {
                     varTime = this.expireAfterUpdate(prior, key, value, expiry, now);
                  }

                  long expirationTime = this.isComputingAsync(value) ? now + 6917529027641081854L : now;
                  if (mayUpdate) {
                     exceedsTolerance = this.exceedsWriteTimeTolerance(prior, varTime, now);
                     if (expired || exceedsTolerance) {
                        this.setWriteTime(prior, this.isComputingAsync(value) ? now + 6917529027641081854L : now);
                     }

                     prior.setValue(value, this.valueReferenceQueue());
                     prior.setWeight(newWeight);
                     this.discardRefresh(prior.getKeyReference());
                  }

                  this.setVariableTime(prior, varTime);
                  this.setAccessTime(prior, expirationTime);
                  break;
               }
            }
         }

         attempts++;
      }

      if (expired) {
         this.notifyRemoval(key, oldValue, RemovalCause.EXPIRED);
      } else if (oldValue == null) {
         this.notifyRemoval(key, null, RemovalCause.COLLECTED);
      } else if (mayUpdate) {
         this.notifyOnReplace(key, oldValue, value);
      }

      int weightedDifference = mayUpdate ? newWeight - oldWeight : 0;
      if (oldValue == null || weightedDifference != 0 || expired) {
         this.afterWrite(new BoundedLocalCache.UpdateTask(prior, weightedDifference));
      } else if (!onlyIfAbsent && exceedsTolerance) {
         this.afterWrite(new BoundedLocalCache.UpdateTask(prior, weightedDifference));
      } else {
         this.afterRead(prior, now, false);
      }

      return expired ? null : oldValue;
   }

   @Override
   public V remove(Object key) {
      K castKey = (K)key;
      Node<K, V>[] node = new Node[1];
      V[] oldValue = (V[])(new Object[1]);
      RemovalCause[] cause = new RemovalCause[1];
      Object lookupKey = this.nodeFactory.newLookupKey(key);
      this.data.computeIfPresent(lookupKey, (k, n) -> {
         synchronized (n) {
            this.requireIsAlive(key, (Node<?, ?>)n);
            oldValue[0] = n.getValue();
            if (oldValue[0] == null) {
               cause[0] = RemovalCause.COLLECTED;
            } else if (this.hasExpired((Node<K, V>)n, this.expirationTicker().read())) {
               cause[0] = RemovalCause.EXPIRED;
            } else {
               cause[0] = RemovalCause.EXPLICIT;
            }

            if (cause[0].wasEvicted()) {
               this.notifyEviction(castKey, oldValue[0], cause[0]);
            }

            this.discardRefresh(lookupKey);
            node[0] = (Node<K, V>)n;
            n.retire();
            return null;
         }
      });
      if (cause[0] != null) {
         this.afterWrite(new BoundedLocalCache.RemovalTask(node[0]));
         this.notifyRemoval(castKey, oldValue[0], cause[0]);
      }

      return cause[0] == RemovalCause.EXPLICIT ? oldValue[0] : null;
   }

   @Override
   public boolean remove(Object key, Object value) {
      Objects.requireNonNull(key);
      if (value == null) {
         return false;
      }

      Node<K, V>[] removed = new Node[1];
      K[] oldKey = (K[])(new Object[1]);
      V[] oldValue = (V[])(new Object[1]);
      RemovalCause[] cause = new RemovalCause[1];
      Object lookupKey = this.nodeFactory.newLookupKey(key);
      this.data.computeIfPresent(lookupKey, (kR, node) -> {
         synchronized (node) {
            this.requireIsAlive(key, (Node<?, ?>)node);
            oldKey[0] = node.getKey();
            oldValue[0] = node.getValue();
            if (oldKey[0] == null || oldValue[0] == null) {
               cause[0] = RemovalCause.COLLECTED;
            } else if (this.hasExpired((Node<K, V>)node, this.expirationTicker().read())) {
               cause[0] = RemovalCause.EXPIRED;
            } else {
               if (!node.containsValue(value)) {
                  return node;
               }

               cause[0] = RemovalCause.EXPLICIT;
            }

            if (cause[0].wasEvicted()) {
               this.notifyEviction(oldKey[0], oldValue[0], cause[0]);
            }

            this.discardRefresh(lookupKey);
            removed[0] = (Node<K, V>)node;
            node.retire();
            return null;
         }
      });
      if (removed[0] == null) {
         return false;
      }

      this.afterWrite(new BoundedLocalCache.RemovalTask(removed[0]));
      this.notifyRemoval(oldKey[0], oldValue[0], cause[0]);
      return cause[0] == RemovalCause.EXPLICIT;
   }

   @Override
   public V replace(K key, V value) {
      Objects.requireNonNull(key);
      long[] now = new long[1];
      int[] oldWeight = new int[1];
      boolean[] exceedsTolerance = new boolean[1];
      K[] nodeKey = (K[])(new Object[1]);
      V[] oldValue = (V[])(new Object[1]);
      int weight = this.weigher.weigh(key, value);
      Node<K, V> node = this.data.computeIfPresent(this.nodeFactory.newLookupKey(key), (k, n) -> {
         synchronized (n) {
            this.requireIsAlive(key, (Node<?, ?>)n);
            nodeKey[0] = n.getKey();
            oldValue[0] = n.getValue();
            oldWeight[0] = n.getWeight();
            if (nodeKey[0] != null && oldValue[0] != null && !this.hasExpired((Node<K, V>)n, now[0] = this.expirationTicker().read())) {
               long varTime = this.expireAfterUpdate((Node<K, V>)n, key, value, this.expiry(), now[0]);
               n.setValue(value, this.valueReferenceQueue());
               n.setWeight(weight);
               long expirationTime = this.isComputingAsync(value) ? now[0] + 6917529027641081854L : now[0];
               exceedsTolerance[0] = this.exceedsWriteTimeTolerance((Node<K, V>)n, varTime, expirationTime);
               if (exceedsTolerance[0]) {
                  this.setWriteTime((Node<K, V>)n, expirationTime);
               }

               this.setAccessTime((Node<K, V>)n, expirationTime);
               this.setVariableTime((Node<K, V>)n, varTime);
               this.discardRefresh(k);
               return n;
            } else {
               oldValue[0] = null;
               return n;
            }
         }
      });
      if (nodeKey[0] != null && oldValue[0] != null) {
         int weightedDifference = weight - oldWeight[0];
         if (!exceedsTolerance[0] && weightedDifference == 0) {
            this.afterRead(node, now[0], false);
         } else {
            this.afterWrite(new BoundedLocalCache.UpdateTask(node, weightedDifference));
         }

         this.notifyOnReplace(nodeKey[0], oldValue[0], value);
         return oldValue[0];
      } else {
         return null;
      }
   }

   @Override
   public boolean replace(K key, V oldValue, V newValue) {
      return this.replace(key, oldValue, newValue, true);
   }

   @Override
   public boolean replace(K key, V oldValue, V newValue, boolean shouldDiscardRefresh) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(oldValue);
      long[] now = new long[1];
      int[] oldWeight = new int[1];
      boolean[] exceedsTolerance = new boolean[1];
      K[] nodeKey = (K[])(new Object[1]);
      V[] prevValue = (V[])(new Object[1]);
      int weight = this.weigher.weigh(key, newValue);
      Node<K, V> node = this.data
         .computeIfPresent(
            this.nodeFactory.newLookupKey(key),
            (k, n) -> {
               synchronized (n) {
                  this.requireIsAlive(key, (Node<?, ?>)n);
                  nodeKey[0] = n.getKey();
                  prevValue[0] = n.getValue();
                  oldWeight[0] = n.getWeight();
                  if (nodeKey[0] != null
                     && prevValue[0] != null
                     && n.containsValue(oldValue)
                     && !this.hasExpired((Node<K, V>)n, now[0] = this.expirationTicker().read())) {
                     long varTime = this.expireAfterUpdate((Node<K, V>)n, key, newValue, this.expiry(), now[0]);
                     n.setValue(newValue, this.valueReferenceQueue());
                     n.setWeight(weight);
                     long expirationTime = this.isComputingAsync(newValue) ? now[0] + 6917529027641081854L : now[0];
                     exceedsTolerance[0] = this.exceedsWriteTimeTolerance((Node<K, V>)n, varTime, expirationTime);
                     if (exceedsTolerance[0]) {
                        this.setWriteTime((Node<K, V>)n, expirationTime);
                     }

                     this.setAccessTime((Node<K, V>)n, expirationTime);
                     this.setVariableTime((Node<K, V>)n, varTime);
                     if (shouldDiscardRefresh) {
                        this.discardRefresh(k);
                     }

                     return n;
                  } else {
                     prevValue[0] = null;
                     return n;
                  }
               }
            }
         );
      if (nodeKey[0] != null && prevValue[0] != null) {
         int weightedDifference = weight - oldWeight[0];
         if (!exceedsTolerance[0] && weightedDifference == 0) {
            this.afterRead(node, now[0], false);
         } else {
            this.afterWrite(new BoundedLocalCache.UpdateTask(node, weightedDifference));
         }

         this.notifyOnReplace(nodeKey[0], prevValue[0], newValue);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      BiFunction<K, V, V> remappingFunction = (keyx, oldValue) -> Objects.requireNonNull((V)function.apply((K)keyx, oldValue));

      for (K key : this.keySet()) {
         long[] now = new long[]{this.expirationTicker().read()};
         Object lookupKey = this.nodeFactory.newLookupKey(key);
         this.remap(key, lookupKey, remappingFunction, this.expiry(), now, false);
      }
   }

   @Override
   public @Nullable V computeIfAbsent(K key, @Var Function<? super K, ? extends V> mappingFunction, boolean recordStats, boolean recordLoad) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(mappingFunction);
      long now = this.expirationTicker().read();
      Node<K, V> node = this.data.get(this.nodeFactory.newLookupKey(key));
      if (node != null) {
         V value = node.getValue();
         if (value != null && !this.hasExpired(node, now)) {
            if (!this.isComputingAsync(value)) {
               this.tryExpireAfterRead(node, key, value, this.expiry(), now);
               this.setAccessTime(node, now);
            }

            V refreshed = this.afterRead(node, now, recordStats);
            return refreshed == null ? value : refreshed;
         }
      }

      if (recordStats) {
         mappingFunction = this.statsAware(mappingFunction, recordLoad);
      }

      Object keyRef = this.nodeFactory.newReferenceKey(key, this.keyReferenceQueue());
      return this.doComputeIfAbsent(key, keyRef, mappingFunction, new long[]{now}, recordStats);
   }

   V doComputeIfAbsent(K key, Object keyRef, Function<? super K, ? extends V> mappingFunction, long[] now, boolean recordStats) {
      V[] oldValue = (V[])(new Object[1]);
      V[] newValue = (V[])(new Object[1]);
      K[] nodeKey = (K[])(new Object[1]);
      Node<K, V>[] removed = new Node[1];
      int[] weight = new int[2];
      RemovalCause[] cause = new RemovalCause[1];
      Node<K, V> node = this.data.compute(keyRef, (k, n) -> {
         if (n == null) {
            newValue[0] = (V)mappingFunction.apply(key);
            if (newValue[0] == null) {
               return null;
            }

            now[0] = this.expirationTicker().read();
            weight[1] = this.weigher.weigh(key, newValue[0]);
            Node<K, V> created = this.nodeFactory.newNode(key, this.keyReferenceQueue(), newValue[0], this.valueReferenceQueue(), weight[1], now[0]);
            long expirationTime = this.isComputingAsync(newValue[0]) ? now[0] + 6917529027641081854L : now[0];
            this.setVariableTime(created, this.expireAfterCreate(key, newValue[0], this.expiry(), now[0]));
            this.setAccessTime(created, expirationTime);
            this.setWriteTime(created, expirationTime);
            return created;
         } else {
            synchronized (n) {
               this.requireIsAlive(key, (Node<?, ?>)n);
               nodeKey[0] = n.getKey();
               weight[0] = n.getWeight();
               oldValue[0] = n.getValue();
               if (nodeKey[0] != null && oldValue[0] != null) {
                  if (!this.hasExpired((Node<K, V>)n, now[0])) {
                     return n;
                  }

                  cause[0] = RemovalCause.EXPIRED;
               } else {
                  cause[0] = RemovalCause.COLLECTED;
               }

               if (cause[0].wasEvicted()) {
                  this.notifyEviction(nodeKey[0], oldValue[0], cause[0]);
               }

               newValue[0] = (V)mappingFunction.apply(key);
               if (newValue[0] == null) {
                  removed[0] = (Node<K, V>)n;
                  n.retire();
                  return null;
               } else {
                  now[0] = this.expirationTicker().read();
                  weight[1] = this.weigher.weigh(key, newValue[0]);
                  long varTime = this.expireAfterCreate(key, newValue[0], this.expiry(), now[0]);
                  n.setValue(newValue[0], this.valueReferenceQueue());
                  n.setWeight(weight[1]);
                  long expirationTime = this.isComputingAsync(newValue[0]) ? now[0] + 6917529027641081854L : now[0];
                  this.setAccessTime((Node<K, V>)n, expirationTime);
                  this.setWriteTime((Node<K, V>)n, expirationTime);
                  this.setVariableTime((Node<K, V>)n, varTime);
                  this.discardRefresh(k);
                  return n;
               }
            }
         }
      });
      if (cause[0] != null) {
         if (cause[0].wasEvicted()) {
            this.statsCounter().recordEviction(weight[0], cause[0]);
         }

         this.notifyRemoval(nodeKey[0], oldValue[0], cause[0]);
      }

      if (node == null) {
         if (removed[0] != null) {
            this.afterWrite(new BoundedLocalCache.RemovalTask(removed[0]));
         }

         return null;
      } else if (newValue[0] == null) {
         if (!this.isComputingAsync(oldValue[0])) {
            this.tryExpireAfterRead(node, key, oldValue[0], this.expiry(), now[0]);
            this.setAccessTime(node, now[0]);
         }

         this.afterRead(node, now[0], recordStats);
         return oldValue[0];
      } else {
         if (oldValue[0] == null && cause[0] == null) {
            this.afterWrite(new BoundedLocalCache.AddTask(node, weight[1]));
         } else {
            int weightedDifference = weight[1] - weight[0];
            this.afterWrite(new BoundedLocalCache.UpdateTask(node, weightedDifference));
         }

         return newValue[0];
      }
   }

   @Override
   public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(remappingFunction);
      Object lookupKey = this.nodeFactory.newLookupKey(key);
      Node<K, V> node = this.data.get(lookupKey);
      if (node == null) {
         return null;
      } else {
         long now;
         if (node.getValue() != null && !this.hasExpired(node, now = this.expirationTicker().read())) {
            BiFunction<? super K, ? super V, ? extends V> statsAwareRemappingFunction = this.statsAware(remappingFunction, true, true);
            return this.remap(key, lookupKey, statsAwareRemappingFunction, this.expiry(), new long[]{now}, false);
         } else {
            this.scheduleDrainBuffers();
            return null;
         }
      }
   }

   @Override
   public @Nullable V compute(
      K key,
      BiFunction<? super K, ? super V, ? extends V> remappingFunction,
      @Nullable Expiry<? super K, ? super V> expiry,
      boolean recordLoad,
      boolean recordLoadFailure
   ) {
      Objects.requireNonNull(key);
      long[] now = new long[]{this.expirationTicker().read()};
      Object keyRef = this.nodeFactory.newReferenceKey(key, this.keyReferenceQueue());
      BiFunction<? super K, ? super V, ? extends V> statsAwareRemappingFunction = this.statsAware(remappingFunction, recordLoad, recordLoadFailure);
      return this.remap(key, keyRef, statsAwareRemappingFunction, expiry, now, true);
   }

   @Override
   public @Nullable V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(value);
      long[] now = new long[]{this.expirationTicker().read()};
      Object keyRef = this.nodeFactory.newReferenceKey(key, this.keyReferenceQueue());
      BiFunction<? super K, ? super V, ? extends V> mergeFunction = (k, oldValue) -> oldValue == null
         ? value
         : this.statsAware(remappingFunction).apply(oldValue, value);
      return this.remap(key, keyRef, mergeFunction, this.expiry(), now, true);
   }

   V remap(
      K key,
      Object keyRef,
      BiFunction<? super K, ? super V, ? extends V> remappingFunction,
      Expiry<? super K, ? super V> expiry,
      long[] now,
      boolean computeIfAbsent
   ) {
      K[] nodeKey = (K[])(new Object[1]);
      V[] oldValue = (V[])(new Object[1]);
      V[] newValue = (V[])(new Object[1]);
      Node<K, V>[] removed = new Node[1];
      int[] weight = new int[2];
      RemovalCause[] cause = new RemovalCause[1];
      boolean[] exceedsTolerance = new boolean[1];
      Node<K, V> node = this.data.compute(keyRef, (kr, n) -> {
         if (n == null) {
            if (!computeIfAbsent) {
               return null;
            }

            newValue[0] = (V)remappingFunction.apply(key, null);
            if (newValue[0] == null) {
               return null;
            }

            now[0] = this.expirationTicker().read();
            weight[1] = this.weigher.weigh(key, newValue[0]);
            long varTime = this.expireAfterCreate(key, newValue[0], expiry, now[0]);
            Node<K, V> created = this.nodeFactory.newNode(keyRef, newValue[0], this.valueReferenceQueue(), weight[1], now[0]);
            long expirationTime = this.isComputingAsync(newValue[0]) ? now[0] + 6917529027641081854L : now[0];
            this.setAccessTime(created, expirationTime);
            this.setWriteTime(created, expirationTime);
            this.setVariableTime(created, varTime);
            this.discardRefresh(key);
            return created;
         } else {
            synchronized (n) {
               this.requireIsAlive(key, (Node<?, ?>)n);
               nodeKey[0] = n.getKey();
               oldValue[0] = n.getValue();
               if (nodeKey[0] == null || oldValue[0] == null) {
                  cause[0] = RemovalCause.COLLECTED;
               } else if (this.hasExpired((Node<K, V>)n, this.expirationTicker().read())) {
                  cause[0] = RemovalCause.EXPIRED;
               }

               if (cause[0] != null) {
                  this.notifyEviction(nodeKey[0], oldValue[0], cause[0]);
                  if (!computeIfAbsent) {
                     removed[0] = (Node<K, V>)n;
                     n.retire();
                     return null;
                  }
               }

               newValue[0] = (V)remappingFunction.apply(nodeKey[0], cause[0] == null ? oldValue[0] : null);
               if (newValue[0] == null) {
                  if (cause[0] == null) {
                     cause[0] = RemovalCause.EXPLICIT;
                     this.discardRefresh(kr);
                  }

                  removed[0] = (Node<K, V>)n;
                  n.retire();
                  return null;
               } else {
                  weight[0] = n.getWeight();
                  weight[1] = this.weigher.weigh(key, newValue[0]);
                  now[0] = this.expirationTicker().read();
                  long varTime;
                  if (cause[0] == null) {
                     if (newValue[0] != oldValue[0]) {
                        cause[0] = RemovalCause.REPLACED;
                     }

                     varTime = this.expireAfterUpdate((Node<K, V>)n, key, newValue[0], expiry, now[0]);
                  } else {
                     varTime = this.expireAfterCreate(key, newValue[0], expiry, now[0]);
                  }

                  n.setValue(newValue[0], this.valueReferenceQueue());
                  n.setWeight(weight[1]);
                  long expirationTime = this.isComputingAsync(newValue[0]) ? now[0] + 6917529027641081854L : now[0];
                  exceedsTolerance[0] = this.exceedsWriteTimeTolerance((Node<K, V>)n, varTime, expirationTime);
                  if (cause[0] != null && cause[0].wasEvicted() || exceedsTolerance[0]) {
                     this.setWriteTime((Node<K, V>)n, expirationTime);
                  }

                  this.setAccessTime((Node<K, V>)n, expirationTime);
                  this.setVariableTime((Node<K, V>)n, varTime);
                  this.discardRefresh(kr);
                  return n;
               }
            }
         }
      });
      if (cause[0] != null) {
         if (cause[0] == RemovalCause.REPLACED) {
            Objects.requireNonNull(newValue[0]);
            this.notifyOnReplace(key, oldValue[0], newValue[0]);
         } else {
            if (cause[0].wasEvicted()) {
               this.statsCounter().recordEviction(weight[0], cause[0]);
            }

            this.notifyRemoval(nodeKey[0], oldValue[0], cause[0]);
         }
      }

      if (removed[0] != null) {
         this.afterWrite(new BoundedLocalCache.RemovalTask(removed[0]));
      } else if (node != null) {
         if (oldValue[0] == null && cause[0] == null) {
            this.afterWrite(new BoundedLocalCache.AddTask(node, weight[1]));
         } else {
            int weightedDifference = weight[1] - weight[0];
            if (!exceedsTolerance[0] && weightedDifference == 0) {
               this.afterRead(node, now[0], false);
               if (cause[0] != null && cause[0].wasEvicted()) {
                  this.scheduleDrainBuffers();
               }
            } else {
               this.afterWrite(new BoundedLocalCache.UpdateTask(node, weightedDifference));
            }
         }
      }

      return newValue[0];
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      BoundedLocalCache.EntryIterator<K, V> iterator = new BoundedLocalCache.EntryIterator<>(this);

      while (iterator.hasNext()) {
         action.accept(iterator.key, iterator.value);
         iterator.advance();
      }
   }

   @Override
   public Set<K> keySet() {
      Set<K> ks = this.keySet;
      return ks == null ? (this.keySet = new BoundedLocalCache.KeySetView<>(this)) : ks;
   }

   @Override
   public Collection<V> values() {
      Collection<V> vs = this.values;
      return vs == null ? (this.values = new BoundedLocalCache.ValuesView<>(this)) : vs;
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      Set<Entry<K, V>> es = this.entrySet;
      return es == null ? (this.entrySet = new BoundedLocalCache.EntrySetView<>(this)) : es;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      }

      if (!(o instanceof Map)) {
         return false;
      }

      Map<?, ?> map = (Map<?, ?>)o;
      if (this.size() != map.size()) {
         return false;
      }

      long now = this.expirationTicker().read();

      for (Node<K, V> node : this.data.values()) {
         K key = node.getKey();
         V value = node.getValue();
         if (key != null && value != null && node.isAlive() && !this.hasExpired(node, now)) {
            Object val = map.get(key);
            if (val != null && (val == value || val.equals(value))) {
               continue;
            }

            return false;
         }

         this.scheduleDrainBuffers();
         return false;
      }

      return true;
   }

   @Override
   public int hashCode() {
      int hash = 0;
      long now = this.expirationTicker().read();

      for (Node<K, V> node : this.data.values()) {
         K key = node.getKey();
         V value = node.getValue();
         if (key != null && value != null && node.isAlive() && !this.hasExpired(node, now)) {
            hash += key.hashCode() ^ value.hashCode();
         } else {
            this.scheduleDrainBuffers();
         }
      }

      return hash;
   }

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder().append('{');
      long now = this.expirationTicker().read();

      for (Node<K, V> node : this.data.values()) {
         K key = node.getKey();
         V value = node.getValue();
         if (key != null && value != null && node.isAlive() && !this.hasExpired(node, now)) {
            if (result.length() != 1) {
               result.append(',').append(' ');
            }

            result.append(key == this ? "(this Map)" : key);
            result.append('=');
            result.append(value == this ? "(this Map)" : value);
         } else {
            this.scheduleDrainBuffers();
         }
      }

      return result.append('}').toString();
   }

   <T> T evictionOrder(boolean hottest, Function<@Nullable V, @Nullable V> transformer, Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
      Comparator<Node<K, V>> comparator = Comparator.comparingInt(node -> {
         K key = node.getKey();
         return key == null ? 0 : this.frequencySketch().frequency(key);
      });
      Iterable<Node<K, V>> iterable;
      if (hottest) {
         iterable = () -> {
            LinkedDeque.PeekingIterator<Node<K, V>> secondary = LinkedDeque.PeekingIterator.comparing(
               this.accessOrderProbationDeque().descendingIterator(), this.accessOrderWindowDeque().descendingIterator(), comparator
            );
            return LinkedDeque.PeekingIterator.concat(this.accessOrderProtectedDeque().descendingIterator(), secondary);
         };
      } else {
         iterable = () -> {
            LinkedDeque.PeekingIterator<Node<K, V>> primary = LinkedDeque.PeekingIterator.comparing(
               this.accessOrderWindowDeque().iterator(), this.accessOrderProbationDeque().iterator(), comparator.reversed()
            );
            return LinkedDeque.PeekingIterator.concat(primary, this.accessOrderProtectedDeque().iterator());
         };
      }

      return this.snapshot(iterable, transformer, mappingFunction);
   }

   <T> T expireAfterAccessOrder(boolean oldest, Function<@Nullable V, @Nullable V> transformer, Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
      Iterable<Node<K, V>> iterable;
      if (this.evicts()) {
         iterable = () -> {
            Comparator<Node<K, V>> comparator = Comparator.comparingLong(Node::getAccessTime);
            LinkedDeque.PeekingIterator<Node<K, V>> first;
            LinkedDeque.PeekingIterator<Node<K, V>> second;
            LinkedDeque.PeekingIterator<Node<K, V>> third;
            if (oldest) {
               first = this.accessOrderWindowDeque().iterator();
               second = this.accessOrderProbationDeque().iterator();
               third = this.accessOrderProtectedDeque().iterator();
            } else {
               comparator = comparator.reversed();
               first = this.accessOrderWindowDeque().descendingIterator();
               second = this.accessOrderProbationDeque().descendingIterator();
               third = this.accessOrderProtectedDeque().descendingIterator();
            }

            return LinkedDeque.PeekingIterator.comparing(LinkedDeque.PeekingIterator.comparing(first, second, comparator), third, comparator);
         };
      } else {
         iterable = oldest ? this.accessOrderWindowDeque() : this.accessOrderWindowDeque()::descendingIterator;
      }

      return this.snapshot(iterable, transformer, mappingFunction);
   }

   <T> T snapshot(Iterable<Node<K, V>> iterable, Function<@Nullable V, @Nullable V> transformer, Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      Objects.requireNonNull(transformer);
      Objects.requireNonNull(iterable);
      this.evictionLock.lock();

      try {
         this.maintenance(null);

         try (Stream<Node<K, V>> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterable.iterator(), 1297), false)) {
            return mappingFunction.apply(
               stream.<Policy.CacheEntry<K, V>>map(node -> this.nodeToCacheEntry((Node<K, V>)node, transformer)).filter(Objects::nonNull)
            );
         }
      } finally {
         this.evictionLock.unlock();
         this.rescheduleCleanUpIfIncomplete();
      }
   }

   Policy.@Nullable CacheEntry<K, V> nodeToCacheEntry(Node<K, V> node, Function<@Nullable V, @Nullable V> transformer) {
      V value = transformer.apply(node.getValue());
      K key = node.getKey();
      long now;
      if (key != null && value != null && node.isAlive() && !this.hasExpired(node, now = this.expirationTicker().read())) {
         long expiresAfter = Long.MAX_VALUE;
         if (this.expiresAfterAccess()) {
            expiresAfter = Math.min(expiresAfter, now - node.getAccessTime() + this.expiresAfterAccessNanos());
         }

         if (this.expiresAfterWrite()) {
            expiresAfter = Math.min(expiresAfter, (now & -2L) - (node.getWriteTime() & -2L) + this.expiresAfterWriteNanos());
         }

         if (this.expiresVariable()) {
            expiresAfter = node.getVariableTime() - now;
         }

         long refreshableAt = this.refreshAfterWrite() ? node.getWriteTime() + this.refreshAfterWriteNanos() : now + Long.MAX_VALUE;
         int weight = node.getPolicyWeight();
         return SnapshotEntry.forEntry(key, value, now, weight, now + expiresAfter, refreshableAt);
      } else {
         return null;
      }
   }

   static <K, V> SerializationProxy<K, V> makeSerializationProxy(BoundedLocalCache<?, ?> cache) {
      SerializationProxy<K, V> proxy = new SerializationProxy<>();
      proxy.weakKeys = cache.collectKeys();
      proxy.weakValues = cache.nodeFactory.weakValues();
      proxy.softValues = cache.nodeFactory.softValues();
      proxy.isRecordingStats = cache.isRecordingStats();
      proxy.evictionListener = cache.evictionListener;
      proxy.removalListener = cache.removalListener();
      proxy.ticker = cache.expirationTicker();
      if (cache.expiresAfterAccess()) {
         proxy.expiresAfterAccessNanos = cache.expiresAfterAccessNanos();
      }

      if (cache.expiresAfterWrite()) {
         proxy.expiresAfterWriteNanos = cache.expiresAfterWriteNanos();
      }

      if (cache.expiresVariable()) {
         proxy.expiry = cache.expiry();
      }

      if (cache.refreshAfterWrite()) {
         proxy.refreshAfterWriteNanos = cache.refreshAfterWriteNanos();
      }

      if (cache.evicts()) {
         if (cache.isWeighted) {
            proxy.weigher = cache.weigher;
            proxy.maximumWeight = cache.maximum();
         } else {
            proxy.maximumSize = cache.maximum();
         }
      }

      proxy.cacheLoader = cache.cacheLoader;
      proxy.async = cache.isAsync;
      return proxy;
   }

   final class AddTask implements Runnable {
      final Node<K, V> node;
      final int weight;

      AddTask(Node<K, V> node, int weight) {
         this.weight = weight;
         this.node = node;
      }

      @GuardedBy("evictionLock")
      @Override
      public void run() {
         if (BoundedLocalCache.this.evicts()) {
            BoundedLocalCache.this.setWeightedSize(BoundedLocalCache.this.weightedSize() + this.weight);
            BoundedLocalCache.this.setWindowWeightedSize(BoundedLocalCache.this.windowWeightedSize() + this.weight);
            this.node.setPolicyWeight(this.node.getPolicyWeight() + this.weight);
            long maximum = BoundedLocalCache.this.maximum();
            if (BoundedLocalCache.this.weightedSize() >= maximum >>> 1) {
               if (BoundedLocalCache.this.weightedSize() > 9223372034707292160L) {
                  BoundedLocalCache.this.evictEntries();
               } else {
                  long capacity = BoundedLocalCache.this.isWeighted() ? BoundedLocalCache.this.data.mappingCount() : maximum;
                  BoundedLocalCache.this.frequencySketch().ensureCapacity(capacity);
               }
            }

            K key = this.node.getKey();
            if (key != null) {
               BoundedLocalCache.this.frequencySketch().increment(key);
            }

            BoundedLocalCache.this.setMissesInSample(BoundedLocalCache.this.missesInSample() + 1);
         }

         boolean isAlive;
         synchronized (this.node) {
            isAlive = this.node.isAlive();
         }

         if (isAlive) {
            if (BoundedLocalCache.this.expiresAfterWrite()) {
               BoundedLocalCache.this.writeOrderDeque().offerLast(this.node);
            }

            if (BoundedLocalCache.this.expiresVariable()) {
               BoundedLocalCache.this.timerWheel().schedule(this.node);
            }

            if (BoundedLocalCache.this.evicts()) {
               if (this.weight > BoundedLocalCache.this.maximum()) {
                  BoundedLocalCache.this.evictEntry(this.node, RemovalCause.SIZE, BoundedLocalCache.this.expirationTicker().read());
               } else if (this.weight > BoundedLocalCache.this.windowMaximum()) {
                  BoundedLocalCache.this.accessOrderWindowDeque().offerFirst(this.node);
               } else {
                  BoundedLocalCache.this.accessOrderWindowDeque().offerLast(this.node);
               }
            } else if (BoundedLocalCache.this.expiresAfterAccess()) {
               BoundedLocalCache.this.accessOrderWindowDeque().offerLast(this.node);
            }
         }
      }
   }

   static final class BoundedLocalAsyncCache<K, V> implements LocalAsyncCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final BoundedLocalCache<K, CompletableFuture<V>> cache;
      final boolean isWeighted;
      @Nullable ConcurrentMap<K, CompletableFuture<V>> mapView;
      LocalAsyncCache.@Nullable CacheView<K, V> cacheView;
      @Nullable Policy<K, V> policy;

      BoundedLocalAsyncCache(Caffeine<K, V> builder) {
         this.cache = LocalCacheFactory.newBoundedLocalCache(builder, null, true);
         this.isWeighted = builder.isWeighted();
      }

      public BoundedLocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      @Override
      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<>(this)) : this.mapView;
      }

      @Override
      public Cache<K, V> synchronous() {
         return this.cacheView == null ? (this.cacheView = new LocalAsyncCache.CacheView<>(this)) : this.cacheView;
      }

      @Override
      public Policy<K, V> policy() {
         if (this.policy == null) {
            BoundedLocalCache<K, V> castCache = this.cache;
            Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
            Function<V, V> castTransformer = transformer;
            this.policy = new BoundedLocalCache.BoundedPolicy<>(castCache, castTransformer, this.isWeighted);
         }

         return this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private Object writeReplace() {
         return BoundedLocalCache.makeSerializationProxy(this.cache);
      }
   }

   static final class BoundedLocalAsyncLoadingCache<K, V> extends LocalAsyncLoadingCache<K, V> implements Serializable {
      private static final long serialVersionUID = 1L;
      final BoundedLocalCache<K, CompletableFuture<V>> cache;
      final boolean isWeighted;
      @Nullable ConcurrentMap<K, CompletableFuture<V>> mapView;
      @Nullable Policy<K, V> policy;

      BoundedLocalAsyncLoadingCache(Caffeine<K, V> builder, AsyncCacheLoader<? super K, V> loader) {
         super(loader);
         this.isWeighted = builder.isWeighted();
         this.cache = LocalCacheFactory.newBoundedLocalCache(builder, loader, true);
      }

      public BoundedLocalCache<K, CompletableFuture<V>> cache() {
         return this.cache;
      }

      @Override
      public ConcurrentMap<K, CompletableFuture<V>> asMap() {
         return this.mapView == null ? (this.mapView = new LocalAsyncCache.AsyncAsMapView<>(this)) : this.mapView;
      }

      @Override
      public Policy<K, V> policy() {
         if (this.policy == null) {
            BoundedLocalCache<K, V> castCache = this.cache;
            Function<CompletableFuture<V>, V> transformer = Async::getIfReady;
            Function<V, V> castTransformer = transformer;
            this.policy = new BoundedLocalCache.BoundedPolicy<>(castCache, castTransformer, this.isWeighted);
         }

         return this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private Object writeReplace() {
         return BoundedLocalCache.makeSerializationProxy(this.cache);
      }
   }

   static final class BoundedLocalLoadingCache<K, V> extends BoundedLocalCache.BoundedLocalManualCache<K, V> implements LocalLoadingCache<K, V> {
      private static final long serialVersionUID = 1L;
      final Function<K, @Nullable V> mappingFunction;
      final @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction;

      BoundedLocalLoadingCache(Caffeine<K, V> builder, CacheLoader<? super K, V> loader) {
         super(builder, loader);
         Objects.requireNonNull(loader);
         this.mappingFunction = LocalLoadingCache.newMappingFunction(loader);
         this.bulkMappingFunction = LocalLoadingCache.newBulkMappingFunction(loader);
      }

      @Override
      public AsyncCacheLoader<? super K, V> cacheLoader() {
         return this.cache.cacheLoader;
      }

      @Override
      public Function<K, @Nullable V> mappingFunction() {
         return this.mappingFunction;
      }

      @Override
      public @Nullable Function<Set<? extends K>, Map<K, V>> bulkMappingFunction() {
         return this.bulkMappingFunction;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private Object writeReplace() {
         return BoundedLocalCache.makeSerializationProxy(this.cache);
      }
   }

   static class BoundedLocalManualCache<K, V> implements LocalManualCache<K, V>, Serializable {
      private static final long serialVersionUID = 1L;
      final BoundedLocalCache<K, V> cache;
      @Nullable Policy<K, V> policy;

      BoundedLocalManualCache(Caffeine<K, V> builder) {
         this(builder, null);
      }

      BoundedLocalManualCache(Caffeine<K, V> builder, @Nullable CacheLoader<? super K, V> loader) {
         this.cache = LocalCacheFactory.newBoundedLocalCache(builder, loader, false);
      }

      public final BoundedLocalCache<K, V> cache() {
         return this.cache;
      }

      @Override
      public final Policy<K, V> policy() {
         if (this.policy == null) {
            Function<V, V> identity = v -> v;
            this.policy = new BoundedLocalCache.BoundedPolicy<>(this.cache, identity, this.cache.isWeighted);
         }

         return this.policy;
      }

      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Proxy required");
      }

      private Object writeReplace() {
         return BoundedLocalCache.makeSerializationProxy(this.cache);
      }
   }

   static final class BoundedPolicy<K, V> implements Policy<K, V> {
      final Function<@Nullable V, @Nullable V> transformer;
      final BoundedLocalCache<K, V> cache;
      final boolean isWeighted;
      @Nullable Optional<Policy.Eviction<K, V>> eviction;
      @Nullable Optional<Policy.FixedRefresh<K, V>> refreshes;
      @Nullable Optional<Policy.FixedExpiration<K, V>> afterWrite;
      @Nullable Optional<Policy.FixedExpiration<K, V>> afterAccess;
      @Nullable Optional<Policy.VarExpiration<K, V>> variable;

      BoundedPolicy(BoundedLocalCache<K, V> cache, Function<@Nullable V, @Nullable V> transformer, boolean isWeighted) {
         this.transformer = transformer;
         this.isWeighted = isWeighted;
         this.cache = cache;
      }

      @Override
      public boolean isRecordingStats() {
         return this.cache.isRecordingStats();
      }

      @Override
      public @Nullable V getIfPresentQuietly(K key) {
         return this.transformer.apply(this.cache.getIfPresentQuietly(key));
      }

      @Override
      public Policy.@Nullable CacheEntry<K, V> getEntryIfPresentQuietly(K key) {
         Node<K, V> node = this.cache.data.get(this.cache.nodeFactory.newLookupKey(key));
         return node == null ? null : this.cache.nodeToCacheEntry(node, this.transformer);
      }

      @Override
      public Map<K, CompletableFuture<V>> refreshes() {
         ConcurrentMap<Object, CompletableFuture<?>> refreshes = this.cache.refreshes;
         if (refreshes != null && !refreshes.isEmpty()) {
            if (this.cache.collectKeys()) {
               IdentityHashMap<K, CompletableFuture<V>> inFlight = new IdentityHashMap<>(refreshes.size());

               for (Entry<Object, CompletableFuture<?>> entry : refreshes.entrySet()) {
                  K key = (K)((References.InternalReference)entry.getKey()).get();
                  CompletableFuture<V> future = (CompletableFuture<V>)entry.getValue();
                  if (key != null) {
                     inFlight.put(key, future);
                  }
               }

               return Collections.unmodifiableMap(inFlight);
            } else {
               Map<K, CompletableFuture<V>> castedRefreshes = (Map<K, CompletableFuture<V>>)refreshes;
               return Collections.unmodifiableMap(new HashMap<>(castedRefreshes));
            }
         } else {
            return Collections.unmodifiableMap(Collections.emptyMap());
         }
      }

      @Override
      public Optional<Policy.Eviction<K, V>> eviction() {
         return this.cache.evicts()
            ? (this.eviction == null ? (this.eviction = Optional.of(new BoundedLocalCache.BoundedPolicy.BoundedEviction())) : this.eviction)
            : Optional.empty();
      }

      @Override
      public Optional<Policy.FixedExpiration<K, V>> expireAfterAccess() {
         if (!this.cache.expiresAfterAccess()) {
            return Optional.empty();
         } else {
            return this.afterAccess == null
               ? (this.afterAccess = Optional.of(new BoundedLocalCache.BoundedPolicy.BoundedExpireAfterAccess()))
               : this.afterAccess;
         }
      }

      @Override
      public Optional<Policy.FixedExpiration<K, V>> expireAfterWrite() {
         if (!this.cache.expiresAfterWrite()) {
            return Optional.empty();
         } else {
            return this.afterWrite == null ? (this.afterWrite = Optional.of(new BoundedLocalCache.BoundedPolicy.BoundedExpireAfterWrite())) : this.afterWrite;
         }
      }

      @Override
      public Optional<Policy.VarExpiration<K, V>> expireVariably() {
         if (!this.cache.expiresVariable()) {
            return Optional.empty();
         } else {
            return this.variable == null ? (this.variable = Optional.of(new BoundedLocalCache.BoundedPolicy.BoundedVarExpiration())) : this.variable;
         }
      }

      @Override
      public Optional<Policy.FixedRefresh<K, V>> refreshAfterWrite() {
         if (!this.cache.refreshAfterWrite()) {
            return Optional.empty();
         } else {
            return this.refreshes == null ? (this.refreshes = Optional.of(new BoundedLocalCache.BoundedPolicy.BoundedRefreshAfterWrite())) : this.refreshes;
         }
      }

      final class BoundedEviction implements Policy.Eviction<K, V> {
         @Override
         public boolean isWeighted() {
            return BoundedPolicy.this.isWeighted;
         }

         @Override
         public OptionalInt weightOf(K key) {
            Objects.requireNonNull(key);
            if (!BoundedPolicy.this.isWeighted) {
               return OptionalInt.empty();
            }

            Node<K, V> node = BoundedPolicy.this.cache.data.get(BoundedPolicy.this.cache.nodeFactory.newLookupKey(key));
            if (node != null && !BoundedPolicy.this.cache.hasExpired(node, BoundedPolicy.this.cache.expirationTicker().read())) {
               synchronized (node) {
                  return OptionalInt.of(node.getWeight());
               }
            } else {
               return OptionalInt.empty();
            }
         }

         @Override
         public OptionalLong weightedSize() {
            if (BoundedPolicy.this.cache.evicts() && this.isWeighted()) {
               BoundedPolicy.this.cache.evictionLock.lock();

               try {
                  if (BoundedPolicy.this.cache.drainStatusOpaque() == 1) {
                     BoundedPolicy.this.cache.maintenance(null);
                  }

                  return OptionalLong.of(Math.max(0L, BoundedPolicy.this.cache.weightedSize()));
               } finally {
                  BoundedPolicy.this.cache.evictionLock.unlock();
                  BoundedPolicy.this.cache.rescheduleCleanUpIfIncomplete();
               }
            } else {
               return OptionalLong.empty();
            }
         }

         @Override
         public long getMaximum() {
            BoundedPolicy.this.cache.evictionLock.lock();

            try {
               if (BoundedPolicy.this.cache.drainStatusOpaque() == 1) {
                  BoundedPolicy.this.cache.maintenance(null);
               }

               return BoundedPolicy.this.cache.maximum();
            } finally {
               BoundedPolicy.this.cache.evictionLock.unlock();
               BoundedPolicy.this.cache.rescheduleCleanUpIfIncomplete();
            }
         }

         @Override
         public void setMaximum(long maximum) {
            BoundedPolicy.this.cache.evictionLock.lock();

            try {
               BoundedPolicy.this.cache.setMaximumSize(maximum);
               BoundedPolicy.this.cache.maintenance(null);
            } finally {
               BoundedPolicy.this.cache.evictionLock.unlock();
               BoundedPolicy.this.cache.rescheduleCleanUpIfIncomplete();
            }
         }

         @Override
         public Map<K, V> coldest(int limit) {
            int expectedSize = Math.min(limit, BoundedPolicy.this.cache.size());
            BoundedLocalCache.SizeLimiter<K, V> limiter = new BoundedLocalCache.SizeLimiter<>(expectedSize, limit);
            return BoundedPolicy.this.cache.evictionOrder(false, BoundedPolicy.this.transformer, limiter);
         }

         @Override
         public Map<K, V> coldestWeighted(long weightLimit) {
            Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>> limiter = this.isWeighted()
               ? new BoundedLocalCache.WeightLimiter<>(weightLimit)
               : new BoundedLocalCache.SizeLimiter<>((int)Math.min(weightLimit, BoundedPolicy.this.cache.size()), weightLimit);
            return BoundedPolicy.this.cache.evictionOrder(false, BoundedPolicy.this.transformer, limiter);
         }

         @Override
         public <T> T coldest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            return BoundedPolicy.this.cache.evictionOrder(false, BoundedPolicy.this.transformer, mappingFunction);
         }

         @Override
         public Map<K, V> hottest(int limit) {
            int expectedSize = Math.min(limit, BoundedPolicy.this.cache.size());
            BoundedLocalCache.SizeLimiter<K, V> limiter = new BoundedLocalCache.SizeLimiter<>(expectedSize, limit);
            return BoundedPolicy.this.cache.evictionOrder(true, BoundedPolicy.this.transformer, limiter);
         }

         @Override
         public Map<K, V> hottestWeighted(long weightLimit) {
            Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>> limiter = this.isWeighted()
               ? new BoundedLocalCache.WeightLimiter<>(weightLimit)
               : new BoundedLocalCache.SizeLimiter<>((int)Math.min(weightLimit, BoundedPolicy.this.cache.size()), weightLimit);
            return BoundedPolicy.this.cache.evictionOrder(true, BoundedPolicy.this.transformer, limiter);
         }

         @Override
         public <T> T hottest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            Objects.requireNonNull(mappingFunction);
            return BoundedPolicy.this.cache.evictionOrder(true, BoundedPolicy.this.transformer, mappingFunction);
         }
      }

      final class BoundedExpireAfterAccess implements Policy.FixedExpiration<K, V> {
         @Override
         public OptionalLong ageOf(K key, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = BoundedPolicy.this.cache.data.get(lookupKey);
            if (node == null) {
               return OptionalLong.empty();
            }

            long now = BoundedPolicy.this.cache.expirationTicker().read();
            return BoundedPolicy.this.cache.hasExpired(node, now)
               ? OptionalLong.empty()
               : OptionalLong.of(unit.convert(now - node.getAccessTime(), TimeUnit.NANOSECONDS));
         }

         @Override
         public long getExpiresAfter(TimeUnit unit) {
            return unit.convert(BoundedPolicy.this.cache.expiresAfterAccessNanos(), TimeUnit.NANOSECONDS);
         }

         @Override
         public void setExpiresAfter(long duration, TimeUnit unit) {
            Caffeine.requireArgument(duration >= 0L);
            BoundedPolicy.this.cache.setExpiresAfterAccessNanos(unit.toNanos(duration));
            BoundedPolicy.this.cache.scheduleAfterWrite();
         }

         @Override
         public Map<K, V> oldest(int limit) {
            return this.oldest(new BoundedLocalCache.SizeLimiter<>(Math.min(limit, BoundedPolicy.this.cache.size()), limit));
         }

         @Override
         public <T> T oldest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return BoundedPolicy.this.cache.expireAfterAccessOrder(true, BoundedPolicy.this.transformer, mappingFunction);
         }

         @Override
         public Map<K, V> youngest(int limit) {
            return this.youngest(new BoundedLocalCache.SizeLimiter<>(Math.min(limit, BoundedPolicy.this.cache.size()), limit));
         }

         @Override
         public <T> T youngest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return BoundedPolicy.this.cache.expireAfterAccessOrder(false, BoundedPolicy.this.transformer, mappingFunction);
         }
      }

      final class BoundedExpireAfterWrite implements Policy.FixedExpiration<K, V> {
         @Override
         public OptionalLong ageOf(K key, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = BoundedPolicy.this.cache.data.get(lookupKey);
            if (node == null) {
               return OptionalLong.empty();
            }

            long now = BoundedPolicy.this.cache.expirationTicker().read();
            return BoundedPolicy.this.cache.hasExpired(node, now)
               ? OptionalLong.empty()
               : OptionalLong.of(unit.convert(now - node.getWriteTime(), TimeUnit.NANOSECONDS));
         }

         @Override
         public long getExpiresAfter(TimeUnit unit) {
            return unit.convert(BoundedPolicy.this.cache.expiresAfterWriteNanos(), TimeUnit.NANOSECONDS);
         }

         @Override
         public void setExpiresAfter(long duration, TimeUnit unit) {
            Caffeine.requireArgument(duration >= 0L);
            BoundedPolicy.this.cache.setExpiresAfterWriteNanos(unit.toNanos(duration));
            BoundedPolicy.this.cache.scheduleAfterWrite();
         }

         @Override
         public Map<K, V> oldest(int limit) {
            return this.oldest(new BoundedLocalCache.SizeLimiter<>(Math.min(limit, BoundedPolicy.this.cache.size()), limit));
         }

         @Override
         public <T> T oldest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return BoundedPolicy.this.cache.snapshot(BoundedPolicy.this.cache.writeOrderDeque(), BoundedPolicy.this.transformer, mappingFunction);
         }

         @Override
         public Map<K, V> youngest(int limit) {
            return this.youngest(new BoundedLocalCache.SizeLimiter<>(Math.min(limit, BoundedPolicy.this.cache.size()), limit));
         }

         @Override
         public <T> T youngest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return BoundedPolicy.this.cache
               .snapshot(BoundedPolicy.this.cache.writeOrderDeque()::descendingIterator, BoundedPolicy.this.transformer, mappingFunction);
         }
      }

      final class BoundedRefreshAfterWrite implements Policy.FixedRefresh<K, V> {
         @Override
         public OptionalLong ageOf(K key, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = BoundedPolicy.this.cache.data.get(lookupKey);
            if (node == null) {
               return OptionalLong.empty();
            }

            long now = BoundedPolicy.this.cache.expirationTicker().read();
            return BoundedPolicy.this.cache.hasExpired(node, now)
               ? OptionalLong.empty()
               : OptionalLong.of(unit.convert(now - node.getWriteTime(), TimeUnit.NANOSECONDS));
         }

         @Override
         public long getRefreshesAfter(TimeUnit unit) {
            return unit.convert(BoundedPolicy.this.cache.refreshAfterWriteNanos(), TimeUnit.NANOSECONDS);
         }

         @Override
         public void setRefreshesAfter(long duration, TimeUnit unit) {
            Caffeine.requireArgument(duration >= 0L);
            BoundedPolicy.this.cache.setRefreshAfterWriteNanos(unit.toNanos(duration));
            BoundedPolicy.this.cache.scheduleAfterWrite();
         }
      }

      final class BoundedVarExpiration implements Policy.VarExpiration<K, V> {
         @Override
         public OptionalLong getExpiresAfter(K key, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = BoundedPolicy.this.cache.data.get(lookupKey);
            if (node == null) {
               return OptionalLong.empty();
            }

            long now = BoundedPolicy.this.cache.expirationTicker().read();
            return BoundedPolicy.this.cache.hasExpired(node, now)
               ? OptionalLong.empty()
               : OptionalLong.of(unit.convert(node.getVariableTime() - now, TimeUnit.NANOSECONDS));
         }

         @Override
         public void setExpiresAfter(K key, long duration, TimeUnit unit) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(unit);
            Caffeine.requireArgument(duration >= 0L);
            Object lookupKey = BoundedPolicy.this.cache.nodeFactory.newLookupKey(key);
            Node<K, V> node = BoundedPolicy.this.cache.data.get(lookupKey);
            if (node != null) {
               long durationNanos = TimeUnit.NANOSECONDS.convert(duration, unit);
               long now;
               synchronized (node) {
                  now = BoundedPolicy.this.cache.expirationTicker().read();
                  if (BoundedPolicy.this.cache.hasExpired(node, now)) {
                     return;
                  }

                  node.setVariableTime(now + Math.min(durationNanos, 4611686018427387903L));
               }

               BoundedPolicy.this.cache.afterRead(node, now, false);
            }
         }

         @Override
         public @Nullable V put(K key, V value, long duration, TimeUnit unit) {
            Objects.requireNonNull(unit);
            Objects.requireNonNull(value);
            Caffeine.requireArgument(duration >= 0L);
            return (V)(BoundedPolicy.this.cache.isAsync
               ? this.putAsync((V)key, (long)value, duration, unit)
               : this.putSync((V)key, (long)value, duration, unit, false));
         }

         @Override
         public @Nullable V putIfAbsent(K key, V value, long duration, TimeUnit unit) {
            Objects.requireNonNull(unit);
            Objects.requireNonNull(value);
            Caffeine.requireArgument(duration >= 0L);
            return (V)(BoundedPolicy.this.cache.isAsync
               ? this.putIfAbsentAsync((V)key, (long)value, duration, unit)
               : this.putSync((V)key, (long)value, duration, unit, true));
         }

         @Nullable V putSync(K key, V value, long duration, TimeUnit unit, boolean onlyIfAbsent) {
            BoundedLocalCache.BoundedPolicy.FixedExpireAfterWrite<K, V> expiry = new BoundedLocalCache.BoundedPolicy.FixedExpireAfterWrite<>(duration, unit);
            return BoundedPolicy.this.cache.put(key, value, expiry, onlyIfAbsent);
         }

         @Nullable V putIfAbsentAsync(K key, V value, long duration, TimeUnit unit) {
            Expiry<K, V> expiry = new Async.AsyncExpiry<>(new BoundedLocalCache.BoundedPolicy.FixedExpireAfterWrite<>(duration, unit));
            V asyncValue = (V)CompletableFuture.<V>completedFuture(value);

            while (true) {
               CompletableFuture<V> priorFuture = (CompletableFuture<V>)BoundedPolicy.this.cache.getIfPresent(key, false);
               if (priorFuture != null) {
                  if (!priorFuture.isDone()) {
                     Async.getWhenSuccessful(priorFuture);
                     continue;
                  }

                  V prior = Async.getWhenSuccessful(priorFuture);
                  if (prior != null) {
                     return prior;
                  }
               }

               boolean[] added = new boolean[]{false};
               CompletableFuture<V> computed = (CompletableFuture<V>)BoundedPolicy.this.cache.compute(key, (k, oldValue) -> {
                  CompletableFuture<V> oldValueFuture = (CompletableFuture<V>)oldValue;
                  added[0] = oldValueFuture == null || oldValueFuture.isDone() && Async.<V>getIfReady(oldValueFuture) == null;
                  return added[0] ? asyncValue : oldValue;
               }, expiry, false, false);
               if (added[0]) {
                  return null;
               }

               V prior = Async.getWhenSuccessful(computed);
               if (prior != null) {
                  return prior;
               }
            }
         }

         @Nullable V putAsync(K key, V value, long duration, TimeUnit unit) {
            Expiry<K, V> expiry = new Async.AsyncExpiry<>(new BoundedLocalCache.BoundedPolicy.FixedExpireAfterWrite<>(duration, unit));
            V asyncValue = (V)CompletableFuture.<V>completedFuture(value);
            CompletableFuture<V> oldValueFuture = (CompletableFuture<V>)BoundedPolicy.this.cache.put(key, asyncValue, expiry, false);
            return Async.getWhenSuccessful(oldValueFuture);
         }

         @Override
         public @Nullable V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Duration duration) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(duration);
            Objects.requireNonNull(remappingFunction);
            Caffeine.requireArgument(!duration.isNegative(), "duration cannot be negative: %s", duration);
            BoundedLocalCache.BoundedPolicy.FixedExpireAfterWrite<K, V> expiry = new BoundedLocalCache.BoundedPolicy.FixedExpireAfterWrite<>(
               Caffeine.toNanosSaturated(duration), TimeUnit.NANOSECONDS
            );
            return (V)(BoundedPolicy.this.cache.isAsync
               ? this.computeAsync(key, remappingFunction, expiry)
               : BoundedPolicy.this.cache.compute(key, remappingFunction, expiry, true, true));
         }

         @Nullable V computeAsync(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction, Expiry<? super K, ? super V> expiry) {
            LocalCache<K, CompletableFuture<V>> delegate = BoundedPolicy.this.cache;
            V[] newValue = (V[])(new Object[1]);

            CompletableFuture<V> valueFuture;
            do {
               Async.getWhenSuccessful(delegate.getIfPresentQuietly(key));
               valueFuture = delegate.compute(key, (k, oldValueFuture) -> {
                  if (oldValueFuture != null && !oldValueFuture.isDone()) {
                     return oldValueFuture;
                  }

                  V oldValue = Async.getIfReady((CompletableFuture<V>)oldValueFuture);
                  BiFunction<? super K, ? super V, ? extends V> function = delegate.statsAware(remappingFunction, true, true);
                  newValue[0] = (V)function.apply(key, oldValue);
                  return newValue[0] == null ? null : CompletableFuture.completedFuture(newValue[0]);
               }, new Async.AsyncExpiry<>(expiry), false, false);
               if (newValue[0] != null) {
                  return newValue[0];
               }
            } while (valueFuture != null);

            return null;
         }

         @Override
         public Map<K, V> oldest(int limit) {
            return this.oldest(new BoundedLocalCache.SizeLimiter<>(Math.min(limit, BoundedPolicy.this.cache.size()), limit));
         }

         @Override
         public <T> T oldest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return BoundedPolicy.this.cache.snapshot(BoundedPolicy.this.cache.timerWheel(), BoundedPolicy.this.transformer, mappingFunction);
         }

         @Override
         public Map<K, V> youngest(int limit) {
            return this.youngest(new BoundedLocalCache.SizeLimiter<>(Math.min(limit, BoundedPolicy.this.cache.size()), limit));
         }

         @Override
         public <T> T youngest(Function<Stream<Policy.CacheEntry<K, V>>, T> mappingFunction) {
            return BoundedPolicy.this.cache
               .snapshot(BoundedPolicy.this.cache.timerWheel()::descendingIterator, BoundedPolicy.this.transformer, mappingFunction);
         }
      }

      static final class FixedExpireAfterWrite<K, V> implements Expiry<K, V> {
         final long duration;
         final TimeUnit unit;

         FixedExpireAfterWrite(long duration, TimeUnit unit) {
            this.duration = duration;
            this.unit = unit;
         }

         @Override
         public long expireAfterCreate(K key, V value, long currentTime) {
            return this.unit.toNanos(this.duration);
         }

         @Override
         public long expireAfterUpdate(K key, V value, long currentTime, long currentDuration) {
            return this.unit.toNanos(this.duration);
         }

         @CanIgnoreReturnValue
         @Override
         public long expireAfterRead(K key, V value, long currentTime, long currentDuration) {
            return currentDuration;
         }
      }
   }

   static final class EntryIterator<K, V> implements Iterator<Entry<K, V>> {
      final BoundedLocalCache<K, V> cache;
      final Iterator<Node<K, V>> iterator;
      @Nullable K key;
      @Nullable V value;
      @Nullable K removalKey;
      @Nullable Node<K, V> next;

      EntryIterator(BoundedLocalCache<K, V> cache) {
         this.iterator = cache.data.values().iterator();
         this.cache = cache;
      }

      @Override
      public boolean hasNext() {
         if (this.next != null) {
            return true;
         }

         long now = this.cache.expirationTicker().read();

         while (this.iterator.hasNext()) {
            this.next = this.iterator.next();
            this.value = this.next.getValue();
            this.key = this.next.getKey();
            boolean evictable = this.key == null || this.value == null || this.cache.hasExpired(this.next, now);
            if (!evictable && this.next.isAlive()) {
               return true;
            }

            if (evictable) {
               this.cache.scheduleDrainBuffers();
            }

            this.advance();
         }

         return false;
      }

      void advance() {
         this.value = null;
         this.next = null;
         this.key = null;
      }

      K nextKey() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         }

         this.removalKey = this.key;
         this.advance();
         return Objects.requireNonNull(this.removalKey);
      }

      V nextValue() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         }

         this.removalKey = this.key;
         V val = this.value;
         this.advance();
         return Objects.requireNonNull(val);
      }

      public Entry<K, V> next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         }

         WriteThroughEntry<K, V> entry = new WriteThroughEntry<>(this.cache, Objects.requireNonNull(this.key), Objects.requireNonNull(this.value));
         this.removalKey = this.key;
         this.advance();
         return entry;
      }

      @Override
      public void remove() {
         if (this.removalKey == null) {
            throw new IllegalStateException();
         }

         this.cache.remove(this.removalKey);
         this.removalKey = null;
      }
   }

   static final class EntrySetView<K, V> extends AbstractSet<Entry<K, V>> {
      final BoundedLocalCache<K, V> cache;

      EntrySetView(BoundedLocalCache<K, V> cache) {
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public int size() {
         return this.cache.size();
      }

      @Override
      public void clear() {
         this.cache.clear();
      }

      @Override
      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<?, ?> entry = (Entry<?, ?>)o;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null) {
               Node<K, V> node = this.cache.data.get(this.cache.nodeFactory.newLookupKey(key));
               return node != null && node.containsValue(value);
            } else {
               return false;
            }
         }
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean modified = false;
         if (collection instanceof Set && collection.size() > this.size()) {
            for (Entry<K, V> entry : this) {
               if (collection.contains(entry)) {
                  modified |= this.remove(entry);
               }
            }
         } else {
            for (Object item : collection) {
               modified |= item != null && this.remove(item);
            }
         }

         return modified;
      }

      @Override
      public boolean remove(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         }

         Entry<?, ?> entry = (Entry<?, ?>)o;
         Object key = entry.getKey();
         return key != null && this.cache.remove(key, entry.getValue());
      }

      @Override
      public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
         boolean modified = false;

         for (Entry<K, V> entry : this) {
            if (filter.test(entry)) {
               modified |= this.cache.remove(entry.getKey(), entry.getValue());
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;

         for (Entry<K, V> entry : this) {
            if (!collection.contains(entry) && this.remove(entry)) {
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public Iterator<Entry<K, V>> iterator() {
         return new BoundedLocalCache.EntryIterator<>(this.cache);
      }

      @Override
      public Spliterator<Entry<K, V>> spliterator() {
         return new BoundedLocalCache.EntrySpliterator<>(this.cache);
      }
   }

   static final class EntrySpliterator<K, V> implements Spliterator<Entry<K, V>> {
      final Spliterator<Node<K, V>> spliterator;
      final BoundedLocalCache<K, V> cache;

      EntrySpliterator(BoundedLocalCache<K, V> cache) {
         this(cache, cache.data.values().spliterator());
      }

      EntrySpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
         this.spliterator = Objects.requireNonNull(spliterator);
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
         Objects.requireNonNull(action);
         Consumer<Node<K, V>> consumer = node -> {
            K key = node.getKey();
            V value = node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now)) {
               action.accept(new WriteThroughEntry<>(this.cache, key, value));
            }
         };
         this.spliterator.forEachRemaining(consumer);
      }

      @Override
      public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
         boolean[] advanced = new boolean[]{false};
         Consumer<Node<K, V>> consumer = node -> {
            K key = node.getKey();
            V value = node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now)) {
               action.accept(new WriteThroughEntry<>(this.cache, key, value));
               advanced[0] = true;
            }
         };

         while (this.spliterator.tryAdvance(consumer)) {
            if (advanced[0]) {
               return true;
            }
         }

         return false;
      }

      @Override
      public @Nullable Spliterator<Entry<K, V>> trySplit() {
         Spliterator<Node<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new BoundedLocalCache.EntrySpliterator<>(this.cache, split);
      }

      @Override
      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      @Override
      public int characteristics() {
         return 4353;
      }
   }

   static final class KeyIterator<K, V> implements Iterator<K> {
      final BoundedLocalCache.EntryIterator<K, V> iterator;

      KeyIterator(BoundedLocalCache<K, V> cache) {
         this.iterator = new BoundedLocalCache.EntryIterator<>(cache);
      }

      @Override
      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      @Override
      public K next() {
         return this.iterator.nextKey();
      }

      @Override
      public void remove() {
         this.iterator.remove();
      }
   }

   static final class KeySetView<K, V> extends AbstractSet<K> {
      final BoundedLocalCache<K, V> cache;

      KeySetView(BoundedLocalCache<K, V> cache) {
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public int size() {
         return this.cache.size();
      }

      @Override
      public void clear() {
         this.cache.clear();
      }

      @Override
      public boolean contains(Object o) {
         return this.cache.containsKey(o);
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean modified = false;
         if (collection instanceof Set && collection.size() > this.size()) {
            for (K key : this) {
               if (collection.contains(key)) {
                  modified |= this.remove(key);
               }
            }
         } else {
            for (Object item : collection) {
               modified |= item != null && this.remove(item);
            }
         }

         return modified;
      }

      @Override
      public boolean remove(Object o) {
         return this.cache.remove(o) != null;
      }

      @Override
      public boolean removeIf(Predicate<? super K> filter) {
         boolean modified = false;

         for (K key : this) {
            if (filter.test(key) && this.remove(key)) {
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;

         for (K key : this) {
            if (!collection.contains(key) && this.remove(key)) {
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public Iterator<K> iterator() {
         return new BoundedLocalCache.KeyIterator<>(this.cache);
      }

      @Override
      public Spliterator<K> spliterator() {
         return new BoundedLocalCache.KeySpliterator<>(this.cache);
      }
   }

   static final class KeySpliterator<K, V> implements Spliterator<K> {
      final Spliterator<Node<K, V>> spliterator;
      final BoundedLocalCache<K, V> cache;

      KeySpliterator(BoundedLocalCache<K, V> cache) {
         this(cache, cache.data.values().spliterator());
      }

      KeySpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
         this.spliterator = Objects.requireNonNull(spliterator);
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public void forEachRemaining(Consumer<? super K> action) {
         Objects.requireNonNull(action);
         Consumer<Node<K, V>> consumer = node -> {
            K key = node.getKey();
            V value = node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now)) {
               action.accept(key);
            }
         };
         this.spliterator.forEachRemaining(consumer);
      }

      @Override
      public boolean tryAdvance(Consumer<? super K> action) {
         boolean[] advanced = new boolean[]{false};
         Consumer<Node<K, V>> consumer = node -> {
            K key = node.getKey();
            V value = node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now)) {
               action.accept(key);
               advanced[0] = true;
            }
         };

         while (this.spliterator.tryAdvance(consumer)) {
            if (advanced[0]) {
               return true;
            }
         }

         return false;
      }

      @Override
      public @Nullable Spliterator<K> trySplit() {
         Spliterator<Node<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new BoundedLocalCache.KeySpliterator<>(this.cache, split);
      }

      @Override
      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      @Override
      public int characteristics() {
         return 4353;
      }
   }

   static final class PerformCleanupTask extends ForkJoinTask<Void> implements Runnable {
      private static final long serialVersionUID = 1L;
      final WeakReference<BoundedLocalCache<?, ?>> reference;

      PerformCleanupTask(BoundedLocalCache<?, ?> cache) {
         this.reference = new WeakReference<>(cache);
      }

      @Override
      public boolean exec() {
         try {
            this.run();
         } catch (Throwable t) {
            BoundedLocalCache.logger.log(Level.ERROR, "Exception thrown when performing the maintenance task", t);
         }

         return false;
      }

      @Override
      public void run() {
         BoundedLocalCache<?, ?> cache = this.reference.get();
         if (cache != null) {
            cache.performCleanUp(null);
         }
      }

      public void complete(@Nullable Void value) {
      }

      public void setRawResult(@Nullable Void value) {
      }

      public @Nullable Void getRawResult() {
         return null;
      }

      @Override
      public void completeExceptionally(@Nullable Throwable t) {
      }

      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
         return false;
      }
   }

   final class RemovalTask implements Runnable {
      final Node<K, V> node;

      RemovalTask(Node<K, V> node) {
         this.node = node;
      }

      @GuardedBy("evictionLock")
      @Override
      public void run() {
         if (!this.node.inWindow() || !BoundedLocalCache.this.evicts() && !BoundedLocalCache.this.expiresAfterAccess()) {
            if (BoundedLocalCache.this.evicts()) {
               if (this.node.inMainProbation()) {
                  BoundedLocalCache.this.accessOrderProbationDeque().remove(this.node);
               } else {
                  BoundedLocalCache.this.accessOrderProtectedDeque().remove(this.node);
               }
            }
         } else {
            BoundedLocalCache.this.accessOrderWindowDeque().remove(this.node);
         }

         if (BoundedLocalCache.this.expiresAfterWrite()) {
            BoundedLocalCache.this.writeOrderDeque().remove(this.node);
         } else if (BoundedLocalCache.this.expiresVariable()) {
            BoundedLocalCache.this.timerWheel().deschedule(this.node);
         }

         BoundedLocalCache.this.makeDead(this.node);
      }
   }

   static final class SizeLimiter<K, V> implements Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>> {
      private final int expectedSize;
      private final long limit;

      SizeLimiter(int expectedSize, long limit) {
         Caffeine.requireArgument(limit >= 0L);
         this.expectedSize = expectedSize;
         this.limit = limit;
      }

      public Map<K, V> apply(Stream<Policy.CacheEntry<K, V>> stream) {
         LinkedHashMap<K, V> map = new LinkedHashMap<>(Caffeine.calculateHashMapCapacity(this.expectedSize));
         stream.limit(this.limit).forEach(entry -> map.put(entry.getKey(), entry.getValue()));
         return Collections.unmodifiableMap(map);
      }
   }

   final class UpdateTask implements Runnable {
      final int weightDifference;
      final Node<K, V> node;

      public UpdateTask(Node<K, V> node, int weightDifference) {
         this.weightDifference = weightDifference;
         this.node = node;
      }

      @GuardedBy("evictionLock")
      @Override
      public void run() {
         if (BoundedLocalCache.this.expiresAfterWrite()) {
            BoundedLocalCache.reorder(BoundedLocalCache.this.writeOrderDeque(), this.node);
         } else if (BoundedLocalCache.this.expiresVariable()) {
            BoundedLocalCache.this.timerWheel().reschedule(this.node);
         }

         if (BoundedLocalCache.this.evicts()) {
            int oldWeightedSize = this.node.getPolicyWeight();
            this.node.setPolicyWeight(oldWeightedSize + this.weightDifference);
            if (this.node.inWindow()) {
               BoundedLocalCache.this.setWindowWeightedSize(BoundedLocalCache.this.windowWeightedSize() + this.weightDifference);
               if (this.node.getPolicyWeight() > BoundedLocalCache.this.maximum()) {
                  BoundedLocalCache.this.evictEntry(this.node, RemovalCause.SIZE, BoundedLocalCache.this.expirationTicker().read());
               } else if (this.node.getPolicyWeight() <= BoundedLocalCache.this.windowMaximum()) {
                  BoundedLocalCache.this.onAccess(this.node);
               } else if (BoundedLocalCache.this.accessOrderWindowDeque().contains(this.node)) {
                  BoundedLocalCache.this.accessOrderWindowDeque().moveToFront(this.node);
               }
            } else if (this.node.inMainProbation()) {
               if (this.node.getPolicyWeight() <= BoundedLocalCache.this.maximum()) {
                  BoundedLocalCache.this.onAccess(this.node);
               } else {
                  BoundedLocalCache.this.evictEntry(this.node, RemovalCause.SIZE, BoundedLocalCache.this.expirationTicker().read());
               }
            } else if (this.node.inMainProtected()) {
               BoundedLocalCache.this.setMainProtectedWeightedSize(BoundedLocalCache.this.mainProtectedWeightedSize() + this.weightDifference);
               if (this.node.getPolicyWeight() <= BoundedLocalCache.this.maximum()) {
                  BoundedLocalCache.this.onAccess(this.node);
               } else {
                  BoundedLocalCache.this.evictEntry(this.node, RemovalCause.SIZE, BoundedLocalCache.this.expirationTicker().read());
               }
            }

            BoundedLocalCache.this.setWeightedSize(BoundedLocalCache.this.weightedSize() + this.weightDifference);
            if (BoundedLocalCache.this.weightedSize() > 9223372034707292160L) {
               BoundedLocalCache.this.evictEntries();
            }
         } else if (BoundedLocalCache.this.expiresAfterAccess()) {
            BoundedLocalCache.this.onAccess(this.node);
         }
      }
   }

   static final class ValueIterator<K, V> implements Iterator<V> {
      final BoundedLocalCache.EntryIterator<K, V> iterator;

      ValueIterator(BoundedLocalCache<K, V> cache) {
         this.iterator = new BoundedLocalCache.EntryIterator<>(cache);
      }

      @Override
      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      @Override
      public V next() {
         return this.iterator.nextValue();
      }

      @Override
      public void remove() {
         this.iterator.remove();
      }
   }

   static final class ValueSpliterator<K, V> implements Spliterator<V> {
      final Spliterator<Node<K, V>> spliterator;
      final BoundedLocalCache<K, V> cache;

      ValueSpliterator(BoundedLocalCache<K, V> cache) {
         this(cache, cache.data.values().spliterator());
      }

      ValueSpliterator(BoundedLocalCache<K, V> cache, Spliterator<Node<K, V>> spliterator) {
         this.spliterator = Objects.requireNonNull(spliterator);
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public void forEachRemaining(Consumer<? super V> action) {
         Objects.requireNonNull(action);
         Consumer<Node<K, V>> consumer = node -> {
            K key = node.getKey();
            V value = node.getValue();
            long now = this.cache.expirationTicker().read();
            if (key != null && value != null && node.isAlive() && !this.cache.hasExpired(node, now)) {
               action.accept(value);
            }
         };
         this.spliterator.forEachRemaining(consumer);
      }

      @Override
      public boolean tryAdvance(Consumer<? super V> action) {
         boolean[] advanced = new boolean[]{false};
         long now = this.cache.expirationTicker().read();
         Consumer<Node<K, V>> consumer = node -> {
            K key = node.getKey();
            V value = node.getValue();
            if (key != null && value != null && !this.cache.hasExpired(node, now) && node.isAlive()) {
               action.accept(value);
               advanced[0] = true;
            }
         };

         while (this.spliterator.tryAdvance(consumer)) {
            if (advanced[0]) {
               return true;
            }
         }

         return false;
      }

      @Override
      public @Nullable Spliterator<V> trySplit() {
         Spliterator<Node<K, V>> split = this.spliterator.trySplit();
         return split == null ? null : new BoundedLocalCache.ValueSpliterator<>(this.cache, split);
      }

      @Override
      public long estimateSize() {
         return this.spliterator.estimateSize();
      }

      @Override
      public int characteristics() {
         return 4352;
      }
   }

   static final class ValuesView<K, V> extends AbstractCollection<V> {
      final BoundedLocalCache<K, V> cache;

      ValuesView(BoundedLocalCache<K, V> cache) {
         this.cache = Objects.requireNonNull(cache);
      }

      @Override
      public int size() {
         return this.cache.size();
      }

      @Override
      public void clear() {
         this.cache.clear();
      }

      @Override
      public boolean contains(Object o) {
         return this.cache.containsValue(o);
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         boolean modified = false;

         for (BoundedLocalCache.EntryIterator<K, V> iterator = new BoundedLocalCache.EntryIterator<>(this.cache); iterator.hasNext(); iterator.advance()) {
            K key = Objects.requireNonNull(iterator.key);
            V value = Objects.requireNonNull(iterator.value);
            if (collection.contains(value) && this.cache.remove(key, value)) {
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public boolean remove(Object o) {
         if (o == null) {
            return false;
         }

         BoundedLocalCache.EntryIterator<K, V> iterator = new BoundedLocalCache.EntryIterator<>(this.cache);

         while (iterator.hasNext()) {
            K key = Objects.requireNonNull(iterator.key);
            V value = Objects.requireNonNull(iterator.value);
            if (o.equals(value) && this.cache.remove(key, value)) {
               return true;
            }

            iterator.advance();
         }

         return false;
      }

      @Override
      public boolean removeIf(Predicate<? super V> filter) {
         boolean modified = false;

         for (BoundedLocalCache.EntryIterator<K, V> iterator = new BoundedLocalCache.EntryIterator<>(this.cache); iterator.hasNext(); iterator.advance()) {
            V value = Objects.requireNonNull(iterator.value);
            if (filter.test(value)) {
               K key = Objects.requireNonNull(iterator.key);
               modified |= this.cache.remove(key, value);
            }
         }

         return modified;
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         boolean modified = false;

         for (BoundedLocalCache.EntryIterator<K, V> iterator = new BoundedLocalCache.EntryIterator<>(this.cache); iterator.hasNext(); iterator.advance()) {
            K key = Objects.requireNonNull(iterator.key);
            V value = Objects.requireNonNull(iterator.value);
            if (!collection.contains(value) && this.cache.remove(key, value)) {
               modified = true;
            }
         }

         return modified;
      }

      @Override
      public Iterator<V> iterator() {
         return new BoundedLocalCache.ValueIterator<>(this.cache);
      }

      @Override
      public Spliterator<V> spliterator() {
         return new BoundedLocalCache.ValueSpliterator<>(this.cache);
      }
   }

   static final class WeightLimiter<K, V> implements Function<Stream<Policy.CacheEntry<K, V>>, Map<K, V>> {
      private final long weightLimit;
      private long weightedSize;

      WeightLimiter(long weightLimit) {
         Caffeine.requireArgument(weightLimit >= 0L);
         this.weightLimit = weightLimit;
      }

      public Map<K, V> apply(Stream<Policy.CacheEntry<K, V>> stream) {
         LinkedHashMap<K, V> map = new LinkedHashMap<>();
         stream.takeWhile(entry -> {
            this.weightedSize = Math.addExact(this.weightedSize, entry.weight());
            return this.weightedSize <= this.weightLimit;
         }).forEach(entry -> map.put(entry.getKey(), entry.getValue()));
         return Collections.unmodifiableMap(map);
      }
   }
}
