package com.dfsek.tectonic.impl.abstraction;

import com.dfsek.tectonic.api.config.Configuration;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AbstractConfiguration implements Configuration {
   private final List<AbstractConfiguration.Layer> tree = new ArrayList<>();
   private final Prototype base;
   private int layer = 0;

   public AbstractConfiguration(Prototype base) {
      this.base = base;
      this.tree.add(new AbstractConfiguration.Layer());
   }

   @Override
   public Object get(@NotNull String key) {
      for (AbstractConfiguration.Layer p : this.tree) {
         Object l = p.get(key);
         if (l != null) {
            return l;
         }
      }

      return null;
   }

   public Object getBase(String key) {
      return this.base.getConfig().get(key);
   }

   public String getID() {
      return this.base.getID();
   }

   public Prototype getBase() {
      return this.base;
   }

   @Override
   public boolean contains(@NotNull String key) {
      for (AbstractConfiguration.Layer p : this.tree) {
         if (p.contains(key)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String getName() {
      return this.getID();
   }

   public void add(Prototype prototype) {
      this.tree.get(this.layer).add(prototype);
   }

   public int next() {
      int l0 = this.layer++;
      this.tree.add(new AbstractConfiguration.Layer());
      return l0;
   }

   public void reset(int layer) {
      this.layer = layer;
   }

   public boolean containsBase(String key) {
      return this.base.getConfig().contains(key);
   }

   private static final class Layer {
      private final List<Prototype> items = new ArrayList<>();

      private Layer() {
      }

      public void add(Prototype item) {
         this.items.add(item);
      }

      public Object get(String key) {
         for (Prototype p : this.items) {
            if (p.getConfig().contains(key)) {
               return p.getConfig().get(key);
            }
         }

         return null;
      }

      public boolean contains(String key) {
         for (Prototype p : this.items) {
            if (p.getConfig().contains(key)) {
               return true;
            }
         }

         return false;
      }
   }
}
