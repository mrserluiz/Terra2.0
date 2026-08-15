package com.dfsek.terra.lib.commons.text;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.StringUtils;
import java.util.HashSet;
import java.util.Set;

public class CaseUtils {
   public static String toCamelCase(String str, boolean capitalizeFirstLetter, char... delimiters) {
      if (StringUtils.isEmpty(str)) {
         return str;
      }

      str = str.toLowerCase();
      int strLen = str.length();
      int[] newCodePoints = new int[strLen];
      int outOffset = 0;
      Set<Integer> delimiterSet = toDelimiterSet(delimiters);
      boolean capitalizeNext = capitalizeFirstLetter;
      int index = 0;

      while (index < strLen) {
         int codePoint = str.codePointAt(index);
         if (delimiterSet.contains(codePoint)) {
            capitalizeNext = outOffset != 0;
            index += Character.charCount(codePoint);
         } else if (!capitalizeNext && (outOffset != 0 || !capitalizeFirstLetter)) {
            newCodePoints[outOffset++] = codePoint;
            index += Character.charCount(codePoint);
         } else {
            int titleCaseCodePoint = Character.toTitleCase(codePoint);
            newCodePoints[outOffset++] = titleCaseCodePoint;
            index += Character.charCount(titleCaseCodePoint);
            capitalizeNext = false;
         }
      }

      return new String(newCodePoints, 0, outOffset);
   }

   private static Set<Integer> toDelimiterSet(char[] delimiters) {
      Set<Integer> delimiterHashSet = new HashSet<>();
      delimiterHashSet.add(Character.codePointAt(new char[]{' '}, 0));
      if (ArrayUtils.isEmpty(delimiters)) {
         return delimiterHashSet;
      }

      for (int index = 0; index < delimiters.length; index++) {
         delimiterHashSet.add(Character.codePointAt(delimiters, index));
      }

      return delimiterHashSet;
   }
}
