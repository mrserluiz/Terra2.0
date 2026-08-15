package com.dfsek.terra.lib.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;

@Deprecated
public class UnicodeUnpairedSurrogateRemover extends CodePointTranslator {
   @Override
   public boolean translate(int codePoint, Writer out) throws IOException {
      return codePoint >= 55296 && codePoint <= 57343;
   }
}
