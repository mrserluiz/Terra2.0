package org.incendo.cloud.util;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.INTERNAL)
public final class TypeUtils {
   private TypeUtils() {
   }

   public static String simpleName(final @NonNull Type type) {
      String simpleName = GenericTypeReflector.erase(type).getSimpleName();
      if (type instanceof ParameterizedType) {
         String paramTypes = Arrays.stream(((ParameterizedType)type).getActualTypeArguments()).map(TypeUtils::simpleName).collect(Collectors.joining(", "));
         return simpleName + '<' + paramTypes + '>';
      } else {
         return simpleName;
      }
   }
}
