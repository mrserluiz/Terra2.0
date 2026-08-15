package com.dfsek.terra.lib.commons.io.serialization;

import com.dfsek.terra.lib.commons.io.FilenameUtils;

final class WildcardClassNameMatcher implements ClassNameMatcher {
   private final String pattern;

   WildcardClassNameMatcher(String pattern) {
      this.pattern = pattern;
   }

   @Override
   public boolean matches(String className) {
      return FilenameUtils.wildcardMatch(className, this.pattern);
   }
}
