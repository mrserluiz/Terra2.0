package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Preconditions {
   private Preconditions() {
   }

   public static void checkArgument(boolean expression) {
      if (!expression) {
         throw new IllegalArgumentException();
      }
   }

   public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.stringValueOf(errorMessage));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, @Nullable Object @Nullable ... errorMessageArgs) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, errorMessageArgs));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, char p1) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, int p1) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, long p1) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, @Nullable Object p1) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, char p1, char p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, char p1, int p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, char p1, long p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, char p1, @Nullable Object p2) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, int p1, char p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, int p1, int p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, int p1, long p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, int p1, @Nullable Object p2) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, long p1, char p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, long p1, int p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, long p1, long p2) {
      if (!expression) {
         throw new IllegalArgumentException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, long p1, @Nullable Object p2) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, @Nullable Object p1, char p2) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, @Nullable Object p1, int p2) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, @Nullable Object p1, long p2) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, @Nullable String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkArgument(boolean expression, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2, p3));
      }
   }

   public static void checkArgument(
      boolean expression, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4
   ) {
      if (!expression) {
         throw new IllegalArgumentException(Platform.lenientFormat(errorMessageTemplate, p1, p2, p3, p4));
      }
   }

   public static void checkState(boolean expression) {
      if (!expression) {
         throw new IllegalStateException();
      }
   }

   public static void checkState(boolean expression, @Nullable Object errorMessage) {
      if (!expression) {
         throw new IllegalStateException(Platform.stringValueOf(errorMessage));
      }
   }

   public static void checkState(boolean expression, @Nullable String errorMessageTemplate, @Nullable Object @Nullable ... errorMessageArgs) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, errorMessageArgs));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, char p1) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, int p1) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, long p1) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, @Nullable Object p1) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, char p1, char p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, char p1, int p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, char p1, long p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, char p1, @Nullable Object p2) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, int p1, char p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, int p1, int p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, int p1, long p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, int p1, @Nullable Object p2) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, long p1, char p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, long p1, int p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, long p1, long p2) {
      if (!expression) {
         throw new IllegalStateException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, long p1, @Nullable Object p2) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, @Nullable Object p1, char p2) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, @Nullable Object p1, int p2) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, @Nullable Object p1, long p2) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void checkState(boolean expression, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2, p3));
      }
   }

   public static void checkState(
      boolean expression, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4
   ) {
      if (!expression) {
         throw new IllegalStateException(Platform.lenientFormat(errorMessageTemplate, p1, p2, p3, p4));
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference) {
      if (reference == null) {
         throw new NullPointerException();
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, @Nullable Object errorMessage) {
      if (reference == null) {
         throw new NullPointerException(Platform.stringValueOf(errorMessage));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, @Nullable Object @Nullable ... errorMessageArgs) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, errorMessageArgs));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, char p1) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, int p1) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, long p1) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, @Nullable Object p1) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, char p1, char p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, char p1, int p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, char p1, long p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, char p1, @Nullable Object p2) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, int p1, char p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, int p1, int p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, int p1, long p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, int p1, @Nullable Object p2) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, long p1, char p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, long p1, int p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, long p1, long p2) {
      if (reference == null) {
         throw new NullPointerException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, long p1, @Nullable Object p2) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, @Nullable Object p1, char p2) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, @Nullable Object p1, int p2) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, @Nullable Object p1, long p2) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(@Nullable T reference, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2, p3));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static <T> T checkNotNull(
      @Nullable T reference, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4
   ) {
      if (reference == null) {
         throw new NullPointerException(Platform.lenientFormat(errorMessageTemplate, p1, p2, p3, p4));
      } else {
         return reference;
      }
   }

   @CanIgnoreReturnValue
   public static int checkElementIndex(int index, int size) {
      return checkElementIndex(index, size, "index");
   }

   @CanIgnoreReturnValue
   public static int checkElementIndex(int index, int size, String desc) {
      if (index >= 0 && index < size) {
         return index;
      } else {
         throw new IndexOutOfBoundsException(badElementIndex(index, size, desc));
      }
   }

   private static String badElementIndex(int index, int size, String desc) {
      if (index < 0) {
         return Strings.lenientFormat("%s (%s) must not be negative", desc, index);
      } else if (size < 0) {
         throw new IllegalArgumentException("negative size: " + size);
      } else {
         return Strings.lenientFormat("%s (%s) must be less than size (%s)", desc, index, size);
      }
   }

   @CanIgnoreReturnValue
   public static int checkPositionIndex(int index, int size) {
      return checkPositionIndex(index, size, "index");
   }

   @CanIgnoreReturnValue
   public static int checkPositionIndex(int index, int size, String desc) {
      if (index >= 0 && index <= size) {
         return index;
      } else {
         throw new IndexOutOfBoundsException(badPositionIndex(index, size, desc));
      }
   }

   private static String badPositionIndex(int index, int size, String desc) {
      if (index < 0) {
         return Strings.lenientFormat("%s (%s) must not be negative", desc, index);
      } else if (size < 0) {
         throw new IllegalArgumentException("negative size: " + size);
      } else {
         return Strings.lenientFormat("%s (%s) must not be greater than size (%s)", desc, index, size);
      }
   }

   public static void checkPositionIndexes(int start, int end, int size) {
      if (start < 0 || end < start || end > size) {
         throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
      }
   }

   private static String badPositionIndexes(int start, int end, int size) {
      if (start < 0 || start > size) {
         return badPositionIndex(start, size, "start index");
      } else {
         return end >= 0 && end <= size
            ? Strings.lenientFormat("end index (%s) must not be less than start index (%s)", end, start)
            : badPositionIndex(end, size, "end index");
      }
   }
}
