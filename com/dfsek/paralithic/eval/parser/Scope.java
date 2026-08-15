package com.dfsek.paralithic.eval.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class Scope {
   private static Scope root;
   private Scope parent;
   private final Map<String, NamedConstant> namedConstants = new ConcurrentHashMap<>();
   private final List<String> invocationVars = new ArrayList<>();
   private final Map<String, Integer> localVars = new HashMap<>();

   public Scope() {
      this(false);
   }

   private Scope(boolean skipParent) {
      if (!skipParent) {
         this.parent = getRootScope();
      }
   }

   private static Scope getRootScope() {
      if (root == null) {
         synchronized (Scope.class) {
            root = new Scope(true);
            root.create("pi", Math.PI);
            root.create("euler", Math.E);
         }
      }

      return root;
   }

   public Scope withParent(Scope parent) {
      if (parent == null) {
         this.parent = getRootScope();
      } else {
         this.parent = parent;
      }

      return this;
   }

   public NamedConstant create(String name, double value) {
      NamedConstant result = new NamedConstant(name, value);
      this.namedConstants.put(name, result);
      return result;
   }

   private int totalLocalVariablesInParents() {
      int total = 0;
      if (this.parent != null) {
         total += this.parent.localVars.size() + this.parent.totalLocalVariablesInParents();
      }

      return total;
   }

   public int addLocalVariable(String name) {
      if (this.localVars.containsKey(name)) {
         throw new IllegalArgumentException(
            String.format("Variable '%s' has already been declared in this scope, this should be ensured outside this class", name)
         );
      }

      int index = this.totalLocalVariablesInParents() + this.localVars.size();
      this.localVars.put(name, index);
      return index;
   }

   public Integer getLocalVariableIndex(String name) {
      if (this.localVars.containsKey(name)) {
         return this.localVars.get(name);
      } else {
         return this.parent != null ? this.parent.getLocalVariableIndex(name) : null;
      }
   }

   public Scope getParent() {
      if (this.parent == null) {
         throw new IllegalStateException("Attempted to get parent when none exist");
      } else {
         return this.parent;
      }
   }

   public void addInvocationVariable(String name) {
      if (!this.invocationVars.contains(name) && this.find(name) == null) {
         this.invocationVars.add(name);
      } else {
         throw new IllegalArgumentException("constant \"" + name + "\" already defined in this scope.");
      }
   }

   public void removeInvocationVariable(String name) {
      this.invocationVars.remove(name);
   }

   public int getInvocationVarIndex(String name) {
      int index = this.invocationVars.indexOf(name);
      if (index >= 0) {
         return index;
      } else {
         return this.parent != null ? this.parent.getInvocationVarIndex(name) : -1;
      }
   }

   public NamedConstant find(String name) {
      if (this.namedConstants.containsKey(name)) {
         return this.namedConstants.get(name);
      } else {
         return this.parent != null ? this.parent.find(name) : null;
      }
   }

   public NamedConstant remove(String name) {
      return this.namedConstants.containsKey(name) ? this.namedConstants.remove(name) : null;
   }

   public Set<String> getLocalNames() {
      return this.namedConstants.keySet();
   }

   public Set<String> getNames() {
      if (this.parent == null) {
         return this.getLocalNames();
      }

      Set<String> result = new TreeSet<>();
      result.addAll(this.parent.getNames());
      result.addAll(this.getLocalNames());
      return result;
   }

   public Collection<NamedConstant> getLocalConstants() {
      return this.namedConstants.values();
   }

   public Collection<NamedConstant> getConstants() {
      if (this.parent == null) {
         return this.getLocalConstants();
      }

      List<NamedConstant> result = new ArrayList<>();
      result.addAll(this.parent.getConstants());
      result.addAll(this.getLocalConstants());
      return result;
   }
}
