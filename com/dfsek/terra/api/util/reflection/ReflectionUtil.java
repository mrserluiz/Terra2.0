package com.dfsek.terra.api.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

public final class ReflectionUtil {
   private static final Unsafe UNSAFE;

   public static Field[] getFields(@NotNull Class<?> type) {
      Field[] result = type.getDeclaredFields();
      Class<?> parentClass = type.getSuperclass();
      if (parentClass != null) {
         result = Stream.concat(Arrays.stream(result), Arrays.stream(getFields(parentClass))).toArray(Field[]::new);
      }

      return result;
   }

   public static void setFinalField(Object obj, String fieldName, Object value) throws NoSuchFieldException {
      Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      long fieldOffset = UNSAFE.objectFieldOffset(field);
      UNSAFE.putObject(obj, fieldOffset, value);
   }

   public static Method[] getMethods(@NotNull Class<?> type) {
      Method[] result = type.getDeclaredMethods();
      Class<?> parentClass = type.getSuperclass();
      if (parentClass != null) {
         result = Stream.concat(Arrays.stream(result), Arrays.stream(getMethods(parentClass))).toArray(Method[]::new);
      }

      return result;
   }

   public static <T extends Annotation> void ifAnnotationPresent(AnnotatedElement element, Class<? extends T> annotation, Consumer<T> operation) {
      T a = element.getAnnotation((Class<T>)annotation);
      if (a != null) {
         operation.accept(a);
      }
   }

   public static Class<?> getRawType(Type type) {
      if (type instanceof Class) {
         return (Class<?>)type;
      } else if (type instanceof ParameterizedType parameterizedType) {
         Type rawType = parameterizedType.getRawType();
         return (Class<?>)rawType;
      } else {
         if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType)type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
         }

         if (type instanceof TypeVariable) {
            return Object.class;
         }

         if (type instanceof WildcardType) {
            return getRawType(((WildcardType)type).getUpperBounds()[0]);
         }

         String className = type == null ? "null" : type.getClass().getName();
         throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + className);
      }
   }

   public static String typeToString(Type type) {
      return type instanceof Class ? ((Class)type).getName() : type.toString();
   }

   public static boolean equals(Type a, Type b) {
      if (a == b) {
         return true;
      } else if (a instanceof Class) {
         return a.equals(b);
      } else if (a instanceof ParameterizedType pa) {
         return !(b instanceof ParameterizedType pb)
            ? false
            : Objects.equals(pa.getOwnerType(), pb.getOwnerType())
               && pa.getRawType().equals(pb.getRawType())
               && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
      } else if (a instanceof GenericArrayType ga) {
         return b instanceof GenericArrayType gb ? equals(ga.getGenericComponentType(), gb.getGenericComponentType()) : false;
      } else if (a instanceof WildcardType wa) {
         return !(b instanceof WildcardType wb)
            ? false
            : Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds()) && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());
      } else if (a instanceof TypeVariable<?> va) {
         return !(b instanceof TypeVariable<?> vb) ? false : va.getGenericDeclaration() == vb.getGenericDeclaration() && va.getName().equals(vb.getName());
      } else {
         return false;
      }
   }

   static {
      try {
         Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
         unsafeField.setAccessible(true);
         UNSAFE = (Unsafe)unsafeField.get(null);
      } catch (NoSuchFieldException | IllegalAccessException e) {
         throw new RuntimeException(e);
      }
   }
}
