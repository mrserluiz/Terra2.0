package com.dfsek.terra.profiler;

public class Frame {
   private final String id;
   private final long start;

   public Frame(String id) {
      this.id = id;
      this.start = System.nanoTime();
   }

   @Override
   public String toString() {
      return this.id;
   }

   public String getId() {
      return this.id;
   }

   public long getStart() {
      return this.start;
   }
}
