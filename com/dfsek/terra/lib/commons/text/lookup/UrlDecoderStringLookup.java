package com.dfsek.terra.lib.commons.text.lookup;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

final class UrlDecoderStringLookup extends AbstractStringLookup {
   static final UrlDecoderStringLookup INSTANCE = new UrlDecoderStringLookup();

   private UrlDecoderStringLookup() {
   }

   String decode(String key, String enc) throws UnsupportedEncodingException {
      return URLDecoder.decode(key, enc);
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      String enc = StandardCharsets.UTF_8.name();

      try {
         return this.decode(key, enc);
      } catch (UnsupportedEncodingException e) {
         throw IllegalArgumentExceptions.format(e, "%s: source=%s, encoding=%s", e, key, enc);
      }
   }
}
