package com.dfsek.terra.api.profiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Timings {
   private final Map<String, Timings> subItems = new HashMap<>();
   private final List<Long> timings = new ArrayList<>();

   public void addTime(long time) {
      this.timings.add(time);
   }

   public double average() {
      return (double)this.timings.stream().reduce(0L, Long::sum).longValue() / this.timings.size();
   }

   public long max() {
      return this.timings.stream().mapToLong(Long::longValue).max().orElse(0L);
   }

   public long min() {
      return this.timings.stream().mapToLong(Long::longValue).min().orElse(0L);
   }

   public double sum() {
      return this.timings.stream().mapToDouble(Long::doubleValue).sum();
   }

   @Override
   public String toString() {
      return this.toString(1, this, Collections.emptySet());
   }

   private String toString(int indent, Timings parent, Set<Integer> branches) {
      StringBuilder builder = new StringBuilder();
      builder.append(this.min() / 1000000.0)
         .append("ms min / ")
         .append(this.average() / 1000000.0)
         .append("ms avg / ")
         .append(this.max() / 1000000.0)
         .append("ms max (")
         .append(this.timings.size())
         .append(" samples, ")
         .append(this.sum() / parent.sum() * 100.0)
         .append("% of parent)");
      List<String> frames = new ArrayList<>();
      Set<Integer> newBranches = new HashSet<>(branches);
      newBranches.add(indent);
      this.subItems.forEach((id, timings) -> frames.add(id + ": " + timings.toString(indent + 1, this, newBranches)));

      for (int i = 0; i < frames.size(); i++) {
         builder.append('\n');

         for (int j = 0; j < indent; j++) {
            if (branches.contains(j)) {
               builder.append("│   ");
            } else {
               builder.append("    ");
            }
         }

         if (i == frames.size() - 1 && !frames.get(i).contains("\n")) {
            builder.append("└───");
         } else {
            builder.append("├───");
         }

         builder.append(frames.get(i));
      }

      return builder.toString();
   }

   public List<Long> getTimings() {
      return this.timings;
   }

   public Timings getSubItem(String id) {
      return this.subItems.computeIfAbsent(id, s -> new Timings());
   }
}
