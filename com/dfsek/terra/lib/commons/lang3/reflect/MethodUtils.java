package com.dfsek.terra.lib.commons.lang3.reflect;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import com.dfsek.terra.lib.commons.lang3.Validate;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodUtils {
   private static final Comparator<Method> METHOD_BY_SIGNATURE = Comparator.comparing(Method::toString);

   private static int distance(Class<?>[] fromClassArray, Class<?>[] toClassArray) {
      int answer = 0;
      if (!ClassUtils.isAssignable(fromClassArray, toClassArray, true)) {
         return -1;
      }

      for (int offset = 0; offset < fromClassArray.length; offset++) {
         Class<?> aClass = fromClassArray[offset];
         Class<?> toClass = toClassArray[offset];
         if (aClass != null && !aClass.equals(toClass)) {
            if (ClassUtils.isAssignable(aClass, toClass, true) && !ClassUtils.isAssignable(aClass, toClass, false)) {
               answer++;
            } else {
               answer += 2;
            }
         }
      }

      return answer;
   }

   public static Method getAccessibleMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
      return getAccessibleMethod(getMethodObject(cls, methodName, parameterTypes));
   }

   public static Method getAccessibleMethod(Method method) {
      if (!MemberUtils.isAccessible(method)) {
         return null;
      }

      Class<?> cls = method.getDeclaringClass();
      if (ClassUtils.isPublic(cls)) {
         return method;
      }

      String methodName = method.getName();
      Class<?>[] parameterTypes = method.getParameterTypes();
      method = getAccessibleMethodFromInterfaceNest(cls, methodName, parameterTypes);
      if (method == null) {
         method = getAccessibleMethodFromSuperclass(cls, methodName, parameterTypes);
      }

      return method;
   }

   private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls, String methodName, Class<?>... parameterTypes) {
      while (cls != null) {
         Class<?>[] interfaces = cls.getInterfaces();

         for (Class<?> anInterface : interfaces) {
            if (ClassUtils.isPublic(anInterface)) {
               try {
                  return anInterface.getDeclaredMethod(methodName, parameterTypes);
               } catch (NoSuchMethodException var9) {
                  Method method = getAccessibleMethodFromInterfaceNest(anInterface, methodName, parameterTypes);
                  if (method != null) {
                     return method;
                  }
               }
            }
         }

         cls = cls.getSuperclass();
      }

      return null;
   }

   private static Method getAccessibleMethodFromSuperclass(Class<?> cls, String methodName, Class<?>... parameterTypes) {
      for (Class<?> parentClass = cls.getSuperclass(); parentClass != null; parentClass = parentClass.getSuperclass()) {
         if (ClassUtils.isPublic(parentClass)) {
            return getMethodObject(parentClass, methodName, parameterTypes);
         }
      }

      return null;
   }

   private static List<Class<?>> getAllSuperclassesAndInterfaces(Class<?> cls) {
      if (cls == null) {
         return null;
      }

      List<Class<?>> allSuperClassesAndInterfaces = new ArrayList<>();
      List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(cls);
      int superClassIndex = 0;
      List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(cls);
      int interfaceIndex = 0;

      while (interfaceIndex < allInterfaces.size() || superClassIndex < allSuperclasses.size()) {
         Class<?> acls;
         if (interfaceIndex >= allInterfaces.size()) {
            acls = allSuperclasses.get(superClassIndex++);
         } else if (superClassIndex < allSuperclasses.size() && superClassIndex < interfaceIndex) {
            acls = allSuperclasses.get(superClassIndex++);
         } else {
            acls = allInterfaces.get(interfaceIndex++);
         }

         allSuperClassesAndInterfaces.add(acls);
      }

      return allSuperClassesAndInterfaces;
   }

   public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationCls, boolean searchSupers, boolean ignoreAccess) {
      Objects.requireNonNull(method, "method");
      Objects.requireNonNull(annotationCls, "annotationCls");
      if (!ignoreAccess && !MemberUtils.isAccessible(method)) {
         return null;
      }

      A annotation = method.getAnnotation(annotationCls);
      if (annotation == null && searchSupers) {
         Class<?> mcls = method.getDeclaringClass();

         for (Class<?> acls : getAllSuperclassesAndInterfaces(mcls)) {
            Method equivalentMethod = ignoreAccess
               ? getMatchingMethod(acls, method.getName(), method.getParameterTypes())
               : getMatchingAccessibleMethod(acls, method.getName(), method.getParameterTypes());
            if (equivalentMethod != null) {
               annotation = equivalentMethod.getAnnotation(annotationCls);
               if (annotation != null) {
                  break;
               }
            }
         }
      }

      return annotation;
   }

   public static Method getMatchingAccessibleMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
      Method candidate = getMethodObject(cls, methodName, parameterTypes);
      if (candidate != null) {
         return MemberUtils.setAccessibleWorkaround(candidate);
      }

      Method[] methods = cls.getMethods();
      List<Method> matchingMethods = Stream.of(methods)
         .filter(method -> method.getName().equals(methodName) && MemberUtils.isMatchingMethod(method, parameterTypes))
         .collect(Collectors.toList());
      matchingMethods.sort(METHOD_BY_SIGNATURE);
      Method bestMatch = null;

      for (Method method : matchingMethods) {
         Method accessibleMethod = getAccessibleMethod(method);
         if (accessibleMethod != null && (bestMatch == null || MemberUtils.compareMethodFit(accessibleMethod, bestMatch, parameterTypes) < 0)) {
            bestMatch = accessibleMethod;
         }
      }

      if (bestMatch != null) {
         MemberUtils.setAccessibleWorkaround(bestMatch);
      }

      if (bestMatch != null && bestMatch.isVarArgs() && bestMatch.getParameterTypes().length > 0 && parameterTypes.length > 0) {
         Class<?>[] methodParameterTypes = bestMatch.getParameterTypes();
         Class<?> methodParameterComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
         String methodParameterComponentTypeName = ClassUtils.primitiveToWrapper(methodParameterComponentType).getName();
         Class<?> lastParameterType = parameterTypes[parameterTypes.length - 1];
         String parameterTypeName = lastParameterType == null ? null : lastParameterType.getName();
         String parameterTypeSuperClassName = lastParameterType == null ? null : lastParameterType.getSuperclass().getName();
         if (parameterTypeName != null
            && parameterTypeSuperClassName != null
            && !methodParameterComponentTypeName.equals(parameterTypeName)
            && !methodParameterComponentTypeName.equals(parameterTypeSuperClassName)) {
            return null;
         }
      }

      return bestMatch;
   }

   public static Method getMatchingMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
      Objects.requireNonNull(cls, "cls");
      Validate.notEmpty(methodName, "methodName");
      List<Method> methods = Stream.of(cls.getDeclaredMethods()).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
      ClassUtils.getAllSuperclasses(cls)
         .stream()
         .map(Class::getDeclaredMethods)
         .flatMap(Stream::of)
         .filter(method -> method.getName().equals(methodName))
         .forEach(methods::add);

      for (Method method : methods) {
         if (Arrays.deepEquals(method.getParameterTypes(), parameterTypes)) {
            return method;
         }
      }

      TreeMap<Integer, List<Method>> candidates = new TreeMap<>();
      methods.stream().filter(method -> ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true)).forEach(method -> {
         int distance = distance(parameterTypes, method.getParameterTypes());
         List<Method> candidatesAtDistance = candidates.computeIfAbsent(distance, k -> new ArrayList<>());
         candidatesAtDistance.add(method);
      });
      if (candidates.isEmpty()) {
         return null;
      } else {
         List<Method> bestCandidates = candidates.values().iterator().next();
         if (bestCandidates.size() != 1 && Objects.equals(bestCandidates.get(0).getDeclaringClass(), bestCandidates.get(1).getDeclaringClass())) {
            throw new IllegalStateException(
               String.format(
                  "Found multiple candidates for method %s on class %s : %s",
                  methodName + Stream.of(parameterTypes).map(String::valueOf).collect(Collectors.joining(",", "(", ")")),
                  cls.getName(),
                  bestCandidates.stream().map(Method::toString).collect(Collectors.joining(",", "[", "]"))
               )
            );
         } else {
            return bestCandidates.get(0);
         }
      }
   }

   public static Method getMethodObject(Class<?> cls, String name, Class<?>... parameterTypes) {
      try {
         return cls.getMethod(name, parameterTypes);
      } catch (NoSuchMethodException | SecurityException e) {
         return null;
      }
   }

   public static List<Method> getMethodsListWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
      return getMethodsListWithAnnotation(cls, annotationCls, false, false);
   }

   public static List<Method> getMethodsListWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls, boolean searchSupers, boolean ignoreAccess) {
      Objects.requireNonNull(cls, "cls");
      Objects.requireNonNull(annotationCls, "annotationCls");
      List<Class<?>> classes = searchSupers ? getAllSuperclassesAndInterfaces(cls) : new ArrayList<>();
      classes.add(0, cls);
      List<Method> annotatedMethods = new ArrayList<>();
      classes.forEach(acls -> {
         Method[] methods = ignoreAccess ? acls.getDeclaredMethods() : acls.getMethods();
         Stream.of(methods).filter(method -> method.isAnnotationPresent(annotationCls)).forEachOrdered(annotatedMethods::add);
      });
      return annotatedMethods;
   }

   public static Method[] getMethodsWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
      return getMethodsWithAnnotation(cls, annotationCls, false, false);
   }

   public static Method[] getMethodsWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls, boolean searchSupers, boolean ignoreAccess) {
      return getMethodsListWithAnnotation(cls, annotationCls, searchSupers, ignoreAccess).toArray(ArrayUtils.EMPTY_METHOD_ARRAY);
   }

   public static Set<Method> getOverrideHierarchy(Method method, ClassUtils.Interfaces interfacesBehavior) {
      Objects.requireNonNull(method, "method");
      Set<Method> result = new LinkedHashSet<>();
      result.add(method);
      Class<?>[] parameterTypes = method.getParameterTypes();
      Class<?> declaringClass = method.getDeclaringClass();
      Iterator<Class<?>> hierarchy = ClassUtils.hierarchy(declaringClass, interfacesBehavior).iterator();
      hierarchy.next();

      label34:
      while (hierarchy.hasNext()) {
         Class<?> c = hierarchy.next();
         Method m = getMatchingAccessibleMethod(c, method.getName(), parameterTypes);
         if (m != null) {
            if (Arrays.equals(m.getParameterTypes(), parameterTypes)) {
               result.add(m);
            } else {
               Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(declaringClass, m.getDeclaringClass());

               for (int i = 0; i < parameterTypes.length; i++) {
                  Type childType = TypeUtils.unrollVariables(typeArguments, method.getGenericParameterTypes()[i]);
                  Type parentType = TypeUtils.unrollVariables(typeArguments, m.getGenericParameterTypes()[i]);
                  if (!TypeUtils.equals(childType, parentType)) {
                     continue label34;
                  }
               }

               result.add(m);
            }
         }
      }

      return result;
   }

   static Object[] getVarArgs(Object[] args, Class<?>[] methodParameterTypes) {
      if (args.length != methodParameterTypes.length
         || args[args.length - 1] != null && !args[args.length - 1].getClass().equals(methodParameterTypes[methodParameterTypes.length - 1])) {
         Object[] newArgs = ArrayUtils.arraycopy(args, 0, 0, methodParameterTypes.length - 1, () -> new Object[methodParameterTypes.length]);
         Class<?> varArgComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
         int varArgLength = args.length - methodParameterTypes.length + 1;
         Object varArgsArray = ArrayUtils.arraycopy(
            args,
            methodParameterTypes.length - 1,
            0,
            varArgLength,
            s -> (Object[])Array.newInstance(ClassUtils.primitiveToWrapper(varArgComponentType), varArgLength)
         );
         if (varArgComponentType.isPrimitive()) {
            varArgsArray = ArrayUtils.toPrimitive(varArgsArray);
         }

         newArgs[methodParameterTypes.length - 1] = varArgsArray;
         return newArgs;
      } else {
         return args;
      }
   }

   public static Object invokeExactMethod(Object object, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      return invokeExactMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
   }

   public static Object invokeExactMethod(Object object, String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      args = ArrayUtils.nullToEmpty(args);
      return invokeExactMethod(object, methodName, args, ClassUtils.toClass(args));
   }

   public static Object invokeExactMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      Objects.requireNonNull(object, "object");
      args = ArrayUtils.nullToEmpty(args);
      parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
      Class<?> cls = object.getClass();
      Method method = getAccessibleMethod(cls, methodName, parameterTypes);
      if (method == null) {
         throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + cls.getName());
      } else {
         return method.invoke(object, args);
      }
   }

   public static Object invokeExactStaticMethod(Class<?> cls, String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      args = ArrayUtils.nullToEmpty(args);
      return invokeExactStaticMethod(cls, methodName, args, ClassUtils.toClass(args));
   }

   public static Object invokeExactStaticMethod(Class<?> cls, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      args = ArrayUtils.nullToEmpty(args);
      parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
      Method method = getAccessibleMethod(cls, methodName, parameterTypes);
      if (method == null) {
         throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + cls.getName());
      } else {
         return method.invoke(null, args);
      }
   }

   public static Object invokeMethod(Object object, boolean forceAccess, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      return invokeMethod(object, forceAccess, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
   }

   public static Object invokeMethod(Object object, boolean forceAccess, String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      args = ArrayUtils.nullToEmpty(args);
      return invokeMethod(object, forceAccess, methodName, args, ClassUtils.toClass(args));
   }

   public static Object invokeMethod(Object object, boolean forceAccess, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      Objects.requireNonNull(object, "object");
      parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
      args = ArrayUtils.nullToEmpty(args);
      Class<? extends Object> cls = (Class<? extends Object>)object.getClass();
      String messagePrefix;
      Method method;
      if (forceAccess) {
         messagePrefix = "No such method: ";
         method = getMatchingMethod(cls, methodName, parameterTypes);
         if (method != null && !method.isAccessible()) {
            method.setAccessible(true);
         }
      } else {
         messagePrefix = "No such accessible method: ";
         method = getMatchingAccessibleMethod(cls, methodName, parameterTypes);
      }

      if (method == null) {
         throw new NoSuchMethodException(messagePrefix + methodName + "() on object: " + cls.getName());
      }

      args = toVarArgs(method, args);
      return method.invoke(object, args);
   }

   public static Object invokeMethod(Object object, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      return invokeMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
   }

   public static Object invokeMethod(Object object, String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      args = ArrayUtils.nullToEmpty(args);
      return invokeMethod(object, methodName, args, ClassUtils.toClass(args));
   }

   public static Object invokeMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      return invokeMethod(object, false, methodName, args, parameterTypes);
   }

   public static Object invokeStaticMethod(Class<?> cls, String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      args = ArrayUtils.nullToEmpty(args);
      return invokeStaticMethod(cls, methodName, args, ClassUtils.toClass(args));
   }

   public static Object invokeStaticMethod(Class<?> cls, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      args = ArrayUtils.nullToEmpty(args);
      parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
      Method method = getMatchingAccessibleMethod(cls, methodName, parameterTypes);
      if (method == null) {
         throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + cls.getName());
      }

      args = toVarArgs(method, args);
      return method.invoke(null, args);
   }

   private static Object[] toVarArgs(Method method, Object[] args) {
      if (method.isVarArgs()) {
         Class<?>[] methodParameterTypes = method.getParameterTypes();
         args = getVarArgs(args, methodParameterTypes);
      }

      return args;
   }
}
