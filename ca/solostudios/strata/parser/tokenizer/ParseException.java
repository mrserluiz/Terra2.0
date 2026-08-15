package ca.solostudios.strata.parser.tokenizer;

import org.jetbrains.annotations.NotNull;

public final class ParseException extends Exception {
   private static final long serialVersionUID = -2935358064424839548L;
   @NotNull
   private final Position position;

   public ParseException(@NotNull String message, @NotNull String parseString, @NotNull Position position) {
      super(String.format(String.format("%%s\n%%s\n%%%ss", position.getPos() == 0 ? "" : position.getPos()), message, parseString, '^'), null, false, false);
      this.position = position;
   }

   public ParseException(@NotNull String message, @NotNull Position position) {
      super(message, null, true, false);
      this.position = position;
   }

   public ParseException(@NotNull Exception exception, @NotNull String parseString, @NotNull Position position) {
      this(exception.getMessage(), parseString, position);
      this.addSuppressed(exception);
   }

   public ParseException(@NotNull Exception exception, @NotNull Position position) {
      this(exception.getMessage(), position);
      this.addSuppressed(exception);
   }

   @Override
   public String toString() {
      return String.format("%s", this.getMessage());
   }

   @NotNull
   public Position getPosition() {
      return this.position;
   }
}
