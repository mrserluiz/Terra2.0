package com.dfsek.terra.api.config;

import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.util.reflection.TypeKey;

public interface ConfigType<T extends AbstractableTemplate, R> {
   T getTemplate(ConfigPack var1, Platform var2);

   ConfigFactory<T, R> getFactory();

   TypeKey<R> getTypeKey();
}
