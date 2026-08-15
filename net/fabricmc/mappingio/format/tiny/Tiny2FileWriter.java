package net.fabricmc.mappingio.format.tiny;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingWriter;
import org.jetbrains.annotations.Nullable;

public final class Tiny2FileWriter implements MappingWriter {
   private static final Set<MappingFlag> flags = EnumSet.of(
      MappingFlag.NEEDS_HEADER_METADATA,
      MappingFlag.NEEDS_METADATA_UNIQUENESS,
      MappingFlag.NEEDS_ELEMENT_UNIQUENESS,
      MappingFlag.NEEDS_SRC_FIELD_DESC,
      MappingFlag.NEEDS_SRC_METHOD_DESC
   );
   private final Writer writer;
   private boolean escapeNames;
   private boolean wroteEscapedNamesProperty;
   private String[] dstNames;

   public Tiny2FileWriter(Writer writer, boolean escapeNames) {
      this.writer = writer;
      this.escapeNames = escapeNames;
   }

   @Override
   public void close() throws IOException {
      this.writer.close();
   }

   @Override
   public Set<MappingFlag> getFlags() {
      return flags;
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
      this.dstNames = new String[dstNamespaces.size()];
      this.write("tiny\t2\t0\t");
      this.write(srcNamespace);

      for (String dstNamespace : dstNamespaces) {
         this.writeTab();
         this.write(dstNamespace);
      }

      this.writeLn();
   }

   @Override
   public void visitMetadata(String key, @Nullable String value) throws IOException {
      switch (key) {
         case "escaped-names":
            this.escapeNames = true;
            this.wroteEscapedNamesProperty = true;
         default:
            this.writeTab();
            this.write(key);
            if (value != null) {
               this.writeTab();
               this.write(value);
            }

            this.writeLn();
            return;
         case "migrationmap:order":
      }
   }

   @Override
   public boolean visitContent() throws IOException {
      if (this.escapeNames && !this.wroteEscapedNamesProperty) {
         this.write("\t");
         this.write("escaped-names");
         this.writeLn();
      }

      return true;
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.write("c\t");
      this.writeName(srcName);
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.write("\tf\t");
      this.writeName(srcDesc);
      this.writeTab();
      this.writeName(srcName);
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.write("\tm\t");
      this.writeName(srcDesc);
      this.writeTab();
      this.writeName(srcName);
      return true;
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
      this.write("\t\tp\t");
      this.write(lvIndex);
      this.writeTab();
      if (srcName != null) {
         this.writeName(srcName);
      }

      return true;
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) throws IOException {
      this.write("\t\tv\t");
      this.write(lvIndex);
      this.writeTab();
      this.write(startOpIdx);
      this.writeTab();
      this.write(Math.max(lvtRowIndex, -1));
      this.writeTab();
      if (srcName != null) {
         this.writeName(srcName);
      }

      return true;
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) {
      this.dstNames[namespace] = name;
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      for (String dstName : this.dstNames) {
         this.writeTab();
         if (dstName != null) {
            this.writeName(dstName);
         }
      }

      this.writeLn();
      Arrays.fill(this.dstNames, null);
      return true;
   }

   @Override
   public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
      this.writeTabs(targetKind.level);
      this.write("\tc\t");
      this.writeEscaped(comment);
      this.writeLn();
   }

   private void write(String str) throws IOException {
      this.writer.write(str);
   }

   private void write(int i) throws IOException {
      this.write(Integer.toString(i));
   }

   private void writeEscaped(String str) throws IOException {
      Tiny2Util.writeEscaped(str, this.writer);
   }

   private void writeName(String str) throws IOException {
      if (this.escapeNames) {
         this.writeEscaped(str);
      } else {
         this.write(str);
      }
   }

   private void writeLn() throws IOException {
      this.writer.write(10);
   }

   private void writeTab() throws IOException {
      this.writer.write(9);
   }

   private void writeTabs(int count) throws IOException {
      for (int i = 0; i < count; i++) {
         this.writer.write(9);
      }
   }
}
