package com.dfsek.terra.lib.commons.lang3.builder;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import com.dfsek.terra.lib.commons.lang3.tuple.Pair;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EqualsBuilder implements Builder<Boolean> {
   private static final ThreadLocal<Set<Pair<IDKey, IDKey>>> REGISTRY = ThreadLocal.withInitial(HashSet::new);
   private boolean isEquals = true;
   private boolean testTransients;
   private boolean testRecursive;
   private List<Class<?>> bypassReflectionClasses = new ArrayList<>(1);
   private Class<?> reflectUpToClass;
   private String[] excludeFields;

   static Pair<IDKey, IDKey> getRegisterPair(Object lhs, Object rhs) {
      return Pair.of(new IDKey(lhs), new IDKey(rhs));
   }

   static Set<Pair<IDKey, IDKey>> getRegistry() {
      return REGISTRY.get();
   }

   static boolean isRegistered(Object lhs, Object rhs) {
      Set<Pair<IDKey, IDKey>> registry = getRegistry();
      Pair<IDKey, IDKey> pair = getRegisterPair(lhs, rhs);
      Pair<IDKey, IDKey> swappedPair = Pair.of(pair.getRight(), pair.getLeft());
      return registry != null && (registry.contains(pair) || registry.contains(swappedPair));
   }

   public static boolean reflectionEquals(Object lhs, Object rhs, boolean testTransients) {
      return reflectionEquals(lhs, rhs, testTransients, null);
   }

   public static boolean reflectionEquals(
      Object lhs, Object rhs, boolean testTransients, Class<?> reflectUpToClass, boolean testRecursive, String... excludeFields
   ) {
      if (lhs == rhs) {
         return true;
      } else {
         return lhs != null && rhs != null
            ? new EqualsBuilder()
               .setExcludeFields(excludeFields)
               .setReflectUpToClass(reflectUpToClass)
               .setTestTransients(testTransients)
               .setTestRecursive(testRecursive)
               .reflectionAppend(lhs, rhs)
               .isEquals()
            : false;
      }
   }

   public static boolean reflectionEquals(Object lhs, Object rhs, boolean testTransients, Class<?> reflectUpToClass, String... excludeFields) {
      return reflectionEquals(lhs, rhs, testTransients, reflectUpToClass, false, excludeFields);
   }

   public static boolean reflectionEquals(Object lhs, Object rhs, Collection<String> excludeFields) {
      return reflectionEquals(lhs, rhs, ReflectionToStringBuilder.toNoNullStringArray(excludeFields));
   }

   public static boolean reflectionEquals(Object lhs, Object rhs, String... excludeFields) {
      return reflectionEquals(lhs, rhs, false, null, excludeFields);
   }

   private static void register(Object lhs, Object rhs) {
      getRegistry().add(getRegisterPair(lhs, rhs));
   }

   private static void unregister(Object lhs, Object rhs) {
      Set<Pair<IDKey, IDKey>> registry = getRegistry();
      registry.remove(getRegisterPair(lhs, rhs));
      if (registry.isEmpty()) {
         REGISTRY.remove();
      }
   }

   public EqualsBuilder() {
      this.bypassReflectionClasses.add(String.class);
   }

   public EqualsBuilder append(boolean lhs, boolean rhs) {
      if (!this.isEquals) {
         return this;
      }

      this.isEquals = lhs == rhs;
      return this;
   }

   public EqualsBuilder append(boolean[] lhs, boolean[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(byte lhs, byte rhs) {
      if (this.isEquals) {
         this.isEquals = lhs == rhs;
      }

      return this;
   }

   public EqualsBuilder append(byte[] lhs, byte[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(char lhs, char rhs) {
      if (this.isEquals) {
         this.isEquals = lhs == rhs;
      }

      return this;
   }

   public EqualsBuilder append(char[] lhs, char[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(double lhs, double rhs) {
      return this.isEquals ? this.append(Double.doubleToLongBits(lhs), Double.doubleToLongBits(rhs)) : this;
   }

   public EqualsBuilder append(double[] lhs, double[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(float lhs, float rhs) {
      return this.isEquals ? this.append(Float.floatToIntBits(lhs), Float.floatToIntBits(rhs)) : this;
   }

   public EqualsBuilder append(float[] lhs, float[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(int lhs, int rhs) {
      if (this.isEquals) {
         this.isEquals = lhs == rhs;
      }

      return this;
   }

   public EqualsBuilder append(int[] lhs, int[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(long lhs, long rhs) {
      if (this.isEquals) {
         this.isEquals = lhs == rhs;
      }

      return this;
   }

   public EqualsBuilder append(long[] lhs, long[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(Object lhs, Object rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         Class<?> lhsClass = lhs.getClass();
         if (lhsClass.isArray()) {
            this.appendArray(lhs, rhs);
         } else if (this.testRecursive && !ClassUtils.isPrimitiveOrWrapper(lhsClass)) {
            this.reflectionAppend(lhs, rhs);
         } else {
            this.isEquals = lhs.equals(rhs);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(Object[] lhs, Object[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   public EqualsBuilder append(short lhs, short rhs) {
      if (this.isEquals) {
         this.isEquals = lhs == rhs;
      }

      return this;
   }

   public EqualsBuilder append(short[] lhs, short[] rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         if (lhs.length != rhs.length) {
            this.setEquals(false);
            return this;
         }

         for (int i = 0; i < lhs.length && this.isEquals; i++) {
            this.append(lhs[i], rhs[i]);
         }

         return this;
      } else {
         this.setEquals(false);
         return this;
      }
   }

   private void appendArray(Object lhs, Object rhs) {
      if (lhs.getClass() != rhs.getClass()) {
         this.setEquals(false);
      } else if (lhs instanceof long[]) {
         this.append((long[])lhs, (long[])rhs);
      } else if (lhs instanceof int[]) {
         this.append((int[])lhs, (int[])rhs);
      } else if (lhs instanceof short[]) {
         this.append((short[])lhs, (short[])rhs);
      } else if (lhs instanceof char[]) {
         this.append((char[])lhs, (char[])rhs);
      } else if (lhs instanceof byte[]) {
         this.append((byte[])lhs, (byte[])rhs);
      } else if (lhs instanceof double[]) {
         this.append((double[])lhs, (double[])rhs);
      } else if (lhs instanceof float[]) {
         this.append((float[])lhs, (float[])rhs);
      } else if (lhs instanceof boolean[]) {
         this.append((boolean[])lhs, (boolean[])rhs);
      } else {
         this.append((Object[])lhs, (Object[])rhs);
      }
   }

   public EqualsBuilder appendSuper(boolean superEquals) {
      if (!this.isEquals) {
         return this;
      }

      this.isEquals = superEquals;
      return this;
   }

   public Boolean build() {
      return this.isEquals();
   }

   public boolean isEquals() {
      return this.isEquals;
   }

   public EqualsBuilder reflectionAppend(Object lhs, Object rhs) {
      if (!this.isEquals) {
         return this;
      }

      if (lhs == rhs) {
         return this;
      }

      if (lhs != null && rhs != null) {
         Class<?> lhsClass = lhs.getClass();
         Class<?> rhsClass = rhs.getClass();
         Class<?> testClass;
         if (lhsClass.isInstance(rhs)) {
            testClass = lhsClass;
            if (!rhsClass.isInstance(lhs)) {
               testClass = rhsClass;
            }
         } else {
            if (!rhsClass.isInstance(lhs)) {
               this.isEquals = false;
               return this;
            }

            testClass = rhsClass;
            if (!lhsClass.isInstance(rhs)) {
               testClass = lhsClass;
            }
         }

         try {
            if (testClass.isArray()) {
               this.append(lhs, rhs);
            } else if (this.bypassReflectionClasses == null
               || !this.bypassReflectionClasses.contains(lhsClass) && !this.bypassReflectionClasses.contains(rhsClass)) {
               this.reflectionAppend(lhs, rhs, testClass);

               while (testClass.getSuperclass() != null && testClass != this.reflectUpToClass) {
                  testClass = testClass.getSuperclass();
                  this.reflectionAppend(lhs, rhs, testClass);
               }
            } else {
               this.isEquals = lhs.equals(rhs);
            }
         } catch (IllegalArgumentException e) {
            this.isEquals = false;
         }

         return this;
      } else {
         this.isEquals = false;
         return this;
      }
   }

   private void reflectionAppend(Object lhs, Object rhs, Class<?> clazz) {
      if (!isRegistered(lhs, rhs)) {
         try {
            register(lhs, rhs);
            Field[] fields = clazz.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);

            for (int i = 0; i < fields.length && this.isEquals; i++) {
               Field field = fields[i];
               if (!ArrayUtils.contains(this.excludeFields, field.getName())
                  && !field.getName().contains("$")
                  && (this.testTransients || !Modifier.isTransient(field.getModifiers()))
                  && !Modifier.isStatic(field.getModifiers())
                  && !field.isAnnotationPresent(EqualsExclude.class)) {
                  this.append(Reflection.getUnchecked(field, lhs), Reflection.getUnchecked(field, rhs));
               }
            }
         } finally {
            unregister(lhs, rhs);
         }
      }
   }

   public void reset() {
      this.isEquals = true;
   }

   public EqualsBuilder setBypassReflectionClasses(List<Class<?>> bypassReflectionClasses) {
      this.bypassReflectionClasses = bypassReflectionClasses;
      return this;
   }

   protected void setEquals(boolean isEquals) {
      this.isEquals = isEquals;
   }

   public EqualsBuilder setExcludeFields(String... excludeFields) {
      this.excludeFields = excludeFields;
      return this;
   }

   public EqualsBuilder setReflectUpToClass(Class<?> reflectUpToClass) {
      this.reflectUpToClass = reflectUpToClass;
      return this;
   }

   public EqualsBuilder setTestRecursive(boolean testRecursive) {
      this.testRecursive = testRecursive;
      return this;
   }

   public EqualsBuilder setTestTransients(boolean testTransients) {
      this.testTransients = testTransients;
      return this;
   }
}
