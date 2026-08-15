package com.dfsek.tectonic.api.depth;

public class IndexLevel implements Level {
   private final int index;

   public IndexLevel(int index) {
      this.index = index;
   }

   @Override
   public String descriptor() {
      return "[" + this.index + "]";
   }

   @Override
   public String joinDescriptor() {
      return "";
   }

   @Override
   public String verboseDescriptor() {
      return "At index " + this.index;
   }

   public int getIndex() {
      return this.index;
   }
}
