package com.dfsek.terra.lib.commons.text.matcher;

import com.dfsek.terra.lib.commons.lang3.CharSequenceUtils;

public interface StringMatcher {
   default StringMatcher andThen(StringMatcher stringMatcher) {
      return StringMatcherFactory.INSTANCE.andMatcher(this, stringMatcher);
   }

   default int isMatch(char[] buffer, int pos) {
      return this.isMatch(buffer, pos, 0, buffer.length);
   }

   int isMatch(char[] var1, int var2, int var3, int var4);

   default int isMatch(CharSequence buffer, int pos) {
      return this.isMatch(buffer, pos, 0, buffer.length());
   }

   default int isMatch(CharSequence buffer, int start, int bufferStart, int bufferEnd) {
      return this.isMatch(CharSequenceUtils.toCharArray(buffer), start, bufferEnd, bufferEnd);
   }

   default int size() {
      return 0;
   }
}
