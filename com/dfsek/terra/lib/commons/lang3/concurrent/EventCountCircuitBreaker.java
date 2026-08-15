package com.dfsek.terra.lib.commons.lang3.concurrent;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class EventCountCircuitBreaker extends AbstractCircuitBreaker<Integer> {
   private static final Map<AbstractCircuitBreaker.State, EventCountCircuitBreaker.StateStrategy> STRATEGY_MAP = createStrategyMap();
   private final AtomicReference<EventCountCircuitBreaker.CheckIntervalData> checkIntervalData = new AtomicReference<>(
      new EventCountCircuitBreaker.CheckIntervalData(0, 0L)
   );
   private final int openingThreshold;
   private final long openingInterval;
   private final int closingThreshold;
   private final long closingInterval;

   private static Map<AbstractCircuitBreaker.State, EventCountCircuitBreaker.StateStrategy> createStrategyMap() {
      Map<AbstractCircuitBreaker.State, EventCountCircuitBreaker.StateStrategy> map = new EnumMap<>(AbstractCircuitBreaker.State.class);
      map.put(AbstractCircuitBreaker.State.CLOSED, new EventCountCircuitBreaker.StateStrategyClosed());
      map.put(AbstractCircuitBreaker.State.OPEN, new EventCountCircuitBreaker.StateStrategyOpen());
      return map;
   }

   private static EventCountCircuitBreaker.StateStrategy stateStrategy(AbstractCircuitBreaker.State state) {
      return STRATEGY_MAP.get(state);
   }

   public EventCountCircuitBreaker(int threshold, long checkInterval, TimeUnit checkUnit) {
      this(threshold, checkInterval, checkUnit, threshold);
   }

   public EventCountCircuitBreaker(int openingThreshold, long checkInterval, TimeUnit checkUnit, int closingThreshold) {
      this(openingThreshold, checkInterval, checkUnit, closingThreshold, checkInterval, checkUnit);
   }

   public EventCountCircuitBreaker(
      int openingThreshold, long openingInterval, TimeUnit openingUnit, int closingThreshold, long closingInterval, TimeUnit closingUnit
   ) {
      this.openingThreshold = openingThreshold;
      this.openingInterval = openingUnit.toNanos(openingInterval);
      this.closingThreshold = closingThreshold;
      this.closingInterval = closingUnit.toNanos(closingInterval);
   }

   private void changeStateAndStartNewCheckInterval(AbstractCircuitBreaker.State newState) {
      this.changeState(newState);
      this.checkIntervalData.set(new EventCountCircuitBreaker.CheckIntervalData(0, this.nanoTime()));
   }

   @Override
   public boolean checkState() {
      return this.performStateCheck(0);
   }

   @Override
   public void close() {
      super.close();
      this.checkIntervalData.set(new EventCountCircuitBreaker.CheckIntervalData(0, this.nanoTime()));
   }

   public long getClosingInterval() {
      return this.closingInterval;
   }

   public int getClosingThreshold() {
      return this.closingThreshold;
   }

   public long getOpeningInterval() {
      return this.openingInterval;
   }

   public int getOpeningThreshold() {
      return this.openingThreshold;
   }

   public boolean incrementAndCheckState() {
      return this.incrementAndCheckState(1);
   }

   public boolean incrementAndCheckState(Integer increment) {
      return this.performStateCheck(increment);
   }

   long nanoTime() {
      return System.nanoTime();
   }

   private EventCountCircuitBreaker.CheckIntervalData nextCheckIntervalData(
      int increment, EventCountCircuitBreaker.CheckIntervalData currentData, AbstractCircuitBreaker.State currentState, long time
   ) {
      EventCountCircuitBreaker.CheckIntervalData nextData;
      if (stateStrategy(currentState).isCheckIntervalFinished(this, currentData, time)) {
         nextData = new EventCountCircuitBreaker.CheckIntervalData(increment, time);
      } else {
         nextData = currentData.increment(increment);
      }

      return nextData;
   }

   @Override
   public void open() {
      super.open();
      this.checkIntervalData.set(new EventCountCircuitBreaker.CheckIntervalData(0, this.nanoTime()));
   }

   private boolean performStateCheck(int increment) {
      EventCountCircuitBreaker.CheckIntervalData currentData;
      EventCountCircuitBreaker.CheckIntervalData nextData;
      AbstractCircuitBreaker.State currentState;
      do {
         long time = this.nanoTime();
         currentState = this.state.get();
         currentData = this.checkIntervalData.get();
         nextData = this.nextCheckIntervalData(increment, currentData, currentState, time);
      } while (!this.updateCheckIntervalData(currentData, nextData));

      if (stateStrategy(currentState).isStateTransition(this, currentData, nextData)) {
         currentState = currentState.oppositeState();
         this.changeStateAndStartNewCheckInterval(currentState);
      }

      return !isOpen(currentState);
   }

   private boolean updateCheckIntervalData(EventCountCircuitBreaker.CheckIntervalData currentData, EventCountCircuitBreaker.CheckIntervalData nextData) {
      return currentData == nextData || this.checkIntervalData.compareAndSet(currentData, nextData);
   }

   private static final class CheckIntervalData {
      private final int eventCount;
      private final long checkIntervalStart;

      CheckIntervalData(int count, long intervalStart) {
         this.eventCount = count;
         this.checkIntervalStart = intervalStart;
      }

      public long getCheckIntervalStart() {
         return this.checkIntervalStart;
      }

      public int getEventCount() {
         return this.eventCount;
      }

      public EventCountCircuitBreaker.CheckIntervalData increment(int delta) {
         return delta == 0 ? this : new EventCountCircuitBreaker.CheckIntervalData(this.getEventCount() + delta, this.getCheckIntervalStart());
      }
   }

   private abstract static class StateStrategy {
      private StateStrategy() {
      }

      protected abstract long fetchCheckInterval(EventCountCircuitBreaker var1);

      public boolean isCheckIntervalFinished(EventCountCircuitBreaker breaker, EventCountCircuitBreaker.CheckIntervalData currentData, long now) {
         return now - currentData.getCheckIntervalStart() > this.fetchCheckInterval(breaker);
      }

      public abstract boolean isStateTransition(
         EventCountCircuitBreaker var1, EventCountCircuitBreaker.CheckIntervalData var2, EventCountCircuitBreaker.CheckIntervalData var3
      );
   }

   private static final class StateStrategyClosed extends EventCountCircuitBreaker.StateStrategy {
      private StateStrategyClosed() {
      }

      @Override
      protected long fetchCheckInterval(EventCountCircuitBreaker breaker) {
         return breaker.getOpeningInterval();
      }

      @Override
      public boolean isStateTransition(
         EventCountCircuitBreaker breaker, EventCountCircuitBreaker.CheckIntervalData currentData, EventCountCircuitBreaker.CheckIntervalData nextData
      ) {
         return nextData.getEventCount() > breaker.getOpeningThreshold();
      }
   }

   private static final class StateStrategyOpen extends EventCountCircuitBreaker.StateStrategy {
      private StateStrategyOpen() {
      }

      @Override
      protected long fetchCheckInterval(EventCountCircuitBreaker breaker) {
         return breaker.getClosingInterval();
      }

      @Override
      public boolean isStateTransition(
         EventCountCircuitBreaker breaker, EventCountCircuitBreaker.CheckIntervalData currentData, EventCountCircuitBreaker.CheckIntervalData nextData
      ) {
         return nextData.getCheckIntervalStart() != currentData.getCheckIntervalStart() && currentData.getEventCount() < breaker.getClosingThreshold();
      }
   }
}
