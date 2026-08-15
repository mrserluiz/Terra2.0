package com.dfsek.terra.lib.google.common.reflect;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

abstract class TypeCapture<T> {
   final Type capture() {
      Type superclass = this.getClass().getGenericSuperclass();
      Preconditions.checkArgument(superclass instanceof ParameterizedType, "%s isn't parameterized", superclass);
      return ((ParameterizedType)superclass).getActualTypeArguments()[0];
   }
}
