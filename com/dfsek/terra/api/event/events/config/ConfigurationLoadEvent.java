package com.dfsek.terra.api.event.events.config;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.impl.abstraction.AbstractConfiguration;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.config.ConfigType;
import com.dfsek.terra.api.event.events.FailThroughEvent;
import com.dfsek.terra.api.event.events.PackEvent;
import com.dfsek.terra.api.util.reflection.ReflectionUtil;
import java.util.function.Consumer;

public class ConfigurationLoadEvent implements PackEvent, FailThroughEvent {
   private final ConfigPack pack;
   private final AbstractConfiguration configuration;
   private final Consumer<ConfigTemplate> loader;
   private final ConfigType<?, ?> type;
   private final Object loaded;

   public ConfigurationLoadEvent(ConfigPack pack, AbstractConfiguration configuration, Consumer<ConfigTemplate> loader, ConfigType<?, ?> type, Object loaded) {
      this.pack = pack;
      this.configuration = configuration;
      this.loader = loader;
      this.type = type;
      this.loaded = loaded;
   }

   public <T extends ConfigTemplate> T load(T template) {
      this.loader.accept(template);
      return template;
   }

   public boolean is(Class<?> clazz) {
      return clazz.isAssignableFrom(this.type.getTypeKey().getRawType());
   }

   @Override
   public ConfigPack getPack() {
      return this.pack;
   }

   public AbstractConfiguration getConfiguration() {
      return this.configuration;
   }

   public ConfigType<?, ?> getType() {
      return this.type;
   }

   public <T> T getLoadedObject(Class<T> clazz) {
      if (!clazz.isAssignableFrom(this.type.getTypeKey().getRawType())) {
         throw new ClassCastException(
            "Cannot assign object from loader of type "
               + ReflectionUtil.typeToString(this.type.getTypeKey().getType())
               + " to class "
               + clazz.getCanonicalName()
         );
      } else {
         return (T)this.loaded;
      }
   }

   public <T> T getLoadedObject() {
      return (T)this.loaded;
   }
}
