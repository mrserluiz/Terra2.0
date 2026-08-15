package net.fabricmc.mappingio.format.enigma;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public final class EnigmaDirReader {
   private EnigmaDirReader() {
   }

   public static void read(Path dir, MappingVisitor visitor) throws IOException {
      read(dir, "source", "target", visitor);
   }

   public static void read(Path dir, final String sourceNs, final String targetNs, MappingVisitor visitor) throws IOException {
      if (!Files.exists(dir)) {
         throw new IOException("Directory does not exist: " + dir);
      }

      if (!Files.isDirectory(dir)) {
         throw new IOException("Not a directory: " + dir);
      }

      Set<MappingFlag> flags = visitor.getFlags();
      MappingVisitor parentVisitor = null;
      if (flags.contains(MappingFlag.NEEDS_ELEMENT_UNIQUENESS) || flags.contains(MappingFlag.NEEDS_MULTIPLE_PASSES)) {
         parentVisitor = visitor;
         visitor = new MemoryMappingTree();
      }

      if (visitor.visitHeader()) {
         visitor.visitNamespaces(sourceNs, Collections.singletonList(targetNs));
      }

      final MappingVisitor delegatingVisitor = new ForwardingMappingVisitor(visitor) {
         private boolean visitedContent;
         private boolean visitContent;

         @Override
         public boolean visitHeader() throws IOException {
            return false;
         }

         @Override
         public boolean visitContent() throws IOException {
            if (!this.visitedContent) {
               this.visitedContent = true;
               this.visitContent = super.visitContent();
            }

            return this.visitContent;
         }

         @Override
         public boolean visitEnd() throws IOException {
            return true;
         }
      };
      Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().endsWith("." + MappingFormat.ENIGMA_FILE.fileExt)) {
               EnigmaFileReader.read(Files.newBufferedReader(file), sourceNs, targetNs, delegatingVisitor);
            }

            return FileVisitResult.CONTINUE;
         }
      });
      if (!visitor.visitEnd() || parentVisitor != null) {
         if (parentVisitor == null) {
            throw new IllegalStateException("repeated visitation requested without NEEDS_MULTIPLE_PASSES");
         }

         ((MappingTree)visitor).accept(parentVisitor);
      }
   }
}
