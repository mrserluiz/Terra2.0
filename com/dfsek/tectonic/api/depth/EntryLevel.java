package com.dfsek.tectonic.api.depth;

public class EntryLevel implements Level {
   private final String name;

   public EntryLevel(String name) {
      this.name = name;
   }

   @Override
   public String descriptor() {
      return this.name;
   }

   @Override
   public String joinDescriptor() {
      return ".";
   }

   @Override
   public String verboseDescriptor() {
      return "In entry \"" + this.name + "\"";
   }

   public String getName() {
      return this.name;
   }
}
