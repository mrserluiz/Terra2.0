package io.leangen.geantyref;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class GenericTypeReflector {
   private static final WildcardType UNBOUND_WILDCARD = new WildcardTypeImpl(new Type[]{Object.class}, new Type[0]);
   private static final Map<Class<?>, Class<?>> BOX_TYPES;

   public static Class<?> erase(Type type) {
      if (type instanceof Class) {
         return (Class<?>)type;
      } else if (type instanceof ParameterizedType) {
         return (Class<?>)((ParameterizedType)type).getRawType();
      } else if (type instanceof TypeVariable) {
         TypeVariable<?> tv = (TypeVariable<?>)type;
         return tv.getBounds().length == 0 ? Object.class : erase(tv.getBounds()[0]);
      } else if (type instanceof GenericArrayType) {
         GenericArrayType aType = (GenericArrayType)type;
         return GenericArrayTypeImpl.createArrayType(erase(aType.getGenericComponentType()));
      } else if (type instanceof WildcardType) {
         WildcardType wildcardType = (WildcardType)type;
         Type[] lowerBounds = wildcardType.getLowerBounds();
         return erase(lowerBounds.length > 0 ? lowerBounds[0] : wildcardType.getUpperBounds()[0]);
      } else if (type instanceof CaptureType) {
         CaptureType captureType = (CaptureType)type;
         Type[] lowerBounds = captureType.getLowerBounds();
         return erase(lowerBounds.length > 0 ? lowerBounds[0] : captureType.getUpperBounds()[0]);
      } else {
         throw new RuntimeException("not supported: " + type.getClass());
      }
   }

   public static Type box(Type type) {
      Class<?> boxed = BOX_TYPES.get(type);
      return boxed != null ? boxed : type;
   }

   public static boolean isBoxType(Type type) {
      return BOX_TYPES.containsValue(type);
   }

   public static boolean isFullyBound(Type type) {
      if (type instanceof Class) {
         return true;
      } else if (type instanceof ParameterizedType) {
         return Arrays.stream(((ParameterizedType)type).getActualTypeArguments()).allMatch(GenericTypeReflector::isFullyBound);
      } else {
         return type instanceof GenericArrayType ? isFullyBound(((GenericArrayType)type).getGenericComponentType()) : false;
      }
   }

   private static AnnotatedType mapTypeParameters(AnnotatedType toMapType, AnnotatedType typeAndParams) {
      return mapTypeParameters(toMapType, typeAndParams, VarMap.MappingMode.EXACT);
   }

   private static AnnotatedType mapTypeParameters(AnnotatedType toMapType, AnnotatedType typeAndParams, VarMap.MappingMode mappingMode) {
      if (isMissingTypeParameters(typeAndParams.getType())) {
         return new AnnotatedTypeImpl(erase(toMapType.getType()), toMapType.getAnnotations());
      }

      VarMap varMap = new VarMap();
      AnnotatedType handlingTypeAndParams = typeAndParams;

      while (handlingTypeAndParams instanceof AnnotatedParameterizedType) {
         AnnotatedParameterizedType pType = (AnnotatedParameterizedType)handlingTypeAndParams;
         Class<?> clazz = (Class<?>)((ParameterizedType)pType.getType()).getRawType();
         TypeVariable<?>[] vars = clazz.getTypeParameters();
         varMap.addAll(vars, pType.getAnnotatedActualTypeArguments());
         Type owner = ((ParameterizedType)pType.getType()).getOwnerType();
         handlingTypeAndParams = owner == null ? null : annotate(owner);
      }

      return varMap.map(toMapType, mappingMode);
   }

   public static AnnotatedType resolveExactType(AnnotatedType unresolved, AnnotatedType typeAndParams) {
      return resolveType(unresolved, expandGenerics(typeAndParams), VarMap.MappingMode.EXACT);
   }

   public static Type resolveExactType(Type unresolved, Type typeAndParams) {
      return resolveType(annotate(unresolved), annotate(typeAndParams, true), VarMap.MappingMode.EXACT).getType();
   }

   public static AnnotatedType resolveType(AnnotatedType unresolved, AnnotatedType typeAndParams) {
      return resolveType(unresolved, expandGenerics(typeAndParams), VarMap.MappingMode.ALLOW_INCOMPLETE);
   }

   public static Type resolveType(Type unresolved, Type typeAndParams) {
      return resolveType(annotate(unresolved), annotate(typeAndParams, true), VarMap.MappingMode.ALLOW_INCOMPLETE).getType();
   }

   private static AnnotatedType resolveType(AnnotatedType unresolved, AnnotatedType typeAndParams, VarMap.MappingMode mappingMode) {
      if (unresolved instanceof AnnotatedParameterizedType) {
         AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)unresolved;
         AnnotatedType[] params = mapArray(
            parameterizedType.getAnnotatedActualTypeArguments(), AnnotatedType[]::new, p -> resolveType(p, typeAndParams, mappingMode)
         );
         return replaceParameters(parameterizedType, params);
      }

      if (unresolved instanceof AnnotatedWildcardType) {
         AnnotatedType[] lower = mapArray(
            ((AnnotatedWildcardType)unresolved).getAnnotatedLowerBounds(), AnnotatedType[]::new, b -> resolveType(b, typeAndParams, mappingMode)
         );
         AnnotatedType[] upper = mapArray(
            ((AnnotatedWildcardType)unresolved).getAnnotatedUpperBounds(), AnnotatedType[]::new, b -> resolveType(b, typeAndParams, mappingMode)
         );
         return new AnnotatedWildcardTypeImpl((WildcardType)unresolved.getType(), unresolved.getAnnotations(), lower, upper);
      }

      if (unresolved instanceof AnnotatedTypeVariable) {
         TypeVariable<?> var = (TypeVariable<?>)unresolved.getType();
         if (var.getGenericDeclaration() instanceof Class) {
            AnnotatedType resolved = getTypeParameter(typeAndParams, (TypeVariable<? extends Class<?>>)var);
            if (resolved != null) {
               return updateAnnotations(resolved, unresolved.getAnnotations());
            }
         }

         if (mappingMode.equals(VarMap.MappingMode.ALLOW_INCOMPLETE)) {
            return unresolved;
         } else {
            throw new IllegalArgumentException(
               "Variable " + var.getName() + " is not declared by the given type " + typeAndParams.getType().getTypeName() + " or its super types"
            );
         }
      } else if (unresolved instanceof AnnotatedArrayType) {
         AnnotatedType componentType = resolveType(((AnnotatedArrayType)unresolved).getAnnotatedGenericComponentType(), typeAndParams, mappingMode);
         return new AnnotatedArrayTypeImpl(TypeFactory.arrayOf(componentType.getType()), unresolved.getAnnotations(), componentType);
      } else {
         return unresolved;
      }
   }

   public static boolean isMissingTypeParameters(Type type) {
      if (type instanceof Class) {
         Class<?> clazz = (Class<?>)type;
         if (Modifier.isStatic(clazz.getModifiers())) {
            return clazz.getTypeParameters().length != 0;
         }

         for (Class<?> enclosing = clazz; enclosing != null; enclosing = enclosing.getEnclosingClass()) {
            if (enclosing.getTypeParameters().length != 0) {
               return true;
            }
         }

         return false;
      } else if (type instanceof ParameterizedType) {
         return false;
      } else {
         throw new AssertionError("Unexpected type " + type.getClass());
      }
   }

   public static Type addWildcardParameters(Class<?> clazz) {
      if (clazz.isArray()) {
         return GenericArrayTypeImpl.createArrayType(addWildcardParameters(clazz.getComponentType()));
      } else if (isMissingTypeParameters(clazz)) {
         TypeVariable<?>[] vars = clazz.getTypeParameters();
         Type[] arguments = new Type[vars.length];
         Arrays.fill(arguments, UNBOUND_WILDCARD);
         Type owner = clazz.getDeclaringClass() == null ? null : addWildcardParameters(clazz.getDeclaringClass());
         return new ParameterizedTypeImpl(clazz, arguments, owner);
      } else {
         return clazz;
      }
   }

   public static AnnotatedType getExactSuperType(AnnotatedType subType, Class<?> searchSuperClass) {
      if (subType instanceof AnnotatedParameterizedType || subType.getType() instanceof Class || subType instanceof AnnotatedArrayType) {
         Class<?> superClass = erase(subType.getType());
         if (searchSuperClass == superClass) {
            return subType;
         }

         if (!searchSuperClass.isAssignableFrom(superClass)) {
            return null;
         }
      }

      for (AnnotatedType superType : getExactDirectSuperTypes(subType)) {
         AnnotatedType result = getExactSuperType(superType, searchSuperClass);
         if (result != null) {
            return result;
         }
      }

      return null;
   }

   public static Type getExactSuperType(Type subType, Class<?> searchSuperClass) {
      AnnotatedType superType = getExactSuperType(annotate(subType), searchSuperClass);
      return superType == null ? null : superType.getType();
   }

   public static AnnotatedType getExactSubType(AnnotatedType superType, Class<?> searchSubClass) {
      Type subType = searchSubClass;
      if (searchSubClass.getTypeParameters().length > 0) {
         subType = TypeFactory.parameterizedClass(searchSubClass, searchSubClass.getTypeParameters());
      }

      AnnotatedType annotatedSubType = annotate(subType);
      Class<?> rawSuperType = erase(superType.getType());
      if (searchSubClass.isArray() && superType instanceof AnnotatedArrayType) {
         return rawSuperType.isAssignableFrom(searchSubClass)
            ? AnnotatedArrayTypeImpl.createArrayType(
               getExactSubType(((AnnotatedArrayType)superType).getAnnotatedGenericComponentType(), searchSubClass.getComponentType()), new Annotation[0]
            )
            : null;
      }

      if (searchSubClass.getTypeParameters().length == 0) {
         return annotatedSubType;
      }

      if (!(superType instanceof AnnotatedParameterizedType)) {
         return annotate(searchSubClass);
      }

      AnnotatedParameterizedType parameterizedSuperType = (AnnotatedParameterizedType)superType;
      AnnotatedParameterizedType matched = (AnnotatedParameterizedType)getExactSuperType(annotatedSubType, rawSuperType);
      if (matched == null) {
         return null;
      }

      VarMap varMap = new VarMap();

      try {
         extractVariables(parameterizedSuperType, matched, searchSubClass, varMap);
         return varMap.map(annotatedSubType);
      } catch (UnresolvedTypeVariableException e) {
         return annotate(searchSubClass);
      } catch (IllegalArgumentException e) {
         return null;
      }
   }

   public static Type getExactSubType(Type superType, Class<?> searchSubClass) {
      AnnotatedType resolvedSubtype = getExactSubType(annotate(superType), searchSubClass);
      return resolvedSubtype == null ? null : resolvedSubtype.getType();
   }

   public static AnnotatedType getTypeParameter(AnnotatedType type, TypeVariable<? extends Class<?>> variable) {
      Class<?> clazz = (Class<?>)variable.getGenericDeclaration();
      AnnotatedType superType = getExactSuperType(type, clazz);
      if (superType instanceof AnnotatedParameterizedType) {
         int index = Arrays.asList(clazz.getTypeParameters()).indexOf(variable);
         AnnotatedType resolvedVarType = ((AnnotatedParameterizedType)superType).getAnnotatedActualTypeArguments()[index];
         return updateAnnotations(resolvedVarType, variable.getAnnotations());
      } else {
         return null;
      }
   }

   public static Type getTypeParameter(Type type, TypeVariable<? extends Class<?>> variable) {
      AnnotatedType typeParameter = getTypeParameter(annotate(type), variable);
      return typeParameter == null ? null : typeParameter.getType();
   }

   public static boolean isSuperType(Type superType, Type subType) {
      if (!(superType instanceof ParameterizedType) && !(superType instanceof Class) && !(superType instanceof GenericArrayType)) {
         if (superType instanceof CaptureType) {
            if (superType.equals(subType)) {
               return true;
            }

            for (Type lowerBound : ((CaptureType)superType).getLowerBounds()) {
               if (isSuperType(lowerBound, subType)) {
                  return true;
               }
            }

            return false;
         } else {
            throw new RuntimeException("Type not supported: " + superType.getClass());
         }
      } else {
         Class<?> superClass = erase(superType);
         AnnotatedType annotatedMappedSubType = getExactSuperType(capture(annotate(subType)), superClass);
         Type mappedSubType = annotatedMappedSubType == null ? null : annotatedMappedSubType.getType();
         if (mappedSubType == null) {
            return false;
         }

         if (superType instanceof Class) {
            return true;
         }

         if (mappedSubType instanceof Class) {
            return true;
         }

         if (mappedSubType instanceof GenericArrayType) {
            Type superComponentType = getArrayComponentType(superType);
            assert superComponentType != null;
            Type mappedSubComponentType = getArrayComponentType(mappedSubType);
            assert mappedSubComponentType != null;
            return isSuperType(superComponentType, mappedSubComponentType);
         }

         assert mappedSubType instanceof ParameterizedType;
         assert superType instanceof ParameterizedType;
         ParameterizedType pMappedSubType = (ParameterizedType)mappedSubType;
         assert pMappedSubType.getRawType() == superClass;
         ParameterizedType pSuperType = (ParameterizedType)superType;
         Type[] superTypeArgs = pSuperType.getActualTypeArguments();
         Type[] subTypeArgs = pMappedSubType.getActualTypeArguments();
         assert superTypeArgs.length == subTypeArgs.length;

         for (int i = 0; i < superTypeArgs.length; i++) {
            if (!contains(superTypeArgs[i], subTypeArgs[i])) {
               return false;
            }
         }

         return pSuperType.getOwnerType() == null || isSuperType(pSuperType.getOwnerType(), pMappedSubType.getOwnerType());
      }
   }

   private static boolean isArraySupertype(Type arraySuperType, Type subType) {
      Type superTypeComponent = getArrayComponentType(arraySuperType);
      assert superTypeComponent != null;
      Type subTypeComponent = getArrayComponentType(subType);
      return subTypeComponent == null ? false : isSuperType(superTypeComponent, subTypeComponent);
   }

   public static AnnotatedType getArrayComponentType(AnnotatedType type) {
      if (type.getType() instanceof Class) {
         Class<?> clazz = (Class<?>)type.getType();
         return new AnnotatedTypeImpl(clazz.getComponentType(), clazz.getAnnotations());
      } else if (type instanceof AnnotatedArrayType) {
         AnnotatedArrayType aType = (AnnotatedArrayType)type;
         return aType.getAnnotatedGenericComponentType();
      } else {
         return null;
      }
   }

   public static Type getArrayComponentType(Type type) {
      AnnotatedType componentType = getArrayComponentType(annotate(type));
      return componentType == null ? null : componentType.getType();
   }

   private static boolean contains(Type containingType, Type containedType) {
      if (containingType instanceof WildcardType) {
         WildcardType wContainingType = (WildcardType)containingType;

         for (Type upperBound : wContainingType.getUpperBounds()) {
            if (!isSuperType(upperBound, containedType)) {
               return false;
            }
         }

         for (Type lowerBound : wContainingType.getLowerBounds()) {
            if (!isSuperType(containedType, lowerBound)) {
               return false;
            }
         }

         return true;
      } else {
         return containingType.equals(containedType);
      }
   }

   private static void extractVariables(
      AnnotatedParameterizedType resolvedTyped, AnnotatedParameterizedType unresolvedType, Class<?> declaringClass, VarMap variables
   ) {
      for (int i = 0; i < resolvedTyped.getAnnotatedActualTypeArguments().length; i++) {
         AnnotatedType unresolvedParam = unresolvedType.getAnnotatedActualTypeArguments()[i];
         AnnotatedType resolvedParam = resolvedTyped.getAnnotatedActualTypeArguments()[i];
         Type var = unresolvedParam.getType();
         if (var instanceof TypeVariable && ((TypeVariable)var).getGenericDeclaration() == declaringClass) {
            variables.add((TypeVariable)var, resolvedParam);
         } else if (unresolvedParam instanceof AnnotatedParameterizedType) {
            if (!(resolvedParam instanceof AnnotatedParameterizedType) || !erase(unresolvedParam.getType()).equals(erase(resolvedParam.getType()))) {
               throw new IllegalArgumentException("The provided types do not match in shape");
            }

            extractVariables((AnnotatedParameterizedType)resolvedParam, (AnnotatedParameterizedType)unresolvedParam, declaringClass, variables);
         }
      }
   }

   private static AnnotatedType[] getExactDirectSuperTypes(AnnotatedType type) {
      if (type instanceof AnnotatedParameterizedType || type != null && type.getType() instanceof Class) {
         Class<?> clazz;
         if (type instanceof AnnotatedParameterizedType) {
            clazz = (Class<?>)((ParameterizedType)type.getType()).getRawType();
         } else {
            clazz = (Class<?>)type.getType();
            if (clazz.isArray()) {
               return getArrayExactDirectSuperTypes(annotate(clazz));
            }
         }

         AnnotatedType[] superInterfaces = clazz.getAnnotatedInterfaces();
         AnnotatedType superClass = clazz.getAnnotatedSuperclass();
         if (superClass == null && superInterfaces.length == 0 && clazz.isInterface()) {
            return new AnnotatedType[]{new AnnotatedTypeImpl(Object.class)};
         }

         AnnotatedType[] result;
         int resultIndex;
         if (superClass == null) {
            result = new AnnotatedType[superInterfaces.length];
            resultIndex = 0;
         } else {
            result = new AnnotatedType[superInterfaces.length + 1];
            resultIndex = 1;
            result[0] = mapTypeParameters(superClass, type);
         }

         for (AnnotatedType superInterface : superInterfaces) {
            result[resultIndex++] = mapTypeParameters(superInterface, type);
         }

         return result;
      } else if (type instanceof AnnotatedTypeVariable) {
         AnnotatedTypeVariable tv = (AnnotatedTypeVariable)type;
         return tv.getAnnotatedBounds();
      } else if (type instanceof AnnotatedWildcardType) {
         return ((AnnotatedWildcardType)type).getAnnotatedUpperBounds();
      } else if (type instanceof AnnotatedCaptureTypeImpl) {
         return ((AnnotatedCaptureTypeImpl)type).getAnnotatedUpperBounds();
      } else if (type instanceof AnnotatedArrayType) {
         return getArrayExactDirectSuperTypes(type);
      } else if (type == null) {
         throw new NullPointerException();
      } else {
         throw new RuntimeException("not implemented type: " + type);
      }
   }

   private static AnnotatedType[] getArrayExactDirectSuperTypes(AnnotatedType arrayType) {
      AnnotatedType typeComponent = getArrayComponentType(arrayType);
      AnnotatedType[] result;
      int resultIndex;
      if (typeComponent != null && typeComponent.getType() instanceof Class && ((Class)typeComponent.getType()).isPrimitive()) {
         resultIndex = 0;
         result = new AnnotatedType[3];
      } else {
         AnnotatedType[] componentSupertypes = getExactDirectSuperTypes(typeComponent);
         result = new AnnotatedType[componentSupertypes.length + 3];

         for (resultIndex = 0; resultIndex < componentSupertypes.length; resultIndex++) {
            result[resultIndex] = AnnotatedArrayTypeImpl.createArrayType(componentSupertypes[resultIndex], new Annotation[0]);
         }
      }

      result[resultIndex++] = new AnnotatedTypeImpl(Object.class);
      result[resultIndex++] = new AnnotatedTypeImpl(Cloneable.class);
      result[resultIndex++] = new AnnotatedTypeImpl(Serializable.class);
      return result;
   }

   public static AnnotatedType getExactReturnType(Method m, AnnotatedType declaringType) {
      return getReturnType(m, declaringType, VarMap.MappingMode.EXACT);
   }

   public static Type getExactReturnType(Method m, Type declaringType) {
      return getExactReturnType(m, annotate(declaringType)).getType();
   }

   public static AnnotatedType getReturnType(Method m, AnnotatedType declaringType) {
      return getReturnType(m, declaringType, VarMap.MappingMode.ALLOW_INCOMPLETE);
   }

   public static Type getReturnType(Method m, Type declaringType) {
      return getReturnType(m, annotate(declaringType)).getType();
   }

   private static AnnotatedType getReturnType(Method m, AnnotatedType declaringType, VarMap.MappingMode mappingMode) {
      AnnotatedType returnType = m.getAnnotatedReturnType();
      AnnotatedType exactDeclaringType = getExactSuperType(capture(declaringType), m.getDeclaringClass());
      if (exactDeclaringType == null) {
         throw new IllegalArgumentException("The method " + m + " is not a member of type " + declaringType);
      } else {
         return mapTypeParameters(returnType, exactDeclaringType, mappingMode);
      }
   }

   public static AnnotatedType getExactFieldType(Field f, AnnotatedType declaringType) {
      return getFieldType(f, declaringType, VarMap.MappingMode.EXACT);
   }

   public static Type getExactFieldType(Field f, Type type) {
      return getExactFieldType(f, annotate(type)).getType();
   }

   public static AnnotatedType getFieldType(Field f, AnnotatedType declaringType) {
      return getFieldType(f, declaringType, VarMap.MappingMode.ALLOW_INCOMPLETE);
   }

   public static Type getFieldType(Field f, Type type) {
      return getFieldType(f, annotate(type)).getType();
   }

   private static AnnotatedType getFieldType(Field f, AnnotatedType declaringType, VarMap.MappingMode mappingMode) {
      AnnotatedType returnType = f.getAnnotatedType();
      AnnotatedType exactDeclaringType = getExactSuperType(capture(declaringType), f.getDeclaringClass());
      if (exactDeclaringType == null) {
         throw new IllegalArgumentException("The field " + f + " is not a member of type " + declaringType);
      } else {
         return mapTypeParameters(returnType, exactDeclaringType, mappingMode);
      }
   }

   public static AnnotatedType[] getExactParameterTypes(Executable exe, AnnotatedType declaringType) {
      return getParameterTypes(exe, declaringType, VarMap.MappingMode.EXACT);
   }

   public static Type[] getExactParameterTypes(Executable exe, Type declaringType) {
      return mapArray(getExactParameterTypes(exe, annotate(declaringType)), Type[]::new, AnnotatedType::getType);
   }

   public static AnnotatedType[] getParameterTypes(Executable exe, AnnotatedType declaringType) {
      return getParameterTypes(exe, declaringType, VarMap.MappingMode.ALLOW_INCOMPLETE);
   }

   public static Type[] getParameterTypes(Executable exe, Type declaringType) {
      return mapArray(getParameterTypes(exe, annotate(declaringType)), Type[]::new, AnnotatedType::getType);
   }

   private static AnnotatedType[] getParameterTypes(Executable exe, AnnotatedType declaringType, VarMap.MappingMode mappingMode) {
      AnnotatedType[] parameterTypes = exe.getAnnotatedParameterTypes();
      AnnotatedType exactDeclaringType = getExactSuperType(capture(declaringType), exe.getDeclaringClass());
      if (exactDeclaringType == null) {
         throw new IllegalArgumentException("The method/constructor " + exe + " is not a member of type " + declaringType);
      }

      AnnotatedType[] result = new AnnotatedType[parameterTypes.length];

      for (int i = 0; i < parameterTypes.length; i++) {
         result[i] = mapTypeParameters(parameterTypes[i], exactDeclaringType, mappingMode);
      }

      return result;
   }

   public static AnnotatedType capture(AnnotatedType type) {
      return type instanceof AnnotatedParameterizedType ? capture((AnnotatedParameterizedType)type) : type;
   }

   public static AnnotatedParameterizedType capture(AnnotatedParameterizedType type) {
      VarMap varMap = new VarMap();
      List<AnnotatedCaptureTypeImpl> toInit = new ArrayList<>();
      Class<?> clazz = (Class<?>)((ParameterizedType)type.getType()).getRawType();
      AnnotatedType[] arguments = type.getAnnotatedActualTypeArguments();
      TypeVariable<?>[] vars = clazz.getTypeParameters();
      AnnotatedType[] capturedArguments = new AnnotatedType[arguments.length];
      assert arguments.length == vars.length;

      for (int i = 0; i < arguments.length; i++) {
         AnnotatedType argument = arguments[i];
         if (argument instanceof AnnotatedWildcardType) {
            AnnotatedCaptureTypeImpl captured = new AnnotatedCaptureTypeImpl((AnnotatedWildcardType)argument, new AnnotatedTypeVariableImpl(vars[i]));
            argument = captured;
            toInit.add(captured);
         }

         capturedArguments[i] = argument;
         varMap.add(vars[i], argument);
      }

      for (AnnotatedCaptureTypeImpl captured : toInit) {
         captured.init(varMap);
      }

      ParameterizedType inner = (ParameterizedType)type.getType();
      AnnotatedType ownerType = inner.getOwnerType() == null ? null : capture(annotate(inner.getOwnerType()));
      Type[] rawArgs = mapArray(capturedArguments, Type[]::new, AnnotatedType::getType);
      ParameterizedType nn = new ParameterizedTypeImpl(clazz, rawArgs, ownerType == null ? null : ownerType.getType());
      return new AnnotatedParameterizedTypeImpl(nn, type.getAnnotations(), capturedArguments);
   }

   public static String getTypeName(Type type) {
      if (type instanceof Class) {
         Class<?> clazz = (Class<?>)type;
         return clazz.isArray() ? getTypeName(clazz.getComponentType()) + "[]" : clazz.getName();
      } else {
         return type.toString();
      }
   }

   public static List<Class<?>> getUpperBoundClassAndInterfaces(Type type) {
      LinkedHashSet<Class<?>> result = new LinkedHashSet<>();
      buildUpperBoundClassAndInterfaces(type, result);
      return new ArrayList<>(result);
   }

   private static AnnotatedType annotate(Type type, boolean expandGenerics) {
      return annotate(type, expandGenerics, new HashMap<>());
   }

   public static AnnotatedType annotate(Type type) {
      return annotate(type, false);
   }

   public static AnnotatedType annotate(Type type, Annotation[] annotations) {
      return updateAnnotations(annotate(type), annotations);
   }

   private static AnnotatedType annotate(Type type, boolean expandGenerics, Map<GenericTypeReflector.CaptureCacheKey, AnnotatedType> cache) {
      if (!(type instanceof ParameterizedType)) {
         if (type instanceof CaptureType) {
            GenericTypeReflector.CaptureCacheKey key = new GenericTypeReflector.CaptureCacheKey((CaptureType)type);
            if (cache.containsKey(key)) {
               return cache.get(key);
            }

            CaptureType capture = (CaptureType)type;
            AnnotatedCaptureType annotatedCapture = new AnnotatedCaptureTypeImpl(
               capture,
               (AnnotatedWildcardType)annotate(capture.getWildcardType(), expandGenerics, cache),
               (AnnotatedTypeVariable)annotate(capture.getTypeVariable(), expandGenerics, cache)
            );
            cache.put(new GenericTypeReflector.CaptureCacheKey(capture), annotatedCapture);
            AnnotatedType[] upperBounds = mapArray(capture.getUpperBounds(), AnnotatedType[]::new, bound -> annotate(bound, expandGenerics, cache));
            annotatedCapture.setAnnotatedUpperBounds(upperBounds);
            return annotatedCapture;
         } else {
            if (type instanceof WildcardType) {
               WildcardType wildcard = (WildcardType)type;
               AnnotatedType[] lowerBounds = mapArray(wildcard.getLowerBounds(), AnnotatedType[]::new, bound -> annotate(bound, expandGenerics, cache));
               AnnotatedType[] upperBounds = mapArray(wildcard.getUpperBounds(), AnnotatedType[]::new, bound -> annotate(bound, expandGenerics, cache));
               return new AnnotatedWildcardTypeImpl(wildcard, erase(type).getAnnotations(), lowerBounds, upperBounds);
            }

            if (type instanceof TypeVariable) {
               return new AnnotatedTypeVariableImpl((TypeVariable<?>)type);
            }

            if (type instanceof GenericArrayType) {
               GenericArrayType genArray = (GenericArrayType)type;
               return new AnnotatedArrayTypeImpl(genArray, new Annotation[0], annotate(genArray.getGenericComponentType(), expandGenerics, cache));
            }

            if (type instanceof Class) {
               Class<?> clazz = (Class<?>)type;
               if (clazz.isArray()) {
                  Class<?> componentClass = clazz.getComponentType();
                  return AnnotatedArrayTypeImpl.createArrayType(new AnnotatedTypeImpl(componentClass, componentClass.getAnnotations()), new Annotation[0]);
               } else {
                  return clazz.getTypeParameters().length > 0 && expandGenerics
                     ? expandClassGenerics(clazz)
                     : new AnnotatedTypeImpl(clazz, clazz.getAnnotations());
               }
            } else {
               throw new IllegalArgumentException("Unrecognized type: " + type.getTypeName());
            }
         }
      } else {
         ParameterizedType parameterized = (ParameterizedType)type;
         AnnotatedType[] params = new AnnotatedType[parameterized.getActualTypeArguments().length];

         for (int i = 0; i < params.length; i++) {
            AnnotatedType param = annotate(parameterized.getActualTypeArguments()[i], expandGenerics, cache);
            params[i] = updateAnnotations(param, erase(type).getTypeParameters()[i].getAnnotations());
         }

         return new AnnotatedParameterizedTypeImpl(parameterized, erase(type).getAnnotations(), params);
      }
   }

   public static <T extends AnnotatedType> T replaceAnnotations(T original, Annotation[] annotations) {
      if (original instanceof AnnotatedParameterizedType) {
         return (T)(new AnnotatedParameterizedTypeImpl(
            (ParameterizedType)original.getType(), annotations, ((AnnotatedParameterizedType)original).getAnnotatedActualTypeArguments()
         ));
      } else if (original instanceof AnnotatedCaptureType) {
         AnnotatedCaptureTypeImpl capture = (AnnotatedCaptureTypeImpl)original;
         return (T)capture.setAnnotations(annotations);
      } else if (original instanceof AnnotatedWildcardType) {
         return (T)(new AnnotatedWildcardTypeImpl(
            (WildcardType)original.getType(),
            annotations,
            ((AnnotatedWildcardType)original).getAnnotatedLowerBounds(),
            ((AnnotatedWildcardType)original).getAnnotatedUpperBounds()
         ));
      } else if (original instanceof AnnotatedTypeVariable) {
         return (T)(new AnnotatedTypeVariableImpl((TypeVariable<?>)original.getType(), annotations));
      } else {
         return (T)(original instanceof AnnotatedArrayType
            ? new AnnotatedArrayTypeImpl(original.getType(), annotations, ((AnnotatedArrayType)original).getAnnotatedGenericComponentType())
            : new AnnotatedTypeImpl(original.getType(), annotations));
      }
   }

   public static <T extends AnnotatedType> T updateAnnotations(T original, Annotation[] annotations) {
      return annotations != null && annotations.length != 0 && !Arrays.equals(original.getAnnotations(), annotations)
         ? replaceAnnotations(original, merge(original.getAnnotations(), annotations))
         : original;
   }

   public static <T extends AnnotatedType> T mergeAnnotations(T t1, T t2) {
      Annotation[] merged = merge(t1.getAnnotations(), t2.getAnnotations());
      if (t1 instanceof AnnotatedParameterizedType) {
         AnnotatedType[] p1 = ((AnnotatedParameterizedType)t1).getAnnotatedActualTypeArguments();
         AnnotatedType[] p2 = ((AnnotatedParameterizedType)t2).getAnnotatedActualTypeArguments();
         AnnotatedType[] params = new AnnotatedType[p1.length];

         for (int i = 0; i < p1.length; i++) {
            params[i] = mergeAnnotations(p1[i], p2[i]);
         }

         return (T)(new AnnotatedParameterizedTypeImpl((ParameterizedType)t1.getType(), merged, params));
      } else if (!(t1 instanceof AnnotatedWildcardType)) {
         if (t1 instanceof AnnotatedTypeVariable) {
            return (T)(new AnnotatedTypeVariableImpl((TypeVariable<?>)t1.getType(), merged));
         } else if (t1 instanceof AnnotatedArrayType) {
            AnnotatedType componentType = mergeAnnotations(
               ((AnnotatedArrayType)t1).getAnnotatedGenericComponentType(), ((AnnotatedArrayType)t2).getAnnotatedGenericComponentType()
            );
            return (T)(new AnnotatedArrayTypeImpl(t1.getType(), merged, componentType));
         } else {
            return (T)(new AnnotatedTypeImpl(t1.getType(), merged));
         }
      } else {
         AnnotatedType[] l1 = ((AnnotatedWildcardType)t1).getAnnotatedLowerBounds();
         AnnotatedType[] l2 = ((AnnotatedWildcardType)t2).getAnnotatedLowerBounds();
         AnnotatedType[] lowerBounds = new AnnotatedType[l1.length];

         for (int i = 0; i < l1.length; i++) {
            lowerBounds[i] = mergeAnnotations(l1[i], l2[i]);
         }

         AnnotatedType[] u1 = ((AnnotatedWildcardType)t1).getAnnotatedUpperBounds();
         AnnotatedType[] u2 = ((AnnotatedWildcardType)t2).getAnnotatedUpperBounds();
         AnnotatedType[] upperBounds = new AnnotatedType[u1.length];

         for (int i = 0; i < u1.length; i++) {
            upperBounds[i] = mergeAnnotations(u1[i], u2[i]);
         }

         return (T)(new AnnotatedWildcardTypeImpl((WildcardType)t1.getType(), merged, lowerBounds, upperBounds));
      }
   }

   public static AnnotatedParameterizedType replaceParameters(AnnotatedParameterizedType type, AnnotatedType[] typeParameters) {
      return replaceParameters(type, new Annotation[0], typeParameters);
   }

   private static AnnotatedParameterizedType replaceParameters(AnnotatedParameterizedType type, Annotation[] annotations, AnnotatedType[] typeParameters) {
      Type[] rawArguments = mapArray(typeParameters, Type[]::new, AnnotatedType::getType);
      ParameterizedType inner = (ParameterizedType)type.getType();
      ParameterizedType rawType = (ParameterizedType)TypeFactory.parameterizedInnerClass(inner.getOwnerType(), erase(inner), rawArguments);
      return new AnnotatedParameterizedTypeImpl(rawType, merge(type.getAnnotations(), annotations), typeParameters);
   }

   public static <T extends AnnotatedType> T toCanonical(T type) {
      return toCanonical(type, Function.identity());
   }

   public static <T extends AnnotatedType> T toCanonicalBoxed(T type) {
      return toCanonical(type, GenericTypeReflector::box);
   }

   private static <T extends AnnotatedType> T toCanonical(T type, final Function<Type, Type> leafTransformer) {
      return (T)transform(
         type,
         new TypeVisitor() {
            @Override
            protected AnnotatedType visitClass(AnnotatedType type) {
               Annotation[] annotations = type.getAnnotations();
               Class<?> raw = (Class<?>)type.getType();
               annotations = GenericTypeReflector.merge(annotations, raw.getAnnotations());
               return new AnnotatedTypeImpl(leafTransformer.apply(type.getType()), annotations);
            }

            @Override
            protected AnnotatedType visitArray(AnnotatedArrayType type) {
               return new AnnotatedArrayTypeImpl(
                  leafTransformer.apply(type.getType()), type.getAnnotations(), GenericTypeReflector.transform(type.getAnnotatedGenericComponentType(), this)
               );
            }

            @Override
            protected AnnotatedType visitParameterizedType(AnnotatedParameterizedType type) {
               AnnotatedType[] params = Arrays.stream(type.getAnnotatedActualTypeArguments())
                  .map(param -> GenericTypeReflector.transform(param, this))
                  .toArray(AnnotatedType[]::new);
               Class<?> raw = (Class<?>)((ParameterizedType)type.getType()).getRawType();
               return GenericTypeReflector.replaceParameters(type, raw.getAnnotations(), params);
            }
         }
      );
   }

   private static AnnotatedType expandGenerics(AnnotatedType type) {
      return transform(type, new TypeVisitor() {
         @Override
         public AnnotatedType visitClass(AnnotatedType type) {
            Class<?> clazz = (Class<?>)type.getType();
            return clazz.getTypeParameters().length > 0 ? GenericTypeReflector.expandClassGenerics(clazz) : type;
         }
      });
   }

   public static AnnotatedType transform(AnnotatedType type, TypeVisitor visitor) {
      if (type instanceof AnnotatedParameterizedType) {
         return visitor.visitParameterizedType((AnnotatedParameterizedType)type);
      } else if (type instanceof AnnotatedWildcardType) {
         return visitor.visitWildcardType((AnnotatedWildcardType)type);
      } else if (type instanceof AnnotatedTypeVariable) {
         return visitor.visitVariable((AnnotatedTypeVariable)type);
      } else if (type instanceof AnnotatedArrayType) {
         return visitor.visitArray((AnnotatedArrayType)type);
      } else if (type instanceof AnnotatedCaptureType) {
         return visitor.visitCaptureType((AnnotatedCaptureType)type);
      } else {
         return type.getType() instanceof Class ? visitor.visitClass(type) : visitor.visitUnmatched(type);
      }
   }

   public static AnnotatedType reduceBounded(AnnotatedType type) {
      AnnotatedType capture = capture(type);
      return transform(
         capture,
         new TypeVisitor() {
            @Override
            protected AnnotatedType visitVariable(AnnotatedTypeVariable type) {
               return GenericTypeReflector.updateAnnotations(GenericTypeReflector.transform(type.getAnnotatedBounds()[0], this), type.getAnnotations());
            }

            @Override
            protected AnnotatedType visitWildcardType(AnnotatedWildcardType type) {
               return type.getAnnotatedLowerBounds().length > 0
                  ? GenericTypeReflector.updateAnnotations(GenericTypeReflector.transform(type.getAnnotatedLowerBounds()[0], this), type.getAnnotations())
                  : GenericTypeReflector.updateAnnotations(GenericTypeReflector.transform(type.getAnnotatedUpperBounds()[0], this), type.getAnnotations());
            }

            @Override
            protected AnnotatedType visitCaptureType(AnnotatedCaptureType type) {
               AnnotatedType bound = type.getAnnotatedLowerBounds().length > 0 ? type.getAnnotatedLowerBounds()[0] : type.getAnnotatedUpperBounds()[0];
               if (bound instanceof AnnotatedParameterizedType) {
                  AnnotatedType[] typeArguments = ((AnnotatedParameterizedType)bound).getAnnotatedActualTypeArguments();

                  for (AnnotatedType typeArgument : typeArguments) {
                     if (type.equals(typeArgument)) {
                        ParameterizedType parameterizedType = (ParameterizedType)bound.getType();
                        return GenericTypeReflector.annotate(
                           parameterizedType.getRawType(), GenericTypeReflector.merge(type.getAnnotations(), bound.getAnnotations())
                        );
                     }
                  }
               }

               return GenericTypeReflector.updateAnnotations(GenericTypeReflector.transform(bound, this), type.getAnnotations());
            }
         }
      );
   }

   private static AnnotatedParameterizedType expandClassGenerics(Class<?> type) {
      ParameterizedType inner = new ParameterizedTypeImpl(type, type.getTypeParameters(), type.getDeclaringClass());
      AnnotatedType[] params = mapArray(type.getTypeParameters(), AnnotatedType[]::new, GenericTypeReflector::annotate);
      return new AnnotatedParameterizedTypeImpl(inner, type.getAnnotations(), params);
   }

   public static Annotation[] merge(Annotation[]... annotations) {
      Set<Annotation> result = new LinkedHashSet<>();

      for (Annotation[] annos : annotations) {
         for (Annotation anno : annos) {
            result.add(anno);
         }
      }

      return result.toArray(new Annotation[0]);
   }

   static boolean typeArraysEqual(AnnotatedType[] t1, AnnotatedType[] t2) {
      if (t1 == t2) {
         return true;
      }

      if (t1 == null) {
         return false;
      }

      if (t2 == null) {
         return false;
      }

      if (t1.length != t2.length) {
         return false;
      }

      for (int i = 0; i < t1.length; i++) {
         if (!t1[i].getType().equals(t2[i].getType()) || !Arrays.equals(t1[i].getAnnotations(), t2[i].getAnnotations())) {
            return false;
         }
      }

      return true;
   }

   public static int hashCode(AnnotatedType... types) {
      int typeHash = Arrays.stream(types).mapToInt(t -> t.getType().hashCode()).reduce(0, (x, y) -> 127 * x ^ y);
      int annotationHash = hashCode(Arrays.stream(types).flatMap(t -> Arrays.stream(t.getAnnotations())));
      return 31 * typeHash ^ annotationHash;
   }

   static int hashCode(Stream<Annotation> annotations) {
      return annotations.mapToInt(a -> 31 * a.annotationType().hashCode() ^ a.hashCode()).reduce(0, (x, y) -> 127 * x ^ y);
   }

   public static boolean equals(AnnotatedType t1, AnnotatedType t2) {
      Objects.requireNonNull(t1);
      Objects.requireNonNull(t2);
      t1 = toCanonical(t1);
      t2 = toCanonical(t2);
      return t1.equals(t2);
   }

   private static void buildUpperBoundClassAndInterfaces(Type type, Set<Class<?>> result) {
      if (!(type instanceof ParameterizedType) && !(type instanceof Class)) {
         for (AnnotatedType superType : getExactDirectSuperTypes(annotate(type))) {
            buildUpperBoundClassAndInterfaces(superType.getType(), result);
         }
      } else {
         result.add(erase(type));
      }
   }

   private static <I, O> O[] mapArray(I[] array, IntFunction<O[]> resultCtor, Function<I, O> mapper) {
      O[] result = (O[])resultCtor.apply(array.length);

      for (int i = 0; i < array.length; i++) {
         result[i] = mapper.apply(array[i]);
      }

      return result;
   }

   static {
      Map<Class<?>, Class<?>> boxTypes = new HashMap<>();
      boxTypes.put(boolean.class, Boolean.class);
      boxTypes.put(byte.class, Byte.class);
      boxTypes.put(char.class, Character.class);
      boxTypes.put(double.class, Double.class);
      boxTypes.put(float.class, Float.class);
      boxTypes.put(int.class, Integer.class);
      boxTypes.put(long.class, Long.class);
      boxTypes.put(short.class, Short.class);
      boxTypes.put(void.class, Void.class);
      BOX_TYPES = Collections.unmodifiableMap(boxTypes);
   }

   private static class AnnotatedCaptureCacheKey {
      AnnotatedCaptureType capture;
      CaptureType raw;

      AnnotatedCaptureCacheKey(AnnotatedCaptureType capture) {
         this.capture = capture;
         this.raw = (CaptureType)capture.getType();
      }

      @Override
      public int hashCode() {
         return 127 * this.raw.getWildcardType().hashCode()
            ^ this.raw.getTypeVariable().hashCode()
            ^ GenericTypeReflector.hashCode(Arrays.stream(this.capture.getAnnotations()));
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof GenericTypeReflector.AnnotatedCaptureCacheKey)) {
            return false;
         }

         GenericTypeReflector.AnnotatedCaptureCacheKey that = (GenericTypeReflector.AnnotatedCaptureCacheKey)obj;
         return this.capture == that.capture
            || new GenericTypeReflector.CaptureCacheKey(this.raw).equals(new GenericTypeReflector.CaptureCacheKey(that.raw))
               && Arrays.equals(this.capture.getAnnotations(), that.capture.getAnnotations());
      }
   }

   static class CaptureCacheKey {
      CaptureType capture;

      CaptureCacheKey(CaptureType capture) {
         this.capture = capture;
      }

      @Override
      public int hashCode() {
         return 127 * this.capture.getWildcardType().hashCode() ^ this.capture.getTypeVariable().hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (!(obj instanceof GenericTypeReflector.CaptureCacheKey)) {
            return false;
         }

         CaptureType that = ((GenericTypeReflector.CaptureCacheKey)obj).capture;
         return this.capture == that
            || this.capture.getWildcardType().equals(that.getWildcardType())
               && this.capture.getTypeVariable().equals(that.getTypeVariable())
               && Arrays.equals(this.capture.getUpperBounds(), that.getUpperBounds());
      }
   }
}
