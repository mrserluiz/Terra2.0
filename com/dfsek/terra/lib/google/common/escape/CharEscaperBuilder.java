package com.dfsek.terra.lib.google.common.escape;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class CharEscaperBuilder {
   private final Map<Character, String> map;
   private int max = -1;

   public CharEscaperBuilder() {
      this.map = new HashMap<>();
   }

   @CanIgnoreReturnValue
   public CharEscaperBuilder addEscape(char c, String r) {
      this.map.put(c, Preconditions.checkNotNull(r));
      if (c > this.max) {
         this.max = c;
      }

      return this;
   }

   @CanIgnoreReturnValue
   public CharEscaperBuilder addEscapes(char[] cs, String r) {
      Preconditions.checkNotNull(r);

      for (char c : cs) {
         this.addEscape(c, r);
      }

      return this;
   }

   public char[] @Nullable [] toArray() {
      char[][] result = new char[this.max + 1][];

      for (Entry<Character, String> entry : this.map.entrySet()) {
         result[entry.getKey()] = entry.getValue().toCharArray();
      }

      return result;
   }

   public Escaper toEscaper() {
      return new CharEscaperBuilder.CharArrayDecorator(this.toArray());
   }

   private static class CharArrayDecorator extends CharEscaper {
      private final char[] @Nullable [] replacements;
      private final int replaceLength;

      CharArrayDecorator(char[] @Nullable [] replacements) {
         this.replacements = replacements;
         this.replaceLength = replacements.length;
      }

      @Override
      public String escape(String s) {
         int slen = s.length();

         for (int index = 0; index < slen; index++) {
            char c = s.charAt(index);
            if (c < this.replacements.length && this.replacements[c] != null) {
               return this.escapeSlow(s, index);
            }
         }

         return s;
      }

      @Override
      protected char @Nullable [] escape(char c) {
         return c < this.replaceLength ? this.replacements[c] : null;
      }
   }
}
