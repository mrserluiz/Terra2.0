package com.dfsek.terra.lib.commons.lang3.reflect;

import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class MemberUtils {
   private static final int ACCESS_TEST = 7;
   private static final Class<?>[] ORDERED_PRIMITIVE_TYPES = new Class[]{byte.class, short.class, char.class, int.class, long.class, float.class, double.class};

   static int compareConstructorFit(Constructor<?> left, Constructor<?> right, Class<?>[] actual) {
      return compareParameterTypes(MemberUtils.Executable.of(left), MemberUtils.Executable.of(right), actual);
   }

   static int compareMethodFit(Method left, Method right, Class<?>[] actual) {
      return compareParameterTypes(MemberUtils.Executable.of(left), MemberUtils.Executable.of(right), actual);
   }

   private static int compareParameterTypes(MemberUtils.Executable left, MemberUtils.Executable right, Class<?>[] actual) {
      float leftCost = getTotalTransformationCost(actual, left);
      float rightCost = getTotalTransformationCost(actual, right);
      return Float.compare(leftCost, rightCost);
   }

   private static float getObjectTransformationCost(Class<?> srcClass, Class<?> destClass) {
      if (destClass.isPrimitive()) {
         return getPrimitivePromotionCost(srcClass, destClass);
      }

      float cost = 0.0F;

      while (srcClass != null && !destClass.equals(srcClass)) {
         if (destClass.isInterface() && ClassUtils.isAssignable(srcClass, destClass)) {
            cost += 0.25F;
            break;
         }

         cost++;
         srcClass = srcClass.getSuperclass();
      }

      if (srcClass == null) {
         cost += 1.5F;
      }

      return cost;
   }

   private static float getPrimitivePromotionCost(Class<?> srcClass, Class<?> destClass) {
      if (srcClass == null) {
         return 1.5F;
      }

      float cost = 0.0F;
      Class<?> cls = srcClass;
      if (!cls.isPrimitive()) {
         cost += 0.1F;
         cls = ClassUtils.wrapperToPrimitive(cls);
      }

      for (int i = 0; cls != destClass && i < ORDERED_PRIMITIVE_TYPES.length; i++) {
         if (cls == ORDERED_PRIMITIVE_TYPES[i]) {
            cost += 0.1F;
            if (i < ORDERED_PRIMITIVE_TYPES.length - 1) {
               cls = ORDERED_PRIMITIVE_TYPES[i + 1];
            }
         }
      }

      return cost;
   }

   private static float getTotalTransformationCost(Class<?>[] srcArgs, MemberUtils.Executable executable) {
      Class<?>[] destArgs = executable.getParameterTypes();
      boolean isVarArgs = executable.isVarArgs();
      float totalCost = 0.0F;
      long normalArgsLen = isVarArgs ? destArgs.length - 1 : destArgs.length;
      if (srcArgs.length < normalArgsLen) {
         return Float.MAX_VALUE;
      }

      for (int i = 0; i < normalArgsLen; i++) {
         totalCost += getObjectTransformationCost(srcArgs[i], destArgs[i]);
      }

      if (isVarArgs) {
         boolean noVarArgsPassed = srcArgs.length < destArgs.length;
         boolean explicitArrayForVarargs = srcArgs.length == destArgs.length && srcArgs[srcArgs.length - 1] != null && srcArgs[srcArgs.length - 1].isArray();
         float varArgsCost = 0.001F;
         Class<?> destClass = destArgs[destArgs.length - 1].getComponentType();
         if (noVarArgsPassed) {
            totalCost += getObjectTransformationCost(destClass, Object.class) + 0.001F;
         } else if (explicitArrayForVarargs) {
            Class<?> sourceClass = srcArgs[srcArgs.length - 1].getComponentType();
            totalCost += getObjectTransformationCost(sourceClass, destClass) + 0.001F;
         } else {
            for (int i = destArgs.length - 1; i < srcArgs.length; i++) {
               Class<?> srcClass = srcArgs[i];
               totalCost += getObjectTransformationCost(srcClass, destClass) + 0.001F;
            }
         }
      }

      return totalCost;
   }

   static boolean isAccessible(Member member) {
      return isPublic(member) && !member.isSynthetic();
   }

   static boolean isMatchingConstructor(Constructor<?> method, Class<?>[] parameterTypes) {
      return isMatchingExecutable(MemberUtils.Executable.of(method), parameterTypes);
   }

   private static boolean isMatchingExecutable(MemberUtils.Executable method, Class<?>[] parameterTypes) {
      Class<?>[] methodParameterTypes = method.getParameterTypes();
      if (ClassUtils.isAssignable(parameterTypes, methodParameterTypes, true)) {
         return true;
      }

      if (!method.isVarArgs()) {
         return false;
      }

      int i;
      for (i = 0; i < methodParameterTypes.length - 1 && i < parameterTypes.length; i++) {
         if (!ClassUtils.isAssignable(parameterTypes[i], methodParameterTypes[i], true)) {
            return false;
         }
      }

      Class<?> varArgParameterType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();

      while (i < parameterTypes.length) {
         if (!ClassUtils.isAssignable(parameterTypes[i], varArgParameterType, true)) {
            return false;
         }

         i++;
      }

      return true;
   }

   static boolean isMatchingMethod(Method method, Class<?>[] parameterTypes) {
      return isMatchingExecutable(MemberUtils.Executable.of(method), parameterTypes);
   }

   static boolean isPackageAccess(int modifiers) {
      return (modifiers & 7) == 0;
   }

   static boolean isPublic(Member member) {
      return member != null && Modifier.isPublic(member.getModifiers());
   }

   static boolean isStatic(Member member) {
      return member != null && Modifier.isStatic(member.getModifiers());
   }

   static <T extends AccessibleObject> T setAccessibleWorkaround(T obj) {
      if (obj != null && !obj.isAccessible()) {
         Member m = (Member)obj;
         if (!obj.isAccessible() && isPublic(m) && isPackageAccess(m.getDeclaringClass().getModifiers())) {
            try {
               obj.setAccessible(true);
               return obj;
            } catch (SecurityException var3) {
            }
         }

         return obj;
      } else {
         return obj;
      }
   }

   private static final class Executable {
      private final Class<?>[] parameterTypes;
      private final boolean isVarArgs;

      private static MemberUtils.Executable of(Constructor<?> constructor) {
         return new MemberUtils.Executable(constructor);
      }

      private static MemberUtils.Executable of(Method method) {
         return new MemberUtils.Executable(method);
      }

      private Executable(Constructor<?> constructor) {
         this.parameterTypes = constructor.getParameterTypes();
         this.isVarArgs = constructor.isVarArgs();
      }

      private Executable(Method method) {
         this.parameterTypes = method.getParameterTypes();
         this.isVarArgs = method.isVarArgs();
      }

      public Class<?>[] getParameterTypes() {
         return this.parameterTypes;
      }

      public boolean isVarArgs() {
         return this.isVarArgs;
      }
   }
}
