package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.StringUtils;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class PropertiesStringLookup extends AbstractPathFencedLookup {
   static final PropertiesStringLookup INSTANCE = new PropertiesStringLookup((Path[])null);
   static final String SEPARATOR = "::";

   static String toPropertyKey(String file, String key) {
      return AbstractStringLookup.toLookupKey(file, "::", key);
   }

   PropertiesStringLookup(Path... fences) {
      super(fences);
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      String[] keys = key.split("::");
      int keyLen = keys.length;
      if (keyLen < 2) {
         throw IllegalArgumentExceptions.format("Bad properties key format [%s]; expected format is %s.", key, toPropertyKey("DocumentPath", "Key"));
      }

      String documentPath = keys[0];
      String propertyKey = StringUtils.substringAfter(key, "::");

      try {
         Properties properties = new Properties();
         InputStream inputStream = Files.newInputStream(this.getPath(documentPath));

         try {
            properties.load(inputStream);
         } catch (Throwable var11) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }
            }

            throw var11;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return properties.getProperty(propertyKey);
      } catch (Exception e) {
         throw IllegalArgumentExceptions.format(e, "Error looking up properties [%s] and key [%s].", documentPath, propertyKey);
      }
   }
}
