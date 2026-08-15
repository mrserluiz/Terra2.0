package com.dfsek.terra.api.profiler;

import java.util.Map;

public interface Profiler {
   void push(String var1);

   void pop(String var1);

   void start();

   void stop();

   void reset();

   Map<String, Timings> getTimings();
}
