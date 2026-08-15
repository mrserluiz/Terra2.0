package com.dfsek.terra.lib.commons.lang3.builder;

import com.dfsek.terra.lib.commons.lang3.ArraySorter;
import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.stream.Streams;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public class ReflectionToStringBuilder extends ToStringBuilder {
   private boolean appendStatics;
   private boolean appendTransients;
   private boolean excludeNullValues;
   protected String[] excludeFieldNames;
   protected String[] includeFieldNames;
   private Class<?> upToClass;

   static String[] toNoNullStringArray(Collection<String> collection) {
      return collection == null ? ArrayUtils.EMPTY_STRING_ARRAY : toNoNullStringArray(collection.toArray());
   }

   static String[] toNoNullStringArray(Object[] array) {
      return Streams.nonNull(array).map(Objects::toString).toArray(String[]::new);
   }

   public static String toString(Object object) {
      return toString(object, null, false, false, null);
   }

   public static String toString(Object object, ToStringStyle style) {
      return toString(object, style, false, false, null);
   }

   public static String toString(Object object, ToStringStyle style, boolean outputTransients) {
      return toString(object, style, outputTransients, false, null);
   }

   public static String toString(Object object, ToStringStyle style, boolean outputTransients, boolean outputStatics) {
      return toString(object, style, outputTransients, outputStatics, null);
   }

   public static <T> String toString(
      T object, ToStringStyle style, boolean outputTransients, boolean outputStatics, boolean excludeNullValues, Class<? super T> reflectUpToClass
   ) {
      return new ReflectionToStringBuilder(object, style, null, reflectUpToClass, outputTransients, outputStatics, excludeNullValues).toString();
   }

   public static <T> String toString(T object, ToStringStyle style, boolean outputTransients, boolean outputStatics, Class<? super T> reflectUpToClass) {
      return new ReflectionToStringBuilder(object, style, null, reflectUpToClass, outputTransients, outputStatics).toString();
   }

   public static String toStringExclude(Object object, Collection<String> excludeFieldNames) {
      return toStringExclude(object, toNoNullStringArray(excludeFieldNames));
   }

   public static String toStringExclude(Object object, String... excludeFieldNames) {
      return new ReflectionToStringBuilder(object).setExcludeFieldNames(excludeFieldNames).toString();
   }

   public static String toStringInclude(Object object, Collection<String> includeFieldNames) {
      return toStringInclude(object, toNoNullStringArray(includeFieldNames));
   }

   public static String toStringInclude(Object object, String... includeFieldNames) {
      return new ReflectionToStringBuilder(object).setIncludeFieldNames(includeFieldNames).toString();
   }

   public ReflectionToStringBuilder(Object object) {
      super(object);
   }

   public ReflectionToStringBuilder(Object object, ToStringStyle style) {
      super(object, style);
   }

   public ReflectionToStringBuilder(Object object, ToStringStyle style, StringBuffer buffer) {
      super(object, style, buffer);
   }

   public <T> ReflectionToStringBuilder(
      T object, ToStringStyle style, StringBuffer buffer, Class<? super T> reflectUpToClass, boolean outputTransients, boolean outputStatics
   ) {
      super(object, style, buffer);
      this.setUpToClass(reflectUpToClass);
      this.setAppendTransients(outputTransients);
      this.setAppendStatics(outputStatics);
   }

   public <T> ReflectionToStringBuilder(
      T object,
      ToStringStyle style,
      StringBuffer buffer,
      Class<? super T> reflectUpToClass,
      boolean outputTransients,
      boolean outputStatics,
      boolean excludeNullValues
   ) {
      super(object, style, buffer);
      this.setUpToClass(reflectUpToClass);
      this.setAppendTransients(outputTransients);
      this.setAppendStatics(outputStatics);
      this.setExcludeNullValues(excludeNullValues);
   }

   protected boolean accept(Field field) {
      if (field.getName().indexOf(36) != -1) {
         return false;
      } else if (Modifier.isTransient(field.getModifiers()) && !this.isAppendTransients()) {
         return false;
      } else if (Modifier.isStatic(field.getModifiers()) && !this.isAppendStatics()) {
         return false;
      } else if (this.excludeFieldNames != null && Arrays.binarySearch(this.excludeFieldNames, field.getName()) >= 0) {
         return false;
      } else {
         return ArrayUtils.isNotEmpty(this.includeFieldNames)
            ? Arrays.binarySearch(this.includeFieldNames, field.getName()) >= 0
            : !field.isAnnotationPresent(ToStringExclude.class);
      }
   }

   protected void appendFieldsIn(Class<?> clazz) {
      if (clazz.isArray()) {
         this.reflectionAppendArray(this.getObject());
      } else {
         Field[] fields = ArraySorter.sort(clazz.getDeclaredFields(), Comparator.comparing(Field::getName));
         AccessibleObject.setAccessible(fields, true);

         for (Field field : fields) {
            String fieldName = field.getName();
            if (this.accept(field)) {
               try {
                  Object fieldValue = this.getValue(field);
                  if (!this.excludeNullValues || fieldValue != null) {
                     this.append(fieldName, fieldValue, !field.isAnnotationPresent(ToStringSummary.class));
                  }
               } catch (IllegalAccessException e) {
                  throw new IllegalStateException(e);
               }
            }
         }
      }
   }

   public String[] getExcludeFieldNames() {
      return (String[])this.excludeFieldNames.clone();
   }

   public String[] getIncludeFieldNames() {
      return (String[])this.includeFieldNames.clone();
   }

   public Class<?> getUpToClass() {
      return this.upToClass;
   }

   protected Object getValue(Field field) throws IllegalAccessException {
      return field.get(this.getObject());
   }

   public boolean isAppendStatics() {
      return this.appendStatics;
   }

   public boolean isAppendTransients() {
      return this.appendTransients;
   }

   public boolean isExcludeNullValues() {
      return this.excludeNullValues;
   }

   public ReflectionToStringBuilder reflectionAppendArray(Object array) {
      this.getStyle().reflectionAppendArrayDetail(this.getStringBuffer(), null, array);
      return this;
   }

   public void setAppendStatics(boolean appendStatics) {
      this.appendStatics = appendStatics;
   }

   public void setAppendTransients(boolean appendTransients) {
      this.appendTransients = appendTransients;
   }

   public ReflectionToStringBuilder setExcludeFieldNames(String... excludeFieldNamesParam) {
      if (excludeFieldNamesParam == null) {
         this.excludeFieldNames = null;
      } else {
         this.excludeFieldNames = ArraySorter.sort(toNoNullStringArray(excludeFieldNamesParam));
      }

      return this;
   }

   public void setExcludeNullValues(boolean excludeNullValues) {
      this.excludeNullValues = excludeNullValues;
   }

   public ReflectionToStringBuilder setIncludeFieldNames(String... includeFieldNamesParam) {
      if (includeFieldNamesParam == null) {
         this.includeFieldNames = null;
      } else {
         this.includeFieldNames = ArraySorter.sort(toNoNullStringArray(includeFieldNamesParam));
      }

      return this;
   }

   public void setUpToClass(Class<?> clazz) {
      if (clazz != null) {
         Object object = this.getObject();
         if (object != null && !clazz.isInstance(object)) {
            throw new IllegalArgumentException("Specified class is not a superclass of the object");
         }
      }

      this.upToClass = clazz;
   }

   @Override
   public String toString() {
      if (this.getObject() == null) {
         return this.getStyle().getNullText();
      }

      this.validate();
      Class<?> clazz = this.getObject().getClass();
      this.appendFieldsIn(clazz);

      while (clazz.getSuperclass() != null && clazz != this.getUpToClass()) {
         clazz = clazz.getSuperclass();
         this.appendFieldsIn(clazz);
      }

      return super.toString();
   }

   private void validate() {
      if (ArrayUtils.containsAny(this.excludeFieldNames, this.includeFieldNames)) {
         ToStringStyle.unregister(this.getObject());
         throw new IllegalStateException("includeFieldNames and excludeFieldNames must not intersect");
      }
   }
}
