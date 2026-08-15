package io.leangen.geantyref;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;

public interface AnnotatedCaptureType extends AnnotatedType {
   AnnotatedType[] getAnnotatedUpperBounds();

   AnnotatedType[] getAnnotatedLowerBounds();

   AnnotatedTypeVariable getAnnotatedTypeVariable();

   AnnotatedWildcardType getAnnotatedWildcardType();

   void setAnnotatedUpperBounds(AnnotatedType[] var1);
}
