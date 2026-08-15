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

public final class SrgFileWriter implements MappingWriter {
   private static final Set<MappingFlag> srgFlags = EnumSet.of(MappingFlag.NEEDS_SRC_METHOD_DESC, MappingFlag.NEEDS_DST_METHOD_DESC);
   private static final Set<MappingFlag> xsrgFlags = EnumSet.copyOf(srgFlags);
   private final Writer writer;
   private final boolean xsrg;
   private String classSrcName;
   private String memberSrcName;
   private String memberSrcDesc;
   private String classDstName;
   private String memberDstName;
   private String memberDstDesc;

   public SrgFileWriter(Writer writer, boolean xsrg) {
      this.writer = writer;
      this.xsrg = xsrg;
   }

   @Override
   public void close() throws IOException {
      this.writer.close();
   }

   @Override
   public Set<MappingFlag> getFlags() {
      return this.xsrg ? xsrgFlags : srgFlags;
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
      this.memberDstDesc = null;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      this.memberDstName = null;
      this.memberDstDesc = null;
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
         switch (targetKind) {
            case CLASS:
               this.classDstName = name;
               break;
            case FIELD:
            case METHOD:
               this.memberDstName = name;
               break;
            default:
               throw new IllegalStateException("unexpected invocation for " + targetKind);
         }
      }
   }

   @Override
   public void visitDstDesc(MappedElementKind targetKind, int namespace, String desc) throws IOException {
      if (namespace == 0) {
         this.memberDstDesc = desc;
      }
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      switch (targetKind) {
         case CLASS:
            if (this.classDstName == null) {
               return true;
            }

            this.write("CL: ");
            break;
         case FIELD:
            if (this.memberSrcDesc == null || this.memberDstName == null || this.xsrg && this.memberDstDesc == null) {
               return false;
            }

            this.write("FD: ");
            break;
         case METHOD:
            if (this.memberSrcDesc == null || this.memberDstName == null || this.memberDstDesc == null) {
               return false;
            }

            this.write("MD: ");
            break;
         default:
            throw new IllegalStateException("unexpected invocation for " + targetKind);
      }

      this.write(this.classSrcName);
      if (targetKind != MappedElementKind.CLASS) {
         this.write("/");
         this.write(this.memberSrcName);
         if (targetKind == MappedElementKind.METHOD || this.xsrg) {
            this.write(" ");
            this.write(this.memberSrcDesc);
         }
      }

      this.write(" ");
      if (this.classDstName == null) {
         this.classDstName = this.classSrcName;
      }

      this.write(this.classDstName);
      if (targetKind != MappedElementKind.CLASS) {
         this.write("/");
         this.write(this.memberDstName);
         if (targetKind == MappedElementKind.METHOD || this.xsrg) {
            this.write(" ");
            this.write(this.memberDstDesc);
         }
      }

      this.writeLn();
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

   static {
      xsrgFlags.add(MappingFlag.NEEDS_SRC_FIELD_DESC);
      xsrgFlags.add(MappingFlag.NEEDS_DST_FIELD_DESC);
   }
}
