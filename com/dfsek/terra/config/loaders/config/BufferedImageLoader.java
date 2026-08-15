package com.dfsek.terra.config.loaders.config;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.config.Loader;
import com.dfsek.terra.api.properties.Properties;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.AnnotatedType;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class BufferedImageLoader implements TypeLoader<BufferedImage> {
   private final Loader files;
   private final ConfigPack pack;

   public BufferedImageLoader(Loader files, ConfigPack pack) {
      this.files = files;
      this.pack = pack;
      if (!pack.getContext().has(BufferedImageLoader.ImageCache.class)) {
         pack.getContext().put(new BufferedImageLoader.ImageCache(new ConcurrentHashMap<>()));
      }
   }

   public BufferedImage load(@NotNull AnnotatedType t, @NotNull Object c, @NotNull ConfigLoader loader, DepthTracker depthTracker) throws LoadException {
      return this.pack.getContext().get(BufferedImageLoader.ImageCache.class).map.computeIfAbsent((String)c, s -> {
         try {
            return ImageIO.read(this.files.get(s));
         } catch (IOException e) {
            throw new LoadException("Unable to load image", e, depthTracker);
         }
      });
   }

   private record ImageCache(ConcurrentHashMap<String, BufferedImage> map) implements Properties {
   }
}
