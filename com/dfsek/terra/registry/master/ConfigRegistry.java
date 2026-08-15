package com.dfsek.terra.registry.master;

import com.dfsek.tectonic.api.exception.ConfigException;
import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.util.reflection.TypeKey;
import com.dfsek.terra.config.pack.ConfigPackImpl;
import com.dfsek.terra.registry.OpenRegistryImpl;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigRegistry extends OpenRegistryImpl<ConfigPack> {
   private static final Logger logger = LoggerFactory.getLogger(ConfigRegistry.class);

   public ConfigRegistry() {
      super(TypeKey.of(ConfigPack.class));
   }

   public void load(File folder, Platform platform) throws ConfigException {
      ConfigPack pack = new ConfigPackImpl(folder, platform);
      this.registerChecked(pack.getRegistryKey(), pack);
   }

   public boolean loadAll(Platform platform) {
      boolean valid = true;
      File packsFolder = new File(platform.getDataFolder(), "packs");
      packsFolder.mkdirs();

      for (File dir : Objects.requireNonNull(packsFolder.listFiles(File::isDirectory))) {
         try {
            this.load(dir, platform);
         } catch (RuntimeException e) {
            logger.error("Error loading config pack {}", dir.getName(), e);
            valid = false;
         }
      }

      for (File zip : Objects.requireNonNull(packsFolder.listFiles(file -> file.getName().endsWith(".zip") || file.getName().endsWith(".terra")))) {
         try {
            logger.info("Loading ZIP archive: {}", zip.getName());
            this.load(new ZipFile(zip), platform);
         } catch (IOException | RuntimeException e) {
            logger.error("Error loading config pack {}", zip.getName(), e);
            valid = false;
         }
      }

      return valid;
   }

   public void load(ZipFile file, Platform platform) throws ConfigException {
      ConfigPackImpl pack = new ConfigPackImpl(file, platform);
      this.registerChecked(pack.getRegistryKey(), pack);
   }
}
