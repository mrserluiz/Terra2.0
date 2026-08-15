package com.dfsek.terra.lib.commons.lang3.text;

import com.dfsek.terra.lib.commons.lang3.SystemProperties;
import java.util.Map;
import java.util.Objects;

@Deprecated
public abstract class StrLookup<V> {
   private static final StrLookup<String> NONE_LOOKUP = new StrLookup.MapStrLookup<>(null);
   private static final StrLookup<String> SYSTEM_PROPERTIES_LOOKUP = new StrLookup.SystemPropertiesStrLookup();

   public static <V> StrLookup<V> mapLookup(Map<String, V> map) {
      return new StrLookup.MapStrLookup<>(map);
   }

   public static StrLookup<?> noneLookup() {
      return NONE_LOOKUP;
   }

   public static StrLookup<String> systemPropertiesLookup() {
      return SYSTEM_PROPERTIES_LOOKUP;
   }

   protected StrLookup() {
   }

   public abstract String lookup(String var1);

   static class MapStrLookup<V> extends StrLookup<V> {
      private final Map<String, V> map;

      MapStrLookup(Map<String, V> map) {
         this.map = map;
      }

      @Override
      public String lookup(String key) {
         return this.map == null ? null : Objects.toString(this.map.get(key), null);
      }
   }

   private static final class SystemPropertiesStrLookup extends StrLookup<String> {
      private SystemPropertiesStrLookup() {
      }

      @Override
      public String lookup(String key) {
         return SystemProperties.getProperty(key);
      }
   }
}
