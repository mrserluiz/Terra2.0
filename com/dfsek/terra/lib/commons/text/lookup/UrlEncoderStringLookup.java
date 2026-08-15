package com.dfsek.terra.lib.commons.text.lookup;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class UrlEncoderStringLookup extends AbstractStringLookup {
   static final UrlEncoderStringLookup INSTANCE = new UrlEncoderStringLookup();

   private UrlEncoderStringLookup() {
   }

   String encode(String key, String enc) throws UnsupportedEncodingException {
      return URLEncoder.encode(key, enc);
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      String enc = StandardCharsets.UTF_8.name();

      try {
         return this.encode(key, enc);
      } catch (UnsupportedEncodingException e) {
         throw IllegalArgumentExceptions.format(e, "%s: source=%s, encoding=%s", e, key, enc);
      }
   }
}
