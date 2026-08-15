package io.leangen.geantyref;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeToken<T> {
   private final AnnotatedType type;
   private volatile AnnotatedType canonical;

   protected TypeToken() {
      this.type = this.extractType();
   }

   private TypeToken(AnnotatedType type) {
      this.type = type;
   }

   public static <T> TypeToken<T> get(Class<T> type) {
      return new TypeToken<T>(GenericTypeReflector.annotate(type)) {};
   }

   public static TypeToken<?> get(Type type) {
      return new TypeToken<Object>(GenericTypeReflector.annotate(type)) {};
   }

   public Type getType() {
      return this.type.getType();
   }

   public AnnotatedType getAnnotatedType() {
      return this.type;
   }

   public AnnotatedType getCanonicalType() {
      if (this.canonical == null) {
         this.canonical = GenericTypeReflector.toCanonical(this.type);
      }

      return this.canonical;
   }

   private AnnotatedType extractType() {
      AnnotatedType t = this.getClass().getAnnotatedSuperclass();
      if (!(t instanceof AnnotatedParameterizedType)) {
         throw new RuntimeException("Invalid TypeToken; must specify type parameters");
      } else {
         AnnotatedParameterizedType pt = (AnnotatedParameterizedType)t;
         if (((ParameterizedType)pt.getType()).getRawType() != TypeToken.class) {
            throw new RuntimeException("Invalid TypeToken; must directly extend TypeToken");
         } else {
            return pt.getAnnotatedActualTypeArguments()[0];
         }
      }
   }

   @Override
   public boolean equals(Object obj) {
      return this == obj ? true : obj instanceof TypeToken && this.getCanonicalType().equals(((TypeToken)obj).type);
   }

   @Override
   public int hashCode() {
      return this.getType().hashCode();
   }
}
