package com.dfsek.terra.lib.commons.lang3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class RuntimeEnvironment {
   private static Boolean containsLine(String path, String line) {
      try {
         Stream<String> stream = Files.lines(Paths.get(path));

         Boolean var3;
         try {
            var3 = stream.anyMatch(test -> test.contains(line));
         } catch (Throwable var6) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (stream != null) {
            stream.close();
         }

         return var3;
      } catch (IOException e) {
         return false;
      }
   }

   public static Boolean inContainer() {
      return inDocker() || inPodman();
   }

   static Boolean inDocker() {
      return containsLine("/proc/1/cgroup", "/docker");
   }

   static Boolean inPodman() {
      return containsLine("/proc/1/environ", "container=podman");
   }

   static Boolean inWsl() {
      return containsLine("/proc/1/environ", "container=wslcontainer_host_id");
   }
}
