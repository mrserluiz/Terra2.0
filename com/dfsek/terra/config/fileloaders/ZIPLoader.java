package com.dfsek.terra.config.fileloaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZIPLoader extends LoaderImpl {
   private static final Logger logger = LoggerFactory.getLogger(ZIPLoader.class);
   private final ZipFile file;

   public ZIPLoader(ZipFile file) {
      this.file = file;
   }

   @Override
   public InputStream get(String singleFile) throws IOException {
      Enumeration<? extends ZipEntry> entries = this.file.entries();

      while (entries.hasMoreElements()) {
         ZipEntry entry = entries.nextElement();
         if (!entry.isDirectory() && entry.getName().equals(singleFile)) {
            return this.file.getInputStream(entry);
         }
      }

      throw new IllegalArgumentException("No such file: " + singleFile);
   }

   @Override
   protected void load(String directory, String extension) {
      Enumeration<? extends ZipEntry> entries = this.file.entries();

      while (entries.hasMoreElements()) {
         ZipEntry entry = entries.nextElement();
         if (!entry.isDirectory() && entry.getName().startsWith(directory) && entry.getName().endsWith(extension)) {
            try {
               String rel = entry.getName().substring(directory.length());
               this.streams.put(rel, this.file.getInputStream(entry));
            } catch (IOException e) {
               logger.error("Error while loading file from zip", e);
            }
         }
      }
   }
}
