package com.dfsek.terra.config.fileloaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderLoader extends LoaderImpl {
   private static final Logger logger = LoggerFactory.getLogger(FolderLoader.class);
   private final Path path;

   public FolderLoader(Path path) {
      this.path = path;
   }

   @Override
   public InputStream get(String singleFile) throws IOException {
      return new FileInputStream(new File(this.path.toFile(), singleFile));
   }

   @Override
   protected void load(String directory, String extension) {
      File newPath = new File(this.path.toFile(), directory);
      newPath.mkdirs();

      try (Stream<Path> paths = Files.walk(newPath.toPath())) {
         paths.filter(x$0 -> Files.isRegularFile(x$0)).filter(file -> file.toString().toLowerCase().endsWith(extension)).forEach(file -> {
            try {
               String rel = newPath.toPath().relativize(file).toString();
               this.streams.put(rel, new FileInputStream(file.toFile()));
            } catch (FileNotFoundException e) {
               logger.error("Could not find file to load", ex);
            }
         });
      } catch (IOException e) {
         logger.error("Error while loading files", e);
      }
   }
}
