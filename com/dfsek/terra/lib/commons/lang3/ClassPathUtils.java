package com.dfsek.terra.lib.commons.lang3;

import java.util.Objects;

public class ClassPathUtils {
   public static String packageToPath(String path) {
      return Objects.requireNonNull(path, "path").replace('.', '/');
   }

   public static String pathToPackage(String path) {
      return Objects.requireNonNull(path, "path").replace('/', '.');
   }

   public static String toFullyQualifiedName(Class<?> context, String resourceName) {
      Objects.requireNonNull(context, "context");
      Objects.requireNonNull(resourceName, "resourceName");
      return toFullyQualifiedName(context.getPackage(), resourceName);
   }

   public static String toFullyQualifiedName(Package context, String resourceName) {
      Objects.requireNonNull(context, "context");
      Objects.requireNonNull(resourceName, "resourceName");
      return context.getName() + "." + resourceName;
   }

   public static String toFullyQualifiedPath(Class<?> context, String resourceName) {
      Objects.requireNonNull(context, "context");
      Objects.requireNonNull(resourceName, "resourceName");
      return toFullyQualifiedPath(context.getPackage(), resourceName);
   }

   public static String toFullyQualifiedPath(Package context, String resourceName) {
      Objects.requireNonNull(context, "context");
      Objects.requireNonNull(resourceName, "resourceName");
      return packageToPath(context.getName()) + "/" + resourceName;
   }
}
