package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.StringUtils;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

final class UrlStringLookup extends AbstractStringLookup {
   static final UrlStringLookup INSTANCE = new UrlStringLookup();

   private UrlStringLookup() {
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      String[] keys = key.split(SPLIT_STR);
      int keyLen = keys.length;
      if (keyLen < 2) {
         throw IllegalArgumentExceptions.format("Bad URL key format [%s]; expected format is DocumentPath:Key.", key);
      }

      String charsetName = keys[0];
      String urlStr = StringUtils.substringAfter(key, 58);

      try {
         URL url = new URL(urlStr);
         int size = 8192;
         StringWriter writer = new StringWriter(8192);
         char[] buffer = new char[8192];
         BufferedInputStream bis = new BufferedInputStream(url.openStream());

         try {
            InputStreamReader reader = new InputStreamReader(bis, charsetName);

            int n;
            try {
               while (-1 != (n = reader.read(buffer))) {
                  writer.write(buffer, 0, n);
               }
            } catch (Throwable var16) {
               try {
                  reader.close();
               } catch (Throwable var15) {
                  var16.addSuppressed(var15);
               }

               throw var16;
            }

            reader.close();
         } catch (Throwable var17) {
            try {
               bis.close();
            } catch (Throwable var14) {
               var17.addSuppressed(var14);
            }

            throw var17;
         }

         bis.close();
         return writer.toString();
      } catch (Exception e) {
         throw IllegalArgumentExceptions.format(e, "Error looking up URL [%s] with Charset [%s].", urlStr, charsetName);
      }
   }
}
