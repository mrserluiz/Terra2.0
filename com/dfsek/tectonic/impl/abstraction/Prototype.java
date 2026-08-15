package com.dfsek.tectonic.impl.abstraction;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.config.template.ValidatedConfigTemplate;
import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Final;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.exception.ConfigException;
import com.dfsek.tectonic.api.exception.ValidationException;
import com.dfsek.tectonic.api.exception.abstraction.AbstractionException;
import com.dfsek.tectonic.api.exception.abstraction.CircularInheritanceException;
import com.dfsek.tectonic.api.exception.abstraction.ParentNotFoundException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class Prototype implements ValidatedConfigTemplate {
   private final List<Prototype> children = new ArrayList<>();
   private final Configuration config;
   private final List<Prototype> parents = new ArrayList<>();
   private boolean isRoot = false;
   @Value("id")
   @Final
   private String id;
   @Value("extends")
   @Final
   @Default
   private List<String> extend = Collections.emptyList();
   @Value("abstract")
   @Final
   @Default
   private boolean isAbstract = false;

   public Prototype(Configuration config) throws ConfigException {
      this.config = config;
      new ConfigLoader().load(this, config);
   }

   @NotNull
   public Configuration getConfig() {
      return this.config;
   }

   public boolean isAbstract() {
      return this.isAbstract;
   }

   protected void build(AbstractPool pool, Set<Prototype> parents) throws AbstractionException {
      if (parents.contains(this)) {
         throw new CircularInheritanceException("Circular inheritance detected in config: \"" + this.getID() + "\", extending \"" + this.extend + "\"");
      }

      Set<Prototype> newParents = new HashSet<>(parents);
      newParents.add(this);
      int index = 0;

      for (String parentID : this.extend) {
         Prototype parent = pool.get(parentID);
         if (parent == null) {
            throw new ParentNotFoundException("No such config \"" + parentID + "\". Specified as parent of \"" + this.id + "\" at index " + index);
         }

         this.parents.add(parent);
         parent.build(pool, newParents);
         index++;
      }

      if (this.extend.size() == 0) {
         this.isRoot = true;
      }
   }

   @NotNull
   public String getID() {
      return this.id;
   }

   @NotNull
   public List<Prototype> getParents() {
      return this.parents;
   }

   public boolean isRoot() {
      return this.isRoot;
   }

   @Override
   public boolean validate() throws ValidationException {
      if (!this.id.matches("^[a-zA-Z0-9_-]*$")) {
         throw new ValidationException("ID must only contain alphanumeric characters, hyphens, and underscores. \"" + this.id + "\" is not a valid ID.");
      } else {
         return true;
      }
   }
}
