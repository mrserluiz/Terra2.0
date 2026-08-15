package com.dfsek.terra.lib.google.common.escape;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Escapers {
   private static final Escaper NULL_ESCAPER = new CharEscaper() {
      @Override
      public String escape(String string) {
         return Preconditions.checkNotNull(string);
      }

      @Override
      protected char @Nullable [] escape(char c) {
         return null;
      }
   };

   private Escapers() {
   }

   public static Escaper nullEscaper() {
      return NULL_ESCAPER;
   }

   public static Escapers.Builder builder() {
      return new Escapers.Builder();
   }

   public static @Nullable String computeReplacement(CharEscaper escaper, char c) {
      return stringOrNull(escaper.escape(c));
   }

   public static @Nullable String computeReplacement(UnicodeEscaper escaper, int cp) {
      return stringOrNull(escaper.escape(cp));
   }

   private static @Nullable String stringOrNull(char @Nullable [] in) {
      return in == null ? null : new String(in);
   }

   public static final class Builder {
      private final Map<Character, String> replacementMap = new HashMap<>();
      private char safeMin = 0;
      private char safeMax = '\uffff';
      private @Nullable String unsafeReplacement = null;

      private Builder() {
      }

      @CanIgnoreReturnValue
      public Escapers.Builder setSafeRange(char safeMin, char safeMax) {
         this.safeMin = safeMin;
         this.safeMax = safeMax;
         return this;
      }

      @CanIgnoreReturnValue
      public Escapers.Builder setUnsafeReplacement(@Nullable String unsafeReplacement) {
         this.unsafeReplacement = unsafeReplacement;
         return this;
      }

      @CanIgnoreReturnValue
      public Escapers.Builder addEscape(char c, String replacement) {
         Preconditions.checkNotNull(replacement);
         this.replacementMap.put(c, replacement);
         return this;
      }

      public Escaper build() {
         return new ArrayBasedCharEscaper(this.replacementMap, this.safeMin, this.safeMax) {
            private final char @Nullable [] replacementChars = Builder.this.unsafeReplacement != null ? Builder.this.unsafeReplacement.toCharArray() : null;

            @Override
            protected char @Nullable [] escapeUnsafe(char c) {
               return this.replacementChars;
            }
         };
      }
   }
}
