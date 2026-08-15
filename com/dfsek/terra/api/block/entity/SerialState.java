package com.dfsek.terra.api.block.entity;

import java.util.HashMap;
import java.util.Map;

public class SerialState {
   protected final Map<String, SerialState.Property<?>> properties = new HashMap<>();

   public static Map<String, String> parse(String props) {
      String[] sep = props.split(",");
      Map<String, String> map = new HashMap<>();

      for (String item : sep) {
         map.put(item.substring(0, item.indexOf(61)), item.substring(item.indexOf(61) + 1));
      }

      return map;
   }

   public <T> T get(String id, Class<T> clazz) {
      this.checkExists(id);
      SerialState.Property<?> prop = this.properties.get(id);
      this.checkType(clazz, prop.getValue(), id);
      return (T)prop.getValue();
   }

   private void checkExists(String prop) {
      if (!this.properties.containsKey(prop)) {
         throw new IllegalArgumentException("No such property \"" + prop + "\"");
      }
   }

   private void checkType(Class<?> clazz, Object o, String id) {
      if (!clazz.isInstance(o)) {
         throw new IllegalArgumentException("Invalid data for property " + id + ": " + o);
      }
   }

   public void setProperty(String id, Object value) {
      this.checkExists(id);
      SerialState.Property<?> prop = this.properties.get(id);
      this.checkType(prop.getValueClass(), value, id);
      prop.setValue(value);
   }

   public int getInteger(String id) {
      this.checkExists(id);
      SerialState.Property<?> prop = this.properties.get(id);
      this.checkType(Integer.class, prop.getValue(), id);
      return (Integer)prop.getValue();
   }

   public String getString(String id) {
      this.checkExists(id);
      SerialState.Property<?> prop = this.properties.get(id);
      this.checkType(String.class, prop.getValue(), id);
      return (String)prop.getValue();
   }

   public long getLong(String id) {
      this.checkExists(id);
      SerialState.Property<?> prop = this.properties.get(id);
      this.checkType(Long.class, prop.getValue(), id);
      return (Long)prop.getValue();
   }

   public boolean getBoolean(String id) {
      this.checkExists(id);
      SerialState.Property<?> prop = this.properties.get(id);
      this.checkType(Boolean.class, prop.getValue(), id);
      return (Boolean)prop.getValue();
   }

   protected static class Property<T> {
      private final Class<T> clazz;
      private Object value;

      public Property(Class<T> clazz) {
         this.clazz = clazz;
      }

      public Class<T> getValueClass() {
         return this.clazz;
      }

      public T getValue() {
         return (T)this.value;
      }

      public void setValue(Object value) {
         this.value = value;
      }
   }
}
