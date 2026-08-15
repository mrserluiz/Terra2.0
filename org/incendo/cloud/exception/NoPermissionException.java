package org.incendo.cloud.exception;

import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.permission.PermissionResult;

@API(status = Status.STABLE)
public class NoPermissionException extends CommandParseException {
   private final PermissionResult result;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public NoPermissionException(
      final @NonNull PermissionResult permissionResult, final @NonNull Object commandSender, final @NonNull List<@NonNull CommandComponent<?>> currentChain
   ) {
      super(commandSender, currentChain);
      if (permissionResult.allowed()) {
         throw new IllegalArgumentException("Provided permission result was one that succeeded instead of failed");
      }

      this.result = permissionResult;
   }

   @Override
   public final String getMessage() {
      return String.format("Missing permission '%s'", this.missingPermission());
   }

   @API(status = Status.STABLE)
   public @NonNull Permission missingPermission() {
      return this.result.permission();
   }

   @API(status = Status.STABLE)
   public @NonNull PermissionResult permissionResult() {
      return this.result;
   }

   @Override
   public final synchronized Throwable fillInStackTrace() {
      return this;
   }

   @Override
   public final synchronized Throwable initCause(final Throwable cause) {
      return this;
   }
}
