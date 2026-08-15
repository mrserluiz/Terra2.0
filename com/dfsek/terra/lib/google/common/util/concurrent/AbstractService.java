package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.ForOverride;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
@J2ktIncompatible
public abstract class AbstractService implements Service {
   private static final ListenerCallQueue.Event<Service.Listener> STARTING_EVENT = new ListenerCallQueue.Event<Service.Listener>() {
      public void call(Service.Listener listener) {
         listener.starting();
      }

      @Override
      public String toString() {
         return "starting()";
      }
   };
   private static final ListenerCallQueue.Event<Service.Listener> RUNNING_EVENT = new ListenerCallQueue.Event<Service.Listener>() {
      public void call(Service.Listener listener) {
         listener.running();
      }

      @Override
      public String toString() {
         return "running()";
      }
   };
   private static final ListenerCallQueue.Event<Service.Listener> STOPPING_FROM_STARTING_EVENT = stoppingEvent(Service.State.STARTING);
   private static final ListenerCallQueue.Event<Service.Listener> STOPPING_FROM_RUNNING_EVENT = stoppingEvent(Service.State.RUNNING);
   private static final ListenerCallQueue.Event<Service.Listener> TERMINATED_FROM_NEW_EVENT = terminatedEvent(Service.State.NEW);
   private static final ListenerCallQueue.Event<Service.Listener> TERMINATED_FROM_STARTING_EVENT = terminatedEvent(Service.State.STARTING);
   private static final ListenerCallQueue.Event<Service.Listener> TERMINATED_FROM_RUNNING_EVENT = terminatedEvent(Service.State.RUNNING);
   private static final ListenerCallQueue.Event<Service.Listener> TERMINATED_FROM_STOPPING_EVENT = terminatedEvent(Service.State.STOPPING);
   private final Monitor monitor = new Monitor();
   private final Monitor.Guard isStartable = new AbstractService.IsStartableGuard();
   private final Monitor.Guard isStoppable = new AbstractService.IsStoppableGuard();
   private final Monitor.Guard hasReachedRunning = new AbstractService.HasReachedRunningGuard();
   private final Monitor.Guard isStopped = new AbstractService.IsStoppedGuard();
   private final ListenerCallQueue<Service.Listener> listeners = new ListenerCallQueue<>();
   private volatile AbstractService.StateSnapshot snapshot = new AbstractService.StateSnapshot(Service.State.NEW);

   private static ListenerCallQueue.Event<Service.Listener> terminatedEvent(Service.State from) {
      return new ListenerCallQueue.Event<Service.Listener>() {
         public void call(Service.Listener listener) {
            listener.terminated(from);
         }

         @Override
         public String toString() {
            return "terminated({from = " + from + "})";
         }
      };
   }

   private static ListenerCallQueue.Event<Service.Listener> stoppingEvent(Service.State from) {
      return new ListenerCallQueue.Event<Service.Listener>() {
         public void call(Service.Listener listener) {
            listener.stopping(from);
         }

         @Override
         public String toString() {
            return "stopping({from = " + from + "})";
         }
      };
   }

   protected AbstractService() {
   }

   @ForOverride
   protected abstract void doStart();

   @ForOverride
   protected abstract void doStop();

   @ForOverride
   protected void doCancelStart() {
   }

   @CanIgnoreReturnValue
   @Override
   public final Service startAsync() {
      if (this.monitor.enterIf(this.isStartable)) {
         try {
            this.snapshot = new AbstractService.StateSnapshot(Service.State.STARTING);
            this.enqueueStartingEvent();
            this.doStart();
         } catch (Throwable startupFailure) {
            Platform.restoreInterruptIfIsInterruptedException(startupFailure);
            this.notifyFailed(startupFailure);
         } finally {
            this.monitor.leave();
            this.dispatchListenerEvents();
         }

         return this;
      } else {
         throw new IllegalStateException("Service " + this + " has already been started");
      }
   }

   @CanIgnoreReturnValue
   @Override
   public final Service stopAsync() {
      if (this.monitor.enterIf(this.isStoppable)) {
         try {
            Service.State previous = this.state();
            switch (previous) {
               case NEW:
                  this.snapshot = new AbstractService.StateSnapshot(Service.State.TERMINATED);
                  this.enqueueTerminatedEvent(Service.State.NEW);
                  break;
               case STARTING:
                  this.snapshot = new AbstractService.StateSnapshot(Service.State.STARTING, true, null);
                  this.enqueueStoppingEvent(Service.State.STARTING);
                  this.doCancelStart();
                  break;
               case RUNNING:
                  this.snapshot = new AbstractService.StateSnapshot(Service.State.STOPPING);
                  this.enqueueStoppingEvent(Service.State.RUNNING);
                  this.doStop();
                  break;
               case STOPPING:
               case TERMINATED:
               case FAILED:
                  throw new AssertionError("isStoppable is incorrectly implemented, saw: " + previous);
            }
         } catch (Throwable shutdownFailure) {
            Platform.restoreInterruptIfIsInterruptedException(shutdownFailure);
            this.notifyFailed(shutdownFailure);
         } finally {
            this.monitor.leave();
            this.dispatchListenerEvents();
         }
      }

      return this;
   }

   @Override
   public final void awaitRunning() {
      this.monitor.enterWhenUninterruptibly(this.hasReachedRunning);

      try {
         this.checkCurrentState(Service.State.RUNNING);
      } finally {
         this.monitor.leave();
      }
   }

   @Override
   public final void awaitRunning(Duration timeout) throws TimeoutException {
      Service.super.awaitRunning(timeout);
   }

   @Override
   public final void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException {
      if (this.monitor.enterWhenUninterruptibly(this.hasReachedRunning, timeout, unit)) {
         try {
            this.checkCurrentState(Service.State.RUNNING);
         } finally {
            this.monitor.leave();
         }
      } else {
         throw new TimeoutException("Timed out waiting for " + this + " to reach the RUNNING state.");
      }
   }

   @Override
   public final void awaitTerminated() {
      this.monitor.enterWhenUninterruptibly(this.isStopped);

      try {
         this.checkCurrentState(Service.State.TERMINATED);
      } finally {
         this.monitor.leave();
      }
   }

   @Override
   public final void awaitTerminated(Duration timeout) throws TimeoutException {
      Service.super.awaitTerminated(timeout);
   }

   @Override
   public final void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException {
      if (this.monitor.enterWhenUninterruptibly(this.isStopped, timeout, unit)) {
         try {
            this.checkCurrentState(Service.State.TERMINATED);
         } finally {
            this.monitor.leave();
         }
      } else {
         throw new TimeoutException("Timed out waiting for " + this + " to reach a terminal state. Current state: " + this.state());
      }
   }

   @GuardedBy("monitor")
   private void checkCurrentState(Service.State expected) {
      Service.State actual = this.state();
      if (actual != expected) {
         if (actual == Service.State.FAILED) {
            throw new IllegalStateException("Expected the service " + this + " to be " + expected + ", but the service has FAILED", this.failureCause());
         } else {
            throw new IllegalStateException("Expected the service " + this + " to be " + expected + ", but was " + actual);
         }
      }
   }

   protected final void notifyStarted() {
      this.monitor.enter();

      try {
         if (this.snapshot.state != Service.State.STARTING) {
            IllegalStateException failure = new IllegalStateException("Cannot notifyStarted() when the service is " + this.snapshot.state);
            this.notifyFailed(failure);
            throw failure;
         }

         if (this.snapshot.shutdownWhenStartupFinishes) {
            this.snapshot = new AbstractService.StateSnapshot(Service.State.STOPPING);
            this.doStop();
         } else {
            this.snapshot = new AbstractService.StateSnapshot(Service.State.RUNNING);
            this.enqueueRunningEvent();
         }
      } finally {
         this.monitor.leave();
         this.dispatchListenerEvents();
      }
   }

   protected final void notifyStopped() {
      this.monitor.enter();

      try {
         Service.State previous = this.state();
         switch (previous) {
            case NEW:
            case TERMINATED:
            case FAILED:
               throw new IllegalStateException("Cannot notifyStopped() when the service is " + previous);
            case STARTING:
            case RUNNING:
            case STOPPING:
               this.snapshot = new AbstractService.StateSnapshot(Service.State.TERMINATED);
               this.enqueueTerminatedEvent(previous);
         }
      } finally {
         this.monitor.leave();
         this.dispatchListenerEvents();
      }
   }

   protected final void notifyFailed(Throwable cause) {
      Preconditions.checkNotNull(cause);
      this.monitor.enter();

      try {
         Service.State previous = this.state();
         switch (previous) {
            case NEW:
            case TERMINATED:
               throw new IllegalStateException("Failed while in state:" + previous, cause);
            case STARTING:
            case RUNNING:
            case STOPPING:
               this.snapshot = new AbstractService.StateSnapshot(Service.State.FAILED, false, cause);
               this.enqueueFailedEvent(previous, cause);
            case FAILED:
         }
      } finally {
         this.monitor.leave();
         this.dispatchListenerEvents();
      }
   }

   @Override
   public final boolean isRunning() {
      return this.state() == Service.State.RUNNING;
   }

   @Override
   public final Service.State state() {
      return this.snapshot.externalState();
   }

   @Override
   public final Throwable failureCause() {
      return this.snapshot.failureCause();
   }

   @Override
   public final void addListener(Service.Listener listener, Executor executor) {
      this.listeners.addListener(listener, executor);
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + " [" + this.state() + "]";
   }

   private void dispatchListenerEvents() {
      if (!this.monitor.isOccupiedByCurrentThread()) {
         this.listeners.dispatch();
      }
   }

   private void enqueueStartingEvent() {
      this.listeners.enqueue(STARTING_EVENT);
   }

   private void enqueueRunningEvent() {
      this.listeners.enqueue(RUNNING_EVENT);
   }

   private void enqueueStoppingEvent(Service.State from) {
      if (from == Service.State.STARTING) {
         this.listeners.enqueue(STOPPING_FROM_STARTING_EVENT);
      } else {
         if (from != Service.State.RUNNING) {
            throw new AssertionError();
         }

         this.listeners.enqueue(STOPPING_FROM_RUNNING_EVENT);
      }
   }

   private void enqueueTerminatedEvent(Service.State from) {
      switch (from) {
         case NEW:
            this.listeners.enqueue(TERMINATED_FROM_NEW_EVENT);
            break;
         case STARTING:
            this.listeners.enqueue(TERMINATED_FROM_STARTING_EVENT);
            break;
         case RUNNING:
            this.listeners.enqueue(TERMINATED_FROM_RUNNING_EVENT);
            break;
         case STOPPING:
            this.listeners.enqueue(TERMINATED_FROM_STOPPING_EVENT);
            break;
         case TERMINATED:
         case FAILED:
            throw new AssertionError();
      }
   }

   private void enqueueFailedEvent(Service.State from, Throwable cause) {
      this.listeners.enqueue(new ListenerCallQueue.Event<Service.Listener>() {
         public void call(Service.Listener listener) {
            listener.failed(from, cause);
         }

         @Override
         public String toString() {
            return "failed({from = " + from + ", cause = " + cause + "})";
         }
      });
   }

   private final class HasReachedRunningGuard extends Monitor.Guard {
      HasReachedRunningGuard() {
         super(AbstractService.this.monitor);
      }

      @Override
      public boolean isSatisfied() {
         return AbstractService.this.state().compareTo(Service.State.RUNNING) >= 0;
      }
   }

   private final class IsStartableGuard extends Monitor.Guard {
      IsStartableGuard() {
         super(AbstractService.this.monitor);
      }

      @Override
      public boolean isSatisfied() {
         return AbstractService.this.state() == Service.State.NEW;
      }
   }

   private final class IsStoppableGuard extends Monitor.Guard {
      IsStoppableGuard() {
         super(AbstractService.this.monitor);
      }

      @Override
      public boolean isSatisfied() {
         return AbstractService.this.state().compareTo(Service.State.RUNNING) <= 0;
      }
   }

   private final class IsStoppedGuard extends Monitor.Guard {
      IsStoppedGuard() {
         super(AbstractService.this.monitor);
      }

      @Override
      public boolean isSatisfied() {
         return AbstractService.this.state().compareTo(Service.State.TERMINATED) >= 0;
      }
   }

   private static final class StateSnapshot {
      final Service.State state;
      final boolean shutdownWhenStartupFinishes;
      final @Nullable Throwable failure;

      StateSnapshot(Service.State internalState) {
         this(internalState, false, null);
      }

      StateSnapshot(Service.State internalState, boolean shutdownWhenStartupFinishes, @Nullable Throwable failure) {
         Preconditions.checkArgument(
            !shutdownWhenStartupFinishes || internalState == Service.State.STARTING,
            "shutdownWhenStartupFinishes can only be set if state is STARTING. Got %s instead.",
            internalState
         );
         Preconditions.checkArgument(
            failure != null == (internalState == Service.State.FAILED),
            "A failure cause should be set if and only if the state is failed.  Got %s and %s instead.",
            internalState,
            failure
         );
         this.state = internalState;
         this.shutdownWhenStartupFinishes = shutdownWhenStartupFinishes;
         this.failure = failure;
      }

      Service.State externalState() {
         return this.shutdownWhenStartupFinishes && this.state == Service.State.STARTING ? Service.State.STOPPING : this.state;
      }

      Throwable failureCause() {
         Preconditions.checkState(this.state == Service.State.FAILED, "failureCause() is only valid if the service has failed, service is %s", this.state);
         return Objects.requireNonNull(this.failure);
      }
   }
}
