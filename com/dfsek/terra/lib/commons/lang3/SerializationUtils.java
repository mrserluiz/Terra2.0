package com.dfsek.terra.lib.commons.lang3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;

public class SerializationUtils {
   public static <T extends Serializable> T clone(T object) {
      if (object == null) {
         return null;
      }

      byte[] objectData = serialize(object);
      ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
      Class<T> cls = ObjectUtils.getClass(object);

      try {
         SerializationUtils.ClassLoaderAwareObjectInputStream in = new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, cls.getClassLoader());

         Serializable var5;
         try {
            var5 = cls.cast(in.readObject());
         } catch (Throwable var8) {
            try {
               in.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         in.close();
         return (T)var5;
      } catch (ClassNotFoundException | IOException ex) {
         throw new SerializationException(String.format("%s while reading cloned object data", ex.getClass().getSimpleName()), ex);
      }
   }

   public static <T> T deserialize(byte[] objectData) {
      Objects.requireNonNull(objectData, "objectData");
      return deserialize(new ByteArrayInputStream(objectData));
   }

   public static <T> T deserialize(InputStream inputStream) {
      Objects.requireNonNull(inputStream, "inputStream");

      try {
         ObjectInputStream in = new ObjectInputStream(inputStream);

         Object var3;
         try {
            T obj = (T)in.readObject();
            var3 = obj;
         } catch (Throwable var5) {
            try {
               in.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         in.close();
         return (T)var3;
      } catch (ClassNotFoundException | IOException | NegativeArraySizeException ex) {
         throw new SerializationException(ex);
      }
   }

   public static <T extends Serializable> T roundtrip(T obj) {
      return deserialize(serialize(obj));
   }

   public static byte[] serialize(Serializable obj) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      serialize(obj, baos);
      return baos.toByteArray();
   }

   public static void serialize(Serializable obj, OutputStream outputStream) {
      Objects.requireNonNull(outputStream, "outputStream");

      try {
         ObjectOutputStream out = new ObjectOutputStream(outputStream);

         try {
            out.writeObject(obj);
         } catch (Throwable var6) {
            try {
               out.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         out.close();
      } catch (IOException ex) {
         throw new SerializationException(ex);
      }
   }

   static final class ClassLoaderAwareObjectInputStream extends ObjectInputStream {
      private final ClassLoader classLoader;

      ClassLoaderAwareObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
         super(in);
         this.classLoader = classLoader;
      }

      @Override
      protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
         String name = desc.getName();

         try {
            return Class.forName(name, false, this.classLoader);
         } catch (ClassNotFoundException ex) {
            try {
               return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException cnfe) {
               Class<?> cls = ClassUtils.getPrimitiveClass(name);
               if (cls != null) {
                  return cls;
               } else {
                  throw cnfe;
               }
            }
         }
      }
   }
}
