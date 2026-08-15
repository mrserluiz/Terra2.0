package com.dfsek.terra.lib.commons.lang3.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class StopWatch {
   private static final long NANO_2_MILLIS = 1000000L;
   private final String message;
   private StopWatch.State runningState = StopWatch.State.UNSTARTED;
   private StopWatch.SplitState splitState = StopWatch.SplitState.UNSPLIT;
   private long startTimeNanos;
   private Instant startInstant;
   private Instant stopInstant;
   private long stopTimeNanos;

   public static StopWatch create() {
      return new StopWatch();
   }

   public static StopWatch createStarted() {
      StopWatch sw = new StopWatch();
      sw.start();
      return sw;
   }

   public StopWatch() {
      this(null);
   }

   public StopWatch(String message) {
      this.message = message;
   }

   public String formatSplitTime() {
      return DurationFormatUtils.formatDurationHMS(this.getSplitDuration().toMillis());
   }

   public String formatTime() {
      return DurationFormatUtils.formatDurationHMS(this.getTime());
   }

   public Duration getDuration() {
      return Duration.ofNanos(this.getNanoTime());
   }

   public String getMessage() {
      return this.message;
   }

   public long getNanoTime() {
      if (this.runningState == StopWatch.State.STOPPED || this.runningState == StopWatch.State.SUSPENDED) {
         return this.stopTimeNanos - this.startTimeNanos;
      } else if (this.runningState == StopWatch.State.UNSTARTED) {
         return 0L;
      } else if (this.runningState == StopWatch.State.RUNNING) {
         return System.nanoTime() - this.startTimeNanos;
      } else {
         throw new IllegalStateException("Illegal running state has occurred.");
      }
   }

   public Duration getSplitDuration() {
      return Duration.ofNanos(this.getSplitNanoTime());
   }

   public long getSplitNanoTime() {
      if (this.splitState != StopWatch.SplitState.SPLIT) {
         throw new IllegalStateException("Stopwatch must be split to get the split time.");
      } else {
         return this.stopTimeNanos - this.startTimeNanos;
      }
   }

   @Deprecated
   public long getSplitTime() {
      return this.nanosToMillis(this.getSplitNanoTime());
   }

   public Instant getStartInstant() {
      return Instant.ofEpochMilli(this.getStartTime());
   }

   @Deprecated
   public long getStartTime() {
      if (this.runningState == StopWatch.State.UNSTARTED) {
         throw new IllegalStateException("Stopwatch has not been started");
      } else {
         return this.startInstant.toEpochMilli();
      }
   }

   public Instant getStopInstant() {
      return Instant.ofEpochMilli(this.getStopTime());
   }

   @Deprecated
   public long getStopTime() {
      if (this.runningState == StopWatch.State.UNSTARTED) {
         throw new IllegalStateException("Stopwatch has not been started");
      } else {
         return this.stopInstant.toEpochMilli();
      }
   }

   @Deprecated
   public long getTime() {
      return this.nanosToMillis(this.getNanoTime());
   }

   public long getTime(TimeUnit timeUnit) {
      return timeUnit.convert(this.getNanoTime(), TimeUnit.NANOSECONDS);
   }

   public boolean isStarted() {
      return this.runningState.isStarted();
   }

   public boolean isStopped() {
      return this.runningState.isStopped();
   }

   public boolean isSuspended() {
      return this.runningState.isSuspended();
   }

   private long nanosToMillis(long nanos) {
      return nanos / 1000000L;
   }

   public void reset() {
      this.runningState = StopWatch.State.UNSTARTED;
      this.splitState = StopWatch.SplitState.UNSPLIT;
   }

   public void resume() {
      if (this.runningState != StopWatch.State.SUSPENDED) {
         throw new IllegalStateException("Stopwatch must be suspended to resume. ");
      }

      this.startTimeNanos = this.startTimeNanos + (System.nanoTime() - this.stopTimeNanos);
      this.runningState = StopWatch.State.RUNNING;
   }

   public void split() {
      if (this.runningState != StopWatch.State.RUNNING) {
         throw new IllegalStateException("Stopwatch is not running. ");
      }

      this.stopTimeNanos = System.nanoTime();
      this.splitState = StopWatch.SplitState.SPLIT;
   }

   public void start() {
      if (this.runningState == StopWatch.State.STOPPED) {
         throw new IllegalStateException("Stopwatch must be reset before being restarted. ");
      }

      if (this.runningState != StopWatch.State.UNSTARTED) {
         throw new IllegalStateException("Stopwatch already started. ");
      }

      this.startTimeNanos = System.nanoTime();
      this.startInstant = Instant.now();
      this.runningState = StopWatch.State.RUNNING;
   }

   public void stop() {
      if (this.runningState != StopWatch.State.RUNNING && this.runningState != StopWatch.State.SUSPENDED) {
         throw new IllegalStateException("Stopwatch is not running. ");
      }

      if (this.runningState == StopWatch.State.RUNNING) {
         this.stopTimeNanos = System.nanoTime();
         this.stopInstant = Instant.now();
      }

      this.runningState = StopWatch.State.STOPPED;
   }

   public void suspend() {
      if (this.runningState != StopWatch.State.RUNNING) {
         throw new IllegalStateException("Stopwatch must be running to suspend. ");
      }

      this.stopTimeNanos = System.nanoTime();
      this.stopInstant = Instant.now();
      this.runningState = StopWatch.State.SUSPENDED;
   }

   public String toSplitString() {
      String msgStr = Objects.toString(this.message, "");
      String formattedTime = this.formatSplitTime();
      return msgStr.isEmpty() ? formattedTime : msgStr + " " + formattedTime;
   }

   @Override
   public String toString() {
      String msgStr = Objects.toString(this.message, "");
      String formattedTime = this.formatTime();
      return msgStr.isEmpty() ? formattedTime : msgStr + " " + formattedTime;
   }

   public void unsplit() {
      if (this.splitState != StopWatch.SplitState.SPLIT) {
         throw new IllegalStateException("Stopwatch has not been split. ");
      }

      this.splitState = StopWatch.SplitState.UNSPLIT;
   }

   private enum SplitState {
      SPLIT,
      UNSPLIT;
   }

   private enum State {
      RUNNING {
         @Override
         boolean isStarted() {
            return true;
         }

         @Override
         boolean isStopped() {
            return false;
         }

         @Override
         boolean isSuspended() {
            return false;
         }
      },
      STOPPED {
         @Override
         boolean isStarted() {
            return false;
         }

         @Override
         boolean isStopped() {
            return true;
         }

         @Override
         boolean isSuspended() {
            return false;
         }
      },
      SUSPENDED {
         @Override
         boolean isStarted() {
            return true;
         }

         @Override
         boolean isStopped() {
            return false;
         }

         @Override
         boolean isSuspended() {
            return true;
         }
      },
      UNSTARTED {
         @Override
         boolean isStarted() {
            return false;
         }

         @Override
         boolean isStopped() {
            return true;
         }

         @Override
         boolean isSuspended() {
            return false;
         }
      };

      State() {
      }

      abstract boolean isStarted();

      abstract boolean isStopped();

      abstract boolean isSuspended();
   }
}
