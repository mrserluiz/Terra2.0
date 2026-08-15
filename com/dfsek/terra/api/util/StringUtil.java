package com.dfsek.terra.api.util;

import java.io.File;

public class StringUtil {
   public static String fileName(String path) {
      if (path.contains(File.separator)) {
         return path.substring(path.lastIndexOf(File.separatorChar) + 1, path.lastIndexOf(46));
      } else if (path.contains("/")) {
         return path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(46));
      } else {
         return path.contains(".") ? path.substring(0, path.lastIndexOf(46)) : path;
      }
   }
}
