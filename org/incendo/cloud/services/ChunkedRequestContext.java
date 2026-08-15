package org.incendo.cloud.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class ChunkedRequestContext<Context, Result> {
   private final Object lock = new Object();
   private final List<Context> requests;
   private final Map<Context, Result> results;

   protected ChunkedRequestContext(final @NonNull Collection<Context> requests) {
      this.requests = new ArrayList<>(requests);
      this.results = new HashMap<>(requests.size());
   }

   public final @NonNull Map<Context, Result> availableResults() {
      synchronized (this.lock) {
         return Collections.unmodifiableMap(this.results);
      }
   }

   public final @NonNull List<Context> remaining() {
      synchronized (this.lock) {
         return Collections.unmodifiableList(this.requests);
      }
   }

   public final void storeResult(final @NonNull Context context, final @NonNull Result result) {
      synchronized (this.lock) {
         this.results.put(context, result);
         this.requests.remove(context);
      }
   }

   public final boolean isCompleted() {
      synchronized (this.lock) {
         return this.requests.isEmpty();
      }
   }
}
