package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Throwables;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class Closer implements Closeable {
   @VisibleForTesting
   final Closer.Suppressor suppressor;
   private final Deque<Closeable> stack = new ArrayDeque<>(4);
   private @Nullable Throwable thrown;
   private static final Closer.Suppressor SUPPRESSING_SUPPRESSOR = (closeable, thrown, suppressed) -> {
      if (thrown != suppressed) {
         try {
            thrown.addSuppressed(suppressed);
         } catch (Throwable e) {
            Closeables.logger.log(Level.WARNING, "Suppressing exception thrown when closing " + closeable, suppressed);
         }
      }
   };

   public static Closer create() {
      return new Closer(SUPPRESSING_SUPPRESSOR);
   }

   @VisibleForTesting
   Closer(Closer.Suppressor suppressor) {
      this.suppressor = Preconditions.checkNotNull(suppressor);
   }

   @CanIgnoreReturnValue
   @ParametricNullness
   public <C extends Closeable> C register(@ParametricNullness C closeable) {
      if (closeable != null) {
         this.stack.addFirst(closeable);
      }

      return closeable;
   }

   public RuntimeException rethrow(Throwable e) throws IOException {
      Preconditions.checkNotNull(e);
      this.thrown = e;
      Throwables.throwIfInstanceOf(e, IOException.class);
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
   }

   public <X extends Exception> RuntimeException rethrow(Throwable e, Class<X> declaredType) throws IOException, X {
      Preconditions.checkNotNull(e);
      this.thrown = e;
      Throwables.throwIfInstanceOf(e, IOException.class);
      Throwables.throwIfInstanceOf(e, declaredType);
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
   }

   public <X1 extends Exception, X2 extends Exception> RuntimeException rethrow(Throwable e, Class<X1> declaredType1, Class<X2> declaredType2) throws IOException, X1, X2 {
      Preconditions.checkNotNull(e);
      this.thrown = e;
      Throwables.throwIfInstanceOf(e, IOException.class);
      Throwables.throwIfInstanceOf(e, declaredType1);
      Throwables.throwIfInstanceOf(e, declaredType2);
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
   }

   @Override
   public void close() throws IOException {
      Throwable throwable = this.thrown;

      while (!this.stack.isEmpty()) {
         Closeable closeable = this.stack.removeFirst();

         try {
            closeable.close();
         } catch (Throwable e) {
            if (throwable == null) {
               throwable = e;
            } else {
               this.suppressor.suppress(closeable, throwable, e);
            }
         }
      }

      if (this.thrown == null && throwable != null) {
         Throwables.throwIfInstanceOf(throwable, IOException.class);
         Throwables.throwIfUnchecked(throwable);
         throw new AssertionError(throwable);
      }
   }

   @VisibleForTesting
   interface Suppressor {
      void suppress(Closeable closeable, Throwable thrown, Throwable suppressed);
   }
}
