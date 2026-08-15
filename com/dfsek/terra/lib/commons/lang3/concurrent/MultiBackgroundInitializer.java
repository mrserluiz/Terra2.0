package com.dfsek.terra.lib.commons.lang3.concurrent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class MultiBackgroundInitializer extends BackgroundInitializer<MultiBackgroundInitializer.MultiBackgroundInitializerResults> {
   private final Map<String, BackgroundInitializer<?>> childInitializers = new HashMap<>();

   public MultiBackgroundInitializer() {
   }

   public MultiBackgroundInitializer(ExecutorService exec) {
      super(exec);
   }

   public void addInitializer(String name, BackgroundInitializer<?> backgroundInitializer) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(backgroundInitializer, "backgroundInitializer");
      synchronized (this) {
         if (this.isStarted()) {
            throw new IllegalStateException("addInitializer() must not be called after start()!");
         }

         this.childInitializers.put(name, backgroundInitializer);
      }
   }

   @Override
   public void close() throws ConcurrentException {
      ConcurrentException exception = null;

      for (BackgroundInitializer<?> child : this.childInitializers.values()) {
         try {
            child.close();
         } catch (Exception e) {
            if (exception == null) {
               exception = new ConcurrentException();
            }

            if (e instanceof ConcurrentException) {
               exception.addSuppressed(e.getCause());
            } else {
               exception.addSuppressed(e);
            }
         }
      }

      if (exception != null) {
         throw exception;
      }
   }

   @Override
   protected int getTaskCount() {
      return 1 + this.childInitializers.values().stream().mapToInt(BackgroundInitializer::getTaskCount).sum();
   }

   protected MultiBackgroundInitializer.MultiBackgroundInitializerResults initialize() throws Exception {
      Map<String, BackgroundInitializer<?>> inits;
      synchronized (this) {
         inits = new HashMap<>(this.childInitializers);
      }

      ExecutorService exec = this.getActiveExecutor();
      inits.values().forEach(bi -> {
         if (bi.getExternalExecutor() == null) {
            bi.setExternalExecutor(exec);
         }

         bi.start();
      });
      Map<String, Object> results = new HashMap<>();
      Map<String, ConcurrentException> excepts = new HashMap<>();
      inits.forEach((k, v) -> {
         try {
            results.put(k, v.get());
         } catch (ConcurrentException cex) {
            excepts.put(k, cex);
         }
      });
      return new MultiBackgroundInitializer.MultiBackgroundInitializerResults(inits, results, excepts);
   }

   @Override
   public boolean isInitialized() {
      return this.childInitializers.isEmpty() ? false : this.childInitializers.values().stream().allMatch(BackgroundInitializer::isInitialized);
   }

   public static class MultiBackgroundInitializerResults {
      private final Map<String, BackgroundInitializer<?>> initializers;
      private final Map<String, Object> resultObjects;
      private final Map<String, ConcurrentException> exceptions;

      private MultiBackgroundInitializerResults(
         Map<String, BackgroundInitializer<?>> inits, Map<String, Object> results, Map<String, ConcurrentException> excepts
      ) {
         this.initializers = inits;
         this.resultObjects = results;
         this.exceptions = excepts;
      }

      private BackgroundInitializer<?> checkName(String name) {
         BackgroundInitializer<?> init = this.initializers.get(name);
         if (init == null) {
            throw new NoSuchElementException("No child initializer with name " + name);
         } else {
            return init;
         }
      }

      public ConcurrentException getException(String name) {
         this.checkName(name);
         return this.exceptions.get(name);
      }

      public BackgroundInitializer<?> getInitializer(String name) {
         return this.checkName(name);
      }

      public Object getResultObject(String name) {
         this.checkName(name);
         return this.resultObjects.get(name);
      }

      public Set<String> initializerNames() {
         return Collections.unmodifiableSet(this.initializers.keySet());
      }

      public boolean isException(String name) {
         this.checkName(name);
         return this.exceptions.containsKey(name);
      }

      public boolean isSuccessful() {
         return this.exceptions.isEmpty();
      }
   }
}
