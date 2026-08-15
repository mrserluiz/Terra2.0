package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DoNotMock("Create an AbstractIdleService")
@J2ktIncompatible
@GwtIncompatible
public interface Service {
   @CanIgnoreReturnValue
   Service startAsync();

   boolean isRunning();

   Service.State state();

   @CanIgnoreReturnValue
   Service stopAsync();

   void awaitRunning();

   default void awaitRunning(Duration timeout) throws TimeoutException {
      this.awaitRunning(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   void awaitRunning(long timeout, TimeUnit unit) throws TimeoutException;

   void awaitTerminated();

   default void awaitTerminated(Duration timeout) throws TimeoutException {
      this.awaitTerminated(Internal.toNanosSaturated(timeout), TimeUnit.NANOSECONDS);
   }

   void awaitTerminated(long timeout, TimeUnit unit) throws TimeoutException;

   Throwable failureCause();

   void addListener(Service.Listener listener, Executor executor);

   abstract class Listener {
      public void starting() {
      }

      public void running() {
      }

      public void stopping(Service.State from) {
      }

      public void terminated(Service.State from) {
      }

      public void failed(Service.State from, Throwable failure) {
      }
   }

   enum State {
      NEW,
      STARTING,
      RUNNING,
      STOPPING,
      TERMINATED,
      FAILED;
   }
}
