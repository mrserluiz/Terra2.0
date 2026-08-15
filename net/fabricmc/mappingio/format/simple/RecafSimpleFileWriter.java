package net.fabricmc.mappingio.format.simple;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingWriter;

public final class RecafSimpleFileWriter implements MappingWriter {
   private static final Set<MappingFlag> flags = EnumSet.of(MappingFlag.NEEDS_SRC_FIELD_DESC, MappingFlag.NEEDS_SRC_METHOD_DESC);
   private final Writer writer;
   private String classSrcName;
   private String memberSrcName;
   private String memberSrcDesc;
   private String dstName;

   public RecafSimpleFileWriter(Writer writer) {
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
   public boolean visitField(String srcName, String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      return true;
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, String srcName) throws IOException {
      return false;
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, String srcName) throws IOException {
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
      if (this.dstName == null) {
         return true;
      }

      this.write(this.classSrcName);
      if (targetKind != MappedElementKind.CLASS) {
         if (this.memberSrcName == null) {
            throw new IllegalArgumentException("member source name cannot be null!");
         }

         this.writer.write(46);
         this.write(this.memberSrcName);
         if (this.memberSrcDesc != null) {
            if (targetKind == MappedElementKind.FIELD) {
               this.writeSpace();
            }

            this.write(this.memberSrcDesc);
         }
      }

      this.writeSpace();
      this.write(this.dstName);
      this.writeLn();
      this.dstName = null;
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

   private void writeSpace() throws IOException {
      this.writer.write(32);
   }
}
