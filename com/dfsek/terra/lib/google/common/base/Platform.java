package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
final class Platform {
   private static final Logger logger = Logger.getLogger(Platform.class.getName());
   private static final PatternCompiler patternCompiler = loadPatternCompiler();

   private Platform() {
   }

   static CharMatcher precomputeCharMatcher(CharMatcher matcher) {
      return matcher.precomputedInternal();
   }

   static <T extends Enum<T>> Optional<T> getEnumIfPresent(Class<T> enumClass, String value) {
      WeakReference<? extends Enum<?>> ref = Enums.getEnumConstants(enumClass).get(value);
      return ref == null ? Optional.absent() : Optional.fromNullable(enumClass.cast(ref.get()));
   }

   static String formatCompact4Digits(double value) {
      return String.format(Locale.ROOT, "%.4g", value);
   }

   static boolean stringIsNullOrEmpty(@Nullable String string) {
      return string == null || string.isEmpty();
   }

   static String nullToEmpty(@Nullable String string) {
      return string == null ? "" : string;
   }

   static @Nullable String emptyToNull(@Nullable String string) {
      return stringIsNullOrEmpty(string) ? null : string;
   }

   static String lenientFormat(@Nullable String template, @Nullable Object @Nullable ... args) {
      return Strings.lenientFormat(template, args);
   }

   static String stringValueOf(@Nullable Object o) {
      return String.valueOf(o);
   }

   static CommonPattern compilePattern(String pattern) {
      Preconditions.checkNotNull(pattern);
      return patternCompiler.compile(pattern);
   }

   static boolean patternCompilerIsPcreLike() {
      return patternCompiler.isPcreLike();
   }

   private static PatternCompiler loadPatternCompiler() {
      return new Platform.JdkPatternCompiler();
   }

   private static final class JdkPatternCompiler implements PatternCompiler {
      private JdkPatternCompiler() {
      }

      @Override
      public CommonPattern compile(String pattern) {
         return new JdkPattern(Pattern.compile(pattern));
      }

      @Override
      public boolean isPcreLike() {
         return true;
      }
   }
}
