package com.dfsek.terra.addon;

import ca.solostudios.strata.Versions;
import ca.solostudios.strata.version.Version;
import com.dfsek.terra.addon.exception.AddonLoadException;
import com.dfsek.terra.api.addon.bootstrap.BootstrapAddonClassLoader;
import com.dfsek.terra.api.addon.bootstrap.BootstrapBaseAddon;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapAddonLoader implements BootstrapBaseAddon<BootstrapBaseAddon<?>> {
   private static final Logger logger = LoggerFactory.getLogger(BootstrapAddonLoader.class);
   private static final Version VERSION = Versions.getVersion(1, 0, 0);

   private BootstrapBaseAddon<?> loadAddon(Path addonPath, BootstrapAddonClassLoader parent) {
      logger.debug("Loading bootstrap addon from JAR {}", addonPath);

      try (JarFile jar = new JarFile(addonPath.toFile())) {
         String entry = jar.getManifest().getMainAttributes().getValue("Terra-Bootstrap-Addon-Entry-Point");
         if (entry == null) {
            throw new AddonLoadException("No Terra-Bootstrap-Addon-Entry-Point attribute defined in addon's MANIFEST.MF.");
         }

         try {
            parent.addURL(addonPath.toUri().toURL());
            Object addonObject = parent.loadClass(entry).getConstructor().newInstance();
            if (!(addonObject instanceof BootstrapBaseAddon<?> addon)) {
               throw new AddonLoadException(addonObject.getClass() + " does not extend " + BootstrapBaseAddon.class);
            } else {
               logger.debug(
                  "Loaded bootstrap addon {}@{} with entry point {}", new Object[]{addon.getID(), addon.getVersion().getFormatted(), addonObject.getClass()}
               );
               return addon;
            }
         } catch (InvocationTargetException e) {
            throw new AddonLoadException("Exception occurred while instantiating addon", e);
         } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new AddonLoadException(String.format("No valid default constructor found in entry point %s", entry), e);
         } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new AddonLoadException(String.format("Entry point %s not found in JAR.", entry), e);
         }
      } catch (IOException e) {
         throw new AddonLoadException("Failed to load addon from path " + addonPath, e);
      }
   }

   @Override
   public Iterable<BootstrapBaseAddon<?>> loadAddons(Path addonsFolder, BootstrapAddonClassLoader parent) {
      try {
         Path bootstrapFolder = addonsFolder.resolve("bootstrap");
         Files.createDirectories(bootstrapFolder);
         logger.debug("Loading bootstrap addons from {}", bootstrapFolder);

         try (Stream<Path> bootstrapAddons = Files.walk(bootstrapFolder, 1, FileVisitOption.FOLLOW_LINKS)) {
            return bootstrapAddons.filter(path -> path.toFile().isFile())
               .filter(path -> path.toFile().canRead())
               .filter(path -> path.toString().endsWith(".jar"))
               .map(path -> this.loadAddon(path, parent))
               .collect(Collectors.toList());
         }
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public String getID() {
      return "BOOTSTRAP";
   }

   @Override
   public Version getVersion() {
      return VERSION;
   }
}
