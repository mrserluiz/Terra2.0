package net.fabricmc.mappingio.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public final class VisitOrder {
   private static final AlphanumericComparator ALPHANUM = new AlphanumericComparator(Locale.ROOT);
   private Comparator<MappingTreeView.ClassMappingView> classComparator;
   private Comparator<MappingTreeView.FieldMappingView> fieldComparator;
   private Comparator<MappingTreeView.MethodMappingView> methodComparator;
   private Comparator<MappingTreeView.MethodArgMappingView> methodArgComparator;
   private Comparator<MappingTreeView.MethodVarMappingView> methodVarComparator;
   private boolean methodsFirst;
   private boolean methodVarsFirst;

   private VisitOrder() {
   }

   public static VisitOrder createByInputOrder() {
      return new VisitOrder();
   }

   public static VisitOrder createByName() {
      return new VisitOrder().classesBySrcName().fieldsBySrcNameDesc().methodsBySrcNameDesc().methodArgsByLvIndex().methodVarsByLvIndex();
   }

   public VisitOrder classComparator(Comparator<MappingTreeView.ClassMappingView> comparator) {
      this.classComparator = comparator;
      return this;
   }

   public VisitOrder classesBySrcName() {
      return this.classComparator(compareBySrcName());
   }

   public VisitOrder classesBySrcNameShortFirst() {
      return this.classComparator(compareBySrcNameShortFirst());
   }

   public VisitOrder fieldComparator(Comparator<MappingTreeView.FieldMappingView> comparator) {
      this.fieldComparator = comparator;
      return this;
   }

   public VisitOrder fieldsBySrcNameDesc() {
      return this.fieldComparator(compareBySrcNameDesc());
   }

   public VisitOrder methodComparator(Comparator<MappingTreeView.MethodMappingView> comparator) {
      this.methodComparator = comparator;
      return this;
   }

   public VisitOrder methodsBySrcNameDesc() {
      return this.methodComparator(compareBySrcNameDesc());
   }

   public VisitOrder methodArgComparator(Comparator<MappingTreeView.MethodArgMappingView> comparator) {
      this.methodArgComparator = comparator;
      return this;
   }

   public VisitOrder methodArgsByPosition() {
      return this.methodArgComparator(Comparator.comparingInt(MappingTreeView.MethodArgMappingView::getArgPosition));
   }

   public VisitOrder methodArgsByLvIndex() {
      return this.methodArgComparator(Comparator.comparingInt(MappingTreeView.MethodArgMappingView::getLvIndex));
   }

   public VisitOrder methodVarComparator(Comparator<MappingTreeView.MethodVarMappingView> comparator) {
      this.methodVarComparator = comparator;
      return this;
   }

   public VisitOrder methodVarsByLvtRowIndex() {
      return this.methodVarComparator(
         Comparator.comparingInt(MappingTreeView.MethodVarMappingView::getLvIndex).thenComparingInt(MappingTreeView.MethodVarMappingView::getLvtRowIndex)
      );
   }

   public VisitOrder methodVarsByLvIndex() {
      return this.methodVarComparator(
         Comparator.comparingInt(MappingTreeView.MethodVarMappingView::getLvIndex)
            .thenComparingInt(MappingTreeView.MethodVarMappingView::getStartOpIdx)
            .thenComparingInt(MappingTreeView.MethodVarMappingView::getEndOpIdx)
      );
   }

   public VisitOrder methodsFirst(boolean methodsFirst) {
      this.methodsFirst = methodsFirst;
      return this;
   }

   public VisitOrder fieldsFirst() {
      return this.methodsFirst(false);
   }

   public VisitOrder methodsFirst() {
      return this.methodsFirst(true);
   }

   public VisitOrder methodVarsFirst(boolean varsFirst) {
      this.methodVarsFirst = varsFirst;
      return this;
   }

   public VisitOrder methodArgsFirst() {
      return this.methodVarsFirst(false);
   }

   public VisitOrder methodVarsFirst() {
      return this.methodVarsFirst(true);
   }

   public static <T extends MappingTreeView.ElementMappingView> Comparator<T> compareBySrcName() {
      return (a, b) -> !(a instanceof MappingTreeView.ClassMappingView) && !(b instanceof MappingTreeView.ClassMappingView)
         ? compare(a.getSrcName(), b.getSrcName())
         : compareNestaware(a.getSrcName(), b.getSrcName(), false);
   }

   public static <T extends MappingTreeView.MemberMappingView> Comparator<T> compareBySrcNameDesc() {
      return (a, b) -> {
         int cmp = compare(a.getSrcName(), b.getSrcName());
         return cmp != 0 ? cmp : compare(a.getSrcDesc(), b.getSrcDesc());
      };
   }

   public static <T extends MappingTreeView.ElementMappingView> Comparator<T> compareBySrcNameShortFirst() {
      return (a, b) -> !(a instanceof MappingTreeView.ClassMappingView) && !(b instanceof MappingTreeView.ClassMappingView)
         ? compareShortFirst(a.getSrcName(), b.getSrcName())
         : compareNestaware(a.getSrcName(), b.getSrcName(), true);
   }

   public static <T extends MappingTreeView.MemberMappingView> Comparator<T> compareBySrcNameDescShortFirst() {
      return (a, b) -> {
         int cmp = compareShortFirst(a.getSrcName(), b.getSrcName());
         return cmp != 0 ? cmp : compare(a.getSrcDesc(), b.getSrcDesc());
      };
   }

   public static int compare(@Nullable String a, @Nullable String b) {
      return a != null && b != null ? ALPHANUM.compare(a, b) : compareNullLast(a, b);
   }

   public static int compare(String a, int startA, int endA, String b, int startB, int endB) {
      return ALPHANUM.compare(a.substring(startA, endA), b.substring(startB, endB));
   }

   public static int compareShortFirst(@Nullable String a, @Nullable String b) {
      if (a != null && b != null) {
         int cmp = a.length() - b.length();
         return cmp != 0 ? cmp : ALPHANUM.compare(a, b);
      } else {
         return compareNullLast(a, b);
      }
   }

   public static int compareShortFirst(String a, int startA, int endA, String b, int startB, int endB) {
      int lenA = endA - startA;
      int ret = Integer.compare(lenA, endB - startB);
      return ret != 0 ? ret : ALPHANUM.compare(a.substring(startA, endA), b.substring(startB, endB));
   }

   public static int compareNestaware(@Nullable String a, @Nullable String b) {
      return compareNestaware(a, b, false);
   }

   public static int compareShortFirstNestaware(@Nullable String a, @Nullable String b) {
      return compareNestaware(a, b, true);
   }

   private static int compareNestaware(@Nullable String a, @Nullable String b, boolean shortFirst) {
      if (a != null && b != null) {
         int pos = 0;

         do {
            int endA = a.indexOf(36, pos);
            int endB = b.indexOf(36, pos);
            int positiveEndA = endA < 0 ? a.length() : endA;
            int positiveEndB = endB < 0 ? b.length() : endB;
            int ret = shortFirst ? compareShortFirst(a, pos, positiveEndA, b, pos, positiveEndB) : compare(a, pos, positiveEndA, b, pos, positiveEndB);
            if (ret != 0) {
               return ret;
            }

            if (endA < 0 != endB < 0) {
               return endA < 0 ? -1 : 1;
            }

            pos = endA + 1;
         } while (pos > 0);

         return 0;
      } else {
         return compareNullLast(a, b);
      }
   }

   public static int compareNullLast(@Nullable String a, @Nullable String b) {
      if (a == null) {
         return b == null ? 0 : 1;
      } else {
         return b == null ? -1 : ALPHANUM.compare(a, b);
      }
   }

   public <T extends MappingTreeView.ClassMappingView> Collection<T> sortClasses(Collection<T> classes) {
      return sort(classes, this.classComparator);
   }

   public <T extends MappingTreeView.FieldMappingView> Collection<T> sortFields(Collection<T> fields) {
      return sort(fields, this.fieldComparator);
   }

   public <T extends MappingTreeView.MethodMappingView> Collection<T> sortMethods(Collection<T> methods) {
      return sort(methods, this.methodComparator);
   }

   public <T extends MappingTreeView.MethodArgMappingView> Collection<T> sortMethodArgs(Collection<T> args) {
      return sort(args, this.methodArgComparator);
   }

   public <T extends MappingTreeView.MethodVarMappingView> Collection<T> sortMethodVars(Collection<T> vars) {
      return sort(vars, this.methodVarComparator);
   }

   private static <T> Collection<T> sort(Collection<T> inputs, Comparator<? super T> comparator) {
      if (comparator != null && inputs.size() >= 2) {
         List<T> ret = new ArrayList<>(inputs);
         ret.sort(comparator);
         return ret;
      } else {
         return inputs;
      }
   }

   public boolean isMethodsFirst() {
      return this.methodsFirst;
   }

   public boolean isMethodVarsFirst() {
      return this.methodVarsFirst;
   }
}
