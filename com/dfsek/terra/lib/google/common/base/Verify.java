package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Verify {
   public static void verify(boolean expression) {
      if (!expression) {
         throw new VerifyException();
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, @Nullable Object @Nullable ... errorMessageArgs) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, errorMessageArgs));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, char p1) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, int p1) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, long p1) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, @Nullable Object p1) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, char p1, char p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, int p1, char p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, long p1, char p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, @Nullable Object p1, char p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, char p1, int p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, int p1, int p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, long p1, int p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, @Nullable Object p1, int p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, char p1, long p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, int p1, long p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, long p1, long p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, @Nullable Object p1, long p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, char p1, @Nullable Object p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, int p1, @Nullable Object p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, long p1, @Nullable Object p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2));
      }
   }

   public static void verify(boolean expression, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2, p3));
      }
   }

   public static void verify(
      boolean expression, String errorMessageTemplate, @Nullable Object p1, @Nullable Object p2, @Nullable Object p3, @Nullable Object p4
   ) {
      if (!expression) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, p1, p2, p3, p4));
      }
   }

   @CanIgnoreReturnValue
   public static <T> T verifyNotNull(@Nullable T reference) {
      return verifyNotNull(reference, "expected a non-null reference");
   }

   @CanIgnoreReturnValue
   public static <T> T verifyNotNull(@Nullable T reference, String errorMessageTemplate, @Nullable Object @Nullable ... errorMessageArgs) {
      if (reference == null) {
         throw new VerifyException(Strings.lenientFormat(errorMessageTemplate, errorMessageArgs));
      } else {
         return reference;
      }
   }

   private Verify() {
   }
}
