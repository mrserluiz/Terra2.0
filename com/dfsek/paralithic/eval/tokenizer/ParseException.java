package com.dfsek.paralithic.eval.tokenizer;

import java.util.List;

public class ParseException extends Exception {
   private static final long serialVersionUID = -5618855459424320517L;
   private final transient List<ParseError> errors;

   private ParseException(String message, List<ParseError> errors) {
      super(message);
      this.errors = errors;
   }

   public static ParseException create(List<ParseError> errors) {
      if (errors.size() == 1) {
         return new ParseException(errors.get(0).getMessage(), errors);
      } else {
         return errors.size() > 1
            ? new ParseException(String.format("%d errors occured. First: %s", errors.size(), errors.get(0).getMessage()), errors)
            : new ParseException("An unknown error occured", errors);
      }
   }

   public List<ParseError> getErrors() {
      return this.errors;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();

      for (ParseError error : this.errors) {
         if (sb.length() > 0) {
            sb.append("\n");
         }

         sb.append(error);
      }

      return sb.toString();
   }
}
