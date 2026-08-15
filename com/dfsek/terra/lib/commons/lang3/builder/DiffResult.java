package com.dfsek.terra.lib.commons.lang3.builder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class DiffResult<T> implements Iterable<Diff<?>> {
   public static final String OBJECTS_SAME_STRING = "";
   private final List<Diff<?>> diffList;
   private final T lhs;
   private final T rhs;
   private final ToStringStyle style;
   private final String toStringFormat;

   DiffResult(T lhs, T rhs, List<Diff<?>> diffList, ToStringStyle style, String toStringFormat) {
      this.diffList = Objects.requireNonNull(diffList, "diffList");
      this.lhs = Objects.requireNonNull(lhs, "lhs");
      this.rhs = Objects.requireNonNull(rhs, "rhs");
      this.style = Objects.requireNonNull(style, "style");
      this.toStringFormat = Objects.requireNonNull(toStringFormat, "toStringFormat");
   }

   public List<Diff<?>> getDiffs() {
      return Collections.unmodifiableList(this.diffList);
   }

   public T getLeft() {
      return this.lhs;
   }

   public int getNumberOfDiffs() {
      return this.diffList.size();
   }

   public T getRight() {
      return this.rhs;
   }

   public ToStringStyle getToStringStyle() {
      return this.style;
   }

   @Override
   public Iterator<Diff<?>> iterator() {
      return this.diffList.iterator();
   }

   @Override
   public String toString() {
      return this.toString(this.style);
   }

   public String toString(ToStringStyle style) {
      if (this.diffList.isEmpty()) {
         return "";
      }

      ToStringBuilder lhsBuilder = new ToStringBuilder(this.lhs, style);
      ToStringBuilder rhsBuilder = new ToStringBuilder(this.rhs, style);
      this.diffList.forEach(diff -> {
         lhsBuilder.append(diff.getFieldName(), diff.getLeft());
         rhsBuilder.append(diff.getFieldName(), diff.getRight());
      });
      return String.format(this.toStringFormat, lhsBuilder.build(), rhsBuilder.build());
   }
}
