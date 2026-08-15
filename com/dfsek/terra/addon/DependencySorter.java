package com.dfsek.terra.addon;

import com.dfsek.terra.addon.dependency.CircularDependencyException;
import com.dfsek.terra.addon.dependency.DependencyException;
import com.dfsek.terra.addon.dependency.DependencyVersionException;
import com.dfsek.terra.api.addon.BaseAddon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencySorter {
   private final Map<String, BaseAddon> addons = new HashMap<>();
   private final Map<String, Boolean> visited = new HashMap<>();
   private final List<BaseAddon> addonList = new ArrayList<>();

   public void add(BaseAddon addon) {
      this.addons.put(addon.getID(), addon);
      this.visited.put(addon.getID(), false);
      this.addonList.add(addon);
   }

   private void sortDependencies(BaseAddon addon, List<BaseAddon> sort) {
      addon.getDependencies()
         .forEach(
            (id, range) -> {
               BaseAddon dependency = this.get(id, addon);
               if (!range.isSatisfiedBy(dependency.getVersion())) {
                  throw new DependencyVersionException(
                     "Addon "
                        + addon.getID()
                        + " specifies dependency on "
                        + id
                        + ", versions "
                        + range.getFormatted()
                        + ", but non-matching version "
                        + dependency.getVersion().getFormatted()
                        + " is installed."
                  );
               }

               if (!this.visited.get(dependency.getID())) {
                  this.visited.put(dependency.getID(), true);
                  this.sortDependencies(dependency, sort);
                  sort.add(dependency);
               }
            }
         );
   }

   private BaseAddon get(String id, BaseAddon addon) {
      if (!this.addons.containsKey(id)) {
         throw new DependencyException(
            "Addon "
               + addon.getID()
               + " specifies dependency on "
               + id
               + ", versions "
               + addon.getDependencies().get(id).getFormatted()
               + ", but no such addon is installed."
         );
      } else {
         return this.addons.get(id);
      }
   }

   private void checkDependencies(BaseAddon base, BaseAddon current) {
      current.getDependencies().forEach((id, range) -> {
         BaseAddon dependency = this.get(id, current);
         if (dependency.getID().equals(base.getID())) {
            throw new CircularDependencyException("Addon " + base.getID() + " has circular dependency beginning with " + dependency.getID());
         }

         this.checkDependencies(base, dependency);
      });
   }

   public List<BaseAddon> sort() {
      List<BaseAddon> sorted = new ArrayList<>();

      for (int i = this.addonList.size() - 1; i >= 0; i--) {
         BaseAddon addon = this.addonList.get(i);
         this.checkDependencies(addon, addon);
         this.addonList.remove(i);
         if (!this.visited.get(addon.getID())) {
            this.sortDependencies(addon, sorted);
         }

         if (!this.visited.get(addon.getID())) {
            sorted.add(addon);
            this.visited.put(addon.getID(), true);
         }
      }

      return sorted;
   }
}
