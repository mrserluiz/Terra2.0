package io.leangen.geantyref;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public interface CaptureType extends Type {
   Type[] getUpperBounds();

   void setUpperBounds(Type[] var1);

   Type[] getLowerBounds();

   TypeVariable<?> getTypeVariable();

   WildcardType getWildcardType();
}
