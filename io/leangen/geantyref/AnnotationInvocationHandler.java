package io.leangen.geantyref;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

class AnnotationInvocationHandler implements Annotation, InvocationHandler, Serializable {
   private static final long serialVersionUID = 8615044376674805680L;
   private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
   private final Class<? extends Annotation> annotationType;
   private final Map<String, Object> values;
   private final int hashCode;

   AnnotationInvocationHandler(Class<? extends Annotation> annotationType, Map<String, Object> values) throws AnnotationFormatException {
      Class<?>[] interfaces = annotationType.getInterfaces();
      if (annotationType.isAnnotation() && interfaces.length == 1 && interfaces[0] == Annotation.class) {
         this.annotationType = annotationType;
         this.values = Collections.unmodifiableMap(normalize(annotationType, values));
         this.hashCode = this.calculateHashCode();
      } else {
         throw new AnnotationFormatException(annotationType.getName() + " is not an annotation type");
      }
   }

   static Map<String, Object> normalize(Class<? extends Annotation> annotationType, Map<String, Object> values) throws AnnotationFormatException {
      Set<String> missing = new HashSet<>();
      Set<String> invalid = new HashSet<>();
      Map<String, Object> valid = new HashMap<>();

      for (Method element : annotationType.getDeclaredMethods()) {
         String elementName = element.getName();
         if (values.containsKey(elementName)) {
            Class<?> returnType = element.getReturnType();
            if (returnType.isPrimitive()) {
               returnType = primitiveWrapperMap.get(returnType);
            }

            if (returnType.isInstance(values.get(elementName))) {
               valid.put(elementName, values.get(elementName));
            } else {
               invalid.add(elementName);
            }
         } else if (element.getDefaultValue() != null) {
            valid.put(elementName, element.getDefaultValue());
         } else {
            missing.add(elementName);
         }
      }

      if (!missing.isEmpty()) {
         throw new AnnotationFormatException("Missing value(s) for " + String.join(",", missing));
      } else if (!invalid.isEmpty()) {
         throw new AnnotationFormatException("Incompatible type(s) provided for " + String.join(",", invalid));
      } else {
         return valid;
      }
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return this.values.containsKey(method.getName()) ? this.values.get(method.getName()) : method.invoke(this, args);
   }

   @Override
   public Class<? extends Annotation> annotationType() {
      return this.annotationType;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      }

      if (other == null) {
         return false;
      }

      if (!this.annotationType.isInstance(other)) {
         return false;
      }

      Annotation that = this.annotationType.cast(other);

      for (Entry<String, Object> element : this.values.entrySet()) {
         Object value = element.getValue();

         Object otherValue;
         try {
            otherValue = that.annotationType().getMethod(element.getKey()).invoke(that);
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
         }

         if (!Objects.deepEquals(value, otherValue)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder();
      result.append('@').append(this.annotationType.getName()).append('(');

      for (String elementName : new TreeSet<>(this.values.keySet())) {
         String value;
         if (this.values.get(elementName).getClass().isArray()) {
            value = Arrays.deepToString(new Object[]{this.values.get(elementName)}).replaceAll("^\\[\\[", "[").replaceAll("]]$", "]");
         } else {
            value = this.values.get(elementName).toString();
         }

         result.append(elementName).append('=').append(value).append(", ");
      }

      if (this.values.size() > 0) {
         result.delete(result.length() - 2, result.length());
      }

      result.append(")");
      return result.toString();
   }

   private int calculateHashCode() {
      int hashCode = 0;

      for (Entry<String, Object> element : this.values.entrySet()) {
         hashCode += 127 * element.getKey().hashCode() ^ this.calculateHashCode(element.getValue());
      }

      return hashCode;
   }

   private int calculateHashCode(Object element) {
      if (!element.getClass().isArray()) {
         return element.hashCode();
      } else if (element instanceof Object[]) {
         return Arrays.hashCode((Object[])element);
      } else if (element instanceof byte[]) {
         return Arrays.hashCode((byte[])element);
      } else if (element instanceof short[]) {
         return Arrays.hashCode((short[])element);
      } else if (element instanceof int[]) {
         return Arrays.hashCode((int[])element);
      } else if (element instanceof long[]) {
         return Arrays.hashCode((long[])element);
      } else if (element instanceof char[]) {
         return Arrays.hashCode((char[])element);
      } else if (element instanceof float[]) {
         return Arrays.hashCode((float[])element);
      } else if (element instanceof double[]) {
         return Arrays.hashCode((double[])element);
      } else {
         return element instanceof boolean[] ? Arrays.hashCode((boolean[])element) : Objects.hashCode(element);
      }
   }

   static {
      primitiveWrapperMap.put(boolean.class, Boolean.class);
      primitiveWrapperMap.put(byte.class, Byte.class);
      primitiveWrapperMap.put(char.class, Character.class);
      primitiveWrapperMap.put(short.class, Short.class);
      primitiveWrapperMap.put(int.class, Integer.class);
      primitiveWrapperMap.put(long.class, Long.class);
      primitiveWrapperMap.put(double.class, Double.class);
      primitiveWrapperMap.put(float.class, Float.class);
   }
}
