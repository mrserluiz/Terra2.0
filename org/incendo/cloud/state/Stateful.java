package org.incendo.cloud.state;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface Stateful<S extends State> {
   @NonNull S state();

   boolean transitionIfPossible(@NonNull S in, @NonNull S out);

   default void requireState(final @NonNull S expected) {
      if (!this.state().equals(expected)) {
         throw new IllegalStateException(
            String.format("This operation requires the command manager to be in state '%s', but it is in '%s'", expected, this.state())
         );
      }
   }

   default void transitionOrThrow(final @NonNull S in, final @NonNull S out) {
      if (!this.transitionIfPossible(in, out)) {
         throw new IllegalStateException(String.format("The current state is '%s' but we expected a state of '%s' or '%s'", this.state(), in, out));
      }
   }
}
