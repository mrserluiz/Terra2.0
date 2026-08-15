package org.incendo.cloud.context;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

final class CommandInputImpl implements CommandInput {
   private final String input;
   private int cursor;

   CommandInputImpl(final @NonNull String input) {
      this(input, 0);
   }

   CommandInputImpl(final @NonNull String input, final @NonNegative int cursor) {
      this.input = input;
      this.cursor = cursor;
   }

   @Override
   public @NonNull String input() {
      return this.input;
   }

   @Override
   public @NonNull CommandInput appendString(final @NonNull String string) {
      return this.hasRemainingInput() && !this.remainingInput().endsWith(" ")
         ? new CommandInputImpl(String.format("%s %s", this.input, string), this.cursor)
         : new CommandInputImpl(this.input + string, this.cursor);
   }

   @Override
   public @NonNegative int cursor() {
      return this.cursor;
   }

   @Override
   public void moveCursor(final int chars) {
      if (this.cursor() + chars > this.length()) {
         throw new CommandInput.CursorOutOfBoundsException(this.cursor() + chars, this.length());
      }

      this.cursor += chars;
   }

   @Override
   public @This @NonNull CommandInput cursor(final int cursor) {
      if (cursor >= 0 && cursor <= this.length()) {
         this.cursor = cursor;
         return this;
      } else {
         throw new CommandInput.CursorOutOfBoundsException(cursor, this.length());
      }
   }

   @Override
   public @NonNull CommandInput copy() {
      return new CommandInputImpl(this.input, this.cursor);
   }
}
