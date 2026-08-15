package com.dfsek.terra.lib.commons.lang3.reflect;

import com.dfsek.terra.lib.commons.lang3.AppendableJoiner;
import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import com.dfsek.terra.lib.commons.lang3.ObjectUtils;
import com.dfsek.terra.lib.commons.lang3.Validate;
import com.dfsek.terra.lib.commons.lang3.builder.Builder;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

public class TypeUtils {
   private static final AppendableJoiner<Type> AMP_JOINER = AppendableJoiner.<Type>builder()
      .setDelimiter(" & ")
      .setElementAppender((a, e) -> a.append(toString(e)))
      .get();
   private static final AppendableJoiner<TypeVariable<Class<?>>> CTJ_JOINER = AppendableJoiner.<TypeVariable<Class<?>>>builder()
      .setDelimiter(", ")
      .setElementAppender((a, e) -> a.append(anyToString(e)))
      .get();
   private static final AppendableJoiner<Object> GT_JOINER = AppendableJoiner.<Object>builder()
      .setPrefix("<")
      .setSuffix(">")
      .setDelimiter(", ")
      .setElementAppender((a, e) -> a.append(anyToString(e)))
      .get();
   public static final WildcardType WILDCARD_ALL = wildcardType().withUpperBounds(Object.class).build();

   private static <T> String anyToString(T object) {
      return object instanceof Type ? toString((Type)object) : object.toString();
   }

   private static void appendRecursiveTypes(StringBuilder builder, int[] recursiveTypeIndexes, Type[] argumentTypes) {
      for (int i = 0; i < recursiveTypeIndexes.length; i++) {
         GT_JOINER.join(builder, argumentTypes[i].toString());
      }

      Type[] argumentsFiltered = ArrayUtils.removeAll(argumentTypes, recursiveTypeIndexes);
      if (argumentsFiltered.length > 0) {
         GT_JOINER.join(builder, argumentsFiltered);
      }
   }

   private static <T> String classToString(Class<T> cls) {
      if (cls.isArray()) {
         return toString(cls.getComponentType()) + "[]";
      }

      if (isCyclical(cls)) {
         return cls.getSimpleName() + "(cycle)";
      }

      StringBuilder buf = new StringBuilder();
      if (cls.getEnclosingClass() != null) {
         buf.append(classToString(cls.getEnclosingClass())).append('.').append(cls.getSimpleName());
      } else {
         buf.append(cls.getName());
      }

      if (cls.getTypeParameters().length > 0) {
         CTJ_JOINER.join(buf, cls.getTypeParameters());
      }

      return buf.toString();
   }

   public static boolean containsTypeVariables(Type type) {
      if (type instanceof TypeVariable) {
         return true;
      }

      if (type instanceof Class) {
         return ((Class)type).getTypeParameters().length > 0;
      }

      if (type instanceof ParameterizedType) {
         for (Type arg : ((ParameterizedType)type).getActualTypeArguments()) {
            if (containsTypeVariables(arg)) {
               return true;
            }
         }

         return false;
      } else {
         if (!(type instanceof WildcardType)) {
            return type instanceof GenericArrayType ? containsTypeVariables(((GenericArrayType)type).getGenericComponentType()) : false;
         }

         WildcardType wild = (WildcardType)type;
         return containsTypeVariables(getImplicitLowerBounds(wild)[0]) || containsTypeVariables(getImplicitUpperBounds(wild)[0]);
      }
   }

   private static boolean containsVariableTypeSameParametrizedTypeBound(TypeVariable<?> typeVariable, ParameterizedType parameterizedType) {
      return ArrayUtils.contains(typeVariable.getBounds(), parameterizedType);
   }

   public static Map<TypeVariable<?>, Type> determineTypeArguments(Class<?> cls, ParameterizedType superParameterizedType) {
      Objects.requireNonNull(cls, "cls");
      Objects.requireNonNull(superParameterizedType, "superParameterizedType");
      Class<?> superClass = getRawType(superParameterizedType);
      if (!isAssignable(cls, superClass)) {
         return null;
      }

      if (cls.equals(superClass)) {
         return getTypeArguments(superParameterizedType, superClass, null);
      }

      Type midType = getClosestParentType(cls, superClass);
      if (midType instanceof Class) {
         return determineTypeArguments((Class<?>)midType, superParameterizedType);
      }

      ParameterizedType midParameterizedType = (ParameterizedType)midType;
      Class<?> midClass = getRawType(midParameterizedType);
      Map<TypeVariable<?>, Type> typeVarAssigns = determineTypeArguments(midClass, superParameterizedType);
      mapTypeVariablesToArguments(cls, midParameterizedType, typeVarAssigns);
      return typeVarAssigns;
   }

   private static boolean equals(GenericArrayType genericArrayType, Type type) {
      return type instanceof GenericArrayType && equals(genericArrayType.getGenericComponentType(), ((GenericArrayType)type).getGenericComponentType());
   }

   private static boolean equals(ParameterizedType parameterizedType, Type type) {
      if (type instanceof ParameterizedType) {
         ParameterizedType other = (ParameterizedType)type;
         if (equals(parameterizedType.getRawType(), other.getRawType()) && equals(parameterizedType.getOwnerType(), other.getOwnerType())) {
            return equals(parameterizedType.getActualTypeArguments(), other.getActualTypeArguments());
         }
      }

      return false;
   }

   public static boolean equals(Type type1, Type type2) {
      if (Objects.equals(type1, type2)) {
         return true;
      } else if (type1 instanceof ParameterizedType) {
         return equals((ParameterizedType)type1, type2);
      } else if (type1 instanceof GenericArrayType) {
         return equals((GenericArrayType)type1, type2);
      } else {
         return type1 instanceof WildcardType ? equals((WildcardType)type1, type2) : false;
      }
   }

   private static boolean equals(Type[] type1, Type[] type2) {
      if (type1.length == type2.length) {
         for (int i = 0; i < type1.length; i++) {
            if (!equals(type1[i], type2[i])) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean equals(WildcardType wildcardType, Type type) {
      if (!(type instanceof WildcardType)) {
         return false;
      }

      WildcardType other = (WildcardType)type;
      return equals(getImplicitLowerBounds(wildcardType), getImplicitLowerBounds(other))
         && equals(getImplicitUpperBounds(wildcardType), getImplicitUpperBounds(other));
   }

   private static Type[] extractTypeArgumentsFrom(Map<TypeVariable<?>, Type> mappings, TypeVariable<?>[] variables) {
      Type[] result = new Type[variables.length];
      int index = 0;

      for (TypeVariable<?> var : variables) {
         Validate.isTrue(mappings.containsKey(var), "missing argument mapping for %s", toString(var));
         result[index++] = mappings.get(var);
      }

      return result;
   }

   private static int[] findRecursiveTypes(ParameterizedType parameterizedType) {
      Type[] filteredArgumentTypes = Arrays.copyOf(parameterizedType.getActualTypeArguments(), parameterizedType.getActualTypeArguments().length);
      int[] indexesToRemove = new int[0];

      for (int i = 0; i < filteredArgumentTypes.length; i++) {
         if (filteredArgumentTypes[i] instanceof TypeVariable
            && containsVariableTypeSameParametrizedTypeBound((TypeVariable<?>)filteredArgumentTypes[i], parameterizedType)) {
            indexesToRemove = ArrayUtils.add(indexesToRemove, i);
         }
      }

      return indexesToRemove;
   }

   public static GenericArrayType genericArrayType(Type componentType) {
      return new TypeUtils.GenericArrayTypeImpl(Objects.requireNonNull(componentType, "componentType"));
   }

   private static String genericArrayTypeToString(GenericArrayType genericArrayType) {
      return String.format("%s[]", toString(genericArrayType.getGenericComponentType()));
   }

   public static Type getArrayComponentType(Type type) {
      if (type instanceof Class) {
         Class<?> cls = (Class<?>)type;
         return cls.isArray() ? cls.getComponentType() : null;
      } else {
         return type instanceof GenericArrayType ? ((GenericArrayType)type).getGenericComponentType() : null;
      }
   }

   private static Type getClosestParentType(Class<?> cls, Class<?> superClass) {
      if (superClass.isInterface()) {
         Type[] interfaceTypes = cls.getGenericInterfaces();
         Type genericInterface = null;

         for (Type midType : interfaceTypes) {
            Class<?> midClass;
            if (midType instanceof ParameterizedType) {
               midClass = getRawType((ParameterizedType)midType);
            } else {
               if (!(midType instanceof Class)) {
                  throw new IllegalStateException("Unexpected generic interface type found: " + midType);
               }

               midClass = (Class<?>)midType;
            }

            if (isAssignable(midClass, superClass) && isAssignable(genericInterface, (Type)midClass)) {
               genericInterface = midType;
            }
         }

         if (genericInterface != null) {
            return genericInterface;
         }
      }

      return cls.getGenericSuperclass();
   }

   public static Type[] getImplicitBounds(TypeVariable<?> typeVariable) {
      Objects.requireNonNull(typeVariable, "typeVariable");
      Type[] bounds = typeVariable.getBounds();
      return bounds.length == 0 ? new Type[]{Object.class} : normalizeUpperBounds(bounds);
   }

   public static Type[] getImplicitLowerBounds(WildcardType wildcardType) {
      Objects.requireNonNull(wildcardType, "wildcardType");
      Type[] bounds = wildcardType.getLowerBounds();
      return bounds.length == 0 ? new Type[]{null} : bounds;
   }

   public static Type[] getImplicitUpperBounds(WildcardType wildcardType) {
      Objects.requireNonNull(wildcardType, "wildcardType");
      Type[] bounds = wildcardType.getUpperBounds();
      return bounds.length == 0 ? new Type[]{Object.class} : normalizeUpperBounds(bounds);
   }

   private static Class<?> getRawType(ParameterizedType parameterizedType) {
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class)) {
         throw new IllegalStateException("Wait... What!? Type of rawType: " + rawType);
      } else {
         return (Class<?>)rawType;
      }
   }

   public static Class<?> getRawType(Type type, Type assigningType) {
      if (type instanceof Class) {
         return (Class<?>)type;
      }

      if (type instanceof ParameterizedType) {
         return getRawType((ParameterizedType)type);
      }

      if (type instanceof TypeVariable) {
         if (assigningType == null) {
            return null;
         }

         Object genericDeclaration = ((TypeVariable)type).getGenericDeclaration();
         if (!(genericDeclaration instanceof Class)) {
            return null;
         }

         Map<TypeVariable<?>, Type> typeVarAssigns = getTypeArguments(assigningType, (Class<?>)genericDeclaration);
         if (typeVarAssigns == null) {
            return null;
         }

         Type typeArgument = typeVarAssigns.get(type);
         return typeArgument == null ? null : getRawType(typeArgument, assigningType);
      } else if (type instanceof GenericArrayType) {
         Class<?> rawComponentType = getRawType(((GenericArrayType)type).getGenericComponentType(), assigningType);
         return rawComponentType != null ? Array.newInstance(rawComponentType, 0).getClass() : null;
      } else if (type instanceof WildcardType) {
         return null;
      } else {
         throw new IllegalArgumentException("unknown type: " + type);
      }
   }

   private static Map<TypeVariable<?>, Type> getTypeArguments(Class<?> cls, Class<?> toClass, Map<TypeVariable<?>, Type> subtypeVarAssigns) {
      if (!isAssignable(cls, toClass)) {
         return null;
      }

      if (cls.isPrimitive()) {
         if (toClass.isPrimitive()) {
            return new HashMap<>();
         }

         cls = ClassUtils.primitiveToWrapper(cls);
      }

      HashMap<TypeVariable<?>, Type> typeVarAssigns = subtypeVarAssigns == null ? new HashMap<>() : new HashMap<>(subtypeVarAssigns);
      return toClass.equals(cls) ? typeVarAssigns : getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
   }

   public static Map<TypeVariable<?>, Type> getTypeArguments(ParameterizedType type) {
      return getTypeArguments(type, getRawType(type), null);
   }

   private static Map<TypeVariable<?>, Type> getTypeArguments(
      ParameterizedType parameterizedType, Class<?> toClass, Map<TypeVariable<?>, Type> subtypeVarAssigns
   ) {
      Class<?> cls = getRawType(parameterizedType);
      if (!isAssignable(cls, toClass)) {
         return null;
      }

      Type ownerType = parameterizedType.getOwnerType();
      Map<TypeVariable<?>, Type> typeVarAssigns;
      if (ownerType instanceof ParameterizedType) {
         ParameterizedType parameterizedOwnerType = (ParameterizedType)ownerType;
         typeVarAssigns = getTypeArguments(parameterizedOwnerType, getRawType(parameterizedOwnerType), subtypeVarAssigns);
      } else {
         typeVarAssigns = subtypeVarAssigns == null ? new HashMap<>() : new HashMap<>(subtypeVarAssigns);
      }

      Type[] typeArgs = parameterizedType.getActualTypeArguments();
      TypeVariable<?>[] typeParams = cls.getTypeParameters();

      for (int i = 0; i < typeParams.length; i++) {
         Type typeArg = typeArgs[i];
         typeVarAssigns.put(typeParams[i], typeVarAssigns.getOrDefault(typeArg, typeArg));
      }

      return toClass.equals(cls) ? typeVarAssigns : getTypeArguments(getClosestParentType(cls, toClass), toClass, typeVarAssigns);
   }

   public static Map<TypeVariable<?>, Type> getTypeArguments(Type type, Class<?> toClass) {
      return getTypeArguments(type, toClass, null);
   }

   private static Map<TypeVariable<?>, Type> getTypeArguments(Type type, Class<?> toClass, Map<TypeVariable<?>, Type> subtypeVarAssigns) {
      if (type instanceof Class) {
         return getTypeArguments((Class<?>)type, toClass, subtypeVarAssigns);
      }

      if (type instanceof ParameterizedType) {
         return getTypeArguments((ParameterizedType)type, toClass, subtypeVarAssigns);
      }

      if (type instanceof GenericArrayType) {
         return getTypeArguments(
            ((GenericArrayType)type).getGenericComponentType(), toClass.isArray() ? toClass.getComponentType() : toClass, subtypeVarAssigns
         );
      }

      if (type instanceof WildcardType) {
         for (Type bound : getImplicitUpperBounds((WildcardType)type)) {
            if (isAssignable(bound, toClass)) {
               return getTypeArguments(bound, toClass, subtypeVarAssigns);
            }
         }

         return null;
      } else if (type instanceof TypeVariable) {
         for (Type bound : getImplicitBounds((TypeVariable<?>)type)) {
            if (isAssignable(bound, toClass)) {
               return getTypeArguments(bound, toClass, subtypeVarAssigns);
            }
         }

         return null;
      } else {
         throw new IllegalStateException("found an unhandled type: " + type);
      }
   }

   public static boolean isArrayType(Type type) {
      return type instanceof GenericArrayType || type instanceof Class && ((Class)type).isArray();
   }

   private static boolean isAssignable(Type type, Class<?> toClass) {
      if (type == null) {
         return toClass == null || !toClass.isPrimitive();
      }

      if (toClass == null) {
         return false;
      }

      if (toClass.equals(type)) {
         return true;
      }

      if (type instanceof Class) {
         return ClassUtils.isAssignable((Class<?>)type, toClass);
      }

      if (type instanceof ParameterizedType) {
         return isAssignable(getRawType((ParameterizedType)type), toClass);
      }

      if (type instanceof TypeVariable) {
         for (Type bound : ((TypeVariable)type).getBounds()) {
            if (isAssignable(bound, toClass)) {
               return true;
            }
         }

         return false;
      } else if (!(type instanceof GenericArrayType)) {
         if (type instanceof WildcardType) {
            return false;
         } else {
            throw new IllegalStateException("found an unhandled type: " + type);
         }
      } else {
         return toClass.equals(Object.class)
            || toClass.isArray() && isAssignable(((GenericArrayType)type).getGenericComponentType(), toClass.getComponentType());
      }
   }

   private static boolean isAssignable(Type type, GenericArrayType toGenericArrayType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type == null) {
         return true;
      }

      if (toGenericArrayType == null) {
         return false;
      }

      if (toGenericArrayType.equals(type)) {
         return true;
      }

      Type toComponentType = toGenericArrayType.getGenericComponentType();
      if (!(type instanceof Class)) {
         if (type instanceof GenericArrayType) {
            return isAssignable(((GenericArrayType)type).getGenericComponentType(), toComponentType, typeVarAssigns);
         }

         if (type instanceof WildcardType) {
            for (Type bound : getImplicitUpperBounds((WildcardType)type)) {
               if (isAssignable(bound, toGenericArrayType)) {
                  return true;
               }
            }

            return false;
         } else if (type instanceof TypeVariable) {
            for (Type bound : getImplicitBounds((TypeVariable<?>)type)) {
               if (isAssignable(bound, toGenericArrayType)) {
                  return true;
               }
            }

            return false;
         } else if (type instanceof ParameterizedType) {
            return false;
         } else {
            throw new IllegalStateException("found an unhandled type: " + type);
         }
      } else {
         Class<?> cls = (Class<?>)type;
         return cls.isArray() && isAssignable(cls.getComponentType(), toComponentType, typeVarAssigns);
      }
   }

   private static boolean isAssignable(Type type, ParameterizedType toParameterizedType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type == null) {
         return true;
      }

      if (toParameterizedType == null) {
         return false;
      }

      if (type instanceof GenericArrayType) {
         return false;
      }

      if (toParameterizedType.equals(type)) {
         return true;
      }

      Class<?> toClass = getRawType(toParameterizedType);
      Map<TypeVariable<?>, Type> fromTypeVarAssigns = getTypeArguments(type, toClass, null);
      if (fromTypeVarAssigns == null) {
         return false;
      }

      if (fromTypeVarAssigns.isEmpty()) {
         return true;
      }

      Map<TypeVariable<?>, Type> toTypeVarAssigns = getTypeArguments(toParameterizedType, toClass, typeVarAssigns);

      for (TypeVariable<?> var : toTypeVarAssigns.keySet()) {
         Type toTypeArg = unrollVariableAssignments(var, toTypeVarAssigns);
         Type fromTypeArg = unrollVariableAssignments(var, fromTypeVarAssigns);
         if ((toTypeArg != null || !(fromTypeArg instanceof Class))
            && fromTypeArg != null
            && toTypeArg != null
            && !toTypeArg.equals(fromTypeArg)
            && (!(toTypeArg instanceof WildcardType) || !isAssignable(fromTypeArg, toTypeArg, typeVarAssigns))) {
            return false;
         }
      }

      return true;
   }

   public static boolean isAssignable(Type type, Type toType) {
      return isAssignable(type, toType, null);
   }

   private static boolean isAssignable(Type type, Type toType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (toType == null || toType instanceof Class) {
         return isAssignable(type, (Class<?>)toType);
      } else if (toType instanceof ParameterizedType) {
         return isAssignable(type, (ParameterizedType)toType, typeVarAssigns);
      } else if (toType instanceof GenericArrayType) {
         return isAssignable(type, (GenericArrayType)toType, typeVarAssigns);
      } else if (toType instanceof WildcardType) {
         return isAssignable(type, (WildcardType)toType, typeVarAssigns);
      } else if (toType instanceof TypeVariable) {
         return isAssignable(type, (TypeVariable<?>)toType, typeVarAssigns);
      } else {
         throw new IllegalStateException("found an unhandled type: " + toType);
      }
   }

   private static boolean isAssignable(Type type, TypeVariable<?> toTypeVariable, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type == null) {
         return true;
      }

      if (toTypeVariable == null) {
         return false;
      }

      if (toTypeVariable.equals(type)) {
         return true;
      }

      if (type instanceof TypeVariable) {
         Type[] bounds = getImplicitBounds((TypeVariable<?>)type);

         for (Type bound : bounds) {
            if (isAssignable(bound, toTypeVariable, typeVarAssigns)) {
               return true;
            }
         }
      }

      if (!(type instanceof Class) && !(type instanceof ParameterizedType) && !(type instanceof GenericArrayType) && !(type instanceof WildcardType)) {
         throw new IllegalStateException("found an unhandled type: " + type);
      } else {
         return false;
      }
   }

   private static boolean isAssignable(Type type, WildcardType toWildcardType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type == null) {
         return true;
      }

      if (toWildcardType == null) {
         return false;
      }

      if (toWildcardType.equals(type)) {
         return true;
      }

      Type[] toUpperBounds = getImplicitUpperBounds(toWildcardType);
      Type[] toLowerBounds = getImplicitLowerBounds(toWildcardType);
      if (!(type instanceof WildcardType)) {
         for (Type toBound : toUpperBounds) {
            if (!isAssignable(type, substituteTypeVariables(toBound, typeVarAssigns), typeVarAssigns)) {
               return false;
            }
         }

         for (Type toBound : toLowerBounds) {
            if (!isAssignable(substituteTypeVariables(toBound, typeVarAssigns), type, typeVarAssigns)) {
               return false;
            }
         }

         return true;
      } else {
         WildcardType wildcardType = (WildcardType)type;
         Type[] upperBounds = getImplicitUpperBounds(wildcardType);
         Type[] lowerBounds = getImplicitLowerBounds(wildcardType);

         for (Type toBound : toUpperBounds) {
            toBound = substituteTypeVariables(toBound, typeVarAssigns);

            for (Type bound : upperBounds) {
               if (!isAssignable(bound, toBound, typeVarAssigns)) {
                  return false;
               }
            }
         }

         for (Type toBound : toLowerBounds) {
            toBound = substituteTypeVariables(toBound, typeVarAssigns);

            for (Type bound : lowerBounds) {
               if (!isAssignable(toBound, bound, typeVarAssigns)) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private static boolean isCyclical(Class<?> cls) {
      for (TypeVariable<?> typeParameter : cls.getTypeParameters()) {
         for (AnnotatedType annotatedBound : typeParameter.getAnnotatedBounds()) {
            if (annotatedBound.getType().getTypeName().contains(cls.getName())) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isInstance(Object value, Type type) {
      if (type == null) {
         return false;
      } else {
         return value == null ? !(type instanceof Class) || !((Class)type).isPrimitive() : isAssignable(value.getClass(), type, null);
      }
   }

   private static <T> void mapTypeVariablesToArguments(Class<T> cls, ParameterizedType parameterizedType, Map<TypeVariable<?>, Type> typeVarAssigns) {
      Type ownerType = parameterizedType.getOwnerType();
      if (ownerType instanceof ParameterizedType) {
         mapTypeVariablesToArguments(cls, (ParameterizedType)ownerType, typeVarAssigns);
      }

      Type[] typeArgs = parameterizedType.getActualTypeArguments();
      TypeVariable<?>[] typeVars = getRawType(parameterizedType).getTypeParameters();
      List<TypeVariable<Class<T>>> typeVarList = Arrays.asList(cls.getTypeParameters());

      for (int i = 0; i < typeArgs.length; i++) {
         TypeVariable<?> typeVar = typeVars[i];
         Type typeArg = typeArgs[i];
         if (typeVarList.contains(typeArg) && typeVarAssigns.containsKey(typeVar)) {
            typeVarAssigns.put((TypeVariable<?>)typeArg, typeVarAssigns.get(typeVar));
         }
      }
   }

   public static Type[] normalizeUpperBounds(Type[] bounds) {
      Objects.requireNonNull(bounds, "bounds");
      if (bounds.length < 2) {
         return bounds;
      }

      Set<Type> types = new HashSet<>(bounds.length);

      for (Type type1 : bounds) {
         boolean subtypeFound = false;

         for (Type type2 : bounds) {
            if (type1 != type2 && isAssignable(type2, type1, null)) {
               subtypeFound = true;
               break;
            }
         }

         if (!subtypeFound) {
            types.add(type1);
         }
      }

      return types.toArray(ArrayUtils.EMPTY_TYPE_ARRAY);
   }

   public static final ParameterizedType parameterize(Class<?> rawClass, Map<TypeVariable<?>, Type> typeVariableMap) {
      Objects.requireNonNull(rawClass, "rawClass");
      Objects.requireNonNull(typeVariableMap, "typeVariableMap");
      return parameterizeWithOwner(null, rawClass, extractTypeArgumentsFrom(typeVariableMap, rawClass.getTypeParameters()));
   }

   public static final ParameterizedType parameterize(Class<?> rawClass, Type... typeArguments) {
      return parameterizeWithOwner(null, rawClass, typeArguments);
   }

   private static String parameterizedTypeToString(ParameterizedType parameterizedType) {
      StringBuilder builder = new StringBuilder();
      Type useOwner = parameterizedType.getOwnerType();
      Class<?> raw = (Class<?>)parameterizedType.getRawType();
      if (useOwner == null) {
         builder.append(raw.getName());
      } else {
         if (useOwner instanceof Class) {
            builder.append(((Class)useOwner).getName());
         } else {
            builder.append(useOwner);
         }

         builder.append('.').append(raw.getSimpleName());
      }

      int[] recursiveTypeIndexes = findRecursiveTypes(parameterizedType);
      if (recursiveTypeIndexes.length > 0) {
         appendRecursiveTypes(builder, recursiveTypeIndexes, parameterizedType.getActualTypeArguments());
      } else {
         GT_JOINER.join(builder, parameterizedType.getActualTypeArguments());
      }

      return builder.toString();
   }

   public static final ParameterizedType parameterizeWithOwner(Type owner, Class<?> rawClass, Map<TypeVariable<?>, Type> typeVariableMap) {
      Objects.requireNonNull(rawClass, "rawClass");
      Objects.requireNonNull(typeVariableMap, "typeVariableMap");
      return parameterizeWithOwner(owner, rawClass, extractTypeArgumentsFrom(typeVariableMap, rawClass.getTypeParameters()));
   }

   public static final ParameterizedType parameterizeWithOwner(Type owner, Class<?> rawClass, Type... typeArguments) {
      Objects.requireNonNull(rawClass, "rawClass");
      Type useOwner;
      if (rawClass.getEnclosingClass() == null) {
         Validate.isTrue(owner == null, "no owner allowed for top-level %s", rawClass);
         useOwner = null;
      } else if (owner == null) {
         useOwner = rawClass.getEnclosingClass();
      } else {
         Validate.isTrue(isAssignable(owner, rawClass.getEnclosingClass()), "%s is invalid owner type for parameterized %s", owner, rawClass);
         useOwner = owner;
      }

      Validate.noNullElements(typeArguments, "null type argument at index %s");
      Validate.isTrue(
         rawClass.getTypeParameters().length == typeArguments.length,
         "invalid number of type parameters specified: expected %d, got %d",
         rawClass.getTypeParameters().length,
         typeArguments.length
      );
      return new TypeUtils.ParameterizedTypeImpl(rawClass, useOwner, typeArguments);
   }

   private static Type substituteTypeVariables(Type type, Map<TypeVariable<?>, Type> typeVarAssigns) {
      if (type instanceof TypeVariable && typeVarAssigns != null) {
         Type replacementType = typeVarAssigns.get(type);
         if (replacementType == null) {
            throw new IllegalArgumentException("missing assignment type for type variable " + type);
         } else {
            return replacementType;
         }
      } else {
         return type;
      }
   }

   public static String toLongString(TypeVariable<?> typeVariable) {
      Objects.requireNonNull(typeVariable, "typeVariable");
      StringBuilder buf = new StringBuilder();
      GenericDeclaration d = typeVariable.getGenericDeclaration();
      if (d instanceof Class) {
         Class<?> c;
         for (c = (Class<?>)d; c.getEnclosingClass() != null; c = c.getEnclosingClass()) {
            buf.insert(0, c.getSimpleName()).insert(0, '.');
         }

         buf.insert(0, c.getName());
      } else if (d instanceof Type) {
         buf.append(toString((Type)d));
      } else {
         buf.append(d);
      }

      return buf.append(':').append(typeVariableToString(typeVariable)).toString();
   }

   public static String toString(Type type) {
      Objects.requireNonNull(type, "type");
      if (type instanceof Class) {
         return classToString((Class)type);
      } else if (type instanceof ParameterizedType) {
         return parameterizedTypeToString((ParameterizedType)type);
      } else if (type instanceof WildcardType) {
         return wildcardTypeToString((WildcardType)type);
      } else if (type instanceof TypeVariable) {
         return typeVariableToString((TypeVariable<?>)type);
      } else if (type instanceof GenericArrayType) {
         return genericArrayTypeToString((GenericArrayType)type);
      } else {
         throw new IllegalArgumentException(ObjectUtils.identityToString(type));
      }
   }

   public static boolean typesSatisfyVariables(Map<TypeVariable<?>, Type> typeVariableMap) {
      Objects.requireNonNull(typeVariableMap, "typeVariableMap");

      for (Entry<TypeVariable<?>, Type> entry : typeVariableMap.entrySet()) {
         TypeVariable<?> typeVar = entry.getKey();
         Type type = entry.getValue();

         for (Type bound : getImplicitBounds(typeVar)) {
            if (!isAssignable(type, substituteTypeVariables(bound, typeVariableMap), typeVariableMap)) {
               return false;
            }
         }
      }

      return true;
   }

   private static String typeVariableToString(TypeVariable<?> typeVariable) {
      StringBuilder builder = new StringBuilder(typeVariable.getName());
      Type[] bounds = typeVariable.getBounds();
      if (bounds.length > 0 && (bounds.length != 1 || !Object.class.equals(bounds[0]))) {
         builder.append(" extends ");
         AMP_JOINER.join(builder, typeVariable.getBounds());
      }

      return builder.toString();
   }

   private static Type[] unrollBounds(Map<TypeVariable<?>, Type> typeArguments, Type[] bounds) {
      Type[] result = bounds;

      for (int i = 0; i < result.length; i++) {
         Type unrolled = unrollVariables(typeArguments, result[i]);
         if (unrolled == null) {
            result = ArrayUtils.remove(result, i--);
         } else {
            result[i] = unrolled;
         }
      }

      return result;
   }

   private static Type unrollVariableAssignments(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> typeVarAssigns) {
      while (true) {
         Type result = typeVarAssigns.get(typeVariable);
         if (!(result instanceof TypeVariable) || result.equals(typeVariable)) {
            return result;
         }

         typeVariable = (TypeVariable<?>)result;
      }
   }

   public static Type unrollVariables(Map<TypeVariable<?>, Type> typeArguments, Type type) {
      if (typeArguments == null) {
         typeArguments = Collections.emptyMap();
      }

      if (containsTypeVariables(type)) {
         if (type instanceof TypeVariable) {
            return unrollVariables(typeArguments, typeArguments.get(type));
         }

         if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)type;
            Map<TypeVariable<?>, Type> parameterizedTypeArguments;
            if (p.getOwnerType() == null) {
               parameterizedTypeArguments = typeArguments;
            } else {
               parameterizedTypeArguments = new HashMap<>(typeArguments);
               parameterizedTypeArguments.putAll(getTypeArguments(p));
            }

            Type[] args = p.getActualTypeArguments();

            for (int i = 0; i < args.length; i++) {
               Type unrolled = unrollVariables(parameterizedTypeArguments, args[i]);
               if (unrolled != null) {
                  args[i] = unrolled;
               }
            }

            return parameterizeWithOwner(p.getOwnerType(), (Class<?>)p.getRawType(), args);
         }

         if (type instanceof WildcardType) {
            WildcardType wild = (WildcardType)type;
            return wildcardType()
               .withUpperBounds(unrollBounds(typeArguments, wild.getUpperBounds()))
               .withLowerBounds(unrollBounds(typeArguments, wild.getLowerBounds()))
               .build();
         }
      }

      return type;
   }

   public static TypeUtils.WildcardTypeBuilder wildcardType() {
      return new TypeUtils.WildcardTypeBuilder();
   }

   private static String wildcardTypeToString(WildcardType wildcardType) {
      StringBuilder builder = new StringBuilder().append('?');
      Type[] lowerBounds = wildcardType.getLowerBounds();
      Type[] upperBounds = wildcardType.getUpperBounds();
      if (lowerBounds.length <= 1 && (lowerBounds.length != 1 || lowerBounds[0] == null)) {
         if (upperBounds.length > 1 || upperBounds.length == 1 && !Object.class.equals(upperBounds[0])) {
            AMP_JOINER.join(builder.append(" extends "), upperBounds);
         }
      } else {
         AMP_JOINER.join(builder.append(" super "), lowerBounds);
      }

      return builder.toString();
   }

   public static <T> Typed<T> wrap(Class<T> type) {
      return wrap((Type)type);
   }

   public static <T> Typed<T> wrap(Type type) {
      return () -> type;
   }

   private static final class GenericArrayTypeImpl implements GenericArrayType {
      private final Type componentType;

      private GenericArrayTypeImpl(Type componentType) {
         this.componentType = componentType;
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this || obj instanceof GenericArrayType && TypeUtils.equals(this, (GenericArrayType)obj);
      }

      @Override
      public Type getGenericComponentType() {
         return this.componentType;
      }

      @Override
      public int hashCode() {
         int result = 1072;
         return result | this.componentType.hashCode();
      }

      @Override
      public String toString() {
         return TypeUtils.toString(this);
      }
   }

   private static final class ParameterizedTypeImpl implements ParameterizedType {
      private final Class<?> raw;
      private final Type useOwner;
      private final Type[] typeArguments;

      private ParameterizedTypeImpl(Class<?> rawClass, Type useOwner, Type[] typeArguments) {
         this.raw = rawClass;
         this.useOwner = useOwner;
         this.typeArguments = Arrays.copyOf(typeArguments, typeArguments.length, Type[].class);
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this || obj instanceof ParameterizedType && TypeUtils.equals(this, (ParameterizedType)obj);
      }

      @Override
      public Type[] getActualTypeArguments() {
         return (Type[])this.typeArguments.clone();
      }

      @Override
      public Type getOwnerType() {
         return this.useOwner;
      }

      @Override
      public Type getRawType() {
         return this.raw;
      }

      @Override
      public int hashCode() {
         int result = 1136;
         result |= this.raw.hashCode();
         result <<= 4;
         result |= Objects.hashCode(this.useOwner);
         result <<= 8;
         return result | Arrays.hashCode(this.typeArguments);
      }

      @Override
      public String toString() {
         return TypeUtils.toString(this);
      }
   }

   public static class WildcardTypeBuilder implements Builder<WildcardType> {
      private Type[] upperBounds;
      private Type[] lowerBounds;

      private WildcardTypeBuilder() {
      }

      public WildcardType build() {
         return new TypeUtils.WildcardTypeImpl(this.upperBounds, this.lowerBounds);
      }

      public TypeUtils.WildcardTypeBuilder withLowerBounds(Type... bounds) {
         this.lowerBounds = bounds;
         return this;
      }

      public TypeUtils.WildcardTypeBuilder withUpperBounds(Type... bounds) {
         this.upperBounds = bounds;
         return this;
      }
   }

   private static final class WildcardTypeImpl implements WildcardType {
      private final Type[] upperBounds;
      private final Type[] lowerBounds;

      private WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
         this.upperBounds = ObjectUtils.defaultIfNull(upperBounds, ArrayUtils.EMPTY_TYPE_ARRAY);
         this.lowerBounds = ObjectUtils.defaultIfNull(lowerBounds, ArrayUtils.EMPTY_TYPE_ARRAY);
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this || obj instanceof WildcardType && TypeUtils.equals(this, (WildcardType)obj);
      }

      @Override
      public Type[] getLowerBounds() {
         return (Type[])this.lowerBounds.clone();
      }

      @Override
      public Type[] getUpperBounds() {
         return (Type[])this.upperBounds.clone();
      }

      @Override
      public int hashCode() {
         int result = 18688;
         result |= Arrays.hashCode(this.upperBounds);
         result <<= 8;
         return result | Arrays.hashCode(this.lowerBounds);
      }

      @Override
      public String toString() {
         return TypeUtils.toString(this);
      }
   }
}
