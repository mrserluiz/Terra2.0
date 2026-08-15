package com.dfsek.tectonic.impl.abstraction;

import com.dfsek.tectonic.api.exception.abstraction.AbstractionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class AbstractPool {
   private final Map<String, Prototype> pool = new HashMap<>();

   public void add(Prototype prototype) {
      this.pool.put(prototype.getID(), prototype);
   }

   public Prototype get(String id) {
      return this.pool.get(id);
   }

   public void loadAll() throws AbstractionException {
      for (Entry<String, Prototype> entry : this.pool.entrySet()) {
         entry.getValue().build(this, Collections.emptySet());
      }
   }

   public Set<Prototype> getPrototypes() {
      return new HashSet<>(this.pool.values());
   }
}
