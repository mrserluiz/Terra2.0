package com.dfsek.terra.lib.commons.lang3.reflect;

import com.dfsek.terra.lib.commons.lang3.Validate;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public abstract class TypeLiteral<T> implements Typed<T> {
   private static final TypeVariable<Class<TypeLiteral>> T = TypeLiteral.class.getTypeParameters()[0];
   public final Type value = Validate.notNull(
      TypeUtils.getTypeArguments(this.getClass(), TypeLiteral.class).get(T), "%s does not assign type parameter %s", this.getClass(), TypeUtils.toLongString(T)
   );
   private final String toString = String.format("%s<%s>", TypeLiteral.class.getSimpleName(), TypeUtils.toString(this.value));

   protected TypeLiteral() {
   }

   @Override
   public final boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (!(obj instanceof TypeLiteral)) {
         return false;
      }

      TypeLiteral<?> other = (TypeLiteral<?>)obj;
      return TypeUtils.equals(this.value, other.value);
   }

   @Override
   public Type getType() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return 592 | this.value.hashCode();
   }

   @Override
   public String toString() {
      return this.toString;
   }
}
