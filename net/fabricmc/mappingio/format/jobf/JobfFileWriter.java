package net.fabricmc.mappingio.format.jobf;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingWriter;
import org.jetbrains.annotations.Nullable;

public final class JobfFileWriter implements MappingWriter {
   private static final Set<MappingFlag> flags = EnumSet.of(MappingFlag.NEEDS_SRC_FIELD_DESC, MappingFlag.NEEDS_SRC_METHOD_DESC);
   private final Writer writer;
   private String classSrcName;
   private String memberSrcName;
   private String memberSrcDesc;
   private String dstName;

   public JobfFileWriter(Writer writer) {
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
      this.dstName = null;
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      this.dstName = null;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      this.dstName = null;
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
      boolean isClass = targetKind == MappedElementKind.CLASS;
      boolean isField = false;
      if (this.dstName == null) {
         return isClass;
      }

      if (isClass) {
         int srcNameLastSep = this.classSrcName.lastIndexOf(47);
         int dstNameLastSep = this.dstName.lastIndexOf(47);
         String srcPkg = this.classSrcName.substring(0, srcNameLastSep + 1);
         String dstPkg = this.dstName.substring(0, dstNameLastSep + 1);
         this.classSrcName = this.classSrcName.replace('/', '.');
         if (!srcPkg.equals(dstPkg)) {
            return true;
         }

         this.dstName = this.dstName.substring(dstNameLastSep + 1);
         this.write("c ");
      } else {
         if (!(isField = targetKind == MappedElementKind.FIELD) && targetKind != MappedElementKind.METHOD) {
            throw new IllegalStateException("unexpected invocation for " + targetKind);
         }

         if (this.memberSrcDesc == null) {
            return false;
         }

         this.write(isField ? "f " : "m ");
      }

      this.write(this.classSrcName);
      if (!isClass) {
         this.write(".");
         this.write(this.memberSrcName);
         if (isField) {
            this.write(":");
         }

         this.write(this.memberSrcDesc);
      }

      this.write(" = ");
      this.write(this.dstName);
      this.writeLn();
      return isClass;
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
}
