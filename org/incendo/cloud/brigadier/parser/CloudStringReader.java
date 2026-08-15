package org.incendo.cloud.brigadier.parser;

import com.mojang.brigadier.StringReader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandInput;

final class CloudStringReader extends StringReader {
   private final CommandInput commandInput;

   static @NonNull CloudStringReader of(final @NonNull CommandInput commandInput) {
      return new CloudStringReader(commandInput);
   }

   private CloudStringReader(final @NonNull CommandInput commandInput) {
      super(commandInput.input());
      this.commandInput = commandInput;
      super.setCursor(commandInput.cursor());
   }

   public void setCursor(final int cursor) {
      super.setCursor(cursor);
      this.commandInput.cursor(cursor);
   }

   public char read() {
      super.read();
      return this.commandInput.read();
   }

   public void skip() {
      super.skip();
      this.commandInput.moveCursor(1);
   }
}
