package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public class FinalizableReferenceQueue implements Closeable {
   private static final Logger logger = Logger.getLogger(FinalizableReferenceQueue.class.getName());
   private static final String FINALIZER_CLASS_NAME = "com.dfsek.terra.lib.google.common.base.internal.Finalizer";
   private static final Method startFinalizer;
   final ReferenceQueue<Object> queue = new ReferenceQueue<>();
   final PhantomReference<Object> frqRef = new PhantomReference<>(this, this.queue);
   final boolean threadStarted;

   public FinalizableReferenceQueue() {
      boolean threadStarted = false;

      try {
         startFinalizer.invoke(null, FinalizableReference.class, this.queue, this.frqRef);
         threadStarted = true;
      } catch (IllegalAccessException impossible) {
         throw new AssertionError(impossible);
      } catch (Throwable t) {
         logger.log(Level.INFO, "Failed to start reference finalizer thread. Reference cleanup will only occur when new references are created.", t);
      }

      this.threadStarted = threadStarted;
   }

   @Override
   public void close() {
      this.frqRef.enqueue();
      this.cleanUp();
   }

   void cleanUp() {
      if (!this.threadStarted) {
         Reference<?> reference;
         while ((reference = this.queue.poll()) != null) {
            reference.clear();

            try {
               ((FinalizableReference)reference).finalizeReferent();
            } catch (Throwable t) {
               logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
            }
         }
      }
   }

   private static Class<?> loadFinalizer(FinalizableReferenceQueue.FinalizerLoader... loaders) {
      for (FinalizableReferenceQueue.FinalizerLoader loader : loaders) {
         Class<?> finalizer = loader.loadFinalizer();
         if (finalizer != null) {
            return finalizer;
         }
      }

      throw new AssertionError();
   }

   static Method getStartFinalizer(Class<?> finalizer) {
      try {
         return finalizer.getMethod("startFinalizer", Class.class, ReferenceQueue.class, PhantomReference.class);
      } catch (NoSuchMethodException e) {
         throw new AssertionError(e);
      }
   }

   static {
      Class<?> finalizer = loadFinalizer(
         new FinalizableReferenceQueue.SystemLoader(), new FinalizableReferenceQueue.DecoupledLoader(), new FinalizableReferenceQueue.DirectLoader()
      );
      startFinalizer = getStartFinalizer(finalizer);
   }

   static class DecoupledLoader implements FinalizableReferenceQueue.FinalizerLoader {
      private static final String LOADING_ERROR = "Could not load Finalizer in its own class loader. Loading Finalizer in the current class loader instead. As a result, you will not be able to garbage collect this class loader. To support reclaiming this class loader, either resolve the underlying issue, or move Guava to your system class path.";

      @Override
      public @Nullable Class<?> loadFinalizer() {
         try {
            URLClassLoader finalizerLoader = this.newLoader(this.getBaseUrl());

            Class var2;
            try {
               var2 = finalizerLoader.loadClass("com.dfsek.terra.lib.google.common.base.internal.Finalizer");
            } catch (Throwable var5) {
               if (finalizerLoader != null) {
                  try {
                     finalizerLoader.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (finalizerLoader != null) {
               finalizerLoader.close();
            }

            return var2;
         } catch (Exception e) {
            FinalizableReferenceQueue.logger
               .log(
                  Level.WARNING,
                  "Could not load Finalizer in its own class loader. Loading Finalizer in the current class loader instead. As a result, you will not be able to garbage collect this class loader. To support reclaiming this class loader, either resolve the underlying issue, or move Guava to your system class path.",
                  e
               );
            return null;
         }
      }

      URL getBaseUrl() throws IOException {
         String finalizerPath = "com.dfsek.terra.lib.google.common.base.internal.Finalizer".replace('.', '/') + ".class";
         URL finalizerUrl = this.getClass().getClassLoader().getResource(finalizerPath);
         if (finalizerUrl == null) {
            throw new FileNotFoundException(finalizerPath);
         }

         String urlString = finalizerUrl.toString();
         if (!urlString.endsWith(finalizerPath)) {
            throw new IOException("Unsupported path style: " + urlString);
         }

         urlString = urlString.substring(0, urlString.length() - finalizerPath.length());
         return new URL(finalizerUrl, urlString);
      }

      URLClassLoader newLoader(URL base) {
         return new URLClassLoader(new URL[]{base}, null);
      }
   }

   static class DirectLoader implements FinalizableReferenceQueue.FinalizerLoader {
      @Override
      public Class<?> loadFinalizer() {
         try {
            return Class.forName("com.dfsek.terra.lib.google.common.base.internal.Finalizer");
         } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
         }
      }
   }

   interface FinalizerLoader {
      @Nullable Class<?> loadFinalizer();
   }

   static class SystemLoader implements FinalizableReferenceQueue.FinalizerLoader {
      @VisibleForTesting
      static boolean disabled;

      @Override
      public @Nullable Class<?> loadFinalizer() {
         if (disabled) {
            return null;
         }

         ClassLoader systemLoader;
         try {
            systemLoader = ClassLoader.getSystemClassLoader();
         } catch (SecurityException e) {
            FinalizableReferenceQueue.logger.info("Not allowed to access system class loader.");
            return null;
         }

         if (systemLoader != null) {
            try {
               return systemLoader.loadClass("com.dfsek.terra.lib.google.common.base.internal.Finalizer");
            } catch (ClassNotFoundException e) {
               return null;
            }
         } else {
            return null;
         }
      }
   }
}
