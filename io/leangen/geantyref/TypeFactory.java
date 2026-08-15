package io.leangen.geantyref;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class TypeFactory {
   private static final WildcardType UNBOUND_WILDCARD = new WildcardTypeImpl(new Type[]{Object.class}, new Type[0]);

   public static Type parameterizedClass(Class<?> clazz, Type... arguments) {
      return parameterizedInnerClass(null, clazz, arguments);
   }

   public static AnnotatedType annotatedClass(Class<?> clazz, Annotation[] annotations) {
      return parameterizedAnnotatedClass(clazz, annotations);
   }

   public static AnnotatedType parameterizedAnnotatedClass(Class<?> clazz, Annotation[] annotations, AnnotatedType... arguments) {
      return parameterizedAnnotatedInnerClass(null, clazz, annotations, arguments);
   }

   public static AnnotatedType annotatedInnerClass(Type owner, Class<?> clazz, Annotation[] annotations) {
      return parameterizedAnnotatedInnerClass(owner, clazz, annotations);
   }

   public static AnnotatedType parameterizedAnnotatedInnerClass(Type owner, Class<?> clazz, Annotation[] annotations, AnnotatedType... arguments) {
      if (arguments != null && arguments.length != 0) {
         Type[] typeArguments = Arrays.stream(arguments).map(AnnotatedType::getType).toArray(Type[]::new);
         return new AnnotatedParameterizedTypeImpl((ParameterizedType)parameterizedInnerClass(owner, clazz, typeArguments), annotations, arguments);
      } else {
         return GenericTypeReflector.annotate(clazz, annotations);
      }
   }

   public static AnnotatedParameterizedType parameterizedAnnotatedType(
      ParameterizedType type, Annotation[] typeAnnotations, Annotation[]... argumentAnnotations
   ) {
      if (argumentAnnotations != null && argumentAnnotations.length != 0) {
         AnnotatedType[] typeArguments = new AnnotatedType[type.getActualTypeArguments().length];

         for (int i = 0; i < typeArguments.length; i++) {
            Annotation[] annotations = argumentAnnotations.length > i ? argumentAnnotations[i] : null;
            typeArguments[i] = GenericTypeReflector.annotate(type.getActualTypeArguments()[i], annotations);
         }

         return (AnnotatedParameterizedType)parameterizedAnnotatedClass(GenericTypeReflector.erase(type), typeAnnotations, typeArguments);
      } else {
         return (AnnotatedParameterizedType)GenericTypeReflector.annotate(type, typeAnnotations);
      }
   }

   public static Type innerClass(Type owner, Class<?> clazz) {
      return parameterizedInnerClass(owner, clazz, (Type[])null);
   }

   public static Type parameterizedInnerClass(Type owner, Class<?> clazz, Type... arguments) {
      if (clazz.getDeclaringClass() == null && owner != null) {
         throw new IllegalArgumentException("Cannot specify an owner type for a top level class");
      }

      Type realOwner = transformOwner(owner, clazz);
      if (arguments == null) {
         if (clazz.getTypeParameters().length != 0) {
            return clazz;
         }

         arguments = new Type[0];
      } else if (arguments.length != clazz.getTypeParameters().length) {
         throw new IllegalArgumentException(
            "Incorrect number of type arguments for [" + clazz + "]: expected " + clazz.getTypeParameters().length + ", but got " + arguments.length
         );
      }

      if (!GenericTypeReflector.isMissingTypeParameters(clazz)) {
         return clazz;
      }

      if (realOwner != null && !Modifier.isStatic(clazz.getModifiers()) && GenericTypeReflector.isMissingTypeParameters(realOwner)) {
         return clazz;
      }

      ParameterizedType result = new ParameterizedTypeImpl(clazz, arguments, realOwner);
      checkParametersWithinBound(result);
      return result;
   }

   private static void checkParametersWithinBound(ParameterizedType type) {
      Type[] arguments = type.getActualTypeArguments();
      TypeVariable<?>[] typeParameters = ((Class)type.getRawType()).getTypeParameters();
      VarMap varMap = new VarMap(type);

      for (int i = 0; i < arguments.length; i++) {
         for (Type bound : typeParameters[i].getBounds()) {
            Type replacedBound = varMap.map(bound);
            if (!(arguments[i] instanceof WildcardType)) {
               if (!GenericTypeReflector.isSuperType(replacedBound, arguments[i])) {
                  throw new TypeArgumentNotInBoundException(arguments[i], typeParameters[i], bound);
               }
            } else {
               WildcardType wildcardTypeParameter = (WildcardType)arguments[i];

               for (Type wildcardUpperBound : wildcardTypeParameter.getUpperBounds()) {
                  if (!couldHaveCommonSubtype(replacedBound, wildcardUpperBound)) {
                     throw new TypeArgumentNotInBoundException(arguments[i], typeParameters[i], bound);
                  }
               }

               for (Type wildcardLowerBound : wildcardTypeParameter.getLowerBounds()) {
                  if (!GenericTypeReflector.isSuperType(replacedBound, wildcardLowerBound)) {
                     throw new TypeArgumentNotInBoundException(arguments[i], typeParameters[i], bound);
                  }
               }
            }
         }
      }
   }

   private static boolean couldHaveCommonSubtype(Type type1, Type type2) {
      Class<?> erased1 = GenericTypeReflector.erase(type1);
      Class<?> erased2 = GenericTypeReflector.erase(type2);
      return erased1.isInterface() || erased2.isInterface() || erased1.isAssignableFrom(erased2) || erased2.isAssignableFrom(erased1);
   }

   private static Type transformOwner(Type givenOwner, Class<?> clazz) {
      if (givenOwner == null) {
         return clazz.getDeclaringClass();
      } else {
         Type transformedOwner = GenericTypeReflector.getExactSuperType(GenericTypeReflector.annotate(givenOwner).getType(), clazz.getDeclaringClass());
         if (transformedOwner == null) {
            throw new IllegalArgumentException(
               "Given owner type [" + givenOwner + "] is not appropriate for [" + clazz + "]: it should be a subtype of " + clazz.getDeclaringClass()
            );
         } else {
            return Modifier.isStatic(clazz.getModifiers()) ? GenericTypeReflector.erase(transformedOwner) : transformedOwner;
         }
      }
   }

   public static WildcardType unboundWildcard() {
      return UNBOUND_WILDCARD;
   }

   public static WildcardType wildcardExtends(Type upperBound) {
      if (upperBound == null) {
         throw new NullPointerException();
      } else {
         return new WildcardTypeImpl(new Type[]{upperBound}, new Type[0]);
      }
   }

   public static WildcardType wildcardSuper(Type lowerBound) {
      if (lowerBound == null) {
         throw new NullPointerException();
      } else {
         return new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{lowerBound});
      }
   }

   public static Type arrayOf(Type componentType) {
      return GenericArrayTypeImpl.createArrayType(componentType);
   }

   public static AnnotatedArrayType arrayOf(AnnotatedType componentType, Annotation[] annotations) {
      return AnnotatedArrayTypeImpl.createArrayType(componentType, annotations);
   }

   public static <A extends Annotation> A annotation(Class<A> annotationType, Map<String, Object> values) throws AnnotationFormatException {
      return (A)Proxy.newProxyInstance(
         annotationType.getClassLoader(),
         new Class[]{annotationType},
         new AnnotationInvocationHandler(annotationType, values == null ? Collections.emptyMap() : values)
      );
   }
}
