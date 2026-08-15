package com.dfsek.paralithic.eval.tokenizer;

public class ParseError {
   private final ParseError.Severity severity;
   private final Position pos;
   private final String message;

   protected ParseError(Position pos, String message, ParseError.Severity severity) {
      this.pos = pos;
      this.message = message;
      this.severity = severity;
   }

   public static ParseError warning(Position pos, String msg) {
      String message = msg;
      if (pos.getLine() > 0) {
         message = String.format("%3d:%2d: %s", pos.getLine(), pos.getPos(), msg);
      }

      return new ParseError(pos, message, ParseError.Severity.WARNING);
   }

   public static ParseError error(Position pos, String msg) {
      String message = msg;
      if (pos.getLine() > 0) {
         message = String.format("%3d:%2d: %s", pos.getLine(), pos.getPos(), msg);
      }

      return new ParseError(pos, message, ParseError.Severity.ERROR);
   }

   public Position getPosition() {
      return this.pos;
   }

   public String getMessage() {
      return this.message;
   }

   public ParseError.Severity getSeverity() {
      return this.severity;
   }

   @Override
   public String toString() {
      return String.format("%s %s", this.severity, this.message);
   }

   public enum Severity {
      WARNING,
      ERROR;
   }
}
