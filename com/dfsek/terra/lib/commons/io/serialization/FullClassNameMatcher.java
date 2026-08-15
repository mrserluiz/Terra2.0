package com.dfsek.terra.lib.commons.io.serialization;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class FullClassNameMatcher implements ClassNameMatcher {
   private final Set<String> classesSet;

   FullClassNameMatcher(String... classes) {
      this.classesSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(classes)));
   }

   @Override
   public boolean matches(String className) {
      return this.classesSet.contains(className);
   }
}
