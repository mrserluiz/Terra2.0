package com.dfsek.terra.lib.google.common.reflect;

import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import org.jspecify.annotations.Nullable;

public abstract class Invokable<T, R> implements AnnotatedElement, Member {
   private final AccessibleObject accessibleObject;
   private final Member member;
   private static final boolean ANNOTATED_TYPE_EXISTS = initAnnotatedTypeExists();

   <M extends AccessibleObject & Member> Invokable(M member) {
      Preconditions.checkNotNull(member);
      this.accessibleObject = member;
      this.member = member;
   }

   public static Invokable<?, Object> from(Method method) {
      return new Invokable.MethodInvokable(method);
   }

   public static <T> Invokable<T, T> from(Constructor<T> constructor) {
      return new Invokable.ConstructorInvokable<>(constructor);
   }

   @Override
   public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
      return this.accessibleObject.isAnnotationPresent(annotationClass);
   }

   @Override
   public final <A extends Annotation> @Nullable A getAnnotation(Class<A> annotationClass) {
      return this.accessibleObject.getAnnotation(annotationClass);
   }

   @Override
   public final Annotation[] getAnnotations() {
      return this.accessibleObject.getAnnotations();
   }

   @Override
   public final Annotation[] getDeclaredAnnotations() {
      return this.accessibleObject.getDeclaredAnnotations();
   }

   public abstract TypeVariable<?>[] getTypeParameters();

   public final void setAccessible(boolean flag) {
      this.accessibleObject.setAccessible(flag);
   }

   public final boolean trySetAccessible() {
      try {
         this.accessibleObject.setAccessible(true);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   public final boolean isAccessible() {
      return this.accessibleObject.isAccessible();
   }

   @Override
   public final String getName() {
      return this.member.getName();
   }

   @Override
   public final int getModifiers() {
      return this.member.getModifiers();
   }

   @Override
   public final boolean isSynthetic() {
      return this.member.isSynthetic();
   }

   public final boolean isPublic() {
      return Modifier.isPublic(this.getModifiers());
   }

   public final boolean isProtected() {
      return Modifier.isProtected(this.getModifiers());
   }

   public final boolean isPackagePrivate() {
      return !this.isPrivate() && !this.isPublic() && !this.isProtected();
   }

   public final boolean isPrivate() {
      return Modifier.isPrivate(this.getModifiers());
   }

   public final boolean isStatic() {
      return Modifier.isStatic(this.getModifiers());
   }

   public final boolean isFinal() {
      return Modifier.isFinal(this.getModifiers());
   }

   public final boolean isAbstract() {
      return Modifier.isAbstract(this.getModifiers());
   }

   public final boolean isNative() {
      return Modifier.isNative(this.getModifiers());
   }

   public final boolean isSynchronized() {
      return Modifier.isSynchronized(this.getModifiers());
   }

   final boolean isVolatile() {
      return Modifier.isVolatile(this.getModifiers());
   }

   final boolean isTransient() {
      return Modifier.isTransient(this.getModifiers());
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (!(obj instanceof Invokable)) {
         return false;
      }

      Invokable<?, ?> that = (Invokable<?, ?>)obj;
      return this.getOwnerType().equals(that.getOwnerType()) && this.member.equals(that.member);
   }

   @Override
   public int hashCode() {
      return this.member.hashCode();
   }

   @Override
   public String toString() {
      return this.member.toString();
   }

   public abstract boolean isOverridable();

   public abstract boolean isVarArgs();

   @CanIgnoreReturnValue
   public final @Nullable R invoke(@Nullable T receiver, @Nullable Object... args) throws InvocationTargetException, IllegalAccessException {
      return (R)this.invokeInternal(receiver, Preconditions.checkNotNull(args));
   }

   public final TypeToken<? extends R> getReturnType() {
      return (TypeToken<? extends R>)TypeToken.of(this.getGenericReturnType());
   }

   @IgnoreJRERequirement
   public final ImmutableList<Parameter> getParameters() {
      Type[] parameterTypes = this.getGenericParameterTypes();
      Annotation[][] annotations = this.getParameterAnnotations();
      Object[] annotatedTypes = ANNOTATED_TYPE_EXISTS ? this.getAnnotatedParameterTypes() : new Object[parameterTypes.length];
      ImmutableList.Builder<Parameter> builder = ImmutableList.builder();

      for (int i = 0; i < parameterTypes.length; i++) {
         builder.add(new Parameter(this, i, TypeToken.of(parameterTypes[i]), annotations[i], annotatedTypes[i]));
      }

      return builder.build();
   }

   public final ImmutableList<TypeToken<? extends Throwable>> getExceptionTypes() {
      ImmutableList.Builder<TypeToken<? extends Throwable>> builder = ImmutableList.builder();

      for (Type type : this.getGenericExceptionTypes()) {
         TypeToken<? extends Throwable> exceptionType = (TypeToken<? extends Throwable>)TypeToken.of(type);
         builder.add(exceptionType);
      }

      return builder.build();
   }

   public final <R1 extends R> Invokable<T, R1> returning(Class<R1> returnType) {
      return this.returning(TypeToken.of(returnType));
   }

   public final <R1 extends R> Invokable<T, R1> returning(TypeToken<R1> returnType) {
      if (!returnType.isSupertypeOf(this.getReturnType())) {
         throw new IllegalArgumentException("Invokable is known to return " + this.getReturnType() + ", not " + returnType);
      } else {
         return this;
      }
   }

   @Override
   public final Class<? super T> getDeclaringClass() {
      return (Class<? super T>)this.member.getDeclaringClass();
   }

   public TypeToken<T> getOwnerType() {
      return TypeToken.of((Class<T>)this.getDeclaringClass());
   }

   abstract @Nullable Object invokeInternal(@Nullable Object receiver, @Nullable Object[] args) throws InvocationTargetException, IllegalAccessException;

   abstract Type[] getGenericParameterTypes();

   abstract AnnotatedType[] getAnnotatedParameterTypes();

   abstract Type[] getGenericExceptionTypes();

   abstract Annotation[][] getParameterAnnotations();

   abstract Type getGenericReturnType();

   public abstract AnnotatedType getAnnotatedReturnType();

   private static boolean initAnnotatedTypeExists() {
      try {
         Class.forName("java.lang.reflect.AnnotatedType");
         return true;
      } catch (ClassNotFoundException e) {
         return false;
      }
   }

   static class ConstructorInvokable<T> extends Invokable<T, T> {
      final Constructor<?> constructor;

      ConstructorInvokable(Constructor<?> constructor) {
         super(constructor);
         this.constructor = constructor;
      }

      @Override
      final Object invokeInternal(@Nullable Object receiver, @Nullable Object[] args) throws InvocationTargetException, IllegalAccessException {
         try {
            return this.constructor.newInstance(args);
         } catch (InstantiationException e) {
            throw new RuntimeException(this.constructor + " failed.", e);
         }
      }

      @Override
      Type getGenericReturnType() {
         Class<?> declaringClass = this.getDeclaringClass();
         TypeVariable<?>[] typeParams = declaringClass.getTypeParameters();
         return typeParams.length > 0 ? Types.newParameterizedType(declaringClass, typeParams) : declaringClass;
      }

      @Override
      Type[] getGenericParameterTypes() {
         Type[] types = this.constructor.getGenericParameterTypes();
         if (types.length > 0 && this.mayNeedHiddenThis()) {
            Class<?>[] rawParamTypes = this.constructor.getParameterTypes();
            if (types.length == rawParamTypes.length && rawParamTypes[0] == this.getDeclaringClass().getEnclosingClass()) {
               return Arrays.copyOfRange(types, 1, types.length);
            }
         }

         return types;
      }

      @Override
      AnnotatedType[] getAnnotatedParameterTypes() {
         return this.constructor.getAnnotatedParameterTypes();
      }

      @Override
      public AnnotatedType getAnnotatedReturnType() {
         return this.constructor.getAnnotatedReturnType();
      }

      @Override
      Type[] getGenericExceptionTypes() {
         return this.constructor.getGenericExceptionTypes();
      }

      @Override
      final Annotation[][] getParameterAnnotations() {
         return this.constructor.getParameterAnnotations();
      }

      @Override
      public final TypeVariable<?>[] getTypeParameters() {
         TypeVariable<?>[] declaredByClass = this.getDeclaringClass().getTypeParameters();
         TypeVariable<?>[] declaredByConstructor = this.constructor.getTypeParameters();
         TypeVariable<?>[] result = new TypeVariable[declaredByClass.length + declaredByConstructor.length];
         System.arraycopy(declaredByClass, 0, result, 0, declaredByClass.length);
         System.arraycopy(declaredByConstructor, 0, result, declaredByClass.length, declaredByConstructor.length);
         return result;
      }

      @Override
      public final boolean isOverridable() {
         return false;
      }

      @Override
      public final boolean isVarArgs() {
         return this.constructor.isVarArgs();
      }

      private boolean mayNeedHiddenThis() {
         Class<?> declaringClass = this.constructor.getDeclaringClass();
         if (declaringClass.getEnclosingConstructor() != null) {
            return true;
         }

         Method enclosingMethod = declaringClass.getEnclosingMethod();
         return enclosingMethod != null
            ? !Modifier.isStatic(enclosingMethod.getModifiers())
            : declaringClass.getEnclosingClass() != null && !Modifier.isStatic(declaringClass.getModifiers());
      }
   }

   static class MethodInvokable<T> extends Invokable<T, Object> {
      final Method method;

      MethodInvokable(Method method) {
         super(method);
         this.method = method;
      }

      @Override
      final @Nullable Object invokeInternal(@Nullable Object receiver, @Nullable Object[] args) throws InvocationTargetException, IllegalAccessException {
         return this.method.invoke(receiver, args);
      }

      @Override
      Type getGenericReturnType() {
         return this.method.getGenericReturnType();
      }

      @Override
      Type[] getGenericParameterTypes() {
         return this.method.getGenericParameterTypes();
      }

      @Override
      AnnotatedType[] getAnnotatedParameterTypes() {
         return this.method.getAnnotatedParameterTypes();
      }

      @Override
      public AnnotatedType getAnnotatedReturnType() {
         return this.method.getAnnotatedReturnType();
      }

      @Override
      Type[] getGenericExceptionTypes() {
         return this.method.getGenericExceptionTypes();
      }

      @Override
      final Annotation[][] getParameterAnnotations() {
         return this.method.getParameterAnnotations();
      }

      @Override
      public final TypeVariable<?>[] getTypeParameters() {
         return this.method.getTypeParameters();
      }

      @Override
      public final boolean isOverridable() {
         return !this.isFinal() && !this.isPrivate() && !this.isStatic() && !Modifier.isFinal(this.getDeclaringClass().getModifiers());
      }

      @Override
      public final boolean isVarArgs() {
         return this.method.isVarArgs();
      }
   }
}
