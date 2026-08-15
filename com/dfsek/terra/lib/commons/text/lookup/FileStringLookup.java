package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.StringUtils;
import java.nio.file.Files;
import java.nio.file.Path;

final class FileStringLookup extends AbstractPathFencedLookup {
   static final AbstractStringLookup INSTANCE = new FileStringLookup((Path[])null);

   FileStringLookup(Path... fences) {
      super(fences);
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      String[] keys = key.split(String.valueOf(':'));
      int keyLen = keys.length;
      if (keyLen < 2) {
         throw IllegalArgumentExceptions.format("Bad file key format [%s], expected format is CharsetName:DocumentPath.", key);
      }

      String charsetName = keys[0];
      String fileName = StringUtils.substringAfter(key, 58);

      try {
         return new String(Files.readAllBytes(this.getPath(fileName)), charsetName);
      } catch (Exception e) {
         throw IllegalArgumentExceptions.format(e, "Error looking up file [%s] with charset [%s].", fileName, charsetName);
      }
   }
}
