package com.dfsek.terra.lib.commons.lang3;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Objects;

public class ClassLoaderUtils {
   private static final URL[] EMPTY_URL_ARRAY = new URL[0];

   public static URL[] getSystemURLs() {
      return getURLs(ClassLoader.getSystemClassLoader());
   }

   public static URL[] getThreadURLs() {
      return getURLs(Thread.currentThread().getContextClassLoader());
   }

   private static URL[] getURLs(ClassLoader cl) {
      return cl instanceof URLClassLoader ? ((URLClassLoader)cl).getURLs() : EMPTY_URL_ARRAY;
   }

   public static String toString(ClassLoader classLoader) {
      return classLoader instanceof URLClassLoader ? toString((URLClassLoader)classLoader) : Objects.toString(classLoader);
   }

   public static String toString(URLClassLoader classLoader) {
      return classLoader != null ? classLoader + Arrays.toString(classLoader.getURLs()) : "null";
   }
}
