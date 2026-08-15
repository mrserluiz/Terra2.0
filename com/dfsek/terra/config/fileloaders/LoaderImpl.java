package com.dfsek.terra.config.fileloaders;

import com.dfsek.tectonic.api.exception.ConfigException;
import com.dfsek.terra.api.config.Loader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoaderImpl implements Loader {
   private static final Logger logger = LoggerFactory.getLogger(LoaderImpl.class);
   protected final Map<String, InputStream> streams = new HashMap<>();

   @Override
   public Loader thenNames(Consumer<List<String>> consumer) throws ConfigException {
      consumer.accept(new ArrayList<>(this.streams.keySet()));
      return this;
   }

   @Override
   public Loader thenEntries(Consumer<Set<Entry<String, InputStream>>> consumer) throws ConfigException {
      consumer.accept(this.streams.entrySet());
      return this;
   }

   public LoaderImpl open(String directory, String extension) {
      if (!this.streams.isEmpty()) {
         throw new IllegalStateException("Attempted to load new directory before closing existing InputStreams");
      }

      this.load(directory, extension);
      return this;
   }

   @Override
   public Loader close() {
      this.streams.forEach((name, input) -> {
         try {
            input.close();
         } catch (IOException e) {
            logger.error("Error occurred while loading", e);
         }
      });
      this.streams.clear();
      return this;
   }

   protected abstract void load(String var1, String var2);
}
