package com.dfsek.terra.lib.commons.lang3.reflect;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import com.dfsek.terra.lib.commons.lang3.JavaVersion;
import com.dfsek.terra.lib.commons.lang3.StringUtils;
import com.dfsek.terra.lib.commons.lang3.SystemUtils;
import com.dfsek.terra.lib.commons.lang3.Validate;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FieldUtils {
   public static Field[] getAllFields(Class<?> cls) {
      return getAllFieldsList(cls).toArray(ArrayUtils.EMPTY_FIELD_ARRAY);
   }

   public static List<Field> getAllFieldsList(Class<?> cls) {
      Objects.requireNonNull(cls, "cls");
      List<Field> allFields = new ArrayList<>();

      for (Class<?> currentClass = cls; currentClass != null; currentClass = currentClass.getSuperclass()) {
         Field[] declaredFields = currentClass.getDeclaredFields();
         Collections.addAll(allFields, declaredFields);
      }

      return allFields;
   }

   public static Field getDeclaredField(Class<?> cls, String fieldName) {
      return getDeclaredField(cls, fieldName, false);
   }

   public static Field getDeclaredField(Class<?> cls, String fieldName, boolean forceAccess) {
      Objects.requireNonNull(cls, "cls");
      Validate.isTrue(StringUtils.isNotBlank(fieldName), "The field name must not be blank/empty");

      try {
         Field field = cls.getDeclaredField(fieldName);
         if (!MemberUtils.isAccessible(field)) {
            if (!forceAccess) {
               return null;
            }

            field.setAccessible(true);
         }

         return field;
      } catch (NoSuchFieldException var4) {
         return null;
      }
   }

   public static Field getField(Class<?> cls, String fieldName) {
      return MemberUtils.setAccessibleWorkaround(getField(cls, fieldName, false));
   }

   public static Field getField(Class<?> cls, String fieldName, boolean forceAccess) {
      Objects.requireNonNull(cls, "cls");
      Validate.isTrue(StringUtils.isNotBlank(fieldName), "The field name must not be blank/empty");

      for (Class<?> acls = cls; acls != null; acls = acls.getSuperclass()) {
         try {
            Field field = acls.getDeclaredField(fieldName);
            if (!MemberUtils.isPublic(field)) {
               if (!forceAccess) {
                  continue;
               }

               field.setAccessible(true);
            }

            return field;
         } catch (NoSuchFieldException var8) {
         }
      }

      Field match = null;

      for (Class<?> class1 : ClassUtils.getAllInterfaces(cls)) {
         try {
            Field test = class1.getField(fieldName);
            Validate.isTrue(
               match == null,
               "Reference to field %s is ambiguous relative to %s; a matching field exists on two or more implemented interfaces.",
               fieldName,
               cls
            );
            match = test;
         } catch (NoSuchFieldException var7) {
         }
      }

      return match;
   }

   public static List<Field> getFieldsListWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
      Objects.requireNonNull(annotationCls, "annotationCls");
      return getAllFieldsList(cls).stream().filter(field -> field.getAnnotation(annotationCls) != null).collect(Collectors.toList());
   }

   public static Field[] getFieldsWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
      return getFieldsListWithAnnotation(cls, annotationCls).toArray(ArrayUtils.EMPTY_FIELD_ARRAY);
   }

   public static Object readDeclaredField(Object target, String fieldName) throws IllegalAccessException {
      return readDeclaredField(target, fieldName, false);
   }

   public static Object readDeclaredField(Object target, String fieldName, boolean forceAccess) throws IllegalAccessException {
      Objects.requireNonNull(target, "target");
      Class<?> cls = target.getClass();
      Field field = getDeclaredField(cls, fieldName, forceAccess);
      Validate.isTrue(field != null, "Cannot locate declared field %s.%s", cls, fieldName);
      return readField(field, target, false);
   }

   public static Object readDeclaredStaticField(Class<?> cls, String fieldName) throws IllegalAccessException {
      return readDeclaredStaticField(cls, fieldName, false);
   }

   public static Object readDeclaredStaticField(Class<?> cls, String fieldName, boolean forceAccess) throws IllegalAccessException {
      Field field = getDeclaredField(cls, fieldName, forceAccess);
      Validate.notNull(field, "Cannot locate declared field %s.%s", cls.getName(), fieldName);
      return readStaticField(field, false);
   }

   public static Object readField(Field field, Object target) throws IllegalAccessException {
      return readField(field, target, false);
   }

   public static Object readField(Field field, Object target, boolean forceAccess) throws IllegalAccessException {
      Objects.requireNonNull(field, "field");
      if (forceAccess && !field.isAccessible()) {
         field.setAccessible(true);
      } else {
         MemberUtils.setAccessibleWorkaround(field);
      }

      return field.get(target);
   }

   public static Object readField(Object target, String fieldName) throws IllegalAccessException {
      return readField(target, fieldName, false);
   }

   public static Object readField(Object target, String fieldName, boolean forceAccess) throws IllegalAccessException {
      Objects.requireNonNull(target, "target");
      Class<?> cls = target.getClass();
      Field field = getField(cls, fieldName, forceAccess);
      Validate.isTrue(field != null, "Cannot locate field %s on %s", fieldName, cls);
      return readField(field, target, false);
   }

   public static Object readStaticField(Class<?> cls, String fieldName) throws IllegalAccessException {
      return readStaticField(cls, fieldName, false);
   }

   public static Object readStaticField(Class<?> cls, String fieldName, boolean forceAccess) throws IllegalAccessException {
      Field field = getField(cls, fieldName, forceAccess);
      Validate.notNull(field, "Cannot locate field '%s' on %s", fieldName, cls);
      return readStaticField(field, false);
   }

   public static Object readStaticField(Field field) throws IllegalAccessException {
      return readStaticField(field, false);
   }

   public static Object readStaticField(Field field, boolean forceAccess) throws IllegalAccessException {
      Objects.requireNonNull(field, "field");
      Validate.isTrue(MemberUtils.isStatic(field), "The field '%s' is not static", field.getName());
      return readField(field, null, forceAccess);
   }

   public static void removeFinalModifier(Field field) {
      removeFinalModifier(field, true);
   }

   @Deprecated
   public static void removeFinalModifier(Field field, boolean forceAccess) {
      Objects.requireNonNull(field, "field");

      try {
         if (Modifier.isFinal(field.getModifiers())) {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            boolean doForceAccess = forceAccess && !modifiersField.isAccessible();
            if (doForceAccess) {
               modifiersField.setAccessible(true);
            }

            try {
               modifiersField.setInt(field, field.getModifiers() & -17);
            } finally {
               if (doForceAccess) {
                  modifiersField.setAccessible(false);
               }
            }
         }
      } catch (NoSuchFieldException | IllegalAccessException e) {
         if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_12)) {
            throw new UnsupportedOperationException("In java 12+ final cannot be removed.", e);
         }
      }
   }

   public static void writeDeclaredField(Object target, String fieldName, Object value) throws IllegalAccessException {
      writeDeclaredField(target, fieldName, value, false);
   }

   public static void writeDeclaredField(Object target, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
      Objects.requireNonNull(target, "target");
      Class<?> cls = target.getClass();
      Field field = getDeclaredField(cls, fieldName, forceAccess);
      Validate.isTrue(field != null, "Cannot locate declared field %s.%s", cls.getName(), fieldName);
      writeField(field, target, value, false);
   }

   public static void writeDeclaredStaticField(Class<?> cls, String fieldName, Object value) throws IllegalAccessException {
      writeDeclaredStaticField(cls, fieldName, value, false);
   }

   public static void writeDeclaredStaticField(Class<?> cls, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
      Field field = getDeclaredField(cls, fieldName, forceAccess);
      Validate.notNull(field, "Cannot locate declared field %s.%s", cls.getName(), fieldName);
      writeField(field, null, value, false);
   }

   public static void writeField(Field field, Object target, Object value) throws IllegalAccessException {
      writeField(field, target, value, false);
   }

   public static void writeField(Field field, Object target, Object value, boolean forceAccess) throws IllegalAccessException {
      Objects.requireNonNull(field, "field");
      if (forceAccess && !field.isAccessible()) {
         field.setAccessible(true);
      } else {
         MemberUtils.setAccessibleWorkaround(field);
      }

      field.set(target, value);
   }

   public static void writeField(Object target, String fieldName, Object value) throws IllegalAccessException {
      writeField(target, fieldName, value, false);
   }

   public static void writeField(Object target, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
      Objects.requireNonNull(target, "target");
      Class<?> cls = target.getClass();
      Field field = getField(cls, fieldName, forceAccess);
      Validate.isTrue(field != null, "Cannot locate declared field %s.%s", cls.getName(), fieldName);
      writeField(field, target, value, false);
   }

   public static void writeStaticField(Class<?> cls, String fieldName, Object value) throws IllegalAccessException {
      writeStaticField(cls, fieldName, value, false);
   }

   public static void writeStaticField(Class<?> cls, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
      Field field = getField(cls, fieldName, forceAccess);
      Validate.notNull(field, "Cannot locate field %s on %s", fieldName, cls);
      writeStaticField(field, value, false);
   }

   public static void writeStaticField(Field field, Object value) throws IllegalAccessException {
      writeStaticField(field, value, false);
   }

   public static void writeStaticField(Field field, Object value, boolean forceAccess) throws IllegalAccessException {
      Objects.requireNonNull(field, "field");
      Validate.isTrue(MemberUtils.isStatic(field), "The field %s.%s is not static", field.getDeclaringClass().getName(), field.getName());
      writeField(field, null, value, forceAccess);
   }
}
