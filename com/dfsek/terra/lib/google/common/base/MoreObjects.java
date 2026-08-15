package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class MoreObjects {
   public static <T> T firstNonNull(@Nullable T first, @Nullable T second) {
      if (first != null) {
         return first;
      } else if (second != null) {
         return second;
      } else {
         throw new NullPointerException("Both parameters are null");
      }
   }

   public static MoreObjects.ToStringHelper toStringHelper(Object self) {
      return new MoreObjects.ToStringHelper(self.getClass().getSimpleName());
   }

   public static MoreObjects.ToStringHelper toStringHelper(Class<?> clazz) {
      return new MoreObjects.ToStringHelper(clazz.getSimpleName());
   }

   public static MoreObjects.ToStringHelper toStringHelper(String className) {
      return new MoreObjects.ToStringHelper(className);
   }

   private MoreObjects() {
   }

   public static final class ToStringHelper {
      private final String className;
      private final MoreObjects.ToStringHelper.ValueHolder holderHead = new MoreObjects.ToStringHelper.ValueHolder();
      private MoreObjects.ToStringHelper.ValueHolder holderTail = this.holderHead;
      private boolean omitNullValues = false;
      private boolean omitEmptyValues = false;

      private ToStringHelper(String className) {
         this.className = Preconditions.checkNotNull(className);
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper omitNullValues() {
         this.omitNullValues = true;
         return this;
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper omitEmptyValues() {
         this.omitEmptyValues = true;
         return this;
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper add(String name, @Nullable Object value) {
         return this.addHolder(name, value);
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper add(String name, boolean value) {
         return this.addUnconditionalHolder(name, String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper add(String name, char value) {
         return this.addUnconditionalHolder(name, String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper add(String name, double value) {
         return this.addUnconditionalHolder(name, String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper add(String name, float value) {
         return this.addUnconditionalHolder(name, String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper add(String name, int value) {
         return this.addUnconditionalHolder(name, String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper add(String name, long value) {
         return this.addUnconditionalHolder(name, String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper addValue(@Nullable Object value) {
         return this.addHolder(value);
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper addValue(boolean value) {
         return this.addUnconditionalHolder(String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper addValue(char value) {
         return this.addUnconditionalHolder(String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper addValue(double value) {
         return this.addUnconditionalHolder(String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper addValue(float value) {
         return this.addUnconditionalHolder(String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper addValue(int value) {
         return this.addUnconditionalHolder(String.valueOf(value));
      }

      @CanIgnoreReturnValue
      public MoreObjects.ToStringHelper addValue(long value) {
         return this.addUnconditionalHolder(String.valueOf(value));
      }

      private static boolean isEmpty(Object value) {
         if (value instanceof CharSequence) {
            return ((CharSequence)value).length() == 0;
         } else if (value instanceof Collection) {
            return ((Collection)value).isEmpty();
         } else if (value instanceof Map) {
            return ((Map)value).isEmpty();
         } else if (value instanceof java.util.Optional) {
            return !((java.util.Optional)value).isPresent();
         } else if (value instanceof OptionalInt) {
            return !((OptionalInt)value).isPresent();
         } else if (value instanceof OptionalLong) {
            return !((OptionalLong)value).isPresent();
         } else if (value instanceof OptionalDouble) {
            return !((OptionalDouble)value).isPresent();
         } else if (value instanceof Optional) {
            return !((Optional)value).isPresent();
         } else {
            return value.getClass().isArray() ? Array.getLength(value) == 0 : false;
         }
      }

      @Override
      public String toString() {
         boolean omitNullValuesSnapshot = this.omitNullValues;
         boolean omitEmptyValuesSnapshot = this.omitEmptyValues;
         String nextSeparator = "";
         StringBuilder builder = new StringBuilder(32).append(this.className).append('{');

         for (MoreObjects.ToStringHelper.ValueHolder valueHolder = this.holderHead.next; valueHolder != null; valueHolder = valueHolder.next) {
            Object value = valueHolder.value;
            if (valueHolder instanceof MoreObjects.ToStringHelper.UnconditionalValueHolder
               || (value == null ? !omitNullValuesSnapshot : !omitEmptyValuesSnapshot || !isEmpty(value))) {
               builder.append(nextSeparator);
               nextSeparator = ", ";
               if (valueHolder.name != null) {
                  builder.append(valueHolder.name).append('=');
               }

               if (value != null && value.getClass().isArray()) {
                  Object[] objectArray = new Object[]{value};
                  String arrayString = Arrays.deepToString(objectArray);
                  builder.append(arrayString, 1, arrayString.length() - 1);
               } else {
                  builder.append(value);
               }
            }
         }

         return builder.append('}').toString();
      }

      private MoreObjects.ToStringHelper.ValueHolder addHolder() {
         MoreObjects.ToStringHelper.ValueHolder valueHolder = new MoreObjects.ToStringHelper.ValueHolder();
         this.holderTail = this.holderTail.next = valueHolder;
         return valueHolder;
      }

      @CanIgnoreReturnValue
      private MoreObjects.ToStringHelper addHolder(@Nullable Object value) {
         MoreObjects.ToStringHelper.ValueHolder valueHolder = this.addHolder();
         valueHolder.value = value;
         return this;
      }

      @CanIgnoreReturnValue
      private MoreObjects.ToStringHelper addHolder(String name, @Nullable Object value) {
         MoreObjects.ToStringHelper.ValueHolder valueHolder = this.addHolder();
         valueHolder.value = value;
         valueHolder.name = Preconditions.checkNotNull(name);
         return this;
      }

      private MoreObjects.ToStringHelper.UnconditionalValueHolder addUnconditionalHolder() {
         MoreObjects.ToStringHelper.UnconditionalValueHolder valueHolder = new MoreObjects.ToStringHelper.UnconditionalValueHolder();
         this.holderTail = this.holderTail.next = valueHolder;
         return valueHolder;
      }

      @CanIgnoreReturnValue
      private MoreObjects.ToStringHelper addUnconditionalHolder(Object value) {
         MoreObjects.ToStringHelper.UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
         valueHolder.value = value;
         return this;
      }

      @CanIgnoreReturnValue
      private MoreObjects.ToStringHelper addUnconditionalHolder(String name, Object value) {
         MoreObjects.ToStringHelper.UnconditionalValueHolder valueHolder = this.addUnconditionalHolder();
         valueHolder.value = value;
         valueHolder.name = Preconditions.checkNotNull(name);
         return this;
      }

      private static final class UnconditionalValueHolder extends MoreObjects.ToStringHelper.ValueHolder {
         private UnconditionalValueHolder() {
         }
      }

      static class ValueHolder {
         @Nullable String name;
         @Nullable Object value;
         MoreObjects.ToStringHelper.@Nullable ValueHolder next;
      }
   }
}
