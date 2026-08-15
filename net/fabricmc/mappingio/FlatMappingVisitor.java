package net.fabricmc.mappingio;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.mappingio.adapter.FlatAsRegularMappingVisitor;
import net.fabricmc.mappingio.adapter.RegularAsFlatMappingVisitor;
import org.jetbrains.annotations.Nullable;

public interface FlatMappingVisitor {
   default Set<MappingFlag> getFlags() {
      return MappingFlag.NONE;
   }

   default void reset() {
      throw new UnsupportedOperationException();
   }

   default boolean visitHeader() throws IOException {
      return true;
   }

   void visitNamespaces(String var1, List<String> var2) throws IOException;

   default void visitMetadata(String key, @Nullable String value) throws IOException {
   }

   default boolean visitContent() throws IOException {
      return true;
   }

   boolean visitClass(String var1, @Nullable String[] var2) throws IOException;

   void visitClassComment(String var1, @Nullable String[] var2, String var3) throws IOException;

   boolean visitField(String var1, String var2, @Nullable String var3, @Nullable String[] var4, @Nullable String[] var5, @Nullable String[] var6) throws IOException;

   void visitFieldComment(
      String var1, String var2, @Nullable String var3, @Nullable String[] var4, @Nullable String[] var5, @Nullable String[] var6, String var7
   ) throws IOException;

   boolean visitMethod(String var1, String var2, @Nullable String var3, @Nullable String[] var4, @Nullable String[] var5, @Nullable String[] var6) throws IOException;

   void visitMethodComment(
      String var1, String var2, @Nullable String var3, @Nullable String[] var4, @Nullable String[] var5, @Nullable String[] var6, String var7
   ) throws IOException;

   boolean visitMethodArg(
      String var1,
      String var2,
      @Nullable String var3,
      int var4,
      int var5,
      @Nullable String var6,
      @Nullable String[] var7,
      @Nullable String[] var8,
      @Nullable String[] var9,
      String[] var10
   ) throws IOException;

   void visitMethodArgComment(
      String var1,
      String var2,
      @Nullable String var3,
      int var4,
      int var5,
      @Nullable String var6,
      @Nullable String[] var7,
      @Nullable String[] var8,
      @Nullable String[] var9,
      @Nullable String[] var10,
      String var11
   ) throws IOException;

   boolean visitMethodVar(
      String var1,
      String var2,
      @Nullable String var3,
      int var4,
      int var5,
      int var6,
      int var7,
      @Nullable String var8,
      @Nullable String[] var9,
      @Nullable String[] var10,
      @Nullable String[] var11,
      String[] var12
   ) throws IOException;

   void visitMethodVarComment(
      String var1,
      String var2,
      @Nullable String var3,
      int var4,
      int var5,
      int var6,
      int var7,
      @Nullable String var8,
      @Nullable String[] var9,
      @Nullable String[] var10,
      @Nullable String[] var11,
      @Nullable String[] var12,
      String var13
   ) throws IOException;

   default boolean visitEnd() throws IOException {
      return true;
   }

   default MappingVisitor asRegularVisitor() {
      return new FlatAsRegularMappingVisitor(this);
   }

   static FlatMappingVisitor fromRegularVisitor(MappingVisitor visitor) {
      return new RegularAsFlatMappingVisitor(visitor);
   }

   default boolean visitField(String srcClsName, String srcName, @Nullable String srcDesc, String[] dstNames) throws IOException {
      Objects.requireNonNull(dstNames);
      return this.visitField(srcClsName, srcName, srcDesc, null, dstNames, null);
   }

   default boolean visitMethod(String srcClsName, String srcName, @Nullable String srcDesc, String[] dstNames) throws IOException {
      Objects.requireNonNull(dstNames);
      return this.visitMethod(srcClsName, srcName, srcDesc, null, dstNames, null);
   }

   default boolean visitMethodArg(
      String srcClsName, String srcMethodName, @Nullable String srcMethodDesc, int argPosition, int lvIndex, @Nullable String srcName, String[] dstNames
   ) throws IOException {
      Objects.requireNonNull(dstNames);
      return this.visitMethodArg(srcClsName, srcMethodName, srcMethodDesc, argPosition, lvIndex, srcName, null, null, null, dstNames);
   }

   default boolean visitMethodVar(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcName,
      String[] dstNames
   ) throws IOException {
      Objects.requireNonNull(dstNames);
      return this.visitMethodVar(srcClsName, srcMethodName, srcMethodDesc, lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName, null, null, null, dstNames);
   }

   default boolean visitClass(String srcName, String dstName) throws IOException {
      return this.visitClass(srcName, MappingUtil.toArray(dstName));
   }

   default void visitClassComment(String srcName, String comment) throws IOException {
      this.visitClassComment(srcName, (String)null, comment);
   }

   default void visitClassComment(String srcName, @Nullable String dstName, String comment) throws IOException {
      this.visitClassComment(srcName, MappingUtil.toArray(dstName), comment);
   }

   default boolean visitField(String srcClsName, String srcName, @Nullable String srcDesc, String dstName) throws IOException {
      return this.visitField(srcClsName, srcName, srcDesc, null, dstName, null);
   }

   default boolean visitField(
      String srcClsName, String srcName, @Nullable String srcDesc, @Nullable String dstClsName, String dstName, @Nullable String dstDesc
   ) throws IOException {
      return this.visitField(srcClsName, srcName, srcDesc, MappingUtil.toArray(dstClsName), MappingUtil.toArray(dstName), MappingUtil.toArray(dstDesc));
   }

   default void visitFieldComment(String srcClsName, String srcName, @Nullable String srcDesc, String comment) throws IOException {
      this.visitFieldComment(srcClsName, srcName, srcDesc, (String)null, null, null, comment);
   }

   default void visitFieldComment(
      String srcClsName,
      String srcName,
      @Nullable String srcDesc,
      @Nullable String dstClsName,
      @Nullable String dstName,
      @Nullable String dstDesc,
      String comment
   ) throws IOException {
      this.visitFieldComment(srcClsName, srcName, srcDesc, MappingUtil.toArray(dstClsName), MappingUtil.toArray(dstName), MappingUtil.toArray(dstDesc), comment);
   }

   default boolean visitMethod(String srcClsName, String srcName, @Nullable String srcDesc, String dstName) throws IOException {
      return this.visitMethod(srcClsName, srcName, srcDesc, null, dstName, null);
   }

   default boolean visitMethod(
      String srcClsName, String srcName, @Nullable String srcDesc, @Nullable String dstClsName, String dstName, @Nullable String dstDesc
   ) throws IOException {
      return this.visitMethod(srcClsName, srcName, srcDesc, MappingUtil.toArray(dstClsName), MappingUtil.toArray(dstName), MappingUtil.toArray(dstDesc));
   }

   default void visitMethodComment(String srcClsName, String srcName, @Nullable String srcDesc, String comment) throws IOException {
      this.visitMethodComment(srcClsName, srcName, srcDesc, (String)null, null, null, comment);
   }

   default void visitMethodComment(
      String srcClsName,
      String srcName,
      @Nullable String srcDesc,
      @Nullable String dstClsName,
      @Nullable String dstName,
      @Nullable String dstDesc,
      String comment
   ) throws IOException {
      this.visitMethodComment(
         srcClsName, srcName, srcDesc, MappingUtil.toArray(dstClsName), MappingUtil.toArray(dstName), MappingUtil.toArray(dstDesc), comment
      );
   }

   default boolean visitMethodArg(
      String srcClsName, String srcMethodName, @Nullable String srcMethodDesc, int argPosition, int lvIndex, @Nullable String srcName, String dstName
   ) throws IOException {
      return this.visitMethodArg(srcClsName, srcMethodName, srcMethodDesc, argPosition, lvIndex, srcName, null, null, null, dstName);
   }

   default boolean visitMethodArg(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int argPosition,
      int lvIndex,
      @Nullable String srcName,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      String dstName
   ) throws IOException {
      return this.visitMethodArg(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         argPosition,
         lvIndex,
         srcName,
         MappingUtil.toArray(dstClsName),
         MappingUtil.toArray(dstMethodName),
         MappingUtil.toArray(dstMethodDesc),
         MappingUtil.toArray(dstName)
      );
   }

   default void visitMethodArgComment(
      String srcClsName, String srcMethodName, @Nullable String srcMethodDesc, int argPosition, int lvIndex, @Nullable String srcName, String comment
   ) throws IOException {
      this.visitMethodArgComment(srcClsName, srcMethodName, srcMethodDesc, argPosition, lvIndex, srcName, (String)null, null, null, null, comment);
   }

   default void visitMethodArgComment(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int argPosition,
      int lvIndex,
      @Nullable String srcName,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      @Nullable String dstName,
      String comment
   ) throws IOException {
      this.visitMethodArgComment(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         argPosition,
         lvIndex,
         srcName,
         MappingUtil.toArray(dstClsName),
         MappingUtil.toArray(dstMethodName),
         MappingUtil.toArray(dstMethodDesc),
         MappingUtil.toArray(dstName),
         comment
      );
   }

   default boolean visitMethodVar(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcName,
      String dstName
   ) throws IOException {
      return this.visitMethodVar(srcClsName, srcMethodName, srcMethodDesc, lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName, null, null, null, dstName);
   }

   default boolean visitMethodVar(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcName,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      String dstName
   ) throws IOException {
      return this.visitMethodVar(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         lvtRowIndex,
         lvIndex,
         startOpIdx,
         endOpIdx,
         srcName,
         MappingUtil.toArray(dstClsName),
         MappingUtil.toArray(dstMethodName),
         MappingUtil.toArray(dstMethodDesc),
         MappingUtil.toArray(dstName)
      );
   }

   default void visitMethodVarComment(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcName,
      String comment
   ) throws IOException {
      this.visitMethodVarComment(
         srcClsName, srcMethodName, srcMethodDesc, lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName, (String)null, null, null, null, comment
      );
   }

   default void visitMethodVarComment(
      String srcClsName,
      String srcMethodName,
      @Nullable String srcMethodDesc,
      int lvtRowIndex,
      int lvIndex,
      int startOpIdx,
      int endOpIdx,
      @Nullable String srcName,
      @Nullable String dstClsName,
      @Nullable String dstMethodName,
      @Nullable String dstMethodDesc,
      @Nullable String dstName,
      String comment
   ) throws IOException {
      this.visitMethodVarComment(
         srcClsName,
         srcMethodName,
         srcMethodDesc,
         lvtRowIndex,
         lvIndex,
         startOpIdx,
         endOpIdx,
         srcName,
         MappingUtil.toArray(dstClsName),
         MappingUtil.toArray(dstMethodName),
         MappingUtil.toArray(dstMethodDesc),
         MappingUtil.toArray(dstName),
         comment
      );
   }
}
