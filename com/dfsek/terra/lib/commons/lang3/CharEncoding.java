package com.dfsek.terra.lib.commons.lang3;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;

@Deprecated
public class CharEncoding {
   public static final String ISO_8859_1 = StandardCharsets.ISO_8859_1.name();
   public static final String US_ASCII = StandardCharsets.US_ASCII.name();
   public static final String UTF_16 = StandardCharsets.UTF_16.name();
   public static final String UTF_16BE = StandardCharsets.UTF_16BE.name();
   public static final String UTF_16LE = StandardCharsets.UTF_16LE.name();
   public static final String UTF_8 = StandardCharsets.UTF_8.name();

   @Deprecated
   public static boolean isSupported(String name) {
      if (name == null) {
         return false;
      }

      try {
         return Charset.isSupported(name);
      } catch (IllegalCharsetNameException ex) {
         return false;
      }
   }
}
