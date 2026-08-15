package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class Throwables {
   @J2ktIncompatible
   @GwtIncompatible
   private static final String JAVA_LANG_ACCESS_CLASSNAME = "sun.misc.JavaLangAccess";
   @J2ktIncompatible
   @GwtIncompatible
   @VisibleForTesting
   static final String SHARED_SECRETS_CLASSNAME = "sun.misc.SharedSecrets";
   @J2ktIncompatible
   @GwtIncompatible
   private static final @Nullable Object jla = getJla();
   @J2ktIncompatible
   @GwtIncompatible
   private static final @Nullable Method getStackTraceElementMethod = jla == null ? null : getGetMethod();
   @J2ktIncompatible
   @GwtIncompatible
   private static final @Nullable Method getStackTraceDepthMethod = jla == null ? null : getSizeMethod(jla);

   private Throwables() {
   }

   @GwtIncompatible
   public static <X extends Throwable> void throwIfInstanceOf(Throwable throwable, Class<X> declaredType) throws X {
      Preconditions.checkNotNull(throwable);
      if (declaredType.isInstance(throwable)) {
         throw declaredType.cast(throwable);
      }
   }

   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static <X extends Throwable> void propagateIfInstanceOf(@Nullable Throwable throwable, Class<X> declaredType) throws X {
      if (throwable != null) {
         throwIfInstanceOf(throwable, declaredType);
      }
   }

   public static void throwIfUnchecked(Throwable throwable) {
      Preconditions.checkNotNull(throwable);
      if (throwable instanceof RuntimeException) {
         throw (RuntimeException)throwable;
      }

      if (throwable instanceof Error) {
         throw (Error)throwable;
      }
   }

   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static void propagateIfPossible(@Nullable Throwable throwable) {
      if (throwable != null) {
         throwIfUnchecked(throwable);
      }
   }

   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static <X extends Throwable> void propagateIfPossible(@Nullable Throwable throwable, Class<X> declaredType) throws X {
      propagateIfInstanceOf(throwable, declaredType);
      propagateIfPossible(throwable);
   }

   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static <X1 extends Throwable, X2 extends Throwable> void propagateIfPossible(
      @Nullable Throwable throwable, Class<X1> declaredType1, Class<X2> declaredType2
   ) throws X1, X2 {
      Preconditions.checkNotNull(declaredType2);
      propagateIfInstanceOf(throwable, declaredType1);
      propagateIfPossible(throwable, declaredType2);
   }

   @Deprecated
   @CanIgnoreReturnValue
   @J2ktIncompatible
   @GwtIncompatible
   public static RuntimeException propagate(Throwable throwable) {
      throwIfUnchecked(throwable);
      throw new RuntimeException(throwable);
   }

   public static Throwable getRootCause(Throwable throwable) {
      Throwable slowPointer = throwable;

      Throwable cause;
      for (boolean advanceSlowPointer = false; (cause = throwable.getCause()) != null; advanceSlowPointer = !advanceSlowPointer) {
         throwable = cause;
         if (throwable == slowPointer) {
            throw new IllegalArgumentException("Loop in causal chain detected.", throwable);
         }

         if (advanceSlowPointer) {
            slowPointer = slowPointer.getCause();
         }
      }

      return throwable;
   }

   public static List<Throwable> getCausalChain(Throwable throwable) {
      Preconditions.checkNotNull(throwable);
      List<Throwable> causes = new ArrayList<>(4);
      causes.add(throwable);
      Throwable slowPointer = throwable;

      Throwable cause;
      for (boolean advanceSlowPointer = false; (cause = throwable.getCause()) != null; advanceSlowPointer = !advanceSlowPointer) {
         throwable = cause;
         causes.add(throwable);
         if (throwable == slowPointer) {
            throw new IllegalArgumentException("Loop in causal chain detected.", throwable);
         }

         if (advanceSlowPointer) {
            slowPointer = slowPointer.getCause();
         }
      }

      return Collections.unmodifiableList(causes);
   }

   @GwtIncompatible
   public static <X extends Throwable> @Nullable X getCauseAs(Throwable throwable, Class<X> expectedCauseType) {
      try {
         return expectedCauseType.cast(throwable.getCause());
      } catch (ClassCastException e) {
         e.initCause(throwable);
         throw e;
      }
   }

   @GwtIncompatible
   public static String getStackTraceAsString(Throwable throwable) {
      StringWriter stringWriter = new StringWriter();
      throwable.printStackTrace(new PrintWriter(stringWriter));
      return stringWriter.toString();
   }

   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static List<StackTraceElement> lazyStackTrace(Throwable throwable) {
      return lazyStackTraceIsLazy() ? jlaStackTrace(throwable) : Collections.unmodifiableList(Arrays.asList(throwable.getStackTrace()));
   }

   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static boolean lazyStackTraceIsLazy() {
      return getStackTraceElementMethod != null && getStackTraceDepthMethod != null;
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static List<StackTraceElement> jlaStackTrace(Throwable t) {
      Preconditions.checkNotNull(t);
      return new AbstractList<StackTraceElement>() {
         public StackTraceElement get(int n) {
            return (StackTraceElement)Throwables.invokeAccessibleNonThrowingMethod(
               java.util.Objects.requireNonNull(Throwables.getStackTraceElementMethod), java.util.Objects.requireNonNull(Throwables.jla), t, n
            );
         }

         @Override
         public int size() {
            return (Integer)Throwables.invokeAccessibleNonThrowingMethod(
               java.util.Objects.requireNonNull(Throwables.getStackTraceDepthMethod), java.util.Objects.requireNonNull(Throwables.jla), t
            );
         }
      };
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static Object invokeAccessibleNonThrowingMethod(Method method, Object receiver, Object... params) {
      try {
         return method.invoke(receiver, params);
      } catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
         throw propagate(e.getCause());
      }
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static @Nullable Object getJla() {
      try {
         Class<?> sharedSecrets = Class.forName("sun.misc.SharedSecrets", false, null);
         Method langAccess = sharedSecrets.getMethod("getJavaLangAccess");
         return langAccess.invoke(null);
      } catch (ThreadDeath death) {
         throw death;
      } catch (Throwable t) {
         return null;
      }
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static @Nullable Method getGetMethod() {
      return getJlaMethod("getStackTraceElement", Throwable.class, int.class);
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static @Nullable Method getSizeMethod(Object jla) {
      try {
         Method getStackTraceDepth = getJlaMethod("getStackTraceDepth", Throwable.class);
         if (getStackTraceDepth == null) {
            return null;
         }

         getStackTraceDepth.invoke(jla, new Throwable());
         return getStackTraceDepth;
      } catch (UnsupportedOperationException | IllegalAccessException | InvocationTargetException e) {
         return null;
      }
   }

   @J2ktIncompatible
   @GwtIncompatible
   private static @Nullable Method getJlaMethod(String name, Class<?>... parameterTypes) throws ThreadDeath {
      try {
         return Class.forName("sun.misc.JavaLangAccess", false, null).getMethod(name, parameterTypes);
      } catch (ThreadDeath death) {
         throw death;
      } catch (Throwable t) {
         return null;
      }
   }
}
