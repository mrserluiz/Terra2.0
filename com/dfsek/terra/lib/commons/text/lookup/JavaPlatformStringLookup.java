package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.StringUtils;
import java.util.Locale;

final class JavaPlatformStringLookup extends AbstractStringLookup {
   static final JavaPlatformStringLookup INSTANCE = new JavaPlatformStringLookup();
   private static final String KEY_HARDWARE = "hardware";
   private static final String KEY_LOCALE = "locale";
   private static final String KEY_OS = "os";
   private static final String KEY_RUNTIME = "runtime";
   private static final String KEY_VERSION = "version";
   private static final String KEY_VM = "vm";

   public static void main(String[] args) {
      System.out.println(JavaPlatformStringLookup.class);
      System.out.printf("%s = %s%n", "version", INSTANCE.lookup("version"));
      System.out.printf("%s = %s%n", "runtime", INSTANCE.lookup("runtime"));
      System.out.printf("%s = %s%n", "vm", INSTANCE.lookup("vm"));
      System.out.printf("%s = %s%n", "os", INSTANCE.lookup("os"));
      System.out.printf("%s = %s%n", "hardware", INSTANCE.lookup("hardware"));
      System.out.printf("%s = %s%n", "locale", INSTANCE.lookup("locale"));
   }

   private JavaPlatformStringLookup() {
   }

   String getHardware() {
      return "processors: "
         + Runtime.getRuntime().availableProcessors()
         + ", architecture: "
         + this.getSystemProperty("os.arch")
         + this.getSystemProperty("-", "sun.arch.data.model")
         + this.getSystemProperty(", instruction sets: ", "sun.cpu.isalist");
   }

   String getLocale() {
      return "default locale: " + Locale.getDefault() + ", platform encoding: " + this.getSystemProperty("file.encoding");
   }

   String getOperatingSystem() {
      return this.getSystemProperty("os.name")
         + " "
         + this.getSystemProperty("os.version")
         + this.getSystemProperty(" ", "sun.os.patch.level")
         + ", architecture: "
         + this.getSystemProperty("os.arch")
         + this.getSystemProperty("-", "sun.arch.data.model");
   }

   String getRuntime() {
      return this.getSystemProperty("java.runtime.name")
         + " (build "
         + this.getSystemProperty("java.runtime.version")
         + ") from "
         + this.getSystemProperty("java.vendor");
   }

   private String getSystemProperty(String name) {
      return StringLookupFactory.INSTANCE_SYSTEM_PROPERTIES.lookup(name);
   }

   private String getSystemProperty(String prefix, String name) {
      String value = this.getSystemProperty(name);
      return StringUtils.isEmpty(value) ? "" : prefix + value;
   }

   String getVirtualMachine() {
      return this.getSystemProperty("java.vm.name")
         + " (build "
         + this.getSystemProperty("java.vm.version")
         + ", "
         + this.getSystemProperty("java.vm.info")
         + ")";
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      switch (key) {
         case "version":
            return "Java version " + this.getSystemProperty("java.version");
         case "runtime":
            return this.getRuntime();
         case "vm":
            return this.getVirtualMachine();
         case "os":
            return this.getOperatingSystem();
         case "hardware":
            return this.getHardware();
         case "locale":
            return this.getLocale();
         default:
            throw new IllegalArgumentException(key);
      }
   }
}
