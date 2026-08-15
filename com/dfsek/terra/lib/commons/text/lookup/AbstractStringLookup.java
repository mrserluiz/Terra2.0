package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.StringUtils;

abstract class AbstractStringLookup implements StringLookup {
   protected static final char SPLIT_CH = ':';
   protected static final String SPLIT_STR = String.valueOf(':');

   static String toLookupKey(String left, String right) {
      return toLookupKey(left, SPLIT_STR, right);
   }

   static String toLookupKey(String left, String separator, String right) {
      return left + separator + right;
   }

   @Deprecated
   protected String substringAfter(String value, char ch) {
      return StringUtils.substringAfter(value, ch);
   }

   @Deprecated
   protected String substringAfter(String value, String str) {
      return StringUtils.substringAfter(value, str);
   }

   @Deprecated
   protected String substringAfterLast(String value, char ch) {
      return StringUtils.substringAfterLast(value, ch);
   }
}
