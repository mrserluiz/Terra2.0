package net.fabricmc.mappingio.format.enigma;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.format.MappingFormat;

public final class EnigmaDirWriter extends EnigmaWriterBase {
   private final Path dir;
   private final boolean deleteExistingFiles;

   public EnigmaDirWriter(Path dir, boolean deleteExistingFiles) throws IOException {
      super(null);
      this.dir = dir.toAbsolutePath().normalize();
      this.deleteExistingFiles = deleteExistingFiles;
   }

   @Override
   public boolean visitHeader() throws IOException {
      if (this.deleteExistingFiles && Files.exists(this.dir)) {
         Files.walkFileTree(this.dir, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
               if (file.getFileName().toString().endsWith("." + MappingFormat.ENIGMA_FILE.fileExt)) {
                  Files.delete(file);
               }

               return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path file, IOException exc) throws IOException {
               try {
                  if (!EnigmaDirWriter.this.dir.equals(file)) {
                     Files.delete(file);
                  }
               } catch (DirectoryNotEmptyException var4) {
               }

               return FileVisitResult.CONTINUE;
            }
         });
      }

      return super.visitHeader();
   }

   @Override
   public void close() throws IOException {
      if (this.writer != null) {
         this.writer.close();
         this.writer = null;
         this.currentClass = null;
      }
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      if (targetKind == MappedElementKind.CLASS) {
         String name = this.dstName != null ? this.dstName : this.srcClassName;
         if (this.currentClass == null
            || !name.startsWith(this.currentClass)
            || name.length() > this.currentClass.length() && name.charAt(this.currentClass.length()) != '$') {
            int pos = getNextOuterEnd(name, 0);
            if (pos >= 0) {
               name = name.substring(0, pos);
            }

            Path file = this.dir.resolve(name + "." + MappingFormat.ENIGMA_FILE.fileExt).normalize();
            if (!file.startsWith(this.dir)) {
               throw new RuntimeException("invalid name: " + name);
            }

            if (this.writer != null) {
               this.writer.close();
            }

            this.currentClass = name;
            if (!Files.exists(file)) {
               this.lastWrittenClass = "";
               Files.createDirectories(file.getParent());
            } else {
               List<String> writtenClassParts = new ArrayList<>();
               BufferedReader reader = Files.newBufferedReader(file);

               String line;
               try {
                  while ((line = reader.readLine()) != null) {
                     int offset = 0;

                     while (offset < line.length() && line.charAt(offset) == '\t') {
                        offset++;
                     }

                     if (line.startsWith("CLASS ", offset)) {
                        int start = offset + 6;
                        int end = line.indexOf(32, start);
                        if (end < 0) {
                           end = line.length();
                        }

                        String part = line.substring(start, end);

                        while (writtenClassParts.size() > offset) {
                           writtenClassParts.remove(writtenClassParts.size() - 1);
                        }

                        writtenClassParts.add(part);
                     }
                  }
               } catch (Throwable var13) {
                  if (reader != null) {
                     try {
                        reader.close();
                     } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                     }
                  }

                  throw var13;
               }

               if (reader != null) {
                  reader.close();
               }

               this.lastWrittenClass = String.join("$", writtenClassParts);
            }

            this.writer = Files.newBufferedWriter(file, StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
         }

         this.writeMismatchedOrMissingClasses();
      } else if (targetKind != MappedElementKind.FIELD && targetKind != MappedElementKind.METHOD) {
         this.writer.write(10);
      } else {
         this.writer.write(32);
         this.writer.write(this.desc);
         this.writer.write(10);
      }

      return true;
   }
}
