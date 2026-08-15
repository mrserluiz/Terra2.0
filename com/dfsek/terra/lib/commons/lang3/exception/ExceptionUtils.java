package com.dfsek.terra.lib.commons.lang3.exception;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import com.dfsek.terra.lib.commons.lang3.StringUtils;
import com.dfsek.terra.lib.commons.lang3.reflect.MethodUtils;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ExceptionUtils {
   private static final String[] CAUSE_METHOD_NAMES = new String[]{
      "getCause",
      "getNextException",
      "getTargetException",
      "getException",
      "getSourceException",
      "getRootCause",
      "getCausedByException",
      "getNested",
      "getLinkedException",
      "getNestedException",
      "getLinkedCause",
      "getThrowable"
   };
   private static final int NOT_FOUND = -1;
   static final String WRAPPED_MARKER = " [wrapped] ";

   public static <T extends RuntimeException> T asRuntimeException(Throwable throwable) {
      return eraseType(throwable);
   }

   private static <R, T extends Throwable> R eraseType(Throwable throwable) throws T {
      throw throwable;
   }

   public static void forEach(Throwable throwable, Consumer<Throwable> consumer) {
      stream(throwable).forEach(consumer);
   }

   @Deprecated
   public static Throwable getCause(Throwable throwable) {
      return getCause(throwable, null);
   }

   @Deprecated
   public static Throwable getCause(Throwable throwable, String[] methodNames) {
      if (throwable == null) {
         return null;
      }

      if (methodNames == null) {
         Throwable cause = throwable.getCause();
         if (cause != null) {
            return cause;
         }

         methodNames = CAUSE_METHOD_NAMES;
      }

      return Stream.of(methodNames).map(m -> getCauseUsingMethodName(throwable, m)).filter(Objects::nonNull).findFirst().orElse(null);
   }

   private static Throwable getCauseUsingMethodName(Throwable throwable, String methodName) {
      if (methodName != null) {
         Method method = MethodUtils.getMethodObject(throwable.getClass(), methodName);
         if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
            try {
               return (Throwable)method.invoke(throwable);
            } catch (ReflectiveOperationException var4) {
            }
         }
      }

      return null;
   }

   @Deprecated
   public static String[] getDefaultCauseMethodNames() {
      return ArrayUtils.clone(CAUSE_METHOD_NAMES);
   }

   public static String getMessage(Throwable th) {
      if (th == null) {
         return "";
      }

      String clsName = ClassUtils.getShortClassName(th, null);
      return clsName + ": " + StringUtils.defaultString(th.getMessage());
   }

   public static Throwable getRootCause(Throwable throwable) {
      List<Throwable> list = getThrowableList(throwable);
      return list.isEmpty() ? null : list.get(list.size() - 1);
   }

   public static String getRootCauseMessage(Throwable throwable) {
      Throwable root = getRootCause(throwable);
      return getMessage(root == null ? throwable : root);
   }

   public static String[] getRootCauseStackTrace(Throwable throwable) {
      return getRootCauseStackTraceList(throwable).toArray(ArrayUtils.EMPTY_STRING_ARRAY);
   }

   public static List<String> getRootCauseStackTraceList(Throwable throwable) {
      if (throwable == null) {
         return Collections.emptyList();
      }

      Throwable[] throwables = getThrowables(throwable);
      int count = throwables.length;
      List<String> frames = new ArrayList<>();
      List<String> nextTrace = getStackFrameList(throwables[count - 1]);
      int i = count;

      while (--i >= 0) {
         List<String> trace = nextTrace;
         if (i != 0) {
            nextTrace = getStackFrameList(throwables[i - 1]);
            removeCommonFrames(trace, nextTrace);
         }

         if (i == count - 1) {
            frames.add(throwables[i].toString());
         } else {
            frames.add(" [wrapped] " + throwables[i].toString());
         }

         frames.addAll(trace);
      }

      return frames;
   }

   static List<String> getStackFrameList(Throwable throwable) {
      String stackTrace = getStackTrace(throwable);
      String linebreak = System.lineSeparator();
      StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
      List<String> list = new ArrayList<>();
      boolean traceStarted = false;

      while (frames.hasMoreTokens()) {
         String token = frames.nextToken();
         int at = token.indexOf("at");
         if (at != -1 && token.substring(0, at).trim().isEmpty()) {
            traceStarted = true;
            list.add(token);
         } else if (traceStarted) {
            break;
         }
      }

      return list;
   }

   static String[] getStackFrames(String stackTrace) {
      String linebreak = System.lineSeparator();
      StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
      List<String> list = new ArrayList<>();

      while (frames.hasMoreTokens()) {
         list.add(frames.nextToken());
      }

      return list.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
   }

   public static String[] getStackFrames(Throwable throwable) {
      return throwable == null ? ArrayUtils.EMPTY_STRING_ARRAY : getStackFrames(getStackTrace(throwable));
   }

   public static String getStackTrace(Throwable throwable) {
      if (throwable == null) {
         return "";
      }

      StringWriter sw = new StringWriter();
      throwable.printStackTrace(new PrintWriter(sw, true));
      return sw.toString();
   }

   public static int getThrowableCount(Throwable throwable) {
      return getThrowableList(throwable).size();
   }

   public static List<Throwable> getThrowableList(Throwable throwable) {
      List<Throwable> list = new ArrayList<>();

      while (throwable != null && !list.contains(throwable)) {
         list.add(throwable);
         throwable = throwable.getCause();
      }

      return list;
   }

   public static Throwable[] getThrowables(Throwable throwable) {
      return getThrowableList(throwable).toArray(ArrayUtils.EMPTY_THROWABLE_ARRAY);
   }

   public static boolean hasCause(Throwable chain, Class<? extends Throwable> type) {
      if (chain instanceof UndeclaredThrowableException) {
         chain = chain.getCause();
      }

      return type.isInstance(chain);
   }

   private static int indexOf(Throwable throwable, Class<? extends Throwable> type, int fromIndex, boolean subclass) {
      if (throwable != null && type != null) {
         if (fromIndex < 0) {
            fromIndex = 0;
         }

         Throwable[] throwables = getThrowables(throwable);
         if (fromIndex >= throwables.length) {
            return -1;
         }

         if (subclass) {
            for (int i = fromIndex; i < throwables.length; i++) {
               if (type.isAssignableFrom(throwables[i].getClass())) {
                  return i;
               }
            }
         } else {
            for (int i = fromIndex; i < throwables.length; i++) {
               if (type.equals(throwables[i].getClass())) {
                  return i;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int indexOfThrowable(Throwable throwable, Class<? extends Throwable> clazz) {
      return indexOf(throwable, clazz, 0, false);
   }

   public static int indexOfThrowable(Throwable throwable, Class<? extends Throwable> clazz, int fromIndex) {
      return indexOf(throwable, clazz, fromIndex, false);
   }

   public static int indexOfType(Throwable throwable, Class<? extends Throwable> type) {
      return indexOf(throwable, type, 0, true);
   }

   public static int indexOfType(Throwable throwable, Class<? extends Throwable> type, int fromIndex) {
      return indexOf(throwable, type, fromIndex, true);
   }

   public static boolean isChecked(Throwable throwable) {
      return throwable != null && !(throwable instanceof Error) && !(throwable instanceof RuntimeException);
   }

   public static boolean isUnchecked(Throwable throwable) {
      return throwable != null && (throwable instanceof Error || throwable instanceof RuntimeException);
   }

   public static void printRootCauseStackTrace(Throwable throwable) {
      printRootCauseStackTrace(throwable, System.err);
   }

   public static void printRootCauseStackTrace(Throwable throwable, PrintStream printStream) {
      if (throwable != null) {
         Objects.requireNonNull(printStream, "printStream");
         getRootCauseStackTraceList(throwable).forEach(printStream::println);
         printStream.flush();
      }
   }

   public static void printRootCauseStackTrace(Throwable throwable, PrintWriter printWriter) {
      if (throwable != null) {
         Objects.requireNonNull(printWriter, "printWriter");
         getRootCauseStackTraceList(throwable).forEach(printWriter::println);
         printWriter.flush();
      }
   }

   public static void removeCommonFrames(List<String> causeFrames, List<String> wrapperFrames) {
      Objects.requireNonNull(causeFrames, "causeFrames");
      Objects.requireNonNull(wrapperFrames, "wrapperFrames");
      int causeFrameIndex = causeFrames.size() - 1;

      for (int wrapperFrameIndex = wrapperFrames.size() - 1; causeFrameIndex >= 0 && wrapperFrameIndex >= 0; wrapperFrameIndex--) {
         String causeFrame = causeFrames.get(causeFrameIndex);
         String wrapperFrame = wrapperFrames.get(wrapperFrameIndex);
         if (causeFrame.equals(wrapperFrame)) {
            causeFrames.remove(causeFrameIndex);
         }

         causeFrameIndex--;
      }
   }

   public static <T> T rethrow(Throwable throwable) {
      return eraseType(throwable);
   }

   public static Stream<Throwable> stream(Throwable throwable) {
      return getThrowableList(throwable).stream();
   }

   private static <T extends Throwable> T throwableOf(Throwable throwable, Class<T> type, int fromIndex, boolean subclass) {
      if (throwable != null && type != null) {
         if (fromIndex < 0) {
            fromIndex = 0;
         }

         Throwable[] throwables = getThrowables(throwable);
         if (fromIndex >= throwables.length) {
            return null;
         }

         if (subclass) {
            for (int i = fromIndex; i < throwables.length; i++) {
               if (type.isAssignableFrom(throwables[i].getClass())) {
                  return type.cast(throwables[i]);
               }
            }
         } else {
            for (int i = fromIndex; i < throwables.length; i++) {
               if (type.equals(throwables[i].getClass())) {
                  return type.cast(throwables[i]);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static <T extends Throwable> T throwableOfThrowable(Throwable throwable, Class<T> clazz) {
      return throwableOf(throwable, clazz, 0, false);
   }

   public static <T extends Throwable> T throwableOfThrowable(Throwable throwable, Class<T> clazz, int fromIndex) {
      return throwableOf(throwable, clazz, fromIndex, false);
   }

   public static <T extends Throwable> T throwableOfType(Throwable throwable, Class<T> type) {
      return throwableOf(throwable, type, 0, true);
   }

   public static <T extends Throwable> T throwableOfType(Throwable throwable, Class<T> type, int fromIndex) {
      return throwableOf(throwable, type, fromIndex, true);
   }

   @Deprecated
   public static <T> T throwUnchecked(T throwable) {
      if (throwable instanceof RuntimeException) {
         throw (RuntimeException)throwable;
      } else if (throwable instanceof Error) {
         throw (Error)throwable;
      } else {
         return throwable;
      }
   }

   public static <T extends Throwable> T throwUnchecked(T throwable) {
      if (isUnchecked(throwable)) {
         throw asRuntimeException(throwable);
      } else {
         return throwable;
      }
   }

   public static <R> R wrapAndThrow(Throwable throwable) {
      throw new UndeclaredThrowableException(throwUnchecked(throwable));
   }
}
