package com.google.thirdparty.publicsuffix;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Joiner;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.dfsek.terra.lib.google.common.collect.Queues;
import java.util.Deque;

@GwtCompatible
final class TrieParser {
   private static final Joiner DIRECT_JOINER = Joiner.on("");

   static ImmutableMap<String, PublicSuffixType> parseTrie(CharSequence... encodedChunks) {
      String encoded = DIRECT_JOINER.join(encodedChunks);
      return parseFullString(encoded);
   }

   @VisibleForTesting
   static ImmutableMap<String, PublicSuffixType> parseFullString(String encoded) {
      ImmutableMap.Builder<String, PublicSuffixType> builder = ImmutableMap.builder();
      int encodedLen = encoded.length();
      int idx = 0;

      while (idx < encodedLen) {
         idx += doParseTrieToBuilder(Queues.newArrayDeque(), encoded, idx, builder);
      }

      return builder.buildOrThrow();
   }

   private static int doParseTrieToBuilder(Deque<CharSequence> stack, CharSequence encoded, int start, ImmutableMap.Builder<String, PublicSuffixType> builder) {
      int encodedLen = encoded.length();
      int idx = start;
      char c = 0;

      while (idx < encodedLen) {
         c = encoded.charAt(idx);
         if (c == '&' || c == '?' || c == '!' || c == ':' || c == ',') {
            break;
         }

         idx++;
      }

      stack.push(reverse(encoded.subSequence(start, idx)));
      if (c == '!' || c == '?' || c == ':' || c == ',') {
         String domain = DIRECT_JOINER.join(stack);
         if (domain.length() > 0) {
            builder.put(domain, PublicSuffixType.fromCode(c));
         }
      }

      idx++;
      if (c != '?' && c != ',') {
         while (idx < encodedLen) {
            idx += doParseTrieToBuilder(stack, encoded, idx, builder);
            if (encoded.charAt(idx) == '?' || encoded.charAt(idx) == ',') {
               idx++;
               break;
            }
         }
      }

      stack.pop();
      return idx - start;
   }

   private static CharSequence reverse(CharSequence s) {
      return new StringBuilder(s).reverse();
   }
}
