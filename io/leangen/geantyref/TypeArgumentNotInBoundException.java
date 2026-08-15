package io.leangen.geantyref;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class TypeArgumentNotInBoundException extends IllegalArgumentException {
   private final Type argument;
   private final TypeVariable<?> parameter;
   private final Type bound;

   TypeArgumentNotInBoundException(Type argument, TypeVariable<?> parameter, Type bound) {
      super("Given argument [" + argument + "] for type parameter [" + parameter.getName() + "] is not within the bound [" + bound + "]");
      this.argument = argument;
      this.parameter = parameter;
      this.bound = bound;
   }

   public Type getArgument() {
      return this.argument;
   }

   public TypeVariable<?> getParameter() {
      return this.parameter;
   }

   public Type getBound() {
      return this.bound;
   }
}
