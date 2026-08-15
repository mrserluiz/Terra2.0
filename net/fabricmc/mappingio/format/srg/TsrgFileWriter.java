package net.fabricmc.mappingio.format.srg;

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

public final class TsrgFileWriter implements MappingWriter {
   private static final Set<MappingFlag> tsrgFlags = EnumSet.of(MappingFlag.NEEDS_ELEMENT_UNIQUENESS, MappingFlag.NEEDS_SRC_METHOD_DESC);
   private static final Set<MappingFlag> tsrg2Flags = EnumSet.copyOf(tsrgFlags);
   private final Writer writer;
   private final boolean tsrg2;
   private String clsSrcName;
   private String memberSrcName;
   private String memberSrcDesc;
   private String argSrcName;
   private String[] dstNames;
   private boolean hasAnyDstNames;
   private int lvIndex = -1;
   private boolean classContentVisitPending;
   private boolean methodContentVisitPending;

   public TsrgFileWriter(Writer writer, boolean tsrg2) {
      this.writer = writer;
      this.tsrg2 = tsrg2;
   }

   @Override
   public void close() throws IOException {
      this.writer.close();
   }

   @Override
   public Set<MappingFlag> getFlags() {
      return this.tsrg2 ? tsrg2Flags : tsrgFlags;
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
      this.dstNames = new String[dstNamespaces.size()];
      if (this.tsrg2) {
         this.write("tsrg2 ");
         this.write(srcNamespace);

         for (String dstNamespace : dstNamespaces) {
            this.writeSpace();
            this.write(dstNamespace);
         }

         this.writeLn();
      }
   }

   @Override
   public void visitMetadata(String key, @Nullable String value) throws IOException {
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.clsSrcName = srcName;
      this.hasAnyDstNames = false;
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      this.hasAnyDstNames = false;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      this.hasAnyDstNames = false;
      return true;
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
      if (!this.tsrg2) {
         return false;
      }

      this.lvIndex = lvIndex;
      this.argSrcName = srcName;
      this.hasAnyDstNames = false;
      return true;
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) throws IOException {
      return false;
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) {
      if (this.tsrg2 || namespace == 0) {
         this.dstNames[namespace] = name;
         this.hasAnyDstNames |= name != null;
      }
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      if (this.classContentVisitPending && targetKind != MappedElementKind.CLASS && this.hasAnyDstNames) {
         String[] memberOrArgDstNames = (String[])this.dstNames.clone();
         Arrays.fill(this.dstNames, this.clsSrcName);
         this.visitElementContent(MappedElementKind.CLASS);
         this.classContentVisitPending = false;
         this.dstNames = memberOrArgDstNames;
      }

      if (this.methodContentVisitPending && targetKind == MappedElementKind.METHOD_ARG && this.hasAnyDstNames) {
         String[] argDstNames = (String[])this.dstNames.clone();
         Arrays.fill(this.dstNames, this.memberSrcName);
         this.visitElementContent(MappedElementKind.METHOD);
         this.methodContentVisitPending = false;
         this.dstNames = argDstNames;
      }

      String srcName = null;
      switch (targetKind) {
         case CLASS:
            if (!this.hasAnyDstNames) {
               this.classContentVisitPending = true;
               return true;
            }

            srcName = this.clsSrcName;
            break;
         case FIELD:
         case METHOD:
            if (!this.hasAnyDstNames) {
               if (targetKind == MappedElementKind.METHOD) {
                  this.methodContentVisitPending = true;
                  return this.tsrg2;
               }

               return false;
            }

            srcName = this.memberSrcName;
            this.writeTab();
            break;
         case METHOD_ARG:
            assert this.tsrg2;
            if (!this.hasAnyDstNames) {
               return false;
            }

            srcName = this.argSrcName;
            this.writeTab();
            this.writeTab();
            this.write(Integer.toString(this.lvIndex));
            this.writeSpace();
         case METHOD_VAR:
            assert this.tsrg2;
      }

      this.write(srcName);
      if (targetKind == MappedElementKind.METHOD || targetKind == MappedElementKind.FIELD && this.tsrg2) {
         this.writeSpace();
         this.write(this.memberSrcDesc);
      }

      int dstNsCount = this.tsrg2 ? this.dstNames.length : 1;

      for (int i = 0; i < dstNsCount; i++) {
         String dstName = this.dstNames[i];
         this.writeSpace();
         this.write(dstName != null ? dstName : srcName);
      }

      this.writeLn();
      Arrays.fill(this.dstNames, null);
      return targetKind == MappedElementKind.CLASS || this.tsrg2 && targetKind == MappedElementKind.METHOD;
   }

   @Override
   public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
   }

   private void write(String str) throws IOException {
      this.writer.write(str);
   }

   private void writeTab() throws IOException {
      this.writer.write(9);
   }

   private void writeSpace() throws IOException {
      this.writer.write(32);
   }

   private void writeLn() throws IOException {
      this.writer.write(10);
   }

   static {
      tsrg2Flags.add(MappingFlag.NEEDS_SRC_FIELD_DESC);
   }
}
