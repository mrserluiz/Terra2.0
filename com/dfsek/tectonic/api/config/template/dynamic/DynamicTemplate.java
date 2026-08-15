package com.dfsek.tectonic.api.config.template.dynamic;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.loader.TemplateLoader;
import com.dfsek.tectonic.impl.annotations.Generated;
import java.util.HashMap;
import java.util.Map;

public final class DynamicTemplate implements ConfigTemplate {
   private final Map<String, DynamicValue<?>> values;
   @Generated
   private final Map<String, ?> computed = new HashMap<>();

   @Override
   public TemplateLoader loader() {
      return new DynamicTemplateLoader();
   }

   private DynamicTemplate(Map<String, DynamicValue<?>> values) {
      this.values = values;
   }

   public <T> T get(String value, Class<T> type) {
      DynamicValue<T> dynamicValue = (DynamicValue<T>)this.values.get(value);
      if (!type.isAssignableFrom(dynamicValue.getType())) {
         throw new ClassCastException("Cannot assign " + dynamicValue.getType().getCanonicalName() + " to " + type.getCanonicalName());
      } else {
         return (T)this.computed.get(value);
      }
   }

   Map<String, ?> getComputed() {
      return this.computed;
   }

   Map<String, DynamicValue<?>> getValues() {
      return this.values;
   }

   public static DynamicTemplate.Builder builder() {
      return new DynamicTemplate.Builder();
   }

   public static final class Builder {
      private final Map<String, DynamicValue<?>> values = new HashMap<>();

      private Builder() {
      }

      public <T> DynamicTemplate.Builder value(String id, DynamicValue<T> value) {
         if (this.values.containsKey(id)) {
            throw new IllegalArgumentException("Value for id " + id + " already defined at key " + this.values.get(id).getKey());
         }

         this.values.put(id, value);
         return this;
      }

      public DynamicTemplate build() {
         return new DynamicTemplate(this.values);
      }
   }
}
