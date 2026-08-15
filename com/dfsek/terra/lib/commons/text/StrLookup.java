package com.dfsek.terra.lib.commons.text;

import com.dfsek.terra.lib.commons.text.lookup.StringLookup;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

@Deprecated
public abstract class StrLookup<V> implements StringLookup {
   private static final StrLookup<String> NONE_LOOKUP = new StrLookup.MapStrLookup<>(null);
   private static final StrLookup<String> SYSTEM_PROPERTIES_LOOKUP = new StrLookup.SystemPropertiesStrLookup();

   public static <V> StrLookup<V> mapLookup(Map<String, V> map) {
      return new StrLookup.MapStrLookup<>(map);
   }

   public static StrLookup<?> noneLookup() {
      return NONE_LOOKUP;
   }

   public static StrLookup<String> resourceBundleLookup(ResourceBundle resourceBundle) {
      return new StrLookup.ResourceBundleLookup(resourceBundle);
   }

   public static StrLookup<String> systemPropertiesLookup() {
      return SYSTEM_PROPERTIES_LOOKUP;
   }

   protected StrLookup() {
   }

   private static final class MapStrLookup<V> extends StrLookup<V> {
      private final Map<String, V> map;

      private MapStrLookup(Map<String, V> map) {
         this.map = map != null ? map : Collections.emptyMap();
      }

      @Override
      public String lookup(String key) {
         return Objects.toString(this.map.get(key), null);
      }

      @Override
      public String toString() {
         return super.toString() + " [map=" + this.map + "]";
      }
   }

   private static final class ResourceBundleLookup extends StrLookup<String> {
      private final ResourceBundle resourceBundle;

      private ResourceBundleLookup(ResourceBundle resourceBundle) {
         this.resourceBundle = resourceBundle;
      }

      @Override
      public String lookup(String key) {
         return this.resourceBundle != null && key != null && this.resourceBundle.containsKey(key) ? this.resourceBundle.getString(key) : null;
      }

      @Override
      public String toString() {
         return super.toString() + " [resourceBundle=" + this.resourceBundle + "]";
      }
   }

   private static final class SystemPropertiesStrLookup extends StrLookup<String> {
      private SystemPropertiesStrLookup() {
      }

      @Override
      public String lookup(String key) {
         if (!key.isEmpty()) {
            try {
               return System.getProperty(key);
            } catch (SecurityException var3) {
            }
         }

         return null;
      }
   }
}
