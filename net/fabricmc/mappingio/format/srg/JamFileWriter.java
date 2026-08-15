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

public final class JamFileWriter implements MappingWriter {
   private static final Set<MappingFlag> flags = EnumSet.of(
      MappingFlag.NEEDS_SRC_FIELD_DESC, MappingFlag.NEEDS_SRC_METHOD_DESC, MappingFlag.NEEDS_MULTIPLE_PASSES
   );
   private final Writer writer;
   private boolean classOnlyPass = true;
   private String classSrcName;
   private String memberSrcName;
   private String memberSrcDesc;
   private int argSrcPosition = -1;
   private String classDstName;
   private String memberDstName;
   private String argDstName;

   public JamFileWriter(Writer writer) {
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
      this.classDstName = null;
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      this.memberDstName = null;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      this.memberDstName = null;
      return true;
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
      this.argSrcPosition = argPosition;
      this.argDstName = null;
      return true;
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) throws IOException {
      return false;
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) {
      if (namespace == 0) {
         switch (targetKind) {
            case CLASS:
               this.classDstName = name;
               break;
            case FIELD:
            case METHOD:
               this.memberDstName = name;
               break;
            case METHOD_ARG:
               this.argDstName = name;
               break;
            default:
               throw new IllegalStateException("unexpected invocation for " + targetKind);
         }
      }
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      boolean isClass = targetKind == MappedElementKind.CLASS;
      boolean isMethod = false;
      boolean isArg = false;
      if (isClass) {
         if (!this.classOnlyPass || this.classDstName == null) {
            return true;
         }

         this.write("CL ");
      } else {
         if (targetKind != MappedElementKind.FIELD
            && !(isMethod = targetKind == MappedElementKind.METHOD)
            && !(isArg = targetKind == MappedElementKind.METHOD_ARG)) {
            throw new IllegalStateException("unexpected invocation for " + targetKind);
         }

         if (this.classOnlyPass || this.memberSrcDesc == null) {
            return false;
         }

         if (!isArg && this.memberDstName == null) {
            return isMethod;
         }

         if (isMethod) {
            this.write("MD ");
         } else if (!isArg) {
            this.write("FD ");
         } else {
            if (this.argSrcPosition == -1 || this.argDstName == null) {
               return false;
            }

            this.write("MP ");
         }
      }

      this.write(this.classSrcName);
      this.writeSpace();
      if (isClass) {
         this.write(this.classDstName);
      } else {
         this.write(this.memberSrcName);
         this.writeSpace();
         this.write(this.memberSrcDesc);
         this.writeSpace();
         if (!isArg) {
            this.write(this.memberDstName);
         } else {
            this.write(Integer.toString(this.argSrcPosition));
            this.writeSpace();
            this.write(this.argDstName);
         }
      }

      this.writeLn();
      return isClass || isMethod;
   }

   @Override
   public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
   }

   @Override
   public boolean visitEnd() throws IOException {
      if (this.classOnlyPass) {
         this.classOnlyPass = false;
         return false;
      } else {
         this.classOnlyPass = true;
         return MappingWriter.super.visitEnd();
      }
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
