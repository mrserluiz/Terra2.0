package com.dfsek.terra.lib.commons.text.translate;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class AggregateTranslator extends CharSequenceTranslator {
   private final List<CharSequenceTranslator> translators = new ArrayList<>();

   public AggregateTranslator(CharSequenceTranslator... translators) {
      if (translators != null) {
         Stream.of(translators).filter(Objects::nonNull).forEach(this.translators::add);
      }
   }

   @Override
   public int translate(CharSequence input, int index, Writer writer) throws IOException {
      for (CharSequenceTranslator translator : this.translators) {
         int consumed = translator.translate(input, index, writer);
         if (consumed != 0) {
            return consumed;
         }
      }

      return 0;
   }
}
