package net.fabricmc.mappingio.adapter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.fabricmc.mappingio.FlatMappingVisitor;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingVisitor;
import org.jetbrains.annotations.Nullable;

public final class RegularAsFlatMappingVisitor implements FlatMappingVisitor {
   private final MappingVisitor next;
   private boolean relayDstFieldDescs;
   private boolean relayDstMethodDescs;
   private String lastClass;
   private boolean relayLastClass;
   private String lastMemberName;
   private String lastMemberDesc;
   private boolean lastMemberIsField;
   private boolean relayLastMember;
   private int lastArgPosition;
   private int lastLvIndex;
   private int lastStartOpIdx;
   private boolean lastMethodSubIsArg;
   private boolean relayLastMethodSub;

   public RegularAsFlatMappingVisitor(MappingVisitor next) {
      this.next = next;
   }

   @Override
   public Set<MappingFlag> getFlags() {
      return this.next.getFlags();
   }

   @Override
   public void reset() {
      this.lastClass = null;
      this.lastMemberName = this.lastMemberDesc = null;
      this.lastArgPosition = this.lastLvIndex = this.lastStartOpIdx = -1;
      this.next.reset();
   }

   @Override
   public boolean visitHeader() throws IOException {
      return this.next.visitHeader();
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
      this.next.visitNamespaces(srcNamespace, dstNamespaces);
      Set<MappingFlag> flags = this.next.getFlags();
      this.relayDstFieldDescs = flags.contains(MappingFlag.NEEDS_DST_FIELD_DESC);
      this.relayDstMethodDescs = flags.contains(MappingFlag.NEEDS_DST_METHOD_DESC);
   }

   @Override
   public void visitMetadata(String key, @Nullable String value) throws IOException {
      this.next.visitMetadata(key, value);
   }

   @Override
   public boolean visitContent() throws IOException {
      return this.next.visitContent();
   }

   @Override
   public boolean visitClass(String srcName, String[] dstNames) throws IOException {
      return this.visitClass(srcName, dstNames, null);
   }

   @Override
   public boolean visitClass(String srcName, String dstName) throws IOException {
      return this.visitClass(srcName, null, dstName);
   }

   private boolean visitClass(String srcName, @Nullable String[] dstNames, @Nullable String dstName) throws IOException {
      if (!srcName.equals(this.lastClass)) {
         this.lastClass = srcName;
         this.lastMemberName = this.lastMemberDesc = null;
         this.lastArgPosition = this.lastLvIndex = this.lastStartOpIdx = -1;
         this.relayLastClass = this.next.visitClass(srcName) && this.visitDstNames(MappedElementKind.CLASS, dstNames, dstName);
      }

      return this.relayLastClass;
   }

   @Override
   public void visitClassComment(String srcName, @Nullable String[] dstNames, String comment) throws IOException {
      if (this.visitClass(srcName, dstNames, null)) {
         this.next.visitComment(MappedElementKind.CLASS, comment);
      }
   }

   @Override
   public void visitClassComment(String srcName, @Nullable String dstName, String comment) throws IOException {
      if (this.visitClass(srcName, null, dstName)) {
         this.next.visitComment(MappedElementKind.CLASS, comment);
      }
   }

   @Override
   public boolean visitField(
      String srcClsName, String srcName, @Nullable String srcDesc, @Nullable String[] dstClsNames, String[] dstNames, @Nullable String[] dstDescs
   ) throws IOException {
      return this.visitField(srcClsName, srcName, srcDesc, dstClsNames, dstNames, dstDescs, null, null, null);
   }

   @Override
   public boolean visitField(String srcClsName, String srcName, @Nullable String srcDesc, @Nullable String dstClsName, String dstName, @Nullable String dstDesc) throws IOException {
      return this.visitField(srcClsName, srcName, srcDesc, null, null, null, dstClsName, dstName, dstDesc);
   }

   private boolean visitField(
      String srcClsName,
      String srcName,
      @Nullable String srcDesc,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstNames,
      @Nullable String[] dstDescs,
      @Nullable String dstClsName,
      @Nullable String dstName,
      @Nullable String dstDesc
   ) throws IOException {
      if (!this.visitClass(srcClsName, dstClsNames, dstClsName)) {
         return false;
      }

      if (!this.lastMemberIsField || !srcName.equals(this.lastMemberName) || srcDesc != null && !srcDesc.equals(this.lastMemberDesc)) {
         this.lastMemberName = srcName;
         this.lastMemberDesc = srcDesc;
         this.lastMemberIsField = true;
         this.lastArgPosition = this.lastLvIndex = this.lastStartOpIdx = -1;
         this.relayLastMember = this.next.visitField(srcName, srcDesc)
            && this.visitDstNamesDescs(MappedElementKind.FIELD, dstNames, dstDescs, dstName, dstDesc);
      }

      return this.relayLastMember;
   }

   @Override
   public void visitFieldComment(
      String srcClsName,
      String srcName,
      @Nullable String srcDesc,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstNames,
      @Nullable String[] dstDescs,
      String comment
   ) throws IOException {
      if (this.visitField(srcClsName, srcName, srcDesc, dstClsNames, dstNames, dstDescs, null, null, null)) {
         this.next.visitComment(MappedElementKind.FIELD, comment);
      }
   }

   @Override
   public void visitFieldComment(
      String srcClsName,
      String srcName,
      @Nullable String srcDesc,
      @Nullable String dstClsName,
      @Nullable String dstName,
      @Nullable String dstDesc,
      String comment
   ) throws IOException {
      if (this.visitField(srcClsName, srcName, srcDesc, null, null, null, dstClsName, dstName, dstDesc)) {
         this.next.visitComment(MappedElementKind.FIELD, comment);
      }
   }

   @Override
   public boolean visitMethod(
      String srcClsName, String srcName, @Nullable String srcDesc, @Nullable String[] dstClsNames, String[] dstNames, @Nullable String[] dstDescs
   ) throws IOException {
      return this.visitMethod(srcClsName, srcName, srcDesc, dstClsNames, dstNames, dstDescs, null, null, null);
   }

   @Override
   public boolean visitMethod(
      String srcClsName, String srcName, @Nullable String srcDesc, @Nullable String dstClsName, String dstName, @Nullable String dstDesc
   ) throws IOException {
      return this.visitMethod(srcClsName, srcName, srcDesc, null, null, null, dstClsName, dstName, dstDesc);
   }

   private boolean visitMethod(
      String srcClsName,
      String srcName,
      @Nullable String srcDesc,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstNames,
      @Nullable String[] dstDescs,
      @Nullable String dstClsName,
      @Nullable String dstName,
      @Nullable String dstDesc
   ) throws IOException {
      if (!this.visitClass(srcClsName, dstClsNames, dstClsName)) {
         return false;
      }

      if (this.lastMemberIsField || !srcName.equals(this.lastMemberName) || srcDesc != null && !srcDesc.equals(this.lastMemberDesc)) {
         this.lastMemberName = srcName;
         this.lastMemberDesc = srcDesc;
         this.lastMemberIsField = false;
         this.lastArgPosition = this.lastLvIndex = this.lastStartOpIdx = -1;
         this.relayLastMember = this.next.visitMethod(srcName, srcDesc)
            && this.visitDstNamesDescs(MappedElementKind.METHOD, dstNames, dstDescs, dstName, dstDesc);
      }

      return this.relayLastMember;
   }

   @Override
   public void visitMethodComment(
      String srcClsName,
      String srcName,
      @Nullable String srcDesc,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstNames,
      @Nullable String[] dstDescs,
      String comment
   ) throws IOException {
      if (this.visitMethod(srcClsName, srcName, srcDesc, dstClsNames, dstNames, dstDescs, null, null, null)) {
         this.next.visitComment(MappedElementKind.METHOD, comment);
      }
   }

   @Override
   public void visitMethodComment(
      String srcClsName,
      String srcName,
      @Nullable String srcDesc,
      @Nullable String dstClsName,
      @Nullable String dstName,
      @Nullable String dstDesc,
      String comment
   ) throws IOException {
      if (this.visitMethod(srcClsName, srcName, srcDesc, null, null, null, dstClsName, dstName, dstDesc)) {
         this.next.visitComment(MappedElementKind.METHOD, comment);
      }
   }

   @Override
   public boolean visitMethodArg(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int argPosition,
      int lvIndex,
      @Nullable String srcArgName,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstMethodNames,
      @Nullable String[] dstMethodDescs,
      String[] dstArgNames
   ) throws IOException {
      return this.visitMethodArg(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         argPosition,
         lvIndex,
         srcArgName,
         dstClsNames,
         dstMethodNames,
         dstMethodDescs,
         dstArgNames,
         null,
         null,
         null,
         null
      );
   }

   @Override
   public boolean visitMethodArg(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int argPosition,
      int lvIndex,
      @Nullable String srcArgName,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      String dstArgName
   ) throws IOException {
      return this.visitMethodArg(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         argPosition,
         lvIndex,
         srcArgName,
         null,
         null,
         null,
         null,
         dstClsName,
         dstMethodName,
         dstMethodDesc,
         dstArgName
      );
   }

   private boolean visitMethodArg(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int argPosition,
      int lvIndex,
      @Nullable String srcName,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstMethodNames,
      @Nullable String[] dstMethodDescs,
      @Nullable String[] dstNames,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      @Nullable String dstName
   ) throws IOException {
      if (!this.visitMethod(srcClsName, srcMethodName, srcMethodDesc, dstClsNames, dstMethodNames, dstMethodDescs, dstClsName, dstMethodName, dstMethodDesc)) {
         return false;
      }

      if (!this.lastMethodSubIsArg || argPosition != this.lastArgPosition || lvIndex != this.lastLvIndex) {
         this.lastArgPosition = argPosition;
         this.lastLvIndex = lvIndex;
         this.lastMethodSubIsArg = true;
         this.relayLastMethodSub = this.next.visitMethodArg(argPosition, lvIndex, srcName)
            && this.visitDstNames(MappedElementKind.METHOD_ARG, dstNames, dstName);
      }

      return this.relayLastMethodSub;
   }

   @Override
   public void visitMethodArgComment(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int argPosition,
      int lvIndex,
      @Nullable String srcArgName,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstMethodNames,
      @Nullable String[] dstMethodDescs,
      @Nullable String[] dstArgNames,
      String comment
   ) throws IOException {
      if (this.visitMethodArg(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         argPosition,
         lvIndex,
         srcArgName,
         dstClsNames,
         dstMethodNames,
         dstMethodDescs,
         dstArgNames,
         null,
         null,
         null,
         null
      )) {
         this.next.visitComment(MappedElementKind.METHOD_ARG, comment);
      }
   }

   @Override
   public void visitMethodArgComment(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int argPosition,
      int lvIndex,
      @Nullable String srcArgName,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      @Nullable String dstArgName,
      String comment
   ) throws IOException {
      if (this.visitMethodArg(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         argPosition,
         lvIndex,
         srcArgName,
         null,
         null,
         null,
         null,
         dstClsName,
         dstMethodName,
         dstMethodDesc,
         dstArgName
      )) {
         this.next.visitComment(MappedElementKind.METHOD_ARG, comment);
      }
   }

   @Override
   public boolean visitMethodVar(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcVarName,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstMethodNames,
      @Nullable String[] dstMethodDescs,
      String[] dstVarNames
   ) throws IOException {
      return this.visitMethodVar(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         lvtRowIndex,
         lvIndex,
         startOpIdx,
         endOpIdx,
         srcVarName,
         dstClsNames,
         dstMethodNames,
         dstMethodDescs,
         dstVarNames,
         null,
         null,
         null,
         null
      );
   }

   @Override
   public boolean visitMethodVar(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcVarName,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      String dstVarName
   ) throws IOException {
      return this.visitMethodVar(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         lvtRowIndex,
         lvIndex,
         startOpIdx,
         endOpIdx,
         srcVarName,
         null,
         null,
         null,
         null,
         dstClsName,
         dstMethodName,
         dstMethodDesc,
         dstVarName
      );
   }

   private boolean visitMethodVar(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcName,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstMethodNames,
      @Nullable String[] dstMethodDescs,
      @Nullable String[] dstNames,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      @Nullable String dstName
   ) throws IOException {
      if (!this.visitMethod(srcClsName, srcMethodName, srcMethodDesc, dstClsNames, dstMethodNames, dstMethodDescs, dstClsName, dstMethodName, dstMethodDesc)) {
         return false;
      }

      if (this.lastMethodSubIsArg || lvtRowIndex != this.lastArgPosition || lvIndex != this.lastLvIndex || startOpIdx != this.lastStartOpIdx) {
         this.lastArgPosition = lvtRowIndex;
         this.lastLvIndex = lvIndex;
         this.lastStartOpIdx = startOpIdx;
         this.lastMethodSubIsArg = false;
         this.relayLastMethodSub = this.next.visitMethodVar(lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName)
            && this.visitDstNames(MappedElementKind.METHOD_VAR, dstNames, dstName);
      }

      return this.relayLastMethodSub;
   }

   @Override
   public void visitMethodVarComment(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcVarName,
      @Nullable String[] dstClsNames,
      @Nullable String[] dstMethodNames,
      @Nullable String[] dstMethodDescs,
      @Nullable String[] dstVarNames,
      String comment
   ) throws IOException {
      if (this.visitMethodVar(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         lvtRowIndex,
         lvIndex,
         startOpIdx,
         endOpIdx,
         srcVarName,
         dstClsNames,
         dstMethodNames,
         dstMethodDescs,
         dstVarNames,
         null,
         null,
         null,
         null
      )) {
         this.next.visitComment(MappedElementKind.METHOD_VAR, comment);
      }
   }

   @Override
   public void visitMethodVarComment(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcVarName,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      @Nullable String dstVarName,
      String comment
   ) throws IOException {
      if (this.visitMethodVar(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         lvtRowIndex,
         lvIndex,
         startOpIdx,
         endOpIdx,
         srcVarName,
         null,
         null,
         null,
         null,
         dstClsName,
         dstMethodName,
         dstMethodDesc,
         dstVarName
      )) {
         this.next.visitComment(MappedElementKind.METHOD_VAR, comment);
      }
   }

   @Override
   public boolean visitEnd() throws IOException {
      return this.next.visitEnd();
   }

   private boolean visitDstNames(MappedElementKind targetKind, @Nullable String[] dstNames, @Nullable String dstName) throws IOException {
      if (dstNames != null) {
         for (int i = 0; i < dstNames.length; i++) {
            String name = dstNames[i];
            if (name != null) {
               this.next.visitDstName(targetKind, i, name);
            }
         }
      } else if (dstName != null) {
         this.next.visitDstName(targetKind, 0, dstName);
      }

      return this.next.visitElementContent(targetKind);
   }

   private boolean visitDstNamesDescs(
      MappedElementKind targetKind, @Nullable String[] dstNames, @Nullable String[] dstDescs, @Nullable String dstName, @Nullable String dstDesc
   ) throws IOException {
      boolean relayMemberDesc = targetKind == MappedElementKind.FIELD && this.relayDstFieldDescs
         || targetKind != MappedElementKind.FIELD && this.relayDstMethodDescs;
      if (dstNames == null && dstDescs == null) {
         if (dstName != null) {
            this.next.visitDstName(targetKind, 0, dstName);
         }

         if (dstDesc != null && relayMemberDesc) {
            this.next.visitDstDesc(targetKind, 0, dstDesc);
         }
      } else {
         if (dstNames != null) {
            for (int i = 0; i < dstNames.length; i++) {
               String name = dstNames[i];
               if (name != null) {
                  this.next.visitDstName(targetKind, i, name);
               }
            }
         }

         if (dstDescs != null && relayMemberDesc) {
            for (int i = 0; i < dstDescs.length; i++) {
               String desc = dstDescs[i];
               if (desc != null) {
                  this.next.visitDstDesc(targetKind, i, desc);
               }
            }
         }
      }

      return this.next.visitElementContent(targetKind);
   }
}
