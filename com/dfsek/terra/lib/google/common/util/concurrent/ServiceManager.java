package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.MoreObjects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Predicates;
import com.dfsek.terra.lib.google.common.base.Stopwatch;
import com.dfsek.terra.lib.google.common.collect.Collections2;
import com.dfsek.terra.lib.google.common.collect.ImmutableCollection;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.dfsek.terra.lib.google.common.collect.ImmutableSet;
import com.dfsek.terra.lib.google.common.collect.ImmutableSetMultimap;
import com.dfsek.terra.lib.google.common.collect.Lists;
import com.dfsek.terra.lib.google.common.collect.Maps;
import com.dfsek.terra.lib.google.common.collect.MultimapBuilder;
import com.dfsek.terra.lib.google.common.collect.Multimaps;
import com.dfsek.terra.lib.google.common.collect.Multiset;
import com.dfsek.terra.lib.google.common.collect.Ordering;
import com.dfsek.terra.lib.google.common.collect.SetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@J2ktIncompatible
@GwtIncompatible
public final class ServiceManager implements ServiceManagerBridge {
   private static final LazyLogger logger = new LazyLogger(ServiceManager.class);
   private static final ListenerCallQueue.Event<ServiceManager.Listener> HEALTHY_EVENT = new ListenerCallQueue.Event<ServiceManager.Listener>() {
      public void call(ServiceManager.Listener listener) {
         listener.healthy();
      }

      @Override
      public String toString() {
         return "healthy()";
      }
   };
   private static final ListenerCallQueue.Event<ServiceManager.Listener> STOPPED_EVENT = new ListenerCallQueue.Event<ServiceManager.Listener>() {
      public void call(ServiceManager.Listener listener) {
         listener.stopped();
      }

      @Override
      public String toString() {
         return "stopped()";
      }
   };
   private final ServiceManager.ServiceManagerState state;
   private final ImmutableList<Service> services;

   public ServiceManager(Iterable<? extends Service> services) {
      ImmutableList<Service> copy = ImmutableList.copyOf(services);
      if (copy.isEmpty()) {
         logger.get()
            .log(
               Level.WARNING,
               "ServiceManager configured with no services.  Is your application configured properly?",
               new ServiceManager.EmptyServiceManagerWarning()
            );
         copy = ImmutableList.of(new ServiceManager.NoOpService());
      }

      this.state = new ServiceManager.ServiceManagerState(copy);
      this.services = copy;
      WeakReference<ServiceManager.ServiceManagerState> stateReference = new WeakReference<>(this.state);

      for (Service service : copy) {
         service.addListener(new ServiceManager.ServiceListener(service, stateReference), MoreExecutors.directExecutor());
         Preconditions.checkArgument(service.state() == Service.State.NEW, "Can only manage NEW services, %s", service);
      }

      this.state.markReady();
   }

   public void addListener(ServiceManager.Listener listener, Executor executor) {
      this.state.addListener(listener, executor);
   }

   @CanIgnoreReturnValue
   public ServiceManager startAsync() {
      for (Service service : this.services) {
         Preconditions.checkState(service.state() == Service.State.NEW, "Not all services are NEW, cannot start %s", this);
      }

      for (Service service : this.services) {
         try {
            this.state.tryStartTiming(service);
            service.startAsync();
         } catch (IllegalStateException e) {
            logger.get().log(Level.WARNING, "Unable to start Service " + service, e);
         }
      }

      return this;
   }

   public void awaitHealthy() {
      this.state.awaitHealthy();
   }

   public void awaitHealthy(Duration timeout) throws TimeoutException {
      this.awaitHealthy(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   public void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
      this.state.awaitHealthy(timeout, unit);
   }

   @CanIgnoreReturnValue
   public ServiceManager stopAsync() {
      for (Service service : this.services) {
         service.stopAsync();
      }

      return this;
   }

   public void awaitStopped() {
      this.state.awaitStopped();
   }

   public void awaitStopped(Duration timeout) throws TimeoutException {
      this.awaitStopped(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   public void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
      this.state.awaitStopped(timeout, unit);
   }

   public boolean isHealthy() {
      for (Service service : this.services) {
         if (!service.isRunning()) {
            return false;
         }
      }

      return true;
   }

   public ImmutableSetMultimap<Service.State, Service> servicesByState() {
      return this.state.servicesByState();
   }

   public ImmutableMap<Service, Long> startupTimes() {
      return this.state.startupTimes();
   }

   public ImmutableMap<Service, Duration> startupDurations() {
      return ImmutableMap.copyOf(Maps.transformValues(this.startupTimes(), Duration::ofMillis));
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(ServiceManager.class)
         .add("services", Collections2.filter(this.services, Predicates.not(Predicates.instanceOf(ServiceManager.NoOpService.class))))
         .toString();
   }

   private static final class EmptyServiceManagerWarning extends Throwable {
      private EmptyServiceManagerWarning() {
      }
   }

   private static final class FailedService extends Throwable {
      FailedService(Service service) {
         super(service.toString(), service.failureCause(), false, false);
      }
   }

   public abstract static class Listener {
      public void healthy() {
      }

      public void stopped() {
      }

      public void failure(Service service) {
      }
   }

   private static final class NoOpService extends AbstractService {
      private NoOpService() {
      }

      @Override
      protected void doStart() {
         this.notifyStarted();
      }

      @Override
      protected void doStop() {
         this.notifyStopped();
      }
   }

   private static final class ServiceListener extends Service.Listener {
      final Service service;
      final WeakReference<ServiceManager.ServiceManagerState> state;

      ServiceListener(Service service, WeakReference<ServiceManager.ServiceManagerState> state) {
         this.service = service;
         this.state = state;
      }

      @Override
      public void starting() {
         ServiceManager.ServiceManagerState state = this.state.get();
         if (state != null) {
            state.transitionService(this.service, Service.State.NEW, Service.State.STARTING);
            if (!(this.service instanceof ServiceManager.NoOpService)) {
               ServiceManager.logger.get().log(Level.FINE, "Starting {0}.", this.service);
            }
         }
      }

      @Override
      public void running() {
         ServiceManager.ServiceManagerState state = this.state.get();
         if (state != null) {
            state.transitionService(this.service, Service.State.STARTING, Service.State.RUNNING);
         }
      }

      @Override
      public void stopping(Service.State from) {
         ServiceManager.ServiceManagerState state = this.state.get();
         if (state != null) {
            state.transitionService(this.service, from, Service.State.STOPPING);
         }
      }

      @Override
      public void terminated(Service.State from) {
         ServiceManager.ServiceManagerState state = this.state.get();
         if (state != null) {
            if (!(this.service instanceof ServiceManager.NoOpService)) {
               ServiceManager.logger.get().log(Level.FINE, "Service {0} has terminated. Previous state was: {1}", new Object[]{this.service, from});
            }

            state.transitionService(this.service, from, Service.State.TERMINATED);
         }
      }

      @Override
      public void failed(Service.State from, Throwable failure) {
         ServiceManager.ServiceManagerState state = this.state.get();
         if (state != null) {
            boolean log = !(this.service instanceof ServiceManager.NoOpService);
            log &= from != Service.State.STARTING;
            if (log) {
               ServiceManager.logger.get().log(Level.SEVERE, "Service " + this.service + " has failed in the " + from + " state.", failure);
            }

            state.transitionService(this.service, from, Service.State.FAILED);
         }
      }
   }

   private static final class ServiceManagerState {
      final Monitor monitor = new Monitor();
      @GuardedBy("monitor")
      final SetMultimap<Service.State, Service> servicesByState = MultimapBuilder.enumKeys(Service.State.class).linkedHashSetValues().build();
      @GuardedBy("monitor")
      final Multiset<Service.State> states = this.servicesByState.keys();
      @GuardedBy("monitor")
      final IdentityHashMap<Service, Stopwatch> startupTimers = new IdentityHashMap<>();
      @GuardedBy("monitor")
      boolean ready;
      @GuardedBy("monitor")
      boolean transitioned;
      final int numberOfServices;
      final Monitor.Guard awaitHealthGuard = new ServiceManager.ServiceManagerState.AwaitHealthGuard();
      final Monitor.Guard stoppedGuard = new ServiceManager.ServiceManagerState.StoppedGuard();
      final ListenerCallQueue<ServiceManager.Listener> listeners = new ListenerCallQueue<>();

      ServiceManagerState(ImmutableCollection<Service> services) {
         this.numberOfServices = services.size();
         this.servicesByState.putAll(Service.State.NEW, services);
      }

      void tryStartTiming(Service service) {
         this.monitor.enter();

         try {
            Stopwatch stopwatch = this.startupTimers.get(service);
            if (stopwatch == null) {
               this.startupTimers.put(service, Stopwatch.createStarted());
            }
         } finally {
            this.monitor.leave();
         }
      }

      void markReady() {
         this.monitor.enter();

         try {
            if (this.transitioned) {
               List<Service> servicesInBadStates = Lists.newArrayList();

               for (Service service : this.servicesByState().values()) {
                  if (service.state() != Service.State.NEW) {
                     servicesInBadStates.add(service);
                  }
               }

               throw new IllegalArgumentException(
                  "Services started transitioning asynchronously before the ServiceManager was constructed: " + servicesInBadStates
               );
            }

            this.ready = true;
         } finally {
            this.monitor.leave();
         }
      }

      void addListener(ServiceManager.Listener listener, Executor executor) {
         this.listeners.addListener(listener, executor);
      }

      void awaitHealthy() {
         this.monitor.enterWhenUninterruptibly(this.awaitHealthGuard);

         try {
            this.checkHealthy();
         } finally {
            this.monitor.leave();
         }
      }

      void awaitHealthy(long timeout, TimeUnit unit) throws TimeoutException {
         this.monitor.enter();

         try {
            if (!this.monitor.waitForUninterruptibly(this.awaitHealthGuard, timeout, unit)) {
               throw new TimeoutException(
                  "Timeout waiting for the services to become healthy. The following services have not started: "
                     + Multimaps.<Service.State, Service>filterKeys(
                        this.servicesByState, Predicates.in(ImmutableSet.of(Service.State.NEW, Service.State.STARTING))
                     )
               );
            }

            this.checkHealthy();
         } finally {
            this.monitor.leave();
         }
      }

      void awaitStopped() {
         this.monitor.enterWhenUninterruptibly(this.stoppedGuard);
         this.monitor.leave();
      }

      void awaitStopped(long timeout, TimeUnit unit) throws TimeoutException {
         this.monitor.enter();

         try {
            if (!this.monitor.waitForUninterruptibly(this.stoppedGuard, timeout, unit)) {
               throw new TimeoutException(
                  "Timeout waiting for the services to stop. The following services have not stopped: "
                     + Multimaps.<Service.State, Service>filterKeys(
                        this.servicesByState, Predicates.not(Predicates.in(EnumSet.of(Service.State.TERMINATED, Service.State.FAILED)))
                     )
               );
            }
         } finally {
            this.monitor.leave();
         }
      }

      ImmutableSetMultimap<Service.State, Service> servicesByState() {
         ImmutableSetMultimap.Builder<Service.State, Service> builder = ImmutableSetMultimap.builder();
         this.monitor.enter();

         try {
            for (Entry<Service.State, Service> entry : this.servicesByState.entries()) {
               if (!(entry.getValue() instanceof ServiceManager.NoOpService)) {
                  builder.put(entry);
               }
            }
         } finally {
            this.monitor.leave();
         }

         return builder.build();
      }

      ImmutableMap<Service, Long> startupTimes() {
         this.monitor.enter();

         List<Entry<Service, Long>> loadTimes;
         try {
            loadTimes = Lists.newArrayListWithCapacity(this.startupTimers.size());

            for (Entry<Service, Stopwatch> entry : this.startupTimers.entrySet()) {
               Service service = entry.getKey();
               Stopwatch stopwatch = entry.getValue();
               if (!stopwatch.isRunning() && !(service instanceof ServiceManager.NoOpService)) {
                  loadTimes.add(Maps.immutableEntry(service, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
               }
            }
         } finally {
            this.monitor.leave();
         }

         Collections.sort(loadTimes, Ordering.natural().onResultOf(Entry::getValue));
         return ImmutableMap.copyOf(loadTimes);
      }

      void transitionService(Service service, Service.State from, Service.State to) {
         Preconditions.checkNotNull(service);
         Preconditions.checkArgument(from != to);
         this.monitor.enter();

         try {
            this.transitioned = true;
            if (!this.ready) {
               return;
            }

            Preconditions.checkState(this.servicesByState.remove(from, service), "Service %s not at the expected location in the state map %s", service, from);
            Preconditions.checkState(this.servicesByState.put(to, service), "Service %s in the state map unexpectedly at %s", service, to);
            Stopwatch stopwatch = this.startupTimers.get(service);
            if (stopwatch == null) {
               stopwatch = Stopwatch.createStarted();
               this.startupTimers.put(service, stopwatch);
            }

            if (to.compareTo(Service.State.RUNNING) >= 0 && stopwatch.isRunning()) {
               stopwatch.stop();
               if (!(service instanceof ServiceManager.NoOpService)) {
                  ServiceManager.logger.get().log(Level.FINE, "Started {0} in {1}.", new Object[]{service, stopwatch});
               }
            }

            if (to == Service.State.FAILED) {
               this.enqueueFailedEvent(service);
            }

            if (this.states.count(Service.State.RUNNING) == this.numberOfServices) {
               this.enqueueHealthyEvent();
            } else if (this.states.count(Service.State.TERMINATED) + this.states.count(Service.State.FAILED) == this.numberOfServices) {
               this.enqueueStoppedEvent();
            }
         } finally {
            this.monitor.leave();
            this.dispatchListenerEvents();
         }
      }

      void enqueueStoppedEvent() {
         this.listeners.enqueue(ServiceManager.STOPPED_EVENT);
      }

      void enqueueHealthyEvent() {
         this.listeners.enqueue(ServiceManager.HEALTHY_EVENT);
      }

      void enqueueFailedEvent(Service service) {
         this.listeners.enqueue(new ListenerCallQueue.Event<ServiceManager.Listener>() {
            public void call(ServiceManager.Listener listener) {
               listener.failure(service);
            }

            @Override
            public String toString() {
               return "failed({service=" + service + "})";
            }
         });
      }

      void dispatchListenerEvents() {
         Preconditions.checkState(!this.monitor.isOccupiedByCurrentThread(), "It is incorrect to execute listeners with the monitor held.");
         this.listeners.dispatch();
      }

      @GuardedBy("monitor")
      void checkHealthy() {
         if (this.states.count(Service.State.RUNNING) != this.numberOfServices) {
            IllegalStateException exception = new IllegalStateException(
               "Expected to be healthy after starting. The following services are not running: "
                  + Multimaps.<Service.State, Service>filterKeys(this.servicesByState, Predicates.not(Predicates.equalTo(Service.State.RUNNING)))
            );

            for (Service service : this.servicesByState.get(Service.State.FAILED)) {
               exception.addSuppressed(new ServiceManager.FailedService(service));
            }

            throw exception;
         }
      }

      final class AwaitHealthGuard extends Monitor.Guard {
         AwaitHealthGuard() {
            super(ServiceManagerState.this.monitor);
         }

         @GuardedBy("ServiceManagerState.this.monitor")
         @Override
         public boolean isSatisfied() {
            return ServiceManagerState.this.states.count(Service.State.RUNNING) == ServiceManagerState.this.numberOfServices
               || ServiceManagerState.this.states.contains(Service.State.STOPPING)
               || ServiceManagerState.this.states.contains(Service.State.TERMINATED)
               || ServiceManagerState.this.states.contains(Service.State.FAILED);
         }
      }

      final class StoppedGuard extends Monitor.Guard {
         StoppedGuard() {
            super(ServiceManagerState.this.monitor);
         }

         @GuardedBy("ServiceManagerState.this.monitor")
         @Override
         public boolean isSatisfied() {
            return ServiceManagerState.this.states.count(Service.State.TERMINATED) + ServiceManagerState.this.states.count(Service.State.FAILED)
               == ServiceManagerState.this.numberOfServices;
         }
      }
   }
}
