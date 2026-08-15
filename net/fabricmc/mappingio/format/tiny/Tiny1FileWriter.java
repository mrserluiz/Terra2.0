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

public final class Tiny1FileWriter implements MappingWriter {
   private static final Set<MappingFlag> flags = EnumSet.of(MappingFlag.NEEDS_SRC_FIELD_DESC, MappingFlag.NEEDS_SRC_METHOD_DESC);
   private final Writer writer;
   private String classSrcName;
   private String memberSrcName;
   private String memberSrcDesc;
   private String[] dstNames;

   public Tiny1FileWriter(Writer writer) {
      this.writer = writer;
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
      this.write("v1\t");
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
         case "next-intermediary-class":
         case "next-intermediary-field":
         case "next-intermediary-method":
            this.write("# INTERMEDIARY-COUNTER ");
            switch (key) {
               case "next-intermediary-class":
                  this.write("class");
                  break;
               case "next-intermediary-field":
                  this.write("field");
                  break;
               case "next-intermediary-method":
                  this.write("method");
                  break;
               default:
                  throw new IllegalStateException();
            }

            this.write(" ");
            this.write(value);
            this.writeLn();
      }
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.classSrcName = srcName;
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      return true;
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
      return false;
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) throws IOException {
      return false;
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) {
      this.dstNames[namespace] = name;
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      boolean found = false;

      for (String dstName : this.dstNames) {
         if (dstName != null) {
            found = true;
            break;
         }
      }

      if (!found) {
         return true;
      }

      switch (targetKind) {
         case CLASS:
            this.write("CLASS");
            break;
         case FIELD:
            this.write("FIELD");
            break;
         case METHOD:
            this.write("METHOD");
            break;
         default:
            throw new IllegalStateException("unexpected invocation for " + targetKind);
      }

      this.writeTab();
      this.write(this.classSrcName);
      if (targetKind != MappedElementKind.CLASS) {
         this.writeTab();
         this.write(this.memberSrcDesc);
         this.writeTab();
         this.write(this.memberSrcName);
      }

      for (String dstName : this.dstNames) {
         this.writeTab();
         if (dstName != null) {
            this.write(dstName);
         }
      }

      this.writeLn();
      Arrays.fill(this.dstNames, null);
      return targetKind == MappedElementKind.CLASS;
   }

   @Override
   public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
   }

   private void write(String str) throws IOException {
      this.writer.write(str);
   }

   private void writeLn() throws IOException {
      this.writer.write(10);
   }

   private void writeTab() throws IOException {
      this.writer.write(9);
   }
}
