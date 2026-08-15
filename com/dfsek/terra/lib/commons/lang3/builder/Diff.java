package com.dfsek.terra.lib.commons.lang3.builder;

import com.dfsek.terra.lib.commons.lang3.ObjectUtils;
import com.dfsek.terra.lib.commons.lang3.reflect.TypeUtils;
import com.dfsek.terra.lib.commons.lang3.tuple.Pair;
import java.lang.reflect.Type;
import java.util.Objects;

public abstract class Diff<T> extends Pair<T, T> {
   private static final long serialVersionUID = 1L;
   private final Type type;
   private final String fieldName;

   protected Diff(String fieldName) {
      this.fieldName = Objects.requireNonNull(fieldName);
      this.type = ObjectUtils.defaultIfNull(TypeUtils.getTypeArguments(this.getClass(), Diff.class).get(Diff.class.getTypeParameters()[0]), Object.class);
   }

   Diff(String fieldName, Type type) {
      this.fieldName = Objects.requireNonNull(fieldName);
      this.type = Objects.requireNonNull(type);
   }

   public final String getFieldName() {
      return this.fieldName;
   }

   @Deprecated
   public final Type getType() {
      return this.type;
   }

   @Override
   public final T setValue(T value) {
      throw new UnsupportedOperationException("Cannot alter Diff object.");
   }

   @Override
   public final String toString() {
      return String.format("[%s: %s, %s]", this.fieldName, this.getLeft(), this.getRight());
   }
}
