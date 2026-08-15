package org.incendo.cloud.services.type;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.services.State;

@FunctionalInterface
public interface ConsumerService<Context> extends SideEffectService<Context>, Consumer<Context> {
   static void interrupt() throws ConsumerService.PipeBurst {
      throw new ConsumerService.PipeBurst();
   }

   @Override
   default @NonNull State handle(final @NonNull Context context) {
      try {
         this.accept(context);
      } catch (ConsumerService.PipeBurst burst) {
         return State.ACCEPTED;
      }

      return State.REJECTED;
   }

   @Override
   void accept(@NonNull Context context);

   final class PipeBurst extends RuntimeException {
      private PipeBurst() {
      }

      @Override
      public synchronized Throwable fillInStackTrace() {
         return this;
      }

      @Override
      public synchronized Throwable initCause(final Throwable cause) {
         return this;
      }
   }
}
