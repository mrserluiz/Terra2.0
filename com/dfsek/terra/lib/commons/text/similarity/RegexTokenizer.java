package com.dfsek.terra.lib.commons.text.similarity;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.StringUtils;
import com.dfsek.terra.lib.commons.lang3.Validate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RegexTokenizer implements CharSequenceTokenizer<CharSequence> {
   private static final Pattern PATTERN = Pattern.compile("(\\w)+");
   static final RegexTokenizer INSTANCE = new RegexTokenizer();

   public CharSequence[] apply(CharSequence text) {
      Validate.isTrue(StringUtils.isNotBlank(text), "Invalid text");
      Matcher matcher = PATTERN.matcher(text);
      List<String> tokens = new ArrayList<>();

      while (matcher.find()) {
         tokens.add(matcher.group(0));
      }

      return tokens.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
   }
}
