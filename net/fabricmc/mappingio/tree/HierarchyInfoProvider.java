package net.fabricmc.mappingio.tree;

import java.util.Collection;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface HierarchyInfoProvider<T> {
   String getNamespace();

   @Nullable
   String resolveField(String var1, String var2, @Nullable String var3);

   @Nullable
   String resolveMethod(String var1, String var2, @Nullable String var3);

   @Nullable
   T getMethodHierarchy(String var1, String var2, @Nullable String var3);

   @Nullable
   default T getMethodHierarchy(MappingTreeView.MethodMappingView method) {
      int nsId = method.getTree().getNamespaceId(this.getNamespace());
      if (nsId == -2) {
         throw new IllegalArgumentException("disassociated namespace");
      }

      String owner = method.getOwner().getName(nsId);
      String name = method.getName(nsId);
      String desc = method.getDesc(nsId);
      return owner != null && name != null ? this.getMethodHierarchy(owner, name, desc) : null;
   }

   int getHierarchySize(T var1);

   Collection<? extends MappingTreeView.MethodMappingView> getHierarchyMethods(T var1, MappingTreeView var2);

   default Collection<? extends MappingTree.MethodMapping> getHierarchyMethods(T hierarchy, MappingTree tree) {
      return this.getHierarchyMethods(hierarchy, (MappingTreeView)tree);
   }
}
