package net.fabricmc.mappingio.format.srg;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingWriter;
import org.jetbrains.annotations.Nullable;

public final class CsrgFileWriter implements MappingWriter {
   private static final Set<MappingFlag> flags = EnumSet.of(MappingFlag.NEEDS_SRC_METHOD_DESC);
   private final Writer writer;
   private String classSrcName;
   private String memberSrcName;
   private String methodSrcDesc;
   private String dstName;

   public CsrgFileWriter(Writer writer) {
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
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.classSrcName = srcName;
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.methodSrcDesc = srcDesc;
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
      if (namespace == 0) {
         this.dstName = name;
      }
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      if (this.dstName != null) {
         this.write(this.classSrcName);
         if (targetKind != MappedElementKind.CLASS) {
            this.writeSpace();
            this.write(this.memberSrcName);
            if (targetKind == MappedElementKind.METHOD) {
               this.writeSpace();
               this.write(this.methodSrcDesc);
            }

            this.memberSrcName = this.methodSrcDesc = null;
         }

         this.writeSpace();
         this.write(this.dstName);
         this.writeLn();
         this.dstName = null;
      }

      return targetKind == MappedElementKind.CLASS;
   }

   @Override
   public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
   }

   private void write(String str) throws IOException {
      this.writer.write(str);
   }

   private void writeSpace() throws IOException {
      this.writer.write(32);
   }

   private void writeLn() throws IOException {
      this.writer.write(10);
   }
}
