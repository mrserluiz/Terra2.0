package xyz.jpenilla.reflectionremapper;

import java.util.function.UnaryOperator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
final class ClassNamePreprocessingReflectionRemapper implements ReflectionRemapper {
   private final ReflectionRemapper delegate;
   private final UnaryOperator<String> processor;

   ClassNamePreprocessingReflectionRemapper(final ReflectionRemapper delegate, final UnaryOperator<String> processor) {
      this.delegate = delegate;
      this.processor = processor;
   }

   @Override
   public String remapClassName(final String className) {
      return this.delegate.remapClassName(this.processor.apply(className));
   }

   @Override
   public String remapFieldName(final Class<?> holdingClass, final String fieldName) {
      return this.delegate.remapFieldName(holdingClass, fieldName);
   }

   @Override
   public String remapMethodName(final Class<?> holdingClass, final String methodName, final Class<?>... paramTypes) {
      return this.delegate.remapMethodName(holdingClass, methodName, paramTypes);
   }
}
