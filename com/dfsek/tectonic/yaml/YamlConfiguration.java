package com.dfsek.tectonic.yaml;

import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.terra.lib.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class YamlConfiguration implements Configuration {
   private final Object config;
   private final String name;

   public YamlConfiguration(InputStream is) {
      this(is, null);
   }

   public YamlConfiguration(InputStream is, String name) {
      this.name = name;
      this.config = new Yaml().load(is);
   }

   public YamlConfiguration(String yaml) {
      this(yaml, null);
   }

   public YamlConfiguration(String yaml, String name) {
      this.name = name;
      this.config = new Yaml().load(yaml);
   }

   @Override
   public Object get(@NotNull String key) {
      String[] levels = key.split("\\.");
      Object level = this.config;

      for (String keyLevel : levels) {
         if (!(level instanceof Map)) {
            throw new IllegalArgumentException();
         }

         level = ((Map)level).get(keyLevel);
      }

      return level;
   }

   @Override
   public boolean contains(@NotNull String key) {
      String[] levels = key.split("\\.");
      Object level = this.config;

      for (String keyLevel : levels) {
         if (!(level instanceof Map)) {
            return false;
         }

         level = ((Map)level).get(keyLevel);
      }

      return level != null;
   }

   @Override
   public String getName() {
      return this.name;
   }
}
