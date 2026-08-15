package org.incendo.cloud.services;

import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.services.annotation.Order;
import org.incendo.cloud.services.type.Service;

public final class ServiceRepository<Context, Response> {
   private final Object lock = new Object();
   private final TypeToken<? extends Service<Context, Response>> serviceType;
   private final List<ServiceRepository<Context, Response>.ServiceWrapper<? extends Service<Context, Response>>> implementations;
   private int registrationOrder = 0;

   ServiceRepository(final @NonNull TypeToken<? extends Service<Context, Response>> serviceType) {
      this.serviceType = serviceType;
      this.implementations = new LinkedList<>();
   }

   <T extends Service<Context, Response>> void registerImplementation(final @NonNull T service, final @NonNull Collection<Predicate<Context>> filters) {
      synchronized (this.lock) {
         this.implementations.add(new ServiceRepository.ServiceWrapper<>(service, filters));
      }
   }

   @NonNull LinkedList<ServiceRepository.ServiceWrapper<? extends Service<Context, Response>>> queue() {
      synchronized (this.lock) {
         return new LinkedList<>(this.implementations);
      }
   }

   final class ServiceWrapper<T extends Service<Context, Response>> implements Comparable<ServiceRepository<Context, Response>.ServiceWrapper<T>> {
      private final boolean defaultImplementation;
      private final T implementation;
      private final Collection<Predicate<Context>> filters;
      private final int registrationOrder;
      private final ExecutionOrder executionOrder;

      private ServiceWrapper(final @NonNull T implementation, final @NonNull Collection<Predicate<Context>> filters) {
         this.registrationOrder = ServiceRepository.this.registrationOrder++;
         this.defaultImplementation = ServiceRepository.this.implementations.isEmpty();
         this.implementation = implementation;
         this.filters = filters;
         ExecutionOrder executionOrder = implementation.order();
         if (executionOrder == null) {
            Order order = implementation.getClass().getAnnotation(Order.class);
            if (order != null) {
               executionOrder = order.value();
            } else {
               executionOrder = ExecutionOrder.SOON;
            }
         }

         this.executionOrder = executionOrder;
      }

      @NonNull T implementation() {
         return this.implementation;
      }

      @NonNull Collection<Predicate<Context>> filters() {
         return Collections.unmodifiableCollection(this.filters);
      }

      boolean isDefaultImplementation() {
         return this.defaultImplementation;
      }

      @Override
      public String toString() {
         return String.format(
            "ServiceWrapper{type=%s,implementation=%s}",
            ServiceRepository.this.serviceType.getType().getTypeName(),
            TypeToken.get(this.implementation.getClass()).getType().getTypeName()
         );
      }

      public int compareTo(final ServiceRepository.@NonNull ServiceWrapper<T> other) {
         return Comparator.<ServiceRepository.ServiceWrapper>comparingInt(wrapper -> wrapper.isDefaultImplementation() ? Integer.MIN_VALUE : Integer.MAX_VALUE)
            .thenComparingInt(wrapper -> wrapper.executionOrder.ordinal())
            .thenComparingInt(wrapper -> wrapper.registrationOrder)
            .compare(this, other);
      }
   }
}
