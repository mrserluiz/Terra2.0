package net.fabricmc.mappingio.tree;

import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public interface MappingTree extends MappingTreeView {
   @Nullable
   String setSrcNamespace(String var1);

   List<String> setDstNamespaces(List<String> var1);

   @Override
   List<? extends MappingTree.MetadataEntry> getMetadata();

   @Override
   List<? extends MappingTree.MetadataEntry> getMetadata(String var1);

   void addMetadata(MappingTree.MetadataEntry var1);

   boolean removeMetadata(String var1);

   @Override
   Collection<? extends MappingTree.ClassMapping> getClasses();

   @Nullable
   MappingTree.ClassMapping getClass(String var1);

   @Nullable
   default MappingTree.ClassMapping getClass(String name, int namespace) {
      return (MappingTree.ClassMapping)MappingTreeView.super.getClass(name, namespace);
   }

   MappingTree.ClassMapping addClass(MappingTree.ClassMapping var1);

   @Nullable
   MappingTree.ClassMapping removeClass(String var1);

   @Nullable
   default MappingTree.FieldMapping getField(String srcClsName, String srcName, @Nullable String srcDesc) {
      return (MappingTree.FieldMapping)MappingTreeView.super.getField(srcClsName, srcName, srcDesc);
   }

   @Nullable
   default MappingTree.FieldMapping getField(String clsName, String name, @Nullable String desc, int namespace) {
      return (MappingTree.FieldMapping)MappingTreeView.super.getField(clsName, name, desc, namespace);
   }

   @Nullable
   default MappingTree.MethodMapping getMethod(String srcClsName, String srcName, @Nullable String srcDesc) {
      return (MappingTree.MethodMapping)MappingTreeView.super.getMethod(srcClsName, srcName, srcDesc);
   }

   @Nullable
   default MappingTree.MethodMapping getMethod(String clsName, String name, @Nullable String desc, int namespace) {
      return (MappingTree.MethodMapping)MappingTreeView.super.getMethod(clsName, name, desc, namespace);
   }

   interface ClassMapping extends MappingTree.ElementMapping, MappingTreeView.ClassMappingView {
      @Override
      Collection<? extends MappingTree.FieldMapping> getFields();

      @Nullable
      MappingTree.FieldMapping getField(String var1, @Nullable String var2);

      @Nullable
      default MappingTree.FieldMapping getField(String name, @Nullable String desc, int namespace) {
         return (MappingTree.FieldMapping)MappingTreeView.ClassMappingView.super.getField(name, desc, namespace);
      }

      MappingTree.FieldMapping addField(MappingTree.FieldMapping var1);

      @Nullable
      MappingTree.FieldMapping removeField(String var1, @Nullable String var2);

      @Override
      Collection<? extends MappingTree.MethodMapping> getMethods();

      @Nullable
      MappingTree.MethodMapping getMethod(String var1, @Nullable String var2);

      @Nullable
      default MappingTree.MethodMapping getMethod(String name, @Nullable String desc, int namespace) {
         return (MappingTree.MethodMapping)MappingTreeView.ClassMappingView.super.getMethod(name, desc, namespace);
      }

      MappingTree.MethodMapping addMethod(MappingTree.MethodMapping var1);

      @Nullable
      MappingTree.MethodMapping removeMethod(String var1, @Nullable String var2);
   }

   interface ElementMapping extends MappingTreeView.ElementMappingView {
      MappingTree getTree();

      void setDstName(String var1, int var2);

      void setComment(String var1);
   }

   interface FieldMapping extends MappingTree.MemberMapping, MappingTreeView.FieldMappingView {
   }

   interface MemberMapping extends MappingTree.ElementMapping, MappingTreeView.MemberMappingView {
      MappingTree.ClassMapping getOwner();

      void setSrcDesc(String var1);
   }

   interface MetadataEntry extends MappingTreeView.MetadataEntryView {
   }

   interface MethodArgMapping extends MappingTree.ElementMapping, MappingTreeView.MethodArgMappingView {
      MappingTree.MethodMapping getMethod();

      void setArgPosition(int var1);

      void setLvIndex(int var1);
   }

   interface MethodMapping extends MappingTree.MemberMapping, MappingTreeView.MethodMappingView {
      @Override
      Collection<? extends MappingTree.MethodArgMapping> getArgs();

      @Nullable
      MappingTree.MethodArgMapping getArg(int var1, int var2, @Nullable String var3);

      MappingTree.MethodArgMapping addArg(MappingTree.MethodArgMapping var1);

      @Nullable
      MappingTree.MethodArgMapping removeArg(int var1, int var2, @Nullable String var3);

      @Override
      Collection<? extends MappingTree.MethodVarMapping> getVars();

      @Nullable
      MappingTree.MethodVarMapping getVar(int var1, int var2, int var3, int var4, @Nullable String var5);

      MappingTree.MethodVarMapping addVar(MappingTree.MethodVarMapping var1);

      @Nullable
      MappingTree.MethodVarMapping removeVar(int var1, int var2, int var3, int var4, @Nullable String var5);
   }

   interface MethodVarMapping extends MappingTree.ElementMapping, MappingTreeView.MethodVarMappingView {
      MappingTree.MethodMapping getMethod();

      void setLvtRowIndex(int var1);

      void setLvIndex(int var1, int var2, int var3);
   }
}
