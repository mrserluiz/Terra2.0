package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class ConstantStringLookup extends AbstractStringLookup {
   private static final ConcurrentHashMap<String, String> CONSTANT_CACHE = new ConcurrentHashMap<>();
   private static final char FIELD_SEPARATOR = '.';
   static final ConstantStringLookup INSTANCE = new ConstantStringLookup();

   static void clear() {
      CONSTANT_CACHE.clear();
   }

   protected Class<?> fetchClass(String className) throws ClassNotFoundException {
      return ClassUtils.getClass(className);
   }

   @Override
   public synchronized String lookup(String key) {
      if (key == null) {
         return null;
      }

      String result = CONSTANT_CACHE.get(key);
      if (result != null) {
         return result;
      }

      int fieldPos = key.lastIndexOf(46);
      if (fieldPos < 0) {
         return null;
      }

      try {
         Object value = this.resolveField(key.substring(0, fieldPos), key.substring(fieldPos + 1));
         if (value != null) {
            String string = Objects.toString(value, null);
            CONSTANT_CACHE.put(key, string);
            result = string;
         }

         return result;
      } catch (Exception ex) {
         return null;
      }
   }

   protected Object resolveField(String className, String fieldName) throws ReflectiveOperationException {
      Class<?> clazz = this.fetchClass(className);
      return clazz == null ? null : clazz.getField(fieldName).get(null);
   }
}
