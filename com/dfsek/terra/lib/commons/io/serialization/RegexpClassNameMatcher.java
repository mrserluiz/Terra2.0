package com.dfsek.terra.lib.commons.io.serialization;

import java.util.Objects;
import java.util.regex.Pattern;

final class RegexpClassNameMatcher implements ClassNameMatcher {
   private final Pattern pattern;

   RegexpClassNameMatcher(Pattern pattern) {
      this.pattern = Objects.requireNonNull(pattern, "pattern");
   }

   RegexpClassNameMatcher(String regex) {
      this(Pattern.compile(regex));
   }

   @Override
   public boolean matches(String className) {
      return this.pattern.matcher(className).matches();
   }
}
