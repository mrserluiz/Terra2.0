package com.dfsek.terra.lib.commons.text.matcher;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.StringUtils;

public final class StringMatcherFactory {
   private static final AbstractStringMatcher.CharMatcher COMMA_MATCHER = new AbstractStringMatcher.CharMatcher(',');
   private static final AbstractStringMatcher.CharMatcher DOUBLE_QUOTE_MATCHER = new AbstractStringMatcher.CharMatcher('"');
   public static final StringMatcherFactory INSTANCE = new StringMatcherFactory();
   private static final AbstractStringMatcher.NoneMatcher NONE_MATCHER = new AbstractStringMatcher.NoneMatcher();
   private static final AbstractStringMatcher.CharSetMatcher QUOTE_MATCHER = new AbstractStringMatcher.CharSetMatcher("'\"".toCharArray());
   private static final AbstractStringMatcher.CharMatcher SINGLE_QUOTE_MATCHER = new AbstractStringMatcher.CharMatcher('\'');
   private static final AbstractStringMatcher.CharMatcher SPACE_MATCHER = new AbstractStringMatcher.CharMatcher(' ');
   private static final AbstractStringMatcher.CharSetMatcher SPLIT_MATCHER = new AbstractStringMatcher.CharSetMatcher(" \t\n\r\f".toCharArray());
   private static final AbstractStringMatcher.CharMatcher TAB_MATCHER = new AbstractStringMatcher.CharMatcher('\t');
   private static final AbstractStringMatcher.TrimMatcher TRIM_MATCHER = new AbstractStringMatcher.TrimMatcher();

   private StringMatcherFactory() {
   }

   public StringMatcher andMatcher(StringMatcher... stringMatchers) {
      int len = ArrayUtils.getLength(stringMatchers);
      if (len == 0) {
         return NONE_MATCHER;
      } else {
         return len == 1 ? stringMatchers[0] : new AbstractStringMatcher.AndStringMatcher(stringMatchers);
      }
   }

   public StringMatcher charMatcher(char ch) {
      return new AbstractStringMatcher.CharMatcher(ch);
   }

   public StringMatcher charSetMatcher(char... chars) {
      int len = ArrayUtils.getLength(chars);
      if (len == 0) {
         return NONE_MATCHER;
      } else {
         return len == 1 ? new AbstractStringMatcher.CharMatcher(chars[0]) : new AbstractStringMatcher.CharSetMatcher(chars);
      }
   }

   public StringMatcher charSetMatcher(String chars) {
      int len = StringUtils.length(chars);
      if (len == 0) {
         return NONE_MATCHER;
      } else {
         return len == 1 ? new AbstractStringMatcher.CharMatcher(chars.charAt(0)) : new AbstractStringMatcher.CharSetMatcher(chars.toCharArray());
      }
   }

   public StringMatcher commaMatcher() {
      return COMMA_MATCHER;
   }

   public StringMatcher doubleQuoteMatcher() {
      return DOUBLE_QUOTE_MATCHER;
   }

   public StringMatcher noneMatcher() {
      return NONE_MATCHER;
   }

   public StringMatcher quoteMatcher() {
      return QUOTE_MATCHER;
   }

   public StringMatcher singleQuoteMatcher() {
      return SINGLE_QUOTE_MATCHER;
   }

   public StringMatcher spaceMatcher() {
      return SPACE_MATCHER;
   }

   public StringMatcher splitMatcher() {
      return SPLIT_MATCHER;
   }

   public StringMatcher stringMatcher(char... chars) {
      int length = ArrayUtils.getLength(chars);
      return length == 0 ? NONE_MATCHER : (length == 1 ? new AbstractStringMatcher.CharMatcher(chars[0]) : new AbstractStringMatcher.CharArrayMatcher(chars));
   }

   public StringMatcher stringMatcher(String str) {
      return StringUtils.isEmpty(str) ? NONE_MATCHER : this.stringMatcher(str.toCharArray());
   }

   public StringMatcher tabMatcher() {
      return TAB_MATCHER;
   }

   public StringMatcher trimMatcher() {
      return TRIM_MATCHER;
   }
}
