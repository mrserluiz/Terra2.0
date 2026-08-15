package com.dfsek.terra.lib.commons.lang3.concurrent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractCircuitBreaker<T> implements CircuitBreaker<T> {
   public static final String PROPERTY_NAME = "open";
   protected final AtomicReference<AbstractCircuitBreaker.State> state = new AtomicReference<>(AbstractCircuitBreaker.State.CLOSED);
   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

   protected static boolean isOpen(AbstractCircuitBreaker.State state) {
      return state == AbstractCircuitBreaker.State.OPEN;
   }

   public void addChangeListener(PropertyChangeListener listener) {
      this.changeSupport.addPropertyChangeListener(listener);
   }

   protected void changeState(AbstractCircuitBreaker.State newState) {
      if (this.state.compareAndSet(newState.oppositeState(), newState)) {
         this.changeSupport.firePropertyChange("open", !isOpen(newState), isOpen(newState));
      }
   }

   @Override
   public abstract boolean checkState();

   @Override
   public void close() {
      this.changeState(AbstractCircuitBreaker.State.CLOSED);
   }

   @Override
   public abstract boolean incrementAndCheckState(T var1);

   @Override
   public boolean isClosed() {
      return !this.isOpen();
   }

   @Override
   public boolean isOpen() {
      return isOpen(this.state.get());
   }

   @Override
   public void open() {
      this.changeState(AbstractCircuitBreaker.State.OPEN);
   }

   public void removeChangeListener(PropertyChangeListener listener) {
      this.changeSupport.removePropertyChangeListener(listener);
   }

   protected enum State {
      CLOSED {
         @Override
         public AbstractCircuitBreaker.State oppositeState() {
            return OPEN;
         }
      },
      OPEN {
         @Override
         public AbstractCircuitBreaker.State oppositeState() {
            return CLOSED;
         }
      };

      State() {
      }

      public abstract AbstractCircuitBreaker.State oppositeState();
   }
}
