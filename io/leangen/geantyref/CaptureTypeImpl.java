package io.leangen.geantyref;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CaptureTypeImpl implements CaptureType {
   private final WildcardType wildcard;
   private final TypeVariable<?> variable;
   private final Type[] lowerBounds;
   private Type[] upperBounds;

   CaptureTypeImpl(WildcardType wildcard, TypeVariable<?> variable) {
      this.wildcard = wildcard;
      this.variable = variable;
      this.lowerBounds = wildcard.getLowerBounds();
   }

   void init(VarMap varMap) {
      ArrayList<Type> upperBoundsList = new ArrayList<>(Arrays.asList(varMap.map(this.variable.getBounds())));
      List<Type> wildcardUpperBounds = Arrays.asList(this.wildcard.getUpperBounds());
      if (wildcardUpperBounds.size() > 0 && wildcardUpperBounds.get(0) == Object.class) {
         upperBoundsList.addAll(wildcardUpperBounds.subList(1, wildcardUpperBounds.size()));
      } else {
         upperBoundsList.addAll(wildcardUpperBounds);
      }

      this.upperBounds = new Type[upperBoundsList.size()];
      upperBoundsList.toArray(this.upperBounds);
   }

   @Override
   public Type[] getLowerBounds() {
      return (Type[])this.lowerBounds.clone();
   }

   @Override
   public Type[] getUpperBounds() {
      assert this.upperBounds != null;
      return (Type[])this.upperBounds.clone();
   }

   @Override
   public void setUpperBounds(Type[] upperBounds) {
      this.upperBounds = upperBounds;
   }

   @Override
   public TypeVariable<?> getTypeVariable() {
      return this.variable;
   }

   @Override
   public WildcardType getWildcardType() {
      return this.wildcard;
   }

   @Override
   public String toString() {
      return "capture of " + this.wildcard;
   }
}
